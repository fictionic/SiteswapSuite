package siteswapsuite;

enum ArgumentType {
	FLAG,
	REQUIRES_INT,
	REQUIRES_STRING,
	TAKES_OPTIONS;
}

// cmdline tokens
enum Argument {
	// global options
	ENABLE_DEBUG('d', "debug", ArgumentType.TAKES_OPTIONS),
	// input indicator
	INPUT('i', "input", ArgumentType.REQUIRES_STRING),
	// input options
	NUM_HANDS('h', "numHands", ArgumentType.REQUIRES_INT),
	START_HAND('H', "startHand", ArgumentType.REQUIRES_INT),
	KEEP_ZEROES('z', "keepZeroes", ArgumentType.FLAG),
	// info items
	INFO('\0', "info", ArgumentType.REQUIRES_STRING),
	CAPACITY('c', "capacity", ArgumentType.FLAG),
	VALIDITY('v', "validity", ArgumentType.FLAG),
	PRIMALITY('P', "primality", ArgumentType.FLAG),
	DIFFICULTY('d', "difficulty", ArgumentType.FLAG),
	// operations
	OPS('\0', "ops", ArgumentType.REQUIRES_STRING),
	INVERT('V', "invert", ArgumentType.FLAG),
	SPRING('p', "spring", ArgumentType.FLAG),
	INFINITIZE('f', "infinitize", ArgumentType.FLAG),
	UNINFINITIZE('F', "unInfinitize", ArgumentType.FLAG),
	ANTITOSSIFY('a', "antitossify", ArgumentType.FLAG),
	UNANTITOSSIFY('A', "unAntitossify", ArgumentType.FLAG),
	ANTINEGATE('N', "antiNegate", ArgumentType.FLAG),
	TO_STATE('s', "state", ArgumentType.FLAG),
	// 'big' operations
	TO_SITESWAP('S', "siteswap", ArgumentType.TAKES_OPTIONS),
	TRANSITION('T', "transition", ArgumentType.TAKES_OPTIONS),
	// transition options
	MIN_TRANSITION_LENGTH('l', "minTransitionLength", ArgumentType.REQUIRES_INT),
	MAX_TRANSITIONS('m', "maxTransitions", ArgumentType.REQUIRES_INT),
	SELECT_TRANSITION('o', "selectTransition", ArgumentType.REQUIRES_INT),
	ALLOW_EXTRA_SQUEEZE_CATCHES('q', "allowExtraSqueezeCatches", ArgumentType.FLAG),
	GENERATE_BALL_ANTIBALL_PAIRS('g', "generateBallAntiballPairs", ArgumentType.FLAG),
	UN_ANTITOSSIFY_TRANSITIONS('A', "unAntitossifyTransitions", ArgumentType.FLAG),
	DISPLAY_GENERAL_TRANSITION('G', "displayGeneralTransition", ArgumentType.FLAG),
	// literal values
	LITERAL_INT('\0', null, null),
	LITERAL_STRING('\0', null, null),
	// invalid
	INVALID_TOKEN('\0', null, null);

	// fields
	char shortForm;
	String longForm;
	ArgumentType type;

	// constructor
	private Argument(char shortForm, String longForm, ArgumentType type) {
		this.shortForm = shortForm;
		this.longForm = longForm;
		this.type = type;
	}

	static Argument parseLongOptionName(String str) {
		for(Argument opt : Argument.values()) {
			if(str.equals(opt.longForm)) {
				return opt;
			}
		}
		return INVALID_TOKEN;
	}

	static Argument parseShortOptionName(char ch) {
		for(Argument opt : Argument.values()) {
			if(ch == opt.shortForm) {
				return opt;
			}
		}
		return INVALID_TOKEN;
	}

}


