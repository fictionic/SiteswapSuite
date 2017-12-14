package siteswapsuite;

import java.util.regex.Pattern;

class NodeToken {
	int absoluteHeight;
	boolean isNegative;
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(this.isNegative) {
			builder.append('-');
		}
		builder.append(this.absoluteHeight);
		return builder.toString();
	}
}

class StateNotationToken {

	static enum Type {
		BEAT_OPEN,
		BEAT_CLOSE,
		COMMA,
		COLON,
		NODE;
	}

	Type type;
	NodeToken node;

	StateNotationToken(Type type) {
		this.type = type;
		if(this.type == Type.NODE) {
			this.node = new NodeToken();
		} else {
			this.node = null;
		}
	}

	static NodeToken parseNodeChar(char c) {
		NodeToken token = null;
		char[] tmp = {c};
		String h = new String(tmp);
		if(Pattern.matches("\\d", h)) {
			token = new NodeToken();
			token.absoluteHeight = Integer.parseInt(h);
		} else if(Pattern.matches("[a-z]", h)) {
			token = new NodeToken();
			token.absoluteHeight = (int)(c) - 87;
		}
		return token;
	}

	static Type parseNonNodeChar(char c) {
		switch(c) {
			case '[':
				return Type.BEAT_OPEN;
			case ']':
				return Type.BEAT_CLOSE;
			case ',':
				return Type.COMMA;
			case ':':
				return Type.COLON;
			default:
				return null;
		}
	}

	public String toString() {
		if(this.type == Type.NODE) {
			return this.node.toString();
		} else {
			return this.type.toString();
		}
	}

}


