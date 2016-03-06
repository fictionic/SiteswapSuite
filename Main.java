package siteswapsuite;

import java.util.List;
import java.util.LinkedList;

public class Main {

	static void printf(Object input) { System.out.println(input); }


	static class ParseError extends SiteswapException {
		String message;
		ParseError(String message) {
			this.message = "ERROR: " + message;
		}
		public String getMessage() {
			return this.message;
		}
	}

	static String parseIntFromArg(String arg, int c) {
		int startIndex = c;
		scan:
		while(c < arg.length()) {
			switch(arg.charAt(c)) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					c++;
					break;
				default:
					break scan;
			}
		}
		return arg.substring(startIndex,c);
	}

	static class InputObject {
		String inputNotation;
		NotatedSiteswap siteswap;
		State state;
		NotatedSiteswap modifiedSiteswap;
		State modifiedState;
		boolean isState;
		// hand specification
		int minSSLength = 1;
		int startHand = 0;
		int numHands = -1; // inferred from input notations unless specified
		// info printing options
		boolean printNumBalls = false;
		boolean printState = false;
		boolean printDifficulty = false;
		boolean printValidity = false;
		boolean printPrimality = false;
		// siteswap operation sequence to be performed
		List<SiteswapOperation> operations;

		static enum SiteswapOperation {
			INVERSE, SPRUNG, INFINITIZE, UNINFINITIZE, ANTITOSSIFY, UNANTITOSSIFY, ANTINEGATE;
		}

		InputObject(String arg, String input) throws ParseError {
			this.inputNotation = input;
			int c = 2; // index of char in input arg
			if(arg.charAt(1) == 'i') {
				this.isState = false;
			} else {
				this.isState = true;
				// get minSSLength, if present
				if(arg.length() > 2) {
					String strInt = parseIntFromArg(arg, 2);
					if(strInt.length() > 0) {
						this.minSSLength = Integer.parseInt(strInt);
						c += strInt.length();
					}
				}
			}
			this.operations = new LinkedList<SiteswapOperation>();
			this.parseInputShortOptionsFromIndex(arg, c);
		}

		private void parseInputShortOptionsFromIndex(String arg, int c) throws ParseError {
			// parse rest of input args in this block
			String strInt;
			while(c < arg.length()) {
				switch(arg.charAt(c)) {
					// error checking
					case 'i':
					case 'I':
						throw new ParseError("cannot bundle another -i or -I inside option cluster");
					// hand specification
					case 'h':
						strInt = parseIntFromArg(arg, c+1);
						if(strInt.length() == 0)
							throw new ParseError("option -h requires integer argument");
						else {
							this.startHand = Integer.parseInt(strInt);
							c += strInt.length() + 1;
						}
						break;
					case 'H':
						strInt = parseIntFromArg(arg, c+1);
						if(strInt.length() == 0)
							throw new ParseError("option -H requires integer argument");
						else {
							this.numHands = Integer.parseInt(strInt);
							c += strInt.length() + 1;
						}
						break;
					// siteswap operations
					case 'V':
						this.operations.add(SiteswapOperation.INVERSE);
						c++;
						break;
					case 'p':
						this.operations.add(SiteswapOperation.SPRUNG);
						c++;
						break;
					case 'f':
						this.operations.add(SiteswapOperation.INFINITIZE);
						c++;
						break;
					case 'F':
						this.operations.add(SiteswapOperation.UNINFINITIZE);
						c++;
						break;
					case 'a':
						this.operations.add(SiteswapOperation.ANTITOSSIFY);
						c++;
						break;
					case 'A':
						this.operations.add(SiteswapOperation.UNANTITOSSIFY);
						c++;
						break;
					case 'N':
						this.operations.add(SiteswapOperation.ANTINEGATE);
						c++;
						break;
					// info printing flags
					case 'b':
						this.printNumBalls = true;
						c++;
						break;
					case 's':
						this.printState = true;
						c++;
						break;
					case 'd':
						this.printDifficulty = true;
						c++;
						break;
					case 'v':
						this.printValidity = true;
						c++;
						break;
					case 'P':
						this.printPrimality = true;
						c++;
						break;
					default:
						throw new ParseError("invalid character in input short argument block: '" + arg.charAt(c) + "'");
				}
			}
		}

		void parseInputShortOptions(String arg) throws ParseError {
			this.parseInputShortOptionsFromIndex(arg, 1);
		}

		void parseNotation() throws InvalidNotationException, IncompatibleNumberOfHandsException {
			try {
				this.siteswap = NotatedSiteswap.parseSingle(this.inputNotation, this.numHands, this.startHand);
			} catch(InvalidNotationException e) {
				throw e;
			}
		}

		void runModifications() {
			this.modifiedSiteswap = this.siteswap.deepCopy();
			for(SiteswapOperation m : this.operations) {
				switch(m) {
					case ANTITOSSIFY:
						this.modifiedSiteswap.antitossify();
						break;
					case SPRUNG:
						try {
							this.modifiedSiteswap = this.modifiedSiteswap.spring();
						} catch(SprungException e) {
							printf(e.getMessage());
						}
						break;
					default:
						break;
				}
			}
		}
		
		void computeState() {
			this.state = new State(this.siteswap);
			if(this.operations.size() > 0)
				this.modifiedState = new State(this.modifiedSiteswap);
			else
				this.modifiedState = this.state;
		}

		void displayInfo(int i) {
			printf("INPUT " + i + ":   '" + this.inputNotation + "'");
			// generate string representing operation sequence
			if(!this.operations.isEmpty()) {
				printf(" parsed:     " + this.siteswap.toString());
				printf(" de-parsed:  " + this.siteswap.print());
				printf("---------");
				String ops = "";
				int c = 0;
				for(SiteswapOperation o : operations) {
					switch(o) {
						case INVERSE:
							ops += "inverse";
							break;
						case SPRUNG:
							ops += "sprung";
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
				printf(" Modification Sequence: " + ops);
				printf("---------");
				printf("OUTPUT " + i + ":");
			}
			printf(" parsed:     " + this.modifiedSiteswap.toString());
			printf(" de-parsed:  " + this.modifiedSiteswap.print());
			printf(" numHands:   " + this.modifiedSiteswap.numHands());
			printf(" period:     " + this.modifiedSiteswap.period());
			if(this.printNumBalls)
				printf(" numBalls:   " + this.modifiedSiteswap.numBalls());
			if(this.printValidity)
				printf(" validity:   " + this.modifiedSiteswap.isValid());
			if(this.printState)
				printf(" state:      " + this.state);
			if(this.printDifficulty)
				printf(" difficulty: " + this.modifiedSiteswap.difficulty());
			if(this.printPrimality)
				printf(" primality:  " + this.modifiedSiteswap.isPrime());
		}

	}
	
	static class CommandObject {
		// inputs
		InputObject[] inputs = new InputObject[2];
		int numInputs;

		// transition options
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

		CommandObject(String[] args) throws ParseError, InvalidNotationException, IncompatibleNotationException, IncompatibleNumberOfHandsException, ImpossibleTransitionException {
			this.numInputs = 0;
			// parse args
			int i = 0;
			while(i < args.length) {
				String arg = args[i];
				if(arg.charAt(0) == '-') {
					switch(arg.charAt(1)) {
						case '-':
							throw new ParseError("long options not yet supported");
						case 'i':
						case 'I':
							if(i+1 < args.length) {
								try {
									inputs[this.numInputs] = new InputObject(args[i], args[i+1]);
									this.numInputs++;
									i += 2;
								} catch(ParseError e) {
									throw e;
								}
							} else {
								throw new ParseError("missing input notation");
							}
							break;
						default:
							if(this.numInputs > 0)
								try {
									inputs[this.numInputs - 1].parseInputShortOptions(arg);
								} catch(ParseError e) {
									throw e;
								}
							else
								this.parseCommandShortOptions(arg);
							i++;
							break;
					}
				} else {
					throw new ParseError("expected option, got '" + arg + "'");
				}
			}
			// parse input notations, but don't compute transitions yet!
			switch(this.numInputs) {
				case 0:
					break;
				case 1:
					try {
						this.inputs[0].parseNotation();
						this.inputs[0].runModifications();
						this.inputs[0].computeState();
					} catch(InvalidNotationException | IncompatibleNumberOfHandsException e) {
						throw e;
					}
					break;
				case 2:
					try {
						this.inputPatterns = new CompatibleNotatedSiteswapPair(this.inputs[0].inputNotation, this.inputs[0].numHands, this.inputs[0].startHand, this.inputs[1].inputNotation, this.inputs[1].numHands, this.inputs[1].startHand);
					} catch(InvalidNotationException | IncompatibleNotationException | IncompatibleNumberOfHandsException e) {
						throw e;
					}
					this.inputs[0].siteswap = this.inputPatterns.prefix;
					this.inputs[0].runModifications();
					this.inputs[0].computeState();
					this.inputs[1].siteswap = this.inputPatterns.suffix;
					this.inputs[1].runModifications();
					this.inputs[1].computeState();
					try {
						this.modifiedInputPatterns = new CompatibleNotatedSiteswapPair(this.inputs[0].modifiedSiteswap, this.inputs[1].modifiedSiteswap);
					} catch(IncompatibleNumberOfHandsException e) {
						throw e;
					}
					break;
			}
		}

		void parseCommandShortOptions(String arg) throws ParseError {
			String strInt;
			int c = 1;
			while(c < arg.length()) {
				switch(arg.charAt(c)) {
					// options that require integer argument
					case 'l':
						// minimum length of transition
						strInt = parseIntFromArg(arg, c+1);
						if(strInt.length() == 0)
							throw new ParseError("option -l requires integer argument");
						else {
							this.minTransitionLength = Integer.parseInt(strInt);
							c += strInt.length() + 1;
						}
						break;
					case 'm':
						// maximum number of transitions to compute
						strInt = parseIntFromArg(arg, c+1);
						if(strInt.length() == 0)
							throw new ParseError("option -m requires integer argument");
						else {
							this.maxTransitions = Integer.parseInt(strInt);
							c += strInt.length() + 1;
						}
						break;
					// what type of transition to compute
					case 'q':
						this.allowExtraSqueezeCatches = true;
						c++;
						break;
					case 'g':
						this.generateBallAntiballPairs = true;
						c++;
						break;
					case 'A':
						this.unAntitossifyTransitions = true;
						c++;
						break;
					case 'G':
						this.displayGeneralTransition = true;
						c++;
						break;
					default:
						throw new ParseError("invalid character in transition short option block: '" + arg.charAt(c) + "'");
				}
			}
		}

		void displayOutput() throws ImpossibleTransitionException {
			for(int i=0; i<numInputs; i++) {
				this.inputs[i].displayInfo(i);
				printf("==========");
			}
			switch(this.numInputs) {
				case 0:
				case 1:
					break;
				case 2:
					try {
						this.transitions = new ContextualizedNotatedTransitionList(this.modifiedInputPatterns, this.minTransitionLength, this.maxTransitions, this.allowExtraSqueezeCatches, this.generateBallAntiballPairs);
						if(this.displayGeneralTransition) {
							printf("General Form of Transition:");
							printf(transitions.printGeneralTransition());
						}
						if(this.maxTransitions != 0) {
							if(this.maxTransitions != -1)
								printf("Transitions (first " + this.maxTransitions + "):");
							else
								printf("Transitions:");
							// print transition info based on transition flags
							for(int t=0; t<this.transitions.transitionList().size(); t++) {
								if(this.maxTransitions != -1 && t > this.maxTransitions)
									break;
								printf(this.transitions.transitionList().get(t).print());
								if(this.unAntitossifyTransitions)
									printf(this.transitions.unAntitossifiedTransitionList().get(t).print());
							}
						}
					} catch(ImpossibleTransitionException e) {
						throw e;
					}
					break;
				default:
					printf("ERROR: I don't know what to do with more than 2 inputs!");

			}
		}
	}

	public static void main(String[] args) {
		try {
			CommandObject command = new CommandObject(args);
			command.displayOutput();
		} catch(SiteswapException e) {
			printf(e.getMessage());
		}
	}
}
