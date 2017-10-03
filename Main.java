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

	// cmdline tokens
	static enum Argument {
		// global options
		ENABLE_DEBUG('d', "debug", ArgumentType.TAKES_OWN_OPTIONS),
		// input indicator
		INPUT('i', "input", ArgumentType.TAKES_OWN_OPTIONS),
		// input options
		NUM_HANDS('h', "numHands", ArgumentType.REQUIRES_INT),
		START_HAND('H', "startHand", ArgumentType.REQUIRES_INT),
		KEEP_ZEROES('z', "keepZeroes", ArgumentType.FLAG),
		// info items
		INFO(null, "info", ArgumentType.TAKES_OWN_OPTIONS),
		CAPACITY('c', "capacity", ArgumentType.FLAG),
		VALIDITY('v', "validity", ArgumentType.FLAG),
		PRIMALITY('P', "primality", ArgumentType.FLAG),
		DIFFICULTY('d', "difficulty", ArgumentType.FLAG),
		// siteswap operations
		OPS(null, "ops", ArgumentType.TAKES_OWN_OPTIONS),
		INVERT('V', "invert", ArgumentType.FLAG),
		SPRING('p', "spring", ArgumentType.FLAG),
		INFINITIZE('f', "infinitize", ArgumentType.FLAG),
		UNINFINITIZE('F', "unInfinitize", ArgumentType.FLAG),
		ANTITOSSIFY('a', "antitossify", ArgumentType.FLAG),
		UNANTITOSSIFY('A', "unAntitossify", ArgumentType.FLAG),
		ANTINEGATE('N', "antiNegate", ArgumentType.FLAG),
		// 'big' operations
		TO_SITESWAP('S', "siteswap", ArgumentType.TAKES_OWN_OPTIONS),
		TO_STATE('s', "state", ArgumentType.FLAG),
		TRANSITION('T', "transition", ArgumentType.TAKES_OWN_OPTIONS),
		// transition options
		MIN_TRANSITION_LENGTH('l', "minTransitionLength", ArgumentType.REQUIRES_INT),
		MAX_TRANSITIONS('m', "maxTransitions", ArgumentType.REQUIRES_INT),
		ALLOW_EXTRA_SQUEEZE_CATCHES('q', "allowExtraSqueezeCatches", ArgumentType.FLAG),
		GENERATE_BALL_ANTIBALL_PAIRS('g', "generateBallAntiballPairs", ArgumentType.FLAG),
		UN_ANTITOSSIFY_TRANSITIONS('A', "unAntitossifyTransitions", ArgumentType.FLAG),
		DISPLAY_GENERAL_TRANSITION('G', "displayGeneralTransition", ArgumentType.FLAG),
		// invalid
		INVALID_TOKEN(null, null, null);

		// FOR LATER...
		static enum ArgumentType {
			FLAG,
			REQUIRES_INT,
			TAKES_OWN_OPTIONS;
		}

		// fields
		Character shortForm;
		String longForm;
		ArgumentType type;
		// constructor
		private Argument(Character shortForm, String longForm, ArgumentType type) {
			this.shortForm = shortForm;
			this.longForm = longForm;
			this.type = type;
		}
		// public acess to constructor
		static Argument fromStr(String str) {
			for(Argument opt : Argument.values()) {
				if(str.equals(opt.shortForm) || str.equals(opt.longForm)) {
					return opt;
				}
			}
			return INVALID_TOKEN;
		}
	}

	static class Command {
		List<Chain> chains;

		Command(String[] args) throws ParseError {
			this.chains = new ArrayList<>();
			this.parseArgs(args);
		}

		void parseArgs(String[] args) throws ParseError {
			boolean expectingNotation = false;
			for(String str : args) {
				if(expectingNotation) {
					this.newSingleInputChain(str);
					expectingNotation = false;
					continue;
				}
				Argument arg = Argument.fromStr(str);
				switch(arg) {
					case INPUT:
						expectingNotation = true;
						break;
					case TRANSITION:
						if(this.chains.size() < 2) {
							throw new ParseError("need at least two inputs to compute a transition");
						}
						// create new chain for the transition
						Link from = this.chains.get(-2).getLastLink();
						Link to = this.chains.get(-1).getLastLink();
						this.newDoubleInputChain(from, to);
						break;
					case INVALID_TOKEN:
						throw new ParseError("invalid token: '" + str + "'");
					default:
						if(this.chains.size() == 0) {
							throw new ParseError("need input");
						}
						// pass argument to most recent chain
						this.chains.get(-1).parseArg(str);
						break;
				}
			}
		}

		Chain newSingleInputChain(String str) {
			Chain ret = new Chain(str);
			this.chains.add(ret);
			return ret;
		}

		Chain newDoubleInputChain(Chain from, Chain to) {
			Chain ret = new Chain(from, to);
			this.chains.add(ret);
			return ret;
		}

		class Link {
			boolean isState;
			Siteswap siteswap;
			State state;
		}

		class InputLink extends Link {
			String notation;
		}

		class Chain {
			Link[] inputs;
			List<Link> links;

			// for literal input
			Chain(String notation) {
				this.inputs = new Link[1];
				this.links = new ArrayList<>();
			}

			// for transitions
			Chain(Chain from, Chain to) {
				this.inputs = new Link[2];
			}

			Link getLastLink() {
				return this.links.get(-1);
			}

			void parseArg(String arg) {
			}

		}

		void execute() {
		}

	}

	public static void main(String[] args) {
		Command command = null;
		try {
			command = new Command(args);
			command.execute();
		} catch(SiteswapException e) {
			Util.printf(e.getMessage(), Util.DebugLevel.ERROR);
		}
	}

}
