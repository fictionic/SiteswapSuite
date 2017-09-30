package siteswapsuite;

class CompatibleNotatedSiteswapPair {
	NotatedSiteswap prefix, suffix;
	NotatedState from, to;
	SiteswapNotation compatibleSiteswapNotationType;

	static class Candidate {
		String notation;
		boolean isState;
		int numHands;
		int startHand;
		int minSSLength;
		private Candidate(String notation, boolean isState, int minSSLength, int numHands, int startHand) {
			this.notation = notation;
			this.isState = isState;
			this.minSSLength = minSSLength;
			this.numHands = numHands;
			this.startHand = startHand;
		}
		static Candidate fromState(String notation, int minSSLength, int numHands, int startHand) {
			return new Candidate(notation, true, minSSLength, numHands, startHand);
		}
		static Candidate fromSiteswap(String notation, int numHands, int startHand) {
			return new Candidate(notation, false, -1, numHands, startHand);
		}
	}

	CompatibleNotatedSiteswapPair(CompatibleNotatedSiteswapPair p) {
		this.prefix = p.prefix;
		this.suffix = p.suffix;
		this.from = p.from;
		this.to = p.to;
		this.compatibleSiteswapNotationType = p.compatibleSiteswapNotationType;
	}

	CompatibleNotatedSiteswapPair(Candidate c1, Candidate c2) throws
		InvalidSiteswapNotationException, InvalidStateNotationException,
		IncompatibleNotationException, IncompatibleNumberOfHandsException {
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
		// as  compatible notation type for the transition between them
		SiteswapNotation n1;
		SiteswapNotation n2;
		try {
			n1 = SiteswapNotation.analyze(c1.notation);
			n2 = SiteswapNotation.analyze(c2.notation);
		} catch(InvalidSiteswapNotationException e) {
			throw e;
		}
		try {
			// check if either notation is emptynotation
			if(n1 == SiteswapNotation.EMPTY || n2 == SiteswapNotation.EMPTY) {
				if(numHands == -1) {
					numHands = n2.defaultNumHands();
				}
				this.compatibleSiteswapNotationType = SiteswapNotation.EMPTY;
			} else {
				// otherwise determine best way to parse inputs
				switch(numHands) {
					// if numHands was explicitly set to 1
					case 1:
						if(n1 == SiteswapNotation.ASYNCHRONOUS) {
							if(n2 == SiteswapNotation.ASYNCHRONOUS) {
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
						this.suffix = NotatedSiteswap.parse(c2.notation, 2, c2.startHand);
						break;
						// if numHands was not explicitly set
						// we'll have to determine the proper way to parse based just on the notation types
					case -1:
						switch(n1) {
							case ASYNCHRONOUS:
								if(n2 == SiteswapNotation.ASYNCHRONOUS) {
									numHands = 1;
									this.compatibleSiteswapNotationType = SiteswapNotation.ASYNCHRONOUS;
								} else if(n2 != SiteswapNotation.PASSING) {
									numHands = 2;
									this.compatibleSiteswapNotationType = SiteswapNotation.SYNCHRONOUS;
								} else {
									throw new IncompatibleNotationException(c1.notation, c2.notation);
								}
								break;
							case SYNCHRONOUS:
							case MIXED:
								if(n2 != SiteswapNotation.PASSING) {
									numHands = 2;
									this.compatibleSiteswapNotationType = SiteswapNotation.SYNCHRONOUS;
								} else {
									throw new IncompatibleNotationException(c1.notation, c2.notation);
								}
								break;
							default: // case PASSING
								if(n2 == SiteswapNotation.PASSING) {
									numHands = 4; // TODO
									this.compatibleSiteswapNotationType = SiteswapNotation.PASSING;
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
		// now parse the notations based on what we just computed
		// first c1
		if(c1.isState) {
			this.from = NotatedState.parse(c1.notation, numHands, c1.startHand);
			this.prefix = NotatedSiteswap.assemble(this.from.state.getTransitionToSelf(c1.minSSLength), compatibleSiteswapNotationType);
		} else {
			this.prefix = NotatedSiteswap.parse(c1.notation, numHands, c1.startHand);
			this.from = NotatedState.assemble(new State(prefix.siteswap), StateNotation.defaultNotationType(numHands));
		}
		// now c2
		if(c2.isState) {
			this.to = NotatedState.parse(c2.notation, numHands, c2.startHand);
			this.suffix = NotatedSiteswap.assemble(this.to.state.getTransitionToSelf(c2.minSSLength), compatibleSiteswapNotationType);
		} else {
			this.suffix = NotatedSiteswap.parse(c2.notation, numHands, c2.startHand);
			this.to = NotatedState.assemble(new State(suffix.siteswap), StateNotation.defaultNotationType(numHands));
		}
	}

	CompatibleNotatedSiteswapPair(NotatedSiteswap prefix, NotatedSiteswap suffix) throws IncompatibleNumberOfHandsException {
		if(prefix.siteswap.numHands() != suffix.siteswap.numHands()) {
			throw new IncompatibleNumberOfHandsException();
		}
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
		this.from = NotatedState.assembleAutomatic(new State(this.prefix.siteswap));
		this.to = NotatedState.assembleAutomatic(new State(this.suffix.siteswap));
	}

	public NotatedSiteswap prefix() {
		return this.prefix;
	}

	public NotatedSiteswap suffix() {
		return this.suffix;
	}

}
