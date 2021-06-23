/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 20/9 -06
* Beautified 20/9 -06
*/

package retrotector;

import retrotectorcore.*;

/**
* Interface to CoreChainCollector.
*/
public class ChainCollector {

	private CoreChainCollector coreChainCollector;
	
/**
* The relevant database.
*/
  public final Database COLLECTORDATABASE;

/**
* Constructor.
* @param	theDNA					DNA to use.
* @param	lengthBonus			Bonus factor for each new SubGene in Chain.
* @param	maxSubGeneSkip	Largest number of SubGenes skipped in forming Chains.
* @param	brokenPenalty		Score penalty for broken chain.
* @param	searchBroken		Number of passes searching for broken Chains.
* @param	database				See COLLECTORDATABASE.
*/
 public ChainCollector(DNA theDNA, float lengthBonus, int maxSubGeneSkip, float brokenPenalty, int searchBroken, Database database) throws RetroTectorException {

		try {
			coreChainCollector = new CoreChainCollector(theDNA, lengthBonus, maxSubGeneSkip, brokenPenalty, searchBroken, database, this);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreChainCollector, "ChainCollector", rte);
		}
		COLLECTORDATABASE = coreChainCollector.COLLECTORDATABASE;
		
	} // end of constructor(DNA, float, int, float, int, Database)
  
/**
* Refreshes all SubGenes and their parts.
* @param	inf	RefreshInfo	to send down the line.
*/
  public final void refreshCollector(Motif.RefreshInfo inf) throws RetroTectorException {

		try {
			coreChainCollector.refreshCollector(inf);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreChainCollector, "ChainCollector", rte);
		}

  } // end of refreshCollector(Motif.RefreshInfo)
    
/**
* Scores all detection Motifs over whole THEDNA.
*/
  public final void scoreSubGenes() throws RetroTectorException {

		try {
			coreChainCollector.scoreSubGenes();
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreChainCollector, "ChainCollector", rte);
		}

  } // end of scoreSubGenes()

/**
* Scores all characterization Motifs within a range.
* @param	scoreFirst	Start (internal) position of range.
* @param	scoreLast		End (internal) position of range.
*/
  public final void scoreCharac(int scoreFirst, int scoreLast) throws RetroTectorException {

		try {
			coreChainCollector.scoreCharac(scoreFirst, scoreLast);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreChainCollector, "ChainCollector", rte);
		}

  } // end of scoreCharac(int, int)
    
/**
* @param	index	An index in SUBGENES;
* @return The SubGene in question.
*/
	public final SubGene getSubGene(int index) {
		return coreChainCollector.getSubGene(index);
	} // end of getSubGene(int)
	
/**
* Collects SubGene hits for all SubGenes within a range.
* @param	frameFactor	Factor to multiply score by if all MotifHits in same reading frame.
* @param	lo					First internal position to use.
* @param	hi					Last internal position to use.
*/
  public final void collectSubGeneHits(float frameFactor, int lo, int hi) throws RetroTectorException {

		try {
			coreChainCollector.collectSubGeneHits(frameFactor, lo, hi);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreChainCollector, "ChainCollector", rte);
		}

  } // end of collectSubGeneHits(float, int, int)
  
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
 	public final Chain[ ] collectChains(char strandchar, int chainRangeMin, int chainRangeMax, int subGeneHitsLimit, float keepThreshold, String commentLeader) throws RetroTectorException {

		try {
			return coreChainCollector.collectChains(strandchar, chainRangeMin, chainRangeMax, subGeneHitsLimit, keepThreshold, commentLeader);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreChainCollector, "ChainCollector", rte);
		}
		return null;
		
	} // end of collectChains(char, int, int, int, float, String)
	
	
/**
 * Shows whole hierarchy. Mainly for debugging
 */
 	public void printTree() {
		coreChainCollector.printTree();
	} // end of printTree()
	
/**
* @param	deb	If true, various debugging aids are turned on.
*/
	public void setDebugging(boolean deb) {
		coreChainCollector.setDebugging(deb);
	} // end of setDebugging()
	
}