all: math pure-ss state-based notation main package

package:
	mkdir -p bin
	jar -cfe bin/SiteswapSuite.jar siteswapsuite/Main siteswapsuite/*.class

main:
	javac -d . CompatibleNotatedSiteswapPair.java ContextualizedNotatedTransitionList.java Main.java

math:
	javac -d . InfinityType.java ExtendedInteger.java ExtendedFraction.java

pure-ss:
	javac -d . Toss.java Siteswap.java Notation.java State.java Transition.java

notation:
	javac -d . Notation.java NotatedSiteswap.java

clean:
	rm -r siteswapsuite
	rm SiteswapSuite.jar
