import java.util.List;
import java.util.ArrayList;

public class State {

    private List<HandState> hands;

    // basic constructor: make an empty state
    public State(int numHands) {
	this.hands = new ArrayList<HandState>();
	for(int h=0; h<numHands; h++) {
	    this.hands.add(new HandState());
	}
    }

    // querying basic info
    // ------------
    public int numHands() {
	return this.hands.size();
    }

    public int finiteLengthAtHand(int handIndex) {
	return this.hands.get(handIndex).finiteLength();
    }

    public int repeatedLengthAtHand(int handIndex) {
	return this.hands.get(handIndex).repeatedLength();
    }

    // computing more complicated info
    // ------------
    public ExtendedFraction numBalls() {
	// ...
	return null;
    }

    // getting values
    // ------------
    public int getNowValueAtHand(int handIndex) {
	return this.hands.get(handIndex).getNowValue();
    }

    public int getValueAtHandAtBeat(int handIndex, int beatIndex) {
	return this.hands.get(handIndex).getValueAtBeat(beatIndex);
    }

    // extending HandStates
    // ------------
    public void extendHandStateToBeat(int handIndex, int beatIndex) {
	this.hands.get(handIndex).extendToBeat(beatIndex);
    }

    public void extendHandStateToRepeatedBeat(int handIndex, int beatIndex) {
	this.hands.get(handIndex).extendRepeatedPortionToBeat(beatIndex);
    }

    // altering values (making throws/catches)
    // ------------
    public void incrementNowValueAtHand(int handIndex) {
	incrementValueAtHandAtBeat(handIndex, 0);
    }

    public void decrementNowValueAtHand(int handIndex) {
	decrementValueAtHandAtBeat(handIndex, 0);
    }

    public void incrementValueAtHandAtBeat(int handIndex, int beatIndex) {
	this.hands.get(handIndex).incrementValueAtBeat(beatIndex);
    }

    public void decrementValueAtHandAtBeat(int handIndex, int beatIndex) {
	this.hands.get(handIndex).decrementValueAtBeat(beatIndex);
    }

    public void throwBallWithFinitePositiveHeight(int fromHand, int height, int destHand) {
	this.hands.get(fromHand).decrementNowValue();
	this.hands.get(destHand).incrementValueAtBeat(height);
    }

    public void throwAntiballWithFinitePositiveHeight(int fromHand, int height, int destHand) {
	this.hands.get(fromHand).incrementNowValue();
	this.hands.get(destHand).decrementValueAtBeat(height);
    }

    // other value-altering methods (which don't represent possible juggling phenomena)
    // ------------
    public void incrementValueAtHandAtRepeatedBeat(int handIndex, int beatIndex) {
	this.hands.get(handIndex).incrementValueAtRepeatedBeat(beatIndex);
    }

    public void decrementValueAtHandAtRepeatedBeat(int handIndex, int beatIndex) {
	this.hands.get(handIndex).decrementValueAtRepeatedBeat(beatIndex);
    }

    // for use in getTransition
    // ------------
    public void shiftBackward() {
	for(int h=0; h<this.numHands(); h++) {
	    this.hands.get(h).shiftBackward();
	}
    }

    public void advanceTime() {
	for(int h=0; h<this.numHands(); h++) {
	    this.hands.get(h).advanceTime();
	}
    }

    // misc
    // ------------
    public State deepCopy() {
	List<HandState> newHands = new ArrayList<HandState>();
	for(int h=0; h<this.numHands(); h++) {
	    newHands.add(this.hands.get(h).deepCopy());
	}
	return new State(newHands);
    }

    // for deepCopy
    public State(List<HandState> newHands) {
	this.hands = newHands;
    }

    public String toString() {
	return this.hands.toString();
    }

    private class HandState {

	private Node nowNode;
	private Node firstFiniteNode;
	private Node lastFiniteNode;
	private Node firstRepeatedNode;
	private Node lastRepeatedNode;
	private int finiteLength;
	private int repeatedLength;

