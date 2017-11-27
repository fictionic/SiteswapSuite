package siteswapsuite;

import java.util.List;
import java.util.ArrayList;
import java.lang.NumberFormatException;

class ParseError extends SiteswapException {
	String message;
	ParseError(String message) {
		this.message = "ERROR: " + message;
	}
	public String getMessage() {
		return this.message;
	}
}

class ArgumentCollection {
	Argument head;
	List<Argument> options;
	List<Integer> ints;
	List<String> strings;
	String followUpString;
	int followUpInt;

	private ArgumentCollection() {
		this.head = Argument.INVALID_TOKEN;
		this.options = new ArrayList<>();
		this.ints = new ArrayList<>();
		this.strings = new ArrayList<>();
	}

	static ArgumentCollection parse(String str) {
		ArgumentCollection ret = new ArgumentCollection();
		boolean isLongOption;
		// for long options with inline arguments
		String headStr;
		String optionsStr = null;
		if(str.length() == 2 && str.charAt(0) == '-') {
			isLongOption = false;
			headStr = str;
		} else if(str.charAt(0) == '-' && str.charAt(1) == '-') {
			isLongOption = true;
			str = str.substring(2, str.length());
			// strip out inline options
			int sepIndex = str.indexOf(':');
			if(sepIndex > -1) {
				headStr = str.substring(0, sepIndex);
				optionsStr = str.substring(sepIndex+1, str.length());
			} else {
				headStr = str;
			}
		} else {
			return ret;
		}
		// parse headStr
		if(isLongOption) {
			ret.head = Argument.parseLongOptionName(headStr);
		} else {
			ret.head = Argument.parseShortOptionName(str.charAt(1));
		}
		// parse optionsStr
		if(optionsStr != null && optionsStr.length() > 0) {
			for(String subArg : optionsStr.split(",")) {
				// parse inline arguments to options
				int sepIndex = subArg.indexOf('=');
				String inlineArg = null;
				if(sepIndex > -1) {
					String[] subArgSplit = subArg.split("=", 2);
					subArg = subArgSplit[0];
					inlineArg = subArgSplit[1];
				}
				Argument curArg;
				if(subArg.length() == 1) {
					curArg = Argument.parseShortOptionName(subArg.charAt(0));
				} else {
					curArg = Argument.parseLongOptionName(subArg);
				}
				ret.options.add(curArg);
				// add inline arg if present
				if(curArg.requires == Argument.Requires.REQUIRES_STRING) {
					ret.options.add(Argument.LITERAL_STRING);
					ret.strings.add(inlineArg);
				} else if(curArg.requires == Argument.Requires.REQUIRES_INT) {
					ret.options.add(Argument.LITERAL_INT);
					int intArg = Integer.parseInt(inlineArg);
					ret.ints.add(intArg);
				}
			}
		}
		return ret;
	}

	// for debugging
	public String toString() {
		String ret = "";
		ret += this.head.toString();
		if(this.head.requires == Argument.Requires.REQUIRES_INT) {
			ret += " " + this.followUpInt;
		} else if(this.head.requires == Argument.Requires.REQUIRES_STRING) {
			ret += " " + this.followUpString;
		}
		int intIndex = 0;
		int stringIndex = 0;
		for(Argument arg : this.options) {
			if(arg == Argument.LITERAL_INT) {
				ret += " " + this.ints.get(intIndex++);
			} else if(arg == Argument.LITERAL_STRING) {
				ret += " " + this.strings.get(stringIndex++);
			} else {
				ret += " " + arg.toString();
			}
		}
		return ret;
	}
}

class Command {

	List<Chain> chains;

	Command() {
		this.chains = new ArrayList<>();
	}

