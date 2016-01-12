#!/bin/bash
for file in InfinityType.java ExtendedInteger.java ExtendedFraction.java Toss.java Siteswap.java MutableSiteswap.java Notation.java NotatedSiteswap.java State.java Transition.java NotatedSiteswapTransition.java; do
	if javac -d . $file; then
		continue;
	else
		break;
	fi
done
