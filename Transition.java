package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

class ImpossibleTransitionException extends SiteswapException {
	String message = "ERROR: cannot compute transition between non-finite states";
	public String getMessage() {
		return this.message;
	}
}

abstract class Transition extends Siteswap {

	int eventualPeriod = 0;

	static Transition compute(State from, State to, int minLength, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) throws ImpossibleTransitionException {

		Util.printf(from, Util.DebugLevel.DEBUG);
		Util.printf(to, Util.DebugLevel.DEBUG);
		Util.printf("", Util.DebugLevel.DEBUG);

		// first check that the states are finite, otherwise there won't be a transition
		if(!from.isFinite() || !to.isFinite()) {
			throw new ImpossibleTransitionException();
		}

		// make copies of the states, so as not to muck up the originals
		State fromCopy = from.deepCopy();
		State toCopy = to.deepCopy();
		
		// see if either state is empty
		if(fromCopy.finiteLength() == 0 || toCopy.finiteLength() == 0) {
			return new EmptyTransition(from.numHands());
		}

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

	private static class EmptyTransition extends Transition {
		private EmptyTransition(int numHands) {
			super(numHands);
		}
	}

	private static class StandardTransition extends Transition {
		private StandardTransition(State from, State to, int minLength) {
			super(from.numHands());

			int b = 0; // index of beat in output siteswap

			Util.printf("s1: " + from.toString(), Util.DebugLevel.DEBUG);
			Util.printf("s2: " + to.toString(), Util.DebugLevel.DEBUG);

			State.DiffSum diffs;
			int futureCatches = 0;
			int futureAnticatches = 0;

			diffs = from.diffSums(to); // compute difference sum
			Util.printf(diffs, Util.DebugLevel.DEBUG);

			int ballNumDiff = (diffs.catches - diffs.antiCatches) - (diffs.tosses - diffs.antiTosses);
			Util.printf("ballNumDiff: " + ballNumDiff, Util.DebugLevel.DEBUG);
			int ballNumDiffPositive = (ballNumDiff > 0 ? ballNumDiff : 0);
			int ballNumDiffNegative = (ballNumDiff < 0 ? ballNumDiff : 0);

			Util.printf("this: ", Util.DebugLevel.DEBUG);
			Util.printf(this, Util.DebugLevel.DEBUG);
			Util.printf("", Util.DebugLevel.DEBUG);

			int debugCounter = 20;

			// find the transition!
			while(b < minLength || diffs.tosses != 0 || diffs.antiTosses != 0 || futureCatches + ballNumDiffNegative != diffs.catches || futureAnticatches + ballNumDiffPositive != diffs.antiCatches) {
				Util.printf(">>>>>  b: " + b, Util.DebugLevel.DEBUG);
				this.appendEmptyBeat();
				// see if we can catch new balls/antiballs
				for(int h=0; h<numHands; h++) {
					if(from.getChargeAtBeatAtHand(0,h) == 0) {
						Util.printf(to.getChargeAtBeatAtHand(0,h), Util.DebugLevel.DEBUG);
						if(ballNumDiffNegative < 0 && to.getChargeAtBeatAtHand(0,h) < 0) {
							Util.printf("catching new antiball at beat " + b, Util.DebugLevel.DEBUG);
							this.addInfiniteAntitoss(b, h, InfinityType.NEGATIVE_INFINITY);
							from.decChargeOfNowNodeAtHand(h);
							ballNumDiffNegative++;
						} else if(ballNumDiffPositive > 0 && to.getChargeAtBeatAtHand(0,h) > 0) {
							Util.printf("catching new ball at beat " + b, Util.DebugLevel.DEBUG);
							this.addInfiniteToss(b, h, InfinityType.NEGATIVE_INFINITY);
							from.incChargeOfNowNodeAtHand(h);
							ballNumDiffPositive--;
						}
					}
				}
				// shift goal state backward by one beat, and match lengths
				Util.printf("shifting", Util.DebugLevel.DEBUG);
				to.shiftBackward();
				from.getFiniteNode(to.finiteLength() - 1);
				Util.printf("s1: " + from.toString(), Util.DebugLevel.DEBUG);
				Util.printf("s2: " + to.toString(), Util.DebugLevel.DEBUG);

				// make tosses to match charges in nodes between states
				for(int h=0; h<numHands; h++) {
					int chargeAtHand = from.getChargeAtBeatAtHand(0, h);
					while(chargeAtHand > 0) {
						Util.printf("performing toss at beat " + b, Util.DebugLevel.DEBUG);
						this.addInfiniteToss(b, h, InfinityType.POSITIVE_INFINITY);
						chargeAtHand--;
						if(ballNumDiffNegative < 0 && diffs.catches == 0)
							ballNumDiffNegative++;
						else
							futureCatches++;
					}
					while(chargeAtHand < 0) {
						Util.printf("performing antitoss at beat " + b, Util.DebugLevel.DEBUG);
						this.addInfiniteAntitoss(b, h, InfinityType.POSITIVE_INFINITY);
						chargeAtHand++;
						if(ballNumDiffPositive > 0 && diffs.antiCatches == 0)
							ballNumDiffPositive--;
						else
							futureAnticatches++;
					}
				}
				Util.printf("advancing time", Util.DebugLevel.DEBUG);
				from.advanceTime();
				to.advanceTime();
				b++;

				Util.printf("s1: " + from.toString(), Util.DebugLevel.DEBUG);
				Util.printf("s2: " + to.toString(), Util.DebugLevel.DEBUG);
				diffs = from.diffSums(to);
				Util.printf(diffs, Util.DebugLevel.DEBUG);
				Util.printf("futureCatches: " + futureCatches, Util.DebugLevel.DEBUG);
				Util.printf("futureAnticatches: " + futureAnticatches, Util.DebugLevel.DEBUG);
				Util.printf("ballNumDiffPositive: " + ballNumDiffPositive, Util.DebugLevel.DEBUG);
				Util.printf("ballNumDiffNegative: " + ballNumDiffNegative, Util.DebugLevel.DEBUG);
				Util.printf(this, Util.DebugLevel.DEBUG);
				debugCounter--;
				if(debugCounter == 0) {
					Util.printf("debug counter threshhold reached; aborting", Util.DebugLevel.DEBUG);
					break;
				}
			}

			this.eventualPeriod = b;
			this.appendEmptyBeat();
			Util.printf(this, Util.DebugLevel.DEBUG);

			Util.printf("FINDING CATCHES!", Util.DebugLevel.DEBUG);

			// find catches!
			while(from.finiteLength() > 0) {
				for(int h=0; h<numHands; h++) {
					int diff = to.getChargeAtBeatAtHand(0, h) - from.getChargeAtBeatAtHand(0, h);
					if(diff > 0) {
						Util.printf("catching ball at beat " + b, Util.DebugLevel.DEBUG);
						this.addInfiniteToss(b, h, InfinityType.NEGATIVE_INFINITY);
					} else if(diff < 0) {
						Util.printf("catching antiball at beat " + b, Util.DebugLevel.DEBUG);
						this.addInfiniteAntitoss(b, h, InfinityType.NEGATIVE_INFINITY);
					}
				}
				b++;
				this.appendEmptyBeat();
				from.advanceTime();
				to.advanceTime();
			}
			Util.printf("found general transition:", Util.DebugLevel.DEBUG);
			Util.printf(this, Util.DebugLevel.DEBUG);
			Util.printf("-", Util.DebugLevel.DEBUG);
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

	public List<Siteswap> unInfinitize(int maxTransitions) {
		int numTosses = 0;
		int numAntitosses = 0;
		// count [anti]tosses
		for(int tossBeat=0; tossBeat<eventualPeriod; tossBeat++) {
			// loop through hands
			for(int tossHand=0; tossHand<numHands; tossHand++) {
				// loop through tosses in hand
				for(int tossToss=0; tossToss<this.numTossesAtSite(tossBeat,tossHand); tossToss++) {
					// see if toss at this index is a real toss
					Toss curToss = this.getToss(tossBeat,tossHand,tossToss);
					if(curToss.height().sign() > 0) {
						if(!curToss.isAntitoss())
							numTosses++;
						else
							numAntitosses++;
					}
				}
			}
		}
		int numCatches = 0;
		int numAnticatches = 0;
		// count catches/anticatches
		for(int catchBeat=eventualPeriod; catchBeat<period(); catchBeat++) {
			// loop through hands
			for(int catchHand=0; catchHand<this.numHands; catchHand++) {
				// loop through tosses in hand
				for(int catchToss=0; catchToss<this.numTossesAtSite(catchBeat,catchHand); catchToss++) {
					Toss curCatch = this.getToss(catchBeat, catchHand, catchToss);
					// make sure it's actually a catch, not a zero-toss
					if(curCatch.height().sign() < 0) {
						if(!curCatch.isAntitoss())
							numCatches++;
						else
							numAnticatches++;
					}
				}
			}
		}
		int extraTosses = numTosses - numCatches;
		int extraAntitosses = numAntitosses - numAnticatches;
		Util.printf("     numTosses: " + numTosses, Util.DebugLevel.DEBUG);
		Util.printf("    numCatches: " + numCatches, Util.DebugLevel.DEBUG);
		Util.printf(" numAntitosses: " + numAntitosses, Util.DebugLevel.DEBUG);
		Util.printf("numAnticatches: " + numAnticatches, Util.DebugLevel.DEBUG);
		Util.printf(">     extraTosses: " + extraTosses, Util.DebugLevel.DEBUG);
		Util.printf("> extraAntitosses: " + extraAntitosses, Util.DebugLevel.DEBUG);
		// get list of all options for tosses, and null where non-tosses are
		List<List<Toss>> tossOptionsList = new ArrayList<List<Toss>>();
		for(int tossBeat=0; tossBeat<eventualPeriod; tossBeat++) {
			// loop through hands
			for(int tossHand=0; tossHand<numHands; tossHand++) {
				// loop through tosses in hand
				for(int tossToss=0; tossToss<this.numTossesAtSite(tossBeat,tossHand); tossToss++) {
					// see if toss at this index is a real toss
					Toss curToss = this.getToss(tossBeat,tossHand,tossToss);
					if(curToss.height().sign() <= 0) {
						tossOptionsList.add(null);
					} else {
						// add the appropriate number of infinite-height tosses of appropriate charge
						ArrayList<Toss> tossOptions = new ArrayList<Toss>();
						if(!curToss.isAntitoss()) {
							for(int i=0; i<extraTosses; i++) {
								tossOptions.add(new Toss(InfinityType.POSITIVE_INFINITY, false));
							}
						} else {
							for(int i=0; i<extraAntitosses; i++) {
								tossOptions.add(new Toss(InfinityType.POSITIVE_INFINITY, true));
							}
						}
						// loop through catches to get all other possible tosses
						for(int catchBeat=eventualPeriod; catchBeat<period(); catchBeat++) {
							// loop through hands
							for(int catchHand=0; catchHand<this.numHands; catchHand++) {
								// loop through tosses in hand
								for(int catchToss=0; catchToss<this.numTossesAtSite(catchBeat,catchHand); catchToss++) {
									Toss curCatch = this.getToss(catchBeat, catchHand, catchToss);
									// make sure it's a catch of matching charge
									if(curCatch.height().sign() < 0 && curCatch.isAntitoss() == curToss.isAntitoss()) {
										int height = catchBeat - tossBeat;
										tossOptions.add(new Toss(height, catchHand, curToss.isAntitoss()));
									}
								}
							}
						}
						tossOptionsList.add(tossOptions);
					}
				}
			}
		}
		Util.printf("tossOptionsList", Util.DebugLevel.DEBUG);
		Util.printf(tossOptionsList, Util.DebugLevel.DEBUG);
		Util.printf("toss perms: " + (numCatches + extraTosses), Util.DebugLevel.DEBUG);
		Util.printf("antiToss perms: " + (numAnticatches + extraAntitosses), Util.DebugLevel.DEBUG);
		List<List<Integer>> tossPerms = findAllPermutations(numCatches + extraTosses);
		List<List<Integer>> antiTossPerms = findAllPermutations(numAnticatches + extraAntitosses);
		// combine into final list of transitions! (to be processed by a different class shortly...)
		List<Siteswap> ret = new ArrayList<Siteswap>();
		for(int t1=0; t1<tossPerms.size(); t1++) {
			for(int t2=0; t2<antiTossPerms.size(); t2++) {
				int totalFlatAnyTossIndex = 0;
				List<Integer> curTossPerm = tossPerms.get(t1);
				int flatTossIndex = 0;
				List<Integer> curAntitossPerm = antiTossPerms.get(t2);
				int flatAntitossIndex = 0;
				Siteswap curSS = new Siteswap(numHands);
				if(maxTransitions != -1 && maxTransitions <= 0)
					return ret;
				for(int b=0; b<eventualPeriod; b++) {
					curSS.appendEmptyBeat();
					for(int h=0; h<numHands; h++) {
						for(int t=0; t<numTossesAtSite(b, h); t++) {
							Toss curToss = this.getToss(b, h, t);
							if(tossOptionsList.get(totalFlatAnyTossIndex) == null) {
								curSS.addToss(b, h, curToss);
							} else {
								if(!curToss.isAntitoss()) {
									curSS.addToss(b, h, tossOptionsList.get(totalFlatAnyTossIndex).get(curTossPerm.get(flatTossIndex)));
									flatTossIndex++;
								} else {
									curSS.addToss(b, h, tossOptionsList.get(totalFlatAnyTossIndex).get(curAntitossPerm.get(flatAntitossIndex)));
									flatAntitossIndex++;
								}
							}
							totalFlatAnyTossIndex++;
						}
					}

				}
				ret.add(curSS);
				if(maxTransitions != -1)
					maxTransitions--;
			}
		}
		Util.printf(ret, Util.DebugLevel.DEBUG);
		return ret;
	}

	static List<List<Integer>> findAllPermutations(int numTosses) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i=0; i<numTosses; i++) {
			list.add(i);
		}
		return findAllPermutationsHelper(list);
	}

	static List<List<Integer>> findAllPermutationsHelper(List<Integer> list) {
		if(list.size() == 0) { 
			List<List<Integer>> result = new ArrayList<List<Integer>>();
			result.add(new ArrayList<Integer>());
			return result;
		}
		Integer firstElement = list.remove(0);
		List<List<Integer>> returnValue = new ArrayList<List<Integer>>();
		List<List<Integer>> permutations = findAllPermutationsHelper(list);
		for(List<Integer> smallerPermutated : permutations) {
			for(int index=0; index <= smallerPermutated.size(); index++) {
				List<Integer> temp = new ArrayList<Integer>(smallerPermutated);
				temp.add(index, firstElement);
				returnValue.add(temp);
			}
		}
		return returnValue;
	}

}
