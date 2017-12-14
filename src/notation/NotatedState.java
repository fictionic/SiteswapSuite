package siteswapsuite;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;;

public class NotatedState {

	static enum Type {

		ONEHANDED(1), MULTIHANDED(2);

		private int defaultNumHands;

		private Type(int numHands) {
			this.defaultNumHands = numHands;
		}

		public int defaultNumHands() {
			return this.defaultNumHands;
		}

	}

	Type type;
	State state;

	// regexes
	private static String oneHandedNotationPattern = "[-0-9az{}:]+";
	private static String multiHandedNotationPattern = "[-0-9az:\\[\\]]+";

	// querying basic info
	public Type notationType() { return this.type; }
	public State state() { return this.state; }

	// ---------------------------------- PARSING ---------------------------------

	private static Type getNotationType(String notation) throws	InvalidStateNotationException {
		if(Pattern.matches(oneHandedNotationPattern, notation)) {
			return Type.ONEHANDED;
		}
		if(Pattern.matches(multiHandedNotationPattern, notation)) {
			return Type.MULTIHANDED;
		}
		throw new InvalidStateNotationException();
	}

	public static NotatedState parseAutomatic(String notation, int startHand) throws InvalidStateNotationException {
		Type type = getNotationType(notation);
		NotatedState ret = new NotatedState();
		ret.type = type;
		List<StateNotationToken> tokens = tokenize(notation);
		switch(type) {
			case ONEHANDED:
				ret.state = parseOneHanded(tokens);
				break;
			case MULTIHANDED:
				ret.state = parseMultiHanded(tokens);
				break;
		}
		return ret;
	}

	private static State parseOneHanded(List<StateNotationToken> tokens) {
		// State ret = new State(1);
		// State.Node curNode = ret.nowNode;
		// ret.repeatedLength = 0;
		// for(int i=0; i<tokens.size(); i++) {
		// 	StateNotationToken curToken = tokens.get(i);
		// 	switch(curToken.type) {
		// 		case MINUS:
		// 			isNegative = true;
		// 			break;
		// 		default:
		// 			ret.finiteLength++;
		// 			State.Node newNode = ret.new Node();
		// 			if(ret.nowNode == null) {
		// 				ret.nowNode = newNode;
		// 				curNode = ret.nowNode;
		// 			} else {
		// 				curNode.prev = newNode;
		// 				curNode = newNode;
		// 			}
		// 			ExtendedInteger h = StateNotation.throwHeight(a[i]);
		// 			if(isNegative) {
		// 				h.negate();
		// 			}
		// 			curNode.setChargeAtHand(0, h.finiteValue()); // h is always finite, given what chars we're giving to throwHeight
		// 			isNegative = false;
		// 			break;
		// 	}
		// 	i++;
		// }
		// // TODO: non-finite state parsing
		return null;
	}

	private static State parseMultiHanded(List<StateNotationToken> tokens) {
		// State ret = new State(2); // TODO: more_than_two-handed states... but how to figure out numHands?
		// State.Node curNode = ret.nowNode;
		// boolean isNegative = false;
		// char[] a = notation.toCharArray();
		// int i = 0;
		// boolean seenComma = false;
		// ret.repeatedLength = 0;
		// while(i < a.length) {
		// 	switch(a[i]) {
		// 		case '(':
		// 			ret.finiteLength++;
		// 			State.Node newNode = ret.new Node();
		// 			if(ret.nowNode == null) {
		// 				ret.nowNode = newNode;
		// 				curNode = ret.nowNode;
		// 			} else {
		// 				curNode.prev = newNode;
		// 				curNode = newNode;
		// 			}
		// 			seenComma = false;
		// 			break;
		// 		case ',':
		// 			seenComma = true;
		// 			break;
		// 		case ')':
		// 			break;
		// 		case '-':
		// 			isNegative = true;
		// 			break;
		// 		default:
		// 			ExtendedInteger h = StateNotation.throwHeight(a[i]);
		// 			if(isNegative) {
		// 				h.negate();
		// 			}
		// 			if(!seenComma) {
		// 				curNode.setChargeAtHand(0,h.finiteValue());
		// 			} else {
		// 				curNode.setChargeAtHand(1,h.finiteValue());
		// 			}
		// 			isNegative = false;
		// 	}
		// 	i++;
		// }
		return null;
	}

