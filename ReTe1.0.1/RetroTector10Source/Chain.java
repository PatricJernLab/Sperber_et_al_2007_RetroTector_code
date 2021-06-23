/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 8/12-06
* Beautified 8/12 -06
*/
package retrotector;

import java.util.*;

/**
* A collection of SubGeneHits, at most one from each SubGene.
*/
public class Chain implements Scorable {

// static fields
/**
* Broken Chains are not made with longer insertions than this
*/
  public static final int MAXINSERTION = 3600;
  
/**
* Used by RetroVID to count created simple Chains.
*/
	public static long simpleCounter = 0;
	
/**
* Used by RetroVID to count created broken Chains.
*/
	public static long brokenCounter = 0;
	
/*
* Stack to assemble SubGeneHits for a putative Chain.
*/
	private static int topp = -1; // index of top element
	private static SubGeneHit[ ] hits = new SubGeneHit[SubGene.MAXSUBGENENUMBER + 1]; // SubGene hits
	private static RVector[ ] rvectors = new RVector[SubGene.MAXSUBGENENUMBER + 1]; // RVectors of respective Chains
	private static String[ ] rvgenus = new String[SubGene.MAXSUBGENENUMBER + 1]; // corresponding to rvectors
	private static float[ ] scores = new float[SubGene.MAXSUBGENENUMBER + 1]; // scores of respective Chains
  private static int[ ] mask = new int[SubGene.MAXSUBGENENUMBER + 1]; // SubGene masks of respective Chains

	private static int stackstart; // Start position in DNA of material on assembly stack
	private static SubGeneHit[ ] orderedHits = new SubGeneHit[SubGene.MAXSUBGENENUMBER]; // hits positioned by SubGene
	
// mechanism for making broken Chains
  private static boolean makingBroken = false; // true if making Chain from two Chains
  
