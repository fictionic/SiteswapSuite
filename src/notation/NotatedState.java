package siteswapsuite;

import java.util.regex.Pattern;

public class NotatedState {

	State state;
	StateNotation notationType;

	public StateNotation notationType() {
		return this.notationType;
	}

	public String print() {
		StringBuilder ret = new StringBuilder();
		State.Node curNode = this.state.nowNode;
		for(int i=0; i<this.state.finiteLength; i++) {
			ret.append(curNode.getChargeAtHand(0));
			curNode = curNode.prev;
		}
		if(this.state.repeatedLength > 0) {
			ret.append(":");
			for(int i=0; i<this.state.repeatedLength; i++) {
				ret.append(curNode.getChargeAtHand(0));
				curNode = curNode.prev;
			}
		}
		return ret.toString();
	}

}
