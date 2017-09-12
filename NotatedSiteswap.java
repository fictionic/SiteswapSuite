package siteswapsuite;

import java.util.ArrayList;

class IncompatibleNotationException extends SiteswapException {
	String s1;
	String s2;
	Notation n;
	int numHands;
	IncompatibleNotationException(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
	}
	IncompatibleNotationException(Notation n, int numHands) {
		this.n = n;
		this.numHands = numHands;
		this.s1 = null;
		this.s2 = null;
	}
	public String getMessage() {
		if(this.n == null)
			return "ERROR: notation strings `" + s1 + "' and `" + s2 + "' are incompatible";
		else {
			if(n == Notation.EMPTY)
				return "ERROR: notation type `" + n.name() + "' is incompatible with period > 0";
			else
				return "ERROR: notation type `" + n.name() + "' is incompatible with numHands==" + numHands;
		}
	}
}

class IncompatibleNumberOfHandsException extends SiteswapException {
	String inputNotation;
	int numHands;
	IncompatibleNumberOfHandsException(String inputNotation, int numHands) {
		this.inputNotation = inputNotation;
		this.numHands = numHands;
	}
	IncompatibleNumberOfHandsException() {
		this.inputNotation = null;
	}
	public String getMessage() {
		if(this.inputNotation != null)
			return "ERROR: cannot parse input string '" + this.inputNotation + " as having " + this.numHands + " hands";
		else
			return "ERROR: incompatible number of hands";
	}
}

class SprungException extends SiteswapException {
	String message;
	SprungException(String message) {
		this.message = message;
	}
	public String getMessage() {
		return this.message;
	}
}

public class NotatedSiteswap {

	private static boolean debug = false;
	private static void printf(Object o) {
		if(debug)
			System.out.println(o);
	}

	Notation notationType;
	Siteswap siteswap;

	// querying basic info
	public Notation notationType() { return this.notationType; }
	public Siteswap siteswap() { return this.siteswap; }

	// constructor
	private NotatedSiteswap(Siteswap ss, Notation notationType) {
		this.siteswap = ss;
		this.notationType = notationType;
	}

	// deep copy
	public NotatedSiteswap deepCopy() {
		return new NotatedSiteswap(this.siteswap, this.notationType);
	}

	/* ------- */
	/* PARSING */
	/* ------- */

	// pair a Siteswap with a pre-computed NotationType
	public static NotatedSiteswap assemble(Siteswap ss, Notation notationType) throws IncompatibleNotationException {
		switch(notationType) {
			case EMPTY:
				if(ss.period() == 0)
					return new EmptyNotatedSiteswap(ss);
				else
					throw new IncompatibleNotationException(notationType, -1);
			case ASYNCHRONOUS:
				if(ss.numHands() == 1)
					return new OneHandedNotatedSiteswap(ss);
				if(ss.numHands() == 2)
					return new TwoHandedAsyncNotatedSiteswap(ss); // TODO: figure out what to do here regarding startHand
				else
					throw new IncompatibleNotationException(Notation.ASYNCHRONOUS, ss.numHands());
			case SYNCHRONOUS:
				if(ss.numHands() == 2)
					return new TwoHandedSyncNotatedSiteswap(ss);
				else
					throw new IncompatibleNotationException(Notation.SYNCHRONOUS, ss.numHands());
			case MIXED:
				if(ss.numHands() == 2)
					return new TwoHandedMixedNotatedSiteswap(ss);
				else
					throw new IncompatibleNotationException(Notation.MIXED, ss.numHands());
			default: // case PASSING:
				if(ss.numHands() == 4)
					return new NotatedPassingSiteswap(ss);
				else
					throw new IncompatibleNotationException(Notation.PASSING, ss.numHands());
		}
	}

