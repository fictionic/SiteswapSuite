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
		//(wait, what did I mean by this?... hmm)

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

		//check to see if the states are already equal
		if(st1.equals(st2)) {
			//then we're done; return the empty siteswap
			return out;
		}

		printf("input states:");
		printf("st1: " + st1 + "\nst2: " + st2);
		//make sure the two states have the same length (pad the shorter one with zeroes)
		State.matchLengths(st1, st2);
		printf("lengths matched:");
		printf("st1: " + st1 + "\nst2: " + st2);

		//determine the difference in the number of balls in the two siteswaps
		int gapSize = st2.numBalls() - st1.numBalls();

		int b=-1; //index of beat in out
		int destHand;
		int height;
		Integer tempState2Value;
		boolean madeToss = false;

		//loop until we've found a transition
		while(!st1.equals(st2)) {

			//align states
			alignStates(st1, st2);
			printf("states aligned:");
			printf("st1: " + st1 + "\nst2: " + st2);

			//compute the next beat in the transition
			out.addEmptyBeat();
			b++;
			printf("compute beat " + b);

			//compute hands of the beat
			for(int h=0; h<numHands; h++) {
				printf("\tcompute hand " + h);
				printf("st1: " + st1);
				printf("st2: " + st2);
				//check to see if we need to add any tosses here
				if(st1.getValueAtHand(h) > 0) {
					//while we haven't thrown all the balls in this hand at this beat
					while(st1.getValueAtHand(h) > 0) {
						printf("\t\tfind ball in hand " + h + " of st1");
						printf("\t\tcompute destination of ball:");
						//compute the next toss
						//check each hand to see if it could be the destination of the current toss
						destHand = h;
						do {
							printf("\t\t\tcheck hand: " + destHand + " of st2");
							//check each beat in the handstate to see if it could be the destination
							height = 0;
							while(height < st1.length()) {
								printf("\t\t\t\tcheck beat: " + height);
								tempState2Value = st2.getValueAtHandAtBeat(destHand, height);
								printf("st1: " + st1);
								printf("st2: " + st2);

								if(tempState2Value != null) {
									printf(st1.getValueAtHandAtBeat(destHand, height));
									printf(tempState2Value);
									if(st1.getValueAtHandAtBeat(destHand, height) < tempState2Value) {
										//we've computed the next toss!
										//...so add it
										out.addToss(b, h, height, destHand);
										st1.throwBall(h, height, false, destHand, false); //FIX THIS!!! (shouldn't always be false)
										printf("\t\t\t\tfound destination!");
										printf("\t\t\tadd toss: at beat " + b + ", from hand " + h + ", to hand " + destHand + ", height = " + height);
										printf("st1: " + st1);
										printf("st2: " + st2);
										printf("ss: " + out);
										madeToss = true;
										break;
									}
								}
								height++;
							}
							if(madeToss) {
								madeToss = false;
								break;
							}
							destHand = (destHand + 1) % numHands;
						} while(destHand != h);
					}
				} else {
					//otherwise we can just add a zero toss
					printf("add zero toss to hand " + h + ", beat " + b);
					out.addToss(b, h, 0, h);
					printf("out: " + out);
				}
			}

			//advance time
			printf("advance time:");
			st1.advanceTime();
			st2.advanceTime();
			printf("st1: " + st1);
			printf("st2: " + st2);
		}
		return out;
		//IDEA: MAKE RECURSIVE VERSION OF THIS METHOD THAT DOES A DEPTH-FIRST SEARCH OF ALL DIRECT TRANSITIONS!!!
	}

	private static void alignStates(State st1, State st2) {
		while(!st1.isAlignedWith(st2)) {
			st2.shiftForward();
			st1.padWithOneZero();
		}
	}

	private static List<Siteswap> getOrbits(Siteswap ss) {
		//to do eventually...
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
			Siteswap ss = Parser.parse(args[0], true);
			System.out.println("parsed: " + ss);
			System.out.println("de-parsed: " + Parser.deParse(ss));
			System.out.println("number of balls: " + ss.numBalls());
			System.out.println("valid: " + ss.isValid());
			if(ss.isValid()) {
				System.out.println("period: " + ss.period());
				System.out.println("state: " + getState(ss));
			}
		} else if(args.length == 2) {
			String type1 = Parser.getNotationType(args[0]);
			String type2 = Parser.getNotationType(args[1]);
			Siteswap ss1;
			Siteswap ss2;
			//TO DO: HAVE OPTION FOR WHICH HAND TO START ASYNC PATTERN WITH
			if(type1.equals("async") && type2.equals("async")) {
				//then treat them both as one-handed siteswaps
				ss1 = Parser.parse(args[0], true);
				ss2 = Parser.parse(args[1], true);
			} else {
				//then treat them both as two-handed siteswaps, starting by default with the left hand
				//(TO DO: 3+-handed patterns??)
				ss1 = Parser.parse(args[0], false);
				ss2 = Parser.parse(args[1], false);
			}
			//NEED TO FIGURE OUT A WAY TO DETERMINE HOW MANY HANDS A PATTERN CAN HAVE BASED ON ITS STRING
			if(ss1.numBalls() == ss2.numBalls()) {
				System.out.println("transition from " + args[0] + " to " + args[1] + ":");
				String transition = Parser.deParse(getTransition(ss1, ss2));
				if(transition.equals("")) {
					System.out.println("(no transition necessary)");
				} else {
					System.out.println(transition);
				}
			} else {
				System.out.println("need to have same number of balls (for now)...");
				System.exit(1);
			}
		} else if(args.length == 3) {
			//for now, assume first parameter is option saying to interpret args[1] and args[2] as states, not siteswaps
			System.out.println("transition from " + args[1] + " to " + args[2] + ":");
			State st1 = new State(args[1]);
			State st2 = new State(args[2]);
			System.out.println(Parser.deParse(getTransition(st1, st2, st1.numHands(), "sync")));
		}
	}
}