	void parseArgs(String[] args) throws ParseError, InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException, ImpossibleTransitionException {
		String str;
		for(int i=0; i<args.length; i++) {
			str = args[i];
			Util.printf(str, Util.DebugLevel.DEBUG);
			ArgumentCollection parseResult = ArgumentCollection.parse(str);
			Argument.Requires headType = parseResult.head.requires;
			// collect follow-up if necessary
			if(headType == Argument.Requires.REQUIRES_INT) {
				if(i+1 == args.length) {
					throw new ParseError("argument '" + args[i] + "' requires integer follow-up");
				}
				i++;
				try {
					parseResult.followUpInt = Integer.parseInt(args[i]);
				} catch(NumberFormatException e) {
					throw new ParseError("follow-up '" + args[i] + "' cannot be coerced into an integer");
				}
			} else if(headType == Argument.Requires.REQUIRES_STRING) {
				if(i+1 == args.length) {
					throw new ParseError("argument '" + args[i] + "' requires string follow-up");
				}
				i++;
				parseResult.followUpString = args[i];
			}
			// deal with meaning of argument
			ChainInput chainInput;
			switch(parseResult.head) {
				case ENABLE_DEBUG:
					Util.debugLevel = Util.DebugLevel.DEBUG;
					break;
				case TRANSITION:
					if(this.chains.size() < 2) {
						throw new ParseError("need at least two inputs to compute a transition");
					}
					this.chains.get(this.chains.size()-1).prepare();
					this.chains.add(this.createChain(parseResult));
					break;
				case INPUT:
					if(this.chains.size() > 0) {
						this.chains.get(this.chains.size()-1).prepare();
					}
					this.chains.add(this.createChain(parseResult));
					break;
				case INVALID_TOKEN:
					throw new ParseError("invalid token: '" + str + "'");
				default:
					if(this.chains.size() == 0) {
						throw new ParseError("argument '" + str + "' must be applied to an input");
					}
					// pass argument to most recent chain
					this.getChain(-1).addArg(parseResult);
					break;
			}
		}
	}

	Chain createChain(ArgumentCollection parseResult) throws ParseError, InvalidNotationException, ImpossibleTransitionException {
		ChainInput input = null;
		// check what type of input we have
		switch(parseResult.head) {
			case INPUT:
				input = new LiteralInput(parseResult);
				break;
			case TO_SITESWAP:
				input = new TransitionInput(parseResult);
				break;
			case TRANSITION:
				input = new TransitionInput(parseResult);
				break;
			default:
				// error
				Util.printf("IMPOSSIBLE ERROR IN createChain()", Util.DebugLevel.ERROR);
				System.exit(1);
		}
		return new Chain(input);
	}

	abstract class ChainInput {
		int index;
		ChainInput() throws ParseError, InvalidNotationException {
			this.index = chains.size();
		}
		abstract void addArg(ArgumentCollection arg) throws ParseError;
		abstract String print();
		abstract void process() throws InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException, ImpossibleTransitionException;
		abstract Link getLink();
		public abstract String toString();
	}

	class LiteralInput extends ChainInput {
		String inputNotation;
		String prefix = null;
		boolean isState = false;
		NotatedSiteswap notatedSiteswap;
		NotatedState notatedState;
		int numHands = -1;
		int startHand = -1;
		boolean keepZeroes = false;

		LiteralInput(ArgumentCollection parsedArgs) throws ParseError, InvalidNotationException {
			super();
			this.inputNotation = parsedArgs.followUpString;
			// check for notation type indicators
			if(this.inputNotation.length() > 2 && this.inputNotation.charAt(2) == ':') {
				this.prefix = this.inputNotation.substring(0,2);
				this.inputNotation = this.inputNotation.substring(3,this.inputNotation.length());
				if(this.prefix.equals("ss")) {
					this.isState = false;
				} else if(this.prefix.equals("st")) {
					this.isState = true;
				} else {
					throw new InvalidNotationException("unrecognized input prefix: " + this.prefix);
				}
			}
			// get input options
			int intIndex = 0;
			for(int i=0; i<parsedArgs.options.size(); i++) {
				Argument opt = parsedArgs.options.get(i);
				switch(opt) {
					case NUM_HANDS:
						this.numHands = parsedArgs.ints.get(intIndex++);
						i++;
						break;
					case START_HAND:
						this.startHand = parsedArgs.ints.get(intIndex++);
						i++;
						break;
					case KEEP_ZEROES:
						this.keepZeroes = true;
						break;
					default:
						throw new ParseError("option '" + opt.longForm + "' is not an input option");
				}
			}
		}

		void addArg(ArgumentCollection arg) throws ParseError {
			switch(arg.head) {
				case NUM_HANDS:
					this.numHands = arg.followUpInt;
					break;
				case START_HAND:
					this.startHand = arg.followUpInt;
					break;
				case KEEP_ZEROES:
					this.keepZeroes = true;
					break;
				default:
					throw new ParseError("option '" + arg.head.longForm + "' is not an input option");
			}
		}

