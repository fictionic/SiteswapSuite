import java.util.List;
import java.util.LinkedList;

public class TransitionFinder {

	//FOR DEBUGGING
	public static boolean debug = true;
	public static void printf(Object input) {
		if(debug) {
			System.out.println(input);
		}
	}
	public static Siteswap getTransition(Siteswap ss1, Siteswap ss2) {
		//check if there is a possible transition
		//(later)

		//adjust for the case of one being a one-handed and the other being two-handed
		//(later)

		//determine number of hands in transition siteswap
		//(for now just assume the two have the same number of hands)
		int numHands = ss1.numHands();

		//determine the type of the transition siteswap
		//(for now just assume the two have the same type)
		String type = ss1.type();

		//return transition between the two states
		return getTransition(getState(ss1), getState(ss2), numHands, type);
	}

	public static Siteswap getTransition(State st1, State st2, int numHands, String type) {
		Siteswap out = new Siteswap(numHands, type);

		//make sure the two states have the same length (pad the shorter one with zeroes)
		State.matchLengths(st1, st2);

		int b=-1; //index of beat in out
		int destHand;
		int height;
		Integer tempState2Value;

		//loop until we've found a transition
		while(!st1.equals(st2)) {

			//align states
			alignStates(st1, st2);

			//compute the next beat in the transition
			out.addEmptyBeat();
			b++;

			//compute hands of the beat
			for(int h=0; h<numHands; h++) {
				//while we haven't thrown all the balls in this hand at this beat
				if(st1.getCurValueAtHand(h) > 0) {
					printf("find ball");
					//compute the next toss
					//check each hand to see if it could be the destination of the current toss
					destHand = h;
					do {
						printf("check hand");
						//check each beat in the handstate to see if it could be the destination
						height = 0;
						while(height < st1.length()) {
							printf("check beat: " + height);
							printf(out);
							printf(st1);
							printf(st2);
							tempState2Value = st2.getValueAtHandAtBeat(destHand, height);
							if(tempState2Value != null) {
								if(st1.getValueAtHandAtBeat(destHand, height) < tempState2Value) {
									//we've computed the next toss!
									//...so add it
									out.addToss(b, h, height, destHand);
									printf("ADD TOSS");
									printf(out);
									break;
								}
							}
							height++;
						}
						destHand = (destHand + 1) % numHands;
					} while(destHand != h);
					//alter the states accordingly
					st1.incrementValue(h, b, false);
					st1.incrementValue(destHand, height, true);
					printf(st1);
					printf(st2);
					st1.advanceTime();
					st2.advanceTime();
					printf(st1);
					printf(st2);
				}
			}
		}
		return out;
	}

	private static void alignStates(State st1, State st2) {
		while(!st1.isAlignedWith(st2)) {
			st2.shiftForward();
			st1.padWithOneZero();
		}
	}

	private static List<Siteswap> getOrbits(Siteswap ss) {
		return null;
	}

	private static State getState(Siteswap ss) {
		return new State(ss);
	}

	/*
	   IDEAS

	   -have commandline arguments for forcing it to parse a string as a ss w/ a particular number of hands
	   -and for starting with a particular hand

	 */

	public static void main(String[] args) {
		if(args.length == 1) {
			Siteswap ss = Parser.parse(args[0]);
			System.out.println("parsed: " + ss);
			System.out.println("de-parsed: " + Parser.deParse(ss));
			System.out.println("number of balls: " + ss.numBalls());
			System.out.println("valid: " + ss.isValid());
			if(ss.isValid()) {
				System.out.println("state: " + getState(ss));
			}
		} else if(args.length == 2) {
			Siteswap ss1 = Parser.parse(args[0]);
			Siteswap ss2 = Parser.parse(args[1]);
			if(ss1.numBalls() == ss2.numBalls()) {
				System.out.println("transition from " + args[0] + " to " + args[1] + ":");
				System.out.println(Parser.deParse(getTransition(ss1, ss2)));
			} else {
				System.out.println("need to have same number of balls (for now)...");
				System.exit(1);
			}
		}
	}
}
