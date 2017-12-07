package siteswapsuite;

public enum Argument {
	// debug stuff
	ENABLE_DEBUG('\0', "debug", Role.FIRST, Role.DEBUG, FollowUp.NONE),
	ENABLE_DEBUG_MAIN('\0', "main", Role.DEBUG, FollowUp.NONE),
	ENABLE_DEBUG_SITESWAP('\0', "siteswap", Role.DEBUG, FollowUp.NONE),
	ENABLE_DEBUG_TRANSITION('\0', "transition", Role.DEBUG, FollowUp.NONE),
	// input indicator
	INPUT('i', "input", Role.INPUT, Role.INPUT_OPTION_LITERAL, FollowUp.STRING),
	// input options
	NUM_HANDS('h', "num-hands", Role.INPUT_OPTION_LITERAL, FollowUp.INT),
	START_HAND('H', "start-hand", Role.INPUT_OPTION_LITERAL, FollowUp.INT),
	KEEP_ZEROES('z', "keep-zeroes", Role.INPUT_OPTION_LITERAL, FollowUp.NONE),
	// info items
	INFO('\0', "info", Role.CHAIN, Role.INFO, FollowUp.NONE),
	CAPACITY('c', "capacity", Role.INFO, FollowUp.NONE),
	VALIDITY('v', "validity", Role.INFO, FollowUp.NONE),
	PRIMALITY('P', "primality", Role.INFO, FollowUp.NONE),
	DIFFICULTY('d', "difficulty", Role.INFO, FollowUp.NONE),
	// operations
	OPS('\0', "ops", Role.CHAIN, Role.OPERATION, FollowUp.NONE),
	INVERT('V', "invert", Role.OPERATION, FollowUp.NONE),
	SPRING('p', "spring", Role.OPERATION, FollowUp.NONE),
	INFINITIZE('f', "infinitize", Role.OPERATION, FollowUp.NONE),
	UNINFINITIZE('F', "un-infinitize", Role.OPERATION, FollowUp.NONE),
	ANTITOSSIFY('a', "antitossify", Role.OPERATION, FollowUp.NONE),
	UNANTITOSSIFY('A', "un-antitossify", Role.OPERATION, FollowUp.NONE),
	TO_STATE('s', "to-state", Role.OPERATION, FollowUp.NONE),
	TO_SITESWAP('S', "to-siteswap", Role.OPERATION, Role.INPUT_OPTION_TRANSITION, FollowUp.NONE),
	// operations that take multiple inputs
	TRANSITION('T', "transition", Role.INPUT, Role.INPUT_OPTION_TRANSITION, FollowUp.NONE),
	/* COMBINE
	 * CONCATENATE
	 * */
	// misc operations
	/*
	 * EXTRACT_ORBIT
	 * JUGGLE
	 */
	// transition options
	FROM_INDEX('\0', "from", Role.INPUT_OPTION_TRANSITION, FollowUp.INT),
	TO_INDEX('\0', "to", Role.INPUT_OPTION_TRANSITION, FollowUp.INT),
	MIN_TRANSITION_LENGTH('l', "min-length", Role.INPUT_OPTION_TRANSITION, FollowUp.INT),
	MAX_TRANSITIONS('m', "max-transitions", Role.INPUT_OPTION_TRANSITION, FollowUp.INT),
	SELECT_TRANSITION('o', "select-transition", Role.INPUT_OPTION_TRANSITION, FollowUp.INT),
	ALLOW_EXTRA_SQUEEZE_CATCHES('q', "allow-extra-squeeze-catches", Role.INPUT_OPTION_TRANSITION, FollowUp.NONE),
	GENERATE_BALL_ANTIBALL_PAIRS('g', "generate-ball-antiball-pairs", Role.INPUT_OPTION_TRANSITION, FollowUp.NONE),
	UN_ANTITOSSIFY_OPTION_TRANSITIONS('A', "un-antitossify-transitions", Role.INPUT_OPTION_TRANSITION, FollowUp.NONE),
	DISPLAY_GENERAL_TRANSITION('G', "display-generalized-transition", Role.INPUT_OPTION_TRANSITION, FollowUp.NONE);

	// fields
	char shortForm;
	String longForm;
	Role ownRole, optionsRole;
	FollowUp requires;

	static enum Role {
		FIRST, // must appear before options of all other roles
		INPUT, // begins new chain
		INPUT_OPTION_LITERAL, // only as options to --input
		INPUT_OPTION_TRANSITION, // only as options to --transition or --to-siteswap
		CHAIN, // only within a chain
		OPERATION, // only within a chain or as options to --ops
		INFO, // only within a chain or as options to --info
		DEBUG; // only as options to --debug
	}

	static enum FollowUp {
		NONE,
		INT,
		STRING;
	}

	// constructor
	private Argument(char shortForm, String longForm, Role ownRole, Role optionsRole, FollowUp requires) {
		this.shortForm = shortForm;
		this.longForm = longForm;
		this.ownRole = ownRole;
		this.optionsRole = optionsRole;
		this.requires = requires;
	}
	// shorthand
	private Argument(char shortForm, String longForm, Role ownRole, FollowUp requires) {
		this(shortForm, longForm, ownRole, null, requires);
	}

	static Argument parseLongOptionName(String str, Role targetRole) throws ParseError {
		boolean foundButWrongRole = false;
		Argument wrongArg = null;
		for(Argument opt : Argument.values()) {
			if(str.equals(opt.longForm)) {
				if(targetRole == null || opt.ownRole == targetRole || (targetRole == Role.FIRST && opt.ownRole == Role.INPUT)) {
					return opt;
				} else {
					foundButWrongRole = true;
					wrongArg = opt;
				}
			}
		}
		if(foundButWrongRole) {
			throw new ParseError("argument '" + wrongArg.helpString(true) + "' is not a valid in this context");
		}
		throw new ParseError("unrecognized argument name: '" + str + "'");
	}

	static Argument parseShortOptionName(char ch, Role targetRole) throws ParseError {
		boolean foundButWrongRole = false;
		Argument wrongArg = null;
		for(Argument opt : Argument.values()) {
			if(ch == opt.shortForm) {
				if(targetRole == null || opt.ownRole == targetRole || (targetRole == Role.FIRST && opt.ownRole == Role.INPUT)) {
					return opt;
				} else {
					foundButWrongRole = true;
					wrongArg = opt;
				}
			}
		}
		if(foundButWrongRole) {
			throw new ParseError("argument '" + wrongArg.helpString(true) + "' is not a valid in this context");
		}
		throw new ParseError("unrecognized argument name: '" + ch + "'");
	}

	public String helpString() {
		return this.helpString(false);
	}
	public String helpString(boolean bare) {
		StringBuilder ret = new StringBuilder();
		if(this.shortForm != '\0') {
			if(!bare) {
				ret.append('-');
			}
			ret.append(this.shortForm);
			ret.append('/');
		}
		if(!bare) {
			ret.append("--");
		}
		ret.append(this.longForm);
		return ret.toString();
	}

}
