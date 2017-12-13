package siteswapsuite;

import java.util.regex.Pattern;

public enum SiteswapNotation {

	EMPTY(0), ONEHANDED(1), TWOHANDED(2);

	private int defaultNumHands;

	SiteswapNotation(int numHands) {
		this.defaultNumHands = numHands;
	}

	public int defaultNumHands() {
		return this.defaultNumHands;
	}

	public static SiteswapNotation defaultNotationType(int numHands) {
		switch(numHands) {
			case 0:
				return EMPTY;
			case 1:
				return ONEHANDED;
			case 2:
				return TWOHANDED;
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
	// the empty pattern (need a way of notating it without the empty string, for printing)
	static final String emptyNotation = "(||\\.)";
	public static final String emptyNotationPrint = ".";

	// basics
	static final String modifier = "(-?_?|_?-?)";
	static final String magnitude = "(\\d|[a-z]|&)";

	// async (one-handed)
	static final String asyncToss = "(" + modifier + magnitude + ")";
	static final String asyncMux = "(" + asyncToss + "|\\[" + asyncToss + "*\\])";
	static final String validAsyncNotation = asyncMux + "+";

	// sync (two-handed)
	static final String syncToss = "(" + modifier + magnitude + "x?)";
	static final String syncHand = "(" + syncToss + "|\\[" + syncToss + "*\\])";
	static final String syncBeat = "(\\(" + syncHand + "," + syncHand + "\\)!?)";
	static final String validSyncNotation = syncBeat + "+\\*?";

	// mixed (two-handed)
	static final String validMixedNotation = "(" + syncHand + "|" + syncBeat + ")+";
	//no star notation on mixed, because it would be ambiguous as to whether the whole pattern is starred or just the most recent sync part

	// passing (two two-handed jugglers)
	static final String validPassingNotation = ""; //later...

	// for deparsing
	protected static String reverseThrowHeight(Toss t) {
		String toReturn = "";
		if(t == null) {
			return "0"; // null is passed when there is no toss
		}
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

