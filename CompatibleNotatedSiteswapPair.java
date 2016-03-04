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
			default:
				this.compatibleNotationType = Notation.PASSING;
		}
		this.prefix = prefix;
		this.suffix = suffix;
	}

	CompatibleNotatedSiteswapPair(String s1, int startHand1, String s2, int startHand2) throws InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException {
		// figure out if there is a compatible number of hands for both notations
		Notation n1;
		Notation n2;
		int numHands;
		try {
			n1 = Notation.analyze(s1);
			n2 = Notation.analyze(s2);
		} catch(InvalidNotationException e) {
			throw e;
		}
		// parse the strings appropriately
		try {
			if(n1 == Notation.EMPTY) {
				numHands = n2.defaultNumHands();
				this.prefix = new NotatedSiteswap.EmptyNotatedSiteswap(numHands);
				this.suffix = NotatedSiteswap.parseSingle(s2, numHands, startHand2);
				this.compatibleNotationType = Notation.EMPTY;
			} else if(n2 == Notation.EMPTY) {
				numHands = n1.defaultNumHands();
				this.prefix = NotatedSiteswap.parseSingle(s1, numHands, startHand2);
				this.suffix = new NotatedSiteswap.EmptyNotatedSiteswap(numHands);
				this.compatibleNotationType = Notation.EMPTY;
			} else if(n1 == Notation.ASYNCHRONOUS) {
				if(n2 == Notation.ASYNCHRONOUS) {
					numHands = 1;
					this.prefix = new NotatedSiteswap.OneHandedNotatedSiteswap(s1);
					this.suffix = new NotatedSiteswap.OneHandedNotatedSiteswap(s2);
					this.compatibleNotationType = Notation.ASYNCHRONOUS;
				} else if(n2 == Notation.SYNCHRONOUS) {
					numHands = 2;
					this.prefix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s1, startHand1);
					this.suffix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
					this.compatibleNotationType = Notation.SYNCHRONOUS;
				} else if(n2 == Notation.MIXED) {
					numHands = 2;
					this.prefix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s1, startHand1);
					this.suffix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
					this.compatibleNotationType = Notation.SYNCHRONOUS;
				} else {
					throw new IncompatibleNotationException(s1, s2);
				}
			} else if(n1 == Notation.SYNCHRONOUS) {
				numHands = 2;
				this.compatibleNotationType = Notation.SYNCHRONOUS;
				if(n2 == Notation.ASYNCHRONOUS) {
					this.prefix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s1);
					this.suffix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s2, startHand2);
				} else if(n2 == Notation.SYNCHRONOUS) {
					this.prefix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s1);
					this.suffix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
				} else if(n2 == Notation.MIXED) {
					this.prefix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s1);
					this.suffix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
				} else {
					throw new IncompatibleNotationException(s1, s2);
				}
			} else if(n1 == Notation.MIXED) {
				numHands = 2;
				this.compatibleNotationType = Notation.SYNCHRONOUS;
				if(n2 == Notation.ASYNCHRONOUS) {
					this.prefix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s1);
					this.suffix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s2, startHand2);
				} else if(n2 == Notation.SYNCHRONOUS) {
					this.prefix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s1);
					this.suffix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
				} else if(n2 == Notation.MIXED) {
					this.prefix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s1);
					this.suffix = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
				} else {
					throw new IncompatibleNotationException(s1, s2);
				}
			} else { // n1 == Notation.PASSING
				if(n2 == Notation.PASSING) {
					numHands = 4;
					this.compatibleNotationType = Notation.PASSING;
					this.prefix = new NotatedSiteswap.NotatedPassingSiteswap(s1);
					this.suffix = new NotatedSiteswap.NotatedPassingSiteswap(s2);
				} else {
					throw new IncompatibleNotationException(s1, s2);
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
