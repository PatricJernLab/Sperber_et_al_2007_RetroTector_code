/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 21/9 -06
* Beautified 21/9 -06
*/
package retrotector;

import org.jibble.epsgraphics.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;

/**
* Thread that handles the saving of an Image to a EPS file.
*/
public class EPSSaveThread extends Thread {

	private JComponent THECOMPONENT;
	private float factor = 1.0f;

/**
* Constructor.
*	@param	theComponent	The JComponent to save,
*	@param	percent				Percent display size.
*/
	public EPSSaveThread(JComponent theComponent, int percent) {
		THECOMPONENT = theComponent;
		factor = 0.01f * percent;
	} // end of constructor(JComponent, int)
	
/**
* Does the actual saving.
*/
	public void run() {
		try {
			JFileChooser fd = new JFileChooser(RetroTectorEngine.currentDirectory());
			fd.setDialogTitle("EPS file to save in");
			if (fd.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File ff = fd.getSelectedFile();
				String name = ff.getName();
				if (!name.endsWith(".eps") & !name.endsWith(".EPS")) {
					if (RetroTectorEngine.doQuestion("The file name does not seem to have an EPS extension. Do you want one added")) {
						ff = new File(ff.getParent(), name + ".eps");
					}
				}
				EpsGraphics2D g = new EpsGraphics2D("", ff, 0, 0, 1 + Math.round(THECOMPONENT.getPreferredSize().width * factor), 1 + Math.round(THECOMPONENT.getPreferredSize().height * factor));
				g.setClip(0, 0, 1 + Math.round(THECOMPONENT.getPreferredSize().width * factor), 1 + Math.round(THECOMPONENT.getPreferredSize().height * factor));
				g.scale(factor, factor);
				g.setStroke(new BasicStroke(factor));
				if (THECOMPONENT instanceof JCanvas) {
					((JCanvas) THECOMPONENT).paintComponent(g);
				} else {
					THECOMPONENT.paint(g);
				}
				g.flush();
				g.close();
			}
		} catch (IOException ioe) {
				RetroTectorEngine.displayError(new RetroTectorException("EPSSaveThread", "A Java IOException occurred"));
		}
	} // end of run()

} // end of EPSSaveThread
