package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

class ImpossibleTransitionException extends SiteswapException {
	String message = "ERROR: cannot compute transition between non-finite states";
	public String getMessage() {
		return this.message;
	}
}

abstract class Transition extends Siteswap {

	int eventualPeriod = 0;

	static Transition compute(State from, State to, int minLength, boolean allowExtraSqueezeCatches, boolean generateBallAntiballPairs) throws ImpossibleTransitionException {

		Util.printf(from, Util.DebugLevel.DEBUG);
		Util.printf(to, Util.DebugLevel.DEBUG);
		Util.printf("", Util.DebugLevel.DEBUG);

		// first check that the states are finite, otherwise there won't be a transition
		if(!from.isFinite() || !to.isFinite()) {
			throw new ImpossibleTransitionException();
		}

		// see if either state is empty
		if(fromCopy.finiteLength() == 0 || toCopy.finiteLength() == 0) {
			return new EmptyTransition(from.numHands());
		}

		// equalize the state lengths
		if(fromCopy.finiteLength() < toCopy.finiteLength())
			fromCopy.getFiniteNode(toCopy.finiteLength() - 1);
		else if (fromCopy.finiteLength() > toCopy.finiteLength())
			toCopy.getFiniteNode(fromCopy.finiteLength() - 1);

		// determine which subclass constructor to call
		if(allowExtraSqueezeCatches) {
			if(generateBallAntiballPairs)
				return new OneBeatTransition(fromCopy, toCopy); // minLength is irrelevant here, as it will always end up being one beat, then unAntitossified
			else
				return new AllowExtraSqueezeCatches(fromCopy, toCopy, minLength);
		} else {
			if(generateBallAntiballPairs)
				return new GenerateBallAntiballPairs(fromCopy, toCopy, minLength);
			else
				return new StandardTransition(fromCopy, toCopy, minLength);
		}
	}

}
