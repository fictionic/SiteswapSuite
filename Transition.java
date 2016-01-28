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

		System.out.println("computing transition between states:");
		printf(from);
		printf(to);
		printf("");

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

			int b = 0; // index of beat in output siteswap

			printf("s1: " + from.toString());
			printf("s2: " + to.toString());

			State.DiffSum diffs;
			int futureCatches = 0;
			int futureAnticatches = 0;
			boolean shifted = false;

			diffs = from.diffSums(to); // compute difference sum
			printf(diffs);

			int ballNumDiff = (diffs.catches - diffs.antiCatches) - (diffs.tosses - diffs.antiTosses);
			printf("ballNumDiff: " + ballNumDiff);

			printf("this: ");
			printf(this);
			printf("");

			int debugCounter = 20;

			// find the transition!
			while(b < minLength || diffs.tosses != 0 || diffs.antiTosses != 0 || futureCatches != diffs.catches || futureAnticatches != diffs.antiCatches || shifted) {

				printf(">>>>>  b: " + b);
				this.appendEmptyBeat();
				// see if we can catch new balls/antiballs
				for(int h=0; h<numHands; h++) {
					if(from.getChargeAtBeatAtHand(0,h) == 0) {
						if(ballNumDiff < 0 && to.getChargeAtBeatAtHand(0,h) < 0) {
							printf("catching new antiball at beat " + b);
							this.addInfiniteAntitoss(b, h, InfinityType.NEGATIVE_INFINITY);
							from.decChargeOfNowNodeAtHand(h);
							ballNumDiff++;
						} else if(ballNumDiff > 0 && to.getChargeAtBeatAtHand(0,h) > 0) {
							printf("catching new ball at beat " + b);
							this.addInfiniteToss(b, h, InfinityType.NEGATIVE_INFINITY);
							from.incChargeOfNowNodeAtHand(h);
							ballNumDiff--;
						}
					}
				}
				// shift goal state backward by one beat, and match lengths
				printf("shifting");
				to.shiftBackward();
				from.getFiniteNode(to.finiteLength() - 1);

				// make tosses to match charges in nodes between states
				for(int h=0; h<numHands; h++) {
					int chargeAtHand = from.getChargeAtBeatAtHand(0, h);
					while(chargeAtHand > 0) {
						printf("performing toss at beat " + b);
						this.addInfiniteToss(b, h, InfinityType.POSITIVE_INFINITY);
						chargeAtHand--;
						if(ballNumDiff < 0)
							ballNumDiff++;
						else
							futureCatches++;
					}
					while(chargeAtHand < 0) {
						printf("performing antitoss at beat " + b);
						this.addInfiniteAntitoss(b, h, InfinityType.POSITIVE_INFINITY);
						chargeAtHand++;
						if(ballNumDiff > 0)
							ballNumDiff--;
						else
							futureAnticatches--;
					}
				}
				printf("advancing time");
				from.advanceTime();
				to.advanceTime();
				b++;

				printf("s1: " + from.toString());
				printf("s2: " + to.toString());
				diffs = from.diffSums(to);
				printf(diffs);
				printf("futureCatches: " + futureCatches);
				printf("futureAnticatches: " + futureAnticatches);
				printf("ballNumDiff: " + ballNumDiff);
				printf(this);
				debugCounter--;
				if(debugCounter == 0) {
					printf("debug counter threshhold reached; aborting");
					break;
				}
			}

			this.appendEmptyBeat();
			this.firstCatchIndex = b;
			printf(this);

			printf("FINDING CATCHES!");

			// find catches!
			while(from.finiteLength() > 0) {
				for(int h=0; h<numHands; h++) {
					int diff = to.getChargeAtBeatAtHand(0, h) - from.getChargeAtBeatAtHand(0, h);
					if(diff > 0) {
						printf("catching ball at beat " + b);
						this.addInfiniteToss(b, h, InfinityType.NEGATIVE_INFINITY);
					} else if(diff < 0) {
						printf("catching antiball at beat " + b);
						this.addInfiniteAntitoss(b, h, InfinityType.NEGATIVE_INFINITY);
					}
				}
				b++;
				this.appendEmptyBeat();
				from.advanceTime();
				to.advanceTime();
			}

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
				System.out.println(NotatedSiteswap.assemble(t).print());
			} catch(InvalidNotationException e) {
				System.out.println(e);
			}
		}
	}

}
