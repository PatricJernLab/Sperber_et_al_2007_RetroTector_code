/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/10 -06
* Beautified 12/10 -06
*/
package builtins;

import retrotector.*;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/**
* Executor to display all Chains in a sequence graphically.
*<PRE>
*     Parameters:
*
*   InputFile
* The name of the file to display.
* Default: 001SelectedChains.txt
*		
*   DNAFile
* The file to read DNA from.
* Default: Directory name + .txt
*</PRE>
*/
public class Sequenceview extends Executor {

/**
* Canvas showing Chains according to position in DNA
*/
	class SeqCanvas extends JCanvas {

/**
* Size of big font = 18.
*/
		static final int FONTHEIGHT = 18;

/**
* Preferred width of SeqCanvas = 300.
*/
		final int DISPWIDTH = 300;

/**
* Extra space at end = 100.
*/
		final float ENDSPACE = 100.0f;

/**
* Width (in pixels) of total graphical information = 800.
*/
	 	static final int TOTALWIDTH = 800;

/**
* Height (in pixels) of total graphical information = 300.
*/
		static final int TOTALHEIGHT = 300;

		private ChainGraphInfo[ ] theChainInfo; // the info to build the display on
		private float horfactor; // factor between base number and pixel coordinate
		
		private final int FIRST;
		private final int LAST;
		
		private Font bigFont = new Font("Helvetica", Font.PLAIN, FONTHEIGHT);
		private Font mediumFont = new Font("Helvetica", Font.PLAIN, FONTHEIGHT * 2 / 3);
		private Font smallFont = new Font("Helvetica", Font.PLAIN, FONTHEIGHT / 2);
		
/**
* New canvas showing the specified Chains.
* @param	c			Info about the Chains to display.
*/
	 	SeqCanvas(ChainGraphInfo[ ] c) {
			setPreferredSize(new Dimension(TOTALWIDTH, TOTALHEIGHT));
			setMinimumSize(new Dimension(DISPWIDTH, TOTALHEIGHT));
			theChainInfo = c;
			FIRST = 0;
			LAST = FIRST + primDNA.LENGTH;
			horfactor = (TOTALWIDTH - ENDSPACE) / primDNA.LENGTH;
		} // end of SeqCanvas.constructor(ChainGraphInfo[ ])
		
		
// Converts (internal) base number in DNA to horizontal pixel coordinate.
		private final int basToPos(int bas) {
			return (int) Math.round(bas * horfactor);
		} // end of SeqCanvas.basToPos(int)
		
/**
* As required by JComponent.
*/
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.black);
			int linepos = 60;
			int tickdistance = ((LAST - FIRST) / 1000) * 100;
			g.drawLine(0, linepos, TOTALWIDTH, linepos);
				
// set horizontal markers
			g.setFont(smallFont);
			g.setColor(Color.black);
			int tick = (FIRST / tickdistance) * tickdistance + tickdistance;
			int th;
			while (tick < LAST ) {
				th = basToPos(tick);
				g.drawLine(th, linepos, th, linepos - 10);
				g.drawString(String.valueOf(primDNA.externalize(tick)), th, linepos - 12);
				tick += tickdistance;
			}
			
// find max score
			int topscore = Integer.MIN_VALUE;
			for (int c1=0; c1<theChainInfo.length; c1++) {
				topscore = Math.max(topscore, theChainInfo[c1].SCORE);
			}

// draw rectangles for Chains
			g.setFont(mediumFont);
			int height;
			for (int cc=0; cc<theChainInfo.length; cc++) {
				height = theChainInfo[cc].SCORE * 100 / topscore;
				if (theChainInfo[cc].number.startsWith("S")) {
					int le = basToPos(primDNA.internalize(secDNA.externalize(theChainInfo[cc].LASTBASEPOS)));
					int wi = basToPos(theChainInfo[cc].LASTBASEPOS) - basToPos(theChainInfo[cc].FIRSTBASEPOS);
					g.setColor(Color.red);
					g.drawRect(le, linepos, wi, height);
					g.drawRect(le + 1, linepos + 1, wi - 2, height - 2);
					g.drawString(String.valueOf(theChainInfo[cc].SCORE), le + 3, linepos + height + 24);
					g.drawString("" + theChainInfo[cc].number + "  " + theChainInfo[cc].GENUS, le + 3, linepos + height + 12);
				} else {
					g.setColor(Color.black);
					g.drawRect(basToPos(theChainInfo[cc].FIRSTBASEPOS), linepos, basToPos(theChainInfo[cc].LASTBASEPOS) - basToPos(theChainInfo[cc].FIRSTBASEPOS), height);
					g.drawRect(basToPos(theChainInfo[cc].FIRSTBASEPOS) + 1, linepos + 1, basToPos(theChainInfo[cc].LASTBASEPOS) - basToPos(theChainInfo[cc].FIRSTBASEPOS) - 2, height - 2);
					g.drawString(String.valueOf(theChainInfo[cc].SCORE), basToPos(theChainInfo[cc].FIRSTBASEPOS) + 3, linepos + height + 24);
					g.drawString("" + theChainInfo[cc].number + "  " + theChainInfo[cc].GENUS, basToPos(theChainInfo[cc].FIRSTBASEPOS) + 3, linepos + height + 12);
				}
			}
			
// draw single LTR markers
			int ho;
			int fi;
			int la;
			int ind1;
			int ind2;
      if (pHits != null) {
				g.setColor(Color.black);
				for (int ph=0; ph<pHits.length; ph++) {
					ind1 = pHits[ph].indexOf("(");
					ho = primDNA.internalize(Integer.parseInt(pHits[ph].substring(0, ind1).trim()));
					ind2 = pHits[ph].indexOf("-");
					fi = primDNA.internalize(Integer.parseInt(pHits[ph].substring(ind1 + 1, ind2).trim()));
					ind1 = pHits[ph].indexOf(")");
					la = primDNA.internalize(Integer.parseInt(pHits[ph].substring(ind2 + 1, ind1).trim()));
					g.drawLine(basToPos(fi), linepos, basToPos(fi), linepos - 20); 
					g.drawLine(basToPos(ho), linepos - 20, basToPos(ho), linepos - 30); 
					g.drawLine(basToPos(la), linepos, basToPos(la), linepos - 20); 
					g.drawLine(basToPos(fi), linepos - 20, basToPos(la), linepos - 20);
				}
      }
      if (sHits != null) {
        g.setColor(Color.red);
        for (int sh=0; sh<sHits.length; sh++) {
					ind1 = sHits[sh].indexOf("(");
					ho = primDNA.internalize(Integer.parseInt(sHits[sh].substring(0, ind1).trim()));
					ind2 = sHits[sh].indexOf("-");
					fi = primDNA.internalize(Integer.parseInt(sHits[sh].substring(ind1 + 1, ind2).trim()));
					ind1 = sHits[sh].indexOf(")");
					la = primDNA.internalize(Integer.parseInt(sHits[sh].substring(ind2 + 1, ind1).trim()));
          g.drawLine(basToPos(fi), linepos, basToPos(fi), linepos - 20); 
          g.drawLine(basToPos(ho), linepos - 20, basToPos(ho), linepos - 30); 
          g.drawLine(basToPos(la), linepos, basToPos(la), linepos - 20); 
          g.drawLine(basToPos(fi), linepos - 20, basToPos(la), linepos - 20);
        }
      }

// auxiliary information
			String na = primDNA.NAME;
			int ind = na.lastIndexOf(File.separatorChar);
			if (ind >= 0) {
				na = na.substring(ind);
			}
			ind = na.length() - 4;
			na = na.substring(0, ind) + " primary and secondary";
			g.setColor(Color.black);
			g.drawString(na, 5, 20);

		} // end of SeqCanvas.paintComponent(Graphics)
		
	} // end of SeqCanvas


