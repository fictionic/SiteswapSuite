all: util exception math pure-ss notation transition main package

package:
	mkdir -p bin
	jar -cfe bin/SiteswapSuite.jar siteswapsuite/Main siteswapsuite/*.class

main:
	javac -d . Main.java

math:
	javac -d . InfinityType.java ExtendedInteger.java ExtendedFraction.java

pure-ss:
	javac -d . Toss.java Siteswap.java State.java Transition.java

transition:
	javac -d . Transition.java ContextualizedNotatedTransitionList.java 

notation:
	javac -d . Notation.java NotatedSiteswap.java CompatibleNotatedSiteswapPair.java

exception:
	javac -d . SiteswapException.java

util:
	javac -d . Util.java

clean:
	rm -r siteswapsuite
	rm SiteswapSuite.jar
