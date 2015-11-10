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

**EXAMPLES**
- Find a transition between the siteswaps `5` and `91`:
`$ java TransitionFinder 5 91`
`678`
- Display information about the 3-ball box:
`$ java TransitionFinder '(4,2x)*'`
`parsed: [[[[4, 0]], [[2, 0]]], [[[0, 0]], [[0, 1]]], [[[2, 1]], [[4, 1]]], [[[0, 0]], [[0, 1]]]]`
`de-parsed: (4,2x)(2x,4)`
`number of balls: 3.0`
`valid: true`
`period: 4`
`state: [ 0  0  1 ,  1  0  1 ]`
