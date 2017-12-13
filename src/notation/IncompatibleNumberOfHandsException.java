package siteswapsuite;

class IncompatibleNumberOfHandsException extends SiteswapException {
	String inputNotation;
	int numHands;
	IncompatibleNumberOfHandsException(String inputNotation, int numHands) {
		this.inputNotation = inputNotation;
		this.numHands = numHands;
	}
	IncompatibleNumberOfHandsException() {
		this.inputNotation = null;
	}
	public String getMessage() {
		if(this.inputNotation != null)
			return "ERROR: cannot parse input string '" + this.inputNotation + "' as having " + this.numHands + " hands";
		else
			return "ERROR: incompatible number of hands";
	}
}
