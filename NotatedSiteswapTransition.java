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

	public static NotatedSiteswapTransition parseStrings(String s1, String s2, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) {
		Notation n1, n2;
		int numHands;
		NotatedSiteswap from = null, transition = null, to = null;
		try {
			n1 = Notation.analyze(s1);
			n2 = Notation.analyze(s2);
			// determine if there is a compatible way to parse the inputs
			// (so the resulting siteswaps have the same number of hands)
			if(n1 == Notation.ASYNCHRONOUS && n2 == Notation.ASYNCHRONOUS)
				numHands = 1;
			else if(n1 == Notation.ASYNCHRONOUS && n2 == Notation.SYNCHRONOUS
					|| n2 == Notation.SYNCHRONOUS && n2 == Notation.ASYNCHRONOUS)
				numHands = 2;
			else if(n1 == Notation.SYNCHRONOUS && n2 == Notation.SYNCHRONOUS)
				numHands = 2;
			else if(n1 == Notation.PASSING && n2 == Notation.PASSING)
				numHands = 4;
			else
				throw new IncompatibleNotationException();
			// parse notation into notatedsiteswaps
			// first the prefix
			from = NotatedSiteswap.parse(s1, numHands);
			// then the suffix
			to = NotatedSiteswap.parse(s2, numHands);
			// now get transition
			// first pick a valid notationtype for it to have based on prefix/suffix
			Notation transitionNotationType = Notation.defaultNotationType(numHands);
			transition = NotatedSiteswap.assemble(Transition.compute(from, to, allowExtraSqueezeCatches, generateBallAntiballPairs), transitionNotationType);
		} catch(InvalidNotationException | IncompatibleNotationException e) {
			System.err.println("error: invalid notation");
			System.exit(1);
		}
		return new NotatedSiteswapTransition(from, transition, to);
	}

	private NotatedSiteswapTransition(NotatedSiteswap from, NotatedSiteswap via, NotatedSiteswap to) {
		this.prefix = from;
		this.prefixLength = from.period();
		this.transition = via;
		this.transitionLength = via.period();
		this.suffix = to;
		this.suffixLength = to.period();
	}

	public Siteswap prefix() {
		return this.prefix;
	}

	public Siteswap transition() {
		return this.transition;
	}

	public Siteswap suffix() {
		return this.suffix;
	}

	private MutableSiteswap.Site getSite(int atBeat, int handIndex) {
		if(atBeat < 0) {
			return prefix.getSite(atBeat, handIndex);
		} else if(atBeat < transitionLength) {
			return transition.getSite(atBeat, handIndex);
		} else {
			return suffix.getSite(atBeat - transitionLength, handIndex);
		}
	}

	private Toss getToss(int atBeat, int fromHand, int tossIndex) {
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
				prefix = NotatedSiteswap.parse(args[0]);
				prefix.antitossify();
				transition = NotatedSiteswap.parse(args[1]);
				transition.antitossify();
				suffix = NotatedSiteswap.parse(args[2]);
				suffix.antitossify();
				NotatedSiteswapTransition t = new NotatedSiteswapTransition(prefix, transition, suffix);
				System.out.println(t.print());
			} catch(InvalidNotationException | IncompatibleNotationException e) {
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