  private static Chain firstPartChain; // the first part to make a broken Chain from
  private static Chain secondPartChain; // the second part to make a broken Chain from
  private static int brokenMask; // OR of CHAINMASKS of parts
  private static float brokenPenalty; // Score penalty factor for broken chain
  private static RVector brokenRV; // RVector of broken Chain
	private static float brokenScore; // Score of broken Chain
  
// static methods

/**
* Sets the first Chain to be used in constructing a broken Chain.
* @param	ch		The Chain in question.
* @param	brPen	Score penalty factor for broken chain. Not sure it is actually used.
*/
  public static final void setFirstPartChain(Chain ch, float brPen) {
    firstPartChain = ch;
		secondPartChain = null;
		brokenRV = null;
    makingBroken = true;
    brokenPenalty = brPen;
		brokenScore = 0;
  } // end of setFirstPartChain(Chain, float)

/**
* Sets the second Chain to be used in constructing a broken Chain.
* @param	ch		The Chain in question.
* @return	True if a broken Chain can be made.
*/
  public static final boolean setSecondPartChain(Chain ch) {
    if (!makingBroken) {
      return false;
    }
    int x = ch.firstSubGeneHit().SGHITHOTSPOT - firstPartChain.lastSubGeneHit().SGHITHOTSPOT;
    if (x < 0) { // parts too close?
      return false;
    }
    int maxi = MAXINSERTION + firstPartChain.lastSubGeneHit().PARENTSUBGENE.distanceTo(ch.firstSubGeneHit().PARENTSUBGENE).HIGHESTDISTANCE;
    if (x > maxi) { // parts too distant?
      return false;
    }
    
    if (firstPartChain.lastSubGeneHit().PARENTSUBGENE.SUBGENEINDEX >= ch.firstSubGeneHit().PARENTSUBGENE.SUBGENEINDEX) {
      return false; // overlapping SubGenes
    }

		float integBonus = 0;
    LTRMotifHit lh1; // companion of 5'LTR
    LTRMotifHit lh2; // 3'LTR
// if both parts contain LTRs, are they a pair?
    if ((ch.lastSubGeneHit().PARENTSUBGENE.ISLTRSUBGENE) & (firstPartChain.firstSubGeneHit().PARENTSUBGENE.ISLTRSUBGENE)) { // there are two LTRs
      lh1 = ((LTRMotifHit) firstPartChain.firstSubGeneHit().firstMotifHit()).companion;
      lh2 = (LTRMotifHit) ch.lastSubGeneHit().firstMotifHit();
			if ((lh1 == null) || (lh2.companion == null) || (!lh1.similar(lh2))) {
				return false; // not a pair, do not use both
			} else {
				integBonus = integrationBonus(lh1.integSites);
			}
    }

    secondPartChain = ch;
    brokenRV = firstPartChain.CHAINRVECTOR.copy();
    brokenRV.plus(secondPartChain.CHAINRVECTOR);
		brokenScore = brokenRV.modulus() * brokenPenalty + integBonus;
    brokenMask = firstPartChain.CHAINMASK | secondPartChain.CHAINMASK;
    return true;
  } // end of setSecondPartChain(Chain)
  
// calculates bonus for good integration repeats as 4 * (right - wrong) (not negative)
	private final static float integrationBonus(String instr) {
		String ins = instr.trim();
		ins = ins.substring(ins.lastIndexOf(" ") + 1);
		String ins1 = ins.substring(0, ins.indexOf("<>"));
		String ins2 = ins.substring(ins.indexOf("<>") + 2);
		int right = 0;
		int wrong = 0;
		for (int i=0; i<ins1.length()-3; i++) {
			if (ins1.charAt(i) == ins2.charAt(i+3)) {
				right++;
			} else {
				wrong++;
			}
		}
		if (ins1.charAt(ins1.length() - 2) == 't') {
			right++;
		} else {
			wrong++;
		}
		if (ins1.charAt(ins1.length() - 1) == 'g') {
			right++;
		} else {
			wrong++;
		}
		if (ins2.charAt(0) == 'c') {
			right++;
		} else {
			wrong++;
		}
		if (ins2.charAt(1) == 'a') {
			right++;
		} else {
			wrong++;
		}
		if (right > wrong) {
			return (right - wrong) * 4.0f;
		} else {
			return 0.0f;
		}
	} // end of integrationBonus(String)
  
/**
* @return	True if no SubGeneHit on assembly stack.
*/
	public static final boolean stackEmpty() {
		return (topp < 0);
	} // end of stackEmpty()
	
/**
* Empties assembly stack.
*/
	public static final void resetStack() {
		topp = -1;
		stackstart = -1;
    for (int i=0; i<orderedHits.length; i++) {
      orderedHits[i] = null;
    }
	} // end of resetStack()
	
/**
* @return	Top SubGeneHit on assembly stack, or last SubGeneHit in broken Chain, or null.
*/
	public static final SubGeneHit peekSubGeneHit() {
    if (makingBroken) {
      return secondPartChain.lastSubGeneHit();
    } else {
      if (topp < 0) {
        return null;
      } else {
        return hits[topp];
      }
    }
	} // end of peekSubGeneHit()

/**
* @return	Top RvGenus on assembly stack, or RvGenus of broken Chain, or null.
*/
	public static final String peekRvGenus() throws RetroTectorException {
    if (makingBroken) {
      return brokenRV.rvGenus();
    } else {
      if (topp < 0) {
        return null;
      } else {
        return rvgenus[topp];
      }
    }
	} // end of peekRvGenus()

/**
* @return	Top score on assembly stack, or score of broken Chain, or Float.NaN.
*/
	public static final float peekScore() {
    if (makingBroken) {
      return brokenScore;
    } else {
      if (topp < 0) {
        return Float.NaN;
      } else {
        return scores[topp];
      }
    }
	} // end of peekScore()

/**
* @return	Start position in DNA of material on assembly stack, or Integer.MIN_VALUE.
*/
	public static final int peekStart() {
    if (makingBroken) {
      return firstPartChain.CHAINSTART;
    } else {
      if (topp < 0) {
        return Integer.MIN_VALUE;
      } else {
        return stackstart;
      }
    }
	} // end of peekStart()

/**
* @param	sgh					A SubGeneHit to try to add to end of assembly stack.
* @param	lengthBonus	Factor to multiply by for each hit
* @param	force				If true, do not check for acceptability.
* @return	True if sgh accepted and pushed.
*/
	public static final boolean pushSubGeneHit(SubGeneHit sgh, float lengthBonus, boolean force) throws RetroTectorException {
    if (topp < 0 ) { // stack empty.Go ahead
			topp = 0;
			hits[topp] = sgh;
      orderedHits[sgh.SUBGINDEX] = sgh;
			rvectors[topp] = sgh.RVECTOR.copy();
			scores[topp] = rvectors[topp].modulus();
			stackstart = sgh.FIRSTHITSTART;
			rvgenus[topp] = rvectors[topp].rvGenus();
      mask[topp] = sgh.SUBGMASK;
      makingBroken = false;
			return true;
		}
    
// check acceptable distance between sgh and top of stack
    int dist = hits[topp].SGHITHOTSPOT - sgh.SGHITHOTSPOT;
    LTRMotifHit lh1;
    LTRMotifHit lh2;
		float integBonus = 0;
		DistanceRange dr = sgh.PARENTSUBGENE.distanceTo(hits[topp].PARENTSUBGENE);
		if (force | (dr.containsDistance(dist))) { // distance OK or irrelevant
// if there are two LTRs, are they a pair?
      if ((sgh.PARENTSUBGENE.ISLTRSUBGENE) & (hits[0].PARENTSUBGENE.ISLTRSUBGENE)) {
        lh1 = ((LTRMotifHit) sgh.firstMotifHit()).companion;
        lh2 = (LTRMotifHit) hits[0].firstMotifHit();
				if ((lh1 == null) || (lh2.companion == null) || (!lh1.similar(lh2))) {
					return false; // not a pair, do not use both
				} else { // evaluate similarity between integration sites
					integBonus = integrationBonus(lh1.integSites);
				}
      }

// all fine, push it
      topp++;
      hits[topp] = sgh;
      orderedHits[sgh.SUBGINDEX] = sgh;
      rvectors[topp] = rvectors[topp - 1].copy();
      rvectors[topp].plus(sgh.RVECTOR);
      rvectors[topp].multiplyBy(lengthBonus);
      scores[topp] = rvectors[topp].modulus() + integBonus;
      rvgenus[topp] = rvectors[topp].rvGenus();
      mask[topp] = mask[topp - 1] | sgh.SUBGMASK;
      return true;
		} else {
			return false;
		}
	} // end of pushSubGeneHit(SubGeneHit, float, boolean)
	
/**
* @param	chain	A Chain.
* @return	True if assembly stack contains all SubGeneHits in chain.
*/
	public static final boolean stackContainsChain(Chain chain) {
    if (makingBroken) {
      if ((firstPartChain == null) | (secondPartChain == null)) {
        return false;
      }
      if ((chain.CHAINMASK & ~brokenMask) != 0) {
        return false; // not even all SubGenes
      }
      SubGeneHit sgh;
      for (int g=0; g<chain.COLLECTOR.COLLECTORDATABASE.subGeneNames.length; g++) {
        sgh = chain.getSubGeneHit(g);
        if ((sgh != null) && (!firstPartChain.containsSubGeneHit(sgh)) && (!secondPartChain.containsSubGeneHit(sgh)) ) {
          return false;
        }
      }
      return true;
    } else {
      if (topp < 0) {
        return false;
      }
      if ((chain.CHAINMASK & ~mask[topp]) != 0) {
        return false; // not even all SubGenes
      }
      SubGeneHit sgh;
      for (int g=0; g<chain.COLLECTOR.COLLECTORDATABASE.subGeneNames.length; g++) {
        sgh = chain.getSubGeneHit(g);
        if ((sgh != null) && (sgh != orderedHits[g])) {
          return false;
        }
      }
      return true;
    }
	} // end of stackContainsChain(Chain)

/**
* @param	chain	A Chain.
* @return	True if chain contains all SubGeneHits in assembly stack.
*/
	public final static boolean chainContainsStack(Chain chain) {
    if (makingBroken) {
      if ((firstPartChain == null) | (secondPartChain == null)) {
        return false;
      }
      if ((brokenMask & ~chain.CHAINMASK) != 0) {
        return false; // not even all SubGenes
      }
      SubGeneHit sgh;
      for (int g=0; g<chain.COLLECTOR.COLLECTORDATABASE.subGeneNames.length; g++) {
        sgh = chain.getSubGeneHit(g);
				if (sgh != null) {
					if (((firstPartChain.getSubGeneHit(g) != null) && (firstPartChain.getSubGeneHit(g) != sgh)) | ((secondPartChain.getSubGeneHit(g) != null) && (secondPartChain.getSubGeneHit(g) != sgh))) {
						return false;
					}
				}
      }
      return true;
    } else {
      if (topp < 0) {
        return true;
      }
      if ((mask[topp] & ~chain.CHAINMASK) != 0) {
        return false; // not even all SubGenes
      }
      for (int g=0; g<orderedHits.length; g++) {
        if ((orderedHits[g] != null) && (orderedHits[g] != chain.getSubGeneHit(g))) {
          return false;
        }
      }
      return true;
    }
	} // end of chainContainsStack(Chain)
		
/**
* Remove top element from assembly stack.
* @return	True if assembly stack was not empty.
*/
	public final static boolean pop() {
		if (topp >= 0) {
      orderedHits[hits[topp].SUBGINDEX] = null;
			topp--;
			return true;
		} else {
			return false;
		}
	} // end of pop()
	
/**
* Adds or switches a new PBS into an existing Chain, disregarding distance constraints.
* @param	hit						The PBS in question.
* @param	chain					The existing Chain
* @param	breakPenalty	Penalty factor for broken chain.
* @param	lengthBonus		Factor to multiply by for each hit.
* @return	A new Chain starting with ltr5, or null
*/
	public final static Chain setPBS(MotifHit hit, Chain chain, float breakPenalty, float lengthBonus) throws RetroTectorException {
		Chain fch;
		if (chain.ISBROKEN) {
			fch = setPBS(hit, chain.PART1, breakPenalty, lengthBonus);
			if (fch == null) {
				return null;
			}
			setFirstPartChain(fch, breakPenalty);
			if (!setSecondPartChain(chain.PART2)) {
				return null;
			}
		} else {
			SubGeneHit pbsHit = new SubGeneHit(hit);
			SubGeneHit fiHit = null;
			resetStack();
			if (chain.SUBGENEHITS[0] != null) {
				pushSubGeneHit(chain.SUBGENEHITS[0], lengthBonus, false);
			}
			pushSubGeneHit(pbsHit, lengthBonus, true);
			for (int i=2; i<chain.SUBGENEHITS.length; i++) {
				if (chain.SUBGENEHITS[i] != null) {
					pushSubGeneHit(chain.SUBGENEHITS[i], lengthBonus, true);
				}
			}
		}
		return new Chain(chain.SOURCEDNA, chain.STRANDCHAR, breakPenalty, chain.COLLECTOR);

	} // end of setPBS(MotifHit, Chain, float, float)

/**
* Adds or switches new LTRs into an existing Chain, disregarding distance constraints.
* @param	ltr5					The 5LTR in question.
* @param	ltr3					The 3LTR in question.
* @param	chain					The existing Chain
* @param	breakPenalty	Penalty factor for broken chain.
* @param	lengthBonus		Factor to multiply by for each hit
* @return	A new Chain ending with ltr3, or null.
*/
	public final static Chain setLTRs(LTRMotifHit ltr5, LTRMotifHit ltr3, Chain chain, float breakPenalty, float lengthBonus) throws RetroTectorException {
		if ((ltr5 == null) & (ltr3 == null)) {
			return null;
		}
		
		Chain fch;
		Chain sch;
		if (chain.ISBROKEN) {
			fch = setLTRs(ltr5, null, chain.PART1, breakPenalty, lengthBonus);
			if (fch == null) {
				return null;
			}
			sch = setLTRs(null, ltr3, chain.PART2, breakPenalty, lengthBonus);
			if (sch == null) {
				return null;
			}
			setFirstPartChain(fch, breakPenalty);
			if (setSecondPartChain(sch)) {
				return new Chain(chain.SOURCEDNA, chain.STRANDCHAR, breakPenalty, chain.COLLECTOR);
			} else {
				return null;
			}
		} else if (ltr3 == null) {
			resetStack();
			SubGeneHit ltrHit = new SubGeneHit(ltr5);
			pushSubGeneHit(ltrHit, lengthBonus, true);
			for (int i=1; i<chain.SUBGENEHITS.length; i++) {
				if (chain.SUBGENEHITS[i] != null) {
					pushSubGeneHit(chain.SUBGENEHITS[i], lengthBonus, true);
				}
			}
			return new Chain(chain.SOURCEDNA, chain.STRANDCHAR, breakPenalty, chain.COLLECTOR);
		} else if (ltr5 == null) {
			resetStack();
			SubGeneHit ltrHit = new SubGeneHit(ltr3);
			for (int i=0; i<chain.SUBGENEHITS.length-1; i++) {
				if (chain.SUBGENEHITS[i] != null) {
					pushSubGeneHit(chain.SUBGENEHITS[i], lengthBonus, true);
				}
			}
			pushSubGeneHit(ltrHit, lengthBonus, true);
			return new Chain(chain.SOURCEDNA, chain.STRANDCHAR, breakPenalty, chain.COLLECTOR);
		} else {
			resetStack();
			SubGeneHit ltrHit = new SubGeneHit(ltr5);
			pushSubGeneHit(ltrHit, lengthBonus, true);
			for (int i=1; i<chain.SUBGENEHITS.length-1; i++) {
				if (chain.SUBGENEHITS[i] != null) {
					pushSubGeneHit(chain.SUBGENEHITS[i], lengthBonus, true);
				}
			}
			ltrHit = new SubGeneHit(ltr3);
			pushSubGeneHit(ltrHit, lengthBonus, true);
			return new Chain(chain.SOURCEDNA, chain.STRANDCHAR, breakPenalty, chain.COLLECTOR);
		}
	} // end of setLTRs(LTRMotifHit, LTRMotifHit, Chain, float, float)
	
	
/**
* Adds or switches a new 5LTR into an existing Chain, regarding distance constraints. Not in use at present.
* @param	ltr5					The 5LTR in question.
* @param	chain					The existing Chain
* @param	breakPenalty	Penalty factor for broken chain.
* @param	lengthBonus		Factor to multiply by for each hit.
* @return	A new Chain starting with ltr5, or null
*/
 	public static Chain set5LTR(LTRMotifHit ltr5, Chain chain, float breakPenalty, float lengthBonus) throws RetroTectorException {
		Chain fch;
		if (chain.ISBROKEN) {
			fch = set5LTR(ltr5, chain.PART1, breakPenalty, lengthBonus);
			if (fch == null) {
				return null;
			}
			setFirstPartChain(fch, breakPenalty);
			if (!setSecondPartChain(chain.PART2)) {
				return null;
			}
		} else {
			SubGeneHit ltrHit = new SubGeneHit(ltr5);
			SubGeneHit fiHit = null;
			int fi=1;
// locate first hit after 5'LTR
			while ((fi < chain.SUBGENEHITS.length) && (fiHit = chain.SUBGENEHITS[fi]) == null) {
				fi++;
			}
			if ((fiHit == null) || (ltrHit.PARENTSUBGENE.distanceTo(fiHit.PARENTSUBGENE).containsDistance(fiHit.SGHITHOTSPOT - ltrHit.SGHITHOTSPOT))) {
				resetStack();
				pushSubGeneHit(ltrHit, lengthBonus, true);
				for (int i=1; i<chain.SUBGENEHITS.length-1; i++) {
					if (chain.SUBGENEHITS[i] != null) {
						pushSubGeneHit(chain.SUBGENEHITS[i], lengthBonus, true);
					}
				}
			} else {
				setFirstPartChain(new Chain(ltrHit, chain.STRANDCHAR, chain.COLLECTOR), breakPenalty);
				if (!setSecondPartChain(chain)) {
					return null;
				}
			}
		}
		return new Chain(chain.SOURCEDNA, chain.STRANDCHAR, breakPenalty, chain.COLLECTOR);
	} // end of set5LTR
	
/**
* Adds or switches a new 3LTR into an existing Chain, regarding distance constraints. Not in use at present.
* @param	ltr3					The 3LTR in question.
* @param	chain					The existing Chain
* @param	breakPenalty	Penalty factor for broken chain.
* @param	lengthBonus		Factor to multiply by for each hit
* @return	A new Chain ending with ltr3, or null.
*/
 	public static Chain set3LTR(LTRMotifHit ltr3, Chain chain, float breakPenalty, float lengthBonus) throws RetroTectorException {
		if (chain.ISBROKEN) {
			setFirstPartChain(chain.PART1, breakPenalty);
			if (!setSecondPartChain(set3LTR(ltr3, chain.PART2, breakPenalty, lengthBonus))) {
				return null;
			}
		} else {
			SubGeneHit ltrHit = new SubGeneHit(ltr3);
			SubGeneHit fiHit = null;
			int fi = chain.SUBGENEHITS.length - 2;
// locate last hit before 3'LTR
			while ((fi >= 0) && (fiHit = chain.SUBGENEHITS[fi]) == null) {
				fi--;
			}
			if ((fiHit == null) || (ltrHit.PARENTSUBGENE.distanceTo(fiHit.PARENTSUBGENE).containsDistance(fiHit.SGHITHOTSPOT - ltrHit.SGHITHOTSPOT))) {
				resetStack();
				for (int i=0; i<chain.SUBGENEHITS.length-1; i++) {
					if (chain.SUBGENEHITS[i] != null) {
						pushSubGeneHit(chain.SUBGENEHITS[i], lengthBonus, true);
					}
				}
				pushSubGeneHit(new SubGeneHit(ltr3), lengthBonus, true);
			} else {
				setFirstPartChain(chain, breakPenalty);
				if (!setSecondPartChain(new Chain(ltrHit, chain.STRANDCHAR, chain.COLLECTOR))) {
					return null;
				}
			}
		}
		return new Chain(chain.SOURCEDNA, chain.STRANDCHAR, breakPenalty, chain.COLLECTOR);
	} // end of set3LTR
	
    

//public fields
/**
* True if Chain composed by several Chains.
*/
	public final boolean ISBROKEN;
		
/**
*	 The DNA where the hits are.
*/
	public final DNA SOURCEDNA;
	
/**
* The internal position in the DNA of the start of the first MotifHit.
*/
	public int CHAINSTART;
	
/**
* The internal position in the DNA of the end of the last MotifHit.
*/
	public int CHAINEND;
	
/**
* The score of the whole Chain.
*/
	public final float CHAINSCORE;
	
/**
* String of RVector.RVCHARS representing rvgenus of this Chain.
*/
	public final String CHAINRVGENUS;
	
/**
* First part of broken Chain.
*/
	final Chain PART1;
	
/**
* Second part of broken Chain.
*/
	final Chain PART2;	
	
/**
* The first SubGeneHit.
*/
  public final SubGeneHit FIRSTHIT;
	
/**
* The last SubGeneHit.
*/
  public final SubGeneHit LASTHIT;
  
/**
* The number of SubGeneHits. Used by ChainGraphInfo.
*/
  public final int HITCOUNT;

/**
* Specifies virus genus in vector form.
*/
	public final RVector CHAINRVECTOR;
	
/**
* 'P' or 'S'
*/
	public final char STRANDCHAR;
  
/**
* OR of SUBGMASK of the SubGeneHits.
*/
  public final int CHAINMASK;
	
/**
* The ChainCollector that created this.
*/
  public final ChainCollector COLLECTOR;

/**
* Assigned by RetroVID.
*/
	public int chainNumber = -1;
  
/**
* Assigned by RetroVID using INTEGRATIONSITES in LTRPair.
*/
  public String ltrPairComment = "";
	
/**
* Information about what has been done to this, mainly by ChainProcessors.
*/
	public String[ ] processingHistory = null;
  
/**
* Next in linked list.
*/
	public Chain nextChain = null;
  
	
/**
* The array of SubGeneHits, at indices according to SUBGINDEX.
*/
	private final SubGeneHit[ ] SUBGENEHITS;
	
