/*
* Copyright (©) 2000-2006, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Göran Sperber
* @version 13/11 07
* Beautified 13/11 07
*/
package plugins;

import retrotector.*;
import builtins.*;
import java.util.*;
import java.io.*;

/**
* AbstractChainProcessor which creates an EnvTracer script if appropriate.
*<PRE>
*     Parameters:
*
*   ProcessorPriority
* Priority relative to other ChainProcessors.
* Default: -3.
*
*</PRE>
*/
public class EnvProcessor extends AbstractChainProcessor {
	
/**
* Constructor.
*/
	public EnvProcessor() {
		parameters = new Hashtable();
		getDefaults();
		processorPriority = getFloat(PROCESSORPRIORITYKEY, -3.0f);
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
*	@return	True if ch contains LTR3 hit and IN hit but no SU or TM hit.
*/
	public boolean isEligible(Chain ch, RetroVID retroVID) {
		if (!ch.isSelected()) {
			return false;
		}
		if (ch.get3LTR() == null) {
			return false;
		}
		if (ch.CHAINSCORE < retroVID.getFinalSelectionThreshold()) {
			return false;
		}
		int subgcount = ch.COLLECTOR.COLLECTORDATABASE.subGeneNames.length;
		if (ch.getSubGeneHit(subgcount - 5) == null) {
			return false;
		}
		return true;
	} // end of isEligible(Chain ch, RetroVID)

/**
* Generates EnvTracer script.
* @param	inChain						Chain to process.
* @param	info							ProcessorInfo with parameters.
* @param	retroVID					Caller.
* @return	inChain.
*/
	public Chain processChain(Chain inChain,  ProcessorInfo info, RetroVID retroVID) throws RetroTectorException {
	
		File f = FileNamer.createEnvTracerScript(inChain);
		boolean doubtful = true;
		float ss = 0;
		SubGeneHit sgh;
		MotifHit mh;
		int subgcount = inChain.COLLECTOR.COLLECTORDATABASE.subGeneNames.length;
		sgh = inChain.getSubGeneHit(subgcount - 4);
		if (sgh != null) {
			for (int i=0; (mh=sgh.getMotifHit(i)) != null; i++) {
				ss += mh.MOTIFHITSCORE * mh.MOTIFHITSCORE;
			}
		}
		sgh = inChain.getSubGeneHit(subgcount - 3);
		if (sgh != null) {
			for (int i=0; (mh=sgh.getMotifHit(i)) != null; i++) {
				ss += mh.MOTIFHITSCORE * mh.MOTIFHITSCORE;
			}
		}
		if (ss < 600) {
			doubtful = false;
		}
		if (doubtful) {
			String name = f.getAbsolutePath();
			name = name.substring(0, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length()) + Utilities.SWEPTSCRIPTFILESUFFIX;
			f = new File(name);
		}

		Range er = inChain.findGeneEndRange(inChain.COLLECTOR.COLLECTORDATABASE.getGene("Pol"));
		if (er != null) {
			ExecutorScriptWriter writer = new ExecutorScriptWriter(f, "EnvTracer");
			DNA dna = inChain.SOURCEDNA;
			writer.writeSingleParameter(Executor.DNAFILEKEY, dna.NAME, false);
			writer.writeSingleParameter("Chain", "" + inChain.STRANDCHAR + inChain.chainNumber, false);
			int i1 = inChain.getSubGeneHit(inChain.COLLECTOR.COLLECTORDATABASE.subGeneNames.length - 5).lastMotifHit().MOTIFHITLAST;
			int i2 = dna.externalize(i1);
			writer.writeSingleParameter("StartEnv", "" + i2, false);
			try {
				writer.writeSingleParameter("FirstEndEnv", "" + dna.externalize(inChain.findGeneEndRange(inChain.COLLECTOR.COLLECTORDATABASE.getGene("Pol")).RANGEMAX), false);
			} catch (NullPointerException ne) {
				System.out.println(inChain.COLLECTOR.COLLECTORDATABASE.getGene("Pol"));
				throw ne;
			}
			int i3 = dna.forceInside(inChain.get3LTR().firstMotifHit().MOTIFHITFIRST + 150);
			int i4 = dna.externalize(i3);
			writer.writeSingleParameter("EndEnv", "" + i4, false);
			writer.close();
		}
		return inChain;
	} // end of processChain(Chain, ProcessorInfo, RetroVID)
	
}
