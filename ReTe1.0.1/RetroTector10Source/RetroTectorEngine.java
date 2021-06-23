/*
* Copyright (©) 2000-2010, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0.2
*
* @author  G. Sperber
* @version 17/1 -10
* Beautified 17/1 -10
*/

package retrotector;

import java.io.*;
import java.util.*;
import builtins.*;

/**
* Entry class for awt-less variant. Mainly interface to RetroTector.
*/
public class RetroTectorEngine {

/**
* For thread-safe handling of RetroTectorExceptions.
*/
	static class ErrorDisplayer extends Thread {
	
		private RetroTectorException rse;
		private int level;
		
/**
* Constructor.
* @param	e	The RetroTectorException to display.
* @param	l	The error level.
*/
	public ErrorDisplayer(RetroTectorException e, int l) {
			rse = e;
			level = l;
		} // end of ErrorDisplayer.constructor(RetroTectorException, int)

/**
* As required by Thread.
*/
		public void run() {
			String ostring = "ERROR!!!";
			if (level == WARNINGLEVEL) {
				ostring = "Warning!!";
			} else if (level == NOTICELEVEL) {
				ostring = "Notice!";
			}
			
	// write to log file
			if (latestErrorLog != null) {
				latestErrorLog.println();
				Date da = new Date();
				latestErrorLog.println(da.toString());
				latestErrorLog.println(ostring);
				for (int i=0; i<rse.getSize(); i++) {
					latestErrorLog.println(rse.messagePart(i));
				}
			}
					
	// show in main window if possible, otherwise in text window
			if (retrotector == null) {
				Utilities.outputString(ostring);
				for (int i=0; i<rse.getSize(); i++) {
					Utilities.outputString(rse.messagePart(i));
				}
				Utilities.outputString("");
			} else {
				retrotector.displayerror(rse, level);
			}
			
		} // end of  ErrorDisplayer.run()
		
	} // end of ErrorDisplayer


/**
* Gets hold of the .jar file.
*/
	static {
		String cp = System.getProperty("java.class.path");
		System.out.println("java.class.path is:" + cp);
		int jarind = cp.indexOf(".jar");
		JARFILENAME = cp.substring(0, jarind + 4);
	} // end of static initializer


/**
* Name of RetroTector...jar file.
*/
	public static final String JARFILENAME;
  
/**
* Name of directory with auxiliary information (="Database").
*/
	public static final String DATABASENAME = "Database";

/**
* Name of file in Database with setup information (="Configuration.txt").
*/
	public static final String CONFIGNAME = "Configuration.txt";

/**
* Name of file in Database with Properties (="Properties.txt").
*/
	public static final String PROPERTIESNAME = "Properties.txt";

/**
* First part of name of file in Database with error information (="LatestErrorLog_").
*/
	public static final String LOGFILENAME = "LatestErrorLog_";

/**
* Message when RetroTector is idle (="RetroTector awaits command").
*/
	public static final String READYMESSAGE =	"RetroTector awaits command";

/**
* Information about which version this is.
*/
	public static final String VERSIONSTRING = "RetroTector version 1.0.2 100116";
	
/**
* Indicates an error to displayError.
*/
	public static final int ERRORLEVEL = 0;
	
/**
* Indicates a warning to displayError.
*/
	public static final int WARNINGLEVEL = 1;
	
/**
* Indicates a notice to displayError.
*/
	public static final int NOTICELEVEL = 2;
	
/**
*	True if unpaired LTRs are to be included in Chains (=true).
*/
	public static final boolean USESINGLELTRS = true;
	
/**
* Main window.
*/
	public static RetroTectorInterface retrotector = null;
  
/**
* Thread where interactive commands are executed.
*/
	static ExecutorThread currentThread = null;
	
/**
* For information from Configuration.txt.
*/
	public static Hashtable configurationTable = null;

/**
* String with copyright claim.
*/
	public static final String COPYRIGHTSTRING = VERSIONSTRING + ". Copyright (©) G. O. Sperber & J. Blomberg 2000-2008";
	
/**
* Key of parameter in Configuration.txt showing initial directory (="WorkingDirectory").
*/
 	public static final String WORKINGDIRECTORYKEY =	"WorkingDirectory";
	
/**
* Key of parameter in Configuration.txt showing order in Execute menu	(="Executormenu").
*/
	public static final String EXECMENUKEY = "Executormenu";

/**
* Key of parameter in Configuration.txt indicating cluster mode	(="ClusterMode").
*/
	public static final String CLUSTERMODEKEY = "ClusterMode";

/**
* The Executor script selected.
*/
	static File currentScript;

