/*
* Copyright (©) 2000-2006, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Göran Sperber
* @version 18/9 -06
* Beautified 18/9 -06
*/
package retrotector;

import builtins.*;
import java.util.*;
import java.io.*;

/**
* Superclass of optional postprocessing classes.
*<PRE>
*     Parameters:
*
*   ProcessorPriority
* Priority relative to other ChainProcessors.
* Default: 0.
*
*   MaxOutputChains
* Maximum number of Chains for postProcess() to output.
* Default: 0.
*</PRE>
*/
public abstract class AbstractChainProcessor extends ParameterUser implements Scorable {

/**
* Various information that may be needed by a ChainProcessor.
*/
	public static class ProcessorInfo {
	
/**
* Bonus factor if all MotifHits in a SubGeneHit are in same frame.
*/
		public final float FRAMEFACTOR;
	
/**
* Bonus factor for each MotifHit in Chain.
*/
		public final float LENGTHBONUS;
	
/**
* Largest number of SubGenes skipped in forming Chains.
*/
		public final int MAXSUBGENESKIP;
	
/**
* To multiply score by if broken Chain.
*/
		public final float BROKENPENALTY;
	
/**
* Maximum number of SubGeneHits tried in one SubGene when making a Chain.
*/
		public final int SUBGENEHITSLIMIT;
	
/**
* Maximum number of SubGeneHits tried in one SubGene when improving a Chain.
*/
		public final int PROCESSHITSLIMIT;
	
/**
* Chains with lower score than this are thrown out early.
*/
		public final float KEEPTHRESHOLD;
	
/**
* Auxiliary information is output to screen if true.
*/
		public final boolean DEBUGGING;

/**
* Number of passes searching for broken Chains.
*/
		public final int BROKENPASSES;
		
/**
* Bonus factor for conserved positions.
*/
		public final float CFACTOR;
	
/**
* Coefficient for standard deviation when setting threshold.
*/
		public final float SDFACTOR;
			
/**
* Table with LTRPair descriptions, for LTRMotif.
*/
		public final Hashtable LTRTABLE;
	
/**
* The calling RetroVID.
*/
		public final RetroVID CALLER;
		
/**
* Constructor.
* @param	frameFactor				Bonus factor if all MotifHits in a SubGeneHit are in same frame.
* @param	lengthBonus				Bonus factor for each MotifHit in Chain.
* @param	maxSubGeneSkip		Largest number of SubGenes skipped in forming Chains.
* @param	brokenPenalty			To multiply score by if broken Chain.
* @param	subGeneHitsLimit	Maximum number of SubGeneHits tried in one SubGene when making a Chain.
* @param	processHitsLimit	Maximum number of SubGeneHits tried in one SubGene when improving a Chain.
* @param	keepThreshold			Chains with lower score than this are thrown out early.
* @param	debugging					Auxiliary information is output to screen if true.
* @param	brokenPasses			Number of passes searching for broken Chains.
* @param	cFactor						Bonus factor for conserved positions.
* @param	sdFactor					Coefficient for standard deviation when setting threshold.
* @param	ltrTable					Table with LTRPair descriptions, for LTRMotif.
* @param	caller						The calling RetroVID.
*/
		public ProcessorInfo(float frameFactor, float lengthBonus, int maxSubGeneSkip, float brokenPenalty, int subGeneHitsLimit, int processHitsLimit, float keepThreshold, boolean debugging, int brokenPasses, float cFactor, float sdFactor, Hashtable ltrTable, RetroVID caller) {
			FRAMEFACTOR = frameFactor;
			LENGTHBONUS = lengthBonus;
			MAXSUBGENESKIP = maxSubGeneSkip;
			BROKENPENALTY = brokenPenalty;
			SUBGENEHITSLIMIT = subGeneHitsLimit;
			PROCESSHITSLIMIT = processHitsLimit;
			KEEPTHRESHOLD = keepThreshold;
			DEBUGGING = debugging;
			BROKENPASSES = brokenPasses;
			CFACTOR = cFactor;
			SDFACTOR = sdFactor;
			LTRTABLE = ltrTable;
			CALLER = caller;
		} // end of ProcessorInfo.constructor(float, float, int, float, int, int, float, boolean, int, float, float, Hashtable, RetroVID)
	
	} // end of ProcessorInfo


/**
* Key for ChainProcessor priority = "ProcessorPriority".
*/
	public static final String PROCESSORPRIORITYKEY = "ProcessorPriority";

/**
* Key for maximal number of Chains to output = "MaxOutputChains".
*/
	public static final String MAXOUTPUTKEY = "MaxOutputChains";

/**
* All available ChainProcessors, sorted by processorPriority.
*/
	public static AbstractChainProcessor[ ] processors;

/**
* An instance of this class.
*/
	public static AbstractChainProcessor basalProcessor;

/**
* Collects basalProcessor and processors.
*/
	public final static void collectProcessors() throws RetroTectorException {
		Stack imp = new Stack();
		Class c;
		for (int i=0; i<PluginManager.BUILTANDPLUGINS.length; i++) {
			c = PluginManager.BUILTANDPLUGINS[i];
      if (Utilities.subClassOf(c, "retrotector.AbstractChainProcessor")) {
				try {
					imp.push(c.newInstance());
				} catch (InstantiationException ie) {
				} catch (IllegalAccessException iae) {
					throw new RetroTectorException("ChainProcessor", "Could not instantiate", c.getName());
				}
			}
		}
		basalProcessor = new ChainProcessor();
		imp.push(basalProcessor);
		processors = new AbstractChainProcessor[imp.size()];
		imp.copyInto(processors);
		Utilities.sort(processors); // sort by priority
	} // end of collectProcessors()

/**
* Does reset() on all in processors.
*/
	public final static void resetProcessors() {
		for (int i=0; i<processors.length; i++) {
			processors[i].reset();
		}
	} // end of resetProcessors()
	
	
/**
* ChainProcessors are applied in descending order of this. Default = 0.
*/
	protected float processorPriority;

/**
* Maximal number of Chains handled by postProcess().
*/
	protected int nrToOutput;
	
/**
* Stack of all Chains processed by processChain.
*/
	protected Stack processedChains = new Stack();
	
/**
* Constructor.
*/
	public AbstractChainProcessor() {
		parameters = new Hashtable();
		getDefaults();
		processorPriority = getFloat(PROCESSORPRIORITYKEY, 0.0f);
		nrToOutput = getInt(MAXOUTPUTKEY, 0);
	} // end of constructor()
	
/**
* Empties processedChains.
*/
	public void reset() {
		processedChains = new Stack();
	} // end of reset()
	
/**
* @return	An array of contents of processedChains.
*/
	public Chain[ ] getProcessedChains() {
		Chain[ ] cs = new Chain[processedChains.size()];
		processedChains.copyInto(cs);
		return cs;
	} // end of getProcessedChains()
	
/**
* Performs final operations on all in processedChains. Is typically overridden by subclasses,
* This default version outputs up to nrToOutput Chains to a suitably named file.
*/
	public void postProcess() throws RetroTectorException {
		if (nrToOutput <= 0) {
			return;
		}
		
		if (processedChains.size() == 0) {
			return;
		}
		
    Chain cha = (Chain) processedChains.elementAt(0);
		File allFile = Utilities.uniqueFile(RetroTectorEngine.currentDirectory(), Utilities.className(this), "Chains.txt");
    ExecutorScriptWriter outc1 = new ExecutorScriptWriter(allFile, "Chainview");
    outc1.writeSingleParameter(Executor.DNAFILEKEY, cha.SOURCEDNA.NAME, false);
    outc1.writeSingleParameter(Executor.SELECTEDKEY, Executor.NO, false);
    outc1.writeSingleParameter(Executor.DATABASEKEY, cha.COLLECTOR.COLLECTORDATABASE.DATABASENAME, false);
    int counter = nrToOutput;
    for (int c=0; c<processedChains.size(); c++) {
    	if (counter > 0) {
				cha = (Chain) processedChains.elementAt(c);
    		outc1.startMultiParameter("Chain" + cha.STRANDCHAR + cha.chainNumber, false);
    		ChainGraphInfo inf = new ChainGraphInfo(cha);
    		outc1.appendToMultiParameter(inf.toStrings(cha.SOURCEDNA), false);
    		outc1.finishMultiParameter(false);
    		counter--;
    	}
    }
		outc1.writeComment("Created by " + className() + " with parameters");
		outc1.listSingleParameters(parameters);
    outc1.close();
		RetroTectorEngine.showProgress();
	} // end of postProcess()
	
/**
* As required by Scorable.
*	@return	processorPriority.
*/
	public float fetchScore() {
		return processorPriority;
	} // end of fetchScore
	
/**
* Is typically overridden by subclasses.
*	@param	ch	A Chain.
* @param	retroVID					Caller.
*	@return	True if ch should be processed.
*/
	public boolean isEligible(Chain ch, RetroVID retroVID) {
		return ch.isSelected();
	} // end of isEligible

/**
* Remakes inChain including characterization Motif hits.
* Should be overridden by subclasses.
* @param	inChain						Chain to process.
* @param	info							ProcessorInfo with parameters.
* @param	retroVID					Caller.
* @return	The (possibly) processed Chain, which is also pushed onto processedChains.
*/
	abstract public Chain processChain(Chain inChain, ProcessorInfo info, RetroVID retroVID) throws RetroTectorException;
	
} // end of AbstractChainProcessor
