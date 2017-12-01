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

public class Main {

	static class Command {

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
						for(Argument arg : parseResult.options) {
							if(arg == Argument.INVALID_TOKEN) {
								throw new ParseError("invalid token: '" + str + "'");
							}
							if(arg.role != Argument.Role.DEBUG_ROLE) {
								throw new ParseError("option " + arg + " is not a valid debug scope specifier");
							}
							switch(arg) {
								case ENABLE_DEBUG_MAIN:
									Util.debugClasses.add(Main.class.getName());
									Util.debugClasses.add(Argument.class.getName());
									Util.debugClasses.add(ArgumentCollection.class.getName());
									break;
								case ENABLE_DEBUG_SITESWAP:
									Util.debugClasses.add(Toss.class.getName());
									Util.debugClasses.add(Siteswap.class.getName());
									Util.debugClasses.add(State.class.getName());
									break;
								default:
									throw new ParseError("option " + arg + " is not a valid debug scope specifier");
							}
						}
						break;
					case TRANSITION:
						if(this.chains.size() < 2) {
							throw new ParseError("need at least two inputs to compute a transition");
						}
						this.chains.get(this.chains.size()-1).prepare();
						this.chains.add(this.createChain(parseResult));
						break;
					case TO_SITESWAP:
						if(this.chains.size() < 1) {
							throw new ParseError("need at least one input from which to compute a siteswap");
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
					input = new ToSiteswapInput(parseResult);
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
			TransitionOptions transitionOptions;
			boolean displayGeneralTransition = false;
			boolean unAntitossifyTransitions = false;
			List<Siteswap> transitions;
			GeneralizedTransition generalizedTransition;
			NotatedSiteswap outputSiteswap;

			TransitionInput(ArgumentCollection parsedArgs) throws ParseError, InvalidNotationException {
				super();
				this.transitionOptions = new TransitionOptions();
				int intIndex = 0;
				for(int i=0; i<parsedArgs.options.size(); i++) {
					Argument opt = parsedArgs.options.get(i);
					if(opt.role != Argument.Role.TRANSITION_ROLE) {
						throw new ParseError("option '" + opt.longForm + "' is not a transition option");
					}
					switch(opt) {
						case MAX_TRANSITIONS:
							this.transitionOptions.maxTransitions = parsedArgs.ints.get(intIndex++);
							i++;
							break;
						case ALLOW_EXTRA_SQUEEZE_CATCHES:
							this.transitionOptions.allowExtraSqueezeCatches = true;
							break;
						case GENERATE_BALL_ANTIBALL_PAIRS:
							this.transitionOptions.generateBallAntiballPairs = true;
							break;
						case MIN_TRANSITION_LENGTH:
							this.transitionOptions.minTransitionLength = parsedArgs.ints.get(intIndex++);
							i++;
							break;
						case SELECT_TRANSITION:
							this.transitionOptions.selectTransition = parsedArgs.ints.get(intIndex++);
							i++;
						case DISPLAY_GENERAL_TRANSITION:
							this.displayGeneralTransition = true;
							break;
						default:
							throw new ParseError("transition option '" + opt.longForm + "' is not yet supported");
					}
				}
			}

			void addArg(ArgumentCollection parsedArgs) throws ParseError {
				if(parsedArgs.head.role != Argument.Role.TRANSITION_ROLE) {
					throw new ParseError("option '" + parsedArgs.head.longForm + "' is not a transition option");
				}
				switch(parsedArgs.head) {
					case MAX_TRANSITIONS:
						this.transitionOptions.maxTransitions = parsedArgs.followUpInt;
						break;
					case ALLOW_EXTRA_SQUEEZE_CATCHES:
						this.transitionOptions.allowExtraSqueezeCatches = true;
						break;
					case GENERATE_BALL_ANTIBALL_PAIRS:
						this.transitionOptions.generateBallAntiballPairs = true;
						break;
					case MIN_TRANSITION_LENGTH:
						this.transitionOptions.minTransitionLength = parsedArgs.followUpInt;
						break;
					case SELECT_TRANSITION:
						this.transitionOptions.selectTransition = parsedArgs.followUpInt;
						break;
					case DISPLAY_GENERAL_TRANSITION:
						this.displayGeneralTransition = true;
						break;
					default:
						throw new ParseError("transition option '" + parsedArgs.head.longForm + "' is not yet supported");
				}
			}

			void getToFromIndeces() {
				// by default just take output from two most recent Chains as inputs
				if(this.fromIndex == -1) {
					this.fromIndex = this.index - 2;
				}
				if(this.toIndex == -1) {
					this.toIndex = this.index - 1;
				}
			}

			void process() throws IncompatibleNumberOfHandsException, ImpossibleTransitionException {
				Util.printf("processing " + this, Util.DebugLevel.DEBUG);
				this.getToFromIndeces();
				Link l1 = getChain(this.fromIndex).getLastLink(), l2 = getChain(this.toIndex).getLastLink();
				State from = l1.getState();
				State to = l2.getState();
				if(from.numHands() != to.numHands()) {
					throw new IncompatibleNumberOfHandsException();
				}
				TransitionFinder tf = new TransitionFinder(from, to);
				TransitionResults tr = tf.findTransitions(this.transitionOptions);
				if(this.unAntitossifyTransitions) {
					Util.printf("WARNING: un-antitossify-transitions not yet implemented", Util.DebugLevel.ERROR);
					// tr.unAntitossify(prefix, suffix);
				}
				this.generalizedTransition = tr.getGeneralizedTransition();
				this.transitions = tr.getTransitions();
				// save one as output
				this.outputSiteswap = NotatedSiteswap.assembleAutomatic(tr.getSelectedTransition());
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
				if(this.displayGeneralTransition) {
					ret.append(" generalized transition: ");
					NotatedSiteswap throwsPortion = NotatedSiteswap.assembleAutomatic(this.generalizedTransition.getThrowsPortion());
					ret.append(throwsPortion.print());
					NotatedSiteswap catchesPortion = NotatedSiteswap.assembleAutomatic(this.generalizedTransition.getCatchesPortion());
					ret.append("{");
					ret.append(catchesPortion.print());
					ret.append("}\n");
				}
				ret.append(" results:\n");
				for(int i=0; i<this.transitions.size(); i++) {
					Siteswap ss = this.transitions.get(i);
					ret.append("  " + ss.toString());
					if(i == this.transitionOptions.selectTransition) {
						ret.append(" --->");
					}
					ret.append("\n");
				}
				return ret.toString();
			}

			public String toString() {
				return "transition input chain #" + this.index;
			}
		}

