/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 15/8 -06
* Beautified 15/8 -06
*/
package retrotector;

import builtins.*;
import java.util.*;
import java.io.*;

/**
* Does standard postprocessing of Chains for RetroVID.
* Also superclass of optional postprocessing classes in 'plugins' directory.
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
public class ChainProcessor extends AbstractChainProcessor implements Scorable {

/**
* Remakes inChain including characterization Motif hits.
* Should be overridden by subclasses.
* @param	inChain						Chain to process.
* @param	info							ProcessorInfo with parameters.
* @param	retroVID					Caller.
* @return	The (possibly) processed Chain, which is also pushed onto processedChains.
*/
	public Chain processChain(Chain inChain, ProcessorInfo info, RetroVID retroVID) throws RetroTectorException {

		ChainCollector collector = inChain.COLLECTOR;
// reach over an extra 1000 bases in each direction
    int lolim = inChain.SOURCEDNA.forceInside(inChain.CHAINSTART - 1000);
    int hilim = inChain.SOURCEDNA.forceInside(inChain.CHAINEND + 1000);

    collector.setDebugging(info.DEBUGGING);
    collector.scoreCharac(lolim, hilim);
    
		RetroTectorEngine.showProgress();
    collector.collectSubGeneHits(info.FRAMEFACTOR, lolim, hilim);
		RetroTectorEngine.showProgress();
    
    if (info.DEBUGGING) {
      Utilities.outputString("  Improving Chain" + inChain.STRANDCHAR + inChain.chainNumber);
      collector.printTree();
    }
    
		RetroTectorEngine.showProgress();
// get chains wiithin range
		Chain [ ] chains = collector.collectChains(inChain.STRANDCHAR, lolim, hilim, info.PROCESSHITSLIMIT, info.KEEPTHRESHOLD, "Improving chain " + inChain.STRANDCHAR + inChain.chainNumber + ": ");
    if ((chains == null) || (chains.length == 0) || (chains[0].CHAINSCORE <= inChain.CHAINSCORE)) { // no improvement
			processedChains.push(inChain);
      return inChain;
    }
    
// pick the best new Chain
    Chain theChain = chains[0];
    theChain.chainNumber = inChain.chainNumber;
    
    SubGeneHit l5 = theChain.get5LTR();
    SubGeneHit l3 = theChain.get3LTR();
// if there is one LTR, try to include its companion
    if ((l5 != null) && (l3 == null) && (l5.companion() != null)) {
      Chain.setFirstPartChain(theChain, 1); // no penalty here
      if (theChain.setSecondPartChain(new Chain(new SubGeneHit(l5.companion()), theChain.STRANDCHAR, collector))) {
        theChain = new Chain(theChain.SOURCEDNA, theChain.STRANDCHAR, info.BROKENPENALTY, collector);
      }
    } else if ((l5 == null) && (l3 != null) && (l3.companion() != null)) {
      Chain.setFirstPartChain(new Chain(new SubGeneHit(l3.companion()), theChain.STRANDCHAR, collector), 1);
      if (Chain.setSecondPartChain(theChain)) {
        theChain = new Chain(theChain.SOURCEDNA, theChain.STRANDCHAR, info.BROKENPENALTY, collector);
      }
    }

// the following does nothing at present
		l5 = theChain.get5LTR();
		l3 = theChain.get3LTR();
		MotifHit mh;
		boolean overlap = false;
		int pbsBar = theChain.SOURCEDNA.LENGTH;
		mh = theChain.getPBShit();
		if (mh != null) {
			pbsBar = Math.round(mh.MOTIFHITFIRST + 0.1f * (mh.MOTIFHITLAST - mh.MOTIFHITFIRST));
		}
		if ((l5 != null) && (pbsBar <= l5.LASTHITEND)) {
			overlap = true;
		}
		int pptBar = -1;
		mh = theChain.getPPThit();
		if (mh != null) {
			pptBar = Math.round(mh.MOTIFHITLAST - 0.1f * (mh.MOTIFHITLAST - mh.MOTIFHITFIRST));
		}
		if ((l3 != null) && (pptBar >= l3.FIRSTHITSTART)) {
			overlap = true;
		}
		
    theChain.chainNumber = inChain.chainNumber;
		theChain.select(inChain.isSelected());
		theChain.appendToHistory("Processed by ChainProcessor " + (new Date()).toString());
		processedChains.push(theChain);
    return theChain;

	} // end of processChain

}
