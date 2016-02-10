all: math pure-ss state-based notation main package

package:
	mkdir -p bin
	jar -cfe bin/SiteswapSuite.jar siteswapsuite/Main siteswapsuite/*.class

main:
	javac -d . CompatibleNotatedSiteswapPair.java ContextualizedNotatedTransitionList.java Main.java

math:
	javac -d . InfinityType.java ExtendedInteger.java ExtendedFraction.java

pure-ss:
	javac -d . Toss.java Siteswap.java MutableSiteswap.java 

notation:
	javac -d . Notation.java NotatedSiteswap.java

state-based:
	javac -d . Notation.java State.java Transition.java

clean:
	rm -r siteswapsuite
	rm SiteswapSuite.jar
