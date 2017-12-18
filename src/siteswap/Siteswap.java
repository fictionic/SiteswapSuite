package siteswapsuite;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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
		Util.printf("in isValid()", Util.DebugLevel.DEBUG);
		Util.printf("making copy...", Util.DebugLevel.DEBUG);
		Util.printf("siteswap: " + this.toString(), Util.DebugLevel.DEBUG);
		Siteswap toRunOn = this.deepCopy();
		// turn any `-&`s into catches by pairing them up with `&`s
		Util.printf("unInfinitizing...", Util.DebugLevel.DEBUG);
		toRunOn.unInfinitize();
		Util.printf("antitossifying...", Util.DebugLevel.DEBUG);
		toRunOn.antitossify();
		Util.printf("siteswap: " + toRunOn.toString(), Util.DebugLevel.DEBUG);
		// reset inDegree of each site
		Util.printf("resetting inDegrees to 0...", Util.DebugLevel.DEBUG);
		for(int b=0; b<toRunOn.sites.size(); b++) {
			for(int h=0; h<toRunOn.numHands; h++) {
				toRunOn.getSite(b, h).inDegree = 0;
			}
		}
		// calculate inDegree of each site
		Util.printf("computing inDegrees...", Util.DebugLevel.DEBUG);
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
						destBeat = b + curToss.height().finiteValue();
						destHand = curToss.destHand();
						// check for zero-tosses
						if(destBeat != b || destHand != h) {
							toRunOn.getSite(destBeat, destHand).inDegree += curToss.charge();
						}
					}
				}
			}
		}
		// check if each site's inDegree matches its outDegree
		for(int b=0; b<toRunOn.sites.size(); b++) {
			for(int h=0; h<toRunOn.numHands; h++) {
				if(toRunOn.getSite(b, h).inDegree != toRunOn.getSite(b, h).outDegree()) {
					Util.printf("indegree/outdegree mismatch at beat " + b + ", hand " + h + ":", Util.DebugLevel.DEBUG);
					Util.printf(" indegree: " + toRunOn.getSite(b, h).inDegree, Util.DebugLevel.DEBUG);
					Util.printf("outdegree: " + toRunOn.getSite(b, h).outDegree(), Util.DebugLevel.DEBUG);
					return false;
				}
			}
		}
		Util.printf("all in/outdegrees match; pattern is valid", Util.DebugLevel.DEBUG);
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

	public int maxFiniteHeight() {
		int maxFiniteHeight = 0;
		for(int b=0; b<this.period(); b++) {
			for(int h=0; h<this.numHands(); h++) {
				for(int t=0; t<this.numTossesAtSite(b,h); t++) {
					Toss curToss = this.getToss(b,h,t);
					if(!curToss.height().isInfinite()) {
						int curHeight = curToss.height().finiteValue();
						if(curHeight > maxFiniteHeight) {
							maxFiniteHeight = curHeight;
						}
					}
				}
			}
		}
		return maxFiniteHeight;
	}

	public List<Siteswap> getCycles() {
		if(!this.isValid()) {
			return null;
		}
		if(this.numBalls().numerator().isInfinite()) {
			Util.printf("ERROR: cannot get cycles of siteswap with infinitely many balls", Util.DebugLevel.ERROR);
			return null;
		}
		Siteswap copy = this.deepCopy();
		copy.antitossify(); // TODO: remember which tosses should be antitossified
		// step 1: get max distance away that a toss could be from a destination multiplex site
		int maxDist = this.maxFiniteHeight() + 1;
		// step 2: find multiplex orderings
		Map<Site,List<Toss>> multiplexOrderingMap = new HashMap<>();
		for(int b=0; b<this.period(); b++) {
			for(int h=0; h<this.numHands(); h++) {
				Site curSite = copy.getSite(b,h);
				if(curSite.numTosses() > 1) {
					// we've found a multiplex site
					multiplexOrderingMap.put(curSite, new ArrayList<>());
					for(int targetHeight=0; targetHeight<maxDist; targetHeight++) {
						int b2 = b - targetHeight;
						for(int h2=0; h2<this.numHands(); h2++) {
							for(int t2=0; t2<this.numTossesAtSite(b2,h2); t2++) {
								Toss curToss = copy.getToss(b2,h2,t2);
								if(!curToss.height().isInfinite()) {
									int curHeight = curToss.height().finiteValue();
									if(curHeight == targetHeight) {
										multiplexOrderingMap.get(curSite).add(0, curToss);
									}
								}
							}
						}
					}
				}
			}
		}
		Util.printf(multiplexOrderingMap, Util.DebugLevel.DEBUG);
		// step 3: find cycles
		List<Siteswap> cycles = new ArrayList<Siteswap>();
		for(int b=0; b<this.period(); b++) {
			for(int h=0; h<this.numHands(); h++) {
				Site curSite = copy.getSite(b,h);
				for(int t=0; t<curSite.numTosses();) {
					// cycles must have capacity 1
					if(curSite.getToss(0).isZero()) {
						curSite.removeToss(0);
						break;
					}
					Util.printf("new cycle", Util.DebugLevel.DEBUG);
					Util.printf("b: " + b + ", h: " + h + ", t: " + t, Util.DebugLevel.DEBUG);
					// create new cycle
					Siteswap curCycle = new Siteswap(this.numHands());
					Util.printf("cycle: " + curCycle, Util.DebugLevel.DEBUG);
					Util.printf("this: " + copy, Util.DebugLevel.DEBUG);
					int b2 = b, h2 = h, t2 = t;
					do {
						Util.printf("not done; appending empty tosses up to b=" + (b2+1), Util.DebugLevel.DEBUG);
						curCycle.extendToBeatIndex(b2+1);
						// add next toss to cycle
						Toss curToss = copy.getToss(b2, h2, t2);
						Util.printf("adding toss " + curToss + " to cycle at b=" + b2 + ",h=" + h2, Util.DebugLevel.DEBUG);
						curCycle.addToss(b2, h2, curToss);
						Util.printf("cycle: " + curCycle, Util.DebugLevel.DEBUG);
						Util.printf("removing toss from copy", Util.DebugLevel.DEBUG);
						copy.removeToss(b2, h2, t2);
						Util.printf("this: " + copy, Util.DebugLevel.DEBUG);
						// advance through siteswap
						b2 = b2 + curToss.height().finiteValue();
						h2 = curToss.destHand();
						// ensure correct ordering of multiplex tosses
						Site destSite = copy.getSite(b2,h2);
						if(destSite.numTosses() > 1) {
							t2 = multiplexOrderingMap.get(destSite).indexOf(curToss);
							Util.printf("using proper multiplex ordering: t2=" + t2, Util.DebugLevel.DEBUG);
						} else {
							t2 = 0;
						}
						Util.printf("updating b2,h2,t2", Util.DebugLevel.DEBUG);
						Util.printf("b2=" + b2 + ", h2=" + h2 + ", t2=" + t2, Util.DebugLevel.DEBUG);
					} while(b2 % this.period() != b || h2 != h);
					int newLength = b2 - b;
					Util.printf("done; adding final " + (newLength - curCycle.period()) + " empty tosses", Util.DebugLevel.DEBUG);
					curCycle.extendToBeatIndex(newLength);
					Util.printf("cycle: " + curCycle, Util.DebugLevel.DEBUG);
					Util.printf("", Util.DebugLevel.DEBUG);
					// add cycle to list
					cycles.add(curCycle);
				}
			}
		}
		return cycles;
	}

	public int truePeriod() {
		if(!this.isValid()) {
			return -1;
		}
		List<Siteswap> cycles = this.getCycles();
		int ret = 1;
		for(Siteswap cycle : cycles) {
			ret = Util.lcm(ret, cycle.period());
		}
		return ret;
	}

	public List<Siteswap> getOrbits() {
		if(this.period() == 0) {
			return new ArrayList<>();
		}
		if(!this.isValid()) {
			return null;
		}
		Siteswap copy = this.deepCopy();
		int truePeriod = this.truePeriod();
		int multiple = truePeriod / this.period();
		for(int i=0; i<multiple-1; i++) {
			copy.appendSiteswap(this.deepCopy());
		}
		return copy.getCycles();
	}

	public ExtendedFraction difficulty() {
		Util.printf("WARNING: difficulty calculation not yet implemented", Util.DebugLevel.ERROR);
		// b/(h+h/b)
		// maybe implement arithetic in the Util classes and then just use them here?
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

	public boolean beatIsEmpty(int beatIndex) {
		for(Site site : this.sites.get(beatIndex)) {
			if(!site.isEmpty()) {
				return false;
			}
		}
		return true;
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
		int ret = beatIndex;
		if(beatIndex < 0) {
			Siteswap toAnnex = new Siteswap(this.numHands);
			while(ret < 0) {
				toAnnex.appendEmptyBeat();
				ret++;
			}
			toAnnex.appendSiteswap(this);
			this.sites = toAnnex.sites;
		}
		while(beatIndex > this.period()) {
			this.appendEmptyBeat();
			ret--;
		}
		return ret;
	}

	public void appendSiteswap(Siteswap toAppend) {
		this.sites.addAll(toAppend.sites);
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
										Util.printf("found matching catch at same site; combining into zero-toss", Util.DebugLevel.DEBUG);
										this.exchangeToss(b, h, t,  new Toss(0, h2, curToss.isAntitoss()));
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
											Util.printf("found matching catch at b=" + b2 + ", h=" + h2 + ", t=" + t2 + ": " + curCatch, Util.DebugLevel.DEBUG);
											this.removeToss(b2, h2, t2);
											Toss newToss = new Toss(b2 - b, h2, curToss.charge() == -1);
											Util.printf("new toss: " + newToss, Util.DebugLevel.DEBUG);
											this.exchangeToss(b, h, t, newToss);
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
							this.exchangeToss(b, h, t, newToss);
						} else {
							// add an antitoss to the appropriate site
							Util.printf("found negative non-infinite toss: " + curToss + " at b=" + b + ",h=" + h + ",t=" + t, Util.DebugLevel.DEBUG);
							Toss newToss = new Toss(-1 * curToss.height().finiteValue(), h, true);
							int destBeat = b + curToss.height().finiteValue();
							int destHand = curToss.destHand();
							Util.printf("new toss: " + newToss + ", going to b=" + destBeat + ",h=" + destHand, Util.DebugLevel.DEBUG);
							this.removeToss(b, h, t);
							this.addToss(destBeat, destHand, newToss);
						}
					}
				}
			}
		}
	}

	public void unAntitossify() {
	}

	public void invert() {
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

		// standard constructor - create an empty site
		private Site(int handIndex) {
			this.handIndex = handIndex;
			this.tosses = new ArrayList<Toss>();
			this.inDegree = 0;
		}

		// for deepCopy
		private Site(int handIndex, List<Toss> newTosses) {
			this.handIndex = handIndex;
			this.tosses = newTosses;
			this.inDegree = 0;
		}

		private int numTosses() {
			return this.tosses.size();
		}

		int getOutdegreeOfToss(Toss toss) {
			if(!toss.height().isInfinite() && toss.height().finiteValue() == 0 && toss.destHand() == this.handIndex) {
				return 0;
			} else {
				return toss.charge();
			}
		}

		int outDegree() {
			int outdegree = 0;
			for(Toss toss : this.tosses) {
				outdegree += this.getOutdegreeOfToss(toss);
			}
			return outdegree;
		}

		Toss getToss(int tossIndex) {
			if(tossIndex >= this.tosses.size()) {
				return null;
			}
			return this.tosses.get(tossIndex);
		}

		Toss removeToss(int tossIndex) {
			if(!this.isEmpty()) {
				return this.tosses.remove(tossIndex);
			} else {
				return null;
			}
		}

		void addToss(Toss toss) {
			this.tosses.add(toss);
		}

		void exchangeToss(int tossIndex, Toss newToss) {
			if(this.isEmpty()) {
				Util.printf("ERROR: cannot exchange toss in empty site", Util.DebugLevel.ERROR);
			} else {
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
			return new Site(this.handIndex, newTosses);
		}

		public String toString() {
			return this.tosses.toString();
		}
	}

}
