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
				if(curArg.type == ArgumentType.REQUIRES_STRING) {
					ret.options.add(Argument.LITERAL_STRING);
					ret.strings.add(inlineArg);
				} else if(curArg.type == ArgumentType.REQUIRES_INT) {
					ret.options.add(Argument.LITERAL_INT);
					int intArg = Integer.parseInt(inlineArg);
					ret.ints.add(intArg);
				}
			}
		}
		return ret;
	}
	public String toString() {
		String ret = "";
		ret += this.head.toString();
		if(this.head.type == ArgumentType.REQUIRES_INT) {
			ret += " " + this.followUpInt;
		} else if(this.head.type == ArgumentType.REQUIRES_STRING) {
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

	void parseArgs(String[] args) throws ParseError, InvalidNotationException {
		String str;
		for(int i=0; i<args.length; i++) {
			str = args[i];
			ArgumentCollection parseResult = ArgumentCollection.parse(str);
			ArgumentType headType = parseResult.head.type;
			// collect follow-up if necessary
			if(headType == ArgumentType.REQUIRES_INT) {
				if(i+1 == args.length) {
					throw new ParseError("argument '" + args[i] + "' requires integer follow-up");
				}
				i++;
				try {
					parseResult.followUpInt = Integer.parseInt(args[i]);
				} catch(NumberFormatException e) {
					throw new ParseError("follow-up '" + args[i] + "' cannot be coerced into an integer");
				}
			} else if(headType == ArgumentType.REQUIRES_STRING) {
				if(i+1 == args.length) {
					throw new ParseError("argument '" + args[i] + "' requires string follow-up");
				}
				i++;
				parseResult.followUpString = args[i];
			}
			Util.printf(parseResult, Util.DebugLevel.DEBUG);
			// deal with meaning of argument
			ChainInput chainInput;
			switch(parseResult.head) {
				case ENABLE_DEBUG:
					Util.debugLevel = Util.DebugLevel.DEBUG;
					break;
				case INPUT:
					chainInput = new ChainInput(parseResult);
					this.chains.add(new Chain(chainInput));
					break;
				case TRANSITION:
					if(this.chains.size() < 2) {
						throw new ParseError("need at least two inputs to compute a transition");
					}
					chainInput = new ChainInput(parseResult);
					this.chains.add(new Chain(chainInput));
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

	class ChainInput {
		boolean isTransitionChain;
		// if single input
		String inputNotation;
		String prefix = null;
		boolean isState = false;
		NotatedSiteswap notatedSiteswap;
		NotatedState notatedState;
		int numHands = -1;
		int startHand = 0;
		boolean keepZeroes = false;
		// if double input
		Chain from, to;
		// constructors
		ChainInput(ArgumentCollection parsedArgs) throws ParseError, InvalidNotationException {
			// check what type of input we have
			switch(parsedArgs.head) {
				case INPUT:
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
					break;
				case TO_SITESWAP:
					this.to = getChain(-1);
					this.from = getChain(-1);
					// fall through
				case TRANSITION:
					this.from = getChain(-2);
					this.isTransitionChain = true;
					break;
				default:
					// error
					Util.printf("IMPOSSIBLE ERROR IN ChainInput()", Util.DebugLevel.ERROR);
					System.exit(1);
			}
		}
		void createLink() throws InvalidNotationException, IncompatibleNumberOfHandsException {
			if(this.isTransitionChain) {
				// TODO
			} else {
				if(this.prefix != null) {
					if(this.isState) {
						this.notatedState = NotatedState.parse(this.inputNotation, this.numHands, this.startHand);
					} else {
						this.notatedSiteswap = NotatedSiteswap.parse(this.inputNotation, this.numHands, this.startHand);
					}
				} else {
					try {
						this.notatedSiteswap = NotatedSiteswap.parse(this.inputNotation, this.numHands, this.startHand);
					} catch(InvalidSiteswapNotationException e) {
						try {
							this.notatedState = NotatedState.parse(this.inputNotation, this.numHands, this.startHand);
						} catch(InvalidStateNotationException e2) {
							throw new InvalidNotationException("could not interpret input '" + this.inputNotation + "' as valid siteswap or state notation");
						}
					}
				}
			}
		}
		public String toString() {
			if(this.isTransitionChain) {
				return "todo";
			} else {
				if(this.isState) {
					return this.notatedState.print();
				} else {
					return this.notatedSiteswap.print();
				}
			}
		}
	}

	class Chain {
		ChainInput input;
		List<Link> links;
		Chain() {
			this.links = new ArrayList<>();
		}
		Chain(ChainInput input) throws InvalidNotationException {
			this.input = input;
		}
		Link getLastLink() {
			return this.links.get(-1);
		}
		void addArg(ArgumentCollection arg) {
		}
		void execute() throws InvalidNotationException, IncompatibleNumberOfHandsException {
			// make first link
			this.input.createLink();
			Util.printf(this.input, Util.DebugLevel.DEBUG);
		}
		class Link {
			boolean isState;
			Siteswap siteswap;
			State state;
		}
	}

	Chain getChain(int index) {
		if(index < 0) {
			index += this.chains.size();
		}
		return this.chains.get(index);
	}

	void run() throws InvalidNotationException, IncompatibleNumberOfHandsException {
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