	// build a Siteswap from a notation string and pair it with the determined NotationType
	public static NotatedSiteswap parse(String inputNotation, int numHands, int startHand) throws InvalidNotationException, IncompatibleNumberOfHandsException {
		Notation n;
		try {
			n = Notation.analyze(inputNotation);
		} catch(InvalidNotationException e) {
			throw e;
		}
		switch(n) {
			case EMPTY:
				if(numHands == -1)
					numHands = 0;
				return new EmptyNotatedSiteswap(numHands);
			case ASYNCHRONOUS:
				if(numHands == -1 || numHands == 1)
					return new OneHandedNotatedSiteswap(inputNotation);
				else if(numHands == 2)
					return new TwoHandedAsyncNotatedSiteswap(inputNotation, startHand);
				else
					break;
			case SYNCHRONOUS:
				if(numHands == -1 || numHands == 2)
					return new TwoHandedSyncNotatedSiteswap(inputNotation);
				else
					break;
			case MIXED:
				if(numHands == -1 || numHands == 2)
					return new TwoHandedMixedNotatedSiteswap(inputNotation);
				else
					break;
			default: // case PASSING
				return new NotatedPassingSiteswap(inputNotation);
		}
		throw new IncompatibleNumberOfHandsException(inputNotation, numHands);
	}

	/* ---------- */
	/* DE-PARSING */
	/* ---------- */

	// de-parse a Siteswap
	public String notate(Siteswap ss) {
		if(ss.period() == 0 || ss.numHands() == 0)
			return (new EmptyNotatedSiteswap(ss)).print();
		switch(this.numHands) {
			case 1:
				return (new OneHandedNotatedSiteswap(ss).print());
			case 2:
				// assume synchronous; that's all we can know without digging through the pattern.
				// will return to this later
				return (new TwoHandedSyncNotatedSiteswap(ss).print());
			default:
				return (new NotatedPassingSiteswap(ss).print());
		}
	}

	/* -------------- */
	/* THE SUBCLASSES */
	/* -------------- */

	static class EmptyNotatedSiteswap extends NotatedSiteswap {
		EmptyNotatedSiteswap(Siteswap ss) {
			super(ss, Notation.EMPTY);
		}

		EmptyNotatedSiteswap(int numHands) {
			super(numHands);
		}

		public TwoHandedSyncNotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

		public String print() {
			return Notation.emptyNotation;
		}

	}

	static class OneHandedNotatedSiteswap extends NotatedSiteswap {

		OneHandedNotatedSiteswap(Siteswap ss) {
			super(ss, Notation.ASYNCHRONOUS);
		}

		OneHandedNotatedSiteswap(String s) {
			super(1);
			this.notationType = Notation.ASYNCHRONOUS;
			char[] a = s.toCharArray();
			char curToken;
			int i=0; //index in input string	
			int b=0; //index (beat) in output siteswap
			boolean multi = false; //whether or not we're currently in a multiplex throw
			boolean isNegative = false;
			boolean isAntitoss = false;
			while(i < a.length) {
				curToken = a[i];
				switch(curToken) {
					//comment
					case '[':
						multi = true;
						appendEmptyBeat();
						break;
					case ']':
						multi = false;
						b++;
						break;
					case '-':
						isNegative = true;
						break;
					case '_':
						isAntitoss = true;
						break;
					default:
						ExtendedInteger height = Notation.throwHeight(curToken);
						if(isNegative) {
							height.negate();
							isNegative = false;
						}
						if(!multi) {
							if(b == period()) {
								appendEmptyBeat();
							}
						}
						if(height.isInfinite()) {
							if(isAntitoss)
								addInfiniteAntitoss(b, 0, height.infiniteValue());
							else
								addInfiniteToss(b, 0, height.infiniteValue());
						} else {
							if(isAntitoss)
								addFiniteAntitoss(b, 0, height.finiteValue(), 0);
							else
								addFiniteToss(b, 0, height.finiteValue(), 0);
						}
						if(!multi)
							b++;
						isAntitoss = false;
						break;
				}
				i++;
			}
		}

