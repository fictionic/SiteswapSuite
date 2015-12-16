public class Siteswap {
		
		private int numHands;
		private NotationType notationType;
		private List<Beat> beats;

		// constructors
		public Siteswap(int numHands, NotationType notationType) {
				this.numHands = numHands;
				this.notationType = notationType;
				this.beats = new ArrayList<Beat>();
				this.addZeroBeat();
		}

		// querying basic info
		public int numHands() {
				return this.numHands;
		}

		public int period() {
				return this.beats.size();
		}

		// computing more complicated info
		public ExtendedNaturalNumber numBalls();

		public boolean isValid();

		// for getting tosses
		public int numTossesAtSite(int atBeat, int fromHand);

		public Toss getToss(int atBeat, int fromHand, int tossIndex);

		// adding tosses
		public void addFiniteToss(int atBeat, int fromHand, int height, int toHand) throws BeatIndexOutOfRangeException, HandIndexOutOfRangeException;

		public void addFiniteAntitoss(int atBeat, int fromHand, int height, int toHand) throws BeatIndexOutOfRangeException, HandIndexOutOfRangeException;

		public void addInfiniteToss(int atBeat, int fromHand, InfinityType sign) throws BeatIndexOutOfRangeException, HandIndexOutOfRangeException;

		public void addInfiniteAntitoss(int atBeat, int fromHand, InfinityType sign) throws BeatIndexOutOfRangeException, HandIndexOutOfRangeException;

		// extending pattern
		public void appendZeroBeat();

		public void appendBeat(Beat toAppend);

		public int extendToBeatIndex(int beatIndex); //returns index of beat that was previously "at" given index (either 0 or period(), if any extending happens)

		public void appendSiteswap(Siteswap toApppend);

		// manipulating pattern
		public void antitossify();

		public void unAntitossify();

		public void starify();

		public void springify();

		public void unInfinitize();

		// misc
		public List<Siteswap> infinitize();

		public String toString() {
				return this.beats.toString();
		}

		public Siteswap deepCopy();
		
		// nested class
		private class Beat {
				private List<Hand> hands;
				private int beatIndex;

				private Beat(int beatIndex) {
						this.beatIndex = beatIndex;
						this.hands = new ArrayList<Hand>();
						for(int i=0; i<numHands; i++) {
								this.hands.add(new Hand(this.beatIndex, i);
						}
				}

				private Beat(int beatIndex, List<Hand> handsList) {
					this.beatIndex = beatIndex;
					this.hands = handsList;
				}

				private Beat deepCopy() {
					List<Hand> newHands = new ArrayList<Hand>();
					for(int h=0; h<this.hands.size(); h++) {
						newHands.add(this.hands.get(i));
					}
					return new Beat(this.beatIndex, newHands);
				}

				public String toString() {
					return this.hands.toString();
				}

				private class Hand {
					private List<Toss> tosses;
					private int beatIndex;
					private int handIndex;

					private Hand(int beatIndex, int handIndex) {
						this.beatIndex = beatIndex;
						this.handIndex = handIndex;
						this.tosses = new ArrayList<Toss>();
						this.tosses.add(new Toss(handIndex));
					}
				}
		}
}
