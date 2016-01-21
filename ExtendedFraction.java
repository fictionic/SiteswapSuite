package siteswapsuite;

public class ExtendedFraction {

	private ExtendedInteger numerator;
	private int denominator;
	private int sign;

	private static int gcm(int a, int b) {
		return b == 0 ? a : gcm(b, a % b);
	}

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
				int gcm = gcm(top.finiteValue(), bottom);
				this.numerator = new ExtendedInteger(top.finiteValue()/gcm);
				this.denominator = bottom/gcm;
			} else {
				this.denominator = bottom;
				this.numerator = top;
			}
		} else {
			this.denominator = 0;
			this.numerator = top;
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
				if(this.denominator == 1)
					return this.numerator.toString();
				else
					return this.numerator.toString() + "/" + ((Integer)this.denominator).toString();
		}
	}
}
