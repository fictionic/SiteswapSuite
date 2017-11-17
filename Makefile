all: util exception math pure-ss notation main package

package:
	mkdir -p bin
	jar -cfe bin/SiteswapSuite.jar siteswapsuite/Main siteswapsuite/*.class

exception:
	javac -d . SiteswapException.java InvalidNotationException.java

math:
	javac -d . InfinityType.java ExtendedInteger.java ExtendedFraction.java

pure-ss:
	javac -d . Toss.java Siteswap.java State.java Transition.java 

notation:
	javac -d . SiteswapNotation.java NotatedSiteswap.java StateNotation.java NotatedState.java \
		CompatibleNotatedSiteswapPair.java CompatibleNotatedObjectPair.java ContextualizedNotatedTransitionList.java

main:
	javac -d . Argument.java ParsedArguments.java Main.java 

util:
	javac -d . Util.java

clean:
	rm -r siteswapsuite
	rm SiteswapSuite.jar

test:
	# javac -d . Test.java
	javac -d . ArgumentParser.java
