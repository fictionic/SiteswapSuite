package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class ArgTree {
	// global options
	// Set<String> debugClasses;
	// List<ArgChain> argChains;

	public static ArgTree parseArgTree(String[] args) throws ParseError {
		List<ArgParseResult> flatList = parseArgsToFlatList(args);
		return null;
	}

	private static List<ArgParseResult> parseArgsToFlatList(String[] args) throws ParseError {
		List<ArgParseResult> flatList = new ArrayList<>();
		for(int i=0; i<args.length; i++) {
			String arg = args[i];
			ArgParseResult parsedArg = ArgParseResult.parse(arg);
			// add follow-up if required and present
			Argument.Requires requires = parsedArg.head.arg.requires;
			if(requires == Argument.Requires.REQUIRES_INT) {
				try {
					parsedArg.head.followUpInt = Integer.parseInt(args[++i]);
				} catch(NumberFormatException e) {
					throw new ParseError("follow-up '" + args[i-1] + "' cannot be coerced into an integer");
				} catch(ArrayIndexOutOfBoundsException e) {
					throw new ParseError("argument '" + args[i-1] + "' requires integer follow-up");
				}
			} else if(requires == Argument.Requires.REQUIRES_STRING) {
				try {
					parsedArg.head.followUpString = args[++i];
				} catch(ArrayIndexOutOfBoundsException e) {
					throw new ParseError("argument '" + args[i-1] + "' requires string follow-up");
				}
			}
			flatList.add(parsedArg);
		}
		return flatList;
	}

	public static void main(String[] args) {
		try {
			List<ArgParseResult> stuff = parseArgsToFlatList(args);
			System.out.println(stuff);
		} catch(ParseError e) {
			System.err.println(e);
		}
	}

}
