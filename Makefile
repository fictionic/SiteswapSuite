all: math pure-ss state-based notation
	javac -d . CompatibleNotatedSiteswapPair.java ContextualizedNotatedTransitionList.java Main.java

math:
	javac -d . InfinityType.java ExtendedInteger.java ExtendedFraction.java

pure-ss: math
	javac -d . Toss.java Siteswap.java MutableSiteswap.java 

notation: pure-ss
	javac -d . Notation.java NotatedSiteswap.java

state-based: pure-ss
	javac -d . Notation.java State.java Transition.java

clean:
	rm -r siteswapsuite
