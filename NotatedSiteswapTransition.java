package siteswapsuite;

public class NotatedSiteswapTransition {

	private static String sep = " | ";

	private NotatedSiteswap prefix, transition, suffix;
	private int prefixLength, transitionLength, suffixLength;

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

	public static NotatedSiteswapTransition parseStrings(String s1, String s2, int minLength, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs)
		throws IncompatibleNotationException, InvalidNotationException {
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
		NotatedSiteswap nss1;
		NotatedSiteswap nss2;
		// parse the strings appropriately
		if(n1 == Notation.EMPTY) {
			numHands = n2.defaultNumHands();
			nss1 = new NotatedSiteswap.EmptyNotatedSiteswap(numHands);
			nss2 = NotatedSiteswap.parseSingle(s2);
		} else if(n2 == Notation.EMPTY) {
			numHands = n1.defaultNumHands();
			nss1 = NotatedSiteswap.parseSingle(s1);
			nss2 = new NotatedSiteswap.EmptyNotatedSiteswap(numHands);
		} else if(n1 == Notation.ASYNCHRONOUS) {
			if(n2 == Notation.ASYNCHRONOUS) {
				numHands = 1;
				nss1 = new NotatedSiteswap.OneHandedNotatedSiteswap(s1);
				nss2 = new NotatedSiteswap.OneHandedNotatedSiteswap(s2);
			} else if(n2 == Notation.SYNCHRONOUS) {
				numHands = 2;
				nss1 = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s1);
				nss2 = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
			} else if(n2 == Notation.MIXED) {
				numHands = 2;
				nss1 = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s1);
				nss2 = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
			} else {
				throw new IncompatibleNotationException(s1, s2);
			}
		} else if(n1 == Notation.SYNCHRONOUS) {
			numHands = 2;
			if(n2 == Notation.ASYNCHRONOUS) {
				nss1 = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s1);
				nss2 = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s2);
			} else if(n2 == Notation.SYNCHRONOUS) {
				nss1 = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s1);
				nss2 = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
			} else if(n2 == Notation.MIXED) {
				nss1 = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s1);
				nss2 = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
			} else {
				throw new IncompatibleNotationException(s1, s2);
			}
		} else if(n1 == Notation.MIXED) {
			numHands = 2;
			if(n2 == Notation.ASYNCHRONOUS) {
				nss1 = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s1);
				nss2 = new NotatedSiteswap.TwoHandedAsyncNotatedSiteswap(s2);
			} else if(n2 == Notation.SYNCHRONOUS) {
				nss1 = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s1);
				nss2 = new NotatedSiteswap.TwoHandedSyncNotatedSiteswap(s2);
			} else if(n2 == Notation.MIXED) {
				nss1 = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s1);
				nss2 = new NotatedSiteswap.TwoHandedMixedNotatedSiteswap(s2);
			} else {
				throw new IncompatibleNotationException(s1, s2);
			}
		} else { // n1 == Notation.PASSING
			if(n2 == Notation.PASSING) {
				numHands = 4;
				nss1 = new NotatedSiteswap.NotatedPassingSiteswap(s1);
				nss2 = new NotatedSiteswap.NotatedPassingSiteswap(s2);
			} else {
				throw new IncompatibleNotationException(s1, s2);
			}
		}
		// now get transition
		// first pick a valid notationtype for it to have based on prefix/suffix
		Notation transitionNotationType = Notation.defaultNotationType(numHands);
		NotatedSiteswap t = NotatedSiteswap.assemble(Transition.compute(new State(nss1), new State(nss2), minLength, allowExtraSqueezeCatches, generateBallAntiballPairs), transitionNotationType);
		return new NotatedSiteswapTransition(nss1, t, nss2);
	}

	private NotatedSiteswapTransition(NotatedSiteswap from, NotatedSiteswap via, NotatedSiteswap to) {
		this.prefix = from;
		this.prefixLength = from.period();
		this.transition = via;
		this.transitionLength = via.period();
		this.suffix = to;
		this.suffixLength = to.period();
	}

	public NotatedSiteswap prefix() {
		return this.prefix;
	}

	public NotatedSiteswap transition() {
		return this.transition;
	}

	public NotatedSiteswap suffix() {
		return this.suffix;
	}

	MutableSiteswap.Site getSite(int atBeat, int handIndex) {
		if(atBeat < 0) {
			return prefix.getSite(atBeat, handIndex);
		} else if(atBeat < transitionLength) {
			return transition.getSite(atBeat, handIndex);
		} else {
			return suffix.getSite(atBeat - transitionLength, handIndex);
		}
	}

	public Toss getToss(int atBeat, int fromHand, int tossIndex) {
		if(atBeat < 0) {
			return this.getSite(atBeat, fromHand).getToss(tossIndex);
		} else if(atBeat < transitionLength) {
			return transition.getSite(atBeat, fromHand).getToss(tossIndex);
		} else {
			return suffix.getSite(atBeat - transitionLength, fromHand).getToss(tossIndex);
		}
	}

	// adding tosses
	public void addFiniteToss(int atBeat, int fromHand, int height, int toHand) {
		this.getSite(atBeat, fromHand).addToss(new Toss(height, toHand, false));
	}

	public void addFiniteAntitoss(int atBeat, int fromHand, int height, int toHand) {
		this.getSite(atBeat, fromHand).addToss(new Toss(height, toHand, true));
	}

	public void addInfiniteToss(int atBeat, int fromHand, InfinityType height) {
		this.getSite(atBeat, fromHand).addToss(new Toss(height, false));
	}

	public void addInfiniteAntitoss(int atBeat, int fromHand, InfinityType height) {
		this.getSite(atBeat, fromHand).addToss(new Toss(height, true));
	}

	/*public Siteswap getUnAntiTossifiedTransition() {
		Siteswap newTransition = new Siteswap(transition.numHands());
		//
		Integer newTransitionStart = null, newTransitionEnd = null;
		int destBeat, destHand;
		Toss curToss;
		// un-antitossify transition
		printf("un-antitossifying transition...");
		for(int b=0; b<transition.period(); b++) {
			printf("b: " + b);
			for(int t=0; t<curHand.numTossesAtSite(b, h); t++) {
				curToss = getToss(b, h, t);
				// if it's an antitoss, make it a regular toss and put in its proper place in the output siteswap
				ExtendedInteger tossHeight;
				if(curToss.isAntitoss()) {
					if(curToss.height().isInfinite()) {
						destBeat = b;
						tossHeight = new ExtendedInteger(curToss.height().infiniteValue());
						tossHeight.negate();
						newTransition.addInfiniteToss(destBeat, h, tossHeight.infiniteValue(), curToss.destHand());
					} else {
						destBeat = b + curToss.height();
						tossHeight = new ExtendedInteger(curToss.height().finiteValue());
						tossHeight.negate();
						newTransition.addFiniteToss(destBeat, h, tossHeight.finiteValue(), curToss.destHand());
					}
				} else {
					destBeat = b;
					if(curToss.height().isInfinite()) {
						tossHeight = new ExtendedInteger(curToss.height().infiniteValue());
						newTransition.addInfiniteToss(destBeat, h, tossHeight.infiniteValue(), curToss.destHand());
					} else {
						tossHeight = new ExtendedInteger(curToss.height().finiteValue());
						newTransition.addFiniteToss(destBeat, h, tossHeight.finiteValue(), curToss.destHand());
					}
				}
				destBeat = newTransition.extendToBeatIndex(destBeat);
				printf(newTransition);
				// update endpoints
				if(newTransitionStart == null || destBeat < newTransitionStart)
					newTransitionStart = destBeat;
				if(newTransitionEnd == null || destBeat > newTransitionEnd)
					newTransitionEnd = destBeat;
			}
		}
		printf("start: " + newTransitionStart);
		printf("end: " + newTransitionEnd);
		// un-antitossify prefix
		printf("un-antitossifying prefix...");
		for(int b=-prefixLength; b<0; b++) {
			printf("b: " + b);
			for(int h=0; h<prefix.numHands(); h++) {
				for(int t=0; t<numTossesAtSite(b, h); t++) {
					curToss = getToss(b, h, t);
					printf(curToss);
					if(curToss.isAntitoss()) {
						destBeat = b + curToss.height();
						printf("destBeat: " + destBeat);
						tossHeight = -curToss.height();
						if(tossHeight != 0 && destBeat >= newTransitionStart) {
							newTransition.addToss(destBeat, h, tossHeight, curToss.destHand());
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
		// un-antitossify suffix
		printf("un-antitossifying suffix...");
		int shiftAmount = 0;
		boolean skippedAll;
		int b, base = transitionLength;
		do {
			printf("trying another period");
			skippedAll = true;
			for(int i=0; i<suffixLength; i++) {
				b = base + i;
				printf("b: " + b);
				for(int h=0; h<suffix.numHands(); h++) {b
					curHand = getBeat(b).getHand(h);
					for(int t=0; t<curHand.numTosses(); t++) {a
						curToss = curHand.getToss(t);
						printf(curToss);
						if(curToss.isAntitoss()) {
							destBeat = b + curToss.height();
							tossHeight = -curToss.height();
						} else {
							destBeat = b;
							tossHeight = curToss.height();
						}
						printf("destBeat: " + destBeat);
						if(tossHeight != 0 && (destBeat <= newTransitionEnd || !skippedAll)) {
							destBeat = newTransition.extendToBeatIndex(destBeat);
							newTransition.addToss(destBeat, h, tossHeight, curToss.destHand());
							skippedAll = false;
							if(destBeat < newTransitionStart)
								newTransitionStart = destBeat;
							if(destBeat > newTransitionEnd)
								newTransitionEnd = destBeat;
						} else 
							printf("skip");
					}a
				}b
			}
			base += suffixLength;
		} while(!skippedAll);
		printf("skipped all; done");
		return newTransition.subPattern(newTransitionStart, newTransitionEnd);
	}*/

	public String print() {
		return prefix.print() + sep + transition.print() + sep + suffix.print();
	}

	public static void main(String[] args) {
		if(args.length == 3) {
			NotatedSiteswap prefix;
			NotatedSiteswap transition;
			NotatedSiteswap suffix;
			try {
				prefix = NotatedSiteswap.parseSingle(args[0]);
				prefix.antitossify();
				transition = NotatedSiteswap.parseSingle(args[1]);
				transition.antitossify();
				suffix = NotatedSiteswap.parseSingle(args[2]);
				suffix.antitossify();
				NotatedSiteswapTransition t = new NotatedSiteswapTransition(prefix, transition, suffix);
				System.out.println(t.print());
			} catch(InvalidNotationException e) {
				System.out.println("error");
				System.exit(1);
			}
			//printf("full ss: " + Parser.deParse(t));
			//printf("new transition: " + Parser.deParse(t.getUnAntiTossifiedTransition()));
		} else {
			printf("need 3 args");
		}
	}
}
