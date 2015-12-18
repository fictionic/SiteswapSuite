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

	public static String getNotationType(String s) {
	if(Pattern.matches(validAsyncSiteswapString, s)) {
	return "async";
	} else if(Pattern.matches(validSynchronousSiteswapString, s)) {
	return "sync";
	} else if(Pattern.matches(validMixedNotationTwoHandedSiteswapString, s)) {
	return "mixed";
	} else {
	return "invalid";
	}
	}

	public static Siteswap parse(String s, boolean asSimpleAsPossible) {
	switch(getNotationType(s)) {
	case "async":
	if(asSimpleAsPossible) {
	return parseAsyncAsOneHanded(s);
	}
	return parseAsyncAsTwoHanded(s);
	case "sync":
	return parseSync(s);
	case "mixed":
	return parseMixed(s);
	default:
	System.err.println("syntax error");
	System.exit(1);
	return null;
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
	out.appendEmptyBeat();
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
		out.addEmptyBeat();
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
		b++;
		}
	} else {
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
	}
	isAntitoss = false;
	break;
	}
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
			out.addEmptyBeat();
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
			if(!height.isInfinite())
			destHand = (curHand + height.finiteValue()) % 2; //0=left, 1=right
			if(!multi) {
			//create new beat
			out.addEmptyBeat();
			//add toss of correct height and destination to current hand
			if(height.isInfinite()) {
				if(isAntitoss)
				out.addInfiniteAntitoss(b, curHand, height.infiniteValue());
				else
				out.addInfiniteToss(b, curHand, height.infiniteValue());
			} else {
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
					out.addEmptyBeat();
					curHand = 0;
					break;
				case ',':
					curHand = 1;
					break;
				case ')':
					//add empty beat, cuz that's how sync works
					out.addEmptyBeat();
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
					out.getLastBeat().getHand(curHand).getLastToss().flipDestHand();
					break;
				case '!':
					//remove last beat
					out.removeLastBeat();
					//decrement beat index
					b--;
					break;
				case '*':
					out.addStar();
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
					destHand = (curHand + height) % 2;
					if(height.isInfinite()) {
						if(isAntitoss)
							out.addInfiniteAntitoss(b, curHand, height.infiniteValue());
						else
							out.addInfiniteToss(b, curHand, height.infiniteValue());
					} else {
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

	private static Siteswap parseMixed(String s) {
		//later...
		System.out.println("Parsing of mixed notation not yet implemented...");
		System.exit(1);
		return null;
	}

	private static ExtendedInteger throwHeight(char c) {
		String h = (Character)c.toString();
		if(Pattern.matches("\\d", h)) {
			return new ExtendedInteger(Integer.parseInt(h));
		} else if(Pattern.matches("([a-z])", h)) {
			return new ExtendedInteger((int)(h.toCharArray()[0]) - 87);
		} else { //must be '&'
			return new ExtendedInteger(InfinityType.POSITIVE_INFINITY);
		}
	}

	private static String reverseThrowHeight(Integer h) {
		String toReturn = "";
		if(h < 0) {
			toReturn = "-";
		}
		h = Math.abs(h);
		if(h <= 9) {
			toReturn += h.toString();
		} else if((10 <= h) && (h <= 36) && h != 33) {
			toReturn += (Character.toChars(h - 10 + 97)).toString();
		} else if(h == 33) {
			toReturn += "X";
		} else {
			toReturn += "";
		}
		return toReturn;
	}

	public static String deParse(Siteswap ss) {
		switch(ss.numHands()) {
			case 1:
				return reduceSiteswapString(deParseOneHanded(ss));
			case 2:
				switch(ss.type()) {
					case "async":
						return deParseAsync(ss);
					case "sync":
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
		Siteswap.Beat.Hand curHand;
		Siteswap.Beat.Hand.Toss curToss;
		for(int b=0; b<ss.period(); b++) {
			curHand = ss.getBeat(b).getHand(0);
			if(curHand.numTosses() > 1) {
				out += "[";
				for(int t=0; t<curHand.numTosses(); t++) {
					out += reverseThrowHeight(curHand.getToss(t).height());
				}
				out += "]";
			} else {
				if(curHand.getToss(0).isInfinite()) {
					if(curHand.getToss(0).height() < 0) {
						out += "-";
					}
					out += "&";
				} else {
					out += reverseThrowHeight(curHand.getToss(0).height());
				}
			}
		}
		return out;
	}

	private static String deParseAsync(Siteswap ss) {
		String out = "";
		int curHandIndex;
		Siteswap.Beat curBeat;
		Siteswap.Beat.Hand curHand;
		Siteswap.Beat.Hand.Toss curToss;
		//determine which hand throws first
		if(ss.getBeat(0).getHand(0).isEmpty()) {
			curHandIndex = 1;
		} else {
			curHandIndex = 0;
		}
		//loop through beats of siteswap
		for(int b=0; b<ss.period(); b++) {
			curBeat = ss.getBeat(b);
			curHand = curBeat.getHand(curHandIndex);
			//see if we need to use multiplex notation
			if(curHand.numTosses() > 1) {
				out += "[";
				//loop through tosses of current hand
				for(int t=0; t<curHand.numTosses(); t++) {
					curToss = curHand.getToss(t);
					out += reverseThrowHeight(curToss.height());
					//see if the throw goes where it normally does; add a "x" if not
					if(!curToss.isInfinite() && curToss.destHand() != (curToss.startHand() + curToss.height()) % 2) {
						out += "x";
					}
				}
				out += "]";
			} else {
				curToss = curHand.getLastToss();
				out += reverseThrowHeight(curToss.height());
				//see if the throw goes where it normally does; add a "x" if not
				if(!curToss.isInfinite() && curToss.destHand() != (curToss.startHand() + curToss.height()) % 2) {
					out += "x";
				}
			}
			//alternate curHandIndex
			curHandIndex = (curHandIndex + 1) % 2;
		}
		return out;
	}

	private static String deParseSync(Siteswap ss) {
		String out = "";
		boolean skippedLastBeat = false;
		//loop through beats of siteswap
		for(int b=0; b<ss.period(); b++) {
			out += "(";
			Siteswap.Beat curBeat = ss.getBeat(b);
			//loop through hands within each beat (we know numHands = 2 since we screened for that in parse())
			for(int h=0; h<2; h++) {
				Siteswap.Beat.Hand curHand = curBeat.getHand(h);
				//see if we need to add multiplex notation
				if(curHand.numTosses() > 1) {
					out += "[";
					//loop through tosses within hand
					for(int t=0; t<curHand.numTosses(); t++) {
						Siteswap.Beat.Hand.Toss curToss = curHand.getToss(t);
						out += reverseThrowHeight(curToss.height());
						if(curToss.destHand() != (curToss.startHand() + Math.abs(curToss.height())) % 2) {
							out += "x";
						}
					}
					out += "]";
				} else {
					//account for only toss in hand
					Siteswap.Beat.Hand.Toss curToss = curHand.getLastToss();
					out += reverseThrowHeight(curToss.height());
					if(curToss.destHand() != (curToss.startHand() + Math.abs(curToss.height())) % 2) {
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
			if(b + 1 < ss.period() && ss.getBeat(b+1).isZeroBeat()) {
				//skip this beat
				b++;
			} else {
				//don't skip this beat
				out += "!";
			}
		}
		//TO DO: MAKE THIS USE STAR NOTATION TO FULLY SIMPLIFY?
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
			System.out.println(ss);
			String s = deParse(ss);
			System.out.println(s);
		}
	}
}
