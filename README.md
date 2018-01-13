# SiteswapSuite

SiteswapSuite computes information about juggling patterns as abstracted into [siteswap](http://en.wikipedia.org/wiki/Siteswap) notation, with an emphasis on maximizing the generality of the underlying theory.

## Setup

```
$ git clone https://github.com/fictionic/SiteswapSuite
$ make
```

## Usage

SiteswapSuite takes input, prints things about its inputs, and manipulates its inputs. Because there are many ways to combine these functions, the commandline syntax is very general.

A valid command is of the form `siteswapsuite CHAIN [CHAIN ...]`,  
where `CHAIN` is `INPUT [INFOS] [LINK ...]`,  
where `LINK` is `OPERATION [INFOS]`,  
where `INFOS` is `INFO [INFO ...]`;  
the atoms are `INPUT`, `INFO`, and `OPERATION`.

Each of these will be explained in turn.

### Option Syntax

Some arguments accept options to control their behavior.
Currently the only way to pass options to an argument is as follows: `--argument:option1,option2,...`, where each `optionN` is a (long or short) option name with the leading `--` or `-` removed.
If an option itself requires an argument, specify it with `option=arg`.

For example, to compute at most two transitions, selecting the second, you could pass `-T:m=2,o=1`.

### Input

Inputs can either be literal—that is, parsed from a cmdline argument—or generated from some previously computed siteswap-object(s).

#### Literal Input

`-i[:OPTIONS] NOTATION`, `--input[:OPTIONS] NOTATION`  
Takes one argument, a string of notation. Can be either a siteswap or a state. If the notation can only be interpreted as a state, a state is parsed; otherwise a siteswap is parsed. To explicitly specify state parsing, prefix the notation with `st:`.

##### Literal Input Options

`--num-hands N`, `-h N`  
Force parsing the input notation as having N hands.

`--start-hand N`, `-H N`  
Force parsing the input notation as starting with hand N.

`--keep-zeroes`, `-z`  
Don't strip out tosses of height 0 from parsed pattern (siteswap only).

#### Transition Input

`-T[:OPTIONS]`, `--transition[:OPTIONS]`  
Computes transitions between the output of two chains, and returns one as a result. By default, transitions are computed from the second-to-last chain to the last chain (not including the one begun by this input), and the first transition is chosen as a result.

##### Transition Input Options

`--from N`  
Compute transitions *from* the output of chain N.

`--to N`  
Compute transitions *to* the output of chain N.

`--min-length N`, `-l N`  
Compute transitions no shorter than N beats.

`--max-transitions N`, `-m N`  
Compute no more than N transitions.

##### Transition Options

Multiple operations involve computing transitions, and thus accept these options.

`--select-transition N`, `-o N`  
Select the Nth transition as the resulting object, zero-indexed.

`--allow-extra-squeeze-catches`, `-q`  
Allow extra squeeze catches in transitions. By default, if additional balls need to be caught from infinity in order to transition to the destination state, they will only be caught by empty hands. With this flag set, all additional balls will be caught on the first beat.

`--generate-ball-antiball-pairs`, `-g`  
Allow generation of ball/antiball pairs in transitions. By default, tosses will only be made from hands that have balls to throw, and antitosses will only be made from hands that have antiballs to throw. With this flag set, the transition may generate pairs of one ball and one antiball to be thrown together, reducing the length of the transition. With both `-g` and `-q` set, the transitions will all be one beat in length or less.

`--un-antitossify-transitions`, `-A`  
By default, transitions may contain antitosses.
With this flag set, antitosses will be converted to regular tosses of negative height.
This is separate from the regular operation `--un-antitossify` because the prefix and suffix siteswaps of a transition must be taken into account when un-antitossifying them, as negative tosses can originate from or end up there in the result.

`--display-generalized-transition`, `-G`  
Display the general form of the transition along with actual examples. The general transition is displayed as `<tosses>{<catches>}`, with tosses indicated by `&` (infinite-tosses) and catches indicated by `-&` (negative-infinite tosses).

### Info

When an input siteswap is given, the following information is always printed:
- a notation-independent string representation of the siteswap or state
- the parsed data structure translated back into proper notation
- the dimensions of the siteswap object: hands x period for siteswaps; hands x length for states

Beyond this, options must be given to indicate what information about the input is computed and displayed:

`--capacity`, `-c`  
Number of balls in ('capacity of') the siteswap or state.
If there is no definite number of balls (i.e. when a siteswap can be un-infinitized), gives the minimum (so really 'capacity' is a poor choice of terminology).

`--state`, `-s`  
State associated with given siteswap.

`--validity`, `-v`  
Validity of siteswap / finitude of state.

`--true-period`, `-L`  
How many beats it takes for each ball to return to its original site. (Siteswaps only.)

`--cycles`  
Each unique path followed by any ball, independent of time-shift. (Siteswaps only.)

`--orbits`  
The path followed by each ball. The period of each orbit is the true period of the original pattern. (Siteswaps only.)

`--primality`, `-P`  
Are any states visited more than once while juggling the siteswap? (Siteswaps only.)

`--difficulty`, `-d`  
Difficulty of siteswap/state, as given by Dancey's formula, d = b/(h+h/b).

### Operation

`--to-siteswap[:TRANSITION\_OPTIONS]`, `-S[:TRANSITION\_OPTIONS]`  
Compute a siteswap having the given state.
This is done by computing a transition between the state and itself.
Transition options can be passed. Note that `--min-length` defaults to 1, because you probably want a non-null transition.  

The following operations only apply to siteswaps.

`--to-state`, `-t`  
Return the state associated with the given siteswap.

`--invert`, `-V`  
Time-reverse of pattern.

`--spring`, `-p`  
Double all throw heights, add a ball zigzagging underneath.

`--infinitize`, `-f`  
Replace each toss with a toss of infinite height, and add a catch (negative-infinite-toss) to the site at which the toss is caught.

`--un-infinitize`, `-F`  
Find one way to pair up positive-infinite tosses with negative-infinite tosses of the same charge.

`--antitossify`, `-a`  
Remove all negative tosses, and the equivalent positive antitosses are added in the appropriate place (this does not change the pattern in any way other than appearance.)

`--un-antitossify`, `-A`  
Inverse of above operation.

## Examples

- Find a transition between the siteswaps `5` and `91`:

```
$ siteswapsuite -i 5 -i 91 -T
INPUT 0:
 type: literal
 notation: '5'
---> siteswap:
 parsed: [[[(5, 0)]]]
 notated: 5
 dimension: 1h x 1b
OUTPUT: 
 siteswap: 5
INPUT 1:
 type: literal
 notation: '91'
---> siteswap:
 parsed: [[[(9, 0)]], [[(1, 0)]]]
 notated: 91
 dimension: 1h x 2b
OUTPUT: 
 siteswap: 91
INPUT 2:
 type: transition
 from: output 0
 to: output 1
 results:
  [[[(6, 0)]], [[(7, 0)]], [[(8, 0)]]] --->
  [[[(8, 0)]], [[(5, 0)]], [[(8, 0)]]]
  [[[(8, 0)]], [[(9, 0)]], [[(4, 0)]]]
  [[[(6, 0)]], [[(9, 0)]], [[(6, 0)]]]
  [[[(10, 0)]], [[(5, 0)]], [[(6, 0)]]]
  [[[(10, 0)]], [[(7, 0)]], [[(4, 0)]]]
---> siteswap:
 parsed: [[[(6, 0)]], [[(7, 0)]], [[(8, 0)]]]
 dimension: 1h x 3b
OUTPUT: 
 siteswap: 678
```

- Display information about the 3-ball box:

```
$ siteswapsuite -i '(4,2x)*'` -v -s
INPUT 0:
 type: literal
 notation: '(4,2x)*'
---> siteswap:
 parsed: [[[(4, 0)], [(2, 0)]], [[], []], [[(2, 1)], [(4, 1)]], [[], []]]
 notated: (4,2x)(2x,4)
 dimension: 2h x 4b
 validity: true
 state: (11)(00)(01)
OUTPUT: 
 siteswap: (4,2x)(2x,4)
```