/**
* Window for the presentation of all Chains in a DNA. It has a text part,
* with information about single LTRs, a scrollable SeqCanvas for graphic display and
* a button for saving of the graphics.
*/
	class SeqWindow extends CloseableJFrame implements ActionListener {

		private SeqCanvas theCanvas; // for graphic display
	  private JScrollPane thePane; // to contain theCanvas
		private JTextArea theArea = new JTextArea(15, 50); // for text display
		private ChainGraphInfo[ ] chainInfos; // all the Chains displayable in this window
		private int firstvalid = -1;
		
		private JButton saveButton = new JButton("Save graphics as file");
		
/**
* Creates the window.
* Shows information about the first Chain in the text and graphics parts.
* @param	chainInfos	Array of info for the Chains to show.
* @param	text				Text for the text part
* @param	title				Title for window
*/
	 	SeqWindow(ChainGraphInfo[ ] chainInfos, String[ ] text, String title) {
			super(title);
			this.chainInfos = chainInfos;
			
			theCanvas = new SeqCanvas(chainInfos);
			thePane = new JScrollPane(theCanvas);
      if (text != null) {
        for (int l=0; l<text.length; l++) {
          theArea.append("\n");
          theArea.append(text[l]);
        }
      }
				
			getContentPane().add(thePane, BorderLayout.CENTER);
				
			getContentPane().add(new JScrollPane(theArea), BorderLayout.NORTH);
			
			saveButton.addActionListener(this);
			getContentPane().add(saveButton, BorderLayout.SOUTH);
			
			setLocation(200, 50);
			AWTUtilities.showFrame(this, new Dimension(600, 500));
		} // end of SeqWindow.constructor(ChainGraphInfo[ ], String[ ], String)


/**
* Reacts to clicks on the Save graphics button.
*/
	 	public void actionPerformed(ActionEvent ae) {
	 		if (ae.getSource() == saveButton) {
				new GraphicsJFrame(getTitle(), theCanvas);
	 		}
		} // end of SeqWindow.actionPerformed(ActionEvent)
			
	} // end of SeqWindow
	
	
	DNA primDNA;
	DNA secDNA;
	String[ ] pHits;
	String[ ] sHits;
	private SeqWindow theWindow;
	private int maxn = -1; // highest Chain number encountered
	
