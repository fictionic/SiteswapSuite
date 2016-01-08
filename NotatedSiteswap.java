package siteswapsuite;

import java.util.regex.Pattern;
import java.util.ArrayList;


enum Notation { 
	EMPTY, SYNCHRONOUS, ASYNCHRONOUS, MIXED, PASSING
}

class InvalidNotationException extends Exception {}

public abstract class NotatedSiteswap extends Siteswap {

	protected Notation notationType;

	// querying basic info
	public Notation notationType() {
		return this.notationType;
	}

	/* ------- */
	/* PARSING */
	/* ------- */

	protected static ExtendedInteger throwHeight(char c) {
		String h = ((Character)c).toString();
		if(Pattern.matches("\\d", h)) {
			return new ExtendedInteger(Integer.parseInt(h));
		} else if(Pattern.matches("([a-z])", h)) {
			return new ExtendedInteger((int)(h.toCharArray()[0]) - 87);
		} else { //must be '&'
			return new ExtendedInteger(InfinityType.POSITIVE_INFINITY);
		}
	}

	/* siteswap regex patterns */
	// the empty pattern (need some way of notating it without the empty string, cuz that's never passed on the commandline)
	private static final String emptyNotation = "\\.";

	// basics
	private static final String modifier = "(-?_?|_?-?)";
	private static final String magnitude = "(\\d|[a-z]|&)";

	// async (one-handed)
	private static final String asyncToss = "(" + modifier + magnitude + ")";
	private static final String asyncMux = "(" + asyncToss + "|\\[" + asyncToss + "+\\])";
	private static final String validAsyncNotation = asyncMux + "+";

	// sync (two-handed)
	private static final String syncToss = "(" + modifier + magnitude + "x?)";
	private static final String syncHand = "(" + syncToss + "|\\[" + syncToss + "+\\])";
	private static final String syncBeat = "(\\(" + syncHand + "," + syncHand + "\\)!?)";
	private static final String validSyncNotation = syncBeat + "+\\*?";

	// mixed (two-handed)
	private static final String validMixedNotation = "(" + syncToss + "|" + syncBeat + ")+";
	//no star notation on mixed, because it would be ambiguous as to whether the whole pattern is starred or just the most recent sync part

	// passing (two two-handed jugglers)
	private static final String validPassingNotation = ""; //later...
	
	public static Notation getNotation(String s) {
		if(Pattern.matches(emptyNotation, s))
			return Notation.EMPTY;
		if(Pattern.matches(validAsyncNotation, s))
			return Notation.ASYNCHRONOUS;
		else if(Pattern.matches(validSyncNotation, s))
			return Notation.SYNCHRONOUS;
		else if(Pattern.matches(validMixedNotation, s))
			return Notation.MIXED;
		else if(Pattern.matches(validPassingNotation, s))
			return Notation.PASSING;
		else
			return null;
	}

	// link to Siteswap() constructor for subclasses
	private NotatedSiteswap(int numHands) {
		super(numHands);
	}

	public static NotatedSiteswap parseString(String inputNotation, int desiredNumHands) throws InvalidNotationException {
		// determine how we should parse the input, then parse it that way
		Notation n = getNotation(inputNotation);
		if(n == null)
			throw new InvalidNotationException();
		switch(n) {
			case EMPTY:
				return new EmptyNotatedSiteswap(desiredNumHands);
			case ASYNCHRONOUS:
				if(desiredNumHands == 2)
					return new TwoHandedAsyncNotatedSiteswap(inputNotation);
				else
					return new OneHandedNotatedSiteswap(inputNotation);
			case SYNCHRONOUS:
				return new TwoHandedSyncNotatedSiteswap(inputNotation);
			case MIXED:
				return new TwoHandedMixedNotatedSiteswap(inputNotation);
			default:
				return new NotatedPassingSiteswap(inputNotation);
		}
	}

	public static NotatedSiteswap parseString(String inputNotation) throws InvalidNotationException {
		return parseString(inputNotation, 0);
	}

