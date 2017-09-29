package siteswapsuite;

import java.util.List;
import java.util.LinkedList;
import java.lang.NumberFormatException;

public class Main {

	static class ParseError extends SiteswapException {
		String message;
		ParseError(String message) {
			this.message = "ERROR: " + message;
		}
		public String getMessage() {
			return this.message;
		}
	}

	// cmdline tokens
	static enum GlobalOption {
		ENABLE_DEBUG(false, "-d", "--debug"),
		// transition options
		MIN_TRANSITION_LENGTH(true, "-l", "--minTransitionLength"),
		MAX_TRANSITIONS(true, "-m", "--maxTransitions"),
		ALLOW_EXTRA_SQUEEZE_CATCHES(false, "-q", "--allowExtraSqueezeCatches"),
		GENERATE_BALL_ANTIBALL_PAIRS(false, "-g", "--generateBallAntiballPairs"),
		UN_ANTITOSSIFY_TRANSITIONS(false, "-A", "--unAntitossifyTransitions"),
		DISPLAY_GENERAL_TRANSITION(false, "-G", "--displayGeneralTransition"),
		INVALID_TOKEN(false, null, null);
		boolean requiresParam;
		String shortForm;
		String longForm;
		GlobalOption(boolean requiresParam, String shortForm, String longForm) {
			this.requiresParam = requiresParam;
			this.shortForm = shortForm;
			this.longForm = longForm;
		}
		static GlobalOption fromStr(String str) {
			for(GlobalOption opt : GlobalOption.values()) {
				if(str.equals(opt.shortForm) || str.equals(opt.longForm)) {
					return opt;
				}
			}
			return INVALID_TOKEN;
		}
	}

	static abstract class InputObject {
		boolean isState;
		int index;
		// arguments passed from CommandObject (first will be inputNotation)
		List<String> args;
		// results of parsing arguments
		String inputNotation;
		// hand specification
		int numHands = -1; // inferred from input notations unless specified
		int startHand = 0;
		// info printing settings
		boolean printNumBalls = false;
		boolean printState = false;
		boolean printOrbits;
		boolean printDifficulty = false;
		boolean printValidity = false;
		boolean printPrimality = false;
		// siteswap operation sequence to be performed
		List<InputOption> operations;
		// for state input objects only TODO: eventually make this better?
		int minSSLength = 1;

		// results of parsing notation
		NotatedSiteswap notatedSiteswap;
		NotatedState notatedState;
		NotatedSiteswap modifiedSiteswap;
		NotatedState modifiedState;

		InputObject(int index) {
			this.index = index;
			this.args = new LinkedList<String>();
			this.operations = new LinkedList<InputOption>();
		}

		void addArg(String arg) {
			this.args.add(arg);
		}

		void parseArgs() throws ParseError {
			if(this.args.size() == 0) {
				throw new ParseError("expected input notation");
			}
			this.inputNotation = this.args.get(0);
			int i=1;
			String str;
			int intArg = 0;
			InputOption opt;
			while(i < this.args.size()) {
				str = this.args.get(i);
				opt = InputOption.fromStr(str);
				if(opt.requiresParam) {
					if(i + 1 < this.args.size()) {
						try {
							intArg = Integer.parseInt(this.args.get(i+1));
						} catch(NumberFormatException e) {
							throw new ParseError("option `" + str + "' requires integer argument; got `" + this.args.get(i+1) + "'");
						}
						i++;
					} else {
						throw new ParseError("option `" + str + "' requires integer argument");
					}
				}
				switch(opt) {
					case MIN_SS_LENGTH:
						this.minSSLength = intArg;
						break;
					case NUM_HANDS:
						this.numHands = intArg;
						break;
					case START_HAND:
						this.startHand = intArg;
						break;
					case CAPACITY:
						this.printNumBalls = true;
						break;
					case STATE:
						this.printState = true;
						break;
					case VALIDITY:
						this.printValidity = true;
						break;
					case PRIMALITY:
						this.printPrimality = true;
						break;
					case DIFFICULTY:
						this.printDifficulty = true;
						break;
					case INVALID_TOKEN:
						throw new ParseError("unrecognized input option: `" + str + "'");
					default:
						this.operations.add(opt);
						break;
				}
				i++;
			}
		}

