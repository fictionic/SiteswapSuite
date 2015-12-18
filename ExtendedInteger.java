public class ExtendedInteger {

    private Integer finiteValue;
    private InfinityType infiniteValue;
    private boolean isInfinite;

    public ExtendedInteger(InfinityType value) {
	this.infiniteValue = value;
	this.finiteValue = null;
	this.isInfinite = true;
    }

    public ExtendedInteger(int value) {
	this.finiteValue = value;
	this.infiniteValue = null;		
	this.isInfinite = false;
    }

    public boolean isInfinite() {
	return this.isInfinite;
    }

    public Integer finiteValue() {
	return this.finiteValue;
    }

    public InfinityType infiniteValue() {
	return this.infiniteValue;
    }

    public int sign() {
	if(this.isInfinite) {
	    if(this.infiniteValue == InfinityType.POSITIVE_INFINITY)
		return 1;
	    else
		return -1;
	} else {
	    if(this.finiteValue > 0)
		return 1;
	    else if(this.finiteValue < 0)
		return -1;
	    else
		return 0;
	}
    }

    public void negate() {
	if(this.isInfinite) {
	    if(this.infiniteValue == InfinityType.POSITIVE_INFINITY)
		this.infiniteValue = InfinityType.NEGATIVE_INFINITY;
	    else
		this.infiniteValue = InfinityType.POSITIVE_INFINITY;
	} else {
	    this.finiteValue *= -1;
	}
    }

    public String toString() {
	if(this.isInfinite) {
	    if(this.infiniteValue == InfinityType.POSITIVE_INFINITY)
		return "&";
	    else
		return "-&";
	} else {
	    return this.finiteValue.toString();
	}
    }
}
