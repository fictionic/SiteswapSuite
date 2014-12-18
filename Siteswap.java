import java.util.ArrayList;
import java.util.List;

public class Siteswap {
	private int numHands;
	private String type;
	private List<Beat> beats;
	private boolean hasInDegree;
	private Integer largestTossHeight;

	public Siteswap(int numHands, String type) {
		this.numHands = numHands;
		this.beats = new ArrayList<Beat>();
		this.type = type;
		this.hasInDegree = false;
	}

	private Siteswap(List<Beat> beats, int numHands, String type) {
		this.beats = beats;
		this.numHands = numHands;
		this.type = type;
		this.hasInDegree = false;
	}

	public int numHands() {
		return numHands;
	}

	public String type() {
		return type;
	}

	public boolean hasInDegree() {
		return hasInDegree;
	}

	public double numBalls() {
		int total = 0;
		for(Beat b : beats) {
			total += b.totalBeatValue();
		}
		return (double)total/(double)beats.size();
	}

	public int period() {
		return beats.size();
	}

	private void calculateInDegree() {
		//calculate the indegree of each node (beat.hand)
		//(better than calculating it as the siteswap is created, because
		//a given node might not exist when adding a node that throws to it)

		//reset all indegrees, since we don't want to increment them if they don't start at zero
		for(int b=0; b<period(); b++) {
			for(int h=0; h<getBeat(b).numHands(); h++) {
				getBeat(b).getHand(h).inDegree = 0;
			}
		}

		int height;
		int destHand;
		int toBeat;
		for(int b=0; b<period(); b++) {
			for(int h=0; h<getBeat(b).numHands(); h++) {
				for(int t=0; t<getBeat(b).getHand(h).numTosses(); t++) {
					height = getBeat(b).getHand(h).getToss(t).height;
					destHand = getBeat(b).getHand(h).getToss(t).destHand;
					toBeat = (b + height) % period();
					getBeat(toBeat).getHand(destHand).inDegree++;
				}
			}
		}
		hasInDegree = true;
	}

	public int getLargestTossHeight() {
		//at some point change this so it automatically updates whenever you add a toss (or a beat, or just alter the pattern in any way)
		for(Beat b : beats) {
			for(Beat.Hand h : b.hands) {
				for(Beat.Hand.Toss t : h.tosses) {
					if(largestTossHeight == null || t.height > largestTossHeight) {
						largestTossHeight = t.height;
					}
				}
			}
		}
		return largestTossHeight;
	}

	public boolean isValid() {
		if(!hasInDegree) {
			calculateInDegree();
		}
		//see if each node's indegree equals its outdegree 
		for(int b=0; b<period(); b++) {
			for(int h=0; h<getBeat(b).numHands(); h++) {
				if(getBeat(b).getHand(h).inDegree != getBeat(b).getHand(h).numTosses()) {
					return false;
					//inDegree calculation factors in throws of height zero, so we can count them in numTosses as well
				}
			}
		}
		return true;
	}

	public void addBeat(Beat newBeat) {
		beats.add(newBeat);
		hasInDegree = false;
	}

	//EVENTUALLY GET RID OF THIS METHOD, MAKE NEW BEATS ALWAYS BE ZERO BEATS, AND REMOVE THE ZERO TOSSES WHEN ADDING NONZERO TOSSES
	public Beat addEmptyBeat() {
		Beat emptyBeat = new Beat();
		beats.add(emptyBeat);
		return emptyBeat;
	}

	public void addZeroBeat() {
		Beat zeroBeat = new Beat();
		for(int h=0; h<zeroBeat.numHands(); h++) {
			zeroBeat.getHand(h).addToss();
		}
		beats.add(zeroBeat);
	}

	//adds a new toss from the given hand at the given beat to the given desthand with the given height
	public boolean addToss(int atBeat, int atHand, int tossHeight, int destHand) {
		if(atBeat >= period() || atHand > numHands || destHand > numHands) {
			return false;
		} else {
			getBeat(atBeat).getHand(atHand).addToss(tossHeight, destHand);
			return true;
		}
	}

	public Beat getBeat(int index) {
		return beats.get(index);
	}

	public Beat getLastBeat() {
		if(beats.size() < 1) {
			return null;
		} else {
			return beats.get(beats.size() - 1);
		}
	}

	public Siteswap getSubPattern(int startBeat, int endBeat) {
		//get deep copy of each beat within specified indices
		List<Beat> newBeats = new ArrayList<Beat>();
		for(int b=startBeat; b<=endBeat; b++) {
			newBeats.add(beats.get(b).deepCopy());	
		}
		return new Siteswap(newBeats, numHands, type);
	}

	public void annexPattern(Siteswap toAnnex) {
		for(int b=0; b<toAnnex.period(); b++) {
			addBeat(toAnnex.getBeat(b));
		}
	}

	public void removeLastBeat() {
		beats.remove(beats.size() - 1);
	}

	public void addStar() {
		//this operation only makes sense on two-handed siteswaps
		if(numHands != 2) {
			return;
		}
		//save old period
		int oldPeriod = period();
		//add flipped versions of old beats to end of pattern
		for(int b=0; b<oldPeriod; b++) {
			addBeat(getBeat(b).starBeat());
		}
	}

	public Siteswap sprungify() {
		return null;
		//later...
	}

	public String toString() {
		return beats.toString();
	}

	protected class Beat {
		private List<Hand> hands;

		private Beat() {
			hands = new ArrayList<Hand>();
			for(int i=0; i<numHands; i++) {
				hands.add(new Hand(i));
			}
		}

		private Beat(List<Hand> handList) {
			hands = handList;
		}

