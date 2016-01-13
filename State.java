package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class State {

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
		this.lastFiniteNode = this.firstFiniteNode;
		this.finiteLength = 0;
		this.firstRepeatedNode = null;
		this.lastRepeatedNode = this.firstRepeatedNode;
		this.repeatedLength = 0;
	}

	// construct a state from a siteswap...
	public State(Siteswap ss) {
		this(ss.numHands());
		// we construct a State that represents the state associated with the given ss
		// but to do this we need a State object that keeps track of the current state we're
		// actually in as we juggle through the given pattern.
		State trackerState = new State(this.numHands);
		// we probably need to create some nodes to base things off of
		if(ss.period() > 0) {
			this.firstFiniteNode = new Node(this.firstRepeatedNode);
			//(we'll set lastRepeatedNode later, rather than continually updating it)
			trackerState.extendToBeat(0);
		}
		Node thisCurNode = this.firstFiniteNode;
		// juggle through the pattern!
		for(int b=0; b<ss.period(); b++) {
			// add a new Node for a new beat
			Node newNode = new Node(null);
			thisCurNode.prev = newNode;
			thisCurNode = newNode;
			this.finiteLength++;
			for(int h=0; h<this.numHands; h++) {
				// set value to charge needed at this site
				//int chargeDiff = ss.outDegreeAtSite(b, h) - trackerState.getFiniteNode(0).getChargeAtHand(h);
				trackerState.getFiniteNode(0).getChargeAtHand(h);
				thisCurNode.setChargeAtHand(h, 0);
				// then alter curState
				for(int t=0; t<ss.numTossesAtSite(b, h); t++) {
					Toss toss = ss.getToss(b, h, t);
					ExtendedInteger height = toss.height();
					if(!height.isInfinite()) {
						switch(toss.charge()) {
							case 1:
								trackerState.getFiniteNode(height.finiteValue()).incChargeAtHand(toss.destHand());
								break;
							case -1:
								trackerState.getFiniteNode(height.finiteValue()).decChargeAtHand(toss.destHand());
								break;
							default:
								break;
						}
					} // we don't care about infinite tosses, cuz they don't affect the rest of the state
				}
			}
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
			Node newOnlyFiniteNode = new Node(this.firstRepeatedNode);
			this.firstFiniteNode = newOnlyFiniteNode;
			this.lastFiniteNode = newOnlyFiniteNode;
			this.finiteLength = 1;
		}
		while(beatIndex >= this.finiteLength) {
			Node newLastFiniteNode = new Node(this.firstRepeatedNode);
			this.lastFiniteNode.prev = newLastFiniteNode;
			this.lastFiniteNode = newLastFiniteNode;
			this.finiteLength++;
		}
	}

	void extendToRepeatedBeat(int beatIndex) {
		while(beatIndex >= this.repeatedLength) {
			Node newLastRepeatedNode = new Node(this.firstRepeatedNode);
			this.lastRepeatedNode.prev = newLastRepeatedNode;
			this.lastRepeatedNode = newLastRepeatedNode;
		}
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
		private Node(Node prev) {
			this.handCharges = new ArrayList<Charge>();
			this.prev = prev;
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
		public String toString() {
			return this.handCharges.toString();
		}
		private class Charge {
			private int value;
			private Charge() { this.value = 0; }
			private void set(int newValue) { this.value = newValue; }
			private void inc() { this.value++; }
			private void dec() { this.value--; }
			public String toString() { return Integer.toString(this.value); }
		}
	}

	public static void main(String[] args) {
		try {
			Siteswap ss = NotatedSiteswap.parse(args[0]);
			State s = new State(ss);
			System.out.println(s);
		} catch(InvalidNotationException e) {
			System.out.println("invalid notation");
		}
	}

}
