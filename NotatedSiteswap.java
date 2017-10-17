package siteswapsuite;

import java.util.ArrayList;

class IncompatibleNotationException extends SiteswapException {
	String s1;
	String s2;
	SiteswapNotation n;
	int numHands;
	IncompatibleNotationException(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
	}
	IncompatibleNotationException(SiteswapNotation n, int numHands) {
		this.n = n;
		this.numHands = numHands;
		this.s1 = null;
		this.s2 = null;
	}
	public String getMessage() {
		if(this.n == null)
			return "ERROR: notation strings `" + s1 + "' and `" + s2 + "' are incompatible";
		else {
			if(n == SiteswapNotation.EMPTY) {
				return "ERROR: notation type `" + n.name() + "' is incompatible with period > 0";
			} else {
				return "ERROR: notation type `" + n.name() + "' is incompatible with numHands==" + numHands;
			}
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

public abstract class NotatedSiteswap {

	SiteswapNotation notationType;
	Siteswap siteswap;

	// querying basic info
	public SiteswapNotation notationType() { return this.notationType; }
	public Siteswap siteswap() { return this.siteswap; }

	// printing notation
	public abstract String print();

	// deep copy
	public abstract NotatedSiteswap deepCopy();

	// spring
	public abstract NotatedSiteswap spring() throws SprungException;

	// constructor
	private NotatedSiteswap(Siteswap ss, SiteswapNotation notationType) {
		this.siteswap = ss;
		this.notationType = notationType;
	}

	/* -------------- */
	/* STATIC METHODS */
	/* -------------- */

	public static NotatedSiteswap assembleAutomatic(Siteswap ss) {
		SiteswapNotation notationType = SiteswapNotation.defaultNotationType(ss.numHands());
		switch(notationType) {
			case EMPTY:
				return new EmptyNotatedSiteswap(ss);
			case ASYNCHRONOUS:
				return new OneHandedNotatedSiteswap(ss);
			case SYNCHRONOUS:
				return new TwoHandedSyncNotatedSiteswap(ss);
			default: // case PASSING
				return new NotatedPassingSiteswap(ss);
		}
	}

	// pair a Siteswap with a pre-computed NotationType
	public static NotatedSiteswap assemble(Siteswap ss, SiteswapNotation notationType) throws IncompatibleNotationException {
		switch(notationType) {
			case EMPTY:
				if(ss.period() == 0) {
					return new EmptyNotatedSiteswap(ss);
				} else {
					throw new IncompatibleNotationException(notationType, -1);
				}
			case ASYNCHRONOUS:
				if(ss.numHands() == 1) {
					return new OneHandedNotatedSiteswap(ss);
				} if(ss.numHands() == 2) {
					return new TwoHandedAsyncNotatedSiteswap(ss); // TODO: figure out what to do here regarding startHand
				} else {
					throw new IncompatibleNotationException(SiteswapNotation.ASYNCHRONOUS, ss.numHands());
				}
			case SYNCHRONOUS:
				if(ss.numHands() == 2) {
					return new TwoHandedSyncNotatedSiteswap(ss);
				} else {
					throw new IncompatibleNotationException(SiteswapNotation.SYNCHRONOUS, ss.numHands());
				}
			case MIXED:
				if(ss.numHands() == 2) {
					return new TwoHandedMixedNotatedSiteswap(ss);
				} else {
					throw new IncompatibleNotationException(SiteswapNotation.MIXED, ss.numHands());
				}
			default: // case PASSING:
				if(ss.numHands() == 4) {
					return new NotatedPassingSiteswap(ss);
				} else {
					throw new IncompatibleNotationException(SiteswapNotation.PASSING, ss.numHands());
				}
		}
	}

	// build a Siteswap from a notation string and pair it with the determined NotationType
	public static NotatedSiteswap parse(String inputNotation, int numHands, int startHand) throws InvalidSiteswapNotationException, IncompatibleNumberOfHandsException {
		SiteswapNotation n;
		try {
			n = SiteswapNotation.analyze(inputNotation);
		} catch(InvalidSiteswapNotationException e) {
			throw e;
		}
		switch(n) {
			case EMPTY:
				if(numHands == -1) {
					numHands = 0;
				}
				return new EmptyNotatedSiteswap(numHands);
			case ASYNCHRONOUS:
				if(numHands == -1 || numHands == 1) {
					return new OneHandedNotatedSiteswap(inputNotation);
				} else if(numHands == 2) {
					return new TwoHandedAsyncNotatedSiteswap(inputNotation, startHand);
				} else {
					break;
				}
			case SYNCHRONOUS:
				if(numHands == -1 || numHands == 2) {
					return new TwoHandedSyncNotatedSiteswap(inputNotation);
				} else {
					break;
				}
			case MIXED:
				if(numHands == -1 || numHands == 2) {
					return new TwoHandedMixedNotatedSiteswap(inputNotation);
				} else {
					break;
				}
			default: // case PASSING
				return new NotatedPassingSiteswap(inputNotation);
		}
		throw new IncompatibleNumberOfHandsException(inputNotation, numHands);
	}

	/* -------------- */
	/* THE SUBCLASSES */
	/* -------------- */

	static class EmptyNotatedSiteswap extends NotatedSiteswap {

		// assemble
		EmptyNotatedSiteswap(Siteswap ss) {
			super(ss, SiteswapNotation.EMPTY);
		}

		// "parse"
		EmptyNotatedSiteswap(int numHands) {
			this(new Siteswap(numHands));
		}

		// print
		public String print() {
			return SiteswapNotation.emptyNotationPrint;
		}

		// deep copy
		public NotatedSiteswap deepCopy() {
			return (NotatedSiteswap)(new EmptyNotatedSiteswap(this.siteswap.deepCopy()));
		}

		public NotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

	}

	static class OneHandedNotatedSiteswap extends NotatedSiteswap {

		// assemble
		OneHandedNotatedSiteswap(Siteswap ss) {
			super(ss, SiteswapNotation.ASYNCHRONOUS);
		}

		// parse
		OneHandedNotatedSiteswap(String s) {
			super(new Siteswap(1), SiteswapNotation.ASYNCHRONOUS);
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
						this.siteswap.appendEmptyBeat();
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
						ExtendedInteger height = SiteswapNotation.throwHeight(curToken);
						if(isNegative) {
							height.negate();
							isNegative = false;
						}
						if(!multi) {
							if(b == this.siteswap.period()) {
								this.siteswap.appendEmptyBeat();
							}
						}
						if(height.isInfinite()) {
							if(isAntitoss) {
								this.siteswap.addInfiniteAntitoss(b, 0, height.infiniteValue());
							} else {
								this.siteswap.addInfiniteToss(b, 0, height.infiniteValue());
							}
						} else {
							if(isAntitoss) {
								this.siteswap.addFiniteAntitoss(b, 0, height.finiteValue(), 0);
							} else {
								this.siteswap.addFiniteToss(b, 0, height.finiteValue(), 0);
							}
						}
						if(!multi) {
							b++;
						}
						isAntitoss = false;
						break;
				}
				i++;
			}
		}

		// print
		public String print() {
			String out = "";
			for(int b=0; b<this.siteswap.period(); b++) {
				if(this.siteswap.numTossesAtSite(b, 0) > 1) {
					out += "[";
					for(int t=0; t<this.siteswap.numTossesAtSite(b,0); t++) {
						out += SiteswapNotation.reverseThrowHeight(this.siteswap.getToss(b, 0, t));
					}
					out += "]";
				} else {
					out += SiteswapNotation.reverseThrowHeight(this.siteswap.getToss(b, 0, 0));
				}
			}
			return out;
		}

		// deep copy
		public NotatedSiteswap deepCopy() {
			return (NotatedSiteswap)(new OneHandedNotatedSiteswap(this.siteswap.deepCopy()));
		}

		public NotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

	}

	static class TwoHandedAsyncNotatedSiteswap extends NotatedSiteswap {

		private int startHand;

		// assemble
		TwoHandedAsyncNotatedSiteswap(Siteswap ss) {
			super(ss, SiteswapNotation.ASYNCHRONOUS);
			// TODO: set startHand by looking at beats
			this.startHand = 0;
		}

		// parse
		TwoHandedAsyncNotatedSiteswap(String s, int startHand) {
			super(new Siteswap(2), SiteswapNotation.ASYNCHRONOUS);
			this.startHand = startHand;
			this.notationType = SiteswapNotation.ASYNCHRONOUS;
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
							this.siteswap.appendEmptyBeat();
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
							height = SiteswapNotation.throwHeight(curToken);
							if(isNegative) {
								height.negate();
								isNegative = false;
							}
							if(!multi) {
								//create new beat
								this.siteswap.appendEmptyBeat();
								//add toss of correct height and destination to current hand
								if(height.isInfinite()) {
									if(isAntitoss) {
										this.siteswap.addInfiniteAntitoss(b, curHand, height.infiniteValue());
									} else {
										this.siteswap.addInfiniteToss(b, curHand, height.infiniteValue());
									}
								} else {
									destHand = (curHand + height.finiteValue()) % 2; //0=left, 1=right
									if(destHand < 0) {
										destHand += 2;
									}
									if(isAntitoss) {
										this.siteswap.addFiniteAntitoss(b, curHand, height.finiteValue(), destHand);
									} else {
										this.siteswap.addFiniteToss(b, curHand, height.finiteValue(), destHand);
									}
									//increment beat index
									b++;
								}
							} else {
								//add toss of correct height and destination to current hand
								if(height.isInfinite()) {
									if(isAntitoss) {
										this.siteswap.addInfiniteAntitoss(b, curHand, height.infiniteValue());
									} else {
										this.siteswap.addInfiniteToss(b, curHand, height.infiniteValue());
									}
								} else {
									destHand = (curHand + height.finiteValue()) % 2; //0=left, 1=right
									if(destHand < 0) {
										destHand += 2;
									}
									if(isAntitoss) {
										this.siteswap.addFiniteAntitoss(b, curHand, height.finiteValue(), destHand);
									} else {
										this.siteswap.addFiniteToss(b, curHand, height.finiteValue(), destHand);
									}
								}
							}
							isAntitoss = false;
							break;
					}
					//increment index in input string
					i++;
				}
			} while(this.siteswap.period() % 2 == 1);
		}

		// print
		public String print() {
			String out = "";
			int curHandIndex = this.startHand;
			//loop through beats of siteswap
			for(int b=0; b<this.siteswap.period(); b++) {
				//see if we need to use multiplex notation
				if(this.siteswap.numTossesAtSite(b, curHandIndex) > 1) {
					out += "[";
					//loop through tosses of current hand
					for(int t=0; t<this.siteswap.numTossesAtSite(b, curHandIndex); t++) {
						out += SiteswapNotation.reverseThrowHeight(this.siteswap.getToss(b, curHandIndex, t));
					}
					out += "]";
				} else {
					out += SiteswapNotation.reverseThrowHeight(this.siteswap.getToss(b, curHandIndex, 0));
				}
				//alternate curHandIndex
				curHandIndex = (curHandIndex + 1) % 2;
			}
			return out;
		}

		// deep copy
		public NotatedSiteswap deepCopy() {
			return (NotatedSiteswap)(new TwoHandedAsyncNotatedSiteswap(this.siteswap.deepCopy(), this.startHand));
		}

		// additional constructor for deep copy
		TwoHandedAsyncNotatedSiteswap(Siteswap ss, int startHand) {
			super(ss, SiteswapNotation.ASYNCHRONOUS);
			// TODO: set startHand by looking at beats
			this.startHand = startHand;
		}

		public NotatedSiteswap spring() throws SprungException {
			TwoHandedSyncNotatedSiteswap newSiteswap = new TwoHandedSyncNotatedSiteswap(new Siteswap(2));
			int sprungHand = (this.startHand + 1) % 2;
			for(int b=0; b<this.siteswap.period(); b++) {
				newSiteswap.siteswap.appendEmptyBeat();
				for(int h=0; h<2; h++) {
					if(h == sprungHand) {
						Toss newToss = new Toss(2, (h + 1) % 2, false);
						newSiteswap.siteswap.addToss(b * 2, h, newToss);
					} else {
						if(this.siteswap.siteIsEmpty(b, h))
							continue;
						for(int t=0; t<this.siteswap.numTossesAtSite(b, h); t++) {
							Toss curToss = this.siteswap.getToss(b, h, t);
							Toss newToss;
							if(curToss.height().isInfinite() || curToss.charge() == 0)
								newToss = curToss.deepCopy();
							else
								newToss = new Toss(curToss.height().finiteValue() * 2, curToss.destHand(), curToss.isAntitoss());
							newSiteswap.siteswap.addToss(b * 2, h, newToss);
						}
					}
				}
				newSiteswap.siteswap.appendEmptyBeat();
				sprungHand = (sprungHand + 1) % 2;
			}
			return newSiteswap;
		}

	}

	static class TwoHandedSyncNotatedSiteswap extends NotatedSiteswap {

		// assemble
		TwoHandedSyncNotatedSiteswap(Siteswap ss) {
			super(ss, SiteswapNotation.SYNCHRONOUS);
		}

		// parse
		TwoHandedSyncNotatedSiteswap(String s) {
			this(new Siteswap(2));
			this.notationType = SiteswapNotation.SYNCHRONOUS;
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
						this.siteswap.appendEmptyBeat();
						curHand = 0;
						break;
					case ',':
						curHand = 1;
						break;
					case ')':
						//add empty beat, cuz that's how sync works
						this.siteswap.appendEmptyBeat();
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
						this.siteswap.removeLastBeat();
						b--;
						//decrement beat index
						break;
					case '*':
						this.siteswap.starify();
						break;
					case '-':
						isNegative = true;
						break;
					case '_':
						isAntitoss = true;
						break;
					default: //curToken is a throw height
						height = SiteswapNotation.throwHeight(curToken);
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
							if(destHand < 0) {
								destHand += 2;
							} if(isAntitoss) {
								newToss = new Toss(height.finiteValue(), destHand, true);
							} else {
								newToss = new Toss(height.finiteValue(), destHand, false);
							}
							isAntitoss = false;
						}
						this.siteswap.addToss(b, curHand, newToss);
				}
				i++;
			}
		}

		// print
		public String print() {
			String out = "";
			String nextBeat;
			boolean skippedLastBeat = false;
			boolean allZeroes = true;
			//loop through beats of siteswap
			for(int b=0; b<this.siteswap.period(); b++) {
				nextBeat = "(";
				allZeroes = true;
				//loop through hands within each beat (we know numHands = 2 since we screened for that in parse())
				for(int h=0; h<2; h++) {
					Util.printf("nextBeat: " + nextBeat, Util.DebugLevel.DEBUG);
					//see if we need to add multiplex notation
					if(this.siteswap.numTossesAtSite(b, h) > 1) {
						nextBeat += "[";
						//loop through tosses within hand
						for(int t=0; t<this.siteswap.numTossesAtSite(b, h); t++) {
							Toss curToss = this.siteswap.getToss(b, h, t);
							Util.printf(curToss, Util.DebugLevel.DEBUG);
							nextBeat += SiteswapNotation.reverseThrowHeight(curToss);
							if(curToss.charge() != 0) {
								allZeroes = false;
								if(!curToss.height().isInfinite() && curToss.destHand() != (h + Math.abs(curToss.height().finiteValue())) % 2) {
									nextBeat += "x";
								}
							}
						}
						nextBeat += "]";
					} else if(this.siteswap.numTossesAtSite(b, h) == 1) {
						//account for only toss in hand
						Toss curToss = this.siteswap.getToss(b, h, 0);
						if(!curToss.isZero(h)) {
							Util.printf("encountered non-zero toss", Util.DebugLevel.DEBUG);
							Util.printf(curToss, Util.DebugLevel.DEBUG);
							allZeroes = false;
						}
						nextBeat += SiteswapNotation.reverseThrowHeight(curToss);
						if(!curToss.height().isInfinite() && curToss.destHand() != (h + Math.abs(curToss.height().finiteValue())) % 2) {
							nextBeat += "x";
						}
					} else {
						// notate empty site
						nextBeat += "0";
					}
					//put a comma if we've just finished doing the left hand
					if(h == 0) {
						nextBeat += ",";
					}
				}
				nextBeat += ")";
				if(b == 0) {
					Util.printf("not skipping beat 0", Util.DebugLevel.DEBUG);
					out += nextBeat;
					skippedLastBeat = false;
				} else if(!skippedLastBeat && allZeroes) {
					// skip this beat
					Util.printf("skipping beat " + b, Util.DebugLevel.DEBUG);
					skippedLastBeat = true;
				} else {
					// don't skip this beat
					Util.printf("not skipping beat " + b, Util.DebugLevel.DEBUG);
					if(!skippedLastBeat) {
						out += "!";
					}
					out += nextBeat;
					skippedLastBeat = false;
				}
			}
			if(!skippedLastBeat) {
				Util.printf("adding final '!'", Util.DebugLevel.DEBUG);
				out += "!";
			}
			return out;
		}

		// deep copy
		public NotatedSiteswap deepCopy() {
			return (NotatedSiteswap)(new TwoHandedSyncNotatedSiteswap(this.siteswap.deepCopy()));
		}

		public NotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

	}

	static class TwoHandedMixedNotatedSiteswap extends NotatedSiteswap {

		// assemble
		TwoHandedMixedNotatedSiteswap(Siteswap ss) {
			super(ss, SiteswapNotation.MIXED);
		}

		// parse
		TwoHandedMixedNotatedSiteswap(String s) {
			this(new Siteswap(2));
			Util.printf("WARNINR: Parsing of mixed notation not yet implemented...", Util.DebugLevel.ERROR);
			System.exit(1);
		}

		// print
		public String print() {
			Util.printf("WARNINR: Parsing of mixed notation not yet implemented...", Util.DebugLevel.ERROR);
			System.exit(1);
			return null;
		}

		// deep copy
		public NotatedSiteswap deepCopy() {
			return (NotatedSiteswap)(new TwoHandedMixedNotatedSiteswap(this.siteswap));
		}

		public NotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

	}

	static class NotatedPassingSiteswap extends NotatedSiteswap {

		// assemble
		NotatedPassingSiteswap(Siteswap ss) {
			super(ss, SiteswapNotation.PASSING);
		}

		// parse
		NotatedPassingSiteswap(String s) {
			this(new Siteswap(4));
			Util.printf("Parsing of passing notation not yet implemented...", Util.DebugLevel.ERROR);
			System.exit(1);
		}

		// print
		public String print() {
			Util.printf("Parsing of passing notation not yet implemented...", Util.DebugLevel.ERROR);
			System.exit(1);
			return null;
		}

		// deep copy
		public NotatedSiteswap deepCopy() {
			return (NotatedSiteswap)(new NotatedPassingSiteswap(this.siteswap));
		}

		public NotatedSiteswap spring() throws SprungException {
			throw new SprungException("WARNING: cannot spring a non-async pattern");
		}

	}

}