  private static Database currentDatabase = null;
	private static boolean executing; // flag indicating executor is running and menu commands disabled
	private static File currentDirectory;
	private static PrintWriter latestErrorLog; // for error message logging
	
	private static Properties rtProperties; // to remember from run to run
	
	private static boolean clusterMode = false;

/**
* @return True if optimization for cluster use is requested.
*/
	public final static boolean getClusterMode() { 
		return clusterMode;
	} // end of getClusterMode()
	
/*
* Creates Utilities.databaseDirectory and configurationTable if not done.
*/
	final static void getConfiguration() throws RetroTectorException {
		if (configurationTable == null) {
			Utilities.databaseDirectory = new File(DATABASENAME);
			if (!Utilities.databaseDirectory.exists()) {
				throw new RetroTectorException("RetroTectorEngine", DATABASENAME + " directory does not exist");
			}		
			configurationTable = new Hashtable();
			Utilities.outputString("Reading " + CONFIGNAME);
			File configFile = new File(Utilities.databaseDirectory, CONFIGNAME);
			ParameterFileReader configReader = new ParameterFileReader(configFile, configurationTable);
			configReader.readParameters();
			configReader.close();
		}
	} // end of getConfiguration()

/**
* Entry point.
* Parameters may be:
*		L:|first part of file name|	Log file name will be this + version number + .txt, if first parameter
*		|Executor name|							That executor is executed
*		quit												RetroTector quits
*		D:|directory name|					New working directory is set
*		E:|script name|							That executor script is executed
*		F:|file name|								Parameters as above are picked from file (name should be path
*														unless file is in same directory as RetroTector.jar)
* @param	strings	Parameters, as above
*/
	public static void main(String[ ] strings) {
	
		try {
// get setup info
			getConfiguration();
			try {
				if ((strings.length > 0) && strings[0].startsWith("L:")) {
					latestErrorLog = new PrintWriter(new FileWriter(Utilities.uniqueFile(Utilities.databaseDirectory, strings[0].substring(2), FileNamer.TXTTERMINATOR)));
				} else {
					latestErrorLog = new PrintWriter(new FileWriter(Utilities.uniqueFile(Utilities.databaseDirectory, LOGFILENAME, FileNamer.TXTTERMINATOR)));
				}
			} catch (IOException ioe) {
				throw new RetroTectorException("RetroTectorEngine", "Could not create LatestErrorLog");
			}
			toLogFile("Starting RetroTector session at " + (new Date()).toString());
			rtProperties = new Properties();
			try {
				InputStream prStream = new FileInputStream(new File(Utilities.databaseDirectory, PROPERTIESNAME));
				rtProperties.load(prStream);
				prStream.close();
			} catch (IOException ioe) {
				displayError(new RetroTectorException("RetroTectorEngine", "Properties file could not be read", "It may not exist"), 2);
			}

		} catch (RetroTectorException rse) {
			displayError(rse);
			return;
		}
		Utilities.outputString("Collecting Executors");

// collect Executor subclasses from 'executors' directory
		try {
			if (retrotector == null) {
				Executor.collectExecutors((String[ ]) configurationTable.get(EXECMENUKEY));
			}
			setCurrentDirectory(new File(initialDirectory()), "swps");
		} catch (RetroTectorException rse) {
			displayError(rse);
		}

		if (configurationTable.containsKey(CLUSTERMODEKEY)) {
			clusterMode = ((String) configurationTable.get(CLUSTERMODEKEY)).equals(Executor.YES);
		}

// collect Brooms
		try {
			Broom.collectBrooms();
		} catch (RetroTectorException rse) {
			displayError(rse);
		}


/*
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Calendar lastcal = Calendar.getInstance();
		lastcal.set(2007, 0, 3);
		if (cal.after(lastcal)) {
			System.out.println("This RetroTector has passed its expiration date");
			System.exit(0);
		}
*/
		if (retrotector == null) {
			System.out.println(COPYRIGHTSTRING);
		}
    
		try {
      setInfoField("Reading Motif classes");
      Motif.collectMotifClasses();
      currentDatabase = Database.getDatabase(Executor.ORDINARYDATABASE);
		} catch (RetroTectorException rse) {
			displayError(rse);
		}
      
		AcidMatrix.refreshAcidMatrix(2f);
		BaseMatrix.refreshBaseMatrix(2f);
// react to parameters, if any
		if (strings != null) {
			for (int si=0; si<strings.length; si++) {
				try {
					interpretParam(strings[si], false);
				} catch (RetroTectorException rse) {
					displayError(rse);
				}
			}
		}
		setInfoField("");
		setExecutorField(READYMESSAGE, "swps");
		
// set up for commands from standard input
		BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
		String inLine = null;
		for (;;) {
			try {
				inLine = inReader.readLine();
			} catch (IOException e) {
			}
			if (inLine != null) {
				try {
					System.out.println(inLine + " received");
					interpretParam(inLine, true);
					System.out.println(inLine + " executed");
				} catch (RetroTectorException rte) {
					System.out.println(inLine + " failed");
				}
			}
		}

	} // end of main(String[ ])

/**
* @param	s	Message to display in infoField.
*/
	public final static void setInfoField(String s) {
		if (retrotector != null) {
			retrotector.setinfoField(s);
		}
	} // end of setInfoField(String)
	
