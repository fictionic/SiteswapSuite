package siteswapsuite;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

class Command {

	List<Chain> chains;

	public Command(ArgTree argTree) {
		this.chains = new ArrayList<>();
		// deal with global args
		Util.printf("adding global args", Util.DebugLevel.DEBUG);
		for(ArgWithOptions globalArg : argTree.globalArgs) {
			if(globalArg.head.arg == Argument.ENABLE_DEBUG) {
				Util.debugClasses = new HashSet<>();
				for(ArgWithFollowUp debugArg : globalArg.tail) {
					switch(debugArg.arg) {
						case ENABLE_DEBUG_MAIN:
							Util.debugClasses.add(Main.class.getName());
							Util.debugClasses.add(Command.class.getName());
							Util.debugClasses.add(ArgTree.class.getName());
							break;
						case ENABLE_DEBUG_SITESWAP:
							Util.debugClasses.add(Siteswap.class.getName());
							break;
						case ENABLE_DEBUG_STATE:
							Util.debugClasses.add(State.class.getName());
							break;
						case ENABLE_DEBUG_TRANSITION:
							Util.debugClasses.add(TransitionFinder.class.getName());
							Util.debugClasses.add(TransitionResults.class.getName());
							break;
						default:
							throw new Error("impossible error in Command.fromArgTree");
					}
				}
			}
		}
		// construct chains
		for(ArgTree.ArgChain argChain : argTree.argChains) {
			this.addChain(argChain);
		}
	}

	Chain getChain(int index) {
		if(index < 0) {
			index += this.chains.size();
		}
		return this.chains.get(index);
	}

	void addChain(ArgTree.ArgChain argChain) {
		this.chains.add(new Chain(argChain));
	}

	// parse transition options, removing them from the input list as we go
	// (so the caller doesn't have to scan through them again and can get to the
	// ones that don't get put in a TransitionOptions)
	static TransitionOptions readTransitionArgs(List<ArgWithFollowUp> transitionArgs) {
		TransitionOptions transitionOptions = new TransitionOptions();
		for(int i=0; i<transitionArgs.size(); i++) {
			ArgWithFollowUp transitionArg = transitionArgs.get(i);
			switch(transitionArg.arg) {
				case MIN_TRANSITION_LENGTH:
					transitionOptions.minTransitionLength = transitionArg.followUpInt;
					transitionArgs.remove(i--);
					break;
				case MAX_TRANSITIONS:
					transitionOptions.maxTransitions = transitionArg.followUpInt;
					transitionArgs.remove(i--);
					break;
				case ALLOW_EXTRA_SQUEEZE_CATCHES:
					transitionOptions.allowExtraSqueezeCatches = true;
					transitionArgs.remove(i--);
					break;
				case GENERATE_BALL_ANTIBALL_PAIRS:
					transitionOptions.generateBallAntiballPairs = true;
					transitionArgs.remove(i--);
					break;
				case SELECT_TRANSITION:
					transitionOptions.selectTransition = transitionArg.followUpInt;
					transitionArgs.remove(i--);
					break;
				default:
					break;
			}
		}
		return transitionOptions;
	}

	public String printTransitionOptions(TransitionOptions transitionOptions) {
		StringBuilder ret = new StringBuilder();
		if(transitionOptions.minTransitionLength != -1) {
			ret.append(Argument.MIN_TRANSITION_LENGTH);
			ret.append("=");
			ret.append(transitionOptions.minTransitionLength);
			ret.append(",");
		}
		if(transitionOptions.maxTransitions != -1) {
			ret.append(Argument.MAX_TRANSITIONS);
			ret.append("=");
			ret.append(transitionOptions.maxTransitions);
			ret.append(",");
		}
		if(transitionOptions.allowExtraSqueezeCatches) {
			ret.append(Argument.ALLOW_EXTRA_SQUEEZE_CATCHES);
			ret.append(",");
		}
		if(transitionOptions.generateBallAntiballPairs) {
			ret.append(Argument.GENERATE_BALL_ANTIBALL_PAIRS);
			ret.append(",");
		}
		if(transitionOptions.selectTransition != -1) {
			ret.append(Argument.SELECT_TRANSITION);
			ret.append("=");
			ret.append(transitionOptions.selectTransition);
			ret.append(",");
		}
		ret.deleteCharAt(ret.length() - 1);
		return ret.toString();
	}

