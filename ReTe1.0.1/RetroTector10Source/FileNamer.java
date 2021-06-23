/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/11 -06
* Beautified 12/11 -06
*/

package retrotector;

import java.io.*;

/**
* Class to handle syntax of various file names.
*/
public class FileNamer {

/**
* = 'LTRID_'.
*/
	public static final String LTRIDSCRIPTLEADER = "LTRID_";

/**
* = 'RetroVID_'.
*/
	public static final String RETROVIDSCRIPTLEADER = "RetroVID_";

/**
* = 'PseuGID_'.
*/
	public static final String PSEUGIDSCRIPTLEADER = "PseuGID_";

/**
* = 'ORFID_'.
*/
	public static final String ORFIDSCRIPTLEADER = "ORFID_";

/**
* = 'XonID_'.
*/
	public static final String XONIDSCRIPTLEADER = "XonID_";

/**
* = 'Putein_'.
*/
	public static final String PUTEINFILELEADER = "Putein_";

/**
* = 'EnvTracer'.
*/
	public static final String ENVTRACERSCRIPTLEADER = "EnvTracer";

/**
* = 'EnvTrace'.
*/
	public static final String ENVTRACEFILELEADER = "EnvTrace";

/**
* Å 'XXon_'
*/
	public static final String XXONFILELEADER = "XXon_";
	
/**
* Å 'Xon_'
*/
	public static final String XONFILELEADER = "Xon_";
	
/**
* = 'SelectedChains.txt'.
*/
	public static final String SELECTEDCHAINSTERMINATOR = "SelectedChains.txt";

/**
* = '.txt'.
*/
	public static final String TXTTERMINATOR = ".txt";

/**
* @param	dir	A directory.
* @return	A sweepable LTR script in dir.
*/
	public final static File createLTRIDScript(File dir) {
		return Utilities.uniqueFile(dir, LTRIDSCRIPTLEADER, Utilities.SWEEPABLESCRIPTFILESUFFIX);
	} // end of createLTRIDScript(File)

/**
* @return	A sweepable RetroVID script in current directory.
*/
	public final static File createRetroVIDScript() {
		return Utilities.uniqueFile(RetroTectorEngine.currentDirectory(),
						RETROVIDSCRIPTLEADER, Utilities.SWEEPABLESCRIPTFILESUFFIX);
	} // end of createRetroVIDScript()

/**
* @param	name	A String.
* @return	true if name is a valid RetroVID script name.
*/
	public final static boolean isRetroVIDScript(String name) {
		if (!name.startsWith(RETROVIDSCRIPTLEADER)) {
			return false;
		}
		int ii = -1;
		if (name.endsWith(Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 3);
		} else if (name.endsWith(Utilities.SWEPTSCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 3);
		}
		if (ii < 0) {
			return false;
		} else {
			return true;
		}
	} // end of isRetroVIDScript(String)

/**
* @param	name	A String.
* @return	The file generation if name is a valid RetroVID script name, or -1.
*/
	public final static int getRetroVIDScriptGeneration(String name) {
		if (!isRetroVIDScript(name)) {
			return -1;
		}
		if (name.endsWith(Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 3);
		} else if (name.endsWith(Utilities.SWEPTSCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 3);
		} else {
			return -1;
		}
	} // end of getRetroVIDScriptGeneration(String)

/**
* @param	na	Leading part of file name.
* @return	A SelectedChains file in current directory.
*/
	public final static File createSelectedChainsFile(String na) {
		return Utilities.uniqueFile(RetroTectorEngine.currentDirectory(), na, SELECTEDCHAINSTERMINATOR);
	} // end of createSelectedChainsFile(String)
	
/**
* @param	na					Leading part of file name.
* @param	generation	A specific file generation.
* @return	A SelectedChains file in current directory.
*/
	public final static File getSelectedChainsFile(String na, int generation) {
		return new File(RetroTectorEngine.currentDirectory(), na + Executor.zeroLead(generation, 900) + SELECTEDCHAINSTERMINATOR);
	} // end of getSelectedChainsFile(String, int)
	
/**
* @param	name	A String.
* @return	true if name is a validSelectedChains file name.
*/
	public final static boolean isSelectedChainsFile(String name) {
		if (!name.endsWith(SELECTEDCHAINSTERMINATOR)) {
			return false;
		}
		int ii = getGenerationAt(name, name.length() - SELECTEDCHAINSTERMINATOR.length() - 3);
		if (ii < 0) {
			return false;
		} else {
			return true;
		}
	} // end of isSelectedChainsFile(String)
	
/**
* @param	name	A String.
* @return	The file generation if name is a valid SelectedChains file name, or -1.
*/
	public final static int getChainsFileGeneration(String name) {
		if (!isSelectedChainsFile(name)) {
			return -1;
		}
		return getGenerationAt(name, name.length() - SELECTEDCHAINSTERMINATOR.length() - 3);
	} // end of getChainsFileGeneration(String)

/**
* @param	ch	A Chain.
* @return	A non-sweepable PseuGID script in current directory.
*/
	public final static File createPseuGIDScript(Chain ch) {
		return Utilities.uniqueFile(RetroTectorEngine.currentDirectory(), PSEUGIDSCRIPTLEADER + ch.STRANDCHAR + Executor.zeroLead(ch.chainNumber, 90000) + "_", Utilities.SWEPTSCRIPTFILESUFFIX);
	} // end of createPseuGIDScript(Chain)

/**
* @param	ch				A Chain.
* @param	genuschar	A character representing a genus.
* @param	gene			Name of a Gene.
* @param	suffix		Terminating String.
* @return An ORFID script file.
*/
	public final static File createORFIDScript(Chain ch, char genuschar, String gene, String suffix) {
		String sss = ORFIDSCRIPTLEADER + ch.STRANDCHAR + Executor.zeroLead(ch.chainNumber, 90000) + genuschar + gene + "_";
		return Utilities.uniqueFile(RetroTectorEngine.currentDirectory(), sss, suffix);
	} // end of createORFIDScript(Chain, char, String, String)
	
/**
* @param	name	A String.
* @return	true if name is a valid ORFID script name.
*/
	public final static boolean isORFIDScript(String name) {
		if (!name.startsWith(ORFIDSCRIPTLEADER)) {
			return false;
		}
		int ii = -1;
		if (name.endsWith("M" + Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 4);
		} else if (name.endsWith("N" + Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 4);
		} else if (name.endsWith(Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 3);
		} else if (name.endsWith("M" + Utilities.SWEPTSCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 4);
		} else if (name.endsWith("N" + Utilities.SWEPTSCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 4);
		} else if (name.endsWith(Utilities.SWEPTSCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 3);
		}
		if (ii < 0) {
			return false;
		} else {
			return true;
		}
	} // end of isORFIDScript(String)

/**
* @param	name	A String.
* @return	The file generation if name is a valid ORFID script name, or -1.
*/
	public final static int getORFIDScriptGeneration(String name) {
		if (!isORFIDScript(name)) {
			return -1;
		}
		if (name.endsWith("M" + Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 4);
		} else if (name.endsWith("N" + Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 4);
		} else if (name.endsWith(Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 3);
		} else if (name.endsWith("M" + Utilities.SWEPTSCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 4);
		} else if (name.endsWith("N" + Utilities.SWEPTSCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 4);
		} else if (name.endsWith(Utilities.SWEPTSCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 3);
		} else {
			return -1;
		}
	} // end of getORFIDScriptGeneration((String)

/**
* @param	strand			'P' or 'S'.
* @param	chainnumber			A String with the relevant Chain number.
* @param	virusgenus			A single-character String representing a genus.
* @param	gene						Name of a Gene.
* @param	isStraightened	True if Putein the result of Putein.straightenedPutein().
* @return An ORFID output file and a Putein file.
*/
	public final static File[ ] createPuteinAndOutputFiles(String strand, String chainnumber, String virusgenus, String gene, boolean isStraightened) {
		String fge = strand.substring(0, 1) + chainnumber + virusgenus + gene;
		if (isStraightened) {
			fge = fge + "S";
		}
		fge = fge + "_";
		return Utilities.uniqueFiles(RetroTectorEngine.currentDirectory(), "ORFIDout_" + fge, TXTTERMINATOR, PUTEINFILELEADER + fge, TXTTERMINATOR);
	} // end of createPuteinAndOutputFiles(String, String, String, String)
	
/**
* @param	name	A String.
* @return	true if name is a valid Putein file name.
*/
	public final static boolean isPuteinFile(String name) {
		if (!name.startsWith(PUTEINFILELEADER)) {
			return false;
		}
		if (!name.endsWith(TXTTERMINATOR)) {
			return false;
		}
		int ii = getGenerationAt(name, name.length() - TXTTERMINATOR.length() - 3);
		if (ii < 0) {
			return false;
		} else {
			return true;
		}
	} // end of isPuteinFile(String)
	
/**
* @param	name	A String.
* @param	tag		A String describing a Chain.
* @return	true if name is a valid Putein file name, belonging to the Chain tag describes.
*/
	public final static boolean isPuteinFileFromChain(String name, String tag) {
		if (!isPuteinFile(name)) {
			return false;
		}
		return name.startsWith(PUTEINFILELEADER + tag);
	} // end of isPuteinFileFromChain(String, String)

/**
* @param	name	A String.
* @return	The file generation if name is a valid Putein file name, or -1.
*/
	public final static int getPuteinFileGeneration(String name) {
		if (!isPuteinFile(name)) {
			return -1;
		}
		return getGenerationAt(name, name.length() - TXTTERMINATOR.length() - 3);
	} // end of getPuteinFileGeneration(String)

/**
* @param	puteinFileName	A String.
* @return	The Gene part of puteinFileName, or null if it is not valid.
*/
	public final static String getGeneFromPutein(String puteinFileName) {
		if (isPuteinFile(puteinFileName)) {
			return puteinFileName.substring(14, 17);
		} else {
			return null;
		}
	} // end of getGeneFromPutein(String)
	
/**
* @param	puteinFileName	A String.
* @return	The genus character of puteinFileName, or null if it is not valid.
*/
	public final static String getGenusFromPutein(String puteinFileName) {
		if (isPuteinFile(puteinFileName)) {
			return puteinFileName.substring(13, 14);
		} else {
			return null;
		}
	} // end of getGenusFromPutein(String)

/**
* @param	puteinFileName	A String.
* @return	The strand character of puteinFileName, or null if it is not valid.
*/
	public final static String getStrandFromPutein(String puteinFileName) {
		if (isPuteinFile(puteinFileName)) {
			return puteinFileName.substring(PUTEINFILELEADER.length(), PUTEINFILELEADER.length() + 1);
		} else {
			return null;
		}
	} // end of getStrandFromPutein(String)
	
/**
* @param	puteinFileName	A String.
* @return	The full Chain name from puteinFileName, or null if it is not valid.
*/
	public final static String getLongChainnameFromPutein(String puteinFileName) {
		if (isPuteinFile(puteinFileName)) {
			return puteinFileName.substring(PUTEINFILELEADER.length(), PUTEINFILELEADER.length() + 6);
		} else {
			return null;
		}
	} // end of getLongChainnameFromPutein(String)

/**
* @param	puteinFileName	A String.
* @return	The zero-stripped Chain name from puteinFileName, or null if it is not valid.
*/
	public final static String getChainnameFromPutein(String puteinFileName) {
		if (isPuteinFile(puteinFileName)) {
			StringBuffer sb = new StringBuffer(getLongChainnameFromPutein(puteinFileName));
			while (sb.charAt(1) == '0') {
				sb.deleteCharAt(1);
			}
			return sb.toString();
		} else {
			return null;
		}
	} // end of getChainnameFromPutein(String)

/**
* @param	puteinFileName	A String.
* @return	True if putein is marked as straightened.
*/
	public final static boolean isStraightenedPutein(String puteinFileName) {
		return puteinFileName.charAt(puteinFileName.length() - TXTTERMINATOR.length() - 5) == 'S';
	} // end of isStraightenedPutein(String)
	
/**
* @param	ch	A Chain.
* @return	A sweepable EnvTracer script in current directory.
*/
	public final static File createEnvTracerScript(Chain ch) {
		return Utilities.uniqueFile(RetroTectorEngine.currentDirectory(),
						ENVTRACERSCRIPTLEADER + ch.STRANDCHAR + ch.chainNumber + "_", Utilities.SWEEPABLESCRIPTFILESUFFIX);
	} // end of createEnvTracerScript(Chain)

/**
* @param	ch	A Chain name.
* @return	A non-sweepable EnvTrace file in current directory.
*/
	public final static File createEnvTraceFile(String ch) {
		return Utilities.uniqueFile(RetroTectorEngine.currentDirectory(),
						ENVTRACEFILELEADER + ch + "_", TXTTERMINATOR);
	} // end of createEnvTraceFile(String)

/**
* @param	name	A String.
* @return	true if name is a valid EnvTrace file name.
*/
	public final static boolean isEnvTraceFile(String name) {
		if (!name.startsWith(ENVTRACEFILELEADER)) {
			return false;
		}
		if (!name.endsWith(TXTTERMINATOR)) {
			return false;
		}
		int ii = getGenerationAt(name, name.length() - TXTTERMINATOR.length() - 3);
		if (ii < 0) {
			return false;
		} else {
			return true;
		}
	} // end of isEnvTraceFile(String)

/**
* @param	name	A String.
* @param	tag		A String describing a Chain.
* @return	true if name is a valid EnvTrace file name, belonging to the Chain tag describes.
*/
	public final static boolean isEnvTraceFileFromChain(String name, String tag) {
		if (!isEnvTraceFile(name)) {
			return false;
		}
		return name.startsWith(ENVTRACEFILELEADER + tag);
	} // end of isEnvTraceFileFromChain(String, String)

/**
* @param	name	A String.
* @return	The file generation if name is a valid EnvTrace file name, or -1.
*/
	public final static int getEnvTraceFileGeneration(String name) {
		if (!isEnvTraceFile(name)) {
			return -1;
		}
		return getGenerationAt(name, name.length() - TXTTERMINATOR.length() - 3);
	} // end of getEnvTraceFileGeneration(String)

/**
* @param	envTraceFileName	A String.
* @return	The strand character of envTraceFileName, or null if it is not valid.
*/
	public final static String getStrandFromEnvTrace(String envTraceFileName) {
		if (isEnvTraceFile(envTraceFileName)) {
			return envTraceFileName.substring(ENVTRACEFILELEADER.length(), ENVTRACEFILELEADER.length() + 1);
		} else {
			return null;
		}
	} // end of getStrandFromEnvTrace(String)
	
/**
* @param	envTraceFileName	A String.
* @return	The Chain name from envTraceFileName, or null if it is not valid.
*/
	public final static String getChainnameFromEnvTrace(String envTraceFileName) {
		if (isEnvTraceFile(envTraceFileName)) {
			int ind = envTraceFileName.indexOf(".");
			if (envTraceFileName.indexOf("_") < 0) {
				return envTraceFileName.substring(ENVTRACEFILELEADER.length(), ind - 3);
			} else {
				return envTraceFileName.substring(ENVTRACEFILELEADER.length(), ind - 4);
			}
		} else {
			return null;
		}
	} // end of getChainnameFromEnvTrace(String)

/**
* @param	ch				A Chain.
* @param	sweepable	Determines suffix.
* @return A XonID script file.
*/
	public final static File createXonIDScript(Chain ch, boolean sweepable) {
		String sss = XONIDSCRIPTLEADER + ch.STRANDCHAR + Executor.zeroLead(ch.chainNumber, 90000) + "_";
		String suffix = Utilities.SWEPTSCRIPTFILESUFFIX;
		if (sweepable) {
			suffix = Utilities.SWEEPABLESCRIPTFILESUFFIX;
		}
		return Utilities.uniqueFile(RetroTectorEngine.currentDirectory(), sss, suffix);
	} // end of createXonIDScript(Chain, boolean)

/**
* @param	name	A String.
* @return	true if name is a valid XonID script name.
*/
	public final static boolean isXonIDScript(String name) {
		if (!name.startsWith(XONIDSCRIPTLEADER)) {
			return false;
		}
		int ii = -1;
		if (name.endsWith(Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 3);
		} else if (name.endsWith(Utilities.SWEPTSCRIPTFILESUFFIX)) {
			ii = getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 3);
		}
		if (ii < 0) {
			return false;
		} else {
			return true;
		}
	} // end of isXonIDScript(String)


