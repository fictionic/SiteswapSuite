package siteswapsuite;

class CompatibleNotatedObjectPair {
	NotatedSiteswap prefix, suffix;
	NotatedState from, to;
	SiteswapNotation compatibleSiteswapNotationType;

	static class Candidate {
		boolean isState;
		NotatedSiteswap notatedSiteswap;
		NotatedState notatedState;
		Candidate(NotatedSiteswap notatedSiteswap) {
			this.isState = false;
			this.notatedSiteswap = notatedSiteswap;
		}
		Candidate(NotatedState notatedState) {
			this.isState = true;
			this.notatedState = notatedState;
		}
		int numHands() {
			if(this.isState) {
				return this.notatedState.state.numHands();
			} else {
				return this.notatedSiteswap.siteswap.numHands();
			}
		}
	}

	CompatibleNotatedObjectPair(Candidate c1, Candidate c2) {
		// case 1: both siteswaps
		if(!c1.isState && !c2.isState) {
			switch(c1.notatedSiteswap.notationType()) {
				case EMPTY:
					break;
				case ASYNCHRONOUS:
					break;
				case SYNCHRONOUS:
					break;
				case MIXED:
					break;
				default:
					break;
			}
		} else {
		}
		boolean swap;
		// allow the w/ol.o.g. up next
		if(c1.isState && !c2.isState) {
			swap = true;
			Candidate c3; // temp for swap
			c3 = c1;
			c1 = c2;
			c2 = c3;
		} else {
			swap = false;
		}
		// do the checks
		// without loss of generality assume !c1.isState
		if(!c1.isState) {
			switch(c1.notatedSiteswap.notationType()) {
				case EMPTY:
					if(c2.isState) {
						this.compatibleSiteswapNotationType = SiteswapNotation.defaultNotationType(c2.numHands());
					} else {
						this.compatibleSiteswapNotationType = c2.notatedSiteswap.notationType();
					}
					break;
				case ASYNCHRONOUS:
					if(c2.isState) {
						if(c2.numHands() == 1) {
							this.compatibleSiteswapNotationType = SiteswapNotation.ASYNCHRONOUS;
						} else if(c2.numHands() == 2) {
							this.compatibleSiteswapNotationType = SiteswapNotation.SYNCHRONOUS;
						} else {
							Util.printf("numHands > 2 not yet supported in CompatibleNotatedObjectPair");
							System.exit(1);
						}
					} else {
						this.compatibleSiteswapNotationType = c2.notatedSiteswap.notationType();
					}
					break;
				case SYNCHRONOUS:
					break;
				case MIXED:
				case PASSING:
					break;
			}
		}
	}

}

