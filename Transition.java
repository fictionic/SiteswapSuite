package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

class ImpossibleTransitionException extends Exception {}

abstract class Transition extends MutableSiteswap {

	private static final boolean debug = false;

	private static void printf(Object toPrint) {
		if(debug) {
			if(toPrint == null)
				System.out.println("{null}");
			else
				System.out.println(toPrint);
		}
	}

	int eventualPeriod = 0;

	static Transition compute(State from, State to, int minLength, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) throws ImpossibleTransitionException {

		printf(from);
		printf(to);
		printf("");

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

			printf("s1: " + from.toString());
			printf("s2: " + to.toString());

			State.DiffSum diffs;
			int futureCatches = 0;
			int futureAnticatches = 0;

			diffs = from.diffSums(to); // compute difference sum
			printf(diffs);

			int ballNumDiff = (diffs.catches - diffs.antiCatches) - (diffs.tosses - diffs.antiTosses);
			printf("ballNumDiff: " + ballNumDiff);

			printf("this: ");
			printf(this);
			printf("");

			int debugCounter = 20;

			// find the transition!
			while(b < minLength || diffs.tosses != 0 || diffs.antiTosses != 0 || futureCatches != diffs.catches || futureAnticatches != diffs.antiCatches) {

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
						if(ballNumDiff < 0 && diffs.catches == 0)
							ballNumDiff++;
						else
							futureCatches++;
					}
					while(chargeAtHand < 0) {
						printf("performing antitoss at beat " + b);
						this.addInfiniteAntitoss(b, h, InfinityType.POSITIVE_INFINITY);
						chargeAtHand++;
						if(ballNumDiff > 0 && diffs.antiCatches == 0)
							ballNumDiff--;
						else
							futureAnticatches++;
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

			this.eventualPeriod = b;
			this.appendEmptyBeat();
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
			printf("found general transition:");
			printf(this);
			printf("-");
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

	public List<MutableSiteswap> unInfinitize(int maxTransitions) {
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
		printf("     numTosses: " + numTosses);
		printf("    numCatches: " + numCatches);
		printf(" numAntitosses: " + numAntitosses);
		printf("numAnticatches: " + numAnticatches);
		printf(">     extraTosses: " + extraTosses);
		printf("> extraAntitosses: " + extraAntitosses);
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
		printf("tossOptionsList");
		printf(tossOptionsList);
		printf("toss perms: " + (numCatches + extraTosses));
		printf("antiToss perms: " + (numAnticatches + extraAntitosses));
		List<List<Integer>> tossPerms = findAllPermutations(numCatches + extraTosses);
		List<List<Integer>> antiTossPerms = findAllPermutations(numAnticatches + extraAntitosses);
		// combine into final list of transitions! (to be processed by a different class shortly...)
		List<MutableSiteswap> ret = new ArrayList<MutableSiteswap>();
		for(int t1=0; t1<tossPerms.size(); t1++) {
			for(int t2=0; t2<antiTossPerms.size(); t2++) {
				int totalFlatAnyTossIndex = 0;
				List<Integer> curTossPerm = tossPerms.get(t1);
				int flatTossIndex = 0;
				List<Integer> curAntitossPerm = antiTossPerms.get(t2);
				int flatAntitossIndex = 0;
				MutableSiteswap curSS = new MutableSiteswap(numHands);
				if(maxTransitions <= 0)
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
				maxTransitions--;
			}
		}
		printf(ret);
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
