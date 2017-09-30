package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class Siteswap {

	int numHands;
	List<List<Site>> sites;

	// main constructor - initialize a completely empty siteswap with the given number of hands
	public Siteswap(int numHands) {
		this.numHands = numHands;
		this.sites = new ArrayList<List<Site>>();
	}

	// private constructor, only for subPattern() and NotatedSiteswap
	Siteswap(int numHands, List<List<Site>> sites) {
		this.numHands = numHands;
		this.sites = sites;
	}

	// querying basic info
	public int numHands() {
		return this.numHands;
	}

	public int period() {
		return this.sites.size();
	}

	// computing more complicated info

	public ExtendedFraction numBalls() {
		Siteswap toRunOn = this.deepCopy();
		toRunOn.unAntitossify(); // needs to be implemented
		int finiteValue = 0;
		int numInfinities = 0;
		Toss curToss;
		ExtendedInteger curTossHeight;
		for(int b=0; b<toRunOn.sites.size(); b++) {
			for(int h=0; h<toRunOn.numHands; h++) {
				for(int t=0; t<toRunOn.numTossesAtSite(b, h); t++) {
					curToss = toRunOn.getToss(b, h, t);
					curTossHeight = curToss.height();
					if(curTossHeight.isInfinite()) {
						if(curTossHeight.infiniteValue() == InfinityType.POSITIVE_INFINITY) {
							if(!curToss.isAntitoss()) {
								numInfinities++;
							} else {
								numInfinities--;
							}
						} else {
							if(!curToss.isAntitoss()) {
								numInfinities--;
							} else {
								numInfinities++;
							}
						}
					} else {
						finiteValue += curToss.charge() * curTossHeight.finiteValue();
					}
				}
			}
		}
		ExtendedInteger top;
		if(numInfinities == 0) {
			top = new ExtendedInteger(finiteValue);
		} else {
			if(numInfinities > 0) {
				top = new ExtendedInteger(InfinityType.POSITIVE_INFINITY);
			} else {
				top = new ExtendedInteger(InfinityType.NEGATIVE_INFINITY);
			}
		}
		return new ExtendedFraction(top, this.sites.size());
	}

	public boolean isValid() {
		Siteswap toRunOn = this.deepCopy();
		// turn any `-&`s into catches by pairing them up with `&`s
		toRunOn.unInfinitize();
		// reset inDegree of each site
		for(int b=0; b<toRunOn.sites.size(); b++) {
			for(int h=0; h<toRunOn.numHands; h++) {
				toRunOn.getSite(b, h).inDegree = 0;
			}
		}
		// calculate inDegree of each site
		Toss curToss;
		int destBeat;
		int destHand;
		for(int b=0; b<toRunOn.period(); b++) {
			for(int h=0; h<toRunOn.numHands; h++) {
				for(int t=0; t<toRunOn.numTossesAtSite(b, h); t++) {
					curToss = toRunOn.getToss(b, h, t);
					if(curToss.height().isInfinite()) {
						if(curToss.height().infiniteValue() == InfinityType.NEGATIVE_INFINITY) {
							destBeat = b;
							destHand = h;
							toRunOn.getSite(destBeat, destHand).inDegree += curToss.charge();
						}
					} else {
						destBeat = (b + curToss.height().finiteValue()) % toRunOn.period();
						if(destBeat < 0) {
							destBeat += toRunOn.period();
						}
						destHand = curToss.destHand();
						toRunOn.getSite(destBeat, destHand).inDegree += curToss.charge();
					}
				}
			}
		}
		// check if each site's inDegree matches its outDegree
		for(int b=0; b<toRunOn.sites.size(); b++) {
			for(int h=0; h<toRunOn.numHands; h++) {
				if(toRunOn.getSite(b, h).inDegree != toRunOn.getSite(b, h).outDegree) {
					Util.printf("b: " + b + ", h: " + h, Util.DebugLevel.DEBUG);
					Util.printf("in:  " + toRunOn.getSite(b, h).inDegree, Util.DebugLevel.DEBUG);
					Util.printf("out: " + toRunOn.getSite(b, h).outDegree, Util.DebugLevel.DEBUG);
					return false;
				}
			}
		}
		return true;
	}

	public boolean isPrime() {
		State curState = new State(this);
		State[] prevStates = new State[this.period()];
		prevStates[0] = curState.deepCopy();
		for(int b=0; b<this.period()-1; b++) {
			for(int h=0; h<this.numHands(); h++) {
				for(int t=0; t<this.numTossesAtSite(b, h); t++) {
					Toss toss = this.getToss(b, h, t);
					if(toss.charge() == 0) {
						continue;
					}
					if(toss.height().isInfinite()) {
						if(toss.height().infiniteValue() == InfinityType.POSITIVE_INFINITY) {
							if(toss.charge() == 1) {
								curState.decChargeOfNowNodeAtHand(h);
							} else {
								curState.incChargeOfNowNodeAtHand(h);
							}
						} else {
							if(toss.charge() == 1) {
								curState.incChargeOfNowNodeAtHand(h);
							} else {
								curState.decChargeOfNowNodeAtHand(h);
							}
						}
					} else {
						if(toss.charge() == 1) {
							curState.decChargeOfNowNodeAtHand(h);
							curState.incChargeOfNodeAtHand(toss.height().finiteValue(), toss.destHand());
						} else {
							curState.incChargeOfNowNodeAtHand(h);
							curState.decChargeOfNodeAtHand(toss.height().finiteValue(), toss.destHand());
						}
					}
				}
			}
			curState.advanceTime();
			// check if the new state is the same as any of the previous ones
			for(int i=0; i<=b; i++) {
				if(curState.equals(prevStates[i])) {
					return false;
				}
			}
			prevStates[b+1] = curState.deepCopy();
		}
		return true;
	}

	public List<Siteswap> getOrbits() {
		if(this.numBalls().numerator().isInfinite()) {
			return null;
		}
		List<Siteswap> orbits = new ArrayList<Siteswap>();
		Siteswap copy = this.deepCopy();
		boolean allZero;
		Toss toss;
		while(true) {
			allZero = true;
			// find next orbit
			for(int b=0; b<copy.period(); b++) {
				for(int h=0; h<copy.numHands(); h++) {
					for(int t=0; t<copy.numTossesAtSite(b,h); t++) {
						Util.printf("b: " + b + ", h: " + h + ", t: " + t, Util.DebugLevel.DEBUG);
						toss = copy.getToss(b,h,t);
						if(toss.charge() != 0) {
							allZero = false;
						} else {
							continue;
						}
						// create new orbit
						Siteswap curOrbit = new Siteswap(this.numHands());
						for(int i=0; i<this.period(); i++) {
							curOrbit.appendEmptyBeat();
						}
						// set up adding of first toss
						Toss curToss = toss;
						int b2 = 0, h2 = 0, t2 = 0;
						do {
							// TODO: fix this algorithm
							Util.printf("b2: " + b2 + ", h2: " + h2 + ", t2: " + t2, Util.DebugLevel.DEBUG);
							Util.printf(copy, Util.DebugLevel.DEBUG);
							Util.printf(curOrbit, Util.DebugLevel.DEBUG);
							Util.printf("", Util.DebugLevel.DEBUG);
							// add next toss to orbit
							curToss = copy.getToss(b2, h2, t2);
							curOrbit.addToss(b, h, curToss);
							if(curToss.height().isInfinite()) {
								Util.printf("DON'T KNOW WHAT TO DO WITH INFINITE TOSS WHEN FINDING ORBITS", Util.DebugLevel.DEBUG);
								continue;
							}
							// remove toss from copy
							copy.removeToss(b2,h2,t2);
							// advance through siteswap
							b2 = (b2 + toss.height().finiteValue()) % this.period();
							if(b2 < 0) b2 += this.period();
							h2 = curToss.destHand();
							if(b2 == b && h2 == h) t2 = t; else t2 = 0;
						} while(b2 != b || h2 != h || t2 != t);
						// add orbit to list
						orbits.add(curOrbit);
					}
				}
			}
			if(allZero) {
				break;
			}
		}
		return orbits;
	}

	public ExtendedFraction difficulty() {
		Util.printf("WARNING: difficulty calculation not yet implemented", Util.DebugLevel.ERROR);
		ExtendedFraction b = this.numBalls();
		int h = this.numHands;
		int bottom;
		if(b.numerator().isInfinite())
			bottom = (h > 0 ? 1 : 0);
		else {
		}
		return new ExtendedFraction(new ExtendedInteger(0), 1);
	}

	// querying tosses

	Site getSite(int beatIndex, int handIndex) {
		beatIndex = beatIndex % this.sites.size();
		if(beatIndex < 0) {
			beatIndex += this.sites.size();
		}
		return this.sites.get(beatIndex).get(handIndex);
	}

	public int numTossesAtSite(int atBeat, int fromHand) {
		return this.getSite(atBeat, fromHand).numTosses();
	}

	public int outDegreeAtSite(int atBeat, int fromHand) {
		return this.getSite(atBeat, fromHand).outDegree();
	}

	public boolean siteIsEmpty(int beatIndex, int handIndex) {
		return this.getSite(beatIndex, handIndex).isEmpty();
	}

	public Toss getToss(int atBeat, int fromHand, int tossIndex) {
		return this.getSite(atBeat, fromHand).getToss(tossIndex);
	}

	// adding tosses
	public void addToss(int atBeat, int fromHand, Toss toss) {
		this.getSite(atBeat, fromHand).addToss(toss);
	}

	public void addFiniteToss(int atBeat, int fromHand, int height, int toHand) {
		this.getSite(atBeat, fromHand).addToss(new Toss(height, toHand, false));
	}

	public void addFiniteAntitoss(int atBeat, int fromHand, int height, int toHand) {
		this.getSite(atBeat, fromHand).addToss(new Toss(height, toHand, true));
	}

	public void addInfiniteToss(int atBeat, int fromHand, InfinityType height) {
		this.getSite(atBeat, fromHand).addToss(new Toss(height, false));
	}

	public void addInfiniteAntitoss(int atBeat, int fromHand, InfinityType height) {
		this.getSite(atBeat, fromHand).addToss(new Toss(height, true));
	}

	// editing tosses

	public void exchangeToss(int atBeat, int fromHand, int tossIndex, Toss newToss) {
		this.getSite(atBeat, fromHand).exchangeToss(tossIndex, newToss);
	}

	// removing tosses

	public Toss removeToss(int beatIndex, int handIndex, int tossIndex) {
		return this.getSite(beatIndex, handIndex).removeToss(tossIndex);
	}

	public void removeBeat(int beatIndex) {
		this.sites.remove(beatIndex);
	}

	public void removeLastBeat() {
		this.removeBeat(this.sites.size() - 1);
	}

	// extending pattern
	public void appendEmptyBeat() {
		this.sites.add(new ArrayList<Site>());
		for(int h=0; h<this.numHands; h++)
			this.sites.get(period()-1).add(new Site(h));
	}

	public int extendToBeatIndex(int beatIndex) { //returns index of beat that was previously "at" given index (either 0, period(), or beatIndex)
		if(beatIndex < 0) {
			Siteswap toAnnex = new Siteswap(this.numHands);
			while(beatIndex < 0) {
				toAnnex.appendEmptyBeat();
				beatIndex++;
			}
			toAnnex.appendSiteswap(this);
			this.sites = toAnnex.sites;
		}
		while(beatIndex > this.period()) {
			this.appendEmptyBeat();
			beatIndex--;
		}
		return beatIndex;
	}

	public void appendSiteswap(Siteswap toAppend) {
		return;
	}

	// manipulating pattern
	public void starify() {
		if(this.numHands() != 2) {
			return;
		}
		int originalPeriod = this.period();
		for(int b=0; b<originalPeriod; b++) {
			this.appendEmptyBeat();
			for(int h=0; h<this.numHands; h++) {
				for(int t=0; t<this.numTossesAtSite(b, h); t++) {
					Toss curToss = this.getToss(b, h, t);
					Toss toAdd = curToss.deepCopy();
					toAdd.starify();
					this.addToss(b + originalPeriod, (h + 1) % 2, toAdd);
				}
			}
		}
	}

	public void infinitize() {
	}

	public void unInfinitize() {
		// look for tosses/antitosses of height &
		for(int b=0; b<this.period(); b++) {
			for(int h=0; h<this.numHands(); h++) {
				for(int t=0; t<this.numTossesAtSite(b, h); t++) {
					Toss curToss = this.getToss(b, h, t);
					if(curToss.height().isInfinite() && curToss.height().infiniteValue() == InfinityType.POSITIVE_INFINITY) {
						search: {
							// first search this beat to see if we can make zero-tosses
							for(int h2=0; h2<this.numHands; h2++) {
								for(int t2=0; t2<this.numTossesAtSite(b, h2); t2++) {
									Toss curCatch = this.getToss(b, h2, t2);
									if(curCatch.charge() == curToss.charge() &&
											curCatch.height().isInfinite() &&
											curCatch.height().infiniteValue() == InfinityType.NEGATIVE_INFINITY) {
										this.exchangeToss(b, h, t,  new Toss(0, h2, curToss.isAntitoss()) );
										this.removeToss(b, h2, t2);
										break search;
									}
								}
							}
							// now search for positive tosses
							for(int b2=b+1; b2<b+1+this.period(); b2++) {
								for(int h2=0; h2<this.numHands(); h2++) {
									for(int t2=0; t2<this.numTossesAtSite(b2, h2); t2++) {
										Toss curCatch = this.getToss(b2, h2, t2);
										if(curCatch.charge() == curToss.charge() &&
												curCatch.height().isInfinite() &&
												curCatch.height().infiniteValue() == InfinityType.NEGATIVE_INFINITY) {
											this.removeToss(b2, h2, t2);
											this.exchangeToss(b, h, t, new Toss(b2 - b, h2, curToss.charge() == 1));
											break search;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void antitossify() {
		Siteswap temp = new Siteswap(this.numHands);
		for(int i=0; i<this.period(); i++) {
			temp.appendEmptyBeat();
		}
		// loop through beats
		for(int b=0; b<this.period(); b++) {
			// loop through hands
			for(int h=0; h<this.numHands(); h++) {
				// loop through tosses
				for(int t=0; t<this.numTossesAtSite(b, h); t++) {
					Toss curToss = this.getToss(b, h, t);
					// check if it's a negative toss
					if(curToss.height().sign() < 0) {
						// check if it's infinite
						if(curToss.height().isInfinite()) {
							// replace the negative toss with an antitoss in the same site
							Toss newToss = new Toss(InfinityType.POSITIVE_INFINITY, true);
							temp.addToss(b, h, newToss);
						} else {
							// add an antitoss to the appropriate site
							Toss newToss = new Toss(-1 * curToss.height().finiteValue(), h, true);
							temp.addToss(b + curToss.height().finiteValue(), curToss.destHand(), newToss);
						}
					} else {
						temp.addToss(b, h, curToss.deepCopy());
					}
				}
			}
		}
		this.sites = temp.sites;
	}

	public void unAntitossify() {
	}

	public void invert() {
	}

	public void antiNegate() {
	}

	public Siteswap subPattern(int startBeat, int endBeat) {
		//get deep copy of each beat within specified indices
		List<List<Site>> newSites = new ArrayList<List<Site>>();
		for(int b=startBeat; b<endBeat; b++) {
			List<Site> newBeat = new ArrayList<Site>();
			for(int h=0; h<this.numHands; h++) {
				newBeat.add(this.getSite(b, h).deepCopy());
			}
			newSites.add(newBeat);
		}
		return new Siteswap(this.numHands, newSites);
	}

	public String toString() {
		return this.sites.toString();
	}

	public Siteswap deepCopy() {
		return this.subPattern(0, this.period());
	}

	class Site {
		private List<Toss> tosses;
		private int handIndex;
		private int inDegree;
		private int outDegree;

		// standard constructor - create an empty site
		private Site(int handIndex) {
			this.handIndex = handIndex;
			this.tosses = new ArrayList<Toss>();
			this.inDegree = 0;
			this.outDegree = 0;
		}

		// for deepCopy
		private Site(int handIndex, List<Toss> newTosses, int newOutDegree) {
			this.handIndex = handIndex;
			this.tosses = newTosses;
			this.outDegree = newOutDegree;
			this.inDegree = 0;
		}

		private int numTosses() {
			return this.tosses.size();
		}

		private int outDegree() {
			return this.outDegree;
		}

		Toss getToss(int tossIndex) {
			if(tossIndex >= this.tosses.size()) {
				return null;
			}
			return this.tosses.get(tossIndex);
		}

		Toss removeToss(int tossIndex) {
			if(!this.isEmpty()) {
				this.outDegree -= this.tosses.get(tossIndex).charge();
				return this.tosses.remove(tossIndex);
			} else {
				return null;
			}
		}

		void addToss(Toss toss) {
			this.tosses.add(toss);
			this.outDegree += toss.charge();
		}

		void exchangeToss(int tossIndex, Toss newToss) {
			if(!this.isEmpty()) {
				this.outDegree += newToss.charge() - this.tosses.get(tossIndex).charge();
				this.tosses.set(tossIndex, newToss);
			}
		}

		private boolean isEmpty() {
			return this.tosses.size() == 0;
		}

		private Site deepCopy() {
			List<Toss> newTosses = new ArrayList<Toss>();
			for(int t=0; t<this.tosses.size(); t++) {
				newTosses.add(this.tosses.get(t).deepCopy());
			}
			return new Site(this.handIndex, newTosses, this.outDegree);
		}

		public String toString() {
			return this.tosses.toString();
		}
	}

}
