package siteswapsuite;

class InvalidNotationException extends SiteswapException {
	String s;
	InvalidNotationException(String s) {
		this.s = s;
	}
	public String getMessage() {
		return this.s;
	}
}
