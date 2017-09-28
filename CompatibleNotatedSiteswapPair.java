package siteswapsuite;

class CompatibleNotatedSiteswapPair {
	NotatedSiteswap prefix, suffix;
	NotatedState from, to;
	SiteswapNotation compatibleSiteswapNotationType;

	class Candidate {
		String notation;
		boolean isState;
		int numHands = -1;
		int startHand = -1;
		Candidate(String notation, boolean isState, int numHands, int startHand) {
			this.notation = notation;
			this.isState = isState;
			this.numHands = numHands;
			this.startHand = startHand;
		}
	}

	CompatibleNotatedSiteswapPair(CompatibleNotatedSiteswapPair p) {
		this.prefix = p.prefix;
		this.suffix = p.suffix;
		this.compatibleSiteswapNotationType = p.compatibleSiteswapNotationType;
	}

	CompatibleNotatedSiteswapPair(Candidate c1, Candidate c2) throws
		InvalidSiteswapNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException {
		// figure out if there is a compatible number of hands for both notations
		if(c1.numHands != -1 && c2.numHands != -1 && c1.numHands != c2.numHands) {
			throw new IncompatibleNumberOfHandsException();
		}
		int numHands;
		if(c1.numHands != -1) {
			numHands = c1.numHands;
		} else if(c2.numHands != -1) {
			numHands = c2.numHands;
		} else {
			numHands = -1;
		}
		// then determine what that compatible number of hands is, as well
		// as a compatible notation type for the transition between them
		SiteswapNotation n1;
		SiteswapNotation n2;
		try {
			n1 = SiteswapNotation.analyze(c1.notation);
			n2 = SiteswapNotation.analyze(c2.notation);
		} catch(InvalidSiteswapNotationException e) {
			throw e;
		}
		// parse the strings appropriately
		try {
			// check if either notation is emptynotation
			if(n1 == SiteswapNotation.EMPTY || n2 == SiteswapNotation.EMPTY) {
				if(numHands == -1) {
					numHands = n2.defaultNumHands();
				}
				this.prefix = NotatedSiteswap.parse(c1.notation, numHands, c1.startHand);
				this.suffix = NotatedSiteswap.parse(c2.notation, numHands, c2.startHand);
				this.compatibleSiteswapNotationType = SiteswapNotation.EMPTY;
			} else {
				// otherwise determine best way to parse inputs
				switch(numHands) {
					// if numHands was explicitly set to 1
					case 1:
						if(n1 == SiteswapNotation.ASYNCHRONOUS) {
							if(n2 == SiteswapNotation.ASYNCHRONOUS) {
								this.prefix = new NotatedSiteswap.OneHandedNotatedSiteswap(c1.notation);
								this.suffix = new NotatedSiteswap.OneHandedNotatedSiteswap(c2.notation);
								this.compatibleSiteswapNotationType = SiteswapNotation.ASYNCHRONOUS;
							} else {
								throw new IncompatibleNumberOfHandsException(c2.notation, numHands);
							}
						} else {
							throw new IncompatibleNumberOfHandsException(c1.notation, numHands);
						}
						break;
						// if numHands was explicitly set to 2
					case 2:
						this.compatibleSiteswapNotationType = SiteswapNotation.SYNCHRONOUS; // because a transition between them might not be synchronous
						this.prefix = NotatedSiteswap.parse(c1.notation, 2, c1.startHand);
						this.suffix = NotatedSiteswap.parse(c2.notation, 2, c2.startHand);
						break;
						// if numHands was not explicitly set
						// we'll have to determine the proper way to parse based just on the notation types
					case -1:
						switch(n1) {
							case ASYNCHRONOUS:
								if(n2 == SiteswapNotation.ASYNCHRONOUS) {
									this.compatibleSiteswapNotationType = SiteswapNotation.ASYNCHRONOUS;
									this.prefix = new NotatedSiteswap.OneHandedNotatedSiteswap(c1.notation);
									this.suffix = new NotatedSiteswap.OneHandedNotatedSiteswap(c2.notation);
								} else if(n2 != SiteswapNotation.PASSING) {
									this.compatibleSiteswapNotationType = SiteswapNotation.SYNCHRONOUS;
									this.prefix = NotatedSiteswap.parse(c1.notation, 2, c1.startHand);
									this.suffix = NotatedSiteswap.parse(c2.notation, 2, c2.startHand);
								} else {
									throw new IncompatibleNotationException(c1.notation, c2.notation);
								}
								break;
							case SYNCHRONOUS:
							case MIXED:
								if(n2 != SiteswapNotation.PASSING) {
									this.compatibleSiteswapNotationType = SiteswapNotation.SYNCHRONOUS;
									this.prefix = NotatedSiteswap.parse(c1.notation, 2, -1);
									this.suffix = NotatedSiteswap.parse(c2.notation, 2, c2.startHand);
								} else {
									throw new IncompatibleNotationException(c1.notation, c2.notation);
								}
								break;
							default: // case PASSING
								if(n2 == SiteswapNotation.PASSING) {
									this.compatibleSiteswapNotationType = SiteswapNotation.PASSING;
									this.prefix = NotatedSiteswap.parse(c1.notation, -1, c1.startHand);
									this.suffix = new NotatedSiteswap.NotatedPassingSiteswap(c2.notation);
								} else {
									throw new IncompatibleNotationException(c1.notation, c2.notation);
								}
								break;
						}
						break;
					default:
						// if numHands was explicitly set to something other than 1 or 2
						throw new IncompatibleNumberOfHandsException(c1.notation, numHands);
				}
			}
		} catch(IncompatibleNumberOfHandsException e) {
			throw e;
		}
	}

	CompatibleNotatedSiteswapPair(NotatedSiteswap prefix, NotatedSiteswap suffix) throws IncompatibleNumberOfHandsException {
		if(prefix.siteswap.numHands() != suffix.siteswap.numHands())
			throw new IncompatibleNumberOfHandsException();
		// determine compatibleSiteswapNotationType
		switch(prefix.siteswap.numHands()) {
			case 0:
				this.compatibleSiteswapNotationType = suffix.notationType();
				break;
			case 1:
				this.compatibleSiteswapNotationType = SiteswapNotation.ASYNCHRONOUS;
				break;
			case 2:
				this.compatibleSiteswapNotationType = SiteswapNotation.SYNCHRONOUS;
				break;
			default:
				this.compatibleSiteswapNotationType = SiteswapNotation.PASSING;
		}
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public NotatedSiteswap prefix() {
		return this.prefix;
	}

	public NotatedSiteswap suffix() {
		return this.suffix;
	}

}
