import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;

public class State {
	private List<HandState> hands;
	private int numBalls;
	private int shift;

	public static boolean debug = false;
	public static void printf(Object input) {
		if(debug) {
			try {
				System.out.println(input.toString());
			} catch(NullPointerException e) {
				System.out.println("null");
			}
		}
	}

	public State(Siteswap ss) {
		//initialize instance variable
		this.hands = new ArrayList<HandState>();
		this.numBalls = 0;
		this.shift = 0;
		int goalNumBalls = ((Double)ss.numBalls()).intValue();
		//create an empty HandState for each hand in the pattern
		for(int h=0; h<ss.numHands(); h++) {
			hands.add(new HandState());
		}
		//emulate juggling the pattern, adding balls to hands at beats when needed
		//
		// first turn all of ss's negative tosses into antitosses
		ss.antiTossify();
		printf("computing state for pattern: " + ss);
		int debugCounter = 0;
		do {
			for(int b=0; b<ss.period(); b++) {
				printf("state: " + hands);
				printf("curBeat: " + b);
				//loop through hands
				for(int h=0; h<ss.numHands(); h++) {
					printf("\tcurHand: " + h);
					//account for + throw each ball in this hand at this beat
					for(Siteswap.Beat.Hand.Toss curToss : ss.getBeat(b).getHand(h).tosses) {
						printf("\t\tcurToss: " + curToss);
						//see if we need to do anything with this toss
						if(curToss.height() != 0) {
							//see if we need to account for a ball/antiball
							if(getValue(h) == 0) {
								if(curToss.isAntiToss()) {
									decrementValue(h);
									numBalls--;
									printf("\t\t\taccounted for antiball");
									printf("\t\t\tstate: " + hands);
								} else {
									incrementValue(h);
									this.numBalls++;
									printf("\t\t\taccounted for ball");
									printf("\t\t\tstate: " + hands);
								}
							} else {
								//see if state was opposite sign of curToss
								//(don't know if this is possible for valid siteswaps)
								if(curToss.isAntiToss()) {
									if(getValue(h) > 0) {
										System.out.println("state has opposite sign as curToss...");
										System.exit(1);
									}
								} else {
									if(getValue(h) < 0) {
										System.out.println("state has opposite sign as curToss...");
										System.exit(1);
									}
								}
							}
							throwBall(h, curToss.height(), curToss.isInfinite(), curToss.destHand(), curToss.isAntiToss());
							//adjust changes to numBalls as a result of infinite-valued tosses
							printf("old numBalls: " + numBalls);
							if(curToss.isInfinite()) {
								if(curToss.isAntiToss()) {
									this.numBalls++;
								} else {
									this.numBalls--;
								}
							}
							printf("new numBalls: " + numBalls);
							//print helpful stuff
							if(curToss.isAntiToss()) {
								if(curToss.isInfinite()) {
									printf("\t\t\tthrew antiball with height infinity");
								} else {
									printf("\t\t\tthrew antiball with height " + curToss.height());
								}
							} else {
								if(curToss.isInfinite()) {
									printf("\t\t\tthrew ball with height infinity");
								} else {
									printf("\t\t\tthrew ball with height " + curToss.height());
								}
							}
							printf("\t\t\tstate: " + hands);
						} else {
							if(getValue(h) != 0) {
								System.out.println("encountered zero-toss when state was nonzero...");
								System.exit(1);
							}
						}
					}
					if(getValue(h) != 0) {
						System.out.println("didn't get rid of all balls in hand " + h + " at beat " + b);
						System.exit(1);
					}
				}
				//advance time
				advanceTime();
				printf("\tadvanced time");
				printf("\tstate: " + hands);
			}
			printf("balls accounted for: " + this.numBalls);
			printf("ss.numBalls(): " + goalNumBalls);
			/* debugCounter++;
			if(debugCounter > 5) {
				System.exit(1);
			} */
		} while(this.numBalls != goalNumBalls);
	}

	private State(int numHands) {
		this.hands = new ArrayList<HandState>();
		for(int i=0; i<numHands; i++) {
			hands.add(new HandState());	
		}
	}

