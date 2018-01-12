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

### Input

Inputs can either be literal—that is, parsed from a cmdline argument—or generated from some previously computed siteswap-object(s).

#### Literal Input

`-i[:OPTIONS] NOTATION`, `--input[:OPTIONS] NOTATION`  
Takes one argument, a string of notation. Can be either a siteswap or a state. If the notation can only be interpreted as a state, a state is parsed; otherwise a siteswap is parsed. To explicitly specify state parsing, prefix the notation with `st:`.

##### Options:

`--num-hands N`, `-h N`  
Force parsing the input notation as having `N` hands.

`--start-hand N`, `-H N`  
Force parsing the input notation as starting with hand `N`.

`--keep-zeroes`, `-z`  
Don't strip out tosses of height 0 from parsed pattern (siteswap only).

#### Transition Input

`-T[:OPTIONS]`, `--transition[:OPTIONS]`

Computes transitions between the output of two chains, and returns one as a result. By default, transitions are computed from the second-to-last chain to the last chain (not including the one begun by this input), and the first transition is chosen as a result.

##### Options:

`--from N`  
Compute transitions *from* the output of chain #`N`.

`--to N`  
Compute transitions *to* the output of chain #`N`.

`--min-length N`, `-l N`  
Compute transitions no shorter than `N`.

`--max-transitions N`, `-m N`  
Compute no more than `N` transitions.

`--select-transition N`, `-o N`  
Select the `N`th transition as the resulting object.

##### Transition Options:

`--allow-extra-squeeze-catches`, `-q`  
Allow extra squeeze catches in transitions. By default, if additional balls need to be caught from infinity in order to transition to the destination state, they will only be caught by empty hands. With this flag set, all additional balls will be caught on the first beat.

`--generate-ball-antiball-pairs`, `-g`  
Allow generation of ball/antiball pairs in transitions. By default, tosses will only be made from hands that have balls to throw, and antitosses will only be made from hands that have antiballs to throw. With this flag set, the transition may generate pairs of one ball and one antiball to be thrown together, reducing the length of the transition. With both `-g` and `-q` set, the transitions will all be one beat in length or less.

`--un-antitossify-transitions`, `-A`  
By default, transitions may contain antitosses. With this flag set, antitosses will be converted to regular tosses of negative height.

`--display-generalized-transition`, `-G`  
Display the general form of the transition along with actual examples. The general transition is displayed as `<tosses>{<catches>}`, with tosses indicated by `&` (infinite-tosses) and catches indicated by `-&` (negative-infinite tosses).

### Info

When an input siteswap is given, the following information is always printed:
- a notation-independent string representation of the siteswap or state
- the parsed data structure translated back into proper notation
- the dimensions of the siteswap object: hands x period for siteswaps; hands x finite length for states

Beyond this, options must be given to indicate what information about the input is computed and displayed:

`--capacity`, `-c`  
`Number of balls in ('capacity of') pattern. If there is no definite number of balls, gives the minimum (so really 'capacity' is a poor choice of terminology).

`--state`, `-s`  
State associated with given siteswap.

`--validity`, `-v`  
Validity of siteswap / finitude of state.

`--primality`, `-P`  
Are any states visited more than once while juggling the siteswap?

`--difficulty`, `-d`  
Difficulty of pattern, as given by Dancey's formula b/(h+h/b).

### Operation

`--invert`, `-V`  
Time-reverse of pattern.

`--spring`, `-p`  
Double all throw heights, add a ball zigzagging underneath original tosses.

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
