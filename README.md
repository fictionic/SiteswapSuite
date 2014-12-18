TransitionFinder
================

**DESCRIPTION**  
TransitionFinder finds a direct transition between two given valid juggling patterns in [siteswap](http://en.wikipedia.org/wiki/Siteswap) notation.

As such, it can also do the following tasks:
- determine whether or not a string is in valid siteswap notation
- parse a valid siteswap string into a general Siteswap object (and 'deParse' a Siteswap object into a siteswap string (of appropriate brevity))
- determine whether or not a valid siteswap string represents a valid juggling pattern
- compute the juggling state of a valid juggling pattern
- compute a direct transition pattern between two juggling states


**SYNOPSIS**  
`java TransitionFinder '<siteswap>'` - displays information about the given siteswap (validity, number of balls, juggling state, unabridged notation)

`java TransitionFinder '<siteswap1>' '<siteswap2>'` - prints a transition pattern between the two given patterns, assuming they are valid

**TO DO**  
- give user option to choose which hand to start async patterns with when transitioning to/from sync patterns
- implement getTransition for siteswaps with different numbers of balls, using infinity- and negative-infinity-valued tosses (also choose symbols for such throw heights... options: @, #, $, %, &, ~, <, >, ?, +, =, _ ... I think & for +infinity, % for -infinity)
	- implement infinity- and negative-infinity-valued toss heights in Siteswap.java... don't want to just switch everything to doubles, since that would be a pretty big waste most of the time