	/* ---------- */
	/* DE-PARSING */
	/* ---------- */

	public abstract String print();

	protected static String reverseThrowHeight(Toss t) {
		String toReturn = "";
		ExtendedInteger H = t.height();
		if(H.sign() < 0)
			toReturn += "-";
		if(t.charge() < 0)
			toReturn += "_";
		if(H.isInfinite()) {
			toReturn += "&";
			return toReturn;
		}
		Integer h = Math.abs(H.finiteValue());
		if(h <= 9) {
			toReturn += h.toString();
		} else if((10 <= h) && (h <= 36)) {
			toReturn += (Character.toChars(h - 10 + 97)).toString();
		} else {
			// eventually come up with a better solution?
			toReturn += "{" + h.toString() + "}";
		}
		return toReturn;
	}

	// build a notatedsiteswap from a siteswap
	private NotatedSiteswap(Siteswap ss, Notation notationType) {
		super(ss);
		this.notationType = notationType;
	}

	public String notate(Siteswap ss) {
		if(ss.period() == 0)
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
		EmptyNotatedSiteswap(Siteswap ss) {
			super(ss, Notation.EMPTY);
		}

		EmptyNotatedSiteswap(int numHands) {
			super(numHands);
		}

		public String print() {
			return emptyNotation;
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
						ExtendedInteger height = throwHeight(curToken);
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
						out += reverseThrowHeight(this.getToss(b, 0, t));
					}
					out += "]";
				} else {
					out += reverseThrowHeight(this.getToss(b, 0, 0));
				}
			}
			return out;
		}
	}

	static class TwoHandedAsyncNotatedSiteswap extends NotatedSiteswap {

		TwoHandedAsyncNotatedSiteswap(Siteswap ss) {
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
						height = throwHeight(curToken);
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
						out += reverseThrowHeight(this.getToss(b, curHandIndex, t));
					}
					out += "]";
				} else {
					out += reverseThrowHeight(this.getToss(b, curHandIndex, 0));
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
						height = throwHeight(curToken);
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
			boolean skipBeat = false;
			//loop through beats of siteswap
			for(int b=0; b<this.period(); b++) {
				out += "(";
				//loop through hands within each beat (we know numHands = 2 since we screened for that in parse())
				for(int h=0; h<2; h++) {
					//see if we need to add multiplex notation
					if(this.numTossesAtSite(b, h) > 1) {
						out += "[";
						//loop through tosses within hand
						for(int t=0; t<this.numTossesAtSite(b, h); t++) {
							Toss curToss = this.getToss(b, h, t);
							out += reverseThrowHeight(curToss);
							if(!curToss.height().isInfinite() && curToss.destHand() != (h + Math.abs(curToss.height().finiteValue())) % 2) {
								out += "x";
							}
						}
						out += "]";
					} else {
						//account for only toss in hand
						Toss curToss = this.getToss(b, h, 0);
						out += reverseThrowHeight(curToss);
						if(!curToss.height().isInfinite() && curToss.destHand() != (h + Math.abs(curToss.height().finiteValue())) % 2) {
							out += "x";
						}
					}
					//put a comma if we've just finished doing the left hand
					if(h == 0) {
						out += ",";
					}
				}
				out += ")";
				//check to see if we should add a "!":
				//first check that we didn't just skip the previous beat, then check that the next beat is a zero beat (i.e. "(0,0)!")
				if(skipBeat) {
					//skip this beat
					b++;
					skipBeat = false;
				} else {
					//don't skip this beat
					out += "!";
					skipBeat = true;
				}
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

		public String print() {
			System.out.println("de-parsing of passing notation not yet implemented...");
			System.exit(1);
			return null;
		}
	}

	public static void main(String[] args) {
		if(args.length == 1) {
			try {
				NotatedSiteswap nss = NotatedSiteswap.parseString(args[0]);
				System.out.println("parsed: " + nss.toString());
				String s = nss.print();
				System.out.println("de-parsed: " + s);
			} catch(InvalidNotationException e) {
				System.out.println("invalid notation");
			}
		}
	}
}

