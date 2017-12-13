package siteswapsuite;

import java.util.regex.Pattern;

class TossToken {
	boolean isInfinite;
	int finiteAbsoluteHeight;
	boolean isNegative;
	boolean isAntitoss;
	boolean isFlippedHand;
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(this.isAntitoss) {
			builder.append('_');
		}
		if(this.isNegative) {
			builder.append('-');
		}
		if(this.isInfinite) {
			builder.append('&');
		} else {
			builder.append(this.finiteAbsoluteHeight);
		}
		if(this.isFlippedHand) {
			builder.append('x');
		}
		return builder.toString();
	}
}

class SiteswapNotationToken {

	static enum Type {
		STAR,
		BANG,
		SYNC_OPEN,
		SYNC_CLOSE,
		COMMA,
		MULTI_OPEN,
		MULTI_CLOSE,
		TOSS;
	}

	Type type;
	TossToken toss;

	SiteswapNotationToken(Type type) {
		this.type = type;
		if(this.type == Type.TOSS) {
			this.toss = new TossToken();
		} else {
			this.toss = null;
		}
	}

	static TossToken parseTossChar(char c) {
		TossToken token = null;
		char[] tmp = {c};
		String h = new String(tmp);
		if(Pattern.matches("\\d", h)) {
			token = new TossToken();
			token.isInfinite = false;
			token.finiteAbsoluteHeight = Integer.parseInt(h);
		} else if(Pattern.matches("([a-wyz])", h)) {
			token = new TossToken();
			token.isInfinite = false;
			token.finiteAbsoluteHeight = (int)(c) - 87;
		} else if(c == '&') {
			token = new TossToken();
			token.isInfinite = true;
		}
		return token;
	}

	static Type parseNonTossChar(char c) {
		switch(c) {
			case '*':
				return Type.STAR;
			case '!':
				return Type.BANG;
			case '[':
				return Type.MULTI_OPEN;
			case ']':
				return Type.MULTI_CLOSE;
			case '(':
				return Type.SYNC_OPEN;
			case ')':
				return Type.SYNC_CLOSE;
			case ',':
				return Type.COMMA;
			default:
				return null;
		}
	}

	public String toString() {
		if(this.type == Type.TOSS) {
			return this.toss.toString();
		} else {
			return this.type.toString();
		}
	}

}