		abstract void parseNotation() throws InvalidNotationException, IncompatibleNumberOfHandsException;

		abstract void runComputations();

		void displayInfo() {
			this.displayInputInfo();
			this.displayModificationSequence();
			this.displayComputedInfo();
		}

		void displayModificationSequence() {
			// generate string representing operation sequence
			if(!this.operations.isEmpty()) {
				String ops = "";
				int c = 0;
				for(InputOption o : operations) {
					switch(o) {
						case INVERT:
							ops += "invert";
							break;
						case SPRING:
							ops += "spring";
							break;
						case INFINITIZE:
							ops += "infinitize";
							break;
						case UNINFINITIZE:
							ops += "un-infinitize";
							break;
						case ANTITOSSIFY:
							ops += "antitossify";
							break;
						case UNANTITOSSIFY:
							ops += "un-antitossify";
							break;
						case ANTINEGATE:
							ops += "anti-negate";
							break;
					}
					if(++c < operations.size())
						ops += ", ";
				}
				Util.printf(" Modification Sequence: " + ops, Util.DebugLevel.INFO);
				Util.printf("---------", Util.DebugLevel.INFO);
				Util.printf("OUTPUT " + this.index + ":", Util.DebugLevel.INFO);
			}
		}

		abstract void displayInputInfo();
		abstract void displayComputedInfo();

		// types of operation
		static enum InputOption {
			// state: min ss length
			MIN_SS_LENGTH(true, "-l", "--minSSLength"),
			// siteswap: hand specification
			NUM_HANDS(true, "-h", "--numHands"),
			START_HAND(true, "-H", "--startHand"),
			// siteswap info to compute
			CAPACITY(false, "-c", "--capacity"),
			STATE(false, "-s", "--state"),
			VALIDITY(false, "-v", "--validity"),
			PRIMALITY(false, "-P", "--primality"),
			DIFFICULTY(false, "-d", "--difficulty"),
			// siteswap operations
			INVERT(false, "-V", "--invert"),
			SPRING(false, "-p", "--spring"),
			INFINITIZE(false, "-f", "--infinitize"),
			UNINFINITIZE(false, "-F", "--unInfinitize"),
			ANTITOSSIFY(false, "-a", "--antitossify"),
			UNANTITOSSIFY(false, "-A", "--unAntitossify"),
			ANTINEGATE(false, "-N", "--antiNegate"),
			INVALID_TOKEN(false, null, null);

			boolean requiresParam;
			String shortForm;
			String longForm;

			InputOption(boolean requiresParam, String shortForm, String longForm) {
				this.requiresParam = requiresParam;
				this.shortForm = shortForm;
				this.longForm = longForm;
			}

			static InputOption fromStr(String str) {
				for(InputOption opt : InputOption.values()) {
					if(str.equals(opt.shortForm) || str.equals(opt.longForm)) {
						return opt;
					}
				}
				return INVALID_TOKEN;
			}
		}

	}

	static class SiteswapInputObject extends InputObject {

		// constructor
		SiteswapInputObject(int index) {
			super(index);
			this.isState = false;
		}

		void parseNotation() throws InvalidNotationException, IncompatibleNumberOfHandsException {
			try {
				this.notatedSiteswap = NotatedSiteswap.parse(this.inputNotation, this.numHands, this.startHand);
			} catch(InvalidSiteswapNotationException | IncompatibleNumberOfHandsException e) {
				throw e;
			}
		}

		void runComputations() {
			// first get state
			this.notatedState = NotatedState.assembleAutomatic(new State(this.notatedSiteswap.siteswap));
			// first run modifications to siteswap
			this.modifiedSiteswap = this.notatedSiteswap.deepCopy();
			for(InputOption m : this.operations) {
				switch(m) {
					case ANTITOSSIFY:
						this.modifiedSiteswap.siteswap.antitossify();
						break;
					case SPRING:
						try {
							this.modifiedSiteswap = this.modifiedSiteswap.spring();
						} catch(SprungException e) {
							Util.printf(e.getMessage(), Util.DebugLevel.INFO);
						}
						break;
					default:
						break;
				}
			}
			// then compute the modified state
			if(this.operations.size() > 0) {
				this.modifiedState = NotatedState.assembleAutomatic(new State(this.modifiedSiteswap.siteswap));
			} else {
				this.modifiedState = NotatedState.assembleAutomatic(this.notatedState.state);
			}
		}

