package siteswapsuite;

public class ExtendedFraction {

    private ExtendedInteger numerator;
    private int denominator;
    private int sign;

    public ExtendedFraction(ExtendedInteger top, int bottom) {
	if(bottom != 0) {
	    //extract sign
	    if(top.sign() == 0)
		this.sign = 0;
	    else {
		if(top.sign() < 0 && bottom < 0 || top.sign() > 0 && bottom > 0)
		    this.sign = 1;
		else
		    this.sign = -1;
	    }
	    //take abs of top and bottom
	    bottom = Math.abs(bottom);
	    if(top.sign() < 0)
		top.negate();
	    //reduce common factors
	    if(!top.isInfinite()) {
		int newTop = top.finiteValue();
		int max = Math.max(top.finiteValue(), bottom);
		int c;
		boolean found = false;
		for(int factor=2; factor<=max; factor++) {
		    //look for thing to multiply to get bottom
		    c = 1;
		    while(c * factor <= bottom) {
			if(c * factor == bottom) {
			    found = true;
			    break;
			}
			c++;
		    }
		    if(found) {
			found = false;
			while(c * factor <= newTop) {
			    if(c * factor == newTop) {
				found = true;
				break;
			    }
			    c++;
			}
			if(found) {
			    newTop /= factor;
			    bottom /= factor;
			}
		    }
		}
		this.numerator = new ExtendedInteger(newTop);
		this.denominator = bottom;
	    } else {
		this.numerator = top;
		this.denominator = bottom;
	    }
	}
    }

    public Float floatValue() {
	if(this.denominator == 0)
	    return Float.NaN;
	if(this.numerator.isInfinite()) {
	    if(this.sign < 0)
		return Float.NEGATIVE_INFINITY;
	    else
		return Float.POSITIVE_INFINITY;
	} else {
	    return ((Integer)this.numerator.finiteValue()).floatValue()/((Integer)this.denominator).floatValue();
	}
    }

    public String toString() {
	if(this.denominator == 0)
	    return "NaN";
	if(this.numerator.isInfinite()) {
	    if(this.sign < 0)
		return (new ExtendedInteger(InfinityType.NEGATIVE_INFINITY)).toString();
	    else
		return (new ExtendedInteger(InfinityType.POSITIVE_INFINITY)).toString();
	} else {
	    if(this.sign == 0)
		return "0";
	    else
		return this.numerator.toString() + "/" + ((Integer)this.denominator).toString();
	}
    }
}
