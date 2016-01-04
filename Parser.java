import java.util.List;
import java.util.regex.Pattern;

public class Parser {
	/* siteswap regex patterns:
	   toss = "(-?(\\d|[a-z]|&)x?)"
	   hand = "(toss|\[toss+\])+"
	   asyncSiteswap = "hand+"
	   syncBeat = "\(hand,hand\)!?"
	   syncSiteswap = "(beat+)\\*?"
	   mixedSiteswap = "(toss|beat)+"
	   */
	private static final String validAsyncSiteswapString = "((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\])+";
	private static final String validSynchronousSiteswapString = "(\\(((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\]),((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\])\\)!?)+\\*?";
	private static final String validMixedNotationTwoHandedSiteswapString = "(((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\])|(\\(((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\]),((-?(\\d|[a-z]|&)x?)|\\[(-?(\\d|[a-z]|&)x?)+\\])\\)!?))+";
	//no star notation on mixed, because it would be ambiguous as to whether the whole pattern is starred or just the most recent sync part
	private static final String validMultipleJugglerSiteswapString = ""; //later...

	public static NotationType getNotationType(String s) {
		if(Pattern.matches(validAsyncSiteswapString, s)) {
			return NotationType.ASYNCHRONOUS;
		} else if(Pattern.matches(validSynchronousSiteswapString, s)) {
			return NotationType.SYNCHRONOUS;
		} else if(Pattern.matches(validMixedNotationTwoHandedSiteswapString, s)) {
			return NotationType.MIXED;
		} else {
			return null;
		}
	}

	public static Siteswap parse(String s, boolean asSimpleAsPossible) {
		if(getNotationType(s) == null) {
			System.err.println("syntax error");
			System.exit(1);
			return null;
		}
		switch(getNotationType(s)) {
			case ASYNCHRONOUS:
				if(asSimpleAsPossible) {
					return parseAsyncAsOneHanded(s);
				}
				return parseAsyncAsTwoHanded(s);
			case SYNCHRONOUS:
				return parseSync(s);
			case MIXED:
				return parseMixed(s);
			default:
				return parsePassingNotation(s);
		}	
	}

	public static Siteswap parse(String s) {
		return parse(s, true);
	}

