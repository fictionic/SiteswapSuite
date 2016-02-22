package siteswapsuite;

import java.util.List;

public class Main {

	static void printf(Object input) { System.out.println(input); }

	/*

	   <cmd> = <info_opts> <input> | <t_opts> <input> <input>
	   <input> = <siteswap_input> | <state_input>
	   <siteswap_input> = "-i"<input_opts_short> <siteswap_notation>( <input_opts>)*
	   <state_input> = "-I"<num><input_opts_short> <state_notation>( <input_opts>)*
	   <input_opts> = <input_opts_short> | <input_opts_long>
	   <input_opts_short> = <numHands_short> | <startHand_short> | <ss_operation_short>
	   <ss_operation_short> = 

	 */

	static enum Option {
		String longName;
		char shortName;
		Option(String longName, char shortName) {
			this.longName = longName;
			this.shortName = shortName;
		}

		static class TransitionOption extends Option {
			MIN_TRANSITION_LENGTH("minTransitionLength", 'l'),
			MAX_TRANSITIONS("maxTransitions", 'm'),
			ALLOW_SQUEEZE_CATCHES("allowExtraSqueezeCatches", 'q'),
			GENERATE_BALL_ANTIBALL_PAIRS("generateBallAntiballPairs", 'g'),
			UNANTITOSSIFY_TRANSITION("noAntitosses", 'A');
		}

		static class InfoOption extends Option {
			NUMBALLS("numBalls", 'b'),
			STATE("state", 's'),
			DIFFICULTY("difficulty", 'd'),
			VALIDITY("validity", 'V'),
			PRIMALITY("primality", 'P');
		}

		static class InputOption extends Option {
			NUMHANDS("numHands", 'h'), 
			START_HAND("startHand", 'H');

			static class SiteswapOption extends InputOption {
				INVERSE("inverted", 'v'),
				SPRUNG("sprung", 'p'),
				INFINITIZE("infinitized", 'f'),
				UNINFINITIZE("unInfinitized", 'F'),
				ANTITOSSIFY("antitossified", 'a'),
				UNANTITOSSIFY("unAntitossified", 'A'),
				ANTINEGATE("antiNegated", 'N'); // turn ss with negative tosses into jugglable pattern

				static class StateOption extends InputOption {
					MIN_LENGTH_FROM_STATE("minLength", 'L');
				}

			}

		}

	}

	static class InputObject {
		NotatedSiteswap siteswap;
		State state;
		boolean isState;
		List<SiteswapOperation> operations;
	}
	
	private static void analyze(NotatedSiteswap ss) {
		printf("parsed: " + ss.toString());
		printf("de-parsed: " + ss.print());
		printf("number of balls: " + ss.numBalls());
		printf("number of hands: " + ss.numHands());
		printf("valid: " + ss.isValid());
		printf("period: " + ss.period());
		printf("state: " + new State(ss));
		printf("unInfinitized: " + ss.unInfinitize().toString());
	}

	public static void main(String[] args) {
		// parse cmdline args //
		//
		boolean parseInput = true;
		boolean parseSiteswap = (args[0] == "-i");
		for(int i=1; i<args.length; i++) {
			if(parseInput) {
				if(parseSiteswap) {
				} else { //parse state
				}
			}
			switch(args[i]) {
			}
		}
	}

	/*
	public static void main(String[] args) {
		if(args.length == 1) {
			try {
				NotatedSiteswap ss = NotatedSiteswap.parseSingle(args[0]);
				analyze(ss);
			} catch(InvalidNotationException e) {
				printf(e.getMessage());
			}
		} else if(args.length == 2) {
			try {
				CompatibleNotatedSiteswapPair patterns = new CompatibleNotatedSiteswapPair(args[0], args[1]);
				analyze(patterns.prefix());
				printf("-----");
				analyze(patterns.suffix());
				printf("-----");
				try {
					int maxTransitions = 10;
					ContextualizedNotatedTransitionList transitions = new ContextualizedNotatedTransitionList(patterns, 0, maxTransitions, false, false);
					printf("General Transition:");
					printf(transitions.printGeneralTransition());
					//printf(transitions.generalTransition());
					printf("All Transitions (first " + maxTransitions + "):");
					List<NotatedSiteswap> ts = transitions.transitionList();
					List<NotatedSiteswap> ts2 = transitions.unUnAntitossifiedTransitionList();
					for(int i=0; i<ts.size(); i++) {
						//printf(ts.get(i).print() + "\t" + ts2.get(i).print());
						printf(ts2.get(i).print());
					}
				} catch(ImpossibleTransitionException e) {
					printf("ERROR: cannot compute transition between non-finite states");
				}
			} catch(InvalidNotationException e) {
				printf(e.getMessage());
			} catch(IncompatibleNotationException e) {
				printf(e.getMessage());
			}
		}
	}
	*/

}
