/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 24/9 -06
* Beautified 24/9 -06
*/

package retrotectorcore;

import retrotector.*;
import java.util.*;

/**
* Class to handle Chain construction.
*/
public class CoreChainCollector {

/**
* The DNA to make Chains in.
*/
  private final DNA THEDNA;
  
/**
* Bonus factor for each new SubGene in Chain.
*/
  private final float LENGTHBONUS;

/**
* Largest number of SubGenes skipped in forming Chains.
*/
  private final int MAXSUBGENESKIP;

/**
* Score penalty for broken chain.
*/
  private final float BROKENPENALTY;
  
/**
* >0 if searching for broken Chains.
*/
  private final int SEARCHBROKEN;
  
/**
* The relevant database.
*/
  public final Database COLLECTORDATABASE;

/**
* Number of Chains submitted for inclusion. Mainly for debugging.
*/
	private long insertstried;
  
/**
* Number of Chains rejected because contained by a previous better one. Mainly for debugging.
*/
	private long contained;
  
/**
* Number of Chains rejected because a better one started at same position. Mainly for debugging.
*/
	private long equalled;

/**
* Number of Chains deleted after being accepted. Mainly for debugging.
*/
	private long deleted;

/**
* If True, additional information is output to screen.
*/
 	private boolean debugging = false;
  
/*
* One element for each SubGene.
*/
  private final SubGene[ ] SUBGENES;
	
/**
* Calling ChainCollector.
*/
	private ChainCollector PARENTCOLLECTOR;
	
