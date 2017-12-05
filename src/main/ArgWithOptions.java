package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class ArgWithOptions {
	ArgWithFollowUp head;
	List<ArgWithFollowUp> tail;

	ArgWithOptions() {
		this.tail = new ArrayList<>();
	}

	static ArgWithOptions parse(String argString) throws ParseError {
		ArgWithOptions parseResult = new ArgWithOptions();
		boolean isLongOption;
		String headStr;
		String optionsStr;
		// make sure it's an argument
		if(argString.charAt(0) == '-') {
			String argStringStripped;
			// see if it's a long name
			if(argString.charAt(1) == '-') {
				isLongOption = true;
				argStringStripped = argString.substring(2, argString.length());
			} else {
				isLongOption = false;
				argStringStripped = argString.substring(1, argString.length());
			}
			// strip out inline options
			int sepIndex = argStringStripped.indexOf(':');
			if(sepIndex > -1) {
				headStr = argStringStripped.substring(0, sepIndex);
				optionsStr = argStringStripped.substring(sepIndex+1, argStringStripped.length());
			} else {
				headStr = argStringStripped;
				optionsStr = null;
			}
		} else {
			// invalid token
			throw new ParseError("invalid token: '" + argString + "'");
		}
		// parse headStr
		Argument headArg;
		if(isLongOption) {
			headArg = Argument.parseLongOptionName(headStr);
		} else {
			// make sure it's only 1 char long
			if(headStr.length() > 1) {
				throw new ParseError("invalid token: '" + argString + "'");
			}
			headArg = Argument.parseShortOptionName(headStr.charAt(0));
		}
		parseResult.head = new ArgWithFollowUp(headArg);
		// parse optionsStr
		if(optionsStr != null && optionsStr.length() > 0) {
			// for error printing (yes this is a stupid way of getting this string)
			String headStrFull;
			if(isLongOption) {
				headStrFull = "--" + headStr;
			} else {
				headStrFull = "-" + headStr;
			}
			// make sure the head arg allows options
			if(parseResult.head.arg.optionsRole == null) {
				throw new ParseError("argument '" + headStrFull + "' does not take options");
			}
			// get role that all options must have
			Argument.Role optionsRole = parseResult.head.arg.optionsRole;
			for(String subArgStr : optionsStr.split(",")) {
				// parse inline arguments to options
				int sepIndex = subArgStr.indexOf('=');
				String subArgStrHead;
				String inlineFollowUp;
				if(sepIndex > -1) {
					String[] subArgSplit = subArgStr.split("=", 2);
					subArgStrHead = subArgSplit[0];
					inlineFollowUp = subArgSplit[1];
				} else {
					subArgStrHead = subArgStr;
					inlineFollowUp = null;
				}
				ArgWithFollowUp subArg;
				Argument subArgHead;
				if(subArgStrHead.length() == 1) {
					subArgHead = Argument.parseShortOptionName(subArgStrHead.charAt(0));
				} else {
					subArgHead = Argument.parseLongOptionName(subArgStrHead);
				}
				// check role
				if(subArgHead.ownRole != optionsRole) {
					throw new ParseError("argument '" + subArgStrHead + "' is not a valid option for '" + headStrFull + "'");
				}
				// add inline follow-up if required and present
				if(subArgHead.requires == Argument.FollowUp.STRING) {
					if(inlineFollowUp == null) {
						throw new ParseError("argument '" + subArgStrHead + "' requires string follow-up");
					}
					subArg = new ArgWithFollowUp(subArgHead, inlineFollowUp);
				} else if(subArgHead.requires == Argument.FollowUp.INT) {
					if(inlineFollowUp == null) {
						throw new ParseError("argument '" + subArgStrHead + "' requires integer follow-up");
					}
					subArg = new ArgWithFollowUp(subArgHead, Integer.parseInt(inlineFollowUp));
				} else {
					if(inlineFollowUp != null) {
						throw new ParseError("argument '" + subArgStrHead + "' takes no follow-up");
					}
					subArg = new ArgWithFollowUp(subArgHead);
				}
				parseResult.tail.add(subArg);
			}
		}
		return parseResult;
	}

	// for debugging
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(this.head.arg.toString());
		if(this.tail.size() > 0) {
			ret.append(":");
			for(ArgWithFollowUp option : this.tail) {
				ret.append(option.toString());
				ret.append(",");
			}
			ret.deleteCharAt(ret.length()-1);
		}
		Argument.FollowUp requires = this.head.arg.requires;
		if(requires == Argument.FollowUp.INT) {
			ret.append(" ");
			ret.append(this.head.followUpInt);
		} else if(requires == Argument.FollowUp.STRING) {
			ret.append(" ");
			ret.append(this.head.followUpString);
		}
		return ret.toString();
	}
}
