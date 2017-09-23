package siteswapsuite;

class InvalidStateNotationException extends SiteswapException {
	String message;
	InvalidStateNotationException(String notation) {
		this.message = "ERROR: string `" + notation + "' is not valid state notation";
	}
	public String getMessage() {
		return this.message;
	}
}

public class StateNotation {

	public static String charge = "(-?[0-9a-z])";
	public static String simpleStateNotation = charge + "+";
	public static String beat = "(\\(" + charge + "(" +  "," + charge + ")" + "\\))";
	public static String complexStateNotation = beat + "+";

}
