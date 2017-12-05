package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class ArgParseResult {
	ArgContainer head;
	List<ArgContainer> tail;

	ArgParseResult() {
		this.tail = new ArrayList<>();
	}

	static ArgParseResult parse(String argString) throws ParseError {
		ArgParseResult parseResult = new ArgParseResult();
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
			headArg = Argument.parseShortOptionName(headStr.charAt(0));
		}
		parseResult.head = new ArgContainer(headArg);
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
			for(String subArg : optionsStr.split(",")) {
				// parse inline arguments to options
				int sepIndex = subArg.indexOf('=');
				String inlineFollowUp = null;
				if(sepIndex > -1) {
					String[] subArgSplit = subArg.split("=", 2);
					subArg = subArgSplit[0];
					inlineFollowUp = subArgSplit[1];
				}
				ArgContainer curArg;
				Argument curArgHead;
				if(subArg.length() == 1) {
					curArgHead = Argument.parseShortOptionName(subArg.charAt(0));
				} else {
					curArgHead = Argument.parseLongOptionName(subArg);
				}
				// check role
				if(curArgHead.ownRole != optionsRole) {
					throw new ParseError("argument '" + subArg + "' is not a valid option for '" + headStrFull + "'");
				}
				// add inline follow-up if required and present
				if(curArgHead.requires == Argument.FollowUp.STRING) {
					if(inlineFollowUp == null) {
						throw new ParseError("argument '" + subArg + "' requires string follow-up");
					}
					curArg = new ArgContainer(curArgHead, inlineFollowUp);
				} else if(curArgHead.requires == Argument.FollowUp.INT) {
					if(inlineFollowUp == null) {
						throw new ParseError("argument '" + subArg + "' requires integer follow-up");
					}
					curArg = new ArgContainer(curArgHead, Integer.parseInt(inlineFollowUp));
				} else {
					curArg = new ArgContainer(curArgHead);
				}
				parseResult.tail.add(curArg);
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
			for(ArgContainer option : this.tail) {
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
