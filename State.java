package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class State {

	private static void printf(Object toPrint) {
		if(toPrint == null)
			System.out.println("{null}");
		else
			System.out.println(toPrint);
	}

	private int numHands;
	private Node firstFiniteNode;
	private Node lastFiniteNode;
	private int finiteLength;
	private Node firstRepeatedNode;
	private Node lastRepeatedNode;
	private int repeatedLength;

	// initialize an empty state
	public State(int numHands) {
		this.numHands = numHands;
		this.firstFiniteNode = null;
		this.finiteLength = 0;
		this.firstRepeatedNode = null;
		this.lastRepeatedNode = null;
		this.repeatedLength = 0;
	}

	// construct a state from a siteswap...
	public State(Siteswap ss) {
		this(ss.numHands());

		if(ss.period() > 0) {
			// we construct a State that represents the state associated with the given ss
			// but to do this we need a State object that keeps track of the current state we're
			// actually in as we juggle through the given pattern.
			State simulationState = new State(this.numHands);
			State simulationStateAtLastIteration = new State(this.numHands);
			Node thisCurNode = null;
			printf(" sim: " + simulationState.toString());
			printf("this: " + this.toString());
			// first extend simulationState to have length 1, so we can actually do tosses on it
			simulationState.extendToBeat(0);

			// compute the finite portion of the state
			// --> simulate juggling the pattern until
			//     the state doesn't change from one
			//     period to the next
			do {
				printf("\n");
				printf(" sim: " + simulationState.toString());
				printf("prev: " + simulationStateAtLastIteration.toString());
				simulationStateAtLastIteration = simulationState.deepCopy();
				// assume this next series of nodes will be the repeated portion
				if(thisCurNode != null)
					this.lastFiniteNode = thisCurNode;
				for(int b=0; b<ss.period(); b++) {
					printf("b = " + b);
					// add a new Node for a new beat
					Node newNode = new Node();
					if(thisCurNode == null) {
						this.firstFiniteNode = newNode;
						thisCurNode = newNode;
					} else {
						thisCurNode.prev = newNode;
					}
					thisCurNode = newNode;
					this.finiteLength++;
					for(int h=0; h<this.numHands; h++) {
						// set value to charge needed at this site
						int neededCharge = ss.outDegreeAtSite(b, h);
						int curCharge = simulationState.getFiniteNode(0).getChargeAtHand(h);
						if(curCharge != neededCharge) {
							printf("accounting for balls/antiballs");
							printf("needed charge: " + neededCharge);
							printf("current charge: " + curCharge);
						} else
							printf("no need to account for balls/antiballs");
						thisCurNode.setChargeAtHand(h, neededCharge - curCharge);
						printf("this: " + this.toString());
						// then simulate the tosses at this site
						for(int t=0; t<ss.numTossesAtSite(b, h); t++) {
							Toss toss = ss.getToss(b, h, t);
							printf("simulating toss: " + toss.toString());
							ExtendedInteger height = toss.height();
							if(!height.isInfinite()) {
								switch(toss.charge()) {
									case 1:
										simulationState.getFiniteNode(height.finiteValue()).incChargeAtHand(toss.destHand());
										break;
									case -1:
										simulationState.getFiniteNode(height.finiteValue()).decChargeAtHand(toss.destHand());
										break;
									default:
										break;
								}
							} // we don't care about infinite tosses, cuz they don't affect the rest of the state
							printf(" sim: " + simulationState.toString());
						}
						simulationState.advanceTime();
						printf("advanced time");
						printf(" sim: " + simulationState.toString());
					}
				}
				// set lastRepeatedNode of this
				if(this.lastFiniteNode != null) {
					this.firstRepeatedNode = this.lastFiniteNode.prev;
					this.lastRepeatedNode = thisCurNode;
				}
			} while(!simulationState.equals(simulationStateAtLastIteration));
			printf(" sim: " + simulationState.toString());
			printf("prev: " + simulationStateAtLastIteration.toString());
			printf("\n");
		}
	}

	Node getFiniteNode(int beatsFromNow) {
		this.extendToBeat(beatsFromNow);
		Node n = this.firstFiniteNode;
		for(int b=0; b<beatsFromNow; b++) {
			n = n.prev;
		}
		return n;
	}

	void extendToBeat(int beatIndex) {
		if(this.finiteLength == 0) {
			Node newOnlyFiniteNode = new Node();
			newOnlyFiniteNode.prev = this.firstRepeatedNode;
			this.firstFiniteNode = newOnlyFiniteNode;
			this.lastFiniteNode = newOnlyFiniteNode;
			this.finiteLength = 1;
		}
		while(beatIndex >= this.finiteLength) {
			Node newLastFiniteNode = new Node();
			newLastFiniteNode.prev = this.firstRepeatedNode;
			this.lastFiniteNode.prev = newLastFiniteNode;
			this.lastFiniteNode = newLastFiniteNode;
			this.finiteLength++;
		}
	}

	void extendToRepeatedBeat(int beatIndex) {
		while(beatIndex >= this.repeatedLength) {
			Node newLastRepeatedNode = new Node();
			newLastRepeatedNode.prev = this.firstRepeatedNode;
			this.lastRepeatedNode.prev = newLastRepeatedNode;
			this.lastRepeatedNode = newLastRepeatedNode;
		}
	}

	private void advanceTime() {
		if(this.firstFiniteNode != null) {
			this.firstFiniteNode = this.firstFiniteNode.prev;
			this.finiteLength--;
		}
	}

	public boolean equals(State other) {
		if(other == null)
			return false;
		if(this.numHands != other.numHands)
			return false;
		Node thisCurNode = this.firstFiniteNode;
		Node otherCurNode = other.firstFiniteNode;
		while(thisCurNode != null || otherCurNode != null) {
			if(thisCurNode != null && otherCurNode != null) {
				if(!thisCurNode.equals(otherCurNode))
					return false;
				else {
					thisCurNode = thisCurNode.prev;
					otherCurNode = otherCurNode.prev;
				}
			} else if(thisCurNode == null) {
				if(!otherCurNode.isEmpty())
					return false;
				else
					otherCurNode = otherCurNode.prev;
			} else if(otherCurNode == null) {
				if(!thisCurNode.isEmpty())
					return false;
				else
					thisCurNode = thisCurNode.prev;
			}
		}
		return true;
	}

	public State deepCopy() {
		State out = new State(this.numHands);
		Node thisCurNode = this.firstFiniteNode;
		Node otherCurNode;
		if(this.firstFiniteNode != null) {
			thisCurNode = this.firstFiniteNode;
			out.firstFiniteNode = this.firstFiniteNode.deepCopy();
			otherCurNode = out.firstFiniteNode;
		} else
			return out;
		for(int i=1; i<this.finiteLength; i++) {
			thisCurNode = thisCurNode.prev;
			Node newNode = thisCurNode.deepCopy();
			otherCurNode.prev = newNode;
			otherCurNode = newNode;
		}
		out.lastFiniteNode = otherCurNode;
		if(this.firstRepeatedNode != null) {
			thisCurNode = this.firstRepeatedNode;
			out.firstRepeatedNode = this.firstRepeatedNode.deepCopy();
			otherCurNode = out.firstRepeatedNode;
		} else
			return out;
		for(int i=1; i<this.repeatedLength; i++) {
			thisCurNode = thisCurNode.prev;
			Node newNode = thisCurNode.deepCopy();
			otherCurNode.prev = newNode;
			otherCurNode = newNode;
		}
		out.lastRepeatedNode = otherCurNode;
		return out;
	}

	public String toString() {
		String out = "[";
		Node n = this.firstFiniteNode;
		while(n != null) {
			out += n.toString();
			n = n.prev;
			if(n == this.firstRepeatedNode)
				out += ":";
		}
		out += "]";
		return out;
	}

	private class Node {
		private List<Charge> handCharges;
		Node prev;
		private Node() {
			this.handCharges = new ArrayList<Charge>();
			this.prev = null;
			for(int h=0; h<numHands; h++) {
				this.handCharges.add(new Charge());
			}
		}
		private int getChargeAtHand(int handIndex) {
			return this.handCharges.get(handIndex).value;
		}
		private void setChargeAtHand(int handIndex, int newCharge) {
			this.handCharges.get(handIndex).set(newCharge);
		}
		private void incChargeAtHand(int handIndex) {
			this.handCharges.get(handIndex).inc();
		}
		private void decChargeAtHand(int handIndex) {
			this.handCharges.get(handIndex).dec();
		}
		private boolean isEmpty() {
			for(int h=0; h<numHands; h++) {
				if(this.handCharges.get(h).value != 0)
					return false;
			}
			return true;
		}
		public String toString() {
			return this.handCharges.toString();
		}
		public boolean equals(Node other) {
			for(int h=0; h<numHands; h++) {
				if(this.handCharges.get(h).value != other.handCharges.get(h).value)
					return false;
			}
			return true;
		}
		private Node(Node toCopy) {
			this.handCharges = new ArrayList<Charge>();
			for(int h=0; h<numHands; h++) {
				this.handCharges.add(new Charge(toCopy.handCharges.get(h).value));
			}
			this.prev = null;
		}
		private Node deepCopy() {
			return new Node(this);
		}
		private class Charge {
			private int value;
			private Charge() { this.value = 0; }
			private Charge(int newValue) { this.value = newValue; }
			private void set(int newValue) { this.value = newValue; }
			private void inc() { this.value++; }
			private void dec() { this.value--; }
			public String toString() { return Integer.toString(this.value); }
		}
	}

	public static void main(String[] args) {
		if(args.length == 1) {
			try {
				Siteswap ss = NotatedSiteswap.parse(args[0]);
				try {
				State s = new State(ss);
				printf(s);
				} catch(NullPointerException e) {
					e.printStackTrace();
				}
			} catch(InvalidNotationException e) {
				printf("invalid notation");
			}
		} else if(args.length == 0) {
			State s1 = new State(1);
			State s2 = new State(1);
			s1.extendToBeat(1);
			s2.getFiniteNode(3).incChargeAtHand(0);
			printf(s1);
			printf(s2);
			printf(s1.equals(s2));
			printf(s2.deepCopy());
		}
	}
}