/**
* @param	name	A String.
* @return	The file generation if name is a valid XonID script name, or -1.
*/
	public final static int getXonIDScriptGeneration(String name) {
		if (!isXonIDScript(name)) {
			return -1;
		}
		if (name.endsWith(Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length() - 3);
		} else if (name.endsWith(Utilities.SWEPTSCRIPTFILESUFFIX)) {
			return getGenerationAt(name, name.length() - Utilities.SWEPTSCRIPTFILESUFFIX.length() - 3);
		} else {
			return -1;
		}
	} // end of getXonIDScriptGeneration(String)

/**
* @param	xonIDScriptName	A String.
* @return	The full Chain name from xonIDScriptName, or null if it is not valid.
*/
	public final static String getLongChainnameFromXonIDScript(String xonIDScriptName) {
		if (isXonIDScript(xonIDScriptName)) {
			return xonIDScriptName.substring(XONIDSCRIPTLEADER.length(), XONIDSCRIPTLEADER.length() + 6);
		} else {
			return null;
		}
	} // end of getLongChainnameFromXonIDScript(String)

/**
* @param	xonIDScriptName	Name of XonID script.
* @param	xxon						The xxon to make a file for
* @return	XXon file, or null.
*/
	public final static File createXXonFile(String xonIDScriptName, Xon xxon) {
		return Utilities.uniqueFile(RetroTectorEngine.currentDirectory(), XXONFILELEADER + getLongChainnameFromXonIDScript(xonIDScriptName) + "at" + xxon.positionString() + "_", FileNamer.TXTTERMINATOR);
	} // end of createXXonFile(String, Xon)
	
