package siteswapsuite;

class CompatibleNotatedSiteswapPair {
	NotatedSiteswap prefix, suffix;
	Notation compatibleNotationType;

	CompatibleNotatedSiteswapPair(CompatibleNotatedSiteswapPair p) {
		this.prefix = p.prefix;
		this.suffix = p.suffix;
		this.compatibleNotationType = p.compatibleNotationType;
	}

	CompatibleNotatedSiteswapPair(NotatedSiteswap prefix, NotatedSiteswap suffix) throws IncompatibleNumberOfHandsException {
		if(prefix.numHands() != suffix.numHands())
			throw new IncompatibleNumberOfHandsException();
		// determine compatibleNotationType
		switch(prefix.numHands()) {
			case 0:
				this.compatibleNotationType = suffix.notationType();
				break;
			case 1:
				this.compatibleNotationType = Notation.ASYNCHRONOUS;
				break;
			case 2:
				this.compatibleNotationType = Notation.SYNCHRONOUS;
				break;
			default:
				this.compatibleNotationType = Notation.PASSING;
		}
		this.prefix = prefix;
		this.suffix = suffix;
	}

	CompatibleNotatedSiteswapPair(String s1, int numHands1, int startHand1, String s2, int numHands2, int startHand2) throws InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException {
		// figure out if there is a compatible number of hands for both notations
		if(numHands1 != -1 && numHands2 != -1 && numHands1 != numHands2) {
			throw new IncompatibleNumberOfHandsException();
		}
		Notation n1;
		Notation n2;
		int numHands;
		if(numHands1 != -1)
			numHands = numHands1;
		else if(numHands2 != -1)
			numHands = numHands2;
		else
			numHands = -1;
		try {
			n1 = Notation.analyze(s1);
			n2 = Notation.analyze(s2);
		} catch(InvalidNotationException e) {
			throw e;
		}
		// parse the strings appropriately
		try {
			// check if either notation is emptynotation
			if(n1 == Notation.EMPTY) {
				if(numHands == -1)
					numHands = n2.defaultNumHands();
				this.prefix = new NotatedSiteswap.EmptyNotatedSiteswap(numHands);
				this.suffix = NotatedSiteswap.parseSingle(s2, numHands, startHand2);
				this.compatibleNotationType = Notation.EMPTY;
			} else if(n2 == Notation.EMPTY) {
				if(numHands == -1)
					numHands = n1.defaultNumHands();
				this.prefix = NotatedSiteswap.parseSingle(s1, numHands, startHand2);
				this.suffix = new NotatedSiteswap.EmptyNotatedSiteswap(numHands);
				this.compatibleNotationType = Notation.EMPTY;
			} else {
				// otherwise determine best way to parse inputs
				switch(numHands) {
					// if numHands was explicitly set to 1
					case 1:
						if(n1 == Notation.ASYNCHRONOUS) {
							if(n2 == Notation.ASYNCHRONOUS) {
								this.prefix = new NotatedSiteswap.OneHandedNotatedSiteswap(s1);
								this.suffix = new NotatedSiteswap.OneHandedNotatedSiteswap(s2);
								this.compatibleNotationType = Notation.ASYNCHRONOUS;
							} else
								throw new IncompatibleNumberOfHandsException(s2, numHands);
						} else
							throw new IncompatibleNumberOfHandsException(s1, numHands);
					// if numHands was explicitly set to 2
					case 2:
						this.compatibleNotationType = Notation.SYNCHRONOUS; // because a transition between them might not be synchronous
						switch(n1) {
							case ASYNCHRONOUS:
								this.prefix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s1, startHand1);
								break;
							case SYNCHRONOUS:
								this.prefix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s1);
								break;
							case MIXED:
								this.prefix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s1);
								break;
							default:
								throw new IncompatibleNumberOfHandsException(s1, numHands);
						}
						switch(n2) {
							case ASYNCHRONOUS:
								this.suffix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s2, startHand2);
								break;
							case SYNCHRONOUS:
								this.suffix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
								break;
							case MIXED:
								this.suffix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
								break;
							default:
								throw new IncompatibleNumberOfHandsException(s2, numHands);
						}
						break;
					// if numHands was not explicitly set
					// we'll have to determine the proper way to parse based just on the notation types
					case -1:
						switch(n1) {
							case ASYNCHRONOUS:
								switch(n2) {
									case ASYNCHRONOUS:
										this.compatibleNotationType = Notation.ASYNCHRONOUS;
										this.prefix = new NotatedSiteswap.OneHandedNotatedSiteswap(s1);
										this.suffix = new NotatedSiteswap.OneHandedNotatedSiteswap(s2);
										break;
									case SYNCHRONOUS:
										this.compatibleNotationType = Notation.SYNCHRONOUS;
										this.prefix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s1, startHand1);
										this.suffix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
										break;
									case MIXED:
										this.compatibleNotationType = Notation.SYNCHRONOUS;
										this.prefix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s1, startHand1);
										this.suffix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
										break;
									default: // case PASSING
										throw new IncompatibleNotationException(s1, s2);
								}
								break;
							case SYNCHRONOUS:
								this.compatibleNotationType = Notation.SYNCHRONOUS;
								this.prefix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s1);
								switch(n2) {
									case ASYNCHRONOUS:
										this.suffix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s2, startHand2);
										break;
									case SYNCHRONOUS:
										this.suffix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
										break;
									case MIXED:
										this.suffix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
										break;
									default: // case PASSING
										throw new IncompatibleNotationException(s1, s2);
								}
								break;
							case MIXED:
								this.compatibleNotationType = Notation.SYNCHRONOUS;
								this.prefix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s1);
								switch(n2) {
									case ASYNCHRONOUS:
										this.suffix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s2, startHand2);
										break;
									case SYNCHRONOUS:
										this.suffix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
										break;
									case MIXED:
										this.suffix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
										break;
									default: // case PASSING
								}
								break;
							default: // case PASSING
								this.compatibleNotationType = Notation.SYNCHRONOUS;
								this.prefix = new NotatedSiteswap.NotatedPassingSiteswap(s1);
								switch(n2) {
									case ASYNCHRONOUS:
									case SYNCHRONOUS:
									case MIXED:
										throw new IncompatibleNotationException(s1, s2);
									default: // case PASSING
										this.suffix = new NotatedSiteswap.NotatedPassingSiteswap(s2);
										break;
								}
								break;
						}
						break;
					// if numHands was explicitly set to something other than 1 or 2
					default:
						throw new IncompatibleNumberOfHandsException(s1, numHands);
				}
			}
		} catch(IncompatibleNumberOfHandsException e) {
			throw e;
		}

	}

	public NotatedSiteswap prefix() {
		return this.prefix;
	}

	public NotatedSiteswap suffix() {
		return this.suffix;
	}

}
