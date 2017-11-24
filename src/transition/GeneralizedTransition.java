package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class GeneralizedTransition extends Siteswap {

	private int eventualPeriod;

	public GeneralizedTransition(int numHands) {
		super(numHands);
		this.eventualPeriod = -1;
	}

	protected void setEventualPeriod(int eventualPeriod) {
		this.eventualPeriod = eventualPeriod;
	}

	public Siteswap getThrowsPortion() {
		return this.subPattern(0, this.eventualPeriod);
	}

	public Siteswap getCatchesPortion() {
		return this.subPattern(this.eventualPeriod, this.period());
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
						if(!curToss.isAntitoss()) {
							numTosses++;
						} else {
							numAntitosses++;
						}
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
						if(!curCatch.isAntitoss()) {
							numCatches++;
						} else {
							numAnticatches++;
						}
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
				if(maxTransitions != -1 && maxTransitions <= 0) {
					return ret;
				}
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
				if(maxTransitions != -1) {
					maxTransitions--;
				}
			}
		}
		Util.printf(ret, Util.DebugLevel.DEBUG);
		return ret;
	}

	private static List<List<Integer>> findAllPermutations(int numTosses) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i=0; i<numTosses; i++) {
			list.add(i);
		}
		return findAllPermutationsHelper(list);
	}

	private static List<List<Integer>> findAllPermutationsHelper(List<Integer> list) {
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
