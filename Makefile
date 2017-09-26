all: util exception math pure-ss notation transition main package

package:
	mkdir -p bin
	jar -cfe bin/SiteswapSuite.jar siteswapsuite/Main siteswapsuite/*.class

exception:
	javac -d . SiteswapException.java InvalidNotationException.java

math:
	javac -d . InfinityType.java ExtendedInteger.java ExtendedFraction.java

pure-ss:
	javac -d . Toss.java Siteswap.java State.java

transition:
	javac -d . Transition.java ContextualizedNotatedTransitionList.java 

notation:
	javac -d . SiteswapNotation.java NotatedSiteswap.java CompatibleNotatedSiteswapPair.java StateNotation.java NotatedState.java

main:
	javac -d . Main.java

util:
	javac -d . Util.java

clean:
	rm -r siteswapsuite
	rm SiteswapSuite.jar
