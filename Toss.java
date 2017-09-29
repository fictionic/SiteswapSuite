package siteswapsuite;

public class Toss {

	private ExtendedInteger height;
	private int charge;
	private Integer destHand;

	public Toss(int emptyHandIndex) {
		this.height = new ExtendedInteger(0);
		this.destHand = emptyHandIndex;
		this.charge = 0;
	}

	public Toss(int height, int destHand, boolean isAntitoss) {
		this.height = new ExtendedInteger(height);
		this.destHand = destHand;
		if(isAntitoss) {
			this.charge = -1;
		} else {
			this.charge = 1;
		}
	}

	public Toss(InfinityType height, boolean isAntitoss) {
		this.height = new ExtendedInteger(height);
		this.destHand = null;
		if(isAntitoss) {
			this.charge = -1;
		} else {
			this.charge = 1;
		}
	}

	public ExtendedInteger height() {
		return this.height;
	}

	public Integer destHand() {
		return this.destHand;
	}

	public Boolean isAntitoss() {
		if(this.charge == 0) {
			return null;
		} else {
			if(this.charge == 1) {
				return false;
			} else {
				return true;
			}
		}
	}

	// whether this toss has height zero
	public boolean isZero() {
		if(this.height.isInfinite()) {
			return false;
		}
		return this.height.finiteValue() == 0;
	}

	// whether this toss is a true zero toss (not a 0x)
	public boolean isZero(int sourceHand) {
		if(this.height.isInfinite()) {
			return false;
		}
		return this.height.finiteValue() == 0 && this.destHand == sourceHand;
	}

	public int charge() {
		return this.charge;
	}

	public Toss getStarredToss() {
		if(destHand != null) {
			return new Toss(this.height.finiteValue(), (this.destHand + 1) % 2, this.charge < 0);
		} else {
			return this.deepCopy();
		}
	}

	public void starify() {
		if(destHand != null)
			this.destHand = (this.destHand + 1) % 2;
	}

	public Toss deepCopy() {
		if(this.height.isInfinite()) {
			return new Toss(this.height.infiniteValue(), this.charge < 0);
		} else {
			return new Toss(this.height.finiteValue(), this.destHand, this.charge < 0);
		}
	}

	public String toString() {
		String out = "(";
		if(this.height.isInfinite()) {
			if(this.height.infiniteValue() == InfinityType.NEGATIVE_INFINITY) {
				out += "-";
			}
			if(this.charge < 0) {
				out += "_";
			}
			out += "&)";
		} else {
			if(this.charge < 0) {
				out += "_";
			}
			out += height.finiteValue().toString() + ", " + destHand.toString() + ")";
		}
		return out;
	}
}
