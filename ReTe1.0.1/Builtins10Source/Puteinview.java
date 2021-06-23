/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 2/10 -06
* Beautified 2/10 -06
*/
package builtins;

import retrotector.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
* Makes a graphic display of a Putein file from ORFID.
*<PRE>
*     Parameters:
*   PuteinFile
* The name of the Putein file to display.
*
*   FontSize
* The size of the font to use.
* Default: 14
*</PRE>
*/
public class Puteinview extends Executor {

/**
* Canvas to show contents of Putein file.
*/
	class PuteinCanvas extends JCanvas {

		private final int FONTSIZE;
		private final String[ ] STRINGS; // Strings from "Putein" parameter
		private final Font THEFONT;
		private final int CWIDTH; // width of this
		private final int LHEIGHT; // vertical space per line
		
/**
* Constructor.
*	@param	s				Strings from "Putein" parameter.
*	@param	fontsi	Font size to use.
*/
		PuteinCanvas(String[ ] s, int fontsi) {
			STRINGS = s;
			FONTSIZE = fontsi;
			THEFONT = new Font("Courier", Font.PLAIN, FONTSIZE);
			FontMetrics fm = getFontMetrics(THEFONT);
			CWIDTH = fm.stringWidth(STRINGS[1]) + 50;
			LHEIGHT = fm.getHeight() + 10;
			setPreferredSize(new Dimension(CWIDTH, LHEIGHT * (STRINGS.length + 1)));
			setMinimumSize(new Dimension(CWIDTH, LHEIGHT * 3));
		} // end of PuteinCanvas.constructor(String[ ], int)
		
/**
* As required by JComponent.
*/
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setFont(THEFONT);
      for (int i=0; i<STRINGS.length; i++) {
        g.drawString(STRINGS[i], 1, LHEIGHT * (i + 1));
      }
		} // end of PuteinCanvas.paintComponent(Graphics)
		
	} // end of PuteinCanvas


	final private CloseableJFrame THEFRAME = new CloseableJFrame(""); // main display window

/**
* Standatd Executor constructor.
*/
	public Puteinview() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(PUTEINFILEKEY, "");
		explanations.put(PUTEINFILEKEY, "The Putein file to use");
		orderedkeys.push(PUTEINFILEKEY);
		parameters.put(FONTSIZEKEY, "14");
		explanations.put(FONTSIZEKEY, "The font size to use");
		orderedkeys.push(FONTSIZEKEY);
	} // end of constructor
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 10 02";
  } //end of version()
  
/**
* As required by Executor.
*/
	public final String execute() throws RetroTectorException {
	
// get input file if any
		File f;
		String s1 = getString(PUTEINFILEKEY, "");
		if (s1.length() > 0) {
			f = Utilities.getReadFile(RetroTectorEngine.currentDirectory(), s1);
			THEFRAME.setTitle(s1);
			ParameterFileReader reader = new ParameterFileReader(f, parameters);
			reader.readParameters();
			reader.close();
		} else {
			THEFRAME.setTitle(getString(SCRIPTPATHKEY, ""));
			f = Utilities.getReadFile(getString(SCRIPTPATHKEY, ""));
		}
    int hitsfound = getInt(ORFID.NROFHITSKEY, 0);
    if (hitsfound <= 0) {
      RetroTectorEngine.displayError(new RetroTectorException("Puteinview", "No hits were found"), RetroTectorEngine.NOTICELEVEL);
      return "No hits";
    }
		String[ ] hi = getStringArray(Putein.HITSKEY);
		hi = getStringArray(Putein.PUTEINKEY);
		if (hi == null) {
			hi = getStringArray(Xon.XONKEY);
		}
		PuteinCanvas pc = new PuteinCanvas(hi, getInt(FONTSIZEKEY, 14));
		JScrollPane sp = new JScrollPane(pc);
		THEFRAME.getContentPane().add(sp, BorderLayout.SOUTH);
		
		BufferedReader theReader = null;
		StringBuffer sb = new StringBuffer();
		String line;
		try {
			if (f instanceof ZFile) {
				ZFile zf = (ZFile) f;
				theReader = new BufferedReader(new InputStreamReader(zf.ZIPFILE.getInputStream(zf.ZIPENTRY)));
			} else {
				theReader = new BufferedReader(new FileReader(f));
			}
			line = theReader.readLine();
			while (line != null) {
				sb.append(line);
				sb.append('\n');
				line = theReader.readLine();
			}
			theReader.close();
		} catch (Exception e) {
			RetroTectorException.sendError(this, "Trouble reading from", f.getName());
		}
		
		JTextArea theArea = new JTextArea(sb.toString(), 15,50); // for text display
		THEFRAME.getContentPane().add(new JScrollPane(theArea), BorderLayout.CENTER);

		
		THEFRAME.pack();
		THEFRAME.setBounds(150, 50, 600, 200);
		AWTUtilities.showFrame(THEFRAME, new Dimension(600, 250));

		return "";
	} // end of execute()

} // end of Puteinview
