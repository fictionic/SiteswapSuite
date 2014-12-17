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
- make the parser ignore zero-valued tosses within multiplex notation, unless it is the only toss in that hand at that beat (i.e. [40] should parse to the same thing as 4, but [0] should parse to the same thing as 0)
- investigate the issue with the state of '[503]' being different than that of '[53]'
- have getTransition convert one-handed siteswaps to two-handed ones if the other pattern is two-handed (and check if the result is valid) (so you don't have to do (3,0)!(0,3)! instead of just 3 when transitioning to sync patterns)
