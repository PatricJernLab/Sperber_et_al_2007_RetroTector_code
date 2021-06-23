/*
* Copyright (©) 2000-2005 Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 11/5 -05
* Beautified 11/5 -05
*/
package builtins;

import retrotector.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
* Executor showing details of an LTR, as described in an LTRID output
* or SelectedChains file.
*<PRE>
*     Parameters:
*
*   InputFile
* The name of the file with the LTR descriptions.
* Default: RetroVID_001Script.txt
*</PRE>
*/
public class LTRview extends Executor implements ActionListener {

	private GraphicsJFrame theWindow = null;
	private DNA primDNA = null;
	private DNA secDNA = null;
	private float sizefactor = 1.5f; // to adjust size with
	private JMenuBar mb;

/**
* Standard Executor constructor.
*/
	public LTRview() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(INPUTFILEKEY, "RetroVID_001Script.txt");
		explanations.put(INPUTFILEKEY, "File with LTRs");
		orderedkeys.push(INPUTFILEKEY);
	}
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2005 05 11";
  } //end of version
  
/**
* Executes, using parameters above.
*/
	public String execute() throws RetroTectorException {
	
		String s1 = getString(INPUTFILEKEY, "");
    if (s1.length() > 0) {
      File f = Utilities.getReadFile(RetroTectorEngine.currentDirectory(), s1);
      ParameterFileReader reader = new ParameterFileReader(f, parameters);
      reader.readParameters();
      reader.close();
    }

		primDNA = getDNA(getString(DNAFILEKEY, ""), true);
		secDNA = getDNA(getString(DNAFILEKEY, ""), false);
		mb = new JMenuBar();
		JMenuItem mim;
		JMenu m = new JMenu("LTR");
		String[ ] ss = Utilities.keyList(parameters);
		for (int i=0; i<ss.length; i++) {
			if (ss[i].startsWith(SINGLELTRSUFFIX) | ss[i].endsWith("LTR")) {
				mim = new JMenuItem(ss[i]);
				mim.addActionListener(this);
				m.add(mim);
			}
		}
		mb.add(m);
		
		LTRviewPanel pan = new LTRviewPanel(candOf(m.getItem(0).getText()), theWindow, sizefactor);
		theWindow = new GraphicsJFrame(m.getItem(0).getText(), pan, false);
		theWindow.setJMenuBar(mb);
		theWindow.setLocation(100, 100);
		theWindow.showUp(new Dimension(600, pan.WHEIGHT + 20));
		return "";
		
	} // end of execute
	
	private LTRCandidate candOf(String key) throws RetroTectorException {
		if (key.startsWith(SINGLELTRSUFFIX)) {
			if (key.charAt(SINGLELTRSUFFIX.length()) == 'P') {	
				return new LTRCandidate(primDNA, getStringArray(key));
			} else if (key.charAt(SINGLELTRSUFFIX.length()) == 'S') {
				return new LTRCandidate(secDNA, getStringArray(key));
			} else {
				return null;
			}
		} else if (key.endsWith("LTR")) {
				if (key.startsWith("P")) {
			 return new LTRCandidate(primDNA, getStringArray(key));
			} else if (key.startsWith("S")) {
				return new LTRCandidate(secDNA, getStringArray(key));
			} else {
				return null;
			}
		} else {
			return null;
		}
	} // end of candOf
	
/**
* For selection of the LTR clicked in menu.
*/
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() instanceof JMenuItem) {
			JMenuItem mi = (JMenuItem) ae.getSource();
			String s = mi.getText();
			try {
				theWindow.dispose();
				LTRviewPanel pan = new LTRviewPanel(candOf(s), theWindow, sizefactor);
				theWindow = new GraphicsJFrame(s, pan, false);
				theWindow.setJMenuBar(mb);
				theWindow.setLocation(100, 100);
				theWindow.showUp(new Dimension(600, pan.WHEIGHT + 20));
			} catch (RetroTectorException r) {
				RetroTectorEngine.displayError(r, RetroTectorEngine.ERRORLEVEL);
			}
		}
	} // end of actionPerformed
	
} // end of LTRview