	private static long lastProgTime = -1l; // to pace progPanel

/**
* Step progress indicator if more than 0.5 sec since last.
* Also checks abort flag and makes current thread give up control.
*/
	public final static void showProgress() throws RetroTectorException {
		if ((currentThread != null) && (currentThread.abortFlag)) {
			currentThread = null;
			Executor.abortSweepScripts = true;
			throw new RetroTectorException("RetroTectorEngine", "Aborted, presumably by you.");
		}
		if ((System.currentTimeMillis() - lastProgTime) < 500) {
			return;
		}
		lastProgTime = System.currentTimeMillis();
		if (retrotector != null) {
			retrotector.showprogress();
		}
		Thread.yield();
	} // end of showProgress()
		
/**
* Changes	the directory currently specified in main window directory field.
*	@param	theDirectory	The new directory.
* @param	password			Known only to SweepScripts.
* @return	True if successful.
*/
	public final static boolean setCurrentDirectory(File theDirectory, String password) throws RetroTectorException {
		if (password.equals("swps")) {
			currentDirectory = theDirectory;
		} else {
			return false;
		}
		if (retrotector != null) {
			return retrotector.setcurrentDirectory(theDirectory, password);
		} else {
			return true;
		}
	} // end of setCurrentDirectory(File, String)
	
/**
* @return The directory currently specified in main window directory field, or null if not valid.
*/
	public final static File currentDirectory() {
		if ((currentDirectory == null) || (!currentDirectory.isDirectory())) {
			return null;
		}
		return currentDirectory;
	} // end of currentDirectory()

/**
* Changes	the script file currently specified in main window script field.
*	@param	scriptFile	The new File.
* @param	password		Known only to SweepScripts.
* @return	True if successful.
*/
	public final static boolean setScriptField(File scriptFile, String password) {
		currentScript = scriptFile;
		if (retrotector != null) {
			return retrotector.setscriptField(scriptFile, password);
		} else {
			return false;
		}
	} // end of setScriptField(File, String)

/**
* Displays any message in main window executor field.
* @param	s					Message to display.
* @param	password	Known only to SweepScripts.
* @return	True if successful.
*/
	public final static boolean setExecutorField(String s, String password) {
		if (retrotector != null) {
			return retrotector.setexecutorField(s, password);
		} else {
			return false;
		}
	} // end of setExecutorField(String, String)

/**
* Signal that an executor is running, or none.
* @param	ex	The executor, or null.
*/
	final static void setExecuting(Executor ex) {
		if (ex == null) {
			executing = false;
		} else {
			executing = true;
		}
		if (retrotector != null) {
			retrotector.setexecuting(ex);
		}
	} // end of setExecuting(Executor)

/**
*	@return	True if an executor is at work.
*/
	public final static boolean isExecuting() {
		return executing;
	} // end of isExecuting()

