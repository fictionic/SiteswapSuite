package siteswapsuite;

public class ImpossibleTransitionException extends SiteswapException {
	public String getMessage() {
		return "ERROR: cannot compute transition between non-finite states";
	}
}
