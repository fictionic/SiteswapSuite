public class ExtendedNaturalNumber {

		private Integer finiteValue;
		private InfinityType infiniteValue;
		private boolean isInfinite;

		public ExtendedNaturalNumber(InfinityType value) {
				this.infiniteValue = value;
				this.finiteValue = null;
				this.isInfinite = true;
		}

		public ExtendedNaturalNumber(int value) {
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
}
