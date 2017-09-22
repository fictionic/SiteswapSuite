package siteswapsuite;

import java.util.regex.Pattern;

class InvalidStateNotationException extends Exception {
	String message;
	InvalidStateNotationException(String notation) {
		this.message = "ERROR: string `" + notation + "' is not valid state notation";
	}
	public String getMessage() {
		return this.message;
	}
}

public class NotatedState extends State {

	private static String charge = "(-?[0-9a-z])";
	private static String simpleStateNotation = charge + "+";
	//private static String beat = "(\\(" + charge + "(" +  "," + charge + ")*" + "\\))";
	private static String beat = "(\\(" + charge + "(" +  "," + charge + ")" + "\\))";
	private static String complexStateNotation = beat + "+";

	private NotatedState(int numHands) {
		super(numHands);
	}

	public static NotatedState parse(String inputNotation) throws InvalidStateNotationException {
		// determine type of notation
		if(Pattern.matches(simpleStateNotation, inputNotation))
			return new SimpleNotatedState(inputNotation);
		else if(Pattern.matches(complexStateNotation, inputNotation))
			return new ComplexNotatedState(inputNotation);
		else
			throw new InvalidStateNotationException(inputNotation);
	}

	private static class SimpleNotatedState extends NotatedState {
		private SimpleNotatedState(String s) {
			super(1);
			Node curNode = this.nowNode;
			char[] a = s.toCharArray();
			int i = 0;
			boolean isNegative = false;
			this.repeatedLength = 0;
			while(i < a.length) {
				switch(a[i]) {
					case '-':
						isNegative = true;
						break;
					default:
						this.finiteLength++;
						Node newNode = new Node();
						if(this.nowNode == null) {
							this.nowNode = newNode;
							curNode = nowNode;
						} else {
							curNode.prev = newNode;
							curNode = newNode;
						}
						ExtendedInteger h = Notation.throwHeight(a[i]);
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

	private static class ComplexNotatedState extends NotatedState {
		private ComplexNotatedState(String s) {
			super(2);
			boolean isNegative = false;
			Node curNode = this.nowNode;
			char[] a = s.toCharArray();
			int i = 0;
			boolean seenComma = false;
			this.repeatedLength = 0;
			while(i < a.length) {
				switch(a[i]) {
					case '(':
						this.finiteLength++;
						Node newNode = new Node();
						if(this.nowNode == null) {
							this.nowNode = newNode;
							curNode = nowNode;
						} else {
							curNode.prev = newNode;
							curNode = newNode;
						}
						seenComma = false;
						break;
					case ',':
						seenComma = true;
						break;
					case ')':
						break;
					case '-':
						isNegative = true;
						break;
					default:
						ExtendedInteger h = Notation.throwHeight(a[i]);
						if(isNegative)
							h.negate();
						if(!seenComma)
							curNode.setChargeAtHand(0,h.finiteValue());
						else
							curNode.setChargeAtHand(1,h.finiteValue());
						isNegative = false;

				}
				i++;
			}
		}
	}

	public String print() {
		return "[not yet implemented]";
	}

	public static void main(String[] args) {
		try {
			NotatedState ns = NotatedState.parse(args[0]);
			System.out.println(ns);
		} catch(InvalidStateNotationException e) {
			System.out.println(e.getMessage());
		}
	}


}