		void process() throws InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException {
			Util.printf("processing " + this, Util.DebugLevel.DEBUG);
			// if state or siteswap was explicitly indicated
			if(this.prefix != null) {
				if(this.isState) {
					this.notatedState = NotatedState.parse(this.inputNotation, this.numHands, this.startHand);
				} else {
					this.notatedSiteswap = NotatedSiteswap.parse(this.inputNotation, this.numHands, this.startHand, this.keepZeroes);
				}
			} else {
				try {
					this.notatedSiteswap = NotatedSiteswap.parse(this.inputNotation, this.numHands, this.startHand, this.keepZeroes);
					this.isState = false;
				} catch(InvalidSiteswapNotationException e) {
					try {
						this.notatedState = NotatedState.parse(this.inputNotation, this.numHands, this.startHand);
						this.isState = true;
					} catch(InvalidStateNotationException e2) {
						throw new InvalidNotationException("could not interpret input '" + this.inputNotation + "' as valid siteswap or state notation");
					}
				}
			}
		}

		Link getLink() {
			Link link = new Link();
			link.isState = this.isState;
			if(link.isState) {
				link.state = this.notatedState.state;
			} else {
				link.siteswap = this.notatedSiteswap.siteswap;
			}
			return link;
		}

		public String print() {
			StringBuilder ret = new StringBuilder("INPUT "); ret.append(this.index); ret.append(":\n");
			ret.append(" type: literal\n");
			ret.append(" notation: '"); ret.append(this.inputNotation); ret.append("'\n");
			if(this.numHands != -1) {
				ret.append(" numHands: ");
				ret.append(this.numHands);
				ret.append("\n");
			}
			if(this.startHand != -1) {
				ret.append(" startHand: ");
				ret.append(this.startHand);
				ret.append("\n");
			}
			return ret.toString();
		}

		public String toString() {
			return "literal input chain #" + this.index;
		}
	}

	class TransitionInput extends ChainInput {
		int fromIndex = -1, toIndex = -1;
		int maxTransitions = -1;
		int minTransitionLength = -1;
		boolean allowExtraSqueezeCatches = false;
		boolean generateBallAntiballPairs = false;
		boolean unAntitossifyTransitions = false;
		int selectTransition = 0;
		List<Siteswap> transitions;
		NotatedSiteswap outputSiteswap;

		TransitionInput(ArgumentCollection parsedArgs) throws ParseError, InvalidNotationException {
			super();
			int intIndex = 0;
			for(int i=0; i<parsedArgs.options.size(); i++) {
				Argument opt = parsedArgs.options.get(i);
				switch(opt) {
					case MAX_TRANSITIONS:
						this.maxTransitions = parsedArgs.ints.get(intIndex++);
						i++;
						break;
					case ALLOW_EXTRA_SQUEEZE_CATCHES:
						this.allowExtraSqueezeCatches = true;
						break;
					case GENERATE_BALL_ANTIBALL_PAIRS:
						this.generateBallAntiballPairs = true;
						break;
					default:
						throw new ParseError("option '" + opt.longForm + "' is not a transition option");
				}
			}
		}

		void addArg(ArgumentCollection parsedArgs) throws ParseError {
			switch(parsedArgs.head) {
				case MAX_TRANSITIONS:
					this.maxTransitions = parsedArgs.followUpInt;
					break;
				case ALLOW_EXTRA_SQUEEZE_CATCHES:
					this.allowExtraSqueezeCatches = true;
					break;
				case GENERATE_BALL_ANTIBALL_PAIRS:
					this.generateBallAntiballPairs = true;
					break;
				default:
					throw new ParseError("option '" + parsedArgs.head.longForm + "' is not a transition option");
			}
		}

