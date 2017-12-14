package siteswapsuite;

import java.util.regex.Pattern;

class ValueToken {
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
		VALUE;
	}

	Type type;
	ValueToken value;

	StateNotationToken(Type type) {
		this.type = type;
		if(this.type == Type.VALUE) {
			this.value = new ValueToken();
		} else {
			this.value = null;
		}
	}

	static ValueToken parseNodeChar(char c) {
		ValueToken token = null;
		char[] tmp = {c};
		String h = new String(tmp);
		if(Pattern.matches("\\d", h)) {
			token = new ValueToken();
			token.absoluteHeight = Integer.parseInt(h);
		} else if(Pattern.matches("[a-z]", h)) {
			token = new ValueToken();
			token.absoluteHeight = (int)(c) - 87;
		}
		return token;
	}

	static Type parseNonNodeChar(char c) {
		switch(c) {
			case '(':
				return Type.BEAT_OPEN;
			case ')':
				return Type.BEAT_CLOSE;
			case ':':
				return Type.COLON;
			default:
				return null;
		}
	}

	public String toString() {
		if(this.type == Type.VALUE) {
			return this.value.toString();
		} else {
			return this.type.toString();
		}
	}

}


