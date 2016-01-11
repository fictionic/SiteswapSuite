package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class MutableSiteswap {

	int numHands;
	List<List<MutableSite>> sites;

	// main constructor - initialize a completely empty siteswap with the appropriate number of hands
	public MutableSiteswap(int numHands) {
		this.numHands = numHands;
		this.sites = new ArrayList<List<MutableSite>>();
	}

	// private constructor, only for subPattern()
	private MutableSiteswap(int numHands, List<List<MutableSite>> sites) {
		this.numHands = numHands;
		this.sites = sites;
	}

	// degenerate constructor, only for use in NotatedSiteswap
	/*MutableSiteswap(Siteswap ss) {
		this.numHands = ss.numHands;
		this.sites = new ArrayList<List<MutableSite>>();
		for(int b=0; b<ss.period(); b++) {
			this.sites.add(new ArrayList<MutableSite>());
			for(int h=0; h<ss.numHands(); h++) {
				this.sites.get(b).add(new MutableSite(ss.getSite(b, h)));
			}
		}
		this.sites = ss.sites;
	}*/

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
		toRunOn.unInfinitize();
		toRunOn.unAntitossify();
		int finiteValue = 0;
		int numInfinities = 0;
		ExtendedInteger curTossHeight;
		for(int b=0; b<toRunOn.sites.size(); b++) {
			for(int h=0; h<toRunOn.numHands; h++) {
				for(int t=0; t<toRunOn.numTossesAtSite(b, h); t++) {
					curTossHeight = toRunOn.getToss(b, h, t).height();
					if(curTossHeight.isInfinite()) {
						if(curTossHeight.infiniteValue() == InfinityType.POSITIVE_INFINITY)
							numInfinities++;
						else
							numInfinities--;
					} else
						finiteValue += curTossHeight.finiteValue();
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

	private MutableSite getSite(int beatIndex, int handIndex) {
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
		this.sites.add(new ArrayList<MutableSite>());
	}

	public int extendToBeatIndex(int beatIndex) { //returns index of beat that was previously "at" given index (either 0 or period(), if any extending happens)
		return 0;
	}

	public void appendSiteswap(MutableSiteswap toApppend) {
		return;
	}

	// manipulating pattern
	public void antitossify() {
	}

	public void unAntitossify() {
	}

	public void starify() {
	}

	public void infinitize() {
	}

	// misc
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
		List<List<MutableSite>> newSites = new ArrayList<List<MutableSite>>();
		for(int b=startBeat; b<endBeat; b++) {
			List<MutableSite> newBeat = new ArrayList<MutableSite>();
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

	private class MutableSite {
		private List<Toss> tosses;
		private int handIndex;
		private int inDegree;
		private int outDegree;

		// standard constructor - create an empty site
		private MutableSite(int handIndex) {
			this.handIndex = handIndex;
			this.tosses = new ArrayList<Toss>();
			this.tosses.add(new Toss(handIndex));
			this.inDegree = 0;
			this.outDegree = 0;
		}

		// for deepCopy
		private MutableSite(int handIndex, List<Toss> newTosses, int newOutDegree) {
			this.handIndex = handIndex;
			this.tosses = newTosses;
			this.outDegree = newOutDegree;
			this.inDegree = 0;
		}

		// for converting from Site
		private MutableSite(Siteswap.Site site) {
			this.handIndex = site.handIndex;
			this.tosses = site.tosses;
		}

		private int numTosses() {
			return this.tosses.size();
		}

		private int outDegree() {
			return this.outDegree;
		}

		private Toss getToss(int tossIndex) {
			return this.tosses.get(tossIndex);
		}

		private void addToss(Toss toss) {
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

		private MutableSite deepCopy() {
			List<Toss> newTosses = new ArrayList<Toss>();
			for(int t=0; t<this.tosses.size(); t++) {
				newTosses.add(this.tosses.get(t));
			}
			return new MutableSite(this.handIndex, newTosses, this.outDegree);
		}

		public String toString() {
			return this.tosses.toString();
		}
	}
}
