import java.util.List;
import java.util.regex.Pattern;

public class Parser {
	private static String validAsyncSiteswapString = "((\\d|[a-w]|X|[yz])x?)+";
	private static String validSynchronousSiteswapString = "(\\((\\[((\\d|[a-w]|X|[yz])x?)+\\]|(\\d|[a-w]|X|[yz])x?),(\\[((\\d|[a-w]|X|[yz])x?)+\\]|(\\d|[a-w]|X|[yz])x?)\\))+\\*?";
	private static String validMixedNotationTwoHandedSiteswapString = "((\\[((\\d|[a-w]|X|[yz])x?)+\\]|(\\d|[a-w]|X|[yz])x?)|\\((\\[((\\d|[a-w]|X|[yz])x?)+\\]|(\\d|[a-w]|X|[yz])x?),(\\[((\\d|[a-w]|X|[yz])x?)+\\]|(\\d|[a-w]|X|[yz])x?)\\)!?)+";

	public static boolean checkSyntax(String s) {
		if(Pattern.matches(validMixedNotationTwoHandedSiteswapString, s)) {
			return true;
		} else {
			return false;
		}
	}

	public static String getNotationType(String s) {
		if(Pattern.matches(validAsyncSiteswapString, s)) {
			return "async";
		} else if(Pattern.matches(validSynchronousSiteswapString, s)) {
			return "sync";
		} else {
			return "mixed";
		}
	}

	public static Siteswap parse(String s) {
		switch(getNotationType(s)) {
			case "async":
				return parseAsync(s);
			case "sync":
				return parseSync(s);
			default:
				//case "mixed":
				return parseMixed(s);
		}	
	}
	
	public static Siteswap parseAsync(String s) {
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
		while(i < s.length()) {
			curToken = ((Character)s.charAt(i)).toString();
			//update current hand
			curHand = i % 2;
			switch(curToken) {
				//if curToken is "[", we're now in a multiplex throw, so add all subsequent tosses to the same hand until "]"
				case "[":
					multi = true;
					out.addBeat();
					break;
					//if curToken is "]", we're no longer in a multiplex throw, so add an empty toss to the non-current hand
				case "]":
					multi = false;
					out.getBeat(out.period() - 1).getHand((curHand + 1) % 2).addToss();
					b++;
					break;
					//if curToken is "x", flip the destination hand of the most recently added toss
				case "x":
					out.getBeat(out.period() - 1).getHand((curHand + 1) % 2).getLastToss().flipDestHand();
					break;
					//if curToken is anything else, it has to be a throw height (since it matched the regex for async pattern)
				default:
					int height = throwHeight(curToken);
					int destHand = (curHand + height) % 2; //0=left, 1=right
					if(!multi) {
						//create new beat
						Siteswap.Beat newBeat = out.addBeat();
						//add toss of correct height and destination to current hand
						newBeat.getHand(curHand).addToss(height, destHand);
						//add empty toss to other hand
						newBeat.getHand((curHand + 1) % 2).addToss();
						//increment beat index
						b++;
					} else {
						//add toss of correct height and destination to current hand
						out.getLastBeat().getHand(curHand).addToss(height, destHand);
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
		String curToken;

		while(i < s.length()) {
			curToken = ((Character)s.charAt(i)).toString();
			switch(curToken) {
				case "(":
					//create new beat
					out.addBeat();
					curHand = 0;
					break;
				case ",":
					curHand = 1;
					break;
				case ")":
					//increase beat index by 2, since every (a,b) counts as two beats
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
					//need to make this only star-ify the last non-starified portion of the pattern!!!
					//(e.g. to prevent the first part of (4,2)*(6,2)* from getting star-ified twice)
					/*IDEA: keep track of last beat at which addStar was called, call it on a sub-pattern
					of the siteswap, and annex it to out with Siteswap.annexPattern()*/
					out.addStar();
					break;
				default: //curToken is a throw height
					int height = throwHeight(curToken);
					int destHand = (curHand + height) % 2;
					Siteswap.Beat curBeat = out.getLastBeat();
					curBeat.getHand(curHand).addToss(height, destHand);
					break;
			}
			i++;
		}

		return out;
	}

	private static Siteswap parseMixed(String s) {
		//later...
		return null;
	}

	private static int throwHeight(String h) {
		if(Pattern.matches("\\d", h)) {
			return Integer.parseInt(h);
		} else if(Pattern.matches("([a-w]|[yz])", h)) {
			return (int)(h.toCharArray()[0]) - 87;
		} else { //if h is "X"
			return 33;
		}
	}

	private static String reverseThrowHeight(Integer h) {
		if(h <= 9) {
			return h.toString();
		} else if((10 <= h) && (h <= 36) && h != 33) {
			return (Character.toChars(h - 10 + 97)).toString();
		} else if(h == 33) {
			return "X";
		} else {
			return "";
		}
	}

	//IDEA: make parse() treat async siteswaps as one-handed, and have a method in Siteswap that turns a one-handed ss into a two-handed one


	public static String deParse(Siteswap ss) {
		if(ss.numHands == 2) {
			switch(ss.type) {
				case "async":
					return reduceSiteswapString(deParseAsync(ss));
				case "sync":
					return reduceSiteswapString(deParseSync(ss));
				default:
					//case "mixed":
					return reduceSiteswapString(deParseMixed(ss));
			}
		} else {
			//later...
			return "not yet implemented";
		}
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
					out += reverseThrowHeight(curToss.height);
					//see if the throw goes where it normally does; add a "x" if not
					if(curToss.destHand != (curToss.startHand + curToss.height) % 2) {
						out += "x";
					}
				}
				out += "]";
			} else {
				curToss = curHand.getLastToss();
				out += reverseThrowHeight(curToss.height);
				//see if the throw goes where it normally does; add a "x" if not
				if(curToss.destHand != (curToss.startHand + curToss.height) % 2) {
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
		//loop through beats of siteswap
		for(int b=0; b<ss.period(); b++) {
			out += "(";
			Siteswap.Beat curBeat = ss.getBeat(b);
			//loop through hands within each beat
			for(int h=0; h<2; h++) {
				Siteswap.Beat.Hand curHand = curBeat.getHand(h);
				//see if we need to add multiplex notation
				if(curHand.numTosses() > 1) {
					out += "[";
					//loop through tosses within hand
					for(int t=0; t<curHand.numTosses(); t++) {
						Siteswap.Beat.Hand.Toss curToss = curHand.getToss(t);
						out += reverseThrowHeight(curToss.height);
						if(curToss.destHand != (curToss.startHand + curToss.height) % 2) {
							out += "x";
						}
					}
					out += "]";
				} else {
					//account for only toss in hand
					Siteswap.Beat.Hand.Toss curToss = curHand.getLastToss();
					out += reverseThrowHeight(curToss.height);
					if(curToss.destHand != (curToss.startHand + curToss.height) % 2) {
						out += "x";
					}
				}
				//put a comma if we've just finished doing the left hand
				if(h == 0) {
					out += ",";
				}
			}
			out += ")";
		}
		return out;
	}

	private static String deParseMixed(Siteswap ss) {
		return "";
	}

	private static String reduceSiteswapString(String s) {
		//TO DO LATER
		//(also make it use * notation?)
		return s;
	}

	public static void main(String[] args) {
		if(args.length == 1) {
			Siteswap ss = parse(args[0]);
			System.out.println(ss);
			String s = deParse(ss);
			System.out.println(s);
		}
	}
}
