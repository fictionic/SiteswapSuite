package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class ContextualizedNotatedTransitionList extends CompatibleNotatedSiteswapPair {

	private Transition generalTransition;
	private List<NotatedSiteswap> transitionList;
	private List<NotatedSiteswap> unAntitossifiedTransitionList;
	private int prefixLength, transitionLength, suffixLength;
	private int numHands;

	private static boolean debug = false;

	private static void printf(Object msg) {
		if(debug) {
			try {
				System.out.println(msg);
			} catch(NullPointerException e) {
				System.out.println("null");
			}
		}
	}

	public ContextualizedNotatedTransitionList(CompatibleNotatedSiteswapPair patterns, int minLength, int maxTransitions, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) throws ImpossibleTransitionException {
		super(patterns);
		this.numHands = this.prefix.numHands;
		// get transition
		printf("prefix: " + this.prefix.toString());
		printf("suffix: " + this.suffix.toString());
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
		printf("oldTransition:");
		printf(oldTransition);
		Siteswap newTransition = oldTransition.deepCopy();
		Integer newTransitionStart = null, newTransitionEnd = null;
		int destBeat, destHand;
		Toss curToss;
		// un-antitossify transition
		printf("un-antitossifying transition...");
		for(int b=0; b<oldTransition.period(); b++) {
			printf("b: " + b);
			for(int h=0; h<this.numHands; h++) {
				for(int t=0; t<oldTransition.numTossesAtSite(b, h); t++) {
					curToss = oldTransition.getToss(b, h, t);
					// if it's an antitoss, make it a regular toss and put in its proper place in the output siteswap
					ExtendedInteger tossHeight;
					Toss toAdd;
					if(curToss.charge() != 0 && curToss.isAntitoss()) {
						printf("removing toss");
						newTransition.getSite(b, h).removeToss(t);
						if(curToss.height().isInfinite()) {
							destBeat = b;
							tossHeight = new ExtendedInteger(curToss.height().infiniteValue());
							tossHeight.negate();
							toAdd = new Toss(tossHeight.infiniteValue(), false);
							newTransition.addToss(destBeat, h, toAdd);
						} else {
							destBeat = b + curToss.height().finiteValue();
							printf("extending transition");
							destBeat = newTransition.extendToBeatIndex(destBeat);
							printf(destBeat);
							tossHeight = new ExtendedInteger(-curToss.height().finiteValue());
							toAdd = new Toss(tossHeight.finiteValue(), h, false);
							newTransition.addToss(destBeat, curToss.destHand(), toAdd);
						}
						printf(newTransition);
						// update endpoints
						if(newTransitionStart == null || destBeat < newTransitionStart)
							newTransitionStart = destBeat;
						if(newTransitionEnd == null || destBeat > newTransitionEnd)
							newTransitionEnd = destBeat;
					}
				}
			}
		}
		printf("CURRENT STATE OF TRANSITION:");
		printf(newTransition);
		printf("start: " + newTransitionStart);
		printf("end: " + newTransitionEnd);
		// un-antitossify prefix
		printf("un-antitossifying prefix...");
		for(int b=-prefixLength; b<0; b++) {
			printf("b: " + b);
			for(int h=0; h<prefix.numHands(); h++) {
				for(int t=0; t<prefix.numTossesAtSite(b, h); t++) {
					curToss = prefix.getToss(b, h, t);
					ExtendedInteger tossHeight;
					Toss toAdd;
					printf(curToss);
					if(curToss.charge() != 0 && curToss.isAntitoss()) {
						if(!curToss.height().isInfinite()) {
							destBeat = b + curToss.height().finiteValue();
							printf("destBeat: " + destBeat);
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
								printf("end: " + newTransitionEnd);
							}
						} else 
							printf("skip");
					}
				}
			}
		}
		printf("CURRENT STATE OF TRANSITION:");
		printf(newTransition);
		// un-antitossify suffix
		printf("un-antitossifying suffix...");
		int shiftAmount = 0;
		boolean skippedAll;
		int b, base = oldTransition.period();
		do {
			printf("trying another period");
			skippedAll = true;
			for(int i=0; i<suffixLength; i++) {
				b = base + i;
				printf("b: " + b);
				for(int h=0; h<suffix.numHands(); h++) {
					for(int t=0; t<suffix.numTossesAtSite(b, h); t++) {
						curToss = suffix.getToss(b, h, t);
						ExtendedInteger tossHeight;
						Toss toAdd;
						printf(curToss);
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
							printf("destBeat: " + destBeat);
							if(!tossHeight.isInfinite() && tossHeight.finiteValue() != 0 && (destBeat <= newTransitionEnd || !skippedAll)) {
								destBeat = newTransition.extendToBeatIndex(destBeat);
								newTransition.addToss(destBeat, h, toAdd);
								skippedAll = false;
								if(destBeat < newTransitionStart)
									newTransitionStart = destBeat;
								if(destBeat > newTransitionEnd)
									newTransitionEnd = destBeat;
							} else 
								printf("skip");
						}
					}
				}
			}
			base += suffixLength;
		} while(!skippedAll);
		printf("skipped all; done");
		NotatedSiteswap ret = null;
		try {
			ret = NotatedSiteswap.assemble(newTransition.subPattern(newTransitionStart, newTransitionEnd+1), this.compatibleNotationType);
		} catch(IncompatibleNotationException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		printf(ret);
		return ret;
		}
	}

}
