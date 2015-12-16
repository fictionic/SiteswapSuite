public class Toss {
	
	private int beatIndex;
	private int sourceHand;

	private ExtendedNaturalNumber height;
	private boolean isAntitoss;
	private Integer destHand;

	public Toss(int emptyHandIndex) {
			this.height = new ExtendedNaturalNumber(0);
			this.destHand = emptyHandIndex;
			this.sourceHand = emptyHandIndex;
			this.isAntitoss = false;
	}

	public Toss(int height, int destHand, boolean isAntitoss) {
			this.height = new ExtendedNaturalNumber(height);
			this.destHand = destHand;
			this.isAntitoss = isAntitoss;
	}

	public Toss(InfinityType height, boolean isAntitoss) {
			this.height = new ExtendedNaturalNumber(height);
			this.destHand = null;
			this.isAntitoss = isAntitoss;
	}

	public ExtendedNaturalNumber height() {
			return this.height;
	}

	public Integer destHand() {
			return this.destHand;
	}

	public String toString() {
			String out = "[";
			if(this.height.isInfinite()) {
					if(this.height == InfinityType.NEGATIVE_INFINITY)
							out += "-";
					if(this.isAntitoss)
							out += "_";
					out += "&]";
			else {
					if(this.height < 0)
							out += "-";
					if(this.isAntitoss)
							out += "_";
					out += height.finiteValue().toString() + ", " + destHand.toString() + "]";
			}
			return out;
	}

}
