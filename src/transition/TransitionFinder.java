package siteswapsuite;

public class TransitionFinder {

	private State prefix;
	private State suffix;

	public TransitionFinder(State prefix, State suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public TransitionResults findTransitions(int minLength, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) throws ImpossibleTransitionException {
		return null;
	}

}
