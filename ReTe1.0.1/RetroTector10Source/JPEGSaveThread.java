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

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import com.sun.image.codec.jpeg.*;

/**
* Thread that handles the saving of an Image to a JPEG file.
*/
public class JPEGSaveThread extends Thread {

	private BufferedImage THEIMAGE;

/**
* Constructor.
*	@param	theImage	The BufferedImage to save,
*/
	public JPEGSaveThread(BufferedImage theImage) {
		THEIMAGE = theImage;
	} // end of constructor(BufferedImage)
	
/**
* Does the actual saving.
*/
	public void run() {
		try {
			JFileChooser fd = new JFileChooser(RetroTectorEngine.currentDirectory());
			fd.setDialogTitle("JPEG file to save in");
			if (fd.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File ff = fd.getSelectedFile();
				String name = ff.getName();
				if (!name.endsWith(".jpg") & !name.endsWith(".JPG") & !name.endsWith(".jpeg") & !name.endsWith(".JPEG")) {
					if (RetroTectorEngine.doQuestion("The file name does not seem to have a JPEG extension. Do you want one added")) {
						ff = new File(ff.getParent(), name + ".jpeg");
					}
				}
				BufferedOutputStream theStream = new BufferedOutputStream(new FileOutputStream(ff));
				JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(theStream);

				JPEGEncodeParam jParam = enc.getDefaultJPEGEncodeParam(THEIMAGE);
				jParam.setQuality(1.0f, false);
				enc.setJPEGEncodeParam(jParam);

				enc.encode(THEIMAGE);
				theStream.close();
			}
		} catch (IOException ioe) {
				RetroTectorEngine.displayError(new RetroTectorException("JPEGSaveThread", "A Java IOException occurred"));
		}
	} // end of run()

} // end of JPEGSaveThread