		void process() throws ImpossibleTransitionException {
			Util.printf("processing " + this, Util.DebugLevel.DEBUG);
			// by default just take output from two most recent Chains as inputs
			if(this.fromIndex == -1) {
				this.fromIndex = this.index - 2;
			}
			if(this.toIndex == -1) {
				this.toIndex = this.index - 1;
			}
			Link l1 = getChain(this.fromIndex).getLastLink(), l2 = getChain(this.toIndex).getLastLink();
			State from = l1.getState();
			State to = l2.getState();
			TransitionFinder tf = new TransitionFinder(from, to);
			TransitionResults tr = tf.findTransitions(this.minTransitionLength, this.maxTransitions, this.allowExtraSqueezeCatches, this.generateBallAntiballPairs);
			if(this.unAntitossifyTransitions) {
				// tr.unAntitossify(prefix, suffix);
			}
			GeneralizedTransition gt = tr.getGeneralizedTransition();
			this.transitions = tr.getTransitions();
			// save one as output
			this.outputSiteswap = NotatedSiteswap.assembleAutomatic(transitions.get(this.selectTransition));
		}

		Link getLink() {
			Link ret = new Link();
			ret.isState = false;
			ret.siteswap = this.outputSiteswap.siteswap;
			return ret;
		}

		public String print() {
			StringBuilder ret = new StringBuilder("INPUT "); ret.append(this.index); ret.append(":\n");
			ret.append(" type: transition\n");
			ret.append(" from: output "); ret.append(this.fromIndex); ret.append("\n");
			ret.append(" to: output "); ret.append(this.toIndex); ret.append("\n");
			ret.append(" results:\n");
			for(Siteswap ss : this.transitions) {
				ret.append("  " + ss.toString() + "\n");
			}
			return ret.toString();
		}

		public String toString() {
			return "transition input chain #" + this.index;
		}
	}

	class ChainOutput {
		boolean isState;
		NotatedSiteswap notatedSiteswap;
		NotatedState notatedState;

		ChainOutput(Link link) {
			this.isState = link.isState;
			if(this.isState) {
				this.notatedState = NotatedState.assembleAutomatic(link.state);
			} else {
				this.notatedSiteswap = NotatedSiteswap.assembleAutomatic(link.siteswap);
			}
		}

		String print() {
			if(this.isState) {
				return this.notatedState.print();
			} else {
				return this.notatedSiteswap.print();
			}
		}
	}

	class Link {
		boolean isState;
		Siteswap siteswap;
		State state;
		List<Argument> infos;
		Argument operation;
		Link() {
			this.infos = new ArrayList<>();
		}
		State getState() {
			if(this.isState) {
				return this.state;
			} else {
				return new State(this.siteswap);
			}
		}
		String printInfo() {
			StringBuilder ret = new StringBuilder();
			if(this.isState) {
				ret.append("---> state:\n");
				ret.append(" parsed: " + this.state.toString() + "\n");
			} else {
				ret.append("---> siteswap:\n");
				ret.append(" parsed: " + this.siteswap.toString() + "\n");
			}
			for(Argument infoArg : this.infos) {
				switch(infoArg) {
					case CAPACITY:
						ExtendedFraction capacity;
						if(this.isState) {
							Util.printf("warning: capacity not yet implemented for states", Util.DebugLevel.ERROR);
							capacity = new ExtendedFraction(new ExtendedInteger(0), 1);
						} else {
							capacity = this.siteswap.numBalls();
						}
						ret.append(" capacity: " + capacity.toString() + "\n");
						break;
					case VALIDITY:
						boolean validity;
						if(this.isState) {
							validity = (this.state.repeatedLength == 0);
						} else {
							validity = this.siteswap.isValid();
						}
						ret.append(" validity: " + validity + "\n");
						break;
					case PRIMALITY:
						boolean primality;
						if(this.isState) {
							ret.append(" primality: n/a\n");
						} else {
							ret.append(" primality: " + this.siteswap.isPrime() + "\n");
						}
						break;
					case DIFFICULTY:
						ExtendedFraction difficulty;
						if(this.isState) {
							ret.append(" difficulty: n/a\n");
						} else {
							ret.append(" difficulty: " + this.siteswap.difficulty().toString() + "\n");
						}
						break;
					default:
						break;
				}
			}
			return ret.toString();
		}

		String print() {
			if(this.isState) {
				return this.state.toString();
			} else {
				return this.siteswap.toString();
			}
		}
	}

	class Chain {
		ChainInput input;
		List<Link> links;
		boolean acceptingInputOptions;

		Chain() {
			this.links = new ArrayList<>();
			this.acceptingInputOptions = true;
		}

		Chain(ChainInput input) throws InvalidNotationException {
			this();
			this.input = input;
		}

