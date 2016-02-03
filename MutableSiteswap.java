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
		// reset inDegree of each site
		for(int b=0; b<this.sites.size(); b++) {
			for(int h=0; h<this.numHands; h++) {
				this.getSite(b, h).inDegree = 0;
			}
		}
		// calculate inDegree of each site
		Toss curToss;
		int destBeat;
		int destHand;
		for(int b=0; b<this.period(); b++) {
			for(int h=0; h<this.numHands; h++) {
				for(int t=0; t<this.numTossesAtSite(b, h); t++) {
					curToss = this.getToss(b, h, t);
					if(curToss.charge() != 0 && !curToss.height().isInfinite()) {
						destBeat = (b + curToss.height().finiteValue()) % this.period();
						if(destBeat < 0)
							destBeat += this.period();
						destHand = curToss.destHand();
						this.getSite(destBeat, destHand).inDegree += curToss.charge();
					}
				}
			}
		}
		// check if each site's inDegree matches its outDegree (numTosses)
		for(int b=0; b<this.sites.size(); b++) {
			for(int h=0; h<this.numHands; h++) {
				if(this.getSite(b, h).inDegree != this.getSite(b, h).outDegree)
					return false;
			}
		}
		return true;
	}

	Site getSite(int beatIndex, int handIndex) {
		if(beatIndex < 0)
			beatIndex += this.sites.size();
		else
			beatIndex = beatIndex % this.sites.size();
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

	// removing tosses
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

	public void appendSiteswap(MutableSiteswap toApppend) {
		return;
	}

	// manipulating pattern
	public void starify() {
	}

	public void infinitize() {
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
						ret.getSite(b, h).removeToss(t);
						// check if it's infinite
						if(curToss.height().isInfinite()) {
							// replace the negative toss with an antitoss in the same site
							Toss newToss = new Toss(InfinityType.POSITIVE_INFINITY, true);
							ret.addToss(b, h, newToss);
						} else {
							// add an antitoss to the appropriate site
							Toss newToss = new Toss(-curToss.height().finiteValue(), h, true);
							ret.addToss(b - curToss.height().finiteValue(), curToss.destHand(), newToss);
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

	public List<MutableSiteswap> unInfinitize() {
		return null;
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
}
