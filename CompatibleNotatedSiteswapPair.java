package siteswapsuite;

class CompatibleNotatedSiteswapPair {
	NotatedSiteswap prefix, suffix;
	Notation compatibleNotationType;

	CompatibleNotatedSiteswapPair(CompatibleNotatedSiteswapPair p) {
		this.prefix = p.prefix;
		this.suffix = p.suffix;
		this.compatibleNotationType = p.compatibleNotationType;
	}

	CompatibleNotatedSiteswapPair(String s1, String s2) throws InvalidNotationException, IncompatibleNotationException {
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
		if(n1 == Notation.EMPTY) {
			numHands = n2.defaultNumHands();
			this.prefix = new NotatedSiteswap.EmptyNotatedSiteswap(numHands);
			this.suffix = NotatedSiteswap.parseSingle(s2);
			this.compatibleNotationType = Notation.EMPTY;
		} else if(n2 == Notation.EMPTY) {
			numHands = n1.defaultNumHands();
			this.prefix = NotatedSiteswap.parseSingle(s1);
			this.suffix = new NotatedSiteswap.EmptyNotatedSiteswap(numHands);
			this.compatibleNotationType = Notation.EMPTY;
			System.out.println("BBBBB");
		} else if(n1 == Notation.ASYNCHRONOUS) {
			if(n2 == Notation.ASYNCHRONOUS) {
				numHands = 1;
				this.prefix = new NotatedSiteswap.OneHandedNotatedSiteswap(s1);
				this.suffix = new NotatedSiteswap.OneHandedNotatedSiteswap(s2);
				this.compatibleNotationType = Notation.ASYNCHRONOUS;
			} else if(n2 == Notation.SYNCHRONOUS) {
				numHands = 2;
				this.prefix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s1);
				this.suffix = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
				this.compatibleNotationType = Notation.SYNCHRONOUS;
			} else if(n2 == Notation.MIXED) {
				numHands = 2;
				this.prefix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s1);
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
				this.suffix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s2);
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
				this.suffix = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s2);
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
	}

	public NotatedSiteswap prefix() {
		return this.prefix;
	}

	public NotatedSiteswap suffix() {
		return this.suffix;
	}

}