		public int totalBeatValue() {
			int total = 0;
			for(Hand s : hands) {
				total += s.totalHandValue();
			}
			return total;
		}

		public int numHands() {
			return hands.size();
		}

		public boolean isZeroBeat() {
			for(Hand h : hands) {
				if(!h.isZeroHand()) {
					return false;
				}
			}
			return true;
		}

		public Hand getHand(int index) {
			return hands.get(index);
		}

		private Beat starBeat() {
			if(numHands != 2) {
				//this should never happen, b/c the parent method returns void...
				return this;
			}
			//since there is no addHand() method (because that wouldn't make sense in any situation where you aren't just creating a new beat)
			Beat newBeat = new Beat();
			List<Hand> newHandList = new ArrayList<Hand>();
			//add old right hand as new left hand, and vice-versa
			newHandList.add(hands.get(1).starHand());
			newHandList.add(hands.get(0).starHand());
			return new Beat(newHandList);
		}

		private Beat deepCopy() {
			//get deep copy of each hand in hands
			List<Hand> newHands = new ArrayList<Hand>();
			for(int h=0; h<hands.size(); h++) {
				newHands.add(hands.get(h).deepCopy());
			}
			return new Beat(newHands);
		}

		public String toString() {
			return hands.toString();
		}

		protected class Hand {
			protected List<Toss> tosses;
			protected int handIndex;
			private boolean isEmpty;
			private int inDegree;

			public Hand(int handIndex) {
				this.tosses = new ArrayList<Toss>();
				this.handIndex = handIndex;
				this.isEmpty = true;
				this.inDegree = 0;
			}

			private Hand(List<Toss> newTosses, int newHandIndex, boolean newIsEmpty) {
				this.tosses = newTosses;
				this.handIndex = newHandIndex;
				this.isEmpty = newIsEmpty;
				this.inDegree = 0;
			}

			public int totalHandValue() {
				int total = 0;
				for(Toss t : tosses) {
					total += t.height;
				}
				return total;
			}
			public void addToss(int height, int destHand) {
				tosses.add(new Toss(handIndex, height, destHand));	
				isEmpty = false;
				hasInDegree = false;
			}

			public void addToss() {
				tosses.add(new Toss(handIndex));
				hasInDegree = false;
			}

			private void addToss(Toss newToss) {
				tosses.add(newToss);
				if(newToss.height != 0) {
					isEmpty = false;
				}
				hasInDegree = false;
			}

			private Hand starHand() {
				//flip hand index
				int newHandIndex = (handIndex + 1) % 2;
				Hand newHand = new Hand(newHandIndex);
				//add a copy of all tosses within this hand with altered startHand and destHand values
				for(Toss t : tosses) {
					newHand.addToss(t.starToss(newHandIndex));
				}
				return newHand;
			}

			public int numTosses() {
				return tosses.size();
			}

			private boolean isZeroHand() {
				if(tosses.size() > 1) {
					return false;
				}
				if(!tosses.get(0).isZeroToss()) {
					return false;
				}
				return true;
			}

			public int numNonZeroTosses() {
				int out = 0;
				for(Toss t : tosses) {
					if(!t.isZeroToss()) {
						out++;
					}
				}
				return out;
			}

			public boolean isEmpty() {
				return isEmpty;
			}

			public Toss getToss(int index) {
				return tosses.get(index);
			}

			public Toss getLastToss() {
				return tosses.get(tosses.size() - 1);
			}

			private Hand deepCopy() {
				//get deep copy of each toss in tosses
				List<Toss> newTosses = new ArrayList<Toss>();
				for(int t=0; t<tosses.size(); t++) {
					newTosses.add(tosses.get(t).deepCopy());
				}
				return new Hand(newTosses, handIndex, isEmpty);
			}

			public String toString() {
				return tosses.toString();
			}

			protected class Toss {
				private int startHand;
				private int height;
				private boolean isInfinity;
				private int destHand;

				public Toss(int startHand, int height, int destHand) {
					this.startHand = startHand;
					this.height = height;
					this.isInfinity = false;
					this.destHand = destHand;
				}

				public Toss(int startHand, int height, boolean isInfinity, int destHand) {
					this.startHand = startHand;
					this.height = height;
					this.isInfinity = isInfinity;
					this.destHand = destHand;
				}

				public Toss(int startHand) {
					this.height = 0;
					this.isInfinity = false;
					this.startHand = startHand;
					this.destHand = startHand;
				}

				public int height() {
					return height;
				}

				public boolean isInfinity() {
					return isInfinity;
				}

				public int startHand() {
					return startHand;
				}

				private boolean isZeroToss() {
					return (startHand == destHand && height == 0);
				}

				public int destHand() {
					return destHand;
				}

				public void setDestHand(int newDestHand) {
					this.destHand = newDestHand;
					hasInDegree = false;
				}

				public void flipDestHand() {
					this.destHand = (this.destHand + 1) % 2;
					hasInDegree = false;
				}

				private Toss starToss(int newHandIndex) {
					return new Toss(newHandIndex, height, isInfinity, (destHand + 1) % 2);
				}

				private Toss deepCopy() {
					return new Toss(startHand, height, isInfinity, destHand);
				}

				public String toString() {
					List<Integer> listToss = new ArrayList<Integer>();
					if(!isInfinity) {
						listToss.add(height);
					} else {
						if(height < 0) {
							//negative infinity
							listToss.add("-&");
						} else if(height > 0) {
							//positive infinity
							listToss.add("&");
						} else {
							//don't know how this would happen
							listToss.add("0");
						}
					}
					listToss.add(destHand);
					return listToss.toString();
				}
			}
		}
	}

}


