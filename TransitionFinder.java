import java.util.List;
import java.util.LinkedList;

public class TransitionFinder {

	//FOR DEBUGGING
	public static boolean debug = true;
	public static void printf(Object input) {
		if(debug) {
			try {
				System.out.println(input);
			} catch(NullPointerException e) {
				System.out.println("null");
			}
		}
	}

	/*public static Siteswap getTransitionWrapper(Siteswap ss1, Siteswap ss2, boolean generateBallAntiballPairs) {
		//determine the type of the transition siteswap
		//(for now just assume the two have the same type)
		String type = ss1.type();
		//get raw transition from algorithm, which may contain antitosses
		Siteswap rawTransition;
		if(generateBallAntiballPairs)
			rawTransition = getOneBeatTransition(getState(ss1), getState(ss2), type); //change this when I finish rewriting the algs
		else
			rawTransition = getMultiBeatTransition(getState(ss1), getState(ss2), type);
		//convert into siteswap with all regular tosses
		//Siteswap actualTransition = (new SiteswapTransition(ss1, rawTransition, ss2)).getUnAntiTossifiedTransition();
		//return actualTransition;
		return rawTransition;
	}*/

	/*public static Siteswap getTransitionWrapper(State st1, State st2, String type, boolean generateBallAntiballPairs) {
		//determine the type of the transition siteswap
		//(for now just assume the two have the same type)
		//get raw transition from algorithm, which may contain antitosses
		Siteswap rawTransition;
		if(generateBallAntiballPairs)
			rawTransition = getOneBeatTransition(st1, st2, type); //change this when I finish rewriting the algs
		else
			rawTransition = getMultiBeatTransition(st1, st2, type);
		//convert into siteswap with all regular tosses
		//HOW SHOULD THIS WORK...
		//Siteswap actualTransition = (new SiteswapTransition(ss1, rawTransition, ss2)).getUnAntiTossifiedTransition();
		//return actualTransition;
		return rawTransition;
	}*/

