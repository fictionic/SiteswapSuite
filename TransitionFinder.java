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

		State st1 = getState(ss1);
		State st2 = getState(ss2);

		//see whether either of the states have negative values--if so, we'll need a more general algorithm
		if(st1.hasNegatives() || st2.hasNegatives()) {
			return getTransitionFULL(st1, st2, numHands, type);
		}

		return getTransition(st1, st2, numHands, type);
	}

	public static Siteswap getTransitionFULL(State st1, State st2, int numHands, String type) {
		Siteswap out = getTransitionGENERAL(st1, st2, numHands, type);
		System.out.println(out);
		System.out.println(getTransitionGENERAL(st2, st1, numHands, type));
		out.annexPattern(getTransitionGENERAL(st2, st1, numHands, type));
		System.out.println(out);
		out.unAntiTossify();
		System.out.println(out);
		return out;

	}

	//the fastest most general algorithm (I might want to make one that never generates ball/antiball pairs, only uses what's already there...)
	public static Siteswap getTransitionGENERAL(State st1ORIGINAL, State st2ORIGINAL, int numHands, String type) {
		State st1 = st1ORIGINAL.deepCopy();
		State st2 = st2ORIGINAL.deepCopy();
		Siteswap out = new Siteswap(numHands, type);

		//check to see if the states are already equal
		if(st1.equals(st2)) {
			return out;
		}
		printf("input states:");
		printf("st1: " + st1 + "\nst2: " + st2);
		//make sure the two states have the same length (pad the shorter one with zeroes)
		State.matchLengths(st1, st2);
		printf("lengths matched:");
		printf("st1: " + st1 + "\nst2: " + st2);

		//looping variables
		int b = 0; //index of beat in output siteswap

		//loop until we've found a transition
		while(!st1.equals(st2)) {

			//align states
			printf("aligning states...");
			alignStatesGENERAL(st1, st2);
			printf("states aligned:");
			printf("st1: " + st1 + "\nst2: " + st2);

			//compute the next beat in the transition
			if(b > 0) {
				//we start out with one zero beat, so only add them after the first beat has been calculated
				out.addZeroBeat();
				b++; //b needs to start at -1 so it can be 0 when this is printed, and this needs to be printed here
			}
			printf("compute beat " + b + ":");

			for(int h=0; h<numHands; h++) {
				//compute the next hand of this beat
				printf("\tcompute hand " + h + ":");
				//loop while there are still destinations for throws ???
				while((st1.getValue(h) != 0 || st1.allNowValuesAreZero()) && !st1.equalsUpTo(st2, st2.getShift())) {
					printf(st1);
					printf(st2);
					printf("\tnowValue = " + st1.getValue(h));
					//compute the next batch of throws from this hand
					printf("\t\tcompute next batch of tosses");
					//HEIGHT STARTS AT SHIFT VALUE BECAUSE IN GENERAL THERE'S NO NEED FOR A TRANSITION TO HAVE A 0 TOSS (I THINK???)
					//(it would start at 1 but we don't want it to check against any shifted values of st2, cuz they're null)
					int height = st2.getShift(); //height of the next batch of tosses
					int destHand = h; //destination hand of the next batch of tosses
					int difference = 0;
					int destValue = 0;
					int compareValue = 0;
					printf("\t\tfind destination");
					//check each hand of st1 for spot where the value is different than the
					//corresponding value in st2 (excluding those where st2_i == null)
					boolean foundDest = false;
					do {
						printf("\t\t\tcheck hand " + destHand);
						//check each beat of this handstate
						printf("height: " + height + ", st1.length(): " + st1.length());
						while(height < st1.length()) {
							printf("\t\t\t\tcheck beat " + height);
							if(st1.getValue(destHand, height) != st2.getValue(destHand, height)) {
								//this isn't the most efficient way to get this value, since
								//each call of getvalue runs height times (so it's O(n^2))
								//but generally states are small enough that it's not a big deal
								destValue = st1.getValue(destHand, height);
								compareValue = st2.getValue(destHand, height);
								difference = destValue - compareValue;
								printf("\t\t\t\tfound destination at hand " + destHand + ", beat " + height);
								foundDest = true;
								break;
							}
							height++;
						}
						if(foundDest) {
							break;
						}

						destHand = (destHand + 1) % numHands;
						height = st2.getShift();
					} while(destHand != h);
					//check for if nothing happened in the loop? what would that mean? is it possible?

					//now actually compute the tosses in the batch
					//we want to make tosses from this hand until 
					//first see whether we'll be throwing tosses or antitosses
					printf("destHand: " + destHand + ", height: " + height);
					boolean isAntiToss = difference > 0;
					while(difference != 0) {
						printf("difference: " + difference);
						//throw another [anti]ball
						st1.throwBall(h, height, false, destHand, isAntiToss);
						out.addToss(b, h, height, false, destHand, isAntiToss);
						//update difference
						difference = st1.getValue(destHand, height) - st2.getValue(destHand, height);
						String toPrint = "\t\t\tthrew ";
						if(isAntiToss) {
							toPrint += "anti";
						}
						toPrint += "ball of height " + height + " to hand " + destHand;
						printf(toPrint);
						printf("st1: " + st1 + "\nst2: " + st2);
						printf("ss: " + out);
					} //compute next batch of tosses
				} //compute next hand
			} //compute next beat (loop through hands)
			//advance time
			st1.advanceTime();
			st2.advanceTime();
			printf("advanced time: ");
			printf("st1: " + st1 + "\nst2: " + st2);
		} //while st1 != st2
		return out;
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
				if(st1.getValue(h) > 0) {
					//while we haven't thrown all the balls in this hand at this beat
					while(st1.getValue(h) > 0) {
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
								tempState2Value = st2.getValue(destHand, height);
								if(tempState2Value != null) {
									if(st1.getValue(destHand, height) < tempState2Value) {
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

	private static void alignStatesGENERAL(State st1, State st2) {
		while(!st1.isAlignedWithGENERAL(st2)) {
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
			System.out.println(Parser.deParse(getTransitionFULL(st1, st2, st1.numHands(), "sync")));
		}
	}
}