		public TwoHandedSyncNotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

		public String print() {
			String out = "";
			for(int b=0; b<this.period(); b++) {
				if(this.numTossesAtSite(b, 0) > 1) {
					out += "[";
					for(int t=0; t<this.numTossesAtSite(b,0); t++) {
						out += Notation.reverseThrowHeight(this.getToss(b, 0, t));
					}
					out += "]";
				} else {
					out += Notation.reverseThrowHeight(this.getToss(b, 0, 0));
				}
			}
			return out;
		}

	}

	static class TwoHandedAsyncNotatedSiteswap extends NotatedSiteswap {
		
		private int startHand;

		TwoHandedAsyncNotatedSiteswap(Siteswap ss) {
			super(ss, Notation.ASYNCHRONOUS);
		}

		TwoHandedAsyncNotatedSiteswap(TwoHandedAsyncNotatedSiteswap ss) {
			super(ss, Notation.ASYNCHRONOUS);
			this.startHand = ss.startHand;
		}

		TwoHandedAsyncNotatedSiteswap(String s, int startHand) {
			super(2);
			this.startHand = startHand;
			this.notationType = Notation.ASYNCHRONOUS;
			char[] a = s.toCharArray();
			char curToken;
			int b = 0; //index (beat) in output siteswap
			int curHand = startHand; // which hand's turn it is to throw
			ExtendedInteger height;
			int destHand;
			boolean multi = false; //whether or not we're currently in a multiplex throw
			boolean isNegative = false;
			boolean isAntitoss = false;
			do {
				int i = 0; //index in input string
				while(i < a.length) {
					curToken = a[i];
					//update current hand
					curHand = (b + this.startHand) % 2;
					//System.out.println(curToken);
					switch(curToken) {
						//if curToken is "[", we're now in a multiplex throw, so add all subsequent tosses to the same hand until "]"
						case '[':
							multi = true;
							appendEmptyBeat();
							break;
						case ']':
							multi = false;
							b++;
							break;
							//if curToken is "-", the next toss is negative
						case '-':
							isNegative = true;
							break;
							//if curToken is anything else, it has to be a throw height (since it matched the regex for async pattern)
						default:
							height = Notation.throwHeight(curToken);
							if(isNegative) {
								height.negate();
								isNegative = false;
							}
							if(!multi) {
								//create new beat
								appendEmptyBeat();
								//add toss of correct height and destination to current hand
								if(height.isInfinite()) {
									if(isAntitoss)
										addInfiniteAntitoss(b, curHand, height.infiniteValue());
									else
										addInfiniteToss(b, curHand, height.infiniteValue());
								} else {
									destHand = (curHand + height.finiteValue()) % 2; //0=left, 1=right
									if(destHand < 0)
										destHand += 2;
									if(isAntitoss)
										addFiniteAntitoss(b, curHand, height.finiteValue(), destHand);
									else
										addFiniteToss(b, curHand, height.finiteValue(), destHand);
									//increment beat index
									b++;
								}
							} else {
								//add toss of correct height and destination to current hand
								if(height.isInfinite()) {
									if(isAntitoss)
										addInfiniteAntitoss(b, curHand, height.infiniteValue());
									else
										addInfiniteToss(b, curHand, height.infiniteValue());
								} else {
									destHand = (curHand + height.finiteValue()) % 2; //0=left, 1=right
									if(destHand < 0)
										destHand += 2;
									if(isAntitoss)
										addFiniteAntitoss(b, curHand, height.finiteValue(), destHand);
									else
										addFiniteToss(b, curHand, height.finiteValue(), destHand);
								}
							}
							isAntitoss = false;
							break;
					}
					//increment index in input string
					i++;
				}
			} while(this.period() % 2 == 1);
		}

