package siteswapsuite;

public class ParseError extends SiteswapException {
	String message;
	ParseError(String message) {
		this.message = "ERROR: " + message;
	}
	public String getMessage() {
		return this.message;
	}
}