		void displayInputInfo() {
			Util.printf("INPUT " + this.index + ":   '" + this.inputNotation + "'", Util.DebugLevel.INFO);
			Util.printf("---------", Util.DebugLevel.INFO);
			Util.printf(" parsed:     " + this.notatedSiteswap.siteswap.toString(), Util.DebugLevel.INFO);
			Util.printf(" notated:    " + this.notatedSiteswap.print(), Util.DebugLevel.INFO);
			Util.printf("---------", Util.DebugLevel.INFO);
		}

		void displayComputedInfo() {
			Util.printf(" numHands:   " + this.modifiedSiteswap.siteswap.numHands(), Util.DebugLevel.INFO);
			Util.printf(" period:     " + this.modifiedSiteswap.siteswap.period(), Util.DebugLevel.INFO);
			if(this.printNumBalls) {
				Util.printf(" capacity:   " + this.modifiedSiteswap.siteswap.numBalls(), Util.DebugLevel.INFO);
			}
			if(this.printValidity) {
				Util.printf(" validity:   " + this.modifiedSiteswap.siteswap.isValid(), Util.DebugLevel.INFO);
			}
			if(this.printState) {
				Util.printf(" state:      " + this.notatedState, Util.DebugLevel.INFO);
			}
			if(this.printOrbits) {
				Util.printf(" orbits:", Util.DebugLevel.INFO);
				for(Siteswap orbit : this.modifiedSiteswap.siteswap.getOrbits()) {
					Util.printf(orbit.toString(), Util.DebugLevel.INFO);
				}
			}
			if(this.printDifficulty) {
				Util.printf(" difficulty: " + this.modifiedSiteswap.siteswap.difficulty(), Util.DebugLevel.INFO);
			}
			if(this.printPrimality) {
				Util.printf(" primality:  " + this.modifiedSiteswap.siteswap.isPrime(), Util.DebugLevel.INFO);
			}
		}

	}

	static class StateInputObject extends InputObject {
		private NotatedState notatedState;
		StateInputObject(int index) {
			super(index);
			this.isState = true;
		}
		void parseNotation() throws InvalidStateNotationException, IncompatibleNumberOfHandsException {
			try {
				this.notatedState = NotatedState.parse(this.inputNotation, this.numHands, this.startHand);
			} catch(IncompatibleNumberOfHandsException e) {
				throw e;
			}
		}

		void runComputations() {
			// first run modifications to siteswap
			this.modifiedSiteswap = this.notatedSiteswap.deepCopy();
			for(InputOption m : this.operations) {
				switch(m) {
					case ANTITOSSIFY:
						this.modifiedSiteswap.siteswap.antitossify();
						break;
					case SPRING:
						try {
							this.modifiedSiteswap = this.modifiedSiteswap.spring();
						} catch(SprungException e) {
							Util.printf(e.getMessage(), Util.DebugLevel.INFO);
						}
						break;
					default:
						break;
				}
			}
			// then compute modified state
			this.modifiedState = NotatedState.assembleAutomatic(new State(this.modifiedSiteswap.siteswap));
		}

		void displayInputInfo() {
			Util.printf("INPUT " + this.index + ":   '" + this.inputNotation + "'", Util.DebugLevel.INFO);
			Util.printf("---------", Util.DebugLevel.INFO);
			Util.printf(" parsed:     " + this.notatedState.state.toString(), Util.DebugLevel.INFO);
			Util.printf(" notated:    " + this.notatedState.print(), Util.DebugLevel.INFO);
			Util.printf("---------", Util.DebugLevel.INFO);
		}

