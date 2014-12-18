import java.util.List;
import java.util.ArrayList;

public class State {
	private List<HandState> hands;

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
		//create an empty HandState for each hand in the pattern
		for(int h=0; h<ss.numHands(); h++) {
			hands.add(new HandState());
		}
		//emulate juggling the pattern, adding balls to hands at beats when needed
		int ballsAccountedFor = 0;
		int totalBeats = 0;
		int b = 0;
		do {
			printf("ballsAccountedFor: " + ballsAccountedFor);
			printf("\tcurBeat: " + b);
			//loop through hands
			for(int h=0; h<ss.numHands(); h++) {
				printf("\t\tcurHand: " + h);
				//see if there are more (nonzero) throws in this beat in this hand than we currently have balls for; add balls if so
				while(hands.get(h).valueAt(0) < ss.getBeat(b).getHand(h).numNonZeroTosses()) {
					incrementValue(h, 0);
					ballsAccountedFor++;
					printf("\t\t\taccounted for ball");
					printf("\t\t\tstate: " + hands);
				}
				//now throw all the balls in the hand!
				for(int t=0; t<ss.getBeat(b).getHand(h).numNonZeroTosses(); t++) {
					//eventually change Siteswap so we don't need to make instances of those inner classes outside Siteswap.java
					//or actually maybe not... it is pretty convenient this way
					Siteswap.Beat.Hand.Toss curToss = ss.getBeat(b).getHand(h).getToss(t);
					throwBall(h, curToss.height(), curToss.destHand());
					printf("\t\t\tthrew ball ");
					printf("\t\t\tstate: " + hands);
				}
			}
			//advance time
			advanceTime();
			printf("\t\tadvanced time");
			printf("\t\tstate: " + hands);
			totalBeats++;
			b = totalBeats % ss.period();
		} while(b != 0 || ballsAccountedFor < ss.numBalls());
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

	public void incrementValue(int handIndex, int beatsFromNow) {
		try {
			hands.get(handIndex).incrementValue(beatsFromNow);
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("handIndex cannot be greater than numHands");
		}
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

	public void throwBall(int fromHand, int height, int toHand) {
		/*if(fromHand >= hands.size() || toHand >= hands.size()) {
			System.out.println("hand index out of bounds in throwBall");
			System.exit(1);
		}*/
		hands.get(fromHand).throwBall(height, toHand);
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

		private void throwBall(int height, int destHand) {
			//decrement the current value (since the ball leaves the current hand)
			decrementValue(0);
			//increment the value where the ball gets thrown to
			hands.get(destHand).incrementValue(height);
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
					if(value < 0) {
						System.out.println("value was decremented below 0 somehow...");
						System.exit(1);
					}
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
