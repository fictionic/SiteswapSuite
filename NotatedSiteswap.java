package siteswapsuite;

import java.util.ArrayList;

class IncompatibleNotationException extends Exception {
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
			return "notation strings `" + s1 + "' and `" + s2 + "' are incompatible";
		else {
			if(n == Notation.EMPTY)
				return "notation type `" + n.name() + "' is incompatible with period > 0";
			else
				return "notation type `" + n.name() + "' is incompatible with numHands==" + numHands;
		}
	}
}

public abstract class NotatedSiteswap extends MutableSiteswap {

	private static boolean debug = false;
	private static void printf(Object o) {
		if(debug)
			System.out.println(o);
	}

	protected Notation notationType;

	// querying basic info
	public Notation notationType() {
		return this.notationType;
	}

	/* ------- */
	/* PARSING */
	/* ------- */

	// link to MutableSiteswap() constructor for subclasses
	private NotatedSiteswap(int numHands) {
		super(numHands);
	}

	public static NotatedSiteswap assemble(MutableSiteswap ss) throws IncompatibleNotationException {
		try {
			return assemble(ss, Notation.defaultNotationType(ss.numHands()));
		} catch(IncompatibleNotationException e) {
			throw e;
		}
	}

	public static NotatedSiteswap assemble(MutableSiteswap ss, Notation notationType) throws IncompatibleNotationException {
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
					return new TwoHandedAsyncNotatedSiteswap(ss);
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

	// construct a NotatedSiteswap out of a siteswap and a notationtype
	private NotatedSiteswap(MutableSiteswap ss, Notation notationType) {
		super(ss.numHands, ss.sites);
		this.notationType = notationType;
		// (no compatibility checking is done because it's only called by inner classes
		// as a result of methods it itself calls)
	}

	// construct a NotatedSiteswap out of a siteswap, guessing a good notationtype
	public NotatedSiteswap(MutableSiteswap ss) {
		this(ss, Notation.defaultNotationType(ss.numHands()));
	}

	public static NotatedSiteswap parseSingle(String inputNotation) throws InvalidNotationException {
		Notation n;
		try {
			n = Notation.analyze(inputNotation);
		} catch(InvalidNotationException e) {
			throw e;
		}
		switch(n) {
			case EMPTY:
				return new EmptyNotatedSiteswap(0);
			case ASYNCHRONOUS:
				return new OneHandedNotatedSiteswap(inputNotation);
			case SYNCHRONOUS:
				return new TwoHandedSyncNotatedSiteswap(inputNotation);
			case MIXED:
				return new TwoHandedMixedNotatedSiteswap(inputNotation);
			default: // case PASSING
				return new NotatedPassingSiteswap(inputNotation);
		}
	}

	/* ---------- */
	/* DE-PARSING */
	/* ---------- */

	public abstract String print();

	// de-parse a Siteswap
	public String notate(MutableSiteswap ss) {
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

	static class EmptyNotatedSiteswap extends NotatedSiteswap {
		EmptyNotatedSiteswap(MutableSiteswap ss) {
			super(ss, Notation.EMPTY);
		}

		EmptyNotatedSiteswap(int numHands) {
			super(numHands);
		}

		public String print() {
			return Notation.emptyNotation;
		}
	}

	static class OneHandedNotatedSiteswap extends NotatedSiteswap {

		OneHandedNotatedSiteswap(MutableSiteswap ss) {
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

		TwoHandedAsyncNotatedSiteswap(MutableSiteswap ss) {
			super(ss, Notation.ASYNCHRONOUS);
		}

		TwoHandedAsyncNotatedSiteswap(String s) {
			super(2);
			this.notationType = Notation.ASYNCHRONOUS;
			//double string length if it's odd (so e.g. "3" will become (3,0)!(0,3)!) 
			if(s.length() % 2 == 1) {
				s += s;
			}
			char[] a = s.toCharArray();
			char curToken;
			int i = 0; //index in input string
			int b = 0; //index (beat) in output siteswap
			int curHand = 0; // which hand's turn it is to throw
			ExtendedInteger height;
			int destHand;
			boolean multi = false; //whether or not we're currently in a multiplex throw
			boolean isNegative = false;
			boolean isAntitoss = false;
			while(i < a.length) {
				curToken = a[i];
				//update current hand
				curHand = b % 2;
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
		}

		public String print() {
			String out = "";
			int curHandIndex;
			//determine which hand throws first
			if(this.siteIsEmpty(0, 0)) {
				curHandIndex = 1;
			} else {
				curHandIndex = 0;
			}
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

		TwoHandedSyncNotatedSiteswap(MutableSiteswap ss) {
			super(ss, Notation.SYNCHRONOUS);
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
			boolean lookForX = false;
			char curToken;
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
						getToss(period(), curHand, numTossesAtSite(period(), curHand) - 1).starify();
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
							if(isAntitoss)
								addInfiniteAntitoss(b, curHand, height.infiniteValue());
							else
								addInfiniteToss(b, curHand, height.infiniteValue());
						} else {
							destHand = (curHand + height.finiteValue()) % 2;
							if(isAntitoss)
								addFiniteAntitoss(b, curHand, height.finiteValue(), destHand);
							else
								addFiniteToss(b, curHand, height.finiteValue(), destHand);
							isAntitoss = false;
							break;
						}
				}
				i++;
			}
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
					//see if we need to add multiplex notation
					if(this.numTossesAtSite(b, h) > 1) {
						nextBeat += "[";
						//loop through tosses within hand
						for(int t=0; t<this.numTossesAtSite(b, h); t++) {
							Toss curToss = this.getToss(b, h, t);
							printf(curToss.charge());
							if(curToss.charge() != 0)
								allZeroes = false;
							out += Notation.reverseThrowHeight(curToss);
							if(!curToss.height().isInfinite() && curToss.destHand() != (h + Math.abs(curToss.height().finiteValue())) % 2) {
								nextBeat += "x";
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

		TwoHandedMixedNotatedSiteswap(MutableSiteswap ss) {
			super(ss, Notation.MIXED);
		}

		TwoHandedMixedNotatedSiteswap(String s) {
			super(2);
			System.out.println("Parsing of mixed notation not yet implemented...");
			System.exit(1);
		}

		public String print() {
			System.out.println("de-parsing of mixed notation not yet implemented...");
			System.exit(1);
			return null;
		}
	}

	static class NotatedPassingSiteswap extends NotatedSiteswap {

		NotatedPassingSiteswap(MutableSiteswap ss) {
			super(ss, Notation.PASSING);
		}

		NotatedPassingSiteswap(String s) {
			super(4);
			System.out.println("Parsing of passing notation not yet implemented...");
			System.exit(1);
		}

		public String print() {
			System.out.println("de-parsing of passing notation not yet implemented...");
			System.exit(1);
			return null;
		}
	}

	public static void main(String[] args) {
		if(args.length == 1) {
			try {
				NotatedSiteswap nss = NotatedSiteswap.parseSingle(args[0]);
				System.out.println("parsed: " + nss.toString());
				String s = nss.print();
				System.out.println("de-parsed: " + s);
				//
				//MutableSiteswap mss = nss;
				//NotatedSiteswap blah = NotatedSiteswap.assemble(mss, Notation.SYNCHRONOUS);
				//System.out.println(blah.print());
			} catch(InvalidNotationException e) {
				System.out.println("invalid notation");
			}/* catch(IncompatibleNotationException e) {
				System.out.println("notation type incompatible with given number of hands");
			}*/
		}
	}
}