	class Chain {
		int index;
		ChainInput input;
		List<Link> links;

		Chain(ArgTree.ArgChain argChain) {
			this.index = chains.size();
			Util.printf("creating chain #" + this.index, Util.DebugLevel.DEBUG);
			this.input = new ChainInput(argChain.input);
			this.links = new ArrayList<>();
			// add links
			for(ArgTree.ArgChain.ArgLink argLink : argChain.argLinks) {
				this.links.add(new Link(argLink));
			}

		}

		class ChainInput {
			boolean isTransition;
			NotatedSiteswapOrState notatedSiteswapOrState;
			boolean isState; // cuz we can't create a ^nssors until we have a notatedobject

			// literal input stuff
			String prefix;
			String inputNotation;
			int numHands = -1;
			int startHand = -1;
			boolean keepZeroes = false;

			// transition input stuff
			int fromIndex = -1, toIndex = -1;
			TransitionOptions transitionOptions;
			boolean displayGeneralizedTransition = false;
			boolean unAntitossifyTransitions = false;
			List<Siteswap> transitions;
			GeneralizedTransition generalizedTransition;

			ChainInput(ArgWithOptions input) {
				if(input.head.arg == Argument.INPUT) {
					this.isTransition = false;
					this.inputNotation = input.head.followUpString;
					// check for notation type indicators
					if(this.inputNotation.length() > 2 && this.inputNotation.charAt(2) == ':') {
						this.prefix = this.inputNotation.substring(0,2);
						this.inputNotation = this.inputNotation.substring(3,this.inputNotation.length());
						if(this.prefix.equals("ss")) {
							this.isState = false;
						} else if(this.prefix.equals("st")) {
							this.isState = true;
						} else {
							// Util.ErrorOut(new InvalidNotationException("unrecognized input prefix: " + this.prefix));
							Util.ErrorOut(new InvalidNotationException());
						}
					}
					// parse input options
					for(ArgWithFollowUp inputArg : input.tail) {
						switch(inputArg.arg) {
							case NUM_HANDS:
								this.numHands = inputArg.followUpInt;
								break;
							case START_HAND:
								this.startHand = inputArg.followUpInt;
								break;
							case KEEP_ZEROES:
								this.keepZeroes = true;
								break;
							default:
								assert false;
								break;
						}
					}
				} else {
					this.isTransition = true;
					this.fromIndex = index - 2;
					this.toIndex = index - 1;
					// get general transition options
					this.transitionOptions = Command.readTransitionArgs(input.tail);
					// get other transition options
					for(ArgWithFollowUp transitionArg : input.tail) {
						switch(transitionArg.arg) {
							case FROM_INDEX:
								this.fromIndex = transitionArg.followUpInt;
								break;
							case TO_INDEX:
								this.toIndex = transitionArg.followUpInt;
								break;
							case DISPLAY_GENERAL_TRANSITION:
								this.displayGeneralizedTransition = true;
								break;
							case UNANTITOSSIFY:
								this.unAntitossifyTransitions = true;
								break;
							default:
								break;
						}
					}
				}
			}

