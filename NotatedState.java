package siteswapsuite;

import java.util.regex.Pattern;

public class NotatedState {

	State state;
	StateNotation notationType;

	private NotatedState(int numHands) {
		this.state = new State(numHands);
		this.notationType = null; //???
	}

	public static NotatedState parse(String inputNotation, int numHands, int startHand) throws InvalidStateNotationException, IncompatibleNumberOfHandsException {
		// determine type of notation
		StateNotation notationType = StateNotation.analyze(inputNotation);
		if(notationType == StateNotation.SIMPLE) {
			if(numHands == 1 || numHands == -1) {
				return new SimpleNotatedState(inputNotation);
			} else {
				throw new IncompatibleNumberOfHandsException();
			}
		} else if(notationType == StateNotation.COMPLEX) {
			Util.printf("ERROR: complex state notation parsing not yet supported!", Util.DebugLevel.ERROR);
			System.exit(1);
			return null;
			// return new ComplexNotatedState(inputNotation);
		}
		else {
			throw new InvalidStateNotationException(inputNotation);
		}
	}

	public static NotatedState assembleAutomatic(State state) {
		try {
			return assemble(state, StateNotation.defaultNotationType(state.numHands()));
		} catch(IncompatibleNumberOfHandsException e) {
			Util.printf("ERROR: impossible error in NotatedState.assembleAutomatic", Util.DebugLevel.ERROR);
			System.exit(1);
			return null;
		}
	}

	public static NotatedState assemble(State state, StateNotation notationType) throws IncompatibleNumberOfHandsException {
		NotatedState ret;
		if(notationType == StateNotation.SIMPLE) {
			ret = new SimpleNotatedState();
			ret.state = state;
		} else {
			ret = new ComplexNotatedState();
			ret.state = state;
		}
		return ret;
	}

	private static class SimpleNotatedState extends NotatedState {
		SimpleNotatedState() {
			super(1);
			this.notationType = StateNotation.SIMPLE;
		}
		SimpleNotatedState(String s) {
			this();
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
			// TODO: non-finite state parsing
		}
	}

	private static class ComplexNotatedState extends NotatedState {
		ComplexNotatedState() {
			super(2);
			this.notationType = StateNotation.COMPLEX;
		}
		ComplexNotatedState(String s) {
			this();
			boolean isNegative = false;
			State.Node curNode = this.state.nowNode;
			char[] a = s.toCharArray();
			int i = 0;
			boolean seenComma = false;
			this.state.repeatedLength = 0;
			while(i < a.length) {
				switch(a[i]) {
					case '(':
						this.state.finiteLength++;
						State.Node newNode = this.state.new Node();
						if(this.state.nowNode == null) {
							this.state.nowNode = newNode;
							curNode = this.state.nowNode;
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
						ExtendedInteger h = SiteswapNotation.throwHeight(a[i]);
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
			NotatedState ns = NotatedState.parse(args[0], -1, -1);
			System.out.println(ns);
		} catch(InvalidStateNotationException | IncompatibleNumberOfHandsException e) {
			System.out.println(e.getMessage());
		}
	}


}