	private boolean refreshed = false;
	private boolean subGenesScored = false;

/**
* Constructor.
* @param	theDNA					DNA to use.
* @param	lengthBonus			See LENGTHBONUS.
* @param	maxSubGeneSkip	See MAXSUBGENESKIP.
* @param	brokenPenalty		See BROKENPENALTY.
* @param	searchBroken		See SEARCHBROKEN.
* @param	database				See COLLECTORDATABASE.
* @param	parentCollector	Calling ChainCollector.
*/
 public CoreChainCollector(DNA theDNA, float lengthBonus, int maxSubGeneSkip, float brokenPenalty, int searchBroken, Database database, ChainCollector parentCollector) throws RetroTectorException {
    THEDNA = theDNA;
    LENGTHBONUS = lengthBonus;
    MAXSUBGENESKIP = maxSubGeneSkip;
    BROKENPENALTY = brokenPenalty;
    SEARCHBROKEN = searchBroken;
    COLLECTORDATABASE = database;
    SUBGENES = new SubGene[COLLECTORDATABASE.subGeneNames.length];
    for (int i=0; i<SUBGENES.length; i++) {
      SUBGENES[i] = COLLECTORDATABASE.getSubGene(i).subGeneCopy();
    }
		PARENTCOLLECTOR = parentCollector;
  } // end of constructor(DNA, float, int, float, int, Database, ChainCollector)
  
/**
* Refreshes all SubGenes and their parts.
* @param	inf	RefreshInfo	to send down the line.
*/
  public final void refreshCollector(Motif.RefreshInfo inf) throws RetroTectorException {
		if (refreshed) {
			throw new RetroTectorException("CoreChainCollector", "SubGenes already refreshed");
		}
    for (int i=0; i<SUBGENES.length; i++) {
      SUBGENES[i].refresh(inf);
    }
		refreshed = true;
  } // end of refreshCollector(Motif.RefreshInfo)
    
/**
* Scores all detection Motifs over whole THEDNA.
*/
  public final void scoreSubGenes() throws RetroTectorException {
		if (subGenesScored) {
			throw new RetroTectorException("CoreChainCollector", "SubGenes already scored");
		}
		int f = 0;
		if (THEDNA.FIRSTUNPADDED >= 0) {
			f = THEDNA.FIRSTUNPADDED;
		}
		int l = THEDNA.LENGTH - 1;
		if (THEDNA.LASTUNPADDED >= 0) {
			l = THEDNA.LASTUNPADDED;
		}
    for (int i=0; i<SUBGENES.length; i++) {
      SUBGENES[i].scoreSubGene(THEDNA, f, l);
    }
		subGenesScored = true;
  } // end of scoreSubGenes()

/**
* Scores all characterization Motifs within a range.
* @param	scoreFirst	Start (internal) position of range.
* @param	scoreLast		End (internal) position of range.
*/
  public final void scoreCharac(int scoreFirst, int scoreLast) throws RetroTectorException {
		int f = scoreFirst;
		if ((THEDNA.FIRSTUNPADDED >= 0) && (THEDNA.FIRSTUNPADDED > scoreFirst)) {
			f = THEDNA.FIRSTUNPADDED;
		}
		int l = scoreLast;
		if ((THEDNA.LASTUNPADDED >= 0) && (THEDNA.LASTUNPADDED < scoreLast)) {
			l = THEDNA.LASTUNPADDED;
		}
    for (int i=0; i<SUBGENES.length; i++) {
      SUBGENES[i].scoreCharac(THEDNA, f, l);
    }
  } // end of scoreCharac(int, int)

/**
* @param	index	An index in SUBGENES;
* @return The SubGene in question.
*/
	public final SubGene getSubGene(int index) {
		return SUBGENES[index];
	} // end of getSubGene(int)
	
/**
* Collects SubGene hits for all SubGenes within a range.
* @param	frameFactor	Factor to multiply score by if all MotifHits in same reading frame.
* @param	lo					First internal position to use.
* @param	hi					Last internal position to use.
*/
  public final void collectSubGeneHits(float frameFactor, int lo, int hi) throws RetroTectorException {
    for (int i=0; i<SUBGENES.length; i++) {
      SUBGENES[i].collectSubGeneHits(frameFactor, lo, hi);
    }
  } // end of collectSubGeneHits(float, int,int)
  
/**
 * Does the heavy work of putting Chains together and sifting them
 * @param	strandchar				'P' or 'S'.
 * @param	chainRangeMin			The first (internal) position to search at.
 * @param	chainRangeMax			The last (internal) position to search at.
 * @param	subGeneHitsLimit	Maximum number of SubGeneHits tried in one SubGene.
 * @param	keepThreshold			Chains with lower score than this are discarded early.
 * @param	commentLeader			String to use in info text. Used by ChainProcessor.
 * @return	Array of constructed Chains.
 */
 	public Chain[ ] collectChains(char strandchar, int chainRangeMin, int chainRangeMax, int subGeneHitsLimit, float keepThreshold, String commentLeader) throws RetroTectorException {
	
		Stack mainStack = new Stack(); // working stack
    
    listFoot = null; // empty Chain list
		
		Chain.resetStack(); // subgenehits are collected in Chains assembly Stack
		
		String mstr;
		if (commentLeader == null) {
			mstr = "Collecting simple chains in " + strandchar + " strand starting with SubGene ";
		} else {
			mstr = commentLeader;
		}

		SubGene tempsubgene;
		SubGene theSubGene; // current leading SubGene
		SubGeneHit tempsubgenehit;
		int gen;

// reset counters
		Chain.simpleCounter = 0;

		insertstried = 0;
		contained = 0;
		equalled = 0;
		deleted = 0;

// try starting with each subgene
		for (int subgeneindex=0; subgeneindex<SUBGENES.length; subgeneindex++) {
			RetroTectorEngine.setInfoField(mstr + COLLECTORDATABASE.subGeneNames[subgeneindex]);
			RetroTectorEngine.showProgress();
			theSubGene = SUBGENES[subgeneindex];
			if (debugging) {
				Utilities.outputString("  subgene=" + subgeneindex + " " + theSubGene.SUBGENENAME + " " + (new Date().toString()));
			}
			mainStack.push(theSubGene); // push subgene
// the internal workings of this mechanism are not quite simple. Seems to work though
			theSubGene.pushSubGeneHits(mainStack, null, subGeneHitsLimit, chainRangeMin, chainRangeMax); // push all its SubGeneHits
			int counter = 1000; // to keep track of updating progress indicator
			while (mainStack.size() > 0) {
				if (mainStack.peek() instanceof SubGene) { // no hits found
					if (!Chain.stackEmpty()) { // are there subgenes on assembly stack?
						insertChain(strandchar, keepThreshold); // make a new Chain and try it
						Chain.pop();
					}
					mainStack.pop();
				} else { // hits found
					tempsubgenehit = (SubGeneHit) mainStack.pop();
					tempsubgene = tempsubgenehit.PARENTSUBGENE;
					if (Chain.pushSubGeneHit(tempsubgenehit, LENGTHBONUS, false)) { // move one hit to hit stack
						mainStack.push(tempsubgene); // push its subgene
						int limit = Math.min(COLLECTORDATABASE.subGeneNames.length - 1, tempsubgene.SUBGENEINDEX + MAXSUBGENESKIP + 1); 
						for (gen = tempsubgene.SUBGENEINDEX + 1; gen <= limit; gen++) { // push all successor hits with right distance
							SUBGENES[gen].pushSubGeneHits(mainStack, tempsubgenehit, subGeneHitsLimit, chainRangeMin, chainRangeMax);
						}
					}
				}
				if (--counter <= 0) {
					counter = 1000;
					RetroTectorEngine.showProgress();
				}
			}
		}
		
    if ((listFoot == null) || (listFoot.nextChain == null)) {
      return null; // nothing found
    }
	
		if (debugging) {
			Utilities.outputString(Chain.simpleCounter + " simple Chains created");
			Utilities.outputString("insertstried=" + insertstried);
			Utilities.outputString("contained=" + contained);
			Utilities.outputString("equalled=" + equalled);
			Utilities.outputString("deleted=" + deleted);
		}
				
// transfer Chains into chains
    int chaincount = 0;
    Chain lch = listFoot.nextChain;
    while (lch != null) {
      chaincount++;
      lch = lch.nextChain;
    }
    Chain[ ] chains = new Chain[chaincount];
    lch = listFoot.nextChain;
    chaincount = 0;
    while (lch != null) {
      chains[chaincount++] = lch;
      lch = lch.nextChain;
    }
    if ((SEARCHBROKEN <= 0) | (chaincount <= 0)) { // not much more to do
      Utilities.sort(chains);
      return chains;
    }
		
		RetroTectorEngine.showProgress();
		
		
// collect broken chains into list
		Chain.brokenCounter = 0;
		
    if (commentLeader == null) {
			RetroTectorEngine.setInfoField("Collecting broken chains in " + strandchar + " strand");
		} else {
			RetroTectorEngine.setInfoField(commentLeader + " Collecting broken chains in " + strandchar + " strand");
		}
		for (int search=1; search<=SEARCHBROKEN; search++) {
			for (int c1=0; c1<(chains.length-1); c1++) {
				RetroTectorEngine.showProgress();
				Chain.setFirstPartChain(chains[c1], BROKENPENALTY); 
				for (int c2=c1+1; c2<chains.length; c2++) { // put the earlier in chain1
					if (Chain.setSecondPartChain(chains[c2])) {
						insertChain(strandchar, keepThreshold);
					}
				}
			}
			if (debugging) {
				Utilities.outputString(Chain.brokenCounter + " broken Chains created");
			}
	
			chaincount = 0;
			lch = listFoot.nextChain;
			while (lch != null) {
				chaincount++;
				lch = lch.nextChain;
			}
			chains = new Chain[chaincount];
			lch = listFoot.nextChain;
			chaincount = 0;
			while (lch != null) {
				chains[chaincount++] = lch;
				lch = lch.nextChain;
			}
			Utilities.sort(chains);
			RetroTectorEngine.showProgress();
		}

		RetroTectorEngine.setInfoField("");
		return chains;	
	} // end of collectChains(char, int, int, int, float, String)
	
