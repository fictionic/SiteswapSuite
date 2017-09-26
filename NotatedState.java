package siteswapsuite;

import java.util.regex.Pattern;

public class NotatedState {

	State state;
	StateNotation notationType;

	private NotatedState(int numHands) {
		this.state = new State(numHands);
		this.notationType = null; //???
	}

	public static NotatedState parse(String inputNotation, int numHands, int startHand) throws InvalidStateNotationException {
		// determine type of notation
		if(Pattern.matches(StateNotation.simpleStateNotation, inputNotation))
			return new SimpleNotatedState(inputNotation);
		// else if(Pattern.matches(StateNotation.complexStateNotation, inputNotation))
		// 	return new ComplexNotatedState(inputNotation);
		else
			throw new InvalidStateNotationException(inputNotation);
	}

	private static class SimpleNotatedState extends NotatedState {
		private SimpleNotatedState(String s) {
			super(1);
			State.Node curNode = this.state.nowNode;
			char[] a = s.toCharArray();
			int i = 0;
			boolean isNegative = false;
			this.state.repeatedLength = 0;
			while(i < a.length) {
				switch(a[i]) {
					case '-':
						isNegative = true;
						break;
					default:
						this.state.finiteLength++;
						State.Node newNode = this.state.new Node();
						if(this.state.nowNode == null) {
							this.state.nowNode = newNode;
							curNode = state.nowNode;
						} else {
							curNode.prev = newNode;
							curNode = newNode;
						}
						ExtendedInteger h = SiteswapNotation.throwHeight(a[i]);
						if(isNegative)
							h.negate();
						curNode.setChargeAtHand(0, h.finiteValue()); // h is always finite, given what chars we're giving to throwHeight
						isNegative = false;
						break;
				}
				i++;
			}
		}
	}

	// private static class ComplexNotatedState extends NotatedState {
	// 	private ComplexNotatedState(String s) {
	// 		super(2);
	// 		boolean isNegative = false;
	// 		Node curNode = this.nowNode;
	// 		char[] a = s.toCharArray();
	// 		int i = 0;
	// 		boolean seenComma = false;
	// 		this.repeatedLength = 0;
	// 		while(i < a.length) {
	// 			switch(a[i]) {
	// 				case '(':
	// 					this.finiteLength++;
	// 					Node newNode = new Node();
	// 					if(this.nowNode == null) {
	// 						this.nowNode = newNode;
	// 						curNode = nowNode;
	// 					} else {
	// 						curNode.prev = newNode;
	// 						curNode = newNode;
	// 					}
	// 					seenComma = false;
	// 					break;
	// 				case ',':
	// 					seenComma = true;
	// 					break;
	// 				case ')':
	// 					break;
	// 				case '-':
	// 					isNegative = true;
	// 					break;
	// 				default:
	// 					ExtendedInteger h = SiteswapNotation.throwHeight(a[i]);
	// 					if(isNegative)
	// 						h.negate();
	// 					if(!seenComma)
	// 						curNode.setChargeAtHand(0,h.finiteValue());
	// 					else
	// 						curNode.setChargeAtHand(1,h.finiteValue());
	// 					isNegative = false;
	// 			}
	// 			i++;
	// 		}
	// 	}
	// }

	public String print() {
		return "[not yet implemented]";
	}

	public static void main(String[] args) {
		try {
			NotatedState ns = NotatedState.parse(args[0], -1, -1);
			System.out.println(ns);
		} catch(InvalidStateNotationException e) {
			System.out.println(e.getMessage());
		}
	}


}
