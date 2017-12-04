package siteswapsuite;

class ArgContainer {
	Argument arg;
	int followUpInt;
	String followUpString;
	
	ArgContainer(Argument arg) {
		this.arg = arg;
	}

	ArgContainer(Argument arg, int followUpInt) {
		this.arg = arg;
		this.followUpInt = followUpInt;
	}

	ArgContainer(Argument arg, String followUpString) {
		this.arg = arg;
		this.followUpString = followUpString;
	}

	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(this.arg.toString());
		if(this.arg.requires == Argument.Requires.REQUIRES_INT) {
			ret.append("=");
			ret.append(this.followUpInt);
		} else if(this.arg.requires == Argument.Requires.REQUIRES_INT) {
			ret.append("=");
			ret.append(this.followUpString);
		}
		return ret.toString();
	}

}
