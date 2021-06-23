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

import java.util.*;
import java.io.*;
import java.sql.*;

/**
* Parent of all classes defining commands executable from Execute menu
* or scripts.
* Normally initialize() is called first, and then execute().
*/
public abstract class Executor extends ParameterUser {

// public static fields
// labels for parameters used by several Executors
/**
* =	". Not normally used interactively."
*/
	public static final String NOTINTERACT = ". Not normally used interactively.";

/**
* =	"ScriptPath".
*/
	public static final String SCRIPTPATHKEY = "ScriptPath";
	
/**
* Key to use for input file name = "InputFile".
*/
	public static final String INPUTFILEKEY = "InputFile";
	
/**
* Key to use for output file name = "OutputFile".
*/
	public final static String OUTPUTFILEKEY = "OutputFile";

/**
* Key to use for DNA file name = "DNAFile".
*/
	public static final String DNAFILEKEY = "DNAFile";

/**
* Key to use for putein file name = "PuteinFile".
*/
	public final static String PUTEINFILEKEY = "PuteinFile";
	
/**
* Key to use for bonus factor for conserved bases or acids = "ConservationFactor".
*/
	public static final String CFACTKEY = "ConservationFactor";

/**
* Key to use for SD factor in Motif thresholding = "SDFactor".
*/
	public static final String SDFACTORKEY = "SDFactor";

/**
* Key to use for primary or secondary strand = "Strand".
*/
	public static final String STRANDKEY = "Strand";
	
/**
* Key to use for selection indicator = "Selected".
*/
	public static final String SELECTEDKEY = "Selected";

/**
* Key to use for virus genus = "VirusGenus".
*/
	public static final String VIRUSGENUSKEY = "VirusGenus";

/**
* Key to use for gene = "Gene".
*/
	public static final String GENEKEY = "Gene";

/**
* Key to use for earliest acceptable sequence start = "FirstDNAStart".
*/
	public static final String FIRSTDNASTARTKEY = "FirstDNAStart";

/**
* Key to use for latest acceptable sequence start = "LastDNAStart"
*/
	public static final String LASTDNASTARTKEY = "LastDNAStart";

/**
* Key to use for earliest acceptable sequence end = "FirstDNAEnd".
*/
	public static final String FIRSTDNAENDKEY = "FirstDNAEnd";

/**
* Key to use for latest acceptable sequence end = "LastDNAEnd".
*/
	public static final String LASTDNAENDKEY = "LastDNAEnd";

/**
* Key to use for list of information about MotifHits = "HitInfo".
*/
	public static final String HITINFOKEY = "HitInfo";

/**
* Key to use for Chain number = "ChainNumber".
*/
	public static final String CHAINNUMBERKEY = "ChainNumber";

/**
* Key to use for information about breaks in Chain = "Breaks".
*/
	public static final String BREAKINFOKEY = "Breaks";

/**
* To indicate that both strands be used = "Both".
*/
	public static final String BOTH = "Both";

/**
* To indicate that primary strand be used = "Primary".
*/
	public static final String PRIMARY = "Primary";

/**
* To indicate that secondary strand be used = "Secondary"
*/
	public static final String SECONDARY = "Secondary";

/**
* Key to use for score threshold for Chains to apply ORFID = "ORFIDMinScore".
*/
	public static final String ORFIDMINSCOREKEY = "ORFIDMinScore";

/**
* Key for information about problems encountered by RetroVID = "Trouble".
*/
	public static final String TROUBLEKEY = "Trouble";

/**
* = "Yes"
*/
	public static final String YES = "Yes";

/**
* = "No"
*/
	public static final String NO = "No";

/**
* = "5LTR"
*/
	public static final String LTR5KEY = "5LTR";

/**
* = "3LTR"
*/
	public static final String LTR3KEY = "3LTR";

/**
* Key for font size = "FontSize".
*/
	public static final String FONTSIZEKEY = "FontSize";

/**
* Key for tolerance in LTR pairing = "LTRepTolerance".
*/
	public static final String LTREPTOLERANCEKEY = "LTRepTolerance";
	
/**
* Key for complete list of LTRHits in primary strand = "LTRHitsPrimary".
*/
	public static final String LTRHITSPRIMARYKEY = "LTRHitsPrimary";
	
/**
* Key for complete list of LTRHits in secondary strand = "LTRHitsSecondary".
*/
	public static final String LTRHITSSECONDARYKEY = "LTRHitsSecondary";
	
/**
* Key for list of master row names = "MasterNames".
*/
	public static final String MASTERNAMESKEY = "MasterNames";
  
/**
* Key for name of database to use = "Database".
*/
  public static final String DATABASEKEY = "Database";

/**
* Name of default database subdirectory = "Ordinary".
*/
  public static final String ORDINARYDATABASE = "Ordinary";
	
/**
* Key for debugging Yes or No = "Debugging".
*/
	public static final String DEBUGGINGKEY = "Debugging";

// Keys for ORFID algorithm
/**
* Key for basal score for nonaligned step = "NonAlignedScore".
*/
  public final static String NONALIGNEDSCOREKEY = "NonAlignedScore";

/**
* Key for score for stop codon  = "StopCodonFactor".
*/
	public final static String STOPCODONFACTORKEY = "StopCodonFactor";

/**
* Key for score for frame shift  = "FrameShiftFactor".
*/
  public final static String FRAMESHIFTPENALTYKEY = "FrameShiftFactor";

/**
* Key for score factor for ORF hexamers  = "ORFHexamerFactor".
*/
  public final static String ORFHEXAMERFACTORKEY = "ORFHexamerFactor";

/**
* Key for score factor for non-ORF hexamers  = "NonORFHexamerFactor".
*/
  public final static String NONORFHEXAMERFACTORKEY = "NonORFHexamerFactor";

/**
* Key for score factor for glycosylation sites  = "GlycosylationFactor".
*/
  public final static String GLYCSITEFACTORKEY = "GlycosylationFactor";

/**
* Key for bonus for position in long ORF  = "InORFBonus".
*/
  public final static String INORFBONUSKEY = "InORFBonus";

/**
* Key for factor to multiply Motif hit score by  = "MotifHitFactor".
*/
  public final static String MOTIFHITFACTORKEY = "MotifHitFactor";

/**
* Key for penalty for skipping full column in alignment  = "MasterSkipPenalty".
*/
	public final static String MASTERSKIPPENALTYKEY = "MasterSkipPenalty";
	
/**
* For XonID scripts. = "ChainStart".
*/
	public static final String CHAINSTARTKEY = "ChainStart";
	
/**
* For XonID scripts. = "ChainEnd".
*/
	public static final String CHAINENDKEY = "ChainEnd";

/**
* Used by XonID if ORFID does not seem finished. = "Not all ORFID scripts are swept".
*/
 	public static final String ORFIDSCRIPTSLEFTMESSAGE = "Not all ORFID scripts are swept";
	
/**
* = "SingleLTR_".
*/
 	public static final String SINGLELTRSUFFIX = "SingleLTR_";
	
/**
* = '.
*/
	public static final String BLIP = "'";

/**
* = ".
*/
	public static final String QUOTE = "\"";

/**
* = \.
*/
	public static final String BACKSLASH = "\\";

// public static methods
/**
* @param	name	Name of an Executor class.
* @return	An instance of that class.
*/
	public final static Executor getExecutor(String name) throws RetroTectorException {
		Class c = (Class) allExecutors.get(name);
    if (c == null) {
			throw new RetroTectorException("Executor", "Non-existent Executor", name);
    }
		Object o = null;
		try {
			o = c.newInstance();
		} catch (IllegalAccessException ae) {
			throw new RetroTectorException("Executor", "IllegalAccessException making Executor", name);
		} catch (InstantiationException ie) {
			throw new RetroTectorException("Executor", "InstantiationException making Executor", name);
		}
		return (Executor) o;
	} // end of getExecutor(String)
	
/**
* Utility to provide an integer with leading zeroes.
* @param	value 		The integer to zeroize.
* @param	maxvalue	The highest value possible in this context.
* @return	Value in String form with leading zeroes.
*/
	public final static String zeroLead(int value, int maxvalue) {
		StringBuffer result = new StringBuffer();
		while (maxvalue != 0) {
			result.append('0');
			maxvalue /= 10;
		}
		String s = String.valueOf(value);
		for (int i=0; i<s.length(); i++) {
			result.setCharAt(result.length() - 1 - i, s.charAt(s.length() - 1 - i));
		}
		return result.toString();
	} // end of zeroLead(int, int)
	
/**
* Ensures that a String ends with ".txt".
* @param	s	The String to test.
*	@return	The ".txt"-terminated String.
*/
	public final static String makeTextName(String s) {
		if (s.endsWith(FileNamer.TXTTERMINATOR) | s.endsWith(".TXT")) {
			return s;
		} else {
			return s + FileNamer.TXTTERMINATOR;
		}
	} // end of makeTextName(String)

/**
* Reads a DNA file.
* @param	name					The name (possibly without .txt suffix) of the file.
* @param	primaryStrand	True if primary DNA strand wanted.
* @return	DNA object as specified.
*/
	public final static DNA getDNA(String name, boolean primaryStrand) throws RetroTectorException {
		String fileName = makeTextName(name);
		File f = Utilities.getReadFile(RetroTectorEngine.currentDirectory(), fileName);
		return new DNA(f, primaryStrand);
	} // end of getDNA(String, boolean)
	
//protected static fields
/**
* Signal to abort SweepScripts as soon as possible.
*/
	protected static boolean abortSweepScripts = false;
	
// static methods
/**
* Collects Executor subclasses from the 'builtins' package and the 'plugins' directory.
* They are stored in allExecutors and returned on a Stack.
* @param	namelist	Names in this are collected first, and in that order.
* @return	A Stack of the Executor classes found.
*/
	final static Stack collectExecutors(String[ ] namelist) throws RetroTectorException {
    Stack result = new Stack();
		Class c;
		String na;
    int ind;
		if (namelist != null) {
			for (int i=0; i<namelist.length; i++) {
        na = namelist[i].trim();
        c = (Class) PluginManager.BUILTANDPLUGNAMES.get(na);
        if (c != null) {
          allExecutors.put(na, c);
					result.push(na);
				}
			}
		}
		for (int i=0; i<PluginManager.BUILTANDPLUGINS.length; i++) {
      c = PluginManager.BUILTANDPLUGINS[i];
      na = c.getName();
      ind = na.lastIndexOf(".");
      na = na.substring(ind + 1);
      if ((Utilities.subClassOf(c, "retrotector.Executor")) && (allExecutors.get(na) == null)) {
        allExecutors.put(na, c);
        result.push(na);
			}
		}
		return result;
	} // end of collectExecutors(String[ ])
	

// Stores all Executor subclasses with name as key
	private static final Hashtable allExecutors = new Hashtable();


// public methods
/**
* Reads parameters, from configuration file, script or parameter window.
* If read from script file, its name is added with SCRIPTPATHKEY.
* @param	script	The script to fetch from, or null.
*/
	public void initialize(ParameterFileReader script) throws RetroTectorException {
		getDefaults();
		if (script != null) { // read parameters from script
			runFlag = false;
			interactive = false;
			script.setTable(parameters);
			script.readParameters();
			script.close();
			parameters.put(SCRIPTPATHKEY, script.FILEPATH);
			runFlag = true;
		} else { // read parameters from window
			RetroTectorEngine.doParameterWindow(this, null);
			interactive = true;
		}
	} // end of initialize(ParameterFileReader)
	
/**
* Information about version date, to be displayed in parameter window. Should be overridden.
*/
  public String version() {
    return "No version";
  } // end of version()
	
/**
* Does the actual work.
* @return	Typically "", but possibly something more informative. Hardly used at present.
*/
	public abstract String execute() throws RetroTectorException;


/**
* String to be included by ExecutorThread in the error message caused
* by a Java Runtime Exception.
*/
	public String runtimeExceptionComment() {
		return "running " + Utilities.className(this);
	} // end of runtimeExceptionComment()

/**
* @param	parameterName	Name of a parameter.
* @return	The current default value of that parameter.
*/
	public final String defaultValueOf(String parameterName) {
		getDefaults();
		return getString(parameterName, null);
	} // end of defaultValueOf(String)
	

/**
*	For signalling that initialization was successful. OK to execute if true.
*/
	public boolean runFlag = true;
	

// protected fields
/**
*	Explanations for use by parameter window.
*/
	protected Hashtable explanations;

/**
*	Parameter keys in correct order.
*/
	protected Stack orderedkeys;

/**
*	True if command was started from menu.
*/
	protected boolean interactive = false;
	

// protected methods
/**
* Adds useful information as comments to a ParameterFileWriter.
* @param	theWriter	The ParameterFileWriter in question.
*/
	protected final void writeInfo(ParameterFileWriter theWriter) throws RetroTectorException {
		theWriter.writeComment("Created by " + className() + " with parameters");
		theWriter.listSingleParameters(parameters);
		Enumeration k = PluginManager.BUILTANDPLUGNAMES.keys();
		if (k.hasMoreElements()) {
			theWriter.writeComment("  and plugins");
			String s;
			Class c;
			while (k.hasMoreElements()) {
				s = (String) k.nextElement();
				if (s.indexOf("$") < 0) {
					c = (Class) PluginManager.BUILTANDPLUGNAMES.get(s);
					if (c.getPackage().getName().equals(PluginManager.PLUGINSPACKAGENAME)) {
						theWriter.writeComment(s);
					}
				}
			}
		}
	} // end of writeInfo(ParameterFileWriter)
	
/**
* Advances the progress indicator.
* Checks whether the abort flag is set, and if so, stops the current Thread.
*/
	protected final void showProgress() throws RetroTectorException {
		RetroTectorEngine.showProgress();
	} // end of showProgress()

/**
* Shows any information in the information field of the main window.
* @param	theInfo	The info.
*/
	protected final void showInfo(String theInfo) {
		RetroTectorEngine.setInfoField(theInfo);
	} // end of showInfo(String)
	
/**
* Interrupts, throwing a RetroTectorException with 1 parameter.
*/
	protected final void haltError(String par1)
				throws RetroTectorException {
		throw new RetroTectorException(className(), par1);
	}
	
/**
* Interrupts, throwing a RetroTectorException with 2 parameters.
*/
	protected final void haltError(String par1, String par2)
				throws RetroTectorException {
		throw new RetroTectorException(className(), par1, par2);
	}
	
/**
* Interrupts, throwing a RetroTectorException with 3 parameters.
*/
	protected final void haltError(String par1, String par2, String par3)
				throws RetroTectorException {
		throw new RetroTectorException(className(), par1, par2, par3);
	}
	
/**
* Interrupts, throwing a RetroTectorException with 4 parameters.
*/
	protected final void haltError(String par1, String par2, String par3, String par4)
				throws RetroTectorException {
		throw new RetroTectorException(className(), par1, par2, par3, par4);
	}

/**
* Interrupts, throwing a RetroTectorException with 5 parameters.
*/
	protected final void haltError(String par1, String par2, String par3, String par4, String par5)
				throws RetroTectorException {
		throw new RetroTectorException(className(), par1, par2, par3, par4, par5);
	}

/**
* Handles RetroTectorException thrown by a called method.
* @param	e That RetroTectorException
*/
	protected final void haltError(RetroTectorException e)
				throws RetroTectorException {
// replace sender
		e.THESTRINGS[0] = e.THESTRINGS[0] + " through " + className();
		throw new RetroTectorException(e.THESTRINGS);
	}

/**
* Handles RetroTectorException thrown by a called method, inserting a line.
* @param	firstLine	Line to insert after sender.
* @param	e					That RetroTectorException
*/
	protected final void haltError(String firstLine, RetroTectorException e)
				throws RetroTectorException {
// replace sender
		String[ ] ss = new String[e.THESTRINGS.length + 1];
		ss[0] = e.THESTRINGS[0] + " through " + className();
		ss[1] = firstLine;
		for (int i=1; i<e.THESTRINGS.length; i++) {
			ss[i + 1] = e.THESTRINGS[i];
		}
		throw new RetroTectorException(ss);
	}

/**
* Handles SQLException thrown by a called method, folding the line.
* @param	e That SQLException
*/
	protected final void haltError(SQLException e)
				throws RetroTectorException {
		throw new RetroTectorException(className(), e);
	}

} // end of Executor