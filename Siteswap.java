package siteswapsuite;

import java.util.List;

public abstract class Siteswap {

	int numHands;
	List<List<? extends Site>> sites;

	// ------------- //
	// querying info //
	// ------------- //

	// basic info
	public abstract int numHands();

	public abstract int period();

	// info that needs to be computed
	public abstract ExtendedFraction numBalls();

	public abstract boolean isValid();

	// getting info about particular sites
	public abstract int numTossesAtSite(int atBeat, int fromHand);

	public abstract int outDegreeAtSite(int atBeat, int fromHand);

	public abstract boolean siteIsEmpty(int beatIndex, int handIndex);

	// getting a particular toss
	public abstract Toss getToss(int atBeat, int fromHand, int tossIndex);

	// ----------------------------------------- //
	// computing another pattern out of this one //
	// ----------------------------------------- //

	// analagous to substring
	public abstract Siteswap subPattern(int startBeat, int endBeat);

	// sprung version of pattern (e.g. 2 --> (4,2x)*)
	public abstract Siteswap getSprungPattern();

	// inverse of pattern
	public abstract Siteswap getInverse();

	// --------------- //
	// generic methods //
	// --------------- //
	
	// get a toss-for-toss copy of this pattern
	//public abstract ? super Siteswap deepCopy();

	// universal string representation (mostly for debugging)
	public abstract String toString();

	<T extends Site> T getSite(int atBeat, int atHand) {
		return this.sites.get(atBeat).get(atHand);
	}

	abstract class Site {
		List<Toss> tosses;
		int handIndex;
	}

	// --------------------- //
	// get a mutable version //
	// --------------------- //

}