	public static Siteswap parseAsyncAsOneHanded(String s) {
		Siteswap out = new Siteswap(1, NotationType.ASYNCHRONOUS);
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
						if(b == out.period())
							out.appendEmptyBeat();
					}
					if(height.isInfinite()) {
						if(isAntitoss)
							out.addInfiniteAntitoss(b, 0, height.infiniteValue());
						else
							out.addInfiniteToss(b, 0, height.infiniteValue());
					} else {
						if(isAntitoss)
							out.addFiniteAntitoss(b, 0, height.finiteValue(), 0);
						else
							out.addFiniteToss(b, 0, height.finiteValue(), 0);
					}
					if(!multi)
						b++;
					isAntitoss = false;
					break;
			}
			System.out.println(out);
			i++;
		}
		return out;
	}

	public static Siteswap parseAsyncAsTwoHanded(String s) {
		//double string length if it's odd (so e.g. "3" will become (3,0)!(0,3)!) 
		if(s.length() % 2 == 1) {
			s += s;
		}
		char[] a = s.toCharArray();
		//create new async siteswap
		Siteswap out = new Siteswap(2, NotationType.SYNCHRONOUS);
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
					out.appendEmptyBeat();
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
						out.appendEmptyBeat();
						//add toss of correct height and destination to current hand
						if(height.isInfinite()) {
							if(isAntitoss)
								out.addInfiniteAntitoss(b, curHand, height.infiniteValue());
							else
								out.addInfiniteToss(b, curHand, height.infiniteValue());
						} else {
							destHand = (curHand + height.finiteValue()) % 2; //0=left, 1=right
							if(isAntitoss)
								out.addFiniteAntitoss(b, curHand, height.finiteValue(), destHand);
							else
								out.addFiniteToss(b, curHand, height.finiteValue(), destHand);
							//increment beat index
							b++;
						}
					} else {
						//add toss of correct height and destination to current hand
						if(height.isInfinite()) {
							if(isAntitoss)
								out.addInfiniteAntitoss(b, curHand, height.infiniteValue());
							else
								out.addInfiniteToss(b, curHand, height.infiniteValue());
						} else {
							destHand = (curHand + height.finiteValue()) % 2; //0=left, 1=right
							if(isAntitoss)
								out.addFiniteAntitoss(b, curHand, height.finiteValue(), destHand);
							else
								out.addFiniteToss(b, curHand, height.finiteValue(), destHand);
						}
					}
					isAntitoss = false;
					break;
			}
			//increment index in input string
			i++;
		}
		return out;
	}

	public static Siteswap parseSync(String s) {
		char[] a = s.toCharArray();
		//create new sync siteswap
		Siteswap out = new Siteswap(2, NotationType.SYNCHRONOUS);
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

		while(i < a.length) {
			curToken = a[i];
			switch(curToken) {
				case '(':
					//create new beat
					out.appendEmptyBeat();
					curHand = 0;
					break;
				case ',':
					curHand = 1;
					break;
				case ')':
					//add empty beat, cuz that's how sync works
					out.appendEmptyBeat();
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
					out.getToss(out.period(), curHand, out.numTossesAtSite(out.period(), curHand) - 1).starify();
					break;
				case '!':
					//remove last beat
					out.removeLastBeat();
					//decrement beat index
					b--;
					break;
				case '*':
					out.starify();
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
							out.addInfiniteAntitoss(b, curHand, height.infiniteValue());
						else
							out.addInfiniteToss(b, curHand, height.infiniteValue());
					} else {
						destHand = (curHand + height.finiteValue()) % 2;
						if(isAntitoss)
							out.addFiniteAntitoss(b, curHand, height.finiteValue(), destHand);
						else
							out.addFiniteToss(b, curHand, height.finiteValue(), destHand);
						isAntitoss = false;
						break;
					}
			}
			i++;
		}
		return out;
	}

	private static Siteswap parsePassingNotation(String s) {
		//later...
		System.out.println("Parsing of passing notation not yet implemented...");
		System.exit(1);
		return null;
	}

	private static Siteswap parseMixed(String s) {
		//later...
		System.out.println("Parsing of mixed notation not yet implemented...");
		System.exit(1);
		return null;
	}

	private static ExtendedInteger throwHeight(char c) {
		String h = ((Character)c).toString();
		if(Pattern.matches("\\d", h)) {
			return new ExtendedInteger(Integer.parseInt(h));
		} else if(Pattern.matches("([a-z])", h)) {
			return new ExtendedInteger((int)(h.toCharArray()[0]) - 87);
		} else { //must be '&'
			return new ExtendedInteger(InfinityType.POSITIVE_INFINITY);
		}
	}

	private static String reverseThrowHeight(Toss t) {
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

	public static String deParse(Siteswap ss) {
		switch(ss.numHands()) {
			case 1:
				return reduceSiteswapString(deParseOneHanded(ss));
			case 2:
				switch(ss.notationType()) {
					case ASYNCHRONOUS:
						return deParseAsync(ss);
					case SYNCHRONOUS:
						return deParseSync(ss);
					default:
						//case "mixed":
						return deParseMixed(ss);
				}
			default:
				return "not yet implemented";
		}
	}

	private static String deParseOneHanded(Siteswap ss) {
		String out = "";
		for(int b=0; b<ss.period(); b++) {
			System.out.println(b);
			if(ss.numTossesAtSite(b, 0) > 1) {
				out += "[";
				for(int t=0; t<ss.numTossesAtSite(b,0); t++) {
					out += reverseThrowHeight(ss.getToss(b, 0, t));
				}
				out += "]";
			} else {
				System.out.println(ss.getToss(b, 0, 0));
				out += reverseThrowHeight(ss.getToss(b, 0, 0));
			}
		}
		return out;
	}

	private static String deParseAsync(Siteswap ss) {
		String out = "";
		int curHandIndex;
		//determine which hand throws first
		if(ss.siteIsEmpty(0, 0)) {
			curHandIndex = 1;
		} else {
			curHandIndex = 0;
		}
		//loop through beats of siteswap
		for(int b=0; b<ss.period(); b++) {
			//see if we need to use multiplex notation
			if(ss.numTossesAtSite(b, curHandIndex) > 1) {
				out += "[";
				//loop through tosses of current hand
				for(int t=0; t<ss.numTossesAtSite(b, curHandIndex); t++) {
					out += reverseThrowHeight(ss.getToss(b, curHandIndex, t));
				}
				out += "]";
			} else {
				out += reverseThrowHeight(ss.getToss(b, curHandIndex, 0));
			}
			//alternate curHandIndex
			curHandIndex = (curHandIndex + 1) % 2;
		}
		return out;
	}

	private static String deParseSync(Siteswap ss) {
		String out = "";
		boolean skipBeat = false;
		//loop through beats of siteswap
		for(int b=0; b<ss.period(); b++) {
			out += "(";
			//loop through hands within each beat (we know numHands = 2 since we screened for that in parse())
			for(int h=0; h<2; h++) {
				//see if we need to add multiplex notation
				if(ss.numTossesAtSite(b, h) > 1) {
					out += "[";
					//loop through tosses within hand
					for(int t=0; t<ss.numTossesAtSite(b, h); t++) {
						Toss curToss = ss.getToss(b, h, t);
						out += reverseThrowHeight(curToss);
						if(!curToss.height().isInfinite() && curToss.destHand() != (h + Math.abs(curToss.height().finiteValue())) % 2) {
							out += "x";
						}
					}
					out += "]";
				} else {
					//account for only toss in hand
					Toss curToss = ss.getToss(b, h, 0);
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

	private static String deParseMixed(Siteswap ss) {
		return "not yet implemented";
	}

	private static String reduceSiteswapString(String s) {
		//TO DO LATER
		//(also make it use * notation?)
		return s;
	}

	public static void main(String[] args) {
		if(args.length == 1) {
			Siteswap ss = parse(args[0], true);
			System.out.println("parsed: " + ss.toString());
			String s = deParse(ss);
			System.out.println("de-parsed: " + s);
		}
	}
}
