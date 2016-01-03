public class NotatedSiteswap extends Siteswap {

    private final String inputNotation;
    private final NotationType notationType;

    public NotationType notationType() {
	return this.notationType;
    }

    public String inputNotation() {
	return this.inputNotation;
    }

    /* siteswap regex patterns:
       toss = "(-?(\\d|[a-z]|&)x?)"
       hand = "(toss|\[toss+\])+"
       asyncSiteswap = "hand+"
       syncBeat = "\(hand,hand\)!?"
       syncSiteswap = "(beat+)\\*?"
       mixedSiteswap = "(toss|beat)+"
     */
    private static final String validAsyncSiteswapString = "((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\])+";
    private static final String validSynchronousSiteswapString = "(\\(((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\]),((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\])\\)!?)+\\*?";
    private static final String validMixedNotationTwoHandedSiteswapString = "(((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\])|(\\(((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\]),((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\])\\)!?))+";
    //no star notation on mixed, because it would be ambiguous as to whether the whole pattern is starred or just the most recent sync part
    private static final String validMultipleJugglerSiteswapString = ""; //later...

    public static NotationType getNotationType(String s) {
	if(Pattern.matches(validAsyncSiteswapString, s)) {
	    return NotationType.ASYNCHRONOUS;
	} else if(Pattern.matches(validSynchronousSiteswapString, s)) {
	    return NotationType.SYNCHRONOUS;
	} else if(Pattern.matches(validMixedNotationTwoHandedSiteswapString, s)) {
	    return NotationType.MIXED;
	} else {
	    return null;
	}
    }

    public NotatedSiteswap(String inputNotation) {
	this.inputNotation = inputNotation;
	// determine how we should parse the input, then parse it that way
	this.notationType = getNotationType(inputNotation);
    }
}
