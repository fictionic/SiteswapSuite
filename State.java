package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class State {

	private static final boolean debug = true;
	private static void printf(Object toPrint) {
		if(debug) {
			if(toPrint == null)
				System.out.println("{null}");
			else
				System.out.println(toPrint);
		}
	}

	private int numHands; // number of hands
	private Node nowNode; // node that's next in line to be thrown from
	private int finiteLength; // number of nodes in the finite portion of the state
	private Node firstRepeatedNode; // earliest node in the repeated portion
	private int repeatedLength; // number of nodes in the repeated portion

	public int numHands() { return this.numHands; }
	public int finiteLength() { return this.finiteLength; }
	public boolean isFinite() { return this.repeatedLength == 0; }

	// initialize an empty state
	public State(int numHands) {
		this.numHands = numHands;
		this.nowNode = null;
		this.finiteLength = 0;
		this.firstRepeatedNode = null;
		this.repeatedLength = 0;
	}

	// construct a state from a siteswap...
	public State(Siteswap ss) {
		this(ss.numHands());
		ss = ss.deepCopy(); // don't change the object we're given
		ss.antitossify(); // life is simpler without negative tosses
		// printf(ss);
		if(ss.period() > 0) {
			// we construct a State that represents the state associated with the given siteswap.
			// but to do this we need a State object that keeps track of the current state we're
			// actually in as we juggle through the given pattern. (this will not contain the
			// repeated portion of the final state we end up with.)
			State simulationState = new State(this.numHands);
			// extend it to have length 1, so we can actually do tosses on it
			simulationState.getFiniteNode(0);
			// further, we sample this state at each period, and terminate the algorithm when there is
			// no change from one period to the next
			State simulationStateAtLastIteration = new State(this.numHands);
			// extend it to have length 1, so we can actually do tosses on it
			simulationStateAtLastIteration.getFiniteNode(0);
			// thisCurNode is the furthest node along the state being constructed ('this')
			Node thisCurNode = null;
			// the most recently added node (?)
			Node endOfLastSection = null;
			// whether no changes occurred in the last period
			boolean isAllZeros;

			// compute the finite portion of the state
			// --> simulate juggling the pattern until the state 
			//     doesn't change from one period to the next
			do {
				printf("");
				printf(" sim: " + simulationState.toString());
				printf("prev: " + simulationStateAtLastIteration.toString());
				printf("this: " + this.toString());
				simulationStateAtLastIteration = simulationState.deepCopy(); // sample the simulation state

				// assume this next series of nodes will be the repeated portion
				if(thisCurNode != null) {
					// merge previous section into finite portion, leaving an empty repeated portion to be filled in this iteration
					printf("merging repeated section into finite portion");
					this.finiteLength += ss.period();
					this.repeatedLength = 0;
					printf("this: " + this.toString());
				}
				endOfLastSection = thisCurNode;
				isAllZeros = true;
				for(int b=0; b<ss.period(); b++) {
					printf("b = " + b);
					// add a new Node for a new beat
					printf("\tadding new node");
					Node newNode = new Node();
					if(b == 0) {
						this.firstRepeatedNode = newNode;
					}
					if(thisCurNode == null) { // if this is the very first Node we're adding to the state
						this.nowNode = newNode;
						thisCurNode = newNode;
					} else {
						thisCurNode.prev = newNode;
						thisCurNode = newNode;
					}
					this.repeatedLength++;
					printf("\tthis: " + this.toString());
					for(int h=0; h<this.numHands; h++) {
						printf("\th = " + h);
						// set value to charge needed at this site
						int neededCharge = ss.outDegreeAtSite(b, h);
						int curCharge = simulationState.getFiniteNode(0).getChargeAtHand(h);
						if(curCharge != neededCharge) {
							printf("\t\taccounting for balls/antiballs");
							printf("\t\tneeded charge: " + neededCharge);
							printf("\t\tcurrent charge: " + curCharge);
							thisCurNode.setChargeAtHand(h, neededCharge - curCharge);
							isAllZeros = false;
						} else {
							printf("\t\tno need to account for balls/antiballs");
						}
						printf("\t\tthis: " + this.toString());
						// then simulate the tosses at this site on simulationState
						for(int t=0; t<ss.numTossesAtSite(b, h); t++) {
							Toss toss = ss.getToss(b, h, t);
							printf("\t\t\tsimulating toss: " + toss.toString());
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
							printf("\t\t\tsim: " + simulationState.toString());
						}
					}
					simulationState.advanceTime();
					printf("\t\tadvanced time");
					printf("\t\tsim: " + simulationState.toString());
				}
			} while(!simulationState.equals(simulationStateAtLastIteration));
			printf(" sim: " + simulationState.toString());
			printf("prev: " + simulationStateAtLastIteration.toString());
			printf("this: " + this.toString());
			if(isAllZeros) { // if there is no repeated portion in the final product
				printf("removing repeated portion");
				if(endOfLastSection != null) {
					endOfLastSection.prev = null;
				}
				this.firstRepeatedNode = null;
				this.repeatedLength = 0;
				printf("this: " + this.toString());
				printf("trimming extra zeroes in finite portion");
				// -- remove unnecessary zero nodes --
				// first skip any zero nodes at the start
				Node cur = this.nowNode;
				this.finiteLength = 0; // re-compute this during the process
				while(cur != null && cur.isEmpty()) {
					this.finiteLength++;
					cur = cur.prev;
				}
				// then find the last nonzero node
				Node lastNonZero = cur;
				int lengthOfNewSection = 0;
				while(cur != null) {
					lengthOfNewSection++;
					if(!cur.isEmpty()) {
						lastNonZero = cur;
						this.finiteLength += lengthOfNewSection;
						lengthOfNewSection = 0;
					}
					cur = cur.prev;
				}
				// and make it the last node
				if(lastNonZero != null) {
					lastNonZero.prev = null;
				}
				printf("this: " + this.toString());
			} else { // if there is one
				this.repeatedLength = ss.period();
			}
			printf("\n");
		}
	}

	Node getFiniteNode(int beatIndex) {
		if(this.finiteLength == 0) {
			this.nowNode = new Node();
			this.finiteLength = 1;
		}
		Node curNode = this.nowNode;
		for(int i=0; i<beatIndex; i++) {
			if(curNode.prev == null) {
				Node newLastFiniteNode = new Node();
				curNode.prev = newLastFiniteNode;
				this.finiteLength++;
			}
			curNode = curNode.prev;
		}
		return curNode;
	}

	void incChargeOfNodeAtHand(int b, int h) {
		this.getFiniteNode(b).incChargeAtHand(h);
	}

	void decChargeOfNodeAtHand(int b, int h) {
		this.getFiniteNode(b).decChargeAtHand(h);
	}

	void incChargeOfNowNodeAtHand(int h) {
		this.nowNode.incChargeAtHand(h);
	}

	void decChargeOfNowNodeAtHand(int h) {
		this.nowNode.decChargeAtHand(h);
	}

	void advanceTime() {
		if(this.nowNode != null) {
			this.nowNode = this.nowNode.prev;
			this.finiteLength--;
		}
	}

	void shiftBackward() {
		Node newNode = new Node();
		newNode.prev = this.nowNode;
		this.nowNode = newNode;
		this.finiteLength++;
	}

	public boolean nowNodeIsEmpty() {
		return this.nowNode.isEmpty();
	}

	public int getChargeAtBeatAtHand(int b, int h) {
		return this.getFiniteNode(b).getChargeAtHand(h);
	}

	public boolean equals(State other) {
		if(other == null)
			return false;
		if(this.numHands != other.numHands)
			return false;
		Node thisCurNode = this.nowNode;
		Node otherCurNode = other.nowNode;
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

	class DiffSum {
		int tosses, antiTosses, catches, antiCatches;
		public String toString() {
			String ret = "";
			ret += "tossP: " + tosses;
			ret += "\ntossN: " + antiTosses;
			ret += "\ncatcP: " + catches;
			ret += "\ncatcN: " + antiCatches;
			return ret;
		}
	}

	DiffSum diffSums(State other) {
		Node thisCurNode = this.nowNode;
		Node otherCurNode = other.nowNode;
		DiffSum diffs = new DiffSum();
		for(int i=0; i<this.finiteLength; i++) {
			for(int h=0; h<this.numHands; h++) {
				int t = thisCurNode.getChargeAtHand(h);
				int o = otherCurNode.getChargeAtHand(h);
				if(o < t) {
					if(t <= 0) {
						// o < t <= 0
						diffs.antiCatches += t - o;
						continue;
					}
					if(o >= 0) {
						// 0 <= o < t
						diffs.tosses += t - o;
						continue;
					}
					// o <= 0 <= t
					diffs.tosses += t;
					diffs.antiCatches -= o;
				} else if(t < o) {
					if(o <= 0) {
						// t < o <= 0
						diffs.antiTosses += o - t;
						continue;
					}
					if(t >= 0) {
						// 0 <= o < t
						diffs.catches += o - t;
						continue;
					}
					// t <= 0 <= o
					diffs.antiTosses += o;
					diffs.catches -= t;
				}
			}
			thisCurNode = thisCurNode.prev;
			otherCurNode = otherCurNode.prev;
		}
		return diffs;
	}

	public ExtendedFraction numBalls() {
		if(this.finiteLength + this.repeatedLength == 0) {
			return new ExtendedFraction(new ExtendedInteger(0), 0);
		}
		int finitePortion = 0;
		Node curNode = this.nowNode;
		for(int i=0; i<this.finiteLength; i++) {
			finitePortion += curNode.getTotalCharge();
			curNode = curNode.prev;
		}
		int repeatedPortion = 0;
		int signCounter = 0;
		if(this.repeatedLength > 0) {
			for(int i=0; i<this.repeatedLength; i++) {
				signCounter += curNode.getTotalCharge();
				repeatedPortion += signCounter;
				curNode = curNode.prev;
			}
			ExtendedInteger numerator;
			int denominator;
			if(signCounter == 0) {
				numerator = new ExtendedInteger(finitePortion * this.repeatedLength + repeatedPortion);
				denominator = this.repeatedLength;
			} else if(signCounter < 0) {
				numerator = new ExtendedInteger(InfinityType.NEGATIVE_INFINITY);
				denominator = 1;
			} else {
				numerator = new ExtendedInteger(InfinityType.POSITIVE_INFINITY);
				denominator = 1;
			}
			return new ExtendedFraction(numerator, denominator);
		} else
			return new ExtendedFraction(new ExtendedInteger(finitePortion), 1);
	}

	public State deepCopy() {
		State out = new State(this.numHands);
		out.finiteLength = this.finiteLength;
		out.repeatedLength = this.repeatedLength;
		Node thisCurNode = this.nowNode;
		Node otherCurNode;
		if(this.nowNode != null) {
			thisCurNode = this.nowNode;
			out.nowNode = this.nowNode.deepCopy();
			otherCurNode = out.nowNode;
		} else
			return out;
		for(int i=1; i<this.finiteLength; i++) {
			thisCurNode = thisCurNode.prev;
			Node newNode = thisCurNode.deepCopy();
			otherCurNode.prev = newNode;
			otherCurNode = newNode;
		}
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
		return out;
	}

	public String toString() {
		String out = "[";
		Node n = this.nowNode;
		for(int i=0; i<this.finiteLength; i++) {
			out += n.toString();
			n = n.prev;
		}
		if(this.repeatedLength > 0) {
			out += ":";
			for(int i=0; i<this.repeatedLength; i++) {
				out += n.toString();
				n = n.prev;
			}
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
		private int getTotalCharge() {
			int charge = 0;
			for(int i=0; i<numHands; i++)
				charge += this.handCharges.get(i).value;
			return charge;
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
			//public String toString() { return Integer.toString(this.value); }
			public String toString() { 
				return "" + this.value;
			}
		}
	}

}
