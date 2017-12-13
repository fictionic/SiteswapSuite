package siteswapsuite;

class InvalidNotationException extends SiteswapException {
	InvalidNotationException() {
	}
	public String getMessage() {
		return "invalid notation";
	}
}
