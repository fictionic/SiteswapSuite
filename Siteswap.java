package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class Siteswap {

	int numHands;
	List<Beat> beats;

	// constructor - initialize a completely empty siteswap with the appropriate number of hands
	public Siteswap(int numHands) {
		this.numHands = numHands;
		this.beats = new ArrayList<Beat>();
	}

	// querying basic info
	public int numHands() {
		return this.numHands;
	}

	public int period() {
		return this.beats.size();
	}

	// computing more complicated info
	public ExtendedFraction numBalls() {
		Siteswap toRunOn = this.deepCopy();
		toRunOn.unInfinitize();
		toRunOn.unAntitossify();
		int finiteValue = 0;
		int numInfinities = 0;
		ExtendedInteger curTossHeight;
		for(int b=0; b<toRunOn.beats.size(); b++) {
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
		return new ExtendedFraction(top, this.beats.size());
	}

	public boolean isValid() {
		// reset inDegree of each site
		for(int b=0; b<this.beats.size(); b++) {
			for(int h=0; h<this.numHands; h++) {
				this.beats.get(b).hands.get(h).inDegree = 0;
			}
		}
		// calculate inDegree of each site
		Toss curToss;
		int destBeat;
		int destHand;
		for(int b=0; b<this.beats.size(); b++) {
			for(int h=0; h<this.numHands; h++) {
				for(int t=0; t<this.numTossesAtSite(b, h); t++) {
					curToss = this.getToss(b, h, t);
					if(curToss.charge() != 0 && !curToss.height().isInfinite()) {
						destBeat = (b + curToss.height().finiteValue()) % this.beats.size();
						destHand = curToss.destHand();
						this.getSite(destBeat, destHand).inDegree += curToss.charge();
					}
				}
			}
		}
		// check if each site's inDegree matches its outDegree (numTosses)
		for(int b=0; b<this.beats.size(); b++) {
			for(int h=0; h<this.numHands; h++) {
				if(this.beats.get(b).hands.get(h).inDegree != this.beats.get(b).hands.get(h).outDegree)
					return false;
			}
		}
		return true;
	}

	private Beat getBeat(int beatIndex) {
		if(beatIndex < 0)
			beatIndex += this.beats.size();
		else
			beatIndex = beatIndex % this.beats.size();
		return this.beats.get(beatIndex);
	}

	private Beat.Hand getSite(int beatIndex, int handIndex) {
		return this.getBeat(beatIndex).hands.get(handIndex);
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
	public void addFiniteToss(int atbeat, int fromhand, int height, int tohand) {
		this.getSite(atbeat, fromhand).addFiniteToss(height, tohand);
	}

	public void addFiniteAntitoss(int atBeat, int fromHand, int height, int toHand) {
		this.getSite(atBeat, fromHand).addFiniteAntitoss(height, toHand);
	}

	public void addInfiniteToss(int atBeat, int fromHand, InfinityType height) {
		this.getSite(atBeat, fromHand).addInfiniteToss(height);
	}

	public void addInfiniteAntitoss(int atBeat, int fromHand, InfinityType height) {
		this.getSite(atBeat, fromHand).addInfiniteAntitoss(height);
	}

	// removing tosses
	public void removeBeat(int beatIndex) {
		this.beats.remove(beatIndex);
		if(this.beats.size() == 0)
			this.appendEmptyBeat();
	}

	public void removeLastBeat() {
		this.removeBeat(this.beats.size() - 1);
	}

	// extending pattern
	public void appendEmptyBeat() {
		this.beats.add(new Beat(this.beats.size()));
	}

	public int extendToBeatIndex(int beatIndex) { //returns index of beat that was previously "at" given index (either 0 or period(), if any extending happens)
		return 0;
	}

	public void appendSiteswap(Siteswap toApppend) {
		return;
	}

	// manipulating pattern
	public void antitossify() {
	}

	public void unAntitossify() {
	}

	public void starify() {
	}

	public void springify() {
	}

	public void unInfinitize() {
	}

	// misc
	public List<Siteswap> infinitize() {
		return null;
	}

	public Siteswap getInverse() {
		return null;
	}

	public String toString() {
		return this.beats.toString();
	}

	public Siteswap deepCopy() {
		return null;
	}

	// nested class
	protected class Beat {
		private List<Hand> hands;
		private int beatIndex;

		private Beat(int beatIndex) {
			this.beatIndex = beatIndex;
			this.hands = new ArrayList<Hand>();
			for(int i=0; i<numHands; i++) {
				this.hands.add(new Hand(i));
			}
		}

		private Beat(int beatIndex, List<Hand> handsList) {
			this.beatIndex = beatIndex;
			this.hands = handsList;
		}

		private Beat deepCopy() {
			List<Hand> newHands = new ArrayList<Hand>();
			for(int h=0; h<this.hands.size(); h++) {
				newHands.add(this.hands.get(h));
			}
			return new Beat(this.beatIndex, newHands);
		}

		public String toString() {
			return this.hands.toString();
		}

		private class Hand {
			private List<Toss> tosses;
			private int handIndex;
			private int inDegree;
			private int outDegree;

			private Hand(int handIndex) {
				this.handIndex = handIndex;
				this.tosses = new ArrayList<Toss>();
				this.tosses.add(new Toss(handIndex));
			}

			// for deepCopy
			private Hand(int handIndex, List<Toss> newTosses, int newOutDegree) {
				this.handIndex = handIndex;
				this.tosses = newTosses;
				this.outDegree = newOutDegree;
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

			private void addFiniteToss(int height, int destHand) {
				if(this.isEmpty() && height != 0)
					this.tosses.remove(0);
				if(height != 0) {
					Toss toAdd = new Toss(height, destHand, false);
					this.tosses.add(toAdd);
					this.outDegree++;
				}
			}

			private void addFiniteAntitoss(int height, int destHand) {
				if(this.isEmpty() && height != 0)
					this.tosses.remove(0);
				if(height != 0) {
					Toss toAdd = new Toss(height, destHand, true);
					this.tosses.add(toAdd);
					this.outDegree--;
				}
			}

			private void addInfiniteToss(InfinityType height) {
				if(this.isEmpty())
					this.tosses.remove(0);
				Toss toAdd = new Toss(height, false);
				this.tosses.add(toAdd);
				this.outDegree++;
			}

			private void addInfiniteAntitoss(InfinityType height) {
				if(this.isEmpty())
					this.tosses.remove(0);
				Toss toAdd = new Toss(height, true);
				this.tosses.add(toAdd);
				this.outDegree--;
			}

			private boolean isEmpty() {
				if(this.tosses.size() == 1 && this.tosses.get(0).charge() == 0)
					return true;
				else
					return false;
			}

			private Hand deepCopy() {
				List<Toss> newTosses = new ArrayList<Toss>();
				for(int t=0; t<this.tosses.size(); t++) {
					newTosses.add(this.tosses.get(t));
				}
				return new Hand(this.handIndex, newTosses, this.outDegree);
			}

			public String toString() {
				return this.tosses.toString();
			}
		}
	}
}
