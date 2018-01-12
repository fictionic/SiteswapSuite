# SiteswapSuite

SiteswapSuite computes information about juggling patterns as abstracted into [siteswap](http://en.wikipedia.org/wiki/Siteswap) notation, with an emphasis on maximizing the generality of the underlying theory.

## Usage

SiteswapSuite takes input, prints things about its inputs, and manipulates its inputs. Because there are many ways to combine these functions, the commandline syntax is very general.

A valid command is of the form `siteswapsuite CHAIN [CHAIN ...]`,  
where `CHAIN` is `INPUT [INFOS] [LINK ...]`,  
where `LINK` is `OPERATION [INFOS]`,  
where `INFOS` is `INFO [INFO ...]`; 
the atoms are `INPUT`, `INFO`, and `OPERATION`.

Each of these will be explained in turn.

### Input

Inputs can either be literal—that is, parsed from a cmdline argument—or generated from some previously computed siteswap-object(s).

#### Literal Input

`-i[:OPTIONS] NOTATION`  
`--input[:OPTIONS] NOTATION`

Takes one argument, a string of notation. Can be either a siteswap or a state. If the notation can only be interpreted as a state, a state is parsed; otherwise a siteswap is parsed. To explicitly specify state parsing, prefix the notation with `st:`.

*LITERAL INPUT OPTIONS:*
| long name     | short name | argument | effect                                                                  |
|:--------------|:-----------|:---------|:------------------------------------------------------------------------|
| `num-hands`   | `h`        | int N    | Force parsing the input notation as having N hands.                     |
| `start-hand`  | `H`        | int N    | Force parsing the input notation as starting with hand N.               |
| `keep-zeroes` | `z`        | none     | Don't strip out tosses of height 0 from parsed pattern (siteswap only). |

#### Transition Input

`-T`/`--transition`

*TRANSITION INPUT OPTIONS:* 
| long name          | short name | argument | effect |
|:-------------------|:-----------|:---------|:-------|
|`from`              | (none)     | int N    | Compute transitions *from* the output of chain #N. |
|`to`                | (none)     | int N    | Compute transitions *to* the output of chain #N.   |
|`min-length`        |`l`         | int N    | Compute transitions no shorter than N.             |
|`max-transitions`   |`m`         | int N    | Compute no more than N transitions.                |
|`select-transition` |`o`         | int N    | Select the Nth transition as the resulting object. |

*TRANSITION OPTIONS:*
|long name                         |short name  |argument  |effect  |
|:---------------------------------|:-----------|:---------|:-------|
|`allow-extra-squeeze-catches`     |`q`         |(none)    | |
|`generate-ball-antiball-pairs`    |`g`         |(none)    | |
|`un-antitossify-transitions`      |`A`         |(none)    | |
|`display-generalized-transition`  |`G`         |(none)    | |

### Info

When an input siteswap is given, the following information is always printed:
- a notation-independent string representation of the siteswap as a list of (beats) lists of (hands) lists of tosses
- the parsed siteswap data structure translated back into proper notation
- the number of hands in the pattern
- the period of the pattern

Beyond this, options must be given to indicate what information about the input is computed and displayed:

|long name|short name|effect|
|:--:|:-:|:---|
| `--capacity` |`-c`| Number of balls in ('capacity of') pattern. If there is no definite number of balls, gives the minimum (so really 'capacity' is a poor choice of terminology).|
| `--state` |`-s`| Juggling state of pattern.|
| `--validity` |`-v`| Validity of pattern.|
| `--primality` |`-P`|  Primality of pattern. That is, whether or not a state is visited more than once during one period of the pattern.|
| `--difficulty` |`-d`| 'Difficulty' of pattern, as given by Dancey's formula b/(h+h/b). Thus it does not take into account the details of the siteswap at all.|
### Operation

### OPTIONS

**Modification sequence specification**

After parsing the input into a siteswap pattern, a sequence of modifications may be performed before information about the pattern is computed, and before the pattern is used to compute a transition.

|long name|short name|effect|
|:---:|:-:|:---|
| `--invert` |`-V` | Inverse of pattern. (Technically the time-reverse.)|
| `--spring` |`-p` | Sprung version of pattern.|
| `--infinitize` |`-f` | "Infinitized" pattern. Each toss is replaced with a toss of infinite height, and a catch (negative-infinite-toss) is added at the site at which it is caught.|
| `--unInfinitize` |`-F` | "Un-infinitized" pattern. Finds a possible way to pair up positive-infinite tosses with negative-infinite tosses of the same charge.|
| `--antitossify` |`-a` | "antitossified" pattern. All negative tosses are removed, and the equivalent positive antitosses are added in the appropriate place (this does not change the pattern in any way other than appearance.)|
| `--unAntitossify` |`-A` | "un-antitossified" pattern. Inverse of above operation.|
| `--antiNegate` |`-N` | "anti-negated" pattern. |

(Note: none of these are implemented yet.)

#### Transition Options

|long name|short name|effect|
|:---:|:-:|:---|
| `--minTransitionLength` |`-l N` | Require transitions to be at least N beats long. If no transition is needed to get from one input to the other, this option can be used to force a nonempty transition to be computed.|
| `--maxTransitions` |`-m N` | Compute at most N transitions. By default, all transitions are computed (obviously not all, since there are infinite. I still need to figure out the details here.)|
| `--allowSqueezeCatches` |`-q`   | Allow extra squeeze catches in transitions. By default, if additional balls need to be caught from infinity in order to transition to the destination state, they will only be caught by empty hands. With this flag set, all additional balls will be caught on the first beat.|
| `--allowBallAntiballPairs` |`-g`   | Allow generation of ball/antiball pairs in transitions. By default, tosses will only be made from hands that have balls to throw, and antitosses will only be made from hands that have antiballs to throw. With this flag set, the transition may generate pairs of one ball and one antiball to be thrown together, reducing the length of the transition. With both `-g` and `-q` set, the transitions will all be one beat in length or less.|
| `--unAntitossifyTransitions` |`-A`   | Un-antitossify transitions. By default, transitions may contain antitosses. With this flag set, antitosses will be converted to regular tosses of negative height.|
| `--displayGeneralTransition` |`-G`   | Display the general form of the transition along with actual examples. The general transition is displayed as '\<tosses\>\{\<catches\>\}', with tosses indicated by '&' (infinite-tosses) and catches indicated by '-&' (negative-infinite tosses).|

(Note: only `-G` and `-m` are implemented.)


### SETUP

`$ git clone https://github.com/fictionic/SiteswapSuite`
`$ make`

### BASIC EXAMPLES
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

### ON GENERALITY OF THE THEORY (IN PROGRESS!)

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