/**
* @param	name	A String.
* @return	true if name is a valid XXon file name.
*/
	public final static boolean isXXonFile(String name) {
		if (!name.startsWith(XXONFILELEADER)) {
			return false;
		}
		if (!name.endsWith(TXTTERMINATOR)) {
			return false;
		}
		int ii = getGenerationAt(name, name.length() - TXTTERMINATOR.length() - 3);
		if (ii < 0) {
			return false;
		} else {
			return true;
		}
	} // end of isXXonFile(String)
	
/**
* @param	name	A String.
* @param	tag		A String describing a Chain.
* @return	true if name is a valid XXon file name, belonging to the Chain tag describes.
*/
	public final static boolean isXXonFileFromChain(String name, String tag) {
		if (!isXXonFile(name)) {
			return false;
		}
		return name.startsWith(XXONFILELEADER + tag);
	} // end of isXXonFileFromChain(String, String)

/**
* @param	xonIDScriptName	Name of XonID script.
* @param	xon							The xon to make a file for
* @return	Xon file, or null.
*/
	public final static File createXonFile(String xonIDScriptName, Xon xon) {
		return Utilities.uniqueFile(RetroTectorEngine.currentDirectory(), XONFILELEADER + getLongChainnameFromXonIDScript(xonIDScriptName) + "at" + xon.positionString() + "_", FileNamer.TXTTERMINATOR);
	} // end of createXonFile(String, Xon)
	
/**
* @param	name	A String.
* @return	true if name is a valid Xon file name.
*/
	public final static boolean isXonFile(String name) {
		if (!name.startsWith(XONFILELEADER)) {
			return false;
		}
		if (!name.endsWith(TXTTERMINATOR)) {
			return false;
		}
		int ii = getGenerationAt(name, name.length() - TXTTERMINATOR.length() - 3);
		if (ii < 0) {
			return false;
		} else {
			return true;
		}
	} // end of isXonFile(String)
	
/**
* @param	name	A String.
* @param	tag		A String describing a Chain.
* @return	true if name is a valid Xon file name, belonging to the Chain tag describes.
*/
	public final static boolean isXonFileFromChain(String name, String tag) {
		if (!isXonFile(name)) {
			return false;
		}
		return name.startsWith(XONFILELEADER + tag);
	} // end of isXonFileFromChain(String, String)



	private final static int getGenerationAt(String name, int at) {
		if (at < 0) {
			return -1;
		}
		if (at + 3 > name.length()) {
			return -1;
		}
		try {
			return Utilities.decodeInt(name.substring(at, at + 3));
		} catch (RetroTectorException e) {
			return -1;
		}
	} // end of getGenerationAt(String, int)
	
} // end of FileNamer
