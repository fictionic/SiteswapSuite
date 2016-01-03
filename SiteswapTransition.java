public class SiteswapTransition {
	private Siteswap prefix, transition, suffix;
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

	public SiteswapTransition(Siteswap from, Siteswap via, Siteswap to) {
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

	public Siteswap.Beat getBeat(int b) {
		if(b < 0) {
			return prefix.getBeat(b);
		} else if(b < transitionLength) {
			return transition.getBeat(b);
		} else {
			return suffix.getBeat(b - transitionLength);
		}
	}

	public boolean addToss(int atBeat, int atHand, int tossHeight, int destHand) {
		if(atBeat < 0) {
			return prefix.addToss(atBeat, atHand, tossHeight, destHand);
		} else if(atBeat < transitionLength) {
			return transition.addToss(atBeat, atHand, tossHeight, destHand);
		} else {
			return transition.addToss(atBeat, atHand, tossHeight, destHand);
		}
	}

	public Siteswap getUnAntiTossifiedTransition() {
		Siteswap newTransition = new Siteswap(transition.numHands(), transition.type());
		//
		Integer newTransitionStart = null, newTransitionEnd = null;
		int destBeat, destHand, tossHeight;
		Siteswap.Beat.Hand curHand;
		Siteswap.Beat.Hand.Toss curToss;
		// un-antitossify transition
		printf("un-antitossifying transition...");
		for(int b=0; b<transition.period(); b++) {
			printf("b: " + b);
			for(int h=0; h<transition.numHands(); h++) {
				curHand = transition.getBeat(b).getHand(h);
				for(int t=0; t<curHand.numTosses(); t++) {
					curToss = curHand.getToss(t);
					if(curToss.isAntiToss()) {
						destBeat = b + curToss.height();
						tossHeight = -curToss.height();
					} else {
						destBeat = b;
						tossHeight = curToss.height();
					}
					newTransition.strictAddToss(destBeat, h, tossHeight, curToss.destHand());
					printf(newTransition);
					// update endpoints
					if(newTransitionStart == null || destBeat < newTransitionStart)
						newTransitionStart = destBeat;
					if(newTransitionEnd == null || destBeat > newTransitionEnd)
						newTransitionEnd = destBeat;
				}
			}
		}
		printf("start: " + newTransitionStart);
		printf("end: " + newTransitionEnd);
		// un-antitossify prefix
		printf("un-antitossifying prefix...");
		for(int b=-prefixLength; b<0; b++) {
			printf("b: " + b);
			for(int h=0; h<prefix.numHands(); h++) {
				curHand = prefix.getBeat(b).getHand(h);
				for(int t=0; t<curHand.numTosses(); t++) {
					curToss = curHand.getToss(t);
					printf(curToss);
					if(curToss.isAntiToss()) {
						destBeat = b + curToss.height();
						printf("destBeat: " + destBeat);
						tossHeight = -curToss.height();
						if(tossHeight != 0 && destBeat >= newTransitionStart) {
							newTransition.addToss(destBeat, h, tossHeight, curToss.destHand());
							printf(Parser.deParse(newTransition));
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
				for(int h=0; h<suffix.numHands(); h++) {
					curHand = getBeat(b).getHand(h);
					for(int t=0; t<curHand.numTosses(); t++) {
						curToss = curHand.getToss(t);
						printf(curToss);
						if(curToss.isAntiToss()) {
							destBeat = b + curToss.height();
							tossHeight = -curToss.height();
						} else {
							destBeat = b;
							tossHeight = curToss.height();
						}
						printf("destBeat: " + destBeat);
						if(tossHeight != 0 && (destBeat <= newTransitionEnd || !skippedAll)) {
							newTransition.strictAddToss(destBeat, h, tossHeight, curToss.destHand());
							printf(Parser.deParse(newTransition));
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
			base += suffixLength;
		} while(!skippedAll);
		printf("skipped all; done");
		return newTransition.getCopyOfSubPattern(newTransitionStart, newTransitionEnd);
	}

	public static void main(String[] args) {
		if(args.length == 3) {
			Siteswap prefix = Parser.parse(args[0]);
			prefix.antiTossify();
			Siteswap transition = Parser.parse(args[1]);
			transition.antiTossify();
			Siteswap suffix = Parser.parse(args[2]);
			suffix.antiTossify();
			SiteswapTransition t = new SiteswapTransition(prefix, transition, suffix);
			printf("full ss: " + Parser.deParse(t));
			printf("new transition: " + Parser.deParse(t.getUnAntiTossifiedTransition()));
		} else {
			printf("need 3 args");
		}
	}
}
