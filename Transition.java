package siteswapsuite;

abstract class Transition extends MutableSiteswap {

	private int firstCatchIndex = -1;
	
	static Transition compute(Siteswap from, Siteswap to, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) {
		return compute(new State(from), new State(to), allowExtraSqueezeCatches, generateBallAntiballPairs);
	}

	static Transition compute(State from, State to, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) {
		// determine which subclass constructor to call
		if(allowExtraSqueezeCatches) {
			if(generateBallAntiballPairs)
				return new OneBeatTransition(from, to);
			else
				return new AllowExtraSqueezeCatches(from, to);
		} else {
			if(generateBallAntiballPairs)
				return new GenerateBallAntiballPairs(from, to);
			else
				return new StandardTransition(from, to);
		}
	}

	// link to Siteswap() constructor for subclasses
	private Transition(State from, State to) {
		super(from.numHands());
	}

	private static class StandardTransition extends Transition {
		private StandardTransition(State from, State to) {
			super(from, to);
		}
	}

	private static class AllowExtraSqueezeCatches extends Transition {
		private AllowExtraSqueezeCatches(State from, State to) {
			super(from, to);
		}
	}

	private static class GenerateBallAntiballPairs extends Transition {
		private GenerateBallAntiballPairs(State from, State to) {
			super(from, to);
		}
	}

	private static class OneBeatTransition extends Transition {
		private OneBeatTransition(State from, State to) {
			super(from, to);
		}
	}

}
