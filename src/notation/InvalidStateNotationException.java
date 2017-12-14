package siteswapsuite;

public class InvalidStateNotationException extends InvalidNotationException {
	InvalidStateNotationException() {
	}
	public String getMessage() {
		return "invalid state notation";
	}
}