			void process() {
				Util.printf("processing input #" + index, Util.DebugLevel.DEBUG);
				if(this.isTransition) {
					// make sure indeces are valid
					if(this.fromIndex >= index || this.toIndex >= index) {
						Util.ErrorOut(new ParseError("transition to/from indeces must refer to previous inputs"));
					}
					Link l1 = getChain(this.fromIndex).getLastLink(), l2 = getChain(this.toIndex).getLastLink();
					State from = l1.siteswapOrState.getState();
					State to = l2.siteswapOrState.getState();
					if(from.numHands() != to.numHands()) {
						Util.ErrorOut(new IncompatibleNumberOfHandsException());
					}
					try {
						TransitionFinder tf = new TransitionFinder(from, to);
						TransitionResults tr = tf.findTransitions(this.transitionOptions);
						if(this.unAntitossifyTransitions) {
							Util.printf("WARNING: un-antitossify-transitions not yet implemented", Util.DebugLevel.ERROR);
							// tr.unAntitossify(prefix, suffix);
						}
						this.generalizedTransition = tr.getGeneralizedTransition();
						this.transitions = tr.getTransitions();
						// save one as output, notated
						// first get notation info from input of left input chain
						NotatedSiteswapOrState inputObject = getChain(this.fromIndex).input.notatedSiteswapOrState;
						NotatedSiteswap outputNotatedSiteswap = NotatedSiteswap.assemble(tr.getSelectedTransition(), inputObject.notationType(), inputObject.startHand());
						this.notatedSiteswapOrState = new NotatedSiteswapOrState(outputNotatedSiteswap);
					} catch(ImpossibleTransitionException e) {
						Util.ErrorOut(e);
					}
				} else {
					try {
						if(this.prefix != null) {
							// if state or siteswap was explicitly indicated, parse accordingly
							if(this.isState) {
								this.notatedSiteswapOrState = new NotatedSiteswapOrState(NotatedState.parse(this.inputNotation, this.numHands, this.startHand));
							} else {
								this.notatedSiteswapOrState = new NotatedSiteswapOrState(NotatedSiteswap.parse(this.inputNotation, this.numHands, this.startHand));
							}
						} else {
							// otherwise try both
							try {
								this.notatedSiteswapOrState = new NotatedSiteswapOrState(NotatedSiteswap.parse(this.inputNotation, this.numHands, this.startHand));
							} catch(InvalidSiteswapNotationException e) {
								try {
									this.notatedSiteswapOrState = new NotatedSiteswapOrState(NotatedState.parse(this.inputNotation, this.numHands, this.startHand));
								} catch(InvalidStateNotationException e2) {
									// throw new InvalidNotationException("could not interpret input '" + this.inputNotation + "' as valid siteswap or state notation");
									throw new InvalidNotationException();
								}
							}
						}
					} catch(InvalidNotationException e) {
						Util.ErrorOut(e);
					}
				}
			}