	//the fastest most general algorithm
	public static Siteswap getOneBeatTransition(State st1ORIGINAL, State st2ORIGINAL, String type) {
		int numHands = st1ORIGINAL.numHands();
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
		int b = 0; //index of beat in output siteswap
		//loop until we've found a transition
		while(!st1.equals(st2)) {
			//align states
			alignStates(st1, st2, true);
			printf("states aligned:");
			printf("st1: " + st1 + "\nst2: " + st2);
			//compute the next beat in the transition
			if(b > 0) {
				//we start out with one zero beat (that's what an empty siteswap consists of)
				//so only add them after the first beat has been calculated
				out.addZeroBeat();
				b++;
			}
			printf("compute beat " + b + ":");
			for(int h=0; h<numHands; h++) {
				//compute the next hand of this beat
				printf("\tcompute hand " + h + ":");
				//LOOP HERE
				printf(st1);
				printf(st2);
				printf("\tnowValue = " + st1.getValue(h));
				//compute the next batch of throws from this hand
				printf("\t\tcompute next toss");
				int height = st2.getShift(); //height of the next batch of tosses
				int destHand = h; //destination hand of the next batch of tosses
				int difference = 0;
				int destValue = 0;
				int compareValue = 0;
				printf("\t\tfind destination");
				printf("\t\tlooking for a spot with difference != 0");
				//check each hand of st1 for spot where the value is different than the
				//corresponding value in st2 (excluding those where st2_i == null)
				boolean foundDest = false;
				do {
					printf("\t\t\tcheck hand " + destHand);
					//check each beat of this handstate
					while(height < st1.length()) {
						printf("\t\t\t\tcheck beat " + height);
						destValue = (int)st1.getValue(destHand, height);
						compareValue = (int)st2.getValue(destHand, height);
						difference = destValue - compareValue;
						printf("\t\t\t\t\tdestValue: " + destValue);
						printf("\t\t\t\t\tcompareValue: " + compareValue);
						printf("\t\t\t\t\tdifference: " + difference);
						if(difference != 0) {
							foundDest = true;
							printf("\t\t\t\tfound destination at hand " + destHand + ", beat " + height);
							break;
						}
						height++;
					}
					if(foundDest) {
						break;
					} else {
						// try the next hand
						destHand = (destHand + 1) % numHands;
						height = st2.getShift();
					}
				} while(destHand != h); //destHand starts out as h, so when it becomes h again we'll have looked through every hand
				//
				//now actually compute the tosses in the batch
				//first see whether we'll be throwing tosses or antitosses
				boolean isAntiToss = difference > 0;
				while(true) {
					if(difference == 0)
						break;
					printf("\t\t\tdifference: " + difference);
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
			} //compute next beat (loop through hands)
			//advance time
			st1.advanceTime();
			st2.advanceTime();
			printf("advanced time: ");
			printf("st1: " + st1 + "\nst2: " + st2);
			printf(st1.equals(st2));
		} //while st1 != st2
		return out;
	}

	//REVAMPED ALGORITHM
	public static Siteswap getMultiBeatTransition(State st1ORIGINAL, State st2ORIGINAL, String type, boolean allowExtraSqueezeCatches) {
		int numHands = st1ORIGINAL.numHands();
		State st1 = st1ORIGINAL.deepCopy();
		State st2 = st2ORIGINAL.deepCopy();
		Siteswap transition = new Siteswap(numHands, type);
		printf("input states:");
		printf("st1: " + st1 + "\nst2: " + st2);
		//make sure the two states have the same length (pad the shorter one with zeroes)
		State.matchLengths(st1, st2);
		printf("lengths matched:");
		printf("st1: " + st1 + "\nst2: " + st2);
		//check to see if the states are already equal
		if(st1.equals(st2)) {
			return transition;
		}
		//looping variables
		int ballNumDiff;
		List<Integer> diffs; 
		int diffSumPositive;
		int diffSumNegative; 
		int tossSumPositive = 0;
		int tossSumNegative = 0;
		boolean shifted = false;
		boolean shouldShift;
		int b = 0; //index of beat in output siteswap
		//while there are still tosses left to be made within the transition
		while(true) {
			//update ballnumdiff
			ballNumDiff = st2.numBalls() - st1.numBalls();
			printf("ballNumDiff: " + ballNumDiff);
			//update diffsums
			if(shifted)
				diffs = st1.getDiffSumsWith(st2, 1);
			else
				diffs = st1.getDiffSumsWith(st2, 0);
			diffSumPositive = diffs.get(0);
			diffSumNegative = diffs.get(1);
			printf("diffSums: " + diffs.toString());

			//see if we're done
			if(ballNumDiff + tossSumPositive + tossSumNegative == 0 && tossSumPositive == diffSumPositive && tossSumNegative == diffSumNegative) {
				printf("done");
				break;
			} else
				printf("not done");

			if(!shifted) {
				//see if we should catch a new ball/antiball before shifting (for when we want to end up with a different number of balls)
				for(int h=0; h<numHands; h++) {
					int nowValue1 = st1.getValue(h);
					int nowValue2 = st2.getValue(h);
					if(nowValue2 != 0) {
						if((nowValue1 > 0 && diffSumPositive == 0) || (nowValue1 < 0 && diffSumNegative == 0)) {
							if(nowValue1 > 0) {
								while(st1.getValue(h) > 0) {
									printf("catching new ball");
									//catch a new ball
									transition.addToss(b, h, 0, true, 0, false);
									st1.throwBall(h, 0, true, 0, false);
								}
							} else {
								while(st1.getValue(h) < 0) {
									printf("catching new antiball");
									//catch a new antiball
									transition.addToss(b, h, 0, true, 0, true);
									st1.throwBall(h, 0, true, 0, true);
								}
							}
						}
					}
				}
				//see if we need to shift
				if(blaflah) {
					printf("shifting...");
					st1.padWithOneZero();
					st2.shiftForwardNEW();
					shifted = true;
					printf("st1: " + st1);
					printf("st2: " + st2);
				}
			}

			break;
		}
		return transition;
	}

	private static void alignStates(State st1, State st2, boolean generateBallAntiballPairs) {
		while(!st1.isAlignedWith(st2, generateBallAntiballPairs)) {
			st2.shiftForward();
			st1.padWithOneZero();
		}
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
		} else if(args.length >= 2) {
			if(args.length == 2) {
				String type1 = Parser.getNotationType(args[0]);
				String type2 = Parser.getNotationType(args[1]);
				Siteswap ss1;
				Siteswap ss2;
				if(type1.equals("async") && type2.equals("async")) {
					//then treat them both as one-handed siteswaps
					ss1 = Parser.parse(args[0], true);
					ss2 = Parser.parse(args[1], true);
				} else {
					//then treat them both as two-handed siteswaps, starting by default with the left hand
					ss1 = Parser.parse(args[0], false);
					ss2 = Parser.parse(args[1], false);
				}
				if(ss1.numHands() != ss2.numHands()) {
					System.out.println("error: given patterns have a different number of hands!");
					System.exit(1);
				}
				if(ss1.numBalls() != ss2.numBalls()) {
					System.out.println("need to have same number of balls (for now)...");
					System.exit(1);
				}
				boolean generateBallAntiballPairs = false;
				boolean allowExtraSqueezeCatches = false;
				//Siteswap transition = getTransitionWrapper(ss1, ss2, generateBallAntiballPairs);
				Siteswap transition = getMultiBeatTransition(getState(ss1), getState(ss2), ss1.type(), allowExtraSqueezeCatches);
				if(transition.period() == 0) {
					System.out.println("(no transition necessary)");
				} else {
					System.out.println("transition from " + args[0] + " to " + args[1] + ":");
					System.out.println(Parser.deParse(transition));
					//Siteswap reverse = getTransitionWrapper(ss2, ss1);
					//Siteswap thereAndBack = ss1.annexPattern(transition).annexPattern(ss2).annexPattern(reverse);
					//System.out.println(thereAndBack.isValid());
				}
			} else if(args.length == 3) {
				State st1 = new State(args[1]);
				State st2 = new State(args[2]);
				Siteswap transition = getMultiBeatTransition(st1, st2, "async", false);
				System.out.println(Parser.deParse(transition));
			}
		}
	}
}