		void displayComputedInfo() {
			Util.printf(" numHands:   " + this.modifiedSiteswap.siteswap.numHands(), Util.DebugLevel.INFO);
			Util.printf(" period:     " + this.modifiedSiteswap.siteswap.period(), Util.DebugLevel.INFO);
			if(this.printNumBalls) {
				Util.printf(" capacity:   " + this.modifiedSiteswap.siteswap.numBalls(), Util.DebugLevel.INFO);
			}
			if(this.printValidity) {
				Util.printf(" validity:   " + this.modifiedSiteswap.siteswap.isValid(), Util.DebugLevel.INFO);
			}
			if(this.printState) {
				Util.printf(" siteswap:      " + this.notatedSiteswap, Util.DebugLevel.INFO);
			}
			if(this.printOrbits) {
				Util.printf(" orbits:", Util.DebugLevel.INFO);
				for(Siteswap orbit : this.modifiedSiteswap.siteswap.getOrbits()) {
					Util.printf(orbit.toString(), Util.DebugLevel.INFO);
				}
			}
			if(this.printDifficulty) {
				Util.printf(" difficulty: " + this.modifiedSiteswap.siteswap.difficulty(), Util.DebugLevel.INFO);
			}
			if(this.printPrimality) {
				Util.printf(" primality:  " + this.modifiedSiteswap.siteswap.isPrime(), Util.DebugLevel.INFO);
			}
		}
	}

	static class CommandObject {

		// inputs
		InputObject[] inputs = new InputObject[2];
		int numInputs;

		// transition options
		// [list of args]
		List<String> transitionArgs = new LinkedList<String>();
		// [actual settings]
		int minTransitionLength = 0;
		int maxTransitions = -1;
		boolean displayGeneralTransition = false;
		boolean allowExtraSqueezeCatches = false;
		boolean generateBallAntiballPairs = false;
		boolean unAntitossifyTransitions = false;

		// output objects
		CompatibleNotatedSiteswapPair inputPatterns; // for parsing inputs
		CompatibleNotatedSiteswapPair modifiedInputPatterns; // for computing transition
		ContextualizedNotatedTransitionList transitions;

		// assemble a new command object from a list of cmdline args
		CommandObject(String[] args) throws SiteswapException {
			this.numInputs = 0;
			// first assemble any transition options,
			// then assemble input objects
			for(int i=0; i<args.length; i++) {
				String arg = args[i];
				// assemble input objects
				if(arg.equals("-i")) {
					inputs[this.numInputs] = new SiteswapInputObject(this.numInputs);
					this.numInputs++;
				} else if(arg.equals("-I")) {
					inputs[this.numInputs] = new StateInputObject(this.numInputs);
					this.numInputs++;
				} else {
					if(this.numInputs == 0) {
						// add transition option
						this.transitionArgs.add(arg);
					} else {
						// pass args to most recent input object
						inputs[this.numInputs - 1].addArg(arg);
					}
				}
			}
			// parse all args and set all settings accordingly
			// parse transition args, if any
			this.parseGlobalArgs();
			// parse args of each input object
			this.inputs[0].parseArgs();
			if(this.numInputs == 2) {
				this.inputs[1].parseArgs();
			}
		}

		void parseGlobalArgs() throws ParseError {
			int i = 0;
			String str;
			int intArg = 0;
			GlobalOption opt;
			while(i < this.transitionArgs.size()) {
				str = transitionArgs.get(i);
				opt = GlobalOption.fromStr(str);
				if(opt.requiresParam) {
					if(i + 1 < this.transitionArgs.size()) {
						try {
							intArg = Integer.parseInt(this.transitionArgs.get(i+1));
						} catch(NumberFormatException e) {
							throw new ParseError("option `" + str + "' requires integer argument; got `" + this.transitionArgs.get(i+1) + "'");
						}
						i++;
					} else {
						throw new ParseError("option `" + str + "' requires integer argument");
					}
				}
				switch(opt) {
					case ENABLE_DEBUG:
						Util.debugLevel = Util.DebugLevel.DEBUG;
						break;
					case MIN_TRANSITION_LENGTH:
						this.minTransitionLength = intArg;
						break;
					case MAX_TRANSITIONS:
						this.maxTransitions = intArg;
						break;
					case DISPLAY_GENERAL_TRANSITION:
						this.displayGeneralTransition = true;
						break;
					case ALLOW_EXTRA_SQUEEZE_CATCHES:
						this.allowExtraSqueezeCatches = true;
						break;
					case GENERATE_BALL_ANTIBALL_PAIRS:
						this.generateBallAntiballPairs = true;
						break;
					case UN_ANTITOSSIFY_TRANSITIONS:
						this.unAntitossifyTransitions = true;
						break;
					default:
						throw new ParseError("unrecognized transition option: `" + str + "'");
				}
				i++;
			}
		}