	private HandState() {
	    // begins as [ :  0 ] (infinite repeating 0s)
	    this.firstFiniteNode = null;
	    this.lastFiniteNode = null;
	    this.finiteLength = 0;
	    this.firstRepeatedNode = new Node();
	    this.lastRepeatedNode = this.firstRepeatedNode;
	    this.lastRepeatedNode.prev = this.firstRepeatedNode; // set up the loop
	    this.repeatedLength = 1;
	    this.nowNode = this.firstRepeatedNode;
	}

	// querying basic info
	private int length() {
	    return this.finiteLength + this.repeatedLength;
	}

	private int finiteLength() {
	    return this.finiteLength;
	}

	private int repeatedLength() {
	    return this.repeatedLength;
	}

	// if all repeated nodes are 0
	private boolean isFinite() {
	    Node curNode = this.firstRepeatedNode;
	    for(int b=0; b<this.repeatedLength; b++) {
		if(curNode.value != 0)
		    return false;
		curNode = curNode.prev;
	    }
	    return true;
	}

	private Node getNode(int beatIndex) {
	    int b = 0;
	    Node n = this.nowNode;
	    while(b < beatIndex) {
		n = n.prev;
		b++;
	    }
	    return n;
	}

	private Node getRepeatedNode(int beatIndex) {
	    int b = 0;
	    Node n = this.firstRepeatedNode;
	    while(b < beatIndex) {
		n = n.prev;
		b++;
	    }
	    return n;
	}

	private int getNowValue() {
	    return this.nowNode.value;
	}

	private int getValueAtBeat(int beatIndex) {
	    return this.getNode(beatIndex).value;
	}

	private void extendToBeat(int beatIndex) {
	    if(this.finiteLength >= beatIndex + 1)
		return;
	    if(this.finiteLength == 0) {
		Node newFirstAndLastFiniteNode = new Node();
		this.firstFiniteNode = newFirstAndLastFiniteNode;
		this.lastFiniteNode = newFirstAndLastFiniteNode;
		this.lastFiniteNode.prev = this.firstRepeatedNode;
		this.nowNode = this.firstFiniteNode;
		this.finiteLength++;
	    }
	    while(this.finiteLength < beatIndex + 1) {
		Node newLastFiniteNode = new Node();
		this.lastFiniteNode.prev = newLastFiniteNode;
		this.lastFiniteNode = newLastFiniteNode;
		this.finiteLength++;
	    }
	    this.lastFiniteNode.prev = this.firstRepeatedNode;
	}

	private void incrementNowValue() {
	    this.incrementValueAtBeat(0);
	}

	private void decrementNowValue() {
	    this.decrementValueAtBeat(0);
	}

	private void incrementValueAtBeat(int beatIndex) {
	    this.extendToBeat(beatIndex);
	    this.getNode(beatIndex).increment();
	}

	private void decrementValueAtBeat(int beatIndex) {
	    this.extendToBeat(beatIndex);
	    this.getNode(beatIndex).decrement();
	}

	private void extendRepeatedPortionToBeat(int beatIndex) {
	    if(this.repeatedLength >= beatIndex + 1)
		return;
	    while(this.repeatedLength < beatIndex + 1) {
		Node newLastRepeatedNode = new Node();
		this.lastRepeatedNode.prev = newLastRepeatedNode;
		this.lastRepeatedNode = newLastRepeatedNode;
		this.repeatedLength++;
	    }
	    this.lastRepeatedNode.prev = this.firstRepeatedNode;
	}

	private void incrementValueAtRepeatedBeat(int beatIndex) {
	    this.extendRepeatedPortionToBeat(beatIndex);
	    this.getRepeatedNode(beatIndex).increment();
	}

	private void decrementValueAtRepeatedBeat(int beatIndex) {
	    this.extendRepeatedPortionToBeat(beatIndex);
	    this.getRepeatedNode(beatIndex).decrement();
	}

