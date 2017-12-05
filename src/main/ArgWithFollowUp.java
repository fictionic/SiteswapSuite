package siteswapsuite;

class ArgWithFollowUp {
	Argument arg;
	int followUpInt;
	String followUpString;
	
	ArgWithFollowUp(Argument arg) {
		this.arg = arg;
	}

	ArgWithFollowUp(Argument arg, int followUpInt) {
		this.arg = arg;
		this.followUpInt = followUpInt;
	}

	ArgWithFollowUp(Argument arg, String followUpString) {
		this.arg = arg;
		this.followUpString = followUpString;
	}

	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(this.arg.toString());
		if(this.arg.requires == Argument.FollowUp.INT) {
			ret.append("=");
			ret.append(this.followUpInt);
		} else if(this.arg.requires == Argument.FollowUp.INT) {
			ret.append("=");
			ret.append(this.followUpString);
		}
		return ret.toString();
	}

}
