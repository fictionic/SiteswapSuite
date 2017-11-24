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
		this.numHands = this.prefix.siteswap.numHands;
		// get transition
		Util.printf("prefix: " + this.prefix.toString(), Util.DebugLevel.DEBUG);
		Util.printf("suffix: " + this.suffix.toString(), Util.DebugLevel.DEBUG);
		// then find the general form of the transition, if possible
		try {
			this.generalTransition = Transition.compute(this.from.state, this.to.state, minLength, allowExtraSqueezeCatches, generateBallAntiballPairs);
			this.transitionLength = generalTransition.eventualPeriod;
			// then get a list of the specific transitions
			List<Siteswap> unNotatedTransitionList = this.generalTransition.unInfinitize(maxTransitions);
			// then assemble them into notated siteswaps
			this.transitionList = new ArrayList<NotatedSiteswap>();
			for(int i=0; i<unNotatedTransitionList.size(); i++) {
				try {
					this.transitionList.add(NotatedSiteswap.assemble(unNotatedTransitionList.get(i), this.compatibleSiteswapNotationType));
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

}