	private void shiftBackward() {
	    Node newNowNode = new Node();
	    if(this.firstFiniteNode == null)
		this.firstFiniteNode = newNowNode;
	    else
		newNowNode.prev = this.firstFiniteNode;
	    this.firstFiniteNode = newNowNode;
	    this.nowNode = this.firstFiniteNode;
	    this.finiteLength++;
	}

	private void advanceTime() {
	    if(this.nowNode.value != 0)
		System.out.println("warning: advancing time when nowValue != 0!");
	    if(this.finiteLength > 0) {
		if(this.finiteLength == 1) {
		    this.firstFiniteNode = null;
		    this.lastFiniteNode = null;
		    this.nowNode = this.firstRepeatedNode;
		} else {
		    this.firstFiniteNode = this.firstFiniteNode.prev;
		    this.nowNode = this.firstFiniteNode;
		}
		this.finiteLength--;
	    } else {
		// shift repeated portion
		Node oldFirstRepeatedNode = this.firstRepeatedNode;
		this.firstRepeatedNode = this.firstRepeatedNode.prev;
		this.lastRepeatedNode = oldFirstRepeatedNode;
		this.nowNode = this.firstRepeatedNode;
	    }
	}

	// misc
	private HandState deepCopy() {
	    return new HandState(this);
	}
	// for deepcopy
	private HandState(HandState toCopy) {
	    Node curNode = toCopy.nowNode;
	    this.nowNode = new Node(curNode.value);
	    Node curNodeOfCopy = this.nowNode;
	    Node prevNodeOfCopy;
	    this.firstFiniteNode = null;
	    this.lastFiniteNode = null;
	    for(int i=0; i<toCopy.length(); i++) {
		prevNodeOfCopy = new Node(curNode.prev.value);
		if(curNode == toCopy.firstFiniteNode) {
			this.firstFiniteNode = curNodeOfCopy;
			this.firstFiniteNode.prev = prevNodeOfCopy;
		} else if(curNode == toCopy.lastFiniteNode) {
			this.lastFiniteNode = curNodeOfCopy;
			this.lastFiniteNode.prev = prevNodeOfCopy;
		} else if(curNode == toCopy.firstRepeatedNode) {
			this.firstRepeatedNode = curNodeOfCopy;
			this.firstRepeatedNode.prev = prevNodeOfCopy;
		} else if(curNode == toCopy.lastRepeatedNode) {
			this.lastRepeatedNode = curNodeOfCopy;
			this.lastRepeatedNode.prev = prevNodeOfCopy;
		}
		curNodeOfCopy = prevNodeOfCopy;
	    }
	}

	public String toString() {
	    String out = "";
	    Node curNode = this.nowNode;
	    int b=0;
	    for(b=0; b<this.finiteLength; b++) {
		out += curNode.toString();
		curNode = curNode.prev;
	    }
	    if(this.finiteLength > 0) {
		out += " :";
	    }
	    for(; b<this.length(); b++) {
		out += curNode.toString();
		curNode = curNode.prev;
	    }
	    return out + " ";
	}

	private class Node {
	    private int value;
	    private Node prev;
	    private Node() {
		this(0);
	    }
	    private Node(int value) {
		this.value = value;
		this.prev = null;
	    }
	    private int value() {
		return this.value;
	    }
	    private void increment() {
		this.value++;
	    }
	    private void decrement() {
		this.value--;
	    }
	    public String toString() {
		if(this.value < 0)
		    return " " + this.value;
		else
		    return "  " + this.value;
	    }
	}

    }

    public static void main(String[] args) {
	State s = new State(1);
	System.out.println(s);
	s.incrementNowValueAtHand(0);
	System.out.println(s);
	s.incrementValueAtHandAtBeat(0, 3);
	System.out.println(s);
	s.decrementValueAtHandAtRepeatedBeat(0, 2);
	System.out.println(s);
    }
}
