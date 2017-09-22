package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class ContextualizedNotatedTransitionList extends CompatibleNotatedSiteswapPair {

	private Transition generalTransition;
	private List<NotatedSiteswap> transitionList;
	private List<NotatedSiteswap> unAntitossifiedTransitionList;
	private int prefixLength, transitionLength, suffixLength;
	private int numHands;

	public ContextualizedNotatedTransitionList(CompatibleNotatedSiteswapPair patterns, int minLength, int maxTransitions, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) throws ImpossibleTransitionException {
		super(patterns);
		this.numHands = this.prefix.numHands;
		// get transition
		Util.printf("prefix: " + this.prefix.toString(), Util.DebugLevel.DEBUG);
		Util.printf("suffix: " + this.suffix.toString(), Util.DebugLevel.DEBUG);
		// then find the general form of the transition, if possible
		try {
			this.generalTransition = Transition.compute(new State(this.prefix), new State(this.suffix), minLength, allowExtraSqueezeCatches, generateBallAntiballPairs);
			this.transitionLength = generalTransition.eventualPeriod;
			// then get a list of the specific transitions
			List<Siteswap> unNotatedTransitionList = this.generalTransition.unInfinitize(maxTransitions);
			// then assemble them into notated siteswaps
			this.transitionList = new ArrayList<NotatedSiteswap>();
			for(int i=0; i<unNotatedTransitionList.size(); i++) {
				try {
					this.transitionList.add(NotatedSiteswap.assemble(unNotatedTransitionList.get(i), this.compatibleNotationType));
				} catch(IncompatibleNotationException e) {
					System.out.println("incompatible notations within ContextualizedNotatedTransitionList constructor, somehow...");
					System.out.println(e);
					System.exit(1);
				}
			}
		} catch(ImpossibleTransitionException e) {
			throw e;
		}
	}

	public Siteswap generalTransition() {
		return this.generalTransition;
	}

	public int transitionLength() {
		return this.transitionLength;
	}

	public String printGeneralTransition() {
		try {
			String ret = "";
			NotatedSiteswap firstHalf = NotatedSiteswap.assemble(this.generalTransition.subPattern(0,this.generalTransition.eventualPeriod), this.compatibleNotationType);
			ret += firstHalf.print();
			NotatedSiteswap secondHalf = NotatedSiteswap.assemble(this.generalTransition.subPattern(this.generalTransition.eventualPeriod, this.generalTransition.period()), this.compatibleNotationType);
			ret += "{";
			ret += secondHalf.print();
			ret += "}";
			return ret;
		} catch(IncompatibleNotationException e) {
			return "Impossible error in ContextualizedNotatedTransitionList!!!";
		}
	}

	public List<NotatedSiteswap> transitionList() {
		return this.transitionList;
	}

	public List<NotatedSiteswap> unAntitossifiedTransitionList() {
		if(this.unAntitossifiedTransitionList == null) {
			// then unAntitossify them
			this.unAntitossifiedTransitionList = new ArrayList<NotatedSiteswap>();
			for(int i=0; i<transitionList.size(); i++) {
				this.unAntitossifiedTransitionList.add(this.getUnAntitossifiedTransition(i));
			}
		}
		return this.unAntitossifiedTransitionList;
	}

	private NotatedSiteswap getUnAntitossifiedTransition(int transitionIndex) {
		if(true)
			return this.transitionList.get(transitionIndex); // don't actually do anything until we figure out the alg!
		else {
		////////
		Siteswap oldTransition = this.transitionList.get(transitionIndex);
		Util.printf("oldTransition:", Util.DebugLevel.DEBUG);
		Util.printf(oldTransition, Util.DebugLevel.DEBUG);
		Siteswap newTransition = oldTransition.deepCopy();
		Integer newTransitionStart = null, newTransitionEnd = null;
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
		NotatedSiteswap ret = null;
		try {
			ret = NotatedSiteswap.assemble(newTransition.subPattern(newTransitionStart, newTransitionEnd+1), this.compatibleNotationType);
		} catch(IncompatibleNotationException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		Util.printf(ret, Util.DebugLevel.DEBUG);
		return ret;
		}
	}

}