	private boolean selected = false;
	
/**
* Dummy Chain to use as start of linked list.
*/
	public Chain(	) {
 		ISBROKEN = false;
 		PART1 = null;
 		PART2 = null;
 		SOURCEDNA = null;
		SUBGENEHITS = null;
    FIRSTHIT = null;
    LASTHIT = null;
    HITCOUNT = 0;
		CHAINRVECTOR = null;
		STRANDCHAR = 'n';
		CHAINSTART = -1;
		CHAINEND = -1;
		CHAINRVGENUS = null;
		CHAINSCORE = Float.NaN;		
		CHAINMASK = 0;
    COLLECTOR = null;
	} // end of constructor()
	
/**
* Constructor for a Chain with a single SubGeneHit.
* @param	sgh					The SubGeneHit in question.
* @param	strandchar	'P' or 'S'.
* @param	collector		The ChainCollector creating this.
*/
	public Chain(SubGeneHit sgh, char strandchar, ChainCollector collector) {
 		ISBROKEN = false;
 		PART1 = null;
 		PART2 = null;
 		SOURCEDNA = sgh.SOURCEDNA;
    SUBGENEHITS = new SubGeneHit[collector.COLLECTORDATABASE.subGeneNames.length];
    SUBGENEHITS[sgh.SUBGINDEX] = sgh;
    FIRSTHIT = sgh;
    LASTHIT = sgh;
    HITCOUNT = 1;
		CHAINRVECTOR = sgh.RVECTOR.copy();
		STRANDCHAR = strandchar;
		CHAINSTART = sgh.FIRSTHITSTART;
		CHAINEND = sgh.LASTHITEND;
		CHAINRVGENUS = sgh.RVGENUS;
		CHAINSCORE = sgh.SCORE;		
		CHAINMASK = sgh.SUBGMASK;
    COLLECTOR = collector;
	} // end of constructor(SubGeneHit, char, ChainCollector)

/**
* Constructs a simple Chain out of the assembly stack or partial Chains.
* Calculates score and rvgenus.
* @param	sDNA					The DNA where the hits are.
* @param	strandchar		'P' or 'S'.
* @param	breakPenalty	Penalty factor for broken chain.
* @param	collector			The ChainCollector creating this.
*/
 	public Chain(DNA sDNA, char strandchar, float breakPenalty, ChainCollector collector) throws RetroTectorException {
 	
    COLLECTOR = collector;
    if (!makingBroken) {
      ISBROKEN = false;
      PART1 = null;
      PART2 = null;
      SOURCEDNA = sDNA;
      SUBGENEHITS = new SubGeneHit[COLLECTOR.COLLECTORDATABASE.subGeneNames.length];
      HITCOUNT = topp + 1;
      System.arraycopy(orderedHits, 0, SUBGENEHITS, 0, SUBGENEHITS.length);
      FIRSTHIT = hits[0];
      LASTHIT = hits[topp];
      CHAINRVECTOR = rvectors[topp].copy();
      STRANDCHAR = strandchar;
      CHAINSTART = stackstart;
      CHAINEND = LASTHIT.LASTHITEND;
      CHAINRVGENUS = peekRvGenus();
      CHAINSCORE = peekScore();
      CHAINMASK = mask[topp];
      simpleCounter++;
    } else {
      ISBROKEN = true;
      SOURCEDNA = sDNA;
      SUBGENEHITS = null;
      PART1 = firstPartChain;
      PART2 = secondPartChain;
      HITCOUNT = PART1.HITCOUNT + PART2.HITCOUNT;
      FIRSTHIT = PART1.FIRSTHIT;
      LASTHIT = PART2.LASTHIT;
      
      CHAINRVECTOR = new RVector(PART1.CHAINRVECTOR.getContent());
      CHAINRVECTOR.plus(PART2.CHAINRVECTOR);
      CHAINRVECTOR.multiplyBy(breakPenalty);
      CHAINRVGENUS = CHAINRVECTOR.rvGenus();
      CHAINSCORE = brokenScore;
      STRANDCHAR = strandchar;
          
      CHAINSTART = PART1.CHAINSTART;
      CHAINEND = PART2.CHAINEND;
      CHAINMASK = PART1.CHAINMASK | PART2.CHAINMASK;
      brokenCounter++;
    }
// if two LTRs, check compatibility and adapt ltrPairComment
		ltrPairComment = "";
    if (FIRSTHIT.PARENTSUBGENE.ISLTRSUBGENE & LASTHIT.PARENTSUBGENE.ISLTRSUBGENE) {
      if (!((LTRMotifHit) FIRSTHIT.firstMotifHit()).integSites.equals(((LTRMotifHit) LASTHIT.firstMotifHit()).integSites)) {
				RetroTectorException.sendError(this, "Integration site mismatch");
      }
      ltrPairComment = "Integration sites " + ((LTRMotifHit) FIRSTHIT.firstMotifHit()).integSites;
    } else if (FIRSTHIT.PARENTSUBGENE.ISLTRSUBGENE & !LASTHIT.PARENTSUBGENE.ISLTRSUBGENE) {
      if (((LTRMotifHit) FIRSTHIT.firstMotifHit()).companion != null) {
				ltrPairComment = "Missing 3'LTR at " + SOURCEDNA.externalize(((LTRMotifHit) FIRSTHIT.firstMotifHit()).companion.MOTIFHITHOTSPOT);
			}
    } else if (!FIRSTHIT.PARENTSUBGENE.ISLTRSUBGENE & LASTHIT.PARENTSUBGENE.ISLTRSUBGENE) {
			if (((LTRMotifHit) LASTHIT.firstMotifHit()).companion != null) {
				ltrPairComment = "Missing 5'LTR at " + SOURCEDNA.externalize(((LTRMotifHit) LASTHIT.firstMotifHit()).companion.MOTIFHITHOTSPOT);
			}
    } else {
      ltrPairComment = "";
    }
    
	} // end of constructor(DNA, char, float, ChainCollector)
  
/**
* @param	ch	Another Chain.
* @return	True if all SubGeneHits in ch are also in this.
*/
	public final boolean containsChain(Chain ch) {
    if ((ch.CHAINMASK & ~CHAINMASK) != 0) {
      return false;
    }
    SubGeneHit sgh;
		for (int g=0; g<ch.COLLECTOR.COLLECTORDATABASE.subGeneNames.length; g++) {
      sgh = ch.getSubGeneHit(g);
			if ((sgh != null) && (sgh != getSubGeneHit(g))) {
				return false;
			}
		}
		return true;
	} // end of containsChain(Chain)
	
/**
* Finds out if another chain overlaps this one.
* The two Chains may be from different DNA, ie strands.
* @param	testchain	The other chain to compare with.
* @return	True if testchain fulfills those criteria.
*/
 	public final boolean touchedBy(Chain testchain) {
 		int i1 = SOURCEDNA.internalize(testchain.SOURCEDNA.externalize(testchain.CHAINSTART));
 		if (containsPos(i1)) {
 			return true;
 		}
 		int i2 = SOURCEDNA.internalize(testchain.SOURCEDNA.externalize(testchain.CHAINEND));
 		if (containsPos(i2)) {
 			return true;
 		}
 		if (((i1 - CHAINSTART) * (i2 - CHAINSTART)) < 0) { // i1 and i2 on different sides
 			return true;
 		}
 		return false;
 	} // end of touchedBy(Chain)
				
/**
* Finds out if another, selected, chain overlaps this one, and is better.
* The two Chains may be from different DNA, ie strands.
* @param	testchain	The other chain to compare with.
* @return	True if testchain fulfills those criteria.
*/
 	public final boolean majoredBy(Chain testchain) {
 		if (!testchain.isSelected()) {
 			return false;
 		}
 		if (testchain.CHAINSCORE < CHAINSCORE) {
 			return false;
 		}
 		return touchedBy(testchain);
 	} // end of majoredBy(Chain)
				
/**
* Mark this chain as selected, or not.
* @param	flag	True to select, false to unselect.
*/
	public final void select(boolean flag) {
		selected = flag;
	} // end of select(boolean)
	
/**
* @return	True if this Chain is selected.
*/
	public final boolean isSelected() {
		return selected;
	} // end of isSelected()
	
/**
* @return	The first SubGeneHit.
*/
	public final SubGeneHit firstSubGeneHit() {
		return FIRSTHIT;
	} // end of firstSubGeneHit()

/**
* @return	The 5'LTR, if there is one.
*/
  public final SubGeneHit get5LTR() {
    if (FIRSTHIT.PARENTSUBGENE.ISLTRSUBGENE) {
      return FIRSTHIT;
    } else {
      return null;
    }
  } // end of get5LTR()

/**
* @return	The last SubGeneHit.
*/
	public final SubGeneHit lastSubGeneHit() {
		return LASTHIT;
	} // end of lastSubGeneHit()
	
/**
* @return	The 3'LTR, if there is one.
*/
  public final SubGeneHit get3LTR() {
    if (LASTHIT.PARENTSUBGENE.ISLTRSUBGENE) {
      return LASTHIT;
    } else {
      return null;
    }
  } // end of get3LTR()

/**
* A specified SubGeneHit.
* @param	index	The index of the SubGeneHit.
* @return	The SubGeneHit with that index, or null.
*/
	public final SubGeneHit getSubGeneHit(int index) {
		if (ISBROKEN) {
			SubGeneHit sgh = PART1.getSubGeneHit(index);
			if (sgh != null) {
				return sgh;
			}
			return PART2.getSubGeneHit(index);
		} else {
			if ((index<0) | (index >= SUBGENEHITS.length)) {
				return null;
			} else {
				return SUBGENEHITS[index];
			}
		}
	} // end of getSubGeneHit(int)

/**
* @return	The last MotifHit with PPT Motif, or null. Not in actual use at present.
*/
	public final MotifHit getPPThit() {
		MotifHit mh;
		if (ISBROKEN) {
			mh = PART2.getPPThit();
			if (mh == null) {
				return PART1.getPPThit();
			} else {
				return mh;
			}
		} else {
			if (SUBGENEHITS[SUBGENEHITS.length - 2] != null) {
				return SUBGENEHITS[SUBGENEHITS.length - 2].firstMotifHit();
			} else {
				return null;
			}
		}
	} // end of getPPThit()
	
/**
* @return	The first MotifHit with PBS Motif, or null.
*/
	public MotifHit getPBShit() {
		MotifHit mh;
		if (ISBROKEN) {
			mh = PART1.getPBShit();
			if (mh == null) {
				return PART2.getPBShit();
			} else {
				return mh;
			}
		} else {
			if (SUBGENEHITS[1] != null) {
				return SUBGENEHITS[1].firstMotifHit();
			} else {
				return null;
			}
		}
	} // end of getPBShit()
	
/**
* The score of the whole Chain. Required by Scorable.
*/
 	public final float fetchScore() {
		return CHAINSCORE;
	} // end of fetchScore()
	
/**
* @return	String describing the breaks if BROKEN, or empty String.
*/
	public final String breaks() {
		if (ISBROKEN) {
			return PART1.breaks() + " " + SOURCEDNA.externalize(PART1.CHAINEND) + ">" + SOURCEDNA.externalize(PART2.CHAINSTART) + " " + PART2.breaks();
		}
		return "";
	} // end of breaks()
			
	
/**
* @return	Short text description of Chain. Mainly for debugging.
*/
 	public String toString() {
 		if (ISBROKEN) {
      SubGeneHit sgh;
			StringBuffer out = new StringBuffer(1000);
			out.append("BrokenChain at " + SOURCEDNA.externalize(CHAINSTART) + " score=" + CHAINSCORE + "  Rvgenus=" + CHAINRVGENUS);
			for (int g=0; g<PART1.COLLECTOR.COLLECTORDATABASE.subGeneNames.length; g++ ) {
        sgh = PART1.getSubGeneHit(g);
        if (sgh != null) {
          out.append("\n   " + sgh);
        }
			}
			for (int g=0; g<PART1.COLLECTOR.COLLECTORDATABASE.subGeneNames.length; g++ ) {
        sgh = PART2.getSubGeneHit(g);
        if (sgh != null) {
          out.append("\n   " + sgh);
        }
			}
			return out.toString();
 		}
 		
		int sum = 0;
    int gsum = 0;
		for (int g=0; g<SUBGENEHITS.length; g++ ) {
			if (SUBGENEHITS[g] != null) {
        sum += SUBGENEHITS[g].MOTIFHITS.length;
        gsum++;
      }
		}
		StringBuffer out = new StringBuffer(1000);
		out.append("Chain at " + SOURCEDNA.externalize(CHAINSTART) + " score=" + CHAINSCORE + "  " + gsum + " subgene hits " + sum + " Motif hits.Rvgenus=" + CHAINRVGENUS);
		for (int g=0; g<SUBGENEHITS.length; g++ ) {
			if (SUBGENEHITS[g] != null) {
        out.append("\n   " + SUBGENEHITS[g]);
      }
		}
		return out.toString();
	} // end of toString()

/**
* @param	sg	A SubGene.
* @return	The SubGeneHit from sg in this Chain, or null.
*/
	public final SubGeneHit hitFromSubGene(SubGene sg) {
    if (ISBROKEN) {
      SubGeneHit h = PART1.hitFromSubGene(sg);
      if (h != null) {
        return h;
      } else {
        return PART2.hitFromSubGene(sg);
      }
    } else {
      return SUBGENEHITS[sg.SUBGENEINDEX];
    }
	} // end of hitFromSubGene(SubGene)

/**
* @param	theGroup	A MotifGroup.
* @return	True if this contains a Motif hit from theGroup.
*/
	public final MotifHit hitInMotifGroup(MotifGroup theGroup) {
    SubGene sg = theGroup.SUBGENE;
    SubGeneHit sgh = hitFromSubGene(sg);
    if (sgh == null) {
      return null;
    } else {
      return sgh.hitInGroup(theGroup);
    }
  } // end of hitInMotifGroup(MotifGroup)
  
  
/**
* @param	gene	A Gene.
* @return	The acceptable range of the start of the Gene.
*/
	public final Range findGeneStartRange(Gene gene) throws RetroTectorException {
		if (ISBROKEN) {
      Range r1 = PART1.findGeneStartRange(gene);
      Range r2 = PART2.findGeneStartRange(gene);
      if (r1 == null) {
        return r2;
      } else if (r2 == null) {
        return r1;
      } else {
        r2 = Range.consensus(r1, r2);
        if (r2 == null) {
          return r1;
        } else {
          return r2;
        }
      }
		} else {
			Range result = null;
			Range ra;
			Range raa;
			MotifHit mh;
			for (int f=0; f<gene.GENEMOTIFGROUPS.length; f++) { // go through MotifGroups upwards
				mh = hitInMotifGroup(gene.GENEMOTIFGROUPS[f]);
				if (mh != null) { // get acceptable range relating to that hit
					ra = new Range(Range.GENESTART, mh.MOTIFHITHOTSPOT, gene.GENEMOTIFGROUPS[f].geneStartDistance);
					if (result == null) { // first found
						result = ra;
					} else { // is there still an acceptable range?
						raa = Range.consensus(result, ra);
						if (raa == null) {
							return result;
						} else {
							result = raa;
						}
					}
				}
			}
			if (result != null) {
				result = Range.consensus(result, new Range(result.SPECIFICATION, SOURCEDNA));
			}
			return result;
		}
	} // end of findGeneStartRange(Gene)
		
/**
* @param	gene	A Gene.
* @return	The acceptable range of the end of the Gene.
*/
	public final Range findGeneEndRange(Gene gene) throws RetroTectorException {
		if (ISBROKEN) {
      Range r1 = PART1.findGeneEndRange(gene);
      Range r2 = PART2.findGeneEndRange(gene);
      if (r1 == null) {
        return r2;
      } else if (r2 == null) {
        return r1;
      } else {
        r1 = Range.consensus(r1, r2);
        if (r1 == null) {
          return r2;
        } else {
          return r1;
        }
      }
		} else {
			Range result = null;
			Range ra;
			Range raa;
			MotifHit mh;
			for (int f=gene.GENEMOTIFGROUPS.length-1; f>=0; f--) { // go through MotifGroups from end
				mh = hitInMotifGroup(gene.GENEMOTIFGROUPS[f]);
				if (mh != null) {
					ra = new Range(Range.GENESTART, mh.MOTIFHITHOTSPOT, gene.GENEMOTIFGROUPS[f].geneEndDistance);
					if (result == null) {
						result = ra;
					} else {
						raa = Range.consensus(result, ra);
						if (raa == null) {
							return result;
						} else {
							result = raa;
						}
					}
				}
			}
			if (result != null) {
				result = Range.consensus(result, new Range(result.SPECIFICATION, SOURCEDNA));
			}
			return result;
		}
	} // end of findGeneEndRange(Gene)
	
/**
* Extends processingHistory by one line.
*	@param	s	The line.
*/
	public final void appendToHistory(String s) {
		if (processingHistory == null) {
			processingHistory = new String[1];
			processingHistory[0] = s;
		} else {
			String[ ] ss = new String[processingHistory.length + 1];
			System.arraycopy(processingHistory, 0, ss, 0, processingHistory.length);
			ss[ss.length - 1] = s;
			processingHistory = ss;
		}
	} // end of appendToHistory(String)

/**
* Extends processingHistory by several lines.
*	@param	s	The lines.
*/
	public final void appendToHistory(String[ ] s) {
		for (int i=0; i<s.length; i++) {
			appendToHistory(s[i]);
		}
	} // end of appendToHistory(String[ ])

/**
* @return Sum of lengths of LTRs.
*/
	public final int lengthLTRs() {
		SubGeneHit sgh;
		int result = 0;
		sgh = get5LTR();
		if (sgh != null) {
			result = sgh.LASTHITEND - sgh.FIRSTHITSTART;
		}
		sgh = get3LTR();
		if (sgh != null) {
			result += sgh.LASTHITEND - sgh.FIRSTHITSTART;
		}
		return result;
	} // end of lengthLTRs()

/**
* @param	genus	The character of a genus.
* @return	The cosine with that genus.
*/
	public final float getCosine(char genus) {
		return CHAINRVECTOR.directionCosine(RVector.rvindex(genus));
	} // end of getCosine(char)

// Is pos inside this Chain?
	private final boolean containsPos(int pos) {
		if (pos > CHAINEND) {
			return false;
		}
		if (pos < CHAINSTART) {
			return false;
		}
		return true;
	} // end of containsPos(int)
	
/**
 * Finds out whether this Chain contains a specific SubGeneHit.
 * @param	gh	The SubGeneHit to search for.
 * @return	True if it was found.
 */
 	private final boolean containsSubGeneHit(SubGeneHit gh) {
 		if (ISBROKEN) {
			return PART1.containsSubGeneHit(gh) | PART2.containsSubGeneHit(gh);
		}
		
		if (SUBGENEHITS == null) {
			return false;
		}
		return (gh == SUBGENEHITS[gh.SUBGINDEX]);
	} // end of containsSubGeneHit(SubGeneHit)

/**
* Mainly for debugging.
*/
  public int differentAt(Chain ch) {
    if (ISBROKEN | ch.ISBROKEN) {
      return -3;
    }
    for (int i=0; i<SUBGENEHITS.length; i++) {
      if ((SUBGENEHITS[i] != null) && (SUBGENEHITS[i].differentAt(ch.SUBGENEHITS[i]) != -1)) {
        return i;
      }
    }
    return -1;
  } // end of differentAt(Chain)
	
}
