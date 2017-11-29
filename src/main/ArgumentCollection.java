package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class ArgumentCollection {
	Argument head;
	List<Argument> options;
	List<Integer> ints;
	List<String> strings;
	String followUpString;
	int followUpInt;

	private ArgumentCollection() {
		this.head = Argument.INVALID_TOKEN;
		this.options = new ArrayList<>();
		this.ints = new ArrayList<>();
		this.strings = new ArrayList<>();
	}

	static ArgumentCollection parse(String str) {
		ArgumentCollection ret = new ArgumentCollection();
		boolean isLongOption;
		// for long options with inline arguments
		String headStr;
		String optionsStr = null;
		if(str.charAt(0) == '-') {
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
			return ret;
		}
		// parse headStr
		if(isLongOption) {
			ret.head = Argument.parseLongOptionName(headStr);
		} else {
			ret.head = Argument.parseShortOptionName(str.charAt(0));
		}
		// parse optionsStr
		if(optionsStr != null && optionsStr.length() > 0) {
			for(String subArg : optionsStr.split(",")) {
				// parse inline arguments to options
				int sepIndex = subArg.indexOf('=');
				String inlineArg = null;
				if(sepIndex > -1) {
					String[] subArgSplit = subArg.split("=", 2);
					subArg = subArgSplit[0];
					inlineArg = subArgSplit[1];
				}
				Argument curArg;
				if(subArg.length() == 1) {
					curArg = Argument.parseShortOptionName(subArg.charAt(0));
				} else {
					curArg = Argument.parseLongOptionName(subArg);
				}
				ret.options.add(curArg);
				// add inline arg if present
				if(curArg.requires == Argument.Requires.REQUIRES_STRING) {
					ret.options.add(Argument.LITERAL_STRING);
					ret.strings.add(inlineArg);
				} else if(curArg.requires == Argument.Requires.REQUIRES_INT) {
					ret.options.add(Argument.LITERAL_INT);
					int intArg = Integer.parseInt(inlineArg);
					ret.ints.add(intArg);
				}
			}
		}
		return ret;
	}

	// for debugging
	public String toString() {
		String ret = "";
		ret += this.head.toString();
		if(this.head.requires == Argument.Requires.REQUIRES_INT) {
			ret += " " + this.followUpInt;
		} else if(this.head.requires == Argument.Requires.REQUIRES_STRING) {
			ret += " " + this.followUpString;
		}
		int intIndex = 0;
		int stringIndex = 0;
		for(Argument arg : this.options) {
			if(arg == Argument.LITERAL_INT) {
				ret += " " + this.ints.get(intIndex++);
			} else if(arg == Argument.LITERAL_STRING) {
				ret += " " + this.strings.get(stringIndex++);
			} else {
				ret += " " + arg.toString();
			}
		}
		return ret;
	}
}
