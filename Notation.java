package siteswapsuite;

import java.util.regex.Pattern;

class InvalidNotationException extends Exception {}

public enum Notation { 

	EMPTY(0), ASYNCHRONOUS(1), SYNCHRONOUS(2), MIXED(2), PASSING(4);

	private int defaultNumHands;

	Notation(int numHands) {
		this.defaultNumHands = numHands;
	}

	public int defaultNumHands() {
		return this.defaultNumHands;
	}

	public static Notation defaultNotationType(int numHands) {
		switch(numHands) {
			case 0:
				return EMPTY;
			case 1:
				return ASYNCHRONOUS;
			case 2:
				return SYNCHRONOUS;
			case 4:
				return PASSING;
			default:
				return null;
		}
	}

	/* ------- */
	/* PARSING */
	/* ------- */

	// convert character to height of a throw
	protected static ExtendedInteger throwHeight(char c) {
		String h = ((Character)c).toString();
		if(Pattern.matches("\\d", h)) {
			return new ExtendedInteger(Integer.parseInt(h));
		} else if(Pattern.matches("([a-z])", h)) {
			return new ExtendedInteger((int)(h.toCharArray()[0]) - 87);
		} else { //must be '&'
			return new ExtendedInteger(InfinityType.POSITIVE_INFINITY);
		}
	}

	/* siteswap regex patterns */
	// the empty pattern (need some way of notating it without the empty string, cuz that's never passed on the commandline)
	static final String emptyNotation = "\\.";

	// basics
	static final String modifier = "(-?_?|_?-?)";
	static final String magnitude = "(\\d|[a-z]|&)";

	// async (one-handed)
	static final String asyncToss = "(" + modifier + magnitude + ")";
	static final String asyncMux = "(" + asyncToss + "|\\[" + asyncToss + "+\\])";
	static final String validAsyncNotation = asyncMux + "+";

	// sync (two-handed)
	static final String syncToss = "(" + modifier + magnitude + "x?)";
	static final String syncHand = "(" + syncToss + "|\\[" + syncToss + "+\\])";
	static final String syncBeat = "(\\(" + syncHand + "," + syncHand + "\\)!?)";
	static final String validSyncNotation = syncBeat + "+\\*?";

	// mixed (two-handed)
	static final String validMixedNotation = "(" + syncToss + "|" + syncBeat + ")+";
	//no star notation on mixed, because it would be ambiguous as to whether the whole pattern is starred or just the most recent sync part

	// passing (two two-handed jugglers)
	static final String validPassingNotation = ""; //later...

	// put them all together!
	public static Notation analyze(String s) throws InvalidNotationException {
		if(Pattern.matches(emptyNotation, s))
			return Notation.EMPTY;
		if(Pattern.matches(validAsyncNotation, s))
			return Notation.ASYNCHRONOUS;
		else if(Pattern.matches(validSyncNotation, s))
			return Notation.SYNCHRONOUS;
		else if(Pattern.matches(validMixedNotation, s))
			return Notation.MIXED;
		else if(Pattern.matches(validPassingNotation, s))
			return Notation.PASSING;
		else {
			throw new InvalidNotationException();
		}
	}

	// for deparsing
	protected static String reverseThrowHeight(Toss t) {
		String toReturn = "";
		ExtendedInteger H = t.height();
		if(H.sign() < 0)
			toReturn += "-";
		if(t.charge() < 0)
			toReturn += "_";
		if(H.isInfinite()) {
			toReturn += "&";
			return toReturn;
		}
		Integer h = Math.abs(H.finiteValue());
		if(h <= 9) {
			toReturn += h.toString();
		} else if((10 <= h) && (h <= 36)) {
			toReturn += Character.toString((char)(h - 10 + 97));
		} else {
			// eventually come up with a better solution?
			toReturn += "{" + h.toString() + "}";
		}
		return toReturn;
	}

}

