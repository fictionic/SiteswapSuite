import java.util.List;
import java.util.ArrayList;

public class State {
	private List<HandState> hands;
	private int numBalls;

	//FOR DEBUGGING
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
		hands = new ArrayList<HandState>();
		numBalls = 0;
		int goalNumBalls = ((Double)ss.numBalls()).intValue();
		//create an empty HandState for each hand in the pattern
		for(int h=0; h<ss.numHands(); h++) {
			hands.add(new HandState());
		}
		//emulate juggling the pattern, adding balls to hands at beats when needed
		//turn all of ss's negative tosses into antitosses
		ss.antiTossify();
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
							if(getValueAtHand(h) == 0) {
								if(curToss.isAntiToss()) {
									decrementValue(h);
									numBalls--;
									printf("\t\t\taccounted for antiball");
									printf("\t\t\tstate: " + hands);
								} else {
									incrementValue(h);
									numBalls++;
									printf("\t\t\taccounted for ball");
									printf("\t\t\tstate: " + hands);
								}
							} else {
								//see if state was opposite sign of curToss
								//(don't know if this is possible for valid siteswaps)
								if(curToss.isAntiToss()) {
									if(getValueAtHand(h) > 0) {
										System.out.println("state has opposite sign as curToss...");
										System.exit(1);
									}
								} else {
									if(getValueAtHand(h) < 0) {
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
									numBalls++;

								} else {
									numBalls--;
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
							if(getValueAtHand(h) != 0) {
								System.out.println("encountered zero-toss when state was nonzero...");
								System.exit(1);
							}
						}
					}
					if(getValueAtHand(h) != 0) {
						System.out.println("didn't get rid of all balls in hand " + h + " at beat " + b);
						System.exit(1);
					}
				}
				//advance time
				advanceTime();
				printf("\tadvanced time");
				printf("\tstate: " + hands);
			}
			printf("balls accounted for: " + numBalls);
			printf("ss.numBalls(): " + goalNumBalls);
			debugCounter++;
			if(debugCounter > 2) {
				System.exit(1);
			}
		} while(numBalls != goalNumBalls);
	}

	//constructor only for testing
	private State(int numHands) {
		this.hands = new ArrayList<HandState>();
		for(int i=0; i<numHands; i++) {
			hands.add(new HandState());	
		}
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

	public Integer getValueAtHand(int handIndex) {
		return hands.get(handIndex).nowNode.value;
	}

	public Integer getValueAtHandAtBeat(int handIndex, int beatsFromNow) {
		return hands.get(handIndex).valueAt(beatsFromNow);
	}

	public boolean isAlignedWith(State otherState) {
		//check that the two have the same length
		if(length() != otherState.length()) {
			return false;
		}

		HandState.HandStateNode node;
		HandState.HandStateNode node2;
		//loop through handStates
		for(int h=0; h<hands.size(); h++) {
			node = hands.get(h).nowNode;
			node2 = otherState.hands.get(h).nowNode;
			//loop through beats
			for(int b=0; b<length(); b++) {
				//check that the values at this position are aligned
				if(node2.value != null && node.value > node2.value) {
					//then they aren't aligned
					return false;
				} else {
					//then they are aligned
					node = node.prevNode;
					node2 = node2.prevNode;
				}
			}
		}
		return true;
	}

	public void shiftForward() {
		for(HandState h : hands) {
			h.shiftForward();
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
	}

	//protected cuz it's used by TransitionFinder.java
	protected void advanceTime() {
		for(HandState h : hands) {
			h.advanceTime();
		}
	}

	private void clipOffExtraNodes() {
		for(HandState h : hands) {
			h.clipOffExtraNodes();
		}
	}

	public String toString() {
		//clipOffExtraNodes(); need to put this somewhere better!
		matchHandStateLengths();
		return hands.toString();
	}

	public boolean equals(State otherState) {
		//ignores zero beats at the end
		//check that they have the same number of hands
		if(hands.size() != otherState.hands.size()) {
			return false;
		}
		for(int h=0; h<hands.size(); h++) {
			if(!hands.get(h).equals(otherState.hands.get(h))) {
				return false;
			}
		}
		return true;
	}

	private class HandState {
		private HandStateNode nowNode;
		private HandStateNode lastNode;
		private int length;

		private HandState() {
			this.nowNode = new HandStateNode(0, null);
			this.lastNode = nowNode;
			this.length = 1;
		}

		private void increaseNowValueBy(int amount) {
			nowNode.value += amount;
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

		private void throwBall(int height, boolean isInfinite, int destHand, boolean isAntiToss) {
			//if it's a regular (non-anti) toss, decrement the current value (since the ball leaves the current hand)
			//otherwise, increment the current value (since the antiball leaves the current hand)
			//(this goes for finite and infinite tosses)
			alterValueByOne(0, isAntiToss);
			//then put the ball/antiball at its destination height, but only if it isn't infinite
			if(!isInfinite) {
				//alter the value where the ball gets thrown to by one, again depending on whether or not it's a ball or an antiball
				//(ball --> increment; antiball --> decrement)
				hands.get(destHand).alterValueByOne(height, !isAntiToss);
			}
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

		public String toString() {
			String out = "";
			HandStateNode node = nowNode;
			while(node != null) {
				out = node.toString() + out;
				node = node.prevNode;
			}
			return out;
		}

		public boolean equals(HandState otherHandState) {
			HandStateNode node1 = nowNode;
			HandStateNode node2 = otherHandState.nowNode;
			//check to make sure all nodes up to this.length are equal to corresponding other nodes
			while(node1 != null) {
				//make sure node2 is also not null
				if(node2 == null) {
					return false;
				}
				//make sure their values are the same
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

			public String toString() {
				if(value == null) {
					return "_";
				} else {
					return value.toString();
				}
			}
		}
	}

	public static void main(String[] args) {
		//testing
		State state;
		if(args.length == 1) {
			state = new State(Parser.parse(args[0], true));
			System.out.println(state);
		}

		/*
		   State state1 = new State(Parser.parse("3"));
		   State state2 = new State(Parser.parse("441"));
		   debug = true;
		   printf("state1: " + state1);
		   printf("state2: " + state2);
		   printf("equals: " + state1.equals(state2));
		   debug = false;
		   state1 = new State(Parser.parse("51"));
		   state2 = new State(Parser.parse("50505"));
		   debug = true;
		   printf("state1: " + state1);
		   printf("state2: " + state2);
		   printf("equals: " + state1.equals(state2));
		//System.exit(0);


		//incrementing values
		state = new State(2);
		printf("created blank state w/ 2 hands:");
		printf(state);
		state.incrementValue(0, 0);
		printf("incremented first value in hand 0:");
		printf(state);
		state.incrementValue(1, 3);
		printf("incremented fourth value in hand 1:");
		printf(state);
		state.incrementValue(0, 2);
		printf("incremented third value in hand 0:");
		printf(state);

		//making tosses
		state.throwBall(0, 1, 1);
		printf("threw ball from hand 0 to hand 1 with height 1");
		printf(state);
		state.advanceTime();
		printf("advanced time");
		printf(state);
		state.throwBall(1, 1, 1);
		printf("threw ball from hand 1 to hand 1 with height 1");
		printf(state);
		state.advanceTime();
		printf("advanced time");
		printf(state);
		state.throwBall(1, 5, 0);
		printf("threw ball from hand 1 to hand 0 with height 5");
		printf(state);
		state.advanceTime();
		printf("advanced time");
		printf(state);

		//clipping off zero-valued nodes
		state.decrementValue(0, 4);
		printf("decremented value in hand 0 at beat 4");
		printf(state);
		 */
	}
}
