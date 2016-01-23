package siteswapsuite;

abstract class Transition extends MutableSiteswap {

	private static final boolean debug = true;
	private static void printf(Object toPrint) {
		if(debug) {
			if(toPrint == null)
				System.out.println("{null}");
			else
				System.out.println(toPrint);
		}
	}

	int firstCatchIndex = -1;
	
	static Transition compute(State from, State to, int minLength, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) {
		// first check that the states are finite, otherwise there won't be a transition
		if(!from.isFinite() || !to.isFinite()) {
			System.out.println("error: cannot compute transition between non-finite states");
			return null;
		}

		// make copies of the states, so as not to muck up the originals
		State fromCopy = from.deepCopy();
		State toCopy = to.deepCopy();

		// equalize the state lengths
		if(fromCopy.finiteLength() < toCopy.finiteLength())
			fromCopy.getFiniteNode(toCopy.finiteLength() - 1);
		else if (fromCopy.finiteLength() > toCopy.finiteLength())
			toCopy.getFiniteNode(fromCopy.finiteLength() - 1);

		// determine which subclass constructor to call
		if(allowExtraSqueezeCatches) {
			if(generateBallAntiballPairs)
				return new OneBeatTransition(fromCopy, toCopy); // minLength is irrelevant here, as it will always end up being one beat, then unAntitossified
			else
				return new AllowExtraSqueezeCatches(fromCopy, toCopy, minLength);
		} else {
			if(generateBallAntiballPairs)
				return new GenerateBallAntiballPairs(fromCopy, toCopy, minLength);
			else
				return new StandardTransition(fromCopy, toCopy, minLength);
		}
	}

	// link to Siteswap() constructor for subclasses
	private Transition(int numHands) { super(numHands); }

	private static class StandardTransition extends Transition {
		private StandardTransition(State from, State to, int minLength) {
			super(from.numHands());

			int tossSumPositive = 0;
			int tossSumNegative = 0;
			State.DiffSum diffs;
			int b = 0;

			printf("s1: " + from.toString());
			printf("s2: " + to.toString());

			diffs = from.diffSums(to);
			printf(diffs);

			printf("this: ");
			printf(this);


			int debugCounter = 10;

			// find the transition!
			while(diffs.positive != tossSumPositive || diffs.negative != tossSumNegative) {
				// see if we can make tosses
				// (if the nowNode of other is empty
				if(to.nowNodeIsEmpty()) {
					this.appendEmptyBeat();
					// make tosses
					for(int h=0; h<numHands; h++) {
						int chargeAtHand = from.getChargeAtBeatAtHand(0, h);
						while(chargeAtHand > 0) {
							this.addInfiniteToss(b, h, InfinityType.POSITIVE_INFINITY);
							tossSumPositive++;
							chargeAtHand--;
						}
						while(chargeAtHand < 0) {
							this.addInfiniteAntitoss(b, h, InfinityType.POSITIVE_INFINITY);
							tossSumNegative--;
							chargeAtHand++;
						}
					}
					from.advanceTime();
					to.advanceTime();
					b++;
				} else {
					// shift goal state backward by one beat, and match lengths
					to.shiftBackward();
					from.getFiniteNode(to.finiteLength() - 1);
				}

				printf("s1: " + from.toString());
				printf("s2: " + to.toString());
				diffs = from.diffSums(to);
				printf(diffs);
				printf("tosP: " + tossSumPositive);
				printf("tosN: " + tossSumNegative);
				printf("this: ");
				printf(this);
				debugCounter--;
				if(debugCounter == 0)
					break;
			}

			this.firstCatchIndex = b;

			// find catches!

		}
	}

	private static class AllowExtraSqueezeCatches extends Transition {
		private AllowExtraSqueezeCatches(State from, State to, int minLength) {
			super(from.numHands());
		}
	}

	private static class GenerateBallAntiballPairs extends Transition {
		private GenerateBallAntiballPairs(State from, State to, int minLength) {
			super(from.numHands());
		}
	}

	private static class OneBeatTransition extends Transition {
		private OneBeatTransition(State from, State to) {
			super(from.numHands());
		}
	}

	public static void main(String[] args) {
		if(args.length == 2) {
			try {
				Siteswap ss1 = NotatedSiteswap.parse(args[0]);
				Siteswap ss2 = NotatedSiteswap.parse(args[1]);
				printf(ss1);
				printf(ss2);
				State s1 = new State(ss1);
				State s2 = new State(ss2);
				int minLength = 0;
				boolean allowExtraSqueezeCatches = false;
				boolean generateBallAntiballPairs = false;
				Transition t = compute(s1, s2, minLength, allowExtraSqueezeCatches, generateBallAntiballPairs);
				System.out.println(t);
			} catch(InvalidNotationException e) {
				System.out.println(e);
			}
		}
	}

}
