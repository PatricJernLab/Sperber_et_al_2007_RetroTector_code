/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 4/12 -06
* Beautified 4/12 -06
*/
package plugins;

import retrotector.*;

import java.io.*;
import java.util.*;

/**
* Executor which takes a file in the current directory and cuts it into separate parts.
* The source file should be a bare DNA file (possibly with leading comment
* lines with leading { or >).
*<PRE>
*     Parameters:
*
*   ChunkSize
* The approximate length of each part.
* Default: 2000015000.
*
*   ChunkOverlap
* If several files are created, they overlap approximately by this.
* Default: 15000.
*
*   FileName
* Name of file to partition.
* Default: "".
*
*</PRE>
*/
public class RoughcutDNA extends Executor {

/**
* Key for maximal length of created files = "ChunkSize".
*/
	public static final String CHUNKSIZEKEY = "ChunkSize";

/**
* Key for overlap between created files = "ChunkOverlap".
*/
	public static final String CHUNKOVERLAPKEY = "ChunkOverlap";

/**
* Key for file name = "FileName".
*/
	public static final String FILENAMEKEY = "FileName";

	private BufferedReader sourceReader; // for source file
	private PrintWriter chunkWriter; // for output of current chunk
	private PrintWriter nextchunkWriter = null; // for output of next chunk
	

/**
* Constructor. Specifies obligatory parameters.
*/
	public RoughcutDNA() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(CHUNKSIZEKEY, "2000015000");
		explanations.put(CHUNKSIZEKEY, "Maximal length of part");
		orderedkeys.push(CHUNKSIZEKEY);
		parameters.put(CHUNKOVERLAPKEY, "15000");
		explanations.put(CHUNKOVERLAPKEY, "Overlap between parts");
		orderedkeys.push(CHUNKOVERLAPKEY);
		parameters.put(FILENAMEKEY, "");
		explanations.put(FILENAMEKEY, "Source file name");
		orderedkeys.push(FILENAMEKEY);
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 12 04";
  } // end of version()
	
  private String nextLine() throws IOException {
		String s = sourceReader.readLine();
		if (s == null) {
			sourceReader.close();
			chunkWriter.close();
			if (nextchunkWriter != null) {
				nextchunkWriter.close();
			}
			return null;
		} else {
			return s;
		}
	} // end of nextLine()
	
/**
* Executes, using parameters above.
*/
	public String execute() throws RetroTectorException {

		Compactor comp = Compactor.BASECOMPACTOR;
    File currDir = RetroTectorEngine.currentDirectory();
		if (currDir == null) {
			haltError("The current directory is not valid");
		}

		String sourceFileName = getString(FILENAMEKEY, "");
		File sourceFile = new File(currDir, sourceFileName);
		int dotindex = sourceFileName.lastIndexOf(".");
		char nextidchar = 'B';
		String nextChunkFileName;
		File nextChunkFile;

		sourceReader = null;
		try {
			sourceReader = new BufferedReader(new FileReader(sourceFile));
		} catch (IOException ioe) {
			haltError("Could not open " + sourceFile.getPath());
		}
		
		String chunkFileName = sourceFileName.substring(0, dotindex) + "_A" + FileNamer.TXTTERMINATOR;
		File chunkFile = new File(currDir, chunkFileName); // new file
		chunkWriter = null;
		try {
			chunkWriter = new PrintWriter(new FileWriter(chunkFile));
		} catch (IOException ioe) {
			haltError("Could not open " + chunkFile.getPath());
		}

		String currentline = null;
		try {
			while (((currentline = sourceReader.readLine().trim()).startsWith("{")) || currentline.startsWith(">") || (currentline.length() == 0)) {		
			}
		} catch (IOException ioe) {
			haltError("Trouble reading " + sourceFile.getPath());
		}
		int lineLength = currentline.length();
		
		int chunkLines = getInt(CHUNKSIZEKEY, 2000015000) / lineLength;
		int overlapLines = getInt(CHUNKOVERLAPKEY, 15000) / lineLength;
		int chunkCounter = 0;
		int nextChunkCounter = 0;
		int nextChunkStart = chunkLines - overlapLines;
		
		for (;;) {
			try {
				chunkWriter.println(currentline);
				chunkCounter++;
				if (nextchunkWriter != null) {
					nextchunkWriter.println(currentline);
					nextChunkCounter++;
				}
				if (chunkCounter == chunkLines - overlapLines) {
					nextChunkFileName = sourceFileName.substring(0, dotindex) + "_" + nextidchar + FileNamer.TXTTERMINATOR;
					nextidchar++;
					nextChunkFile = new File(currDir, nextChunkFileName); // new file
					nextchunkWriter = null;
					try {
						nextchunkWriter = new PrintWriter(new FileWriter(nextChunkFile));
					} catch (IOException ioe) {
						haltError("Could not open " + chunkFile.getPath());
					}
					nextChunkCounter = 0;
				}
				if (chunkCounter == chunkLines) {
					chunkWriter.close();
					showProgress();
					chunkWriter = nextchunkWriter;
					nextchunkWriter = null;
					chunkCounter = nextChunkCounter;
				}
				currentline = nextLine();
				if (currentline == null) {
					return "";
				}
			} catch (IOException ioe) {
				haltError("Trouble writing " + chunkFile.getPath());
			}
		}
	} // end of execute()

} // end of RoughcutDNA