		// parse input notation, create siteswap/state objects, apply operations, find transition(s)
		void execute() throws InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException, ImpossibleTransitionException {
			switch(this.numInputs) {
				case 0:
					break;
				case 1:
					try {
						// parse input notations
						this.inputs[0].parseNotation();
						// run siteswap operations
						this.inputs[0].runComputations();
					} catch(InvalidNotationException | IncompatibleNumberOfHandsException e) {
						throw e;
					}
					break;
				case 2:
					try {
						CompatibleNotatedSiteswapPair.Candidate[] candidates = new CompatibleNotatedSiteswapPair.Candidate[2];
						for(int i=0; i<2; i++) {
							if(this.inputs[i].isState) {
								candidates[i] = CompatibleNotatedSiteswapPair.Candidate.fromState(this.inputs[i].inputNotation, 1, this.inputs[i].numHands, this.inputs[i].startHand);
							} else {
								candidates[i] = CompatibleNotatedSiteswapPair.Candidate.fromSiteswap(this.inputs[i].inputNotation, this.inputs[i].numHands, this.inputs[i].startHand);
							}
						}
						// parse notations in a compatible way, generate siteswaps/states
						this.inputPatterns = new CompatibleNotatedSiteswapPair(candidates[0], candidates[1]);
					} catch(InvalidSiteswapNotationException | IncompatibleNotationException | IncompatibleNumberOfHandsException e) {
						throw e;
					}
					// gather results
					this.inputs[0].notatedSiteswap = this.inputPatterns.prefix;
					this.inputs[0].notatedState = this.inputPatterns.from;
					this.inputs[1].notatedSiteswap = this.inputPatterns.suffix;
					this.inputs[1].notatedState = this.inputPatterns.to;
					// run siteswap operations
					this.inputs[0].runComputations();
					this.inputs[1].runComputations();
					// see if resulting patterns have compatible notations
					try {
						this.modifiedInputPatterns = new CompatibleNotatedSiteswapPair(this.inputs[0].modifiedSiteswap, this.inputs[1].modifiedSiteswap);
					} catch(IncompatibleNumberOfHandsException e) {
						throw e;
					}
					// compute transitions between resulting patterns
					try {
						this.transitions = new ContextualizedNotatedTransitionList(this.modifiedInputPatterns, this.minTransitionLength, this.maxTransitions, this.allowExtraSqueezeCatches, this.generateBallAntiballPairs);
					} catch(ImpossibleTransitionException e) {
						throw e;
					}
					break;
			}
		}

		// show results of computation
		void displayOutput() throws ImpossibleTransitionException {
			for(int i=0; i<numInputs; i++) {
				this.inputs[i].displayInfo();
				Util.printf("==========", Util.DebugLevel.INFO);
			}
			switch(this.numInputs) {
				case 0:
				case 1:
					break;
				case 2:
					if(this.displayGeneralTransition) {
						Util.printf("General Form of Transition:", Util.DebugLevel.INFO);
						Util.printf(transitions.printGeneralTransition(), Util.DebugLevel.INFO);
					}
					if(this.maxTransitions != 0) {
						if(this.maxTransitions != -1)
							Util.printf("Transitions (first " + this.maxTransitions + "):", Util.DebugLevel.INFO);
						else
							Util.printf("Transitions:", Util.DebugLevel.INFO);
						// print transition info based on transition flags
						for(int t=0; t<this.transitions.transitionList().size(); t++) {
							if(this.maxTransitions != -1 && t > this.maxTransitions)
								break;
							Util.printf(this.transitions.transitionList().get(t).print(), Util.DebugLevel.INFO);
							if(this.unAntitossifyTransitions)
								Util.printf(this.transitions.unAntitossifiedTransitionList().get(t).print(), Util.DebugLevel.INFO);
						}
					}
					break;
				default:
					Util.printf("ERROR: I don't know what to do with more than 2 inputs!", Util.DebugLevel.INFO);

			}
		}
	}

	public static void main(String[] args) {
		try {
			CommandObject command = new CommandObject(args);
			command.execute();
			command.displayOutput();
		} catch(SiteswapException e) {
			Util.printf(e.getMessage(), Util.DebugLevel.INFO);
		}
	}
}
