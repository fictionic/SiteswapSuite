import java.util.ArrayList;
import java.util.List;

public class Siteswap {
	private int numSites;

	private List<Beat> beats;

	public Siteswap(int numSites) {
		this.numSites = numSites;
		beats = new ArrayList<Beat>();
	}

	public Beat addBeat() {
		Beat toReturn = new Beat();
		beats.add(toReturn);
		return toReturn;
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

	public Beat getBeatAt(int index) {
		return beats.get(index);
	}

	public Beat getLastBeat() {
		return beats.get(beats.size() - 1);
	}

	public String toString() {
		return beats.toString();
	}

	protected class Beat {
		protected List<Site> sites;
		//protected int beatIndex; //not sure if I need this...

		public Beat() {
			sites = new ArrayList<Site>();
			for(int i=0; i<numSites; i++) {
				sites.add(new Site(i));
			}
		}

		public int totalBeatValue() {
			int total = 0;
			for(Site s : sites) {
				total += s.totalSiteValue();
			}
			return total;
		}

		public Site getSiteAt(int index) {
			return sites.get(index);
		}

		public String toString() {
			return sites.toString();
		}

		protected class Site {
			protected List<Toss> tosses;
			protected int siteIndex;

			public Site(int siteIndex) {
				tosses = new ArrayList<Toss>();
				this.siteIndex = siteIndex;
			}
			
			public int totalSiteValue() {
				int total = 0;
				for(Toss t : tosses) {
					total += t.height();
				}
				return total;
			}
			public void addToss(int height, int destSite) {
				tosses.add(new Toss(siteIndex, height, destSite));	
			}

			public void addToss() {
				tosses.add(new Toss(siteIndex));
			}

			public int numTosses() {
				return tosses.size();
			}

			public Toss getTossAt(int index) {
				return tosses.get(index);
			}

			public Toss getLastToss() {
				return tosses.get(tosses.size() - 1);
			}

			public String toString() {
				return tosses.toString();
			}

			protected class Toss {
				protected int startSite;
				protected int height;
				protected int destSite;

				public Toss(int startSite, int height, int destSite) {
					this.startSite = startSite;
					this.height = height;
					this.destSite = destSite;
				}

				public Toss(int startSite) {
					this.height = 0;
					this.startSite = startSite;
					this.destSite = 0;
				}

				public int height() {
					return height;
				}
				public int destSite() {
					return destSite;
				}

				public void setDestSite(int newDestSite) {
					this.destSite = newDestSite;
				}

				public void flipDestHand() {
					this.destSite = (this.destSite + 1) % 2;
				}

				public String toString() {
					List<Integer> tossList = new ArrayList<Integer>();
					tossList.add(height);
					tossList.add(destSite);
					return tossList.toString();
				}
			}
		}
	}

}


