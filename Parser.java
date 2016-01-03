import java.util.List;
import java.util.regex.Pattern;

public class Parser {
	/* siteswap regex patterns:
	   toss = "(-?(\\d|[a-w]|X|[yz]|&)x?)"
	   hand = "(toss|\[toss+\])+"
	   asyncSiteswap = "hand+"
	   syncBeat = "\(hand,hand\)!?"
	   syncSiteswap = "(beat+)\\*?"
	   mixedSiteswap = "(toss|beat)+"
	   */
	private static final String validAsyncSiteswapString = "((-?(\\d|[a-w]|X|[yz]|&)x?)|\\[(-?(\\d|[a-w]|X|[yz]|&)x?)+\\])+";
	private static final String validSynchronousSiteswapString = "(\\(((-?(\\d|[a-w]|X|[yz]|&)x?)|\\[(-?(\\d|[a-w]|X|[yz]|&)x?)+\\]),((-?(\\d|[a-w]|X|[yz]|&)x?)|\\[(-?(\\d|[a-w]|X|[yz]|&)x?)+\\])\\)!?)+\\*?";
	private static final String validMixedNotationTwoHandedSiteswapString = "(((-?(\\d|[a-w]|X|[yz]|&)x?)|\\[(-?(\\d|[a-w]|X|[yz]|&)x?)+\\])|(\\(((-?(\\d|[a-w]|X|[yz]|&)x?)|\\[(-?(\\d|[a-w]|X|[yz]|&)x?)+\\]),((-?(\\d|[a-w]|X|[yz]|&)x?)|\\[(-?(\\d|[a-w]|X|[yz]|&)x?)+\\])\\)!?))+";
	//no star notation on mixed, because it would be ambiguous as to whether the whole pattern is starred or just the most recent sync part
	private static final String validMultipleJugglerSiteswapString = ""; //later...

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
		printf("parsing input as one-handed siteswap...");
		Siteswap out = new Siteswap(1, "async");
		String curToken;
		int i=0; //index in input string	
		int b=0; //index (beat) in output siteswap
		boolean multi = false; //whether or not we're currently in a multiplex throw
		boolean isNegative = false;
		boolean isInfinite = false;
		while(i < s.length()) {
			curToken = ((Character)s.charAt(i)).toString(); //get string of i-ith character of the input string
			switch(curToken) {
				case "[":
					multi = true;
					out.addZeroBeat();
					break;
				case "]":
					multi = false;
					b++;
					break;
				case "-":
					isNegative = true;
					break;
				default:
					int height = throwHeight(curToken);
					if(height == -1) {
						//then height is infinity
						isInfinite = true;
						height = 1; //need height to be positive first, its sign is determined by variable "isNegative"
					} else {
						isInfinite = false;
					}
					if(isNegative) {
						height = -1 * height;
						isNegative = false;
					}
					if(!multi) {
						out.addZeroBeat();
						out.getLastBeat().getHand(0).addToss(height, isInfinite, 0);
						b++;
					} else {
						//ensure we don't add redundant zero-tosses (i.e. only add if there are no tosses or if the height is nonzero)
						if(height != 0 || out.getLastBeat().getHand(0).isEmpty()) {
							out.getLastBeat().getHand(0).addToss(height, isInfinite, 0);
						}
					}
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
		//create new async siteswap
		Siteswap out = new Siteswap(2, "async");
		String curToken;
		int i = 0; //index in input string
		int b = 0; //index (beat) in output siteswap
		int curHand = 0; // which hand's turn it is to throw
		boolean multi = false; //whether or not we're currently in a multiplex throw
		boolean isNegative = false;
		boolean isInfinite = false;
		while(i < s.length()) {
			curToken = ((Character)s.charAt(i)).toString();
			//update current hand
			curHand = b % 2;
			//printf(curToken);
			switch(curToken) {
				//if curToken is "[", we're now in a multiplex throw, so add all subsequent tosses to the same hand until "]"
				case "[":
					multi = true;
					out.addZeroBeat();
					break;
					//if curToken is "]", we're no longer in a multiplex throw, so add an empty toss to the non-current hand
				case "]":
					multi = false;
					out.getLastBeat().getHand((curHand + 1) % 2).addToss();
					b++;
					break;
					//if curToken is "x", flip the destination hand of the most recently added toss
				case "x":
					out.getLastBeat().getHand((curHand + 1) % 2).getLastToss().flipDestHand();
					break;
					//if curToken is "-", the next toss is negative
				case "-":
					isNegative = true;
					break;
					//if curToken is anything else, it has to be a throw height (since it matched the regex for async pattern)
				default:
					int height = throwHeight(curToken);
					if(height == -1) {
						//then height is infinity
						isInfinite = true;
						height = 1; //need height to be positive first, its sign is determined by variable isNegative
					} else {
						isInfinite = false;
					}
					if(isNegative) {
						height = -1 * height;
						isNegative = false;
					}
					int destHand = (curHand + height) % 2; //0=left, 1=right
					if(!multi) {
						//create new beat
						out.addZeroBeat();
						//add toss of correct height and destination to current hand
						out.getLastBeat().getHand(curHand).addToss(height, isInfinite, destHand);
						//add empty toss to other hand
						out.getLastBeat().getHand((curHand + 1) % 2).addToss();
						//increment beat index
						b++;
					} else {
						//add toss of correct height and destination to current hand
						//(only if it isn't a redundant zero-toss
						if(height != 0 || out.getLastBeat().getHand(curHand).isEmpty()) {
							out.getLastBeat().getHand(curHand).addToss(height, isInfinite, destHand);
						}
					}
					break;
			}
			//increment index in input string
			i++;
		}
		return out;
	}

	public static Siteswap parseSync(String s) {
		//create new sync siteswap
		Siteswap out = new Siteswap(2, "sync");
		int i = 0; //index in index string
		int b = 0; //index of beat within output siteswap
		int curHand = 0;
		int lastStarredBeat = 0;
		boolean isInfinite = false;
		boolean isNegative = false;
		String curToken;

		while(i < s.length()) {
			curToken = ((Character)s.charAt(i)).toString();
			switch(curToken) {
				case "(":
					//create new beat
					out.addZeroBeat();
					curHand = 0;
					break;
				case ",":
					curHand = 1;
					break;
				case ")":
					//add empty beat, cuz that's how sync works
					out.addZeroBeat();
					//increase beat index by 2
					b += 2;
					break;
				case "[":
					//doesn't matter whether we're in a multiplex pattern
					// b/c curHand is determined by other notation
					break;
				case "]":
					break;
				case "x":
					//toggle destination hand of most recently added toss
					out.getLastBeat().getHand(curHand).getLastToss().flipDestHand();
					break;
				case "!":
					//remove last beat
					out.removeLastBeat();
					//decrement beat index
					b--;
					break;
				case "*":
					out.addStar();
					break;
				case "-":
					isNegative = true;
					break;
				default: //curToken is a throw height
					int height = throwHeight(curToken);
					if(height == -1) {
						//then height is infinity
						isInfinite = true;
						height = 1; //need height to be positive first, its sign is determined by variable isNegative
					} else {
						isInfinite = false;
					}
					if(isNegative) {
						height = -1 * height;
						isNegative = false;
					}
					int destHand = (curHand + height) % 2;
					Siteswap.Beat curBeat = out.getLastBeat();
					//add toss, only if it's not a redundant zero-toss
					if(height != 0 || curBeat.getHand(curHand).isEmpty()) {
						curBeat.getHand(curHand).addToss(height, isInfinite, destHand);
					}
					break;
			}
			i++;
		}
		return out;
	}

	private static Siteswap parseMixed(String s) {
		//later...
		printf("Parsing of mixed notation not yet implemented...");
		System.exit(1);
		return null;
	}

	private static int throwHeight(String h) {
		if(Pattern.matches("\\d", h)) {
			return Integer.parseInt(h);
		} else if(Pattern.matches("([a-w]|[yz])", h)) {
			return (int)(h.toCharArray()[0]) - 87;
		} else if(h.equals("X")) { //if h is "X"
			return 33;
		} else {
			return -1; //sentinel value, indicates that the height is infinite
		}
	}

	private static String reverseThrowHeight(Siteswap.Beat.Hand.Toss t) {
		String toReturn = "";
		if(t.isAntiToss()) {
			toReturn += "_";
		}
		Integer h = t.height();
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

	public static String deParse(SiteswapTransition ss) {
		// make this more robust later
		return deParse(ss.prefix()) + " | " + deParse(ss.transition()) + " | " + deParse(ss.suffix());
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
					out += reverseThrowHeight(curHand.getToss(t));
				}
				out += "]";
			} else {
				if(curHand.getToss(0).isInfinite()) {
					if(curHand.getToss(0).height() < 0) {
						out += "-";
					}
					out += "&";
				} else {
					out += reverseThrowHeight(curHand.getToss(0));
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
					out += reverseThrowHeight(curToss);
					//see if the throw goes where it normally does; add a "x" if not
					if(!curToss.isInfinite() && curToss.destHand() != (curToss.startHand() + curToss.height()) % 2) {
						out += "x";
					}
				}
				out += "]";
			} else {
				curToss = curHand.getLastToss();
				out += reverseThrowHeight(curToss);
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
						out += reverseThrowHeight(curToss);
						if(curToss.destHand() != (curToss.startHand() + Math.abs(curToss.height())) % 2) {
							out += "x";
						}
					}
					out += "]";
				} else {
					//account for only toss in hand
					Siteswap.Beat.Hand.Toss curToss = curHand.getLastToss();
					out += reverseThrowHeight(curToss);
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
		return "error: deparsing of mixed notation not yet implemented";
	}

	private static String reduceSiteswapString(String s) {
		//TO DO LATER
		//(also make it use * notation?)
		return s;
	}

	public static void main(String[] args) {
		if(args.length == 1) {
			Siteswap ss = parse(args[0], true);
			printf(ss);
			String s = deParse(ss);
			printf(s);
		}
	}
}
