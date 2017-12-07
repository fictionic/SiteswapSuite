package siteswapsuite;

import java.util.List;

public class TransitionResults {

	private int numHands;
	private int selectTransition;
	private List<Siteswap> transitions;
	private GeneralizedTransition generalizedTransition;

	public TransitionResults(GeneralizedTransition generalizedTransition, int maxTransitions, int selectTransition) {
		this.generalizedTransition = generalizedTransition;
		this.transitions = generalizedTransition.unInfinitize(maxTransitions);
		this.numHands = this.generalizedTransition.numHands();
		this.selectTransition = selectTransition;
	}

	public List<Siteswap> getTransitions() {
		return this.transitions;
	}

	public Siteswap getSelectedTransition() {
		// if selectTransition is too big, choose the last one
		if(this.selectTransition >= this.transitions.size()) {
			Util.printf("WARNING: not enough transitions to select #" + this.selectTransition + "; selecting last transition instead", Util.DebugLevel.ERROR);
			this.selectTransition = this.transitions.size() - 1;
		}
		return this.transitions.get(this.selectTransition);
	}

	public GeneralizedTransition getGeneralizedTransition() {
		return this.generalizedTransition;
	}

	protected void unAntitossify(Siteswap prefix, Siteswap suffix) {
		for(int i=0; i<transitions.size(); i++) {
			Siteswap curTransition = this.transitions.get(i);
			this.transitions.set(i, this.getUnAntitossifiedTransition(curTransition, prefix, suffix));
		}
	}

