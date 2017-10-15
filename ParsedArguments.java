package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

class ParsedArguments {
	Argument head;
	List<Argument> tail;
	List<Integer> ints;
	List<String> strings;
	String followUpString;
	int followUpInt;
	private ParsedArguments() {
		this.head = Argument.INVALID_TOKEN;
		this.tail = new ArrayList<>();
		this.ints = new ArrayList<>();
		this.strings = new ArrayList<>();
	}
	static ParsedArguments parse(String str) {
		ParsedArguments ret = new ParsedArguments();
		boolean isLongOption;
		// for long options with inline arguments
		String headStr;
		String tailStr = null;
		if(str.length() == 2 && str.charAt(0) == '-') {
			isLongOption = false;
			headStr = str;
		} else if(str.charAt(0) == '-' && str.charAt(1) == '-') {
			isLongOption = true;
			str = str.substring(2, str.length());
			// strip out inline options
			int sepIndex = str.indexOf(':');
			if(sepIndex > -1) {
				headStr = str.substring(0, sepIndex);
				tailStr = str.substring(sepIndex+1, str.length());
			} else {
				headStr = str;
			}
		} else {
			return ret;
		}
		// parse headStr
		if(isLongOption) {
			ret.head = Argument.parseLongOptionName(headStr);
		} else {
			ret.head = Argument.parseShortOptionName(str.charAt(1));
		}
		// parse tailStr
		if(tailStr != null && tailStr.length() > 0) {
			for(String subArg : tailStr.split(",")) {
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
				ret.tail.add(curArg);
				// add inline arg if present
				if(curArg.requires == Argument.Requires.REQUIRES_STRING) {
					ret.tail.add(Argument.LITERAL_STRING);
					ret.strings.add(inlineArg);
				} else if(curArg.requires == Argument.Requires.REQUIRES_INT) {
					ret.tail.add(Argument.LITERAL_INT);
					int intArg = Integer.parseInt(inlineArg);
					ret.ints.add(intArg);
				}
			}
		}
		return ret;
	}
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
		for(Argument arg : this.tail) {
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