	// ------------------------------- TOKENIZATION -------------------------------

	private static enum NodeState {
		READY, // when there is no pending node, and we can parse a new token
		SEEN_MINUS, // when we've seen a minus
		INSIDE_CURLY; // when we're reading a {literal height}
	}

	private static List<StateNotationToken> tokenize(String notation) throws InvalidStateNotationException {
		NodeState nodeState = NodeState.READY;
		StringBuilder curlyHeight = null; // height string parsed from {literal height} indication
		List<StateNotationToken> tokens = new ArrayList<>();
		StateNotationToken curToken = null;
		for(int i=0; i<notation.length(); i++) {
			char c = notation.charAt(i);
			Util.printf("c=" + c);
			switch(c) {
				case '[':
				case ']':
				case ':':
					switch(nodeState) {
						case READY:
							curToken = new StateNotationToken(StateNotationToken.parseNonNodeChar(c));
							tokens.add(curToken);
							break;
						default:
							throw new InvalidStateNotationException();
					}
					break;
				case '{':
					switch(nodeState) {
						case INSIDE_CURLY:
							throw new InvalidStateNotationException();
						case READY:
							curToken = new StateNotationToken(StateNotationToken.Type.NODE); // create new node
							// fall through
						case SEEN_MINUS:
							curlyHeight = new StringBuilder();
							nodeState = NodeState.INSIDE_CURLY;
							break;
					}
					break;
				case '}':
					switch(nodeState) {
						case INSIDE_CURLY:
							curToken.node.absoluteHeight = Integer.parseInt(curlyHeight.toString());
							tokens.add(curToken);
							nodeState = NodeState.READY;
							break;
						default:
							throw new InvalidStateNotationException();
					}
					break;
				case '-':
					switch(nodeState) {
						case READY:
							curToken = new StateNotationToken(StateNotationToken.Type.NODE);
							curToken.node.isNegative = true;
							nodeState = NodeState.SEEN_MINUS;
							break;
						default:
							throw new InvalidStateNotationException();
					}
					break;
				default:
					switch(nodeState) {
						case READY:
							curToken = new StateNotationToken(StateNotationToken.Type.NODE); // create new node
							// fall through
						case SEEN_MINUS:
							// make sure it's a numeral
							NodeToken rawNodeToken = StateNotationToken.parseNodeChar(c);
							if(rawNodeToken == null) {
								throw new InvalidStateNotationException();
							}
							curToken.node.absoluteHeight = rawNodeToken.absoluteHeight;
							tokens.add(curToken);
							nodeState = NodeState.READY;
							break;
						case INSIDE_CURLY:
							curlyHeight.append(c);
							break;
					}
			}
			Util.printf("curToken: " + curToken);
			Util.printf("tokens: " + tokens);
		}
		return tokens;
	}

	// -------------------------------- DISPLAYING --------------------------------

	public String display() {
		StringBuilder ret = new StringBuilder();
		State.Node curNode = this.state.nowNode;
		for(int i=0; i<this.state.finiteLength; i++) {
			ret.append(curNode.getChargeAtHand(0));
			curNode = curNode.prev;
		}
		if(this.state.repeatedLength > 0) {
			ret.append(":");
			for(int i=0; i<this.state.repeatedLength; i++) {
				ret.append(curNode.getChargeAtHand(0));
				curNode = curNode.prev;
			}
		}
		return ret.toString();
	}

	public static void main(String[] args) {
		try {
			System.out.println(tokenize(args[0]));
		} catch(InvalidStateNotationException e) {
			e.printStackTrace();
		}
	}

}
