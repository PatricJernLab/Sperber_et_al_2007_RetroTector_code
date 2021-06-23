/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/9 -06
* Beautified 22/9 -06
*/

package retrotector;

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

/**
* Thread that handles the saving of an Image to a PNG file.
*/
public class PNGSaveThread extends Thread {

	private BufferedImage THEIMAGE;

/**
* Constructor.
*	@param	theImage	The BufferedImage to save,
*/
	public PNGSaveThread(BufferedImage theImage) {
		THEIMAGE = theImage;
	} // end of constructor
	
/**
* Does the actual saving.
*/
	public void run() {
		try {
			JFileChooser fd = new JFileChooser(RetroTectorEngine.currentDirectory());
			fd.setDialogTitle("PNG file to save in");
			if (fd.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File ff = fd.getSelectedFile();
				String name = ff.getName();
				if (!name.endsWith(".png") & !name.endsWith(".PNG")) {
					if (RetroTectorEngine.doQuestion("The file name does not seem to have a PNG extension. Do you want one added")) {
						ff = new File(ff.getParent(), name + ".png");
					}
				}
				ImageIO.write(THEIMAGE, "PNG", ff);
			}
		} catch (IOException ioe) {
				RetroTectorEngine.displayError(new RetroTectorException("PNGSaveThread", "A Java IOException occurred"));
		}
	} // end of run()

} // end of PNGSaveThread
