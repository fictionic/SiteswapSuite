import java.util.List;
import java.util.LinkedList;

public class State {
	private List<HandState> hands;

	//FOR DEBUGGING
	public static final boolean debug = false;
	public static void printf(String input) {
		if(debug) {
			System.out.println(input);
		}
	}

	public State(Siteswap ss) {
		//initialize instance variable
		hands = new LinkedList<HandState>();
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
				while(hands.get(h).curValue() < ss.getBeat(b).getHand(h).numNonZeroTosses()) {
					hands.get(h).incrementCurValue();
					ballsAccountedFor++;
					printf("\t\t\taccounted for ball");
					printf("\t\t\tstate: " + hands);
				}
				//now throw all the balls in the hand!
				for(int t=0; t<ss.getBeat(b).getHand(h).numNonZeroTosses(); t++) {
					//eventually change Siteswap so we don't need to make instances of those inner classes outside Siteswap.java
					//or actually maybe not... it is pretty convenient this way
					Siteswap.Beat.Hand.Toss curToss = ss.getBeat(b).getHand(h).getToss(t);
					throwBall(h, curToss.destHand(), curToss.height());
					printf("\t\t\tthrew ball ");
					printf("\t\t\tstate: " + hands.toString());
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

	public int numHands() {
		return hands.size();
	}

	public void throwBall(int fromHand, int toHand, int toBeat) {
		printf("\t\t\tfromHand: " + fromHand + ", toHand: " + toHand + ", toBeat: " + toBeat);
		getHandState(fromHand).resetCurValue();
		getHandState(toHand).incrementValue(toBeat);
		hands.toString();
	}

	public void advanceTime() {
		for(HandState hs : hands) {
			hs.advanceTime();
		}
	}

	public int length() {
		int maxLength = 0;
		for(HandState h : hands) {
			if(h.length > maxLength) {
				maxLength = h.length;
			}
		}
		return maxLength;
	}

	public Integer getCurValueAtHand(int handIndex) {
		return hands.get(handIndex).curValue();
	}

	public Integer getValueAtHandAtBeat(int handIndex, int beatIndex) {
		return hands.get(handIndex).valueAt(beatIndex);
	}

	public void shiftForward() {
		for(HandState hs : hands) {
			hs.shiftForward();
		}
	}

	public HandState getHandState(int handIndex) {
		return hands.get(handIndex);
	}

	public boolean isAlignedWith(State state) {
		for(int h=0; h<numHands(); h++) {
			if(!getHandState(h).isAlignedWith(state.getHandState(h))) {
				return false;
			}
		}
		return true;
	}

	private void matchHandStateLengths() {
		//find the longest hand state
		int longest = 0;
		for(HandState h : hands) {
			if(h.length() > longest) {
				longest = h.length();
			}
		}

		//make all handstates that length
		for(HandState h : hands) {
			for(int i=h.length(); i<longest; i++) {
				h.padWithOneZero();
			}
		}
	}

	public void incrementValue(int handIndex, int beatsFromNow, boolean dontDecrement) {
		hands.get(handIndex).incrementValue(beatsFromNow, dontDecrement);
	}

	public static void matchLengths(State st1, State st2) {
		st1.matchHandStateLengths();
		st2.matchHandStateLengths();

		//see if we need to change anything
		if(st1.length() == st2.length()) {
			System.out.println("samelengths: " + st1.length());
			return;
		}
		//find which of the two is shorter
		State shorter = st1;
		State longer = st2;
		if(st1.length() > st2.length()) {
			shorter = st2;
			longer = st1;
		}
		while(shorter.length() < longer.length()) {
			shorter.padWithOneZero();
		}
	}

	protected void padWithOneZero() {
		for(HandState h : hands) {
			h.padWithOneZero();
		}
	}

	public String toString() {
		matchHandStateLengths();
		return hands.toString();
	}

	public boolean equals(State otherState) {
		for(int h=0; h<hands.size(); h++) {
			if(!getHandState(h).equals(otherState.getHandState(h))) {
				return false;
			}
		}
		return true;
	}

	private class HandState {
		private HandStateNode curNode;
		private HandStateNode lastNode;
		private int length;

		/*KEY:
		  <---         later    ---        sooner   	 ---      	  now]
		  lastNode  ...  curNode.prevNode.prevNode  curNode.prevNode  curNode

		 */

		private HandState() {
			this.curNode = new HandStateNode(null);
			this.lastNode = curNode;
			this.length = 1;
		}

		private Integer curValue() {
			return curNode.value();
		}

		public int length() {
			return length;
		}

		private Integer valueAt(int beatIndex) {
			int index=0;
			HandStateNode nodeAt = curNode;
			for(; index<beatIndex; index++) {
				nodeAt = nodeAt.prevNode();
			}
			return nodeAt.value();
		}
		
		private HandStateNode getNode(int beatIndex) {
			int index = 0;
			HandStateNode nodeAt = curNode;
			for(; index<beatIndex; index++) {
				nodeAt = nodeAt.prevNode();
			}
			return nodeAt;
		}

		private void incrementCurValue() {
			curNode.incrementValue();
		}

		private void resetCurValue() {
			curNode.resetValue();
		}

		private void incrementValue(int beatsFromNow, boolean dontDecrement) {
			//get node at which the value is to be inserted, making new empty filler nodes in between if necessary
			HandStateNode node = curNode;
			for(int i=0; i<beatsFromNow; i++) {
				node = node.getPrevNode();

			}
			//increment value of that node
			node.incrementValue(dontDecrement);
		}

		private void incrementValue(int beatsFromNow) {
			incrementValue(beatsFromNow, true);
		}

		private void shiftForward() {
			HandStateNode blank = new HandStateNode();
			curNode = blank;
			length++;
		}

		private void advanceTime() {
			curNode = curNode.prevNode();
			if(length>1) {
				length--;
			}
		}

		private boolean isAlignedWith(HandState otherState) {
			for(int b=0; b<length; b++) {
				if(otherState.valueAt(b) == null) {
					continue;
				} else if(valueAt(b) > otherState.valueAt(b)) {
					return false;
				}
			}
			return true;
		}

		private void padWithOneZero() {
			lastNode = lastNode.getPrevNode();
		}

		public String toString() {
			String out = "";
			HandStateNode n = curNode;
			while(n != null) {
				if(n.value() != null) {
					out = n.value().toString() + out;
				} else {
					out = "_" + out;
				}
				n = n.prevNode(); //not getPrevNode() b/c we don't want this to go forever!!
			}
			return out;
		}

		public boolean equals(HandState otherHandState) {
			HandStateNode curNodeThis = curNode;
			HandStateNode curNodeOther = otherHandState.getNode(0);
			int i=0;
			while(curNodeThis != null) {
				//check to see that the values are the same
				if(curNodeThis.value() != curNodeOther.value()) {
					return false;
				}
				curNodeThis = curNodeThis.prevNode();
				curNodeOther = curNodeOther.prevNode();
			}
			//now make sure curNodeOther is also null
			if(curNodeOther != null) {
				return false;
			}
			return true;
		}

		private class HandStateNode {
			private Integer value; //number of balls at this beat, in this hand
			private HandStateNode prevNode;

			private HandStateNode(HandStateNode prevNode) {
				this.value = 0;
				this.prevNode = prevNode;
			}
			
			//this is only used for shifting states when finding transitions
			//(it doesn't make sense in a normal context for value to be null)
			private HandStateNode() {
				this.value = null;
				this.prevNode = curNode;
			}

			private void incrementValue(boolean dontDecrement) {
				value++;
				if(!dontDecrement) {
					value -= 2;
				}
			}

			private void incrementValue() {
				incrementValue(true);
			}

			//adds an empty node if this.prevNode is null
			private HandStateNode getPrevNode() {
				if(prevNode == null) {
					HandStateNode newNode = new HandStateNode(null);
					prevNode = newNode;
					length++;
				}
				return prevNode;
			}

			private HandStateNode prevNode() {
				return prevNode;
			}

			private void setPrevNode(HandStateNode node) {
				this.prevNode = node;
			}

			private void resetValue() {
				value = 0;
			}

			private Integer value() {
				return value;
			}
		}
	}

	public static void main(String[] args) {
		//testing stuff for getTransition
		State st1 = new State(Parser.parse("3"));
		State st2 = new State(Parser.parse("51"));
		
		System.out.println("raw states:");
		System.out.println(st1);
		System.out.println(st2);

		System.out.println("lengths matched:");
		matchLengths(st1, st2);
		System.out.println(st1);
		System.out.println(st2);

		System.out.println("aligned:");
		while(!st1.isAlignedWith(st2)) {
			st2.shiftForward();
			st1.padWithOneZero();
		}
		System.out.println(st1);
		System.out.println(st2);
	}
}
