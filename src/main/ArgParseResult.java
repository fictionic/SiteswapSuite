package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class ArgParseResult {
	ArgContainer head;
	List<ArgContainer> tail;

	ArgParseResult() {
		this.tail = new ArrayList<>();
	}

	static ArgParseResult parse(String str) throws ParseError {
		ArgParseResult ret = new ArgParseResult();
		boolean isLongOption;
		String headStr;
		String optionsStr = null;
		// make sure it's an argument
		if(str.charAt(0) == '-') {
			// see if it's a long name
			if(str.charAt(1) == '-') {
				isLongOption = true;
				str = str.substring(2, str.length());
			} else {
				isLongOption = false;
				str = str.substring(1, str.length());
			}
			// strip out inline options
			int sepIndex = str.indexOf(':');
			if(sepIndex > -1) {
				headStr = str.substring(0, sepIndex);
				optionsStr = str.substring(sepIndex+1, str.length());
			} else {
				headStr = str;
			}
		} else {
			// invalid token
			throw new ParseError("invalid token: '" + str + "'");
		}
		// parse headStr
		if(isLongOption) {
			ret.head = new ArgContainer(Argument.parseLongOptionName(headStr));
		} else {
			ret.head = new ArgContainer(Argument.parseShortOptionName(str.charAt(0)));
		}
		// parse optionsStr
		if(optionsStr != null && optionsStr.length() > 0) {
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
				// add inline follow-up if required and present
				if(curArgHead.requires == Argument.Requires.REQUIRES_STRING) {
					if(inlineFollowUp == null) {
						throw new ParseError("argument '" + subArg + "' requires string follow-up");
					}
					curArg = new ArgContainer(curArgHead, inlineFollowUp);
				} else if(curArgHead.requires == Argument.Requires.REQUIRES_INT) {
					if(inlineFollowUp == null) {
						throw new ParseError("argument '" + subArg + "' requires integer follow-up");
					}
					curArg = new ArgContainer(curArgHead, Integer.parseInt(inlineFollowUp));
				} else {
					curArg = new ArgContainer(curArgHead);
				}
				ret.tail.add(curArg);
			}
		}
		return ret;
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
		Argument.Requires requires = this.head.arg.requires;
		if(requires == Argument.Requires.REQUIRES_INT) {
			ret.append(" ");
			ret.append(this.head.followUpInt);
		} else if(requires == Argument.Requires.REQUIRES_STRING) {
			ret.append(" ");
			ret.append(this.head.followUpString);
		}
		return ret.toString();
	}
}
