SiteswapSuite
================

###DESCRIPTION
SiteswapSuite computes information about juggling patterns as abstracted into [siteswap](http://en.wikipedia.org/wiki/Siteswap) notation, with an emphasis on maximizing the generality of the underlying theory.

###SYNOPSIS
Display requested information about a siteswap or state:

`sss [INPUT]`

Compute a transition between two inputs:

`sss [TRANSITION_OPTIONS] [INPUT] [INPUT]`

An `[INPUT]` is of the form

`-i <siteswap_notation> [INPUT_OPTIONS]`.

###OPTIONS

Here, `<x>` is taken to be an integer argument. It is always mandatory.

####Input Options

**Info specification**

When an input siteswap is given, the following information is always printed:
- a notation-independent string representation of the siteswap as a list of (beats) lists of (hands) lists of tosses
- the parsed siteswap datastructure translated back into proper notation
- the number of hands in the pattern
- the period of the pattern

Beyond this, options must be given to indicate what information about the input is computed and displayed:

|option|effect|
|:----:|:-----|
|`-b`  | Number of balls in pattern. If there is no definite number of balls, gives the minimum.|
|`-s`  | Juggling state of pattern.|
|`-v`  | Validity of pattern.|
|`-P`  | Primality of pattern. That is, whether or not a state is visited more than once during one period of the pattern.|
|`-d`  | 'Difficulty' of pattern, as given by Dancey's formula b/(h+h/b). Thus it does not take into account the details of the siteswap at all.|

**Hand specification**

These only apply when the input notation is in vanilla siteswap notation.

|option |effect|
|:-----:|:-----|
|`-h<x>`| Force parsing the input notation as having \<x\> hands. This is only useful when giving single inputs to the program, because an appropriate number of hands is inferred for each input pattern given the other pattern. For example, if given the single input `-i 3`, the program will take '3' to represent a one-handed pattern; but `-i 3 -h2` will produce the pattern '(3,0)!(0,3)!'.|
|`-H<x>` | Force parsing the input notation as starting with hand \<x\>. Default is 0. For example: `-i 3 -h2 -H0` produces '(3,0)!(0,3)!', whereas `-i 3 -h2 -H1` produces '(0,3)!(3,0)!'.|

**Modification sequence specification**

After parsing the input into a siteswap pattern, a sequence of modifications may be performed before information about the pattern is computed, and before the pattern is used to compute a transition.

|option|effect|
|:----:|:-----|
|`-V`  |Inverse of pattern. (Technically the time-reverse.)|
|`-p`  |Sprung version of pattern.|
|`-f`  |"Infinitized" pattern. Each toss is replaced with a toss of infinite height, and a catch (negative-infinite-toss) is added at the site at which it is caught.|
|`-F`  |"Un-infinitized" pattern. Finds a possible way to pair up positive-infinite tosses with negative-infinite tosses of the same charge.|
|`-a`  |"antitossified" pattern. All negative tosses are removed, and the equivalent positive antitosses are added in the appropriate place (this does not change the pattern in any way other than appearance.)|
|`-A`  |"un-antitossified" pattern. Inverse of above operation.|
|`-N`  |"anti-negated" pattern. |

 
####Transition Options

|option |effect|
|:-----:|:-----|
|`-l<x>` |Require transitions to be at least \<x\> beats long. If no transition is needed to get from one input to the other, this option can be used to force a nonempty transition to be computed.|
|`-m<x>`|Compute at most \<x\> transitions. By default, all transitions are computed (obviously not all, since there are infinite. I still need to figure out the details here.)|
|`-q`   |Allow extra squeeze catches in transitions. By default, if additional balls need to be caught from infinity in order to transition to the destination state, they will only be caught by empty hands. With this flag set, all additional balls will be caught on the first beat.|
|`-g`   |Allow generation of ball/antiball pairs in transitions. By default, tosses will only be made from hands that have balls to throw, and antitosses will only be made from hands that have antiballs to throw. With this flag set, the transition may generate pairs of one ball and one antiball to be thrown together, reducing the length of the transition. With both `-g` and `-q` set, the transitions will all be one beat in length or less.|
|`-A`   |Un-antitossify transitions. By default, transitions may contain antitosses. With this flag set, antitosses will be converted to regular tosses of negative height.|
|`-G`   |Display the general form of the transition along with actual examples. The general transition is displayed as '\<tosses\>\{\<catches\>\}', with tosses indicated by '&' (infinite-tosses) and catches indicated by '-&' (negative-infinite tosses).|


###SETUP

`$ git clone https://github.com/seeegma/SiteswapSuite`  
`$ make`

###BASIC EXAMPLES
- Find a transition between the siteswaps `5` and `91`:

`$ sss -i 5 -i 91`  
This displays:
```
INPUT 0:   '5'
---------
parsed:     [[[(5, 0)]]]
de-parsed:  5
numHands:   1
period:     1
==========
INPUT 1:   '91'
---------
parsed:     [[[(9, 0)]], [[(1, 0)]]]
de-parsed:  91
numHands:   1
period:     2
==========
Transitions:
678
858
894
696
a56
a74
```

- Display information about the 3-ball box (note that short options can be combined into a single argument):  
`$ sss -ivs '(4,2x)*'`

This displays:  
```
INPUT 0:   '(4,2x)*'
---------
parsed:     [[[(4, 0)], [(2, 0)]], [[(0)], [(0)]], [[(2, 1)], [(4, 1)]], [[(0)], [(0)]]]
de-parsed:  (4,2x)(2x,4)
numHands:   2
period:     4
validity:   true
state:      [[ 1,  1][ 0,  0][ 0,  1][ 0,  0]]
==========
```

###ON GENERALITY OF THE THEORY (IN PROGRESS!)

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