	public State(String stateString) {
		String validStateString = "(-?(\\d|a-w|X|yz))+|\\[(-?(\\d|a-w|X|yz))+(,(-?(\\d|a-w|X|yz))+)*\\]";
		if(!Pattern.matches(validStateString, stateString)) {
			System.out.println("incorrectly formatted state");
			System.exit(1);
		}
		this.hands = new ArrayList<HandState>();
		this.numBalls = 0;
		//see if it's one-handed
		if(!stateString.substring(0,1).equals("[")) {
			HandState hand = new HandState(stateString);
			hands.add(hand);
			this.numBalls = hand.numBallsInHand;
		} else {
			int i = 1; //index of first character of current handString
			int j = stateString.indexOf(",",i);
			while(j != -1) {
				printf("i: " + i + ", j: " + j);
				printf("substring: \'" + stateString.substring(i,j) + "\'");
				HandState newHand = new HandState(stateString.substring(i,j));
				hands.add(newHand);
				this.numBalls += newHand.numBallsInHand;
				//look for the next handString
				i = j + 1; //index of the character after the last comma
				j = stateString.indexOf(",",i); //index of the next comma, if there is one (if not then j==-1 and the loop breaks)
			}
			//add the last handString
			j = stateString.length() - 1;
			printf("i: " + i + ", j: " + j);
			printf("substring: \'" + stateString.substring(i,j) + "\'");
			HandState hand = new HandState(stateString.substring(i,j));
			hands.add(hand);
			this.numBalls += hand.numBallsInHand;
		}
	}

	public int numBalls() {
		return numBalls;
	}

	public int numHands() {
		return hands.size();
	}
	
	public int getShift() {
		return shift;
	}

	public int length() {
		//this should only get called when all handStates have the same length,
		//since they only don't when the state is being created
		return hands.get(0).length;
	}

	public void incrementValue(int handIndex) {
		incrementValue(handIndex, 0);
	}

	public void incrementValue(int handIndex, int beatsFromNow) {
		try {
			hands.get(handIndex).incrementValue(beatsFromNow);
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("handIndex cannot be greater than numHands");
		}
	}

	public void decrementValue(int handIndex) {
		decrementValue(handIndex, 0);
	}

	public void decrementValue(int handIndex, int beatsFromNow) {
		try {
			hands.get(handIndex).decrementValue(beatsFromNow);
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("handIndex cannot be greater than numHands");
		}
	}

	public void padWithOneZero() {
		for(HandState h : hands) {
			h.padWithOneZero();
		}
	}

	public Integer getValue(int handIndex) {
		return hands.get(handIndex).nowNode.value;
	}

	public Integer getValue(int handIndex, int beatsFromNow) {
		return hands.get(handIndex).valueAt(beatsFromNow);
	}

	public List<Integer> getDiffSumsWith(State otherState, int shiftAmount) {
		List<Integer> diffs = new ArrayList<Integer>();
		int diff, positive = 0, negative = 0;
		for(int h=0; h<hands.size(); h++) {
			HandState.HandStateNode node1 = hands.get(h).nowNode;
			HandState.HandStateNode node2 = otherState.hands.get(h).nowNode;
			while(shiftAmount > 0) {
				node1 = node1.prevNode;
				node2 = node2.prevNode;
				shiftAmount--;
			}
			while(node1 != null) {
				diff = node2.value - node1.value;
				if(diff > 0)
					positive += diff;
				if(diff < 0)
					negative += diff;
				node1 = node1.prevNode;
				node2 = node2.prevNode;
			}
		}
		diffs.add(positive);
		diffs.add(negative);
		return diffs;
	}

