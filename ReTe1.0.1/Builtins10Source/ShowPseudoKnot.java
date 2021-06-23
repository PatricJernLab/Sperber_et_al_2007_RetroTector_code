/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 5/10 -06
* Beautified 5/10 -06
*/
package builtins;

import retrotector.*;
import java.util.*;

/**
* Executor to show structure of potential pseudoknots in DNA pasted into a DNAWindow.
* 
*	There are no parameters.
*/
public class ShowPseudoKnot extends Executor {

/**
* Constructor. No parameters to specify.
*/
	public ShowPseudoKnot() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
	} // end of constructor()
	

/**
* Dummy. Nothing to to.
*/
	public void initialize(ParameterFileReader script) {
		runFlag = true;
	} // end of initialize(ParameterFileReader)

	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 10 05";
  } // end of version()

/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		DNA	targetDNA = RetroTectorEngine.doDNAWindow();
		PseudoKnotMotif pseudoknotMotif = (PseudoKnotMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.PSEUDOKNOTMOTIFKEY);
		pseudoknotMotif.refresh(new Motif.RefreshInfo(targetDNA, 0, 0, null));
		MotifHit mh;
		for (int p=0; p<targetDNA.LENGTH; p++) {
			mh = pseudoknotMotif.getMotifHitAt(p);
			if (mh != null) {
				PseudoKnotMotif.KnotTry tryy = pseudoknotMotif.latestTry;
				Utilities.outputString("\n    ShowPseudoKnot output for");
				Utilities.outputString(targetDNA.subString(tryy.STEM1ASTART, tryy.STEM2BEND + 1, true));
				Utilities.outputString("STEM1ASTART " + tryy.STEM1ASTART);
				Utilities.outputString("STEM1AEND " + tryy.STEM1AEND);
				Utilities.outputString("STEM2ASTART " + tryy.STEM2ASTART);
				Utilities.outputString("STEM2AEND " + tryy.STEM2AEND);
				Utilities.outputString("STEM1BSTART " + tryy.STEM1BSTART);
				Utilities.outputString("STEM1BEND " + tryy.STEM1BEND);
				Utilities.outputString("STEM2BSTART " + tryy.STEM2BSTART);
				Utilities.outputString("STEM2BEND " + tryy.STEM2BEND);
				
				StringBuffer line1 = new StringBuffer();
				StringBuffer line2 = new StringBuffer();
				StringBuffer line3 = new StringBuffer();
				StringBuffer line1b = new StringBuffer();
				StringBuffer line2b = new StringBuffer();
				StringBuffer line3b = new StringBuffer();
				
				line1b.append("  S1");
				for (int i1=tryy.STEM1ASTART; i1<=tryy.STEM1AEND; i1++) {
					line1.append(targetDNA.getBase(i1));
					while (line1b.length() < line1.length()) {
						line1b.append(' ');
					}
				}
				line1.append(' ');
				line1b.append(" L1  S2");
				int ilength = 2;
				for (int i2=tryy.STEM1AEND+1; i2<tryy.STEM2ASTART; i2++) {
					line1.append(targetDNA.getBase(i2));
					ilength++;
				}
				line1.append(' ');
				for (int i3=tryy.STEM2ASTART; i3<=tryy.STEM2AEND; i3++) {
					line1.append(targetDNA.getBase(i3));
				}
				line1.append(' ');		
				for (int i4=tryy.STEM2AEND+1; i4<tryy.STEM1BSTART; i4++) {
					line1.append(targetDNA.getBase(i4));
				}
				for (int i5=tryy.STEM1BEND; i5>=tryy.STEM1BSTART; i5--) {
					line2.append(targetDNA.getBase(i5));
				}
				for (int i6=0; i6<ilength; i6++) {
					line2.append(' ');
				}
				for (int i7=tryy.STEM2BEND; i7>=tryy.STEM2BSTART; i7--) {
					line2.append(targetDNA.getBase(i7));
				}
				
				line3.append("  ");
				for (int i8=tryy.STEM1BEND+1; i8<tryy.STEM2BSTART; i8++) {
					line3.append(targetDNA.getBase(i8));
				}
				line3b.append("    L2");
				Utilities.outputString(line1.toString());
				Utilities.outputString(line1b.toString());
				Utilities.outputString(line2.toString());
				Utilities.outputString(line2b.toString());
				Utilities.outputString(line3.toString());
				Utilities.outputString(line3b.toString());
				return "";
			}
		}
		return "Not found";
	} // end of execute()

} // end of ShowPseudoKnot