			public String display() {
				StringBuilder ret = new StringBuilder("INPUT "); ret.append(index); ret.append(":\n");
				if(this.isTransition) {
					ret.append(" type: transition\n");
					ret.append(" from: output "); ret.append(this.fromIndex); ret.append("\n");
					ret.append(" to: output "); ret.append(this.toIndex); ret.append("\n");
					if(this.displayGeneralizedTransition) {
						ret.append(" generalized transition: ");
						NotatedSiteswapOrState inputObject = getChain(this.fromIndex).input.notatedSiteswapOrState;
						NotatedSiteswap throwsPortion = NotatedSiteswap.assemble(this.generalizedTransition.getThrowsPortion(), inputObject.notationType(), inputObject.startHand());
						ret.append(throwsPortion.display());
						NotatedSiteswap catchesPortion = NotatedSiteswap.assemble(this.generalizedTransition.getCatchesPortion(), inputObject.notationType(), inputObject.startHand());
						ret.append("{");
						ret.append(catchesPortion.display());
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

		void addLink(ArgTree.ArgChain.ArgLink argLink) {
			this.links.add(new Link(argLink));
		}

		Link getLastLink() {
			return this.links.get(this.links.size()-1);
		}

		class Link {
			SiteswapOrState siteswapOrState;
			ArgWithOptions operation;
			List<ArgWithFollowUp> infos;

			Link() {
				this.infos = new ArrayList<>();
			}

			Link(ArgTree.ArgChain.ArgLink argLink) {
				this();
				this.operation = argLink.operation;
				this.infos = argLink.infoArgs;
			}

			String display() {
				StringBuilder ret = new StringBuilder();
				if(this.operation != null) {
					ret.append("---> " + this.operation.toString() + " ");
				}
				if(this.siteswapOrState.isState) {
					ret.append("---> state:\n");
					ret.append(" parsed: " + this.siteswapOrState.state.toString() + "\n");
				} else {
					ret.append("---> siteswap:\n");
					ret.append(" parsed: " + this.siteswapOrState.siteswap.toString() + "\n");
				}
				for(ArgWithFollowUp infoArg : this.infos) {
					switch(infoArg.arg) {
						case CAPACITY:
							ExtendedFraction capacity;
							if(this.siteswapOrState.isState) {
								capacity = this.siteswapOrState.state.numBalls();
							} else {
								capacity = this.siteswapOrState.siteswap.numBalls();
							}
							ret.append(" capacity: " + capacity.toString() + "\n");
							break;
						case VALIDITY:
							boolean validity;
							if(this.siteswapOrState.isState) {
								validity = (this.siteswapOrState.state.repeatedLength == 0);
							} else {
								validity = this.siteswapOrState.siteswap.isValid();
							}
							ret.append(" validity: " + validity + "\n");
							break;
						case PRIMALITY:
							boolean primality;
							if(this.siteswapOrState.isState) {
								ret.append(" primality: n/a\n");
							} else {
								ret.append(" primality: " + this.siteswapOrState.siteswap.isPrime() + "\n");
							}
							break;
						case DIFFICULTY:
							ExtendedFraction difficulty;
							if(this.siteswapOrState.isState) {
								ret.append(" difficulty: n/a\n");
							} else {
								ret.append(" difficulty: " + this.siteswapOrState.siteswap.difficulty().toString() + "\n");
							}
							break;
						default:
							break;
					}
				}
				return ret.toString();
			}

		}

		class SiteswapOrState {
			boolean isState;
			Siteswap siteswap;
			State state;
			SiteswapOrState(State state) {
				this.isState = true;
				this.state = state;
			}
			SiteswapOrState(Siteswap siteswap) {
				this.isState = false;
				this.siteswap = siteswap;
			}
			State getState() {
				if(this.isState) {
					return this.state;
				} else {
					return new State(this.siteswap);
				}
			}
			SiteswapOrState deepCopy() {
				if(this.isState) {
					return new SiteswapOrState(this.state.deepCopy());
				} else {
					return new SiteswapOrState(this.siteswap.deepCopy());
				}
			}
		}

		class NotatedSiteswapOrState {
			boolean isState;
			NotatedSiteswap notatedSiteswap;
			NotatedState notatedState;

			NotatedSiteswapOrState(NotatedSiteswap notatedSiteswap) {
				this.isState = false;
				this.notatedSiteswap = notatedSiteswap;
			}

			NotatedSiteswapOrState(NotatedState notatedState) {
				this.isState = true;
				this.notatedState = notatedState;
			}

			SiteswapOrState deNotate() {
				if(this.isState) {
					return new SiteswapOrState(this.notatedState.state);
				} else {
					return new SiteswapOrState(this.notatedSiteswap.siteswap);
				}
			}

			NotationType notationType() {
				if(this.isState) {
					return this.notatedState.type;
				} else {
					return this.notatedSiteswap.type;
				}
			}

			int startHand() {
				if(this.isState) {
					return this.notatedState.startHand;
				} else {
					return this.notatedSiteswap.startHand;
				}
			}

		}

		void execute() {
			Util.printf("executing chain #" + this.index, Util.DebugLevel.DEBUG);
			// process input
			this.input.process();
			// print input info
			Util.printf(this.input.display(), Util.DebugLevel.INFO, false);
			// compute all links
			// first link
			SiteswapOrState inputSiteswapOrState = this.input.notatedSiteswapOrState.deNotate();
			this.links.get(0).siteswapOrState = inputSiteswapOrState;
			Util.printf(this.links.get(0).display(), Util.DebugLevel.INFO, false);
			for(int i=1; i<this.links.size(); i++) {
				Link prevLink = this.links.get(i-1);
				Link curLink = this.links.get(i);
				// first copy prev link's ss-object into new link
				curLink.siteswapOrState = prevLink.siteswapOrState.deepCopy();
				if(curLink.siteswapOrState.isState) {
					switch(curLink.operation.head.arg) {
						case TO_SITESWAP:
							TransitionOptions transitionOptions = Command.readTransitionArgs(curLink.operation.tail);
							// see if any other transition options were passed
							for(int j=0; j<curLink.operation.tail.size(); j++) {
								ArgWithFollowUp illegalArg = curLink.operation.tail.get(j);
								Util.printf("WARNING: operation '" + curLink.operation.head.arg.helpString() + "' does not accept option '" + illegalArg.arg.helpString(true) + "'; ignoring");
								curLink.operation.tail.remove(j);
								j--;
							}
							// make minTransitionLength = 1 by default cuz you probably don't want an empty siteswap
							if(transitionOptions.minTransitionLength == -1) {
								transitionOptions.minTransitionLength = 1;
							}
							State prevState = prevLink.siteswapOrState.getState();
							try {
								TransitionFinder tf = new TransitionFinder(prevState, prevState);
								TransitionResults tr = tf.findTransitions(transitionOptions);
								curLink.siteswapOrState = new SiteswapOrState(tr.getSelectedTransition());
							} catch(ImpossibleTransitionException e) {
								Util.ErrorOut(e);
							}
							break;
						default:
							// TODO make a better exception class for this?
							Util.ErrorOut(new ParseError("operation '" + curLink.operation.head.arg.helpString() + "' cannot be applied to states"));
							break;
					}
				} else {
					switch(curLink.operation.head.arg) {
						case TO_STATE:
							curLink.siteswapOrState = new SiteswapOrState(curLink.siteswapOrState.getState());
							break;
						case INVERT:
							curLink.siteswapOrState.siteswap.invert();
							break;
						case SPRING:
							Util.printf("WARNING: sprung not yet implemented", Util.DebugLevel.ERROR);
							break;
						case INFINITIZE:
							curLink.siteswapOrState.siteswap.infinitize();
							break;
						case UNINFINITIZE:
							curLink.siteswapOrState.siteswap.unInfinitize();
							break;
						case ANTITOSSIFY:
							curLink.siteswapOrState.siteswap.antitossify();
							break;
						case UNANTITOSSIFY:
							curLink.siteswapOrState.siteswap.unAntitossify();
							break;
						default:
							// TODO make a better exception class for this?
							Util.ErrorOut(new ParseError("operation '" + curLink.operation.head.arg + "' cannot be applied to siteswaps"));
							break;
					}
				}
				// print link
				Util.printf(curLink.display(), Util.DebugLevel.INFO, false);
			}
			// get output
			StringBuilder ret = new StringBuilder();
			ret.append("OUTPUT: \n");
			Link lastLink = this.getLastLink();
			// get reference for notation output
			NotatedSiteswapOrState inputObject;
			if(this.input.isTransition) {
				inputObject = getChain(this.input.fromIndex).input.notatedSiteswapOrState;
			} else {
				inputObject = this.input.notatedSiteswapOrState;
			}
			if(lastLink.siteswapOrState.isState) {
				ret.append(" state: " );
				NotatedState notatedState = NotatedState.assemble(lastLink.siteswapOrState.state, inputObject.notationType(), inputObject.startHand());
				ret.append(notatedState.display());
			} else {
				ret.append(" siteswap: " );
				NotatedSiteswap notatedSiteswap = NotatedSiteswap.assemble(lastLink.siteswapOrState.siteswap, inputObject.notationType(), inputObject.startHand());
				ret.append(notatedSiteswap.display());
			}
			// print output
			Util.printf(ret.toString(), Util.DebugLevel.INFO);
		}

	}

	public void run() {
		for(Chain chain : this.chains) {
			chain.execute();
		}
	}

}