/**
* Standard Executor constructor.
*/
	public Sequenceview() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
    File f = RetroTectorEngine.currentDirectory();
		String dirname = f.getName();
		parameters.put(INPUTFILEKEY, "001SelectedChains.txt");
		explanations.put(INPUTFILEKEY, "The Chain (RetroVID output) file to use");
		orderedkeys.push(INPUTFILEKEY);
		parameters.put(DNAFILEKEY, dirname + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The DNA file these chains belong in" + NOTINTERACT);
		orderedkeys.push(DNAFILEKEY);
	} // end of constructor()
		
/**
* Reads parameters, from script or parameter window.
* @param	script	The script to fetch from, or null.
*/
	public void initialize(ParameterFileReader script) throws RetroTectorException {
		super.initialize(script);
		if (runFlag && (script != null)) {
			maxn = script.getMaxchainnumber();
		}
	} // end of initialize(ParameterFileReader)
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 10 12";
  } // end of version()
  

/**
* Execute as specified above.
*/
	public final String execute() throws RetroTectorException {
	
		String s1 = getString(INPUTFILEKEY, "");
		if (s1.length() > 0) {
			File f = Utilities.getReadFile(RetroTectorEngine.currentDirectory(), s1);
			ParameterFileReader reader = new ParameterFileReader(f, parameters);
			reader.readParameters();
			maxn = reader.getMaxchainnumber();
			reader.close();
		}

		Stack chainStack = new Stack();
		ChainGraphInfo cgi;
		String dnaFileName = getString(DNAFILEKEY, "");
      
// get single LTRs
    pHits = getStringArray("PSingleLTRs");
    sHits = getStringArray("SSingleLTRs");      
		
    primDNA = getDNA(getString(DNAFILEKEY, ""), true);
    secDNA = getDNA(getString(DNAFILEKEY, ""), false);
    Object o;
    int count = 100;
    for (int chainnr=1; (chainnr<=maxn) && (count > 0); chainnr++) {
      String s = ParameterFileReader.CHAINP + chainnr;
      if ((o = parameters.get(s)) != null) {
        String[ ] ss = getStringArray(s);
        cgi = new ChainGraphInfo(ss, primDNA);
        cgi.number = "P" + chainnr;
        chainStack.push(cgi);
        count--;
      }
      s = ParameterFileReader.CHAINS + chainnr;
      if ((o = parameters.get(s)) != null) {
        String[ ] ss = getStringArray(s);
        cgi = new ChainGraphInfo(ss, secDNA);
        cgi.number = "S" + chainnr;
        chainStack.push(cgi);
        count--;
      }
      s = "Chain" + chainnr;
      if ((o = parameters.get(s)) != null) {
        String[ ] ss = getStringArray(s);
        cgi = new ChainGraphInfo(ss, secDNA);
        cgi.number = String.valueOf(chainnr);
        chainStack.push(cgi);
        count--;
      }
    }
    ChainGraphInfo[ ] graphinfo = new ChainGraphInfo[chainStack.size()];
    chainStack.copyInto(graphinfo);
    
    String[ ] wholetext = new String[pHits.length + sHits.length + 4];
    wholetext[0] = "Single LTR candidates in primary strand:";
    int w = 1;
    for (int w1=0; w1<pHits.length; w1++) {
      wholetext[w++] = pHits[w1];
    }
    wholetext[w++] = "";
    wholetext[w++] = "Single LTR candidates in secondary strand:";
    for (int w2=0; w2<sHits.length; w2++) {
      wholetext[w++] = sHits[w2];
    }
    wholetext[w++] = "";

    theWindow = new SeqWindow(graphinfo, wholetext, "");
		
		return "";
	} // end of execute()
	 
} // end of Sequenceview
