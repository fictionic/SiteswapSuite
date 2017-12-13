package siteswapsuite;

import java.util.List;
import java.util.ArrayList;

public class NotatedSiteswap {

	SiteswapNotation notationType;
	Siteswap siteswap;

	// querying basic info
	public SiteswapNotation notationType() { return this.notationType; }
	public Siteswap siteswap() { return this.siteswap; }

	// printing notation
	public String display() {
		return null;
	}

	// deep copy
	public NotatedSiteswap deepCopy() {
		return null;
	}

	// constructor
	private NotatedSiteswap(Siteswap ss, SiteswapNotation notationType) {
		this.siteswap = ss;
		this.notationType = notationType;
	}

	// tokenization
	static enum TossHeightState {
		READY, // when there is no pending toss, and we can parse a new token
		SEEN_MINUS, // when we've seen a minus
		SEEN_ANTI, // when we've seen an underscore
		SEEN_BOTH, // when we've seen a minus and an underscore
		INSIDE_CURLY, // when we're reading a {literal height}
		AFTER_HEIGHT; // when we're awaiting a possible landing modifier
	}
	private static List<SiteswapNotationToken> tokenize(String notation) throws InvalidSiteswapNotationException {
		TossHeightState tossHeightState = TossHeightState.READY;
		StringBuilder curlyHeight = null; // height string parsed from {literal height} indication
		List<SiteswapNotationToken> tokens = new ArrayList<>();
		SiteswapNotationToken curToken = null;
		for(int i=0; i<notation.length(); i++) {
			char c = notation.charAt(i);
			Util.printf("c=" + c, Util.DebugLevel.DEBUG);
			switch(c) {
				case '*':
				case '!':
				case '[':
				case ']':
				case '(':
				case ')':
				case ',':
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.parseNonTossChar(c));
							tokens.add(curToken);
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				case '{':
					switch(tossHeightState) {
						case INSIDE_CURLY:
							throw new InvalidSiteswapNotationException();
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.Type.TOSS); // create new toss
							// fall through
						case SEEN_MINUS:
						case SEEN_ANTI:
						case SEEN_BOTH:
							curlyHeight = new StringBuilder();
							tossHeightState = TossHeightState.INSIDE_CURLY;
							break;
					}
					break;
				case '}':
					switch(tossHeightState) {
						case INSIDE_CURLY:
							curToken.toss.finiteAbsoluteHeight = Integer.parseInt(curlyHeight.toString());
							tossHeightState = TossHeightState.AFTER_HEIGHT;
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				case '-':
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.Type.TOSS);
							curToken.toss.isNegative = true;
							tossHeightState = TossHeightState.SEEN_MINUS;
							break;
						case SEEN_ANTI:
							curToken.toss.isNegative = true;
							tossHeightState = TossHeightState.SEEN_BOTH;
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				case '_':
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.Type.TOSS);
							curToken.toss.isAntitoss = true;
							tossHeightState = TossHeightState.SEEN_ANTI;
							break;
						case SEEN_MINUS:
							curToken.toss.isAntitoss = true;
							tossHeightState = TossHeightState.SEEN_BOTH;
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				case 'x':
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							curToken.toss.isFlippedHand = true;
							tokens.add(curToken);
							tossHeightState = TossHeightState.READY;
							break;
						default:
							throw new InvalidSiteswapNotationException();
					}
					break;
				default:
					switch(tossHeightState) {
						case AFTER_HEIGHT:
							tokens.add(curToken); // add previous toss
							tossHeightState = TossHeightState.READY;
							// fall through
						case READY:
							curToken = new SiteswapNotationToken(SiteswapNotationToken.Type.TOSS); // create new toss
							// fall through
						case SEEN_ANTI:
						case SEEN_MINUS:
						case SEEN_BOTH:
							// make sure it's a numeral
							TossToken rawTossToken = SiteswapNotationToken.parseTossChar(c);
							if(rawTossToken == null) {
								throw new InvalidSiteswapNotationException();
							}
							curToken.toss.isInfinite = rawTossToken.isInfinite;
							curToken.toss.finiteAbsoluteHeight = rawTossToken.finiteAbsoluteHeight;
							tossHeightState = TossHeightState.AFTER_HEIGHT;
							break;
						case INSIDE_CURLY:
							curlyHeight.append(c);
							break;
					}
			}
			Util.printf("curToken: " + curToken, Util.DebugLevel.DEBUG);
			Util.printf("tokens: " + tokens, Util.DebugLevel.DEBUG);
		}
		if(tossHeightState == TossHeightState.AFTER_HEIGHT) {
			tokens.add(curToken);
		}
		return tokens;
	}

	public static void main(String[] args) {
		try {
		List<SiteswapNotationToken> tokens = tokenize(args[0]);
		System.out.println(tokens);
		} catch(InvalidSiteswapNotationException e) {
			e.printStackTrace();
		}
	}

}