	public boolean isAlignedWith(State otherState, boolean generateBallAntiballPairs) {
		//check that the two have the same length
		//(they should, becanegatiuse this method should only get called
		// after matchLengths() has been run)
		if(length() != otherState.length()) {
			return false;
		}
		/*
		outline:
		find the sum of the values of all the shifted-over nodes in each hand of st1, assign to shiftSum
		find the sum of every difference st2_i - st1_i, where i ranges over every remaining node in st1, st2, assign to diffSum
		st1 is aligned with st2 if and only if diffSum == shiftSum
		diffSum == shiftSum if and only if it is possible to turn st1 into st2 by performing some sequence of tosses on st1 within one beat, then advancing time one beat
		   */
		//but first see if they haven't been shifted at all. if this is the case then they just have to be the same state,
		//since there isn't any time to make any throws
		if(otherState.getShift() == 0) {
			return this.equals(otherState);
		}
		//we'll compute all four of these values without using the dedicated methods, because that would be slower
		int shiftSumPositive = 0;
		int shiftSumNegative = 0;
		int diffSumPositive = 0;
		int diffSumNegative = 0;
		//calculate sums
		for(int h=0; h<hands.size(); h++) {
			HandState.HandStateNode node1 = hands.get(h).nowNode;
			HandState.HandStateNode node2 = otherState.hands.get(h).nowNode;
			//calculate component of shiftSum from each handState
			for(int b=0; b<otherState.shift; b++) {
				if(node1.value > 0)
					shiftSumPositive += node1.value;
				if(node1.value < 0)
					shiftSumNegative += node1.value;
				node1 = node1.prevNode;
				node2 = node2.prevNode;
			}
			//calculate component of diffSum from each handState
			while(node1 != null) {
				int diff = node2.value - node1.value;
				if(diff > 0)
					diffSumPositive += diff;
				if(diff < 0)
					diffSumNegative += diff;
				node1 = node1.prevNode;
				node2 = node2.prevNode;
			}

		}
		printf("shiftSumPositive: " + shiftSumPositive + "\tshiftSumNegative: " + shiftSumNegative);
		printf("diffSumPositive: " + diffSumPositive + "\tdiffSumNegative: " + diffSumNegative);
		if(generateBallAntiballPairs)
			return shiftSumPositive + shiftSumNegative == diffSumPositive + diffSumNegative;
		else
			return (shiftSumPositive == diffSumPositive && shiftSumNegative == diffSumNegative);
	}

	public boolean allNowValuesAreZero() {
		for(HandState h : hands) {
			if(h.valueAt(0) != 0) {
				return false;
			}
		}
		return true;
	}

	public void shiftForward() {
		shift++;
		for(HandState h : hands) {
			h.shiftForward();
		}
	}

	public void shiftForwardNEW() {
		shift++;
		for(HandState h : hands) {
			h.shiftForwardNEW();
		}
	}

	public static void matchLengths(State state1, State state2) {
		//find the longer of the two states
		//(this method only gets called after both states have run matchHandStateLengths()
		// so we can just take hands.get(0).length as the length of the state)
		State longer = state1;
		State shorter = state2;
		if(longer.length() < shorter.length()) {
			State temp = longer;
			longer = shorter;
			shorter = temp;
		}
		//pad the shorter one with zeroes until it's as long as longer
		while(shorter.length() < longer.length()) {
			shorter.padWithOneZero();
		}
	}

	private void matchHandStateLengths() {
		//determine which handstate is the longest
		int longestLength = hands.get(0).length;
		int i;
		for(i=1; i<hands.size(); i++) {
			if(hands.get(i).length > longestLength) {
				longestLength = hands.get(i).length;
			}
		}
		//pad all handstates with the appropriate number of zeroes
		for(i=0; i<hands.size(); i++) {
			for(int j=hands.get(i).length; j<longestLength; j++) {
				hands.get(i).padWithOneZero();
			}
		}
	}

	public void throwBall(int fromHand, int height, boolean isInfinite, int destHand, boolean isAntiToss) {
		hands.get(fromHand).throwBall(height, isInfinite, destHand, isAntiToss);
		//then put the ball/antiball at its destination height, but only if it isn't infinite
		if(!isInfinite) {
			//alter the value where the ball gets thrown to by one, again depending on whether or not it's a ball or an antiball
			//(ball --> increment; antiball --> decrement)
			hands.get(destHand).alterValueByOne(height, !isAntiToss);
		} else {
			if(isAntiToss)
				this.numBalls++;
			else
				this.numBalls--;
		}
	}

	//protected cuz it's used by TransitionFinder.java
	protected void advanceTime() {
		for(HandState h : hands) {
			h.advanceTime();
		}
		if(shift > 0) {
			shift--;
		}
	}

	private void clipOffExtraNodes() {
		for(HandState h : hands) {
			h.clipOffExtraNodes();
		}
	}

	public boolean hasNegatives() {
		//check if there are any nodes with negative values
		for(HandState h : hands) {
			HandState.HandStateNode node = h.nowNode;
			while(node != null) {
				if(node.value != null && node.value < 0) {
					return true;
				}
				node = node.prevNode;
			}
		}
		return false;
	}

