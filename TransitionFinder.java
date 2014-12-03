import java.util.List;
import java.util.LinkedList;

public class TransitionFinder {

	public static Siteswap getTransition(Siteswap ss1, Siteswap ss2) {
		return null;
	}

	private static List<Siteswap> getOrbits(Siteswap ss) {
		return null;
	}

	private static State getState(Siteswap ss) {
		return new State(ss);
	}

	private static class State {
		private List<HandState> hands;
		private int maxHandStateLength;

		private State(Siteswap ss) {
			//initialize instance variable
			hands = new LinkedList<HandState>();
			maxHandStateLength = 0;
			//create an empty HandState for each hand in the pattern
			for(int h=0; h<ss.numHands(); h++) {
				hands.add(new HandState());
			}
			//emulate juggling the pattern, adding balls to hands at beats when needed
			int ballsAccountedFor = 0;
			int b = 0;
			while(b < ss.period()) {
				System.out.println("ballsAccountedFor: " + ballsAccountedFor);
				System.out.println("\tcurBeat: " + b);
				//loop through hands
				for(int h=0; h<ss.numHands(); h++) {
					System.out.println("\t\tcurHand: " + h);
					//see if there are more (nonzero) throws in this beat in this hand than we currently have balls for; add balls if so
					while(hands.get(h).curValue() < ss.getBeat(b).getHand(h).numNonZeroTosses()) {
						hands.get(h).incrementCurValue();
						ballsAccountedFor++;
						System.out.println("\t\t\taccounted for ball");
						System.out.println("\t\t\tstate: " + hands);
					}
					//now throw all the balls in the hand!
					for(int t=0; t<ss.getBeat(b).getHand(h).numNonZeroTosses(); t++) {
						//eventually change Siteswap so we don't need to make instances of those inner classes outside Siteswap.java
						Siteswap.Beat.Hand.Toss curToss = ss.getBeat(b).getHand(h).getToss(t);
						throwBall(h, curToss.destHand(), curToss.height());
						System.out.println("\t\t\tthrew ball ");
						System.out.println("\t\t\tstate: " + hands);
					}
				}
				//advance time
				advanceTime();
				System.out.println("\t\tadvanced time");
				System.out.println("\t\tstate: " + hands);
				b++;
			}
		}

		private void throwBall(int fromHand, int toHand, int toBeat) {
			System.out.println("\t\t\tfromHand: " + fromHand + ", toHand: " + toHand + ", toBeat: " + toBeat);
			hands.get(fromHand).resetCurValue();
			hands.get(toHand).incrementValue(toBeat);
		}

		private void advanceTime() {
			for(HandState hs : hands) {
				hs.advanceTime();
			}
		}

		public String toString() {
			return hands.toString();
		}

		private class HandState {
			private HandStateNode curNode;
			private HandStateNode lastNode;
			private int length;

			/*KEY:
			  <---         later    ---        sooner    ---    	  now]
			  lastNode lastNode.nextNode   ...   curNode.prevNode curNode

			 */

			private HandState() {
				this.curNode = new HandStateNode(null, null);
				this.lastNode = curNode;
				this.length = 1;
				if(length > maxHandStateLength) {
					maxHandStateLength = 1;
				}
			}

			private int curValue() {
				return curNode.value();
			}

			private void incrementCurValue() {
				curNode.incrementValue();
			}

			private void resetCurValue() {
				curNode.resetValue();
			}

			private void incrementValue(int beatsFromNow) {
				//get node at which the value is to be inserted, making new empty filler nodes in between if necessary
				HandStateNode temp = curNode;
				for(int i=0; i<beatsFromNow; i++) {
					temp = temp.getPrevNode();

				}
				//increment value of that node
				temp.incrementValue();
			}

			private void advanceTime() {
				curNode = curNode.getPrevNode();
				//curNode.nextNode = null;
			}

			public String toString() {
				String out = "";
				HandStateNode n = curNode;
				while(n != null) {
					out = (new Integer(n.value())).toString() + out;
					n = n.prevNode(); //not getPrevNode() b/c we don't want this to go forever!!
				}
				//annex buffer "0"s to front so the handstates all line up
				while(out.length() < maxHandStateLength) {
					out = "0" + out;
				}
				return out;
			}

			private class HandStateNode {
				private int value; //number of balls at this beat, in this hand
				//private HandStateNode nextNode;
				private HandStateNode prevNode;

				private HandStateNode(HandStateNode prevNode, HandStateNode nextNode) {
					this.value = 0;
					this.prevNode = prevNode;
					//this.nextNode = nextNode;
				}

				private void incrementValue() {
					value++;
				}

				private HandStateNode getPrevNode() {
					if(prevNode == null) {
						prevNode = new HandStateNode(null, this);
					}
					return prevNode;
				}

				private HandStateNode prevNode() {
					return prevNode;
				}

				/*private HandStateNode nextNode() {
					return nextNode;
				}*/

				private void resetValue() {
					value = 0;
				}

				private int value() {
					return value;
				}
			}
		}
	}

	public static void main(String[] args) {
		if(args.length == 1) {
			Siteswap ss = Parser.parse(args[0]);
			System.out.println("parsed: " + ss);
			System.out.println("de-parsed: " + Parser.deParse(ss));
			System.out.println("number of balls: " + ss.numBalls());
			System.out.println("valid: " + ss.isValid());
			if(ss.isValid()) {
				System.out.println("state: " + getState(ss));
			}
		}
	}
}