	private static String lastLoggedLine = "";
	
/**
* Outputs a String to log file and, if there is no RetroTector, to the text window.
* @param	s	String to output.
*/
  public final static void toLogFile(String s) {
		lastLoggedLine = s;
    if (latestErrorLog != null) {
			latestErrorLog.println(s);
    }
		if (retrotector == null) {
			Utilities.outputString(s);
		}
  } // end of toLogFile()

/**
* @return	The Database currently in use.
*/
  public final static Database getCurrentDatabase() {
    return currentDatabase;
  } // end of getCurrentDatabase()
  
/**
* Does nothing if name refers to the current Database. Otherwise creates a new Database.
* @param	name	The name of the Database directory.
*/
  public final static void setCurrentDatabase(String name) throws RetroTectorException {
    currentDatabase = Database.getDatabase(name);
  } // end of setCurrentDatabase
	
/**
* Call it a day.
*/
	public final static void doQuit() {
		try {
			OutputStream proStream = new FileOutputStream(new File(Utilities.databaseDirectory, PROPERTIESNAME));
			rtProperties.store(proStream, null);
			proStream.close();
		} catch (IOException ioe) {
			displayError(new RetroTectorException("RetroTectorEngine", "Trouble writing to the Propeties"));
		}
		toLogFile("Total number of single LTRs created by LTRID: " + LTRID.singlesCount);
		toLogFile("Ending RetroTector session at " + (new Date()).toString());
		if (latestErrorLog != null) {
			latestErrorLog.close();
		}
		System.exit(0);
	} // end of doQuit()
	
/**
* @param	key	The label of a property.
* @return The value of the property.
*/
	public final static String getProperty(String key) {
		return rtProperties.getProperty(key);
	} // end of getProperty(String)
	
/**
* @param	key	The key of a property.
* @param	value	A value to give that property.
*/
	public final static void setProperty(String key, String value) {
		rtProperties.setProperty(key, value);
	} // en of setProperty(String, String)


/**
* Emit the system warning sound.
*/
	public final static void beep() {
		if (retrotector != null) {
			retrotector.beep();
		}
	} // end of beep()
	
// Working directory, as far as possible
	static final String initialDirectory() {
		if (configurationTable.containsKey(WORKINGDIRECTORYKEY)) {
			String s = (String) configurationTable.get(WORKINGDIRECTORYKEY);
			if ((!s.equals("none")) & (!s.equals(""))) {
				return s;
			}
		}
		return System.getProperty("user.dir");
	} // end of initialDirectory()
	
// handles start parameters, see main()
	private final static void interpretParam(String thePar, boolean newThread) throws RetroTectorException {
	
		int ind;
		String part1 = null;
		String part2 = null;

		System.err.println();
		System.err.println("      Executing line command");
		System.err.println(thePar);
		if (thePar.equalsIgnoreCase("Quit")) {
			System.err.println("Error count was " + errorCount);
			doQuit();
		}
		
		ind = thePar.indexOf(':');
		if (ind > 0) {
			part1 = thePar.substring(0,ind).trim();
			part2 = thePar.substring(ind + 1).trim();
			if (part1.equalsIgnoreCase("D")) {
				setCurrentDirectory(new File(part2), "swps");
			} else if (part1.equalsIgnoreCase("E")) {
				File f = new File(currentDirectory(), part2);
				setScriptField(f, "swps");
				setExecutorField("Autoexecuting " + part2, "swps");
				executeScript(false);
				setExecutorField(READYMESSAGE, "swps");
			} else if (part1.equalsIgnoreCase("F")) {
				Stack linestack = new Stack();
				try {
					BufferedReader br = new BufferedReader(new FileReader(new File(part2)));
					String line;
					while ((line = br.readLine()) != null) {
						if (line.trim().length() > 0) {
							linestack.push(line);
						}
					}
				} catch (IOException ioe) {
					displayError(new RetroTectorException("RetroTector", "Could not read file", part2));
				}
				String[ ] sss = new String[linestack.size()];
				linestack.copyInto(sss);
				for (int sssi=0; sssi<sss.length; sssi++) {
					interpretParam(sss[sssi], false);
				}
			} else if (part1.equalsIgnoreCase("L")) {
			} else {
				displayError(new RetroTectorException("RetroTector", "Parameter syntax error", thePar));
			}
		} else if (thePar.length() == 0) {
		} else {
			setExecutorField("Autoexecuting " + thePar, "swps");
			executeClass(thePar, newThread);
			setExecutorField(READYMESSAGE, "swps");
		}
		System.err.println();
		System.err.println("   Executed line command");
		System.err.println(thePar);
		System.err.println("   Last logged line was");
		System.err.println(lastLoggedLine);
	} // end of interpretParam(String, boolean)

/**
* Standard way to react to RetroTectorExceptions showing errors. 
* Displays it in the error panel if possible, otherwise just prints it.
* @param	rse	The RetroTectorException to display.
*/
	public final static void displayError(RetroTectorException rse) {
		displayError(rse, ERRORLEVEL);
	} // end of displayError(RetroTectorException)
	