	public State deepCopy() {
		State out = new State(numHands());
		List<HandState> newHands = new ArrayList<HandState>();
		for(int h=0; h<numHands(); h++) {
			newHands.add(hands.get(h).deepCopy());
		}
		out.hands = newHands;
		out.numBalls = this.numBalls;
		return out;
	}

	public String toString() {
		//clipOffExtraNodes(); need to put this somewhere better!
		matchHandStateLengths();

		return hands.toString();
	}

	public boolean equalsUpTo(State otherState, int shift) {
		if(hands.size() != otherState.hands.size()) {
			return false;
		}
		for(int h=0; h<hands.size(); h++) {
			if(!hands.get(h).equalsUpTo(otherState.hands.get(h), shift)) {
				return false;
			}
		}
		return true;
	}

	public boolean equals(State otherState) {
		return equalsUpTo(otherState, 0);
	}

	private class HandState {
		private HandStateNode nowNode;
		private HandStateNode lastNode;
		private int length;
		private int numBallsInHand;

		private HandState() {
			this.nowNode = new HandStateNode(0, null);
			this.lastNode = nowNode;
			this.length = 1;
			this.numBallsInHand = 0;
		}

		private HandState(HandStateNode newNowNode, int newLength) {
			this.nowNode = newNowNode;
			this.length = newLength;
			this.numBallsInHand = 0;
		}

		private HandState(String string) {
			//get the value of the nowNode from the last character of the string
			String curToken = string.substring(string.length()-1, string.length());
			this.nowNode = new HandStateNode(Integer.parseInt(curToken), null);
			this.length = 1;
			this.numBallsInHand = nowNode.value;
			HandStateNode node = nowNode; //to keep track of which node we've just added, and add nodes off of it
			//loop backwards through the rest of the string, adding nodes as we go
			for(int i=string.length()-2; i>=0; i--) {
				curToken = ((Character)string.charAt(i)).toString();
				//negate the last value if we find a -1
				if(curToken.equals("-")) {
					node.value = -1 * node.value;
					this.numBallsInHand += 2 * node.value;
					continue;
				}
				//add new node on the end with the next value
				HandStateNode newNode = new HandStateNode(Integer.parseInt(curToken), null);
				node.prevNode = newNode;
				node = newNode;
				length++;
				this.numBallsInHand += newNode.value;
			}
			//set lastNode to the last node we added
			this.lastNode = node;
		}

		private void increaseNowValueBy(int amount) {
			nowNode.value += amount;
		}

		private void increaseValueAtBeatBy(int beatsFromNow, int amount) {
			int b=0;
			HandStateNode node = nowNode;
			while(b < beatsFromNow) {
				if(node != null) {
					if(node.prevNode == null) {
						padWithOneZero();
					}
					node = node.prevNode;
				} else {
					padWithOneZero();
					node = lastNode;
				}
				b++;
			}
			node.increaseValueBy(amount);
		}

		private void incrementValue(int beatsFromNow) {
			alterValueByOne(beatsFromNow, true);
		}

		private void decrementValue(int beatsFromNow) {
			alterValueByOne(beatsFromNow, false);
		}

		private Integer valueAt(int beatsFromNow) {
			if(beatsFromNow >= length) {
				return 0;
			}
			HandStateNode node = nowNode;
			for(int i=0; i<beatsFromNow; i++) {
				node = node.prevNode;
			}
			return node.value;
		}

		private void alterValueByOne(int beatsFromNow, boolean increment) {
			int b=0;
			HandStateNode node = nowNode;
			while(b < beatsFromNow) {
				if(node != null) {
					if(node.prevNode == null) {
						padWithOneZero();
					}
					node = node.prevNode;
				} else {
					padWithOneZero();
					node = lastNode;
				}
				b++;
			}
			node.alterValueByOne(increment);
		}

		private void padWithOneZero() {
			HandStateNode newLastNode = new HandStateNode(0, null);
			lastNode.prevNode = newLastNode;
			lastNode = newLastNode;
			length++;
		}

		private void shiftForward() {
			HandStateNode newNowNode = new HandStateNode(null, nowNode);
			nowNode = newNowNode;
			length++;
		}

		private void shiftForwardNEW() {
			HandStateNode newNowNode = new HandStateNode(0, nowNode);
			nowNode = newNowNode;
			length++;
		}

