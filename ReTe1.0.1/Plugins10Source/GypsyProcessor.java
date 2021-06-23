/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 2/11 -06
* Beautified 2/11 -06
*/
package plugins;


import retrotector.*;
import builtins.*;
import java.util.*;
import java.io.*;

/**
* AbstractChainProcessor which does first-step Chain collection using Database/Gypsy.
*<PRE>
*     Parameters:
*
*   ProcessorPriority
* Priority relative to other ChainProcessors.
* Default: 1.
*
*   GThreshold
* The lowest gypsy cosine to cause activation.
* Default: .5.
*
*   Debugging
* If this is Yes, bebugging info is output to standard output.
* Default: No
*</PRE>
*/
public class GypsyProcessor extends AbstractChainProcessor {
	
/**
* = "GThreshold".
*/
	public static final String GTHRESHOLDKEY = "GThreshold";

	private float gThreshold;
	private boolean debugging = true;

/**
* Constructor.
*/
	public GypsyProcessor() {
		parameters = new Hashtable();
		getDefaults();
		processorPriority = getFloat(PROCESSORPRIORITYKEY, 1f);
		debugging = getString(Executor.DEBUGGINGKEY, Executor.NO).equals(Executor.YES);
		gThreshold = getFloat(GTHRESHOLDKEY, 0.5f);
	} // end of constructor()
	
/**
* Performs final operations on all in processedChains.
* Dummy.
*/
	public void postProcess() throws RetroTectorException {
	} // end of postProcess()
	
/**
* As required by Scorable.
*	@return	processorPriority.
*/
	public float fetchScore() {
		return processorPriority;
	} // end of fetchScore()
	
/**
*	@param	ch	A Chain.
* @param	retroVID					Caller.
*	@return	True if ch contains LTR5 hit but not PBS hit.
*/
	public boolean isEligible(Chain ch, RetroVID retroVID) {
		return (ch.isSelected() && (ch.getCosine('G') >= gThreshold));
	} // end of isEligible(Chain, RetroVID)

	private DNA dna;
	
/**
* Remakes inChain including characterization Motif hits.
* @param	inChain						Chain to process.
* @param	info							ProcessorInfo with parameters.
* @param	retroVID					Caller.
* @return	The (possibly) processed Chain, which is also pushed onto processedChains.
*/
	public Chain processChain(Chain inChain, ProcessorInfo info, RetroVID retroVID) throws RetroTectorException {

		ChainCollector collector = inChain.COLLECTOR;
		dna = inChain.SOURCEDNA;
		
    ChainCollector coll = new ChainCollector(dna, info.LENGTHBONUS, info.MAXSUBGENESKIP + 2, info.BROKENPENALTY, info.BROKENPASSES, Database.getDatabase("Gypsy"));
    coll.setDebugging(info.DEBUGGING);
    RetroTectorEngine.setInfoField("GypsyProcessor Refreshing in strand " + inChain.STRANDCHAR);
    coll.refreshCollector(new Motif.RefreshInfo(dna, info.CFACTOR, info.SDFACTOR - 1.0f, info.LTRTABLE));
    RetroTectorEngine.setInfoField("GypsyProcessor Scoring in strand " + inChain.STRANDCHAR);
    int lolim = dna.forceInside(inChain.CHAINSTART - 2000);
    int hilim = dna.forceInside(inChain.CHAINEND + 2000);
    coll.scoreSubGenes();
    RetroTectorEngine.setInfoField("GypsyProcessor Collecting SubGene hits in strand " + inChain.STRANDCHAR);
    coll.collectSubGeneHits(info.FRAMEFACTOR, lolim, hilim);
    RetroTectorEngine.setInfoField("Collecting chains in strand " + inChain.STRANDCHAR);
		Chain[ ] chains = coll.collectChains(inChain.STRANDCHAR, 0, dna.LENGTH - 1, info.SUBGENEHITSLIMIT, info.KEEPTHRESHOLD, null);
		if (chains == null) {
			return inChain;
		}
		if (chains.length == 0) {
			return inChain;
		}
		if (chains[0].CHAINSCORE <= inChain.CHAINSCORE) {
			return inChain;
		}
		Chain theChain = chains[0];
		theChain.chainNumber = inChain.chainNumber;
		theChain.select(inChain.isSelected());
		theChain.appendToHistory("Processed by GypsyProcessor " + (new Date()).toString());
		processedChains.push(theChain);
		return theChain;
	} // end of processChain(Chain, ProcessorInfo, RetroVID)
	
}