		Link getLastLink() {
			return this.links.get(this.links.size()-1);
		}
		
		// process input and make first link
		void prepare() throws InvalidNotationException, IncompatibleNumberOfHandsException, IncompatibleNotationException, ImpossibleTransitionException {
			this.acceptingInputOptions = false;
			this.input.process();
			this.links.add(this.input.getLink());
		}

		void addArg(ArgumentCollection arg) throws ParseError, InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException, ImpossibleTransitionException {
			if(this.acceptingInputOptions) {
				if(arg.head.role == Argument.Role.INPUT_ROLE || arg.head.role == Argument.Role.TRANSITION_ROLE) {
					this.input.addArg(arg);
				} else {
					// process input and make first link
					this.prepare();
					// add argument
					this.addNonInputArg(arg);
				}
			} else {
				if(arg.head.role == Argument.Role.INPUT_ROLE) {
					throw new ParseError("input options must come before all others");
				} else {
					this.addNonInputArg(arg);
				}
			}
		}

		void addNonInputArg(ArgumentCollection arg) {
			switch(arg.head.role) {
				case INFO_ROLE:
					if(arg.head == Argument.INFO) {
						this.getLastLink().infos.addAll(arg.options);
					} else {
						this.getLastLink().infos.add(arg.head);
					}
					break;
				case OPERATION_ROLE:
					if(arg.head == Argument.OPS) {
						for(Argument op : arg.options) {
							this.getLastLink().operation = op;
							this.links.add(new Link());
						}
					} else {
						this.getLastLink().operation = arg.head;
						this.links.add(new Link());
					}
					break;
				default:
					break;

			}
		}

		void execute() throws InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException, ImpossibleTransitionException {
			Util.printf("executing " + this, Util.DebugLevel.DEBUG);
			if(this.acceptingInputOptions) {
				this.prepare();
			}
			// print input info
			Util.printf(this.input.print(), Util.DebugLevel.INFO, false);
			// assemble chain by executing operations
			for(int i=0; i<this.links.size()-1; i++) {
				Link link = this.links.get(i);
				// print info
				Util.printf(link.printInfo(), false);
				// next link (holds result of operation)
				Link newLink = this.links.get(i+1);
				if(link.isState) {
					// TODO
					Util.printf("dunno what to do when link is a state (yet)", Util.DebugLevel.ERROR);
					System.exit(1);
				} else {
					// print each operation in the chain
					Util.printf("---> " + link.operation.toString() + " ", false);
					if(link.operation == Argument.TO_STATE) {
						newLink.isState = true;
						newLink.state = new State(link.siteswap);
					} else {
						newLink.isState = false;
						newLink.siteswap = link.siteswap.deepCopy();
						// compute operations
						switch(link.operation) {
							case INVERT:
								newLink.siteswap.invert();
								break;
							case SPRING:
								Util.printf("sprung not yet implemented", Util.DebugLevel.ERROR);
								break;
							case INFINITIZE:
								newLink.siteswap.infinitize();
								break;
							case UNINFINITIZE:
								newLink.siteswap.unInfinitize();
								break;
							case ANTITOSSIFY:
								newLink.siteswap.antitossify();
								break;
							case UNANTITOSSIFY:
								newLink.siteswap.unAntitossify();
								break;
							case ANTINEGATE:
								newLink.siteswap.antiNegate();
								break;
							default:
								break;
						}
					}
				}
				// Util.printf("=====");
			}
			// print info for last link
			Util.printf(this.getLastLink().printInfo());
		}

		ChainOutput getOutput() {
			Link lastLink = this.getLastLink();
			return new ChainOutput(lastLink);
		}

		public String toString() {
			return this.input.toString();
		}
	}

	Chain getChain(int index) {
		if(index < 0) {
			index += this.chains.size();
		}
		return this.chains.get(index);
	}

	void run() throws InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException, ImpossibleTransitionException {
		for(int i=0; i<this.chains.size(); i++) {
			this.getChain(i).execute();
		}
	}

}

public class Main {

	public static void main(String[] args) {
		Command command = null;
		try {
			command = new Command();
			command.parseArgs(args);
			command.run();
		} catch(SiteswapException e) {
			Util.printf(e.getMessage(), Util.DebugLevel.ERROR);
		}
	}

}