	private static long errorCount = 0;
	
/**
* Standard way to react to RetroTectorExceptions. 
* Displays it in the error panel if possible, otherwise just prints it.
* @param	rse		The RetroTectorException to display.
* @param	level	0=error; 1=warning; 2=notice.
*/
	public final static void displayError(RetroTectorException rse, int level) {
		
		if (level == ERRORLEVEL) {
			errorCount++;
		}
		ErrorDisplayer r = new ErrorDisplayer(rse, level);
		r.start();
		
	} // end of displayError(RetroTectorException, int)
	
/**
*	Shows a ParameterWindow if RetroTector is present.
*	@param	ex	The Executor whose ParameterWindow is to be shown.
* @param comp	A JComponent to add at the bottom.
*/
	public static void doParameterWindow(Executor ex, Object comp) {
		if (retrotector != null) {
			retrotector.doparameterWindow(ex, comp);
		}
	} // end of doParameterWindow(Executor, Object)
	
/**
*	Shows a DNAWindow if RetroTector is present.
*	@return	The DNA input to the DNAWindow, or null.
*/
	public static DNA doDNAWindow() throws RetroTectorException {
		if (retrotector != null) {
			return retrotector.doDNAwindow();
		}
		return null;
	} // end of doDNAWindow()
	
/**
* Shows a confirm dialog.
* @param	question	A String to show.
* @return	true if the Yes button was clicked, or if RetroTector is absent.
*/
	public static boolean doQuestion(String question) {
		if (retrotector != null) {
			return retrotector.doquestion(question);
		}
		return true;
	} // end of doQuestion(String)
	
/**
* Shows a PadDialog.
* @param	length	Current length of DNA.
* @return	null if the No button was clicked, or if RetroTector is absent, parameters otherwise.
*/
	public static String[ ] doPadQuestion(int length) throws RetroTectorException {
		if (retrotector != null) {
			return retrotector.dopadQuestion(length);
		}
		return null;
	} // end of doPadQuestion(int)
	
/**
*	@param	clName		Name of an Executor class to execute.
*	@param	newThread	True if it should execute in a Thread of its own.
*/
	final static void executeClass(String clName, boolean newThread) throws RetroTectorException {
		try {
			Executor ex = Executor.getExecutor(clName);
			if (newThread) { // called from actionPerformed
				if (currentThread != null) {
					throw new RetroTectorException("RetroTectorEngine", "Another thread is already running");
				}
				currentThread = new ExecutorThread(ex, null);
				currentThread.start();
			} else { // called from interpretParam
				Hashtable h = new Hashtable();
				ParameterFileReader pfr = new ParameterFileReader(h); // dummy
				ex.initialize(pfr);
				if (ex.runFlag) {
					ex.execute();
				}
			}
		} catch (RetroTectorException rse) {
			displayError(rse);
		} catch (RuntimeException e) {
			RetroTectorEngine.displayError(
					new RetroTectorException(clName, "A Java RuntimeException occurred",
						"There should be more information in the text window")
				);
			RetroTectorEngine.toLogFile(e.toString());
			throw e;
		} finally {
			RetroTectorEngine.setExecuting(null);
		}
	} // end of executeClass(String, boolean)
	
/**
*	Executes current script.
*	@param	newThread	True if it should execute in a Thread of its own.
*/
	final static void executeScript(boolean newThread) {
		ExecutorScriptReader theScript = null;
		if (!currentDirectory().getPath().equalsIgnoreCase(currentScript.getParent())) {
			displayError(new RetroTectorException("RetroTector", "Script not in current directory", currentDirectory().getPath(), currentScript.getParent()));
			return;
		}
		try {
			theScript = new ExecutorScriptReader(currentScript);
			theScript.doExecute(newThread);
		} catch (RetroTectorException rse) {
			displayError(rse);
		}
		return;
	}	// end of executeScript(boolean)
	
/**
* @return	True if this is a Macintosh.
*/
	public final static boolean isMacOS() {
		String s = System.getProperty("os.name");
		return s.startsWith("Mac OS");
	} // end of isMacOS()
	
} // end of RetroTectorEngine