		public TwoHandedSyncNotatedSiteswap spring() throws SprungException {
			TwoHandedSyncNotatedSiteswap newSiteswap = new TwoHandedSyncNotatedSiteswap();
			int sprungHand = (this.startHand + 1) % 2;
			for(int b=0; b<this.period(); b++) {
				newSiteswap.appendEmptyBeat();
				for(int h=0; h<2; h++) {
					if(h == sprungHand) {
						Toss newToss = new Toss(2, (h + 1) % 2, false);
						newSiteswap.addToss(b * 2, h, newToss);
					} else {
						if(this.siteIsEmpty(b, h))
							continue;
						for(int t=0; t<this.numTossesAtSite(b, h); t++) {
							Toss curToss = this.getToss(b, h, t);
							Toss newToss;
							if(curToss.height().isInfinite() || curToss.charge() == 0)
								newToss = curToss.deepCopy();
							else
								newToss = new Toss(curToss.height().finiteValue() * 2, curToss.destHand(), curToss.isAntitoss());
							newSiteswap.addToss(b * 2, h, newToss);
						}
					}
				}
				newSiteswap.appendEmptyBeat();
				sprungHand = (sprungHand + 1) % 2;
			}
			return newSiteswap;
		}

		public String print() {
			String out = "";
			int curHandIndex = this.startHand;
			//loop through beats of siteswap
			for(int b=0; b<this.period(); b++) {
				//see if we need to use multiplex notation
				if(this.numTossesAtSite(b, curHandIndex) > 1) {
					out += "[";
					//loop through tosses of current hand
					for(int t=0; t<this.numTossesAtSite(b, curHandIndex); t++) {
						out += Notation.reverseThrowHeight(this.getToss(b, curHandIndex, t));
					}
					out += "]";
				} else {
					out += Notation.reverseThrowHeight(this.getToss(b, curHandIndex, 0));
				}
				//alternate curHandIndex
				curHandIndex = (curHandIndex + 1) % 2;
			}
			return out;
		}

	}

	static class TwoHandedSyncNotatedSiteswap extends NotatedSiteswap {

		TwoHandedSyncNotatedSiteswap(Siteswap ss) {
			super(ss, Notation.SYNCHRONOUS);
		}

		TwoHandedSyncNotatedSiteswap() {
			super(2);
			this.notationType = Notation.SYNCHRONOUS;
		}

		TwoHandedSyncNotatedSiteswap(String s) {
			super(2);
			this.notationType = Notation.SYNCHRONOUS;
			char[] a = s.toCharArray();
			//create new sync siteswap
			int i = 0; //index in index string
			int b = 0; //index of beat within output siteswap
			int curHand = 0;
			int lastStarredBeat = 0;
			ExtendedInteger height;
			int destHand;
			boolean isNegative = false;
			boolean isAntitoss = false;
			char curToken;
			Toss newToss = null;
			//
			while(i < a.length) {
				curToken = a[i];
				switch(curToken) {
					case '(':
						//create new beat
						appendEmptyBeat();
						curHand = 0;
						break;
					case ',':
						curHand = 1;
						break;
					case ')':
						//add empty beat, cuz that's how sync works
						appendEmptyBeat();
						//increase beat index by 2
						b += 2;
						break;
					case '[':
						//doesn't matter whether we're in a multiplex pattern
						// b/c curHand is determined by other notation (commas and parens)
						break;
					case ']':
						break;
					case 'x':
						//toggle destination hand of most recently added toss
						newToss.starify();
						break;
					case '!':
						//remove last beat
						removeLastBeat();
						//decrement beat index
						b--;
						break;
					case '*':
						starify();
						break;
					case '-':
						isNegative = true;
						break;
					case '_':
						isAntitoss = true;
						break;
					default: //curToken is a throw height
						height = Notation.throwHeight(curToken);
						if(isNegative) {
							height.negate();
							isNegative = false;
						}
						if(height.isInfinite()) {
							if(isAntitoss) {
								newToss = new Toss(height.infiniteValue(), true);
							} else {
								newToss = new Toss(height.infiniteValue(), false);
							}
						} else {
							destHand = (curHand + height.finiteValue()) % 2;
							if(destHand < 0)
								destHand += 2;
							if(isAntitoss)
								newToss = new Toss(height.finiteValue(), destHand, true);
							else
								newToss = new Toss(height.finiteValue(), destHand, false);
							isAntitoss = false;
						}
						addToss(b, curHand, newToss);
				}
				i++;
			}
		}