		private void throwBall(int height, boolean isInfinite, int destHand, boolean isAntiToss) {
			//if it's a regular (non-anti) toss, decrement the current value (since the ball leaves the current hand)
			//otherwise, increment the current value (since the antiball leaves the current hand)
			//(this goes for finite and infinite tosses)
			alterValueByOne(0, isAntiToss);
		}

		private void advanceTime() {
			if(nowNode.prevNode != null) {
				nowNode = nowNode.prevNode;
				length--;
			} else {
				nowNode.value = 0;
				//don't need to decrement length since it shouldn't go below 1
			}
		}

		private void clipOffExtraNodes() {
			//find last nonzero node
			HandStateNode node = nowNode;
			HandStateNode lastNonZeroNode = nowNode;
			int numNodesToClip = 0;
			while(node != null) {
				if(node.value == null || node.value > 0) {
					lastNonZeroNode = node;
					numNodesToClip = 0;
				}
				node = node.prevNode;
				numNodesToClip++;
			}

			//clip off nodes previous to previously found node
			lastNode = lastNonZeroNode;
			lastNode.prevNode = null;
			length -= numNodesToClip - 1; //minus one because we aren't clipping nowNode
		}

		private HandState deepCopy() {
			HandState out = new HandState(nowNode.deepCopy(), length);
			//find lastNode of out
			HandStateNode node = out.nowNode;
			while(node.prevNode != null) {
				node = node.prevNode;
			}
			out.lastNode = node;
			return out;
		}

		public String toString() {
			String out = "";
			HandStateNode node = nowNode;
			while(node != null) {
				out = node.toString() + out;
				node = node.prevNode;
			}
			return out;
		}

		public boolean equalsUpTo(HandState otherHandState, int shift) {
			HandStateNode node1 = nowNode;
			HandStateNode node2 = otherHandState.nowNode;
			//skip the shifted nodes
			for(int i=0; i<shift; i++) {
				node1 = node1.prevNode;
				node2 = node2.prevNode;
			}
			//check to make sure all nodes up to this.length are equal to corresponding other nodes
			while(node1 != null) {
				//make sure node2 is also not null
				if(node2 == null) {
					return false;
				}
				//make sure their values are the same
				if(node1.value == null) {
					if(node2.value != null) {
						return false;
					}
				}
				if(!node1.value.equals(node2.value)) {
					return false;
				}
				//check the next pair of nodes
				node1 = node1.prevNode;
				node2 = node2.prevNode;
			}
			//check that any remaining nodes in otherHandState are zero-valued
			while(node2 != null) {
				if(node2.value != 0) {
					return false;
				}
				node2 = node2.prevNode;
			}
			return true;
		}

		public boolean equals(HandState otherHandState) {
			return equalsUpTo(otherHandState, 0);
		}

		private class HandStateNode {
			private HandStateNode prevNode;
			private Integer value;

			private HandStateNode(Integer value, HandStateNode prevNode) {
				this.value = value;
				this.prevNode = prevNode;
			}

			private void alterValueByOne(boolean increment) {
				if(value == null) {
					System.out.println("value attempted to be altered when null...");
					System.exit(1);
				}
				if(increment) {
					value++;
				} else {
					value--;
				}
			}

			private void increaseValueBy(int amount) {
				value += amount;
			}

			private HandStateNode deepCopy() {
				HandStateNode newPrevNode = null;
				if(prevNode != null) {
					newPrevNode = prevNode.deepCopy();
				}
				HandStateNode toReturn = new HandStateNode(new Integer(value), newPrevNode);
				return toReturn;
			} 

			public String toString() {
				if(value == null) {
					return " _ ";
				} else {
					if(value < 0) {
						return value.toString() + " ";
					} else {
						return " " + value.toString() + " ";
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		//testing
		State state;
		if(args.length == 1) {
			System.out.println("parsing siteswap string...");
			Siteswap ss = Parser.parse(args[0]);
			System.out.println("parsed siteswap string");
			state = new State(ss);
			System.out.println(state.toString());
			System.out.println(state.numBalls());
			state.throwBall(0, 1, true, 0, false);
			System.out.println(state.toString());
			System.out.println(state.numBalls());
		}
		if(args.length == 2) {
			State st1 = new State(args[1]);
			System.out.println(st1);
			System.out.println(st1.numBalls());
		}
	}
}

