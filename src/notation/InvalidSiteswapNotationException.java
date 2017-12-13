package siteswapsuite;

public class InvalidSiteswapNotationException extends InvalidNotationException {
	InvalidSiteswapNotationException() {
	}
	public String getMessage() {
		return "invalid siteswap notation";
	}
}