		public TwoHandedSyncNotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

		public String print() {
			String out = "";
			String nextBeat;
			boolean skippedLastBeat = false;
			boolean allZeroes = true;
			//loop through beats of siteswap
			for(int b=0; b<this.period(); b++) {
				nextBeat = "(";
				allZeroes = true;
				//loop through hands within each beat (we know numHands = 2 since we screened for that in parse())
				for(int h=0; h<2; h++) {
					printf("nextBeat: " + nextBeat);
					//see if we need to add multiplex notation
					if(this.numTossesAtSite(b, h) > 1) {
						nextBeat += "[";
						//loop through tosses within hand
						for(int t=0; t<this.numTossesAtSite(b, h); t++) {
							Toss curToss = this.getToss(b, h, t);
							printf(curToss);
							nextBeat += Notation.reverseThrowHeight(curToss);
							if(curToss.charge() != 0) {
								allZeroes = false;
								if(!curToss.height().isInfinite() && curToss.destHand() != (h + Math.abs(curToss.height().finiteValue())) % 2) {
									nextBeat += "x";
								}
							}
						}
						nextBeat += "]";
					} else {
						//account for only toss in hand
						Toss curToss = this.getToss(b, h, 0);
						if(curToss.charge() != 0) {
							printf("encountered non-zero toss");
							printf(curToss);
							allZeroes = false;
						}
						nextBeat += Notation.reverseThrowHeight(curToss);
						if(!curToss.height().isInfinite() && curToss.destHand() != (h + Math.abs(curToss.height().finiteValue())) % 2) {
							nextBeat += "x";
						}
					}
					//put a comma if we've just finished doing the left hand
					if(h == 0) {
						nextBeat += ",";
					}
				}
				nextBeat += ")";
				if(b == 0) {
					printf("not skipping beat 0");
					out += nextBeat;
					skippedLastBeat = false;
				} else if(!skippedLastBeat && allZeroes) {
					// skip this beat
					printf("skipping beat " + b);
					skippedLastBeat = true;
				} else {
					// don't skip this beat
					printf("not skipping beat " + b);
					if(!skippedLastBeat)
						out += "!";
					out += nextBeat;
					skippedLastBeat = false;
				}
			}
			if(!skippedLastBeat) {
				printf("adding final '!'");
				out += "!";
			}
			return out;
		}

	}

	static class TwoHandedMixedNotatedSiteswap extends NotatedSiteswap {

		TwoHandedMixedNotatedSiteswap(Siteswap ss) {
			super(ss, Notation.MIXED);
		}

		TwoHandedMixedNotatedSiteswap(String s) {
			super(2);
			System.out.println("Parsing of mixed notation not yet implemented...");
			System.exit(1);
		}

		public TwoHandedSyncNotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

		public String print() {
			System.out.println("de-parsing of mixed notation not yet implemented...");
			System.exit(1);
			return null;
		}

	}

	static class NotatedPassingSiteswap extends NotatedSiteswap {

		NotatedPassingSiteswap(Siteswap ss) {
			super(ss, Notation.PASSING);
		}

		NotatedPassingSiteswap(String s) {
			super(4);
			System.out.println("Parsing of passing notation not yet implemented...");
			System.exit(1);
		}

		public TwoHandedSyncNotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

		public String print() {
			System.out.println("de-parsing of passing notation not yet implemented...");
			System.exit(1);
			return null;
		}

	}

}