	private Chain listFoot = null; // first Chain in list

	private int listSize = 0; // number of Chains in list
  
// adds theChain to	tempchainhits, if it fulfills certain criteria
// returns true if it was added
	private final boolean insertChain(char strandchar, float keepThreshold) throws RetroTectorException {

		insertstried++;
				
		if (listFoot == null) {
			listFoot = new Chain(); // dummy Chain
		}
		if (listFoot.nextChain == null) { // no serious Chain present
			listFoot.nextChain = new Chain(THEDNA, strandchar, BROKENPENALTY, PARENTCOLLECTOR);
			listSize = 1;
			return true;
		}
		
		Chain previous = listFoot; // pointer into list

		Chain compChain; // a Chain from list
		Chain tempChain; // new created Chain

    float score = Chain.peekScore(); // score of potential Chain
    if (score < keepThreshold) { // too low score to bother with
      return false;
    }
    int start = Chain.peekStart(); // start of potential Chain
    String rvgenus = Chain.peekRvGenus(); // rvgenus of potential Chain

    compChain = previous.nextChain; // first serious Chain in list

		while ((compChain != null) && (compChain.CHAINSTART < start)) { // is there a containing Chain in list?
			if ((compChain.CHAINSCORE >= score) && Chain.chainContainsStack(compChain)) { // yes, this one is useless
        contained++;
        return false;
      }
      previous = compChain; // try next Chain in list
      compChain = previous.nextChain;
    }
    
// only one Chain should be kept at each start position. while should probably be if
		while ((compChain != null) && (compChain.CHAINSTART == start)) { // is potential chain better than earlier chain with same start?
      if (compChain.CHAINSCORE >= score) { // no, it is useless 
        equalled++;
        return false;
      } else { // yes, delete old Chain
        previous.nextChain = compChain.nextChain;
        listSize--;
        deleted++;
        compChain = previous.nextChain;
      }
    }

// potential Chain has survived. Make and insert it. It is still on assembly stack too
    tempChain = new Chain(THEDNA, strandchar, BROKENPENALTY, PARENTCOLLECTOR);
    previous.nextChain = tempChain;
    previous = tempChain;
    previous.nextChain = compChain;
    listSize++;
    
// get rid of later Chains majored by this one
    int end = Chain.peekSubGeneHit().SGHITHOTSPOT;

    while ((compChain != null) && (compChain.CHAINSTART <= end)) {
      if ((compChain.CHAINSCORE < score) && Chain.stackContainsChain(compChain)) {
        previous.nextChain = compChain.nextChain;
        compChain = previous.nextChain;
        listSize--;
        deleted++;
      } else {
        previous = compChain;
        compChain = previous.nextChain;
      }
    }
    
		return true;
	} // end of insertChain(char, float)
	
/**
* Shows whole hierarchy. Mainly for debugging
*/
 	public void printTree() {
    for (int gg=0; gg<SUBGENES.length; gg++) {
			SubGene g = SUBGENES[gg];
			MotifGroup fam;
			Utilities.outputString(g.SUBGENENAME + ":" + g.nrOfHits() + " hits");
			for (int ff=0; (fam = g.getMotifGroup(ff)) != null; ff++) {
				Utilities.outputString("   " + fam.MOTIFGROUPNAME);
				MotifLitter lit;
				for (int ll=0; (lit = fam.getLitter(ll)) != null; ll++) {
					Utilities.outputString("      " + lit.LITTERRVGENUS + "  " + lit.nrOfHits() + " hits");
					Motif mot;
					for (int mm=0; (mot = lit.getMotif(mm)) != null; mm++) {
						Utilities.outputString("         " + mot.MOTIFID + ":" + mot.MOTIFORIGIN);
					}
				}
			}
		}
	} // end of printTree()
	
/**
* @param	deb	If true, various debugging aids are turned on.
*/
	public void setDebugging(boolean deb) {
		debugging = deb;
	} // end of setDebugging(boolean)
	
} // end of CoreChainCollector
