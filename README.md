SiteswapSuite
================

**DESCRIPTION**  
SiteswapSuite computes information about juggling patterns as abstracted into [siteswap](http://en.wikipedia.org/wiki/Siteswap) notation, with an emphasis on maximizing the generality of the underlying theory.

**SYNOPSIS**  
 Display information about the given siteswap (validity, number of balls, juggling state, unabridged notation):

`java -jar SiteswapSuite.jar <siteswap>`

Print all transitions between the two given patterns (within default constraints (for now)), assuming they are valid:

`java -jar SiteswapSuite.jar <siteswap1> <siteswap2>` 


**SETUP**

`$ git clone https://github.com/seeegma/SiteswapSuite`  
`$ make`

**BASIC EXAMPLES**
- Find a transition between the siteswaps `5` and `91`:

`$ java -jar SiteswapSuite.jar 5 91`  
```
parsed: [[[[5, 0]]]]
de-parsed: 5
number of balls: 5
number of hands: 1
valid: true
period: 1
state: [[ 1][ 1][ 1][ 1][ 1]]
-----
parsed: [[[[9, 0]]], [[[1, 0]]]]
de-parsed: 91
number of balls: 5
number of hands: 1
valid: true
period: 2
state: [[ 1][ 1][ 0][ 1][ 0][ 1][ 0][ 1]]
-----
General Transition:
&&&{000-&0-&0-&0}
All Transitions:
678
858
894
696
a56
a74
```

- Display information about the 3-ball box:
`$ java -jar SiteswapSuite.jar '(4,2x)*'`
```
parsed: [[[[4, 0]], [[2, 0]]], [[[0]], [[0]]], [[[2, 1]], [[4, 1]]], [[[0]], [[0]]]]
de-parsed: (4,2x)(2x,4)
number of balls: 3
number of hands: 2
valid: true
period: 4
state: [[ 1,  1][ 0,  0][ 0,  1][ 0,  0]]
```

**ON GENERALITY OF THE THEORY (IN PROGRESS!)**

When one first learns siteswap, typically one learns the "vanilla" flavor, in which all of the following axioms hold:
- every beat contains exactly one or zero tosses (i.e. no multiplex)
- the hands alternate tossing between beats (i.e. no synchronous)
- all tosses have non-negative height (i.e. no negative siteswaps)
- all tosses have a "charge" of 1 or 0 (i.e. no "antitosses")
- all tosses have finite height (self-explanatory)
- only valid siteswaps have an associated juggling state (i.e. no infinite states)
Some are common extensions to siteswap, with their own standard notation (multiplex, sync), while others are not.  
SiteswapSuite casts out each of these. As such, some explanation is needed.

\<explanation!\>

With these generalizations, the world of possibilities gets much bigger:

**COMPLEX EXAPLES**
- 