		class ToSiteswapInput extends TransitionInput {
			ToSiteswapInput(ArgumentCollection parsedArgs) throws ParseError, InvalidNotationException {
				super(parsedArgs);
				this.transitionOptions.minTransitionLength = 1;
			}
			@Override
			void getToFromIndeces() {
				this.toIndex = this.index-1;
				this.fromIndex = this.index-1;
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
				StringBuilder ret = new StringBuilder();
				ret.append("OUTPUT: \n");
				if(this.isState) {
					ret.append(" state: " );
					ret.append(this.notatedState.print());
				} else {
					ret.append(" siteswap: " );
					ret.append(this.notatedSiteswap.print());
				}
				return ret.toString();
			}
		}

		class Link {
			boolean isState;
			Siteswap siteswap;
			State state;
			List<Argument> infos;
			Argument operation;
			TransitionOptions transitionOptions; // for --to-siteswap

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
								capacity = this.state.numBalls();
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
				Class callingClass;
				return this.links.get(this.links.size()-1);
			}

			// process input and make first link
			void prepare() throws InvalidNotationException, IncompatibleNumberOfHandsException, IncompatibleNotationException, ImpossibleTransitionException {
				if(this.acceptingInputOptions) {
					Util.printf("preparing " + this, Util.DebugLevel.DEBUG);
					this.acceptingInputOptions = false;
					this.input.process();
					this.links.add(this.input.getLink());
				}
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
						// print each operation in the chain
						Util.printf("---> " + link.operation.toString() + " ", false);
						if(link.operation == Argument.TO_SITESWAP) {
							newLink.isState = false;
							TransitionFinder tf = new TransitionFinder(link.state, link.state);
							TransitionResults tr = tf.findTransitions(link.transitionOptions);
							newLink.siteswap = tr.getSelectedTransition();
						} else {
							newLink.isState = true;
							newLink.state = link.state.deepCopy();
							// compute operations
							switch(link.operation) {
								default:
									break;
							}
						}
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
									Util.printf("WARNING: sprung not yet implemented", Util.DebugLevel.ERROR);
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
				}
				// print info for last link
				Util.printf(this.getLastLink().printInfo(), false);
				// print output info
				Util.printf(this.getOutput().print());
				Util.printf("");
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
