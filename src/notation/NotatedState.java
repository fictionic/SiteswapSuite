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
	private static String multiHandedNotationPattern = "[-0-9az:()]+";

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

	public static NotatedState parseAutomatic(String notation) throws InvalidStateNotationException {
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

	private static State parseOneHanded(List<StateNotationToken> tokens) throws InvalidStateNotationException {
		State ret = new State(1);
		State.Node curNode = ret.nowNode;
		ret.repeatedLength = 0;
		boolean inRepeatedPortion = false;
		for(int i=0; i<tokens.size(); i++) {
			StateNotationToken curToken = tokens.get(i);
			Util.printf("curToken: " + curToken, Util.DebugLevel.DEBUG);
			switch(curToken.type) {
				case VALUE:
					// create new node
					State.Node newNode = ret.new Node();
					int nodeHeight = curToken.value.absoluteHeight;
					if(curToken.value.isNegative) {
						nodeHeight *= -1;
					}
					newNode.setChargeAtHand(0, nodeHeight);
					// add node to right place
					if(inRepeatedPortion) {
						ret.repeatedLength++;
						if(ret.firstRepeatedNode == null) {
							ret.firstRepeatedNode = newNode;
						}
						if(curNode == null) {
							curNode = newNode;
							ret.nowNode = newNode;
						} else {
							curNode.prev = newNode;
						}
					} else {
						ret.finiteLength++;
						if(curNode == null) {
							ret.nowNode = newNode;
						} else {
							curNode.prev = newNode;
						}
					}
					curNode = newNode;
					break;
				case COLON:
					if(inRepeatedPortion) {
						throw new InvalidStateNotationException();
					}
					inRepeatedPortion = true;
					break;
			}
		}
		return ret;
	}

	private static State parseMultiHanded(List<StateNotationToken> tokens) throws InvalidStateNotationException {
		State ret = new State(2);
		State.Node curNode = ret.nowNode;
		ret.repeatedLength = 0;
		int curHand = 0;
		boolean inRepeatedPortion = false;
		boolean inBeat = false;
		for(int i=0; i<tokens.size(); i++) {
			StateNotationToken curToken = tokens.get(i);
			Util.printf("curToken: " + curToken, Util.DebugLevel.DEBUG);
			switch(curToken.type) {
				case VALUE:
					if(!inBeat) {
						throw new InvalidStateNotationException();
					}
					int nodeHeight = curToken.value.absoluteHeight;
					if(curToken.value.isNegative) {
						nodeHeight *= -1;
					}
					curNode.setChargeAtHand(curHand, nodeHeight);
					curHand++;
					break;
				case BEAT_OPEN:
					if(inBeat) {
						throw new InvalidStateNotationException();
					}
					// create new node
					State.Node newNode = ret.new Node();
					// add node to right place
					if(inRepeatedPortion) {
						ret.repeatedLength++;
						if(ret.firstRepeatedNode == null) {
							ret.firstRepeatedNode = newNode;
						}
						if(curNode == null) {
							curNode = newNode;
							ret.nowNode = newNode;
						} else {
							curNode.prev = newNode;
						}
					} else {
						ret.finiteLength++;
						if(curNode == null) {
							ret.nowNode = newNode;
						} else {
							curNode.prev = newNode;
						}
					}
					curNode = newNode;
					inBeat = true;
					curHand = 0;
					break;
				case BEAT_CLOSE:
					if(!inBeat) {
						throw new InvalidStateNotationException();
					}
					inBeat = false;
					break;
				case COLON:
					if(inBeat || inRepeatedPortion) {
						throw new InvalidStateNotationException();
					}
					inRepeatedPortion = true;
					break;
			}
		}
		return ret;
	}

	// ------------------------------- TOKENIZATION -------------------------------

	private static enum NodeState {
		READY, // when there is no pending value, and we can parse a new token
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
			Util.printf("c=" + c, Util.DebugLevel.DEBUG);
			switch(c) {
				case '(':
				case ')':
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
							curToken = new StateNotationToken(StateNotationToken.Type.VALUE); // create new value
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
							curToken.value.absoluteHeight = Integer.parseInt(curlyHeight.toString());
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
							curToken = new StateNotationToken(StateNotationToken.Type.VALUE);
							curToken.value.isNegative = true;
							nodeState = NodeState.SEEN_MINUS;
							break;
						default:
							throw new InvalidStateNotationException();
					}
					break;
				default:
					switch(nodeState) {
						case READY:
							curToken = new StateNotationToken(StateNotationToken.Type.VALUE); // create new value
							// fall through
						case SEEN_MINUS:
							// make sure it's a numeral
							ValueToken rawValueToken = StateNotationToken.parseNodeChar(c);
							if(rawValueToken == null) {
								throw new InvalidStateNotationException();
							}
							curToken.value.absoluteHeight = rawValueToken.absoluteHeight;
							tokens.add(curToken);
							nodeState = NodeState.READY;
							break;
						case INSIDE_CURLY:
							curlyHeight.append(c);
							break;
					}
			}
			Util.printf("curToken: " + curToken, Util.DebugLevel.DEBUG);
			Util.printf("tokens: " + tokens, Util.DebugLevel.DEBUG);
		}
		return tokens;
	}

	// -------------------------------- DISPLAYING --------------------------------

	public String display() {
		switch(this.type) {
			case ONEHANDED:
				return this.displayOneHanded();
			case MULTIHANDED:
				return this.displayMultiHanded();
			default:
				return null; //FIXME
		}
	}

	private String displayOneHanded() {
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

	private String displayMultiHanded() {
		StringBuilder ret = new StringBuilder();
		State.Node curNode = this.state.nowNode;
		for(int i=0; i<this.state.finiteLength; i++) {
			ret.append('(');
			for(int h=0; h<this.state.numHands(); h++) {
				ret.append(curNode.getChargeAtHand(h));
			}
			curNode = curNode.prev;
			ret.append(')');
		}
		if(this.state.repeatedLength > 0) {
			ret.append(":");
			for(int i=0; i<this.state.repeatedLength; i++) {
				ret.append('(');
				for(int h=0; h<this.state.numHands(); h++) {
					ret.append(curNode.getChargeAtHand(h));
				}
				curNode = curNode.prev;
				ret.append(')');
			}
		}
		return ret.toString();
	}

	public static void main(String[] args) {
		try {
			List<StateNotationToken> tokens = tokenize(args[0]);
			System.out.println(tokens);
			NotatedState nss = parseAutomatic(args[0]);
			System.out.println(nss.state);
			System.out.println(nss.display());
		} catch(InvalidStateNotationException e) {
			e.printStackTrace();
		}
	}

}
