package siteswapsuite;

import java.util.regex.Pattern;

class InvalidStateNotationException extends InvalidNotationException {
	String message;
	InvalidStateNotationException(String notation) {
		this.message = "ERROR: string `" + notation + "' is not valid state notation";
	}
	public String getMessage() {
		return this.message;
	}
}

public enum StateNotation {

	SIMPLE,
	COMPLEX;

	public static String charge = "(-?[0-9a-z])";
	public static String simpleStateNotation = charge + "+";
	public static String beat = "(\\(" + charge + "(" +  "," + charge + ")*" + "\\))";
	public static String complexStateNotation = "\\(" + beat + "+\\)";

	public static StateNotation analyze(String string) throws InvalidStateNotationException {
		if(Pattern.matches(simpleStateNotation, string)) {
			return StateNotation.SIMPLE;
		} else if(Pattern.matches(complexStateNotation, string)) {
			return StateNotation.COMPLEX;
		} else {
			throw new InvalidStateNotationException(string);
		}
	}

	public static StateNotation defaultNotationType(int numHands) {
		if(numHands == 0) {
			return StateNotation.SIMPLE;
		} else if(numHands > 0) {
			return StateNotation.COMPLEX;
		} else {
			return null;
		}
	}

}
