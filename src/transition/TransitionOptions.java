package siteswapsuite;

public class TransitionOptions {
	int minTransitionLength = -1;
	int maxTransitions = -1;
	boolean allowExtraSqueezeCatches = false;
	boolean generateBallAntiballPairs = false;
	int selectTransition = 0;
	public String toString() {
		StringBuilder ret = new StringBuilder();
		if(this.minTransitionLength != -1) {
			ret.append(Argument.MIN_TRANSITION_LENGTH);
			ret.append("=");
			ret.append(this.minTransitionLength);
			ret.append(",");
		}
		if(this.maxTransitions != -1) {
			ret.append(Argument.MAX_TRANSITIONS);
			ret.append("=");
			ret.append(this.maxTransitions);
			ret.append(",");
		}
		if(this.allowExtraSqueezeCatches) {
			ret.append(Argument.ALLOW_EXTRA_SQUEEZE_CATCHES);
			ret.append(",");
		}
		if(this.generateBallAntiballPairs) {
			ret.append(Argument.GENERATE_BALL_ANTIBALL_PAIRS);
			ret.append(",");
		}
		if(this.selectTransition != -1) {
			ret.append(Argument.SELECT_TRANSITION);
			ret.append("=");
			ret.append(this.selectTransition);
			ret.append(",");
		}
		ret.deleteCharAt(ret.length() - 1);
		return ret.toString();
	}
}
