package siteswapsuite;

import java.util.List;

public class TransitionFinder {

	public static void printf(Object input) { System.out.println(input); }

	/*
	   IDEAS

	   -have commandline arguments for forcing it to parse a string as a ss w/ a particular number of hands
	   -and for starting with a particular hand

	 */

	private static void analyze(NotatedSiteswap ss) {
		printf("parsed: " + ss.toString());
		printf("de-parsed: " + ss.print());
		printf("number of balls: " + ss.numBalls());
		printf("valid: " + ss.isValid());
		printf("period: " + ss.period());
		printf("state: " + new State(ss));
	}

	public static void main(String[] args) {
		if(args.length == 1) {
			try {
				NotatedSiteswap ss = NotatedSiteswap.parseSingle(args[0]);
				analyze(ss);
			} catch(InvalidNotationException e) {
				printf("invalid notation");
			}
		} else if(args.length == 2) {
			try {
				NotatedSiteswapTransition t = NotatedSiteswapTransition.parseStrings(args[0], args[1], 0, false, false);
				NotatedSiteswap from = t.prefix();
				NotatedSiteswap to = t.suffix();
				analyze(from);
				printf("-----");
				analyze(to);
				printf("-----");
				printf("TRANSITION:");
				printf(t.transition().print());
			} catch(InvalidNotationException e) {
				printf("invalid notation");
			} catch(IncompatibleNotationException e) {
				printf("incompatible notations");
			}
		}
	}
}
