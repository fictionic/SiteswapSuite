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

	void parseArgs(String[] args) throws ParseError, InvalidNotationException, IncompatibleNumberOfHandsException {
		String str;
		for(int i=0; i<args.length; i++) {
			str = args[i];
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
					chainInput = new ChainInput(parseResult);
					this.chains.add(new Chain(chainInput));
					break;
				case INPUT:
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
		int index;
		boolean isTransitionChain;
		// if single input
		String inputNotation;
		String prefix = null;
		boolean isState = false;
		NotatedSiteswap notatedSiteswap;
		NotatedState notatedState;
		int numHands = -1;
		int startHand = -1;
		boolean keepZeroes = false;
		// if double input
		Chain from, to;
		// constructors
		ChainInput(ArgumentCollection parsedArgs) throws ParseError, InvalidNotationException {
			this.index = chains.size();
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
					this.isTransitionChain = false;
					break;
				case TRANSITION:
					this.to = getChain(-1);
					this.from = getChain(-2);
					this.isTransitionChain = true;
					break;
				default:
					// error
					Util.printf("IMPOSSIBLE ERROR IN ChainInput()", Util.DebugLevel.ERROR);
					System.exit(1);
			}
		}
		void process() throws InvalidNotationException, IncompatibleNumberOfHandsException {
			if(this.isTransitionChain) {
				// IN PROGRESS
				// assemble Candidates
				CompatibleNotatedObjectPair.Candidate c1, c2;
				// for now just take output from two most recent Chains as inputs
				Link l1 = getChain(-3).getLastLink(), l2 = getChain(-2).getLastLink();
				if(l1.isState) {
					c1 = new CompatibleNotatedObjectPair.Candidate(l1.notatedState);
				} else {
					c1 = new CompatibleNotatedObjectPair.Candidate(l1.notatedSiteswap);
				}
				if(l2.isState) {
					c2 = new CompatibleNotatedObjectPair.Candidate(l2.notatedState);
				} else {
					c2 = new CompatibleNotatedObjectPair.Candidate(l2.notatedSiteswap);
				}
				CompatibleNotatedObjectPair pair = new CompatibleNotatedObjectPair(c1, c2);
				Util.printf("transition mechanics not yet implemented from cmdline", Util.DebugLevel.ERROR);
				System.exit(1);
			} else {
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
		}
		Link getLink() {
			Link link = new Link();
			link.isState = this.isState;
			if(link.isState) {
				link.notatedState = this.notatedState;
			} else {
				link.notatedSiteswap = this.notatedSiteswap;
			}
			return link;
		}
		public String print() {
			StringBuilder ret = new StringBuilder("INPUT "); ret.append(this.index); ret.append(":\n");
			if(this.isTransitionChain) {
				ret.append(" type: transition\n");
				ret.append(" [TODO]\n");
			} else {
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
			}
			return ret.toString();
		}
	}

	class Link {
		boolean isState;
		NotatedSiteswap notatedSiteswap;
		NotatedState notatedState;
		List<Argument> infos;
		Argument operation;
		Link() {
			this.infos = new ArrayList<>();
		}
		String printInfo() {
			StringBuilder ret = new StringBuilder();
			if(this.isState) {
				ret.append("---> state:\n");
				ret.append(" parsed: " + this.notatedState.state.toString() + "\n");
			} else {
				ret.append("---> siteswap:\n");
				ret.append(" parsed: " + this.notatedSiteswap.siteswap.toString() + "\n");
			}
			for(Argument infoArg : this.infos) {
				switch(infoArg) {
					case CAPACITY:
						ExtendedFraction capacity;
						if(this.isState) {
							Util.printf("warning: capacity not yet implemented for states", Util.DebugLevel.ERROR);
							capacity = new ExtendedFraction(new ExtendedInteger(0), 1);
						} else {
							capacity = this.notatedSiteswap.siteswap.numBalls();
						}
						ret.append(" capacity: " + capacity.toString() + "\n");
						break;
					case VALIDITY:
						boolean validity;
						if(this.isState) {
							validity = (this.notatedState.state.repeatedLength == 0);
						} else {
							validity = this.notatedSiteswap.siteswap.isValid();
						}
						ret.append(" validity: " + validity + "\n");
						break;
					case PRIMALITY:
						boolean primality;
						if(this.isState) {
							ret.append(" primality: n/a\n");
						} else {
							ret.append(" primality: " + this.notatedSiteswap.siteswap.isPrime() + "\n");
						}
						break;
					case DIFFICULTY:
						ExtendedFraction difficulty;
						if(this.isState) {
							ret.append(" difficulty: n/a\n");
						} else {
							ret.append(" difficulty: " + this.notatedSiteswap.siteswap.difficulty().toString() + "\n");
						}
						break;
					default:
						break;
				}
			}
			return ret.toString();
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
		void addArg(ArgumentCollection arg) throws ParseError, InvalidNotationException, IncompatibleNumberOfHandsException {
			if(this.acceptingInputOptions) {
				if(arg.head.role == Argument.Role.INPUT_ROLE) {
					this.addInputArg(arg);
				} else {
					this.acceptingInputOptions = false;
					// make first link
					this.input.process();
					this.links.add(this.input.getLink());
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
		void addInputArg(ArgumentCollection arg) {
			switch(arg.head) {
				case NUM_HANDS:
					this.input.numHands = arg.followUpInt;
					break;
				case START_HAND:
					this.input.startHand = arg.followUpInt;
					break;
				case KEEP_ZEROES:
					this.input.keepZeroes = true;
					break;
				default:
					break;
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
		void execute() throws InvalidNotationException, IncompatibleNumberOfHandsException {
			// process if necessary
			if(this.acceptingInputOptions) {
				this.input.process();
				this.links.add(this.input.getLink());
				this.acceptingInputOptions = false;
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
				} else {
					// print each operation in the chain
					Util.printf("---> " + link.operation.toString() + " ", false);
					if(link.operation == Argument.TO_STATE) {
						newLink.isState = true;
						newLink.notatedState = NotatedState.assembleAutomatic(new State(link.notatedSiteswap.siteswap));
					} else {
						newLink.isState = false;
						newLink.notatedSiteswap = link.notatedSiteswap.deepCopy();
						// compute operations
						switch(link.operation) {
							case INVERT:
								newLink.notatedSiteswap.siteswap.invert();
								break;
							case SPRING:
								Util.printf("sprung not yet implemented", Util.DebugLevel.ERROR);
								break;
							case INFINITIZE:
								newLink.notatedSiteswap.siteswap.infinitize();
								break;
							case UNINFINITIZE:
								newLink.notatedSiteswap.siteswap.unInfinitize();
								break;
							case ANTITOSSIFY:
								newLink.notatedSiteswap.siteswap.antitossify();
								break;
							case UNANTITOSSIFY:
								newLink.notatedSiteswap.siteswap.unAntitossify();
								break;
							case ANTINEGATE:
								newLink.notatedSiteswap.siteswap.antiNegate();
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
