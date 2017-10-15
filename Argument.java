package siteswapsuite;

// cmdline tokens
enum Argument {
	// global options
	ENABLE_DEBUG('d', "debug", Requires.REQUIRES_NONE, Role.OTHER_ROLE),
	// input indicator
	INPUT('i', "input", Requires.REQUIRES_STRING, Role.INPUT_ROLE),
	// input options
	NUM_HANDS('h', "numHands", Requires.REQUIRES_INT, Role.INPUT_ROLE),
	START_HAND('H', "startHand", Requires.REQUIRES_INT, Role.INPUT_ROLE),
	KEEP_ZEROES('z', "keepZeroes", Requires.REQUIRES_NONE, Role.INPUT_ROLE),
	// info items
	INFO('\0', "info", Requires.REQUIRES_NONE, Role.INFO_ROLE),
	CAPACITY('c', "capacity", Requires.REQUIRES_NONE, Role.INFO_ROLE),
	VALIDITY('v', "validity", Requires.REQUIRES_NONE, Role.INFO_ROLE),
	PRIMALITY('P', "primality", Requires.REQUIRES_NONE, Role.INFO_ROLE),
	DIFFICULTY('d', "difficulty", Requires.REQUIRES_NONE, Role.INFO_ROLE),
	// operations
	OPS('\0', "ops", Requires.REQUIRES_NONE, Role.OPERATION_ROLE),
	INVERT('V', "invert", Requires.REQUIRES_NONE, Role.OPERATION_ROLE),
	SPRING('p', "spring", Requires.REQUIRES_NONE, Role.OPERATION_ROLE),
	INFINITIZE('f', "infinitize", Requires.REQUIRES_NONE, Role.OPERATION_ROLE),
	UNINFINITIZE('F', "unInfinitize", Requires.REQUIRES_NONE, Role.OPERATION_ROLE),
	ANTITOSSIFY('a', "antitossify", Requires.REQUIRES_NONE, Role.OPERATION_ROLE),
	UNANTITOSSIFY('A', "unAntitossify", Requires.REQUIRES_NONE, Role.OPERATION_ROLE),
	ANTINEGATE('N', "antiNegate", Requires.REQUIRES_NONE, Role.OPERATION_ROLE),
	TO_STATE('s', "state", Requires.REQUIRES_NONE, Role.OPERATION_ROLE),
	// 'big' operations
	TO_SITESWAP('S', "siteswap", Requires.REQUIRES_NONE, Role.TRANSITION_ROLE),
	TRANSITION('T', "transition", Requires.REQUIRES_NONE, Role.TRANSITION_ROLE),
	// transition options
	MIN_TRANSITION_LENGTH('l', "minTransitionLength", Requires.REQUIRES_INT, Role.TRANSITION_ROLE),
	MAX_TRANSITIONS('m', "maxTransitions", Requires.REQUIRES_INT, Role.TRANSITION_ROLE),
	SELECT_TRANSITION('o', "selectTransition", Requires.REQUIRES_INT, Role.TRANSITION_ROLE),
	ALLOW_EXTRA_SQUEEZE_CATCHES('q', "allowExtraSqueezeCatches", Requires.REQUIRES_NONE, Role.TRANSITION_ROLE),
	GENERATE_BALL_ANTIBALL_PAIRS('g', "generateBallAntiballPairs", Requires.REQUIRES_NONE, Role.TRANSITION_ROLE),
	UN_ANTITOSSIFY_TRANSITIONS('A', "unAntitossifyTransitions", Requires.REQUIRES_NONE, Role.TRANSITION_ROLE),
	DISPLAY_GENERAL_TRANSITION('G', "displayGeneralTransition", Requires.REQUIRES_NONE, Role.TRANSITION_ROLE),
	// literal values
	LITERAL_INT('\0', null, null, null),
	LITERAL_STRING('\0', null, null, null),
	// invalid
	INVALID_TOKEN('\0', null, null, null);

	// fields
	char shortForm;
	String longForm;
	Requires requires;
	Role role;

	enum Requires {
		REQUIRES_NONE,
		REQUIRES_INT,
		REQUIRES_STRING;
	}

	enum Role {
		INPUT_ROLE,
		INFO_ROLE,
		OPERATION_ROLE,
		TRANSITION_ROLE,
		OTHER_ROLE;
	}

	// constructor
	private Argument(char shortForm, String longForm, Requires requires, Role role) {
		this.shortForm = shortForm;
		this.longForm = longForm;
		this.requires = requires;
		this.role = role;
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


