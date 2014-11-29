import java.util.ArrayList;
import java.util.List;

public class Siteswap {
	protected int numHands;
	protected String type;

	private List<Beat> beats;

	public Siteswap(int numHands, String type) {
		this.numHands = numHands;
		beats = new ArrayList<Beat>();
		this.type = type;
	}

	public Beat addBeat() {
		Beat toReturn = new Beat();
		beats.add(toReturn);
		return toReturn;
	}

	private void addBeat(Beat newBeat) {
		beats.add(newBeat);
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

	public Beat getBeat(int index) {
		return beats.get(index);
	}

	public Beat getLastBeat() {
		return beats.get(beats.size() - 1);
	}

	public Siteswap getSubPattern(int startBeat, int endBeat) {
		//later...
		return null;
	}

	public void annexPattern(Siteswap toAnnex) {
		//later...
	}

	public void removeLastBeat() {
		beats.remove(beats.size() - 1);
	}

	public void addStar() {
		//this operation only makes sense on two-handed siteswaps
		if(numHands != 2) {
			return;
		}
		int oldNumBeats = beats.size();
		//add flipped versions of old beats to end of pattern
		for(int b=0; b<oldNumBeats; b++) {
			addBeat(beats.get(b).starBeat());
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
		protected List<Hand> hands;
		//protected int beatIndex; //not sure if I need this...

		public Beat() {
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

		public String toString() {
			return hands.toString();
		}

		protected class Hand {
			protected List<Toss> tosses;
			protected int handIndex;
			private boolean isEmpty;

			public Hand(int handIndex) {
				this.tosses = new ArrayList<Toss>();
				this.handIndex = handIndex;
				this.isEmpty = true;
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
				if(height != 0) {
					isEmpty = false;
				}
			}

			public void addToss() {
				tosses.add(new Toss(handIndex));
			}

			private void addToss(Toss newToss) {
				tosses.add(newToss);
				if(newToss.height != 0) {
					isEmpty = false;
				}
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

			public boolean isEmpty() {
				return isEmpty;
			}

			public Toss getToss(int index) {
				return tosses.get(index);
			}

			public Toss getLastToss() {
				return tosses.get(tosses.size() - 1);
			}

			public String toString() {
				return tosses.toString();
			}

			protected class Toss {
				protected int startHand;
				protected int height;
				protected int destHand;

				public Toss(int startHand, int height, int destHand) {
					this.startHand = startHand;
					this.height = height;
					this.destHand = destHand;
				}

				public Toss(int startHand) {
					this.height = 0;
					this.startHand = startHand;
					this.destHand = 0;
				}

				public void setDestHand(int newDestHand) {
					this.destHand = newDestHand;
				}

				public void flipDestHand() {
					this.destHand = (this.destHand + 1) % 2;
				}

				private Toss starToss(int newHandIndex) {
					return new Toss(newHandIndex, height, (destHand + 1) % 2);
				}

				public String toString() {
					List<Integer> tossList = new ArrayList<Integer>();
					tossList.add(height);
					tossList.add(destHand);
					return tossList.toString();
				}
			}
		}
	}

}