	private Siteswap getUnAntitossifiedTransition(Siteswap oldTransition, Siteswap prefix, Siteswap suffix) {
		if(true) {
			Util.printf("WARNING: unantitossification of transitions is not yet implemented!", Util.DebugLevel.ERROR);
			return oldTransition; // don't actually do anything until we figure out the alg!
		} else {
			Util.printf("oldTransition:", Util.DebugLevel.DEBUG);
			Util.printf(oldTransition, Util.DebugLevel.DEBUG);
			Siteswap newTransition = oldTransition.deepCopy();
			Integer newTransitionStart = null, newTransitionEnd = null;
			int prefixLength = prefix.period();
			int suffixLength = suffix.period();
			int destBeat, destHand;
			Toss curToss;
			// un-antitossify transition
			Util.printf("un-antitossifying transition...", Util.DebugLevel.DEBUG);
			for(int b=0; b<oldTransition.period(); b++) {
				Util.printf("b: " + b, Util.DebugLevel.DEBUG);
				for(int h=0; h<this.numHands; h++) {
					for(int t=0; t<oldTransition.numTossesAtSite(b, h); t++) {
						curToss = oldTransition.getToss(b, h, t);
						// if it's an antitoss, make it a regular toss and put in its proper place in the output siteswap
						ExtendedInteger tossHeight;
						Toss toAdd;
						if(curToss.charge() != 0 && curToss.isAntitoss()) {
							Util.printf("removing toss", Util.DebugLevel.DEBUG);
							newTransition.getSite(b, h).removeToss(t);
							if(curToss.height().isInfinite()) {
								destBeat = b;
								tossHeight = new ExtendedInteger(curToss.height().infiniteValue());
								tossHeight.negate();
								toAdd = new Toss(tossHeight.infiniteValue(), false);
								newTransition.addToss(destBeat, h, toAdd);
							} else {
								destBeat = b + curToss.height().finiteValue();
								Util.printf("extending transition", Util.DebugLevel.DEBUG);
								destBeat = newTransition.extendToBeatIndex(destBeat);
								Util.printf(destBeat, Util.DebugLevel.DEBUG);
								tossHeight = new ExtendedInteger(-curToss.height().finiteValue());
								toAdd = new Toss(tossHeight.finiteValue(), h, false);
								newTransition.addToss(destBeat, curToss.destHand(), toAdd);
							}
							Util.printf(newTransition, Util.DebugLevel.DEBUG);
							// update endpoints
							if(newTransitionStart == null || destBeat < newTransitionStart)
								newTransitionStart = destBeat;
							if(newTransitionEnd == null || destBeat > newTransitionEnd)
								newTransitionEnd = destBeat;
						}
					}
				}
			}
			Util.printf("CURRENT STATE OF TRANSITION:", Util.DebugLevel.DEBUG);
			Util.printf(newTransition, Util.DebugLevel.DEBUG);
			Util.printf("start: " + newTransitionStart, Util.DebugLevel.DEBUG);
			Util.printf("end: " + newTransitionEnd, Util.DebugLevel.DEBUG);
			// un-antitossify prefix
			Util.printf("un-antitossifying prefix...", Util.DebugLevel.DEBUG);
			for(int b=-prefixLength; b<0; b++) {
				Util.printf("b: " + b, Util.DebugLevel.DEBUG);
				for(int h=0; h<prefix.numHands(); h++) {
					for(int t=0; t<prefix.numTossesAtSite(b, h); t++) {
						curToss = prefix.getToss(b, h, t);
						ExtendedInteger tossHeight;
						Toss toAdd;
						Util.printf(curToss, Util.DebugLevel.DEBUG);
						if(curToss.charge() != 0 && curToss.isAntitoss()) {
							if(!curToss.height().isInfinite()) {
								destBeat = b + curToss.height().finiteValue();
								Util.printf("destBeat: " + destBeat, Util.DebugLevel.DEBUG);
								tossHeight = new ExtendedInteger(-curToss.height().finiteValue());
								toAdd = new Toss(tossHeight.finiteValue(), curToss.destHand(), false);
							} else {
								destBeat = b;
								tossHeight = new ExtendedInteger(curToss.height().infiniteValue());
								tossHeight.negate();
								toAdd = new Toss(tossHeight.infiniteValue(), false);
							}
							if(!tossHeight.isInfinite() && tossHeight.finiteValue() != 0 && destBeat >= newTransitionStart) {
								newTransition.addToss(destBeat, h, toAdd);
								if(destBeat > newTransitionEnd) {
									newTransitionEnd = destBeat;
									Util.printf("end: " + newTransitionEnd, Util.DebugLevel.DEBUG);
								}
							} else
								Util.printf("skip", Util.DebugLevel.DEBUG);
						}
					}
				}
			}
			Util.printf("CURRENT STATE OF TRANSITION:", Util.DebugLevel.DEBUG);
			Util.printf(newTransition, Util.DebugLevel.DEBUG);
			// un-antitossify suffix
			Util.printf("un-antitossifying suffix...", Util.DebugLevel.DEBUG);
			int shiftAmount = 0;
			boolean skippedAll;
			int b, base = oldTransition.period();
			do {
				Util.printf("trying another period", Util.DebugLevel.DEBUG);
				skippedAll = true;
				for(int i=0; i<suffixLength; i++) {
					b = base + i;
					Util.printf("b: " + b, Util.DebugLevel.DEBUG);
					for(int h=0; h<suffix.numHands(); h++) {
						for(int t=0; t<suffix.numTossesAtSite(b, h); t++) {
							curToss = suffix.getToss(b, h, t);
							ExtendedInteger tossHeight;
							Toss toAdd;
							Util.printf(curToss, Util.DebugLevel.DEBUG);
							if(curToss.charge() != 0 && curToss.isAntitoss()) {
								if(!curToss.height().isInfinite()) {
									destBeat = b + curToss.height().finiteValue();
									tossHeight = new ExtendedInteger(-curToss.height().finiteValue());
									toAdd = new Toss(tossHeight.finiteValue(), curToss.destHand(), false);
								} else {
									destBeat = b;
									tossHeight = new ExtendedInteger(InfinityType.NEGATIVE_INFINITY);
									toAdd = new Toss(tossHeight.infiniteValue(), false);
								}
								Util.printf("destBeat: " + destBeat, Util.DebugLevel.DEBUG);
								if(!tossHeight.isInfinite() && tossHeight.finiteValue() != 0 && (destBeat <= newTransitionEnd || !skippedAll)) {
									destBeat = newTransition.extendToBeatIndex(destBeat);
									newTransition.addToss(destBeat, h, toAdd);
									skippedAll = false;
									if(destBeat < newTransitionStart)
										newTransitionStart = destBeat;
									if(destBeat > newTransitionEnd)
										newTransitionEnd = destBeat;
								} else
									Util.printf("skip", Util.DebugLevel.DEBUG);
							}
						}
					}
				}
				base += suffixLength;
			} while(!skippedAll);
			Util.printf("skipped all; done", Util.DebugLevel.DEBUG);
			Siteswap ret = newTransition.subPattern(newTransitionStart, newTransitionEnd+1);
			Util.printf(ret, Util.DebugLevel.DEBUG);
			return ret;
		}
	}

}
