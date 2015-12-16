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
}
