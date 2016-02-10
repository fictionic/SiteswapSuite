package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class MutableSiteswap implements Siteswap {

	int numHands;
	List<List<Site>> sites;

	// main constructor - initialize a completely empty siteswap with the appropriate number of hands
	public MutableSiteswap(int numHands) {
		this.numHands = numHands;
		this.sites = new ArrayList<List<Site>>();
	}

	// private constructor, only for subPattern() and NotatedSiteswap
	MutableSiteswap(int numHands, List<List<Site>> sites) {
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
		MutableSiteswap toRunOn = this.deepCopy();
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
							if(!curToss.isAntitoss())
								numInfinities++;
							else
								numInfinities--;
						} else {
							if(!curToss.isAntitoss())
								numInfinities--;
							else
								numInfinities++;
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
			if(numInfinities > 0)
				top = new ExtendedInteger(InfinityType.POSITIVE_INFINITY);
			else
				top = new ExtendedInteger(InfinityType.NEGATIVE_INFINITY);
		}
		return new ExtendedFraction(top, this.sites.size());
	}

	public boolean isValid() {
		MutableSiteswap toRunOn = this.unInfinitize();
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
					if(curToss.charge() != 0 && !curToss.height().isInfinite()) {
						destBeat = (b + curToss.height().finiteValue()) % toRunOn.period();
						if(destBeat < 0)
							destBeat += toRunOn.period();
						destHand = curToss.destHand();
						toRunOn.getSite(destBeat, destHand).inDegree += curToss.charge();
					}
				}
			}
		}
		// check if each site's inDegree matches its outDegree (numTosses)
		for(int b=0; b<toRunOn.sites.size(); b++) {
			for(int h=0; h<toRunOn.numHands; h++) {
				if(toRunOn.getSite(b, h).inDegree != toRunOn.getSite(b, h).outDegree)
					return false;
			}
		}
		return true;
	}

	Site getSite(int beatIndex, int handIndex) {
		beatIndex = beatIndex % this.sites.size();
		if(beatIndex < 0)
			beatIndex += this.sites.size();
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
			MutableSiteswap toAnnex = new MutableSiteswap(this.numHands);
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

	public void appendSiteswap(MutableSiteswap toAppend) {
		return;
	}

	// manipulating pattern
	public void starify() {
		if(this.numHands() != 2)
			return;
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

	public MutableSiteswap unInfinitize() {
		MutableSiteswap ret = this.deepCopy();
		// look for tosses/antitosses of height &
		for(int b=0; b<ret.period(); b++) {
			for(int h=0; h<ret.numHands(); h++) {
				for(int t=0; t<ret.numTossesAtSite(b, h); t++) {
					Toss curToss = ret.getToss(b, h, t);
					if(curToss.height().isInfinite() && curToss.height().infiniteValue() == InfinityType.POSITIVE_INFINITY) {
						search: {
							// first search this site to see if we can make zero-tosses
							for(int t2=0; t2<ret.numTossesAtSite(b, h); t2++) {
								Toss curCatch = ret.getToss(b, h, t2);
								if(curCatch.charge() == curToss.charge() &&
										curCatch.height().isInfinite() &&
										curCatch.height().infiniteValue() == InfinityType.NEGATIVE_INFINITY) {
									ret.removeToss(b, h, t2);
									ret.exchangeToss(b, h, t, new Toss(h));
									break search;
								}
							}
							// now search for positive tosses
							for(int b2=b+1; b2<b+1+ret.period(); b2++) {
								for(int h2=0; h2<ret.numHands(); h2++) {
									for(int t2=0; t2<ret.numTossesAtSite(b2, h2); t2++) {
										Toss curCatch = ret.getToss(b2, h2, t2);
										if(curCatch.charge() == curToss.charge() &&
												curCatch.height().isInfinite() &&
												curCatch.height().infiniteValue() == InfinityType.NEGATIVE_INFINITY) {
											ret.removeToss(b2, h2, t2);
											ret.exchangeToss(b, h, t, new Toss(b2 - b, h2, curToss.charge() == 1));
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
		return ret;
	}

	// misc
	public MutableSiteswap antitossify() {
		MutableSiteswap ret = this.deepCopy();
		// loop through beats
		for(int b=0; b<ret.period(); b++) {
			// loop through hands
			for(int h=0; h<ret.numHands(); h++) {
				// loop through tosses
				for(int t=0; t<ret.numTossesAtSite(b, h); t++) {
					Toss curToss = ret.getToss(b, h, t);
					// check if it's a negative toss
					if(curToss.height().sign() < 0) {
						// check if it's infinite
						if(curToss.height().isInfinite()) {
							// replace the negative toss with an antitoss in the same site
							Toss newToss = new Toss(InfinityType.POSITIVE_INFINITY, true);
							ret.exchangeToss(b, h, t, newToss);
						} else {
							// add an antitoss to the appropriate site
							Toss newToss = new Toss(-curToss.height().finiteValue(), h, true);
							ret.exchangeToss(b + curToss.height().finiteValue(), curToss.destHand(), t, newToss);
						}
					}
				}
			}
		}
		return ret;
	}

	public MutableSiteswap unAntitossify() {
		return this;
	}

	public final MutableSiteswap getSprungPattern() {
		return null;
	}

	public final MutableSiteswap getInverse() {
		return null;
	}

	public MutableSiteswap subPattern(int startBeat, int endBeat) {
		//get deep copy of each beat within specified indices
		List<List<Site>> newSites = new ArrayList<List<Site>>();
		for(int b=startBeat; b<endBeat; b++) {
			List<Site> newBeat = new ArrayList<Site>();
			for(int h=0; h<this.numHands; h++) {
				newBeat.add(this.getSite(b, h).deepCopy());
			}
			newSites.add(newBeat);
		}
		return new MutableSiteswap(this.numHands, newSites);
	}

	public String toString() {
		return this.sites.toString();
	}

	public MutableSiteswap deepCopy() {
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
			this.tosses.add(new Toss(handIndex));
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
			return this.tosses.get(tossIndex);
		}

		Toss removeToss(int tossIndex) {
			if(!this.isEmpty()) {
				this.outDegree -= this.tosses.get(tossIndex).charge();
				Toss toReturn = this.tosses.remove(tossIndex);
				if(this.tosses.size() == 0)
					this.tosses.add(new Toss(this.handIndex));
				return toReturn;
			} else
				return null;
		}

		void addToss(Toss toss) {
			if(toss.charge() != 0) {
				if(this.isEmpty())
					this.tosses.remove(0);
				this.tosses.add(toss);
				this.outDegree += toss.charge();
			}
		}

		void exchangeToss(int tossIndex, Toss newToss) {
			this.outDegree += newToss.charge() - this.tosses.get(tossIndex).charge();
			this.tosses.set(tossIndex, newToss);
		}

		private boolean isEmpty() {
			if(this.tosses.size() == 1 && this.tosses.get(0).charge() == 0)
				return true;
			else
				return false;
		}

		private Site deepCopy() {
			List<Toss> newTosses = new ArrayList<Toss>();
			for(int t=0; t<this.tosses.size(); t++) {
				newTosses.add(this.tosses.get(t));
			}
			return new Site(this.handIndex, newTosses, this.outDegree);
		}

		public String toString() {
			return this.tosses.toString();
		}
	}

	public static void main(String[] args) {
		try {
		NotatedSiteswap ss = NotatedSiteswap.parseSingle(args[0]);
		System.out.println(ss.print());
		System.out.println(ss.unInfinitize());
		try {
			NotatedSiteswap ss2 = NotatedSiteswap.assemble(ss.unInfinitize(), ss.notationType());
			System.out.println(ss2.print());
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
