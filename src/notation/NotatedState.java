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
	int startHand;

	// regexes
	private static String oneHandedNotationPattern = "[-0-9a-z{}:]+";
	private static String multiHandedNotationPattern = "[-0-9a-z{}:()]+";

	// querying basic info
	public Type notationType() { return this.type; }
	public State state() { return this.state; }

	// -------------------------------- ASSEMBLING --------------------------------

	public static NotatedState assemble(State state, Type targetType, int startHand) {
		NotatedState ret = new NotatedState();
		ret.state = state;
		switch(state.numHands()) {
			case 1:
				ret.type = Type.ONEHANDED;
				break;
			case 2:
				if(targetType == Type.ONEHANDED) {
					ret.startHand = startHand;
				}
				ret.type = targetType;
				break;
			default:
				ret.type = Type.MULTIHANDED;
				break;
		}
		return ret;
	}

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

	public static NotatedState parse(String notation, int numHands, int startHand) throws InvalidStateNotationException {
		Type type = getNotationType(notation);
		NotatedState ret = new NotatedState();
		ret.type = type;
		List<StateNotationToken> tokens = tokenize(notation);
		switch(type) {
			case ONEHANDED:
				switch(numHands) {
					case -1:
					case 1:
						ret.state = parseOneHanded(tokens);
						break;
					case 2:
						ret.state = parseAsync(tokens, startHand);
						ret.startHand = startHand;
						break;
					default:
						Util.ErrorOut(new IncompatibleNumberOfHandsException());
				}
				break;
			case MULTIHANDED:
				ret.state = parseMultiHanded(tokens, numHands);
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

	private static State parseAsync(List<StateNotationToken> tokens, int startHand) throws InvalidStateNotationException {
		State ret = new State(2);
		State.Node curNode = ret.nowNode;
		ret.repeatedLength = 0;
		int curHand = startHand;
		boolean inRepeatedPortion = false;
		for(int i=0; i<tokens.size(); i++) {
			StateNotationToken curToken = tokens.get(i);
			Util.printf("curToken: " + curToken, Util.DebugLevel.DEBUG);
			switch(curToken.type) {
				case VALUE:
					// create new node
					State.Node newNode = ret.new Node();
					// get the value
					int nodeHeight = curToken.value.absoluteHeight;
					if(curToken.value.isNegative) {
						nodeHeight *= -1;
					}
					newNode.setChargeAtHand(curHand, nodeHeight);
					curHand = (curHand + 1) % 2;
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
		// see if the repeated portion has odd length
		if(ret.repeatedLength % 2 == 1) {
			State.Node lastNode = curNode;
			curNode = ret.firstRepeatedNode;
			int originalRepeatedLength = ret.repeatedLength;
			for(int i=0; i<originalRepeatedLength; i++) {
				ret.repeatedLength++;
				State.Node newNode = ret.new Node();
				newNode.setChargeAtHand(curHand, curNode.getChargeAtHand((curHand+1)%2));
				lastNode.prev = newNode;
				curHand = (curHand + 1) % 2;
				lastNode = newNode;
				curNode = curNode.prev;
			}
		}
		return ret;
	}

	private static State parseMultiHanded(List<StateNotationToken> tokens, int numHands) throws InvalidStateNotationException {
		State ret = new State(2);
		State.Node curNode = ret.nowNode;
		ret.repeatedLength = 0;
		int curHand = 0;
		boolean inRepeatedPortion = false;
		boolean inBeat = false;
		// TODO: do pre-pass through tokens to see if there's a consistent numHands, and test if it's actually == to numHands
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
				if(this.state.numHands() == 1) {
					return this.displayOneHanded();
				} else {
					return this.displayAsync();
				}
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

	private String displayAsync() {
		StringBuilder ret = new StringBuilder();
		State.Node curNode = this.state.nowNode;
		int curHand = this.startHand;
		for(int i=0; i<this.state.finiteLength; i++) {
			ret.append(curNode.getChargeAtHand(curHand));
			curNode = curNode.prev;
			curHand = (curHand + 1) % 2;
		}
		if(this.state.repeatedLength > 0) {
			ret.append(":");
			for(int i=0; i<this.state.repeatedLength; i++) {
				ret.append(curNode.getChargeAtHand(curHand));
				curNode = curNode.prev;
				curHand = (curHand + 1) % 2;
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
			NotatedState nss = parse(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			System.out.println(nss.state);
			System.out.println(nss.display());
			NotatedState nss2 = assemble(nss.state, nss.type, nss.startHand);
			System.out.println(nss2.display());
		} catch(InvalidStateNotationException e) {
			e.printStackTrace();
		}
	}

}
