import java.util.List;
import java.util.regex.Pattern;

public class Parser {
	private static String validGeneralSiteswapString = "((\\[((\\d|[a-w]|X|[yz])x?)+\\]|(\\d|[a-w]|X|[yz])x?)|\\((\\[((\\d|[a-w]|X|[yz])x?)+\\]|(\\d|[a-w]|X|[yz])x?),(\\[((\\d|[a-w]|X|[yz])x?)+\\]|(\\d|[a-w]|X|[yz])x?)\\)!?)+";
	private static String validAsyncSiteswapString = "((\\d|[a-w]|X|[yz])x?)+";

	public static boolean checkSyntax(String s) {
		if(Pattern.matches(validGeneralSiteswapString, s)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isAsync(String s) {
		if(Pattern.matches(validAsyncSiteswapString, s)) {
			return true;
		} else {
			return false;
		}
	}

	public static Siteswap parse(String s) {
		if(isAsync(s)) {
			return parseAsync(s);
		} else {
			return parseGeneral(s);
		}
	}
	
	public static Siteswap parseAsync(String s) {
		Siteswap out = new Siteswap(2);
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
				//if curToken is "[", we're now in a multiplex throw, so add all subsequent tosses to the same site until "]"
				case "[":
					multi = true;
					out.addBeat();
					break;
				//if curToken is "]", we're no longer in a multiplex throw, so add an empty toss to the non-current hand
				case "]":
					multi = false;
					out.getBeatAt(out.period() - 1).getSiteAt((curHand + 1) % 2).addToss();
					b++;
					break;
				//if curToken is "x", flip the destination hand of the most recently added toss
				case "x":
					out.getBeatAt(out.period() - 1).getSiteAt((curHand + 1) % 2).getLastToss().flipDestHand();
					break;
				//if curToken is anything else, it has to be a throw height (since it matched the regex for async pattern)
				default:
					int height = throwHeight(curToken);
					int destHand = (curHand + height) % 2; //0=left, 1=right
					if(!multi) {
						//create new beat
						Siteswap.Beat newBeat = out.addBeat();
						//add toss of correct height and destination to current hand
						newBeat.getSiteAt(curHand).addToss(height, destHand);
						//add empty toss to other hand
						newBeat.getSiteAt((curHand + 1) % 2).addToss();
						//increment beat index
						b++;
					} else {
						//add toss of correct height and destination to current hand
						out.getLastBeat().getSiteAt(curHand).addToss(height, destHand);
					}
					break;
			}
			//increment index in input string
			i++;
		}
		return out;
	}


	public static int throwHeight(String h) {
		if(Pattern.matches("\\d", h)) {
			return Integer.parseInt(h);
		} else if(Pattern.matches("([a-w]|[yz])", h)) {
			return (int)(h.toCharArray()[0]) - 87;
		} else { //if h is "X"
			return 33;
		}
	}

	public static Siteswap parseGeneral(String s) {
		return null;
	}

	public static String deParse(Siteswap ss) {
		return null;
	}
	
	public static void main(String[] args) {
		if(args.length == 1) {
			System.out.println(parse(args[0]));
		}
	}
}
