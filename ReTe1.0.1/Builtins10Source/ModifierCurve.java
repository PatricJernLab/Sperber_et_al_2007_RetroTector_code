/*
* Copyright (©) 2000-2006 Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 30/9 -06
* Beautified 30/9 -06
*/
package builtins;

import retrotector.*;

import java.util.*;
import java.awt.*;
import javax.swing.*;

/**
* Executor to draw curve of a Modifier.
*<PRE>
*     Parameters:
*
*   DNAFile
* The file to read DNA from.
* Default: Directory name + .txt
*
*   Strand
* Primary or Secondary
* Default: Primary
*</PRE>
* A popup menu allows selection of the Modifier to display.
*
*/
public class ModifierCurve extends Executor {

	private final String STOPCODON = "StopCodonModifier";
	private final String GLYCSITE = "GlycSiteModifier";
	private final String TRANSSITE = "TransSiteModifier";
	private final String SPLITOCT = "SplitOctamerModifier";
	private final String ORF = "ORFHexamerModifier";
	private final String NONORF = "NonORFHexamerModifier";
	private final String CPG = "CpGModifier";
	private final String GT = "GTModifier";
	private final String ORFMOD = "AllORFModifier";
	private final String POLYAMOD = "PolyASiteModifier";
	private final String RENDMOD = "REndModifier";
	private JComboBox theChoice = new JComboBox();
	private String chosen = null;
	
/**
* Standard Executor constructor.
*/
	public ModifierCurve() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put(STRANDKEY, PRIMARY);
		explanations.put(STRANDKEY, PRIMARY + " or " + SECONDARY);
		orderedkeys.push(STRANDKEY);
	} // end of constructor()
	
/**
* Reads parameters from parameter window.
* @param	script	Should be null.
* @return	True if successful.
*/
	public final void initialize(ParameterFileReader script) {
		runFlag = false;
		getDefaults();
    theChoice.addItem(GLYCSITE);
    theChoice.addItem(SPLITOCT);
    theChoice.addItem(STOPCODON);
    theChoice.addItem(TRANSSITE);
    theChoice.addItem(ORF);
    theChoice.addItem(NONORF);
    theChoice.addItem(CPG);
    theChoice.addItem(GT);
    theChoice.addItem(ORFMOD);
    theChoice.addItem(POLYAMOD);
    theChoice.addItem(RENDMOD);
		RetroTectorEngine.doParameterWindow(this, theChoice);
    chosen = (String) theChoice.getSelectedItem();
    interactive = true;
	} // end of initialize(ParameterFileReader)
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 09 30";
  } // end of version()
  
/**
* Execute as specified above.
*/
	public final String execute() throws RetroTectorException {
		DNA theDNA = getDNA(getString(DNAFILEKEY, ""), getString(STRANDKEY, PRIMARY).equals(PRIMARY));
		if (chosen.equals(GLYCSITE)) {
			display(theDNA.getGlycSiteModifier());
		} else if (chosen.equals(SPLITOCT)) {
			display(theDNA.getSplitOctamerModifier());
		} else if (chosen.equals(STOPCODON)) {
			display(theDNA.getStopCodonModifier());
		} else if (chosen.equals(TRANSSITE)) {
			display(theDNA.getTransSiteModifier());
		} else if (chosen.equals(ORF)) {
			display(theDNA.getORFHexamerModifier());
		} else if (chosen.equals(NONORF)) {
			display(theDNA.getNonORFHexamerModifier());
		} else if (chosen.equals(CPG)) {
			display(theDNA.getCpGModifier());
		} else if (chosen.equals(GT)) {
			display(theDNA.getGTModifier());
		} else if (chosen.equals(ORFMOD)) {
 			display(new AllORFModifier(theDNA));
		} else if (chosen.equals(POLYAMOD)) {
 			display(new PolyASiteModifier(theDNA));
		} else if (chosen.equals(RENDMOD)) {
 			display(new REndModifier(theDNA));
    }
		return "";
	} // end of execute()

/**
* Shows a curve of modification in a window.
*/
	private final void display(Modifier mod) {
		ModifierJPanel theCanvas = new ModifierJPanel(mod);
		GraphicsJFrame theWindow = new GraphicsJFrame(Utilities.className(mod), theCanvas);
	} // end of display(Modifier)
	
	
} // end of ModifierCurve
