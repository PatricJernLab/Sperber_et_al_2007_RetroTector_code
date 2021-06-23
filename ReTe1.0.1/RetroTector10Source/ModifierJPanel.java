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

package retrotector;

import javax.swing.*;
import java.awt.*;

/**
* Canvas for the displaying of Modifier values. Draws curves for the three frames, or a single curve,
* as appropriate. If there are "5LTR_start", "5LTR_end", "Gag_start", "Gag_end", 
* "Pro_start", "Pro_end", "Pol_start", "Pol_end", "Env_start", "Env_end", "3LTR_start" or "3LTR_end"
* parameters in the DNA, they are indicated in the diagram
*/
public class ModifierJPanel extends JCanvas {

/**
* For frame 1 = black.
*/
  public static final Color FRAME1COLOUR = Color.black;

/**
* For frame 2 = red.
*/
  public static final Color FRAME2COLOUR = Color.red;

/**
* For frame 3 = blue.
*/
  public static final Color FRAME3COLOUR = Color.blue;

	private final int MAXWIDTH = 5000; // no wider than this

	private int step; //between ticked positions
	
	private final int height;
	private final int width;
	private final Modifier parent;
	private final float horfactor;
	private final float vertfactor;
	private DNA theDNA;

/**
* Constructor.
* @param	par	The Modifier to show.
*/
	public ModifierJPanel(Modifier par) {
		parent = par;
		height = 245;
		theDNA = parent.getDNA();
		int d = theDNA.LENGTH;
		if (d < MAXWIDTH) {
			width = d;
			horfactor = 1.0f;
		} else {
			width = MAXWIDTH;
			horfactor = MAXWIDTH * 1.0f / d;
		}
		setPreferredSize(new Dimension(width, height));
		setMinimumSize(new Dimension(width, height));
		vertfactor = 200.0f / parent.getUpperLimit();
		step = 100;
		while (step * horfactor < 200) {
			step += 100;
		}
	} // end of constructor(Modifier)
	
// draws the curve for one reading frame
	private final void frameCurve(Graphics g, int start) {
		if (DNA.frameOf(start) == 1) {
			g.setColor(FRAME1COLOUR);
		} else if (DNA.frameOf(start) == 2) {
			g.setColor(FRAME2COLOUR);
		} else if (DNA.frameOf(start) == 3) {
			g.setColor(FRAME3COLOUR);
		}
		int x = 0;
		int newx;
		float tempf = parent.modification(start - 3);
		int y = 0;
		if (!Float.isNaN(tempf)) {
			y = 200 - Math.round(tempf * vertfactor);
		}
		int newy = y;
		int extpos;
		for (int pos=start; pos<parent.getDNA().LENGTH; pos += 3) {
			newx = Math.round(pos * horfactor);
			tempf = parent.modification(pos);
			if (!Float.isNaN(tempf)) {
				newy = 200 - Math.round(tempf * vertfactor);
				g.drawLine(x, y, newx, newy);
				extpos = parent.getDNA().externalize(pos);
				if ((extpos % step) == 0) {
					g.drawLine(newx, 200, newx, 210);
					g.drawString(String.valueOf(extpos), newx, 220);
				}
			}
			x = newx;
			y = newy;
		}
	} // end of frameCurve(Graphics, int)
			
	
/**
* As required by JComponent.
*/
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawString(String.valueOf(parent.getUpperLimit()), 0, 15);
		g.drawString("" + Utilities.className(parent), 100, 15);
		g.drawString(parent.getDNA().NAME, 250, 15);
		if (parent.getDNA().PRIMARYSTRAND) {
			g.drawString(Executor.PRIMARY + " strand", 400, 15);
		} else {
			g.drawString(Executor.SECONDARY + " strand", 400, 15);
		}
		g.drawLine(0, 200, MAXWIDTH, 200);
		if (parent.FRAMED) {
      frameCurve(g, 3);
      g.drawString("Frame 1", 0, 30);
			frameCurve(g, 4);
      g.drawString("Frame 2", 0, 45);
			frameCurve(g, 5);
      g.drawString("Frame 3", 0, 60);
		} else {
			int x = 0;
			int newx;
			int y = 0;
			float tempf = parent.modification(0);
			if (!Float.isNaN(tempf)) {
				y = 200 - Math.round(tempf * vertfactor);
			}
			int newy = y;
			int extpos;
			for (int pos=1; pos<parent.getDNA().LENGTH; pos++) {
				newx = Math.round(pos * horfactor);
				tempf = parent.modification(pos);
				if (!Float.isNaN(tempf)) {
					newy = 200 - Math.round(tempf * vertfactor);
					g.drawLine(x, y, newx, newy);
					extpos = parent.getDNA().externalize(pos);
					if ((extpos % step) == 0) {
						g.drawLine(newx, 200, newx, 210);
						g.drawString(String.valueOf(extpos), newx, 215);
					}
				}
				x = newx;
				y = newy;
			}
		}
		int papos;
		if ((papos = theDNA.getInt("5LTR_start", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
			g.drawString("5'LTR>", papos + 3, 240);
		}
		if ((papos = theDNA.getInt("5LTR_end", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
		}
		if ((papos = theDNA.getInt("Gag_start", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
			g.drawString("gag>", papos + 3, 240);
		}
		if ((papos = theDNA.getInt("Gag_end", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
		}
		if ((papos = theDNA.getInt("Pro_start", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
			g.drawString("pro>", papos + 3, 240);
		}
		if ((papos = theDNA.getInt("Pro_end", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
		}
		if ((papos = theDNA.getInt("Pol_start", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
			g.drawString("pol>", papos + 3, 240);
		}
		if ((papos = theDNA.getInt("Pol_end", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
		}
		if ((papos = theDNA.getInt("Env_start", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
			g.drawString("env>", papos + 3, 240);
		}
		if ((papos = theDNA.getInt("Env_end", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
		}
		if ((papos = theDNA.getInt("3LTR_start", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
			g.drawString("3'LTR>", papos + 3, 240);
		}
		if ((papos = theDNA.getInt("3LTR_end", Integer.MIN_VALUE)) > 0) {
			papos = Math.round(theDNA.internalize(papos) * horfactor);
			g.drawLine(papos, 200, papos, 240);
		}
	} // end of paintComponent(Graphics)
	
} // end of ModifierJPanel
