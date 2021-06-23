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

import java.io.*;
import java.util.*;

/**
* To allow RetroTectorEngine to access RetroTector only when it is present.
*/
interface RetroTectorInterface {

/**
* Changes	the script file currently specified in main window script field.
*	@param	scriptFile	The new File.
* @param	password		Known only to SweepScripts.
* @return	True if successful.
*/
	boolean setscriptField(final File scriptFile, String password);
			
/**
* Displays any message in main window executor field.
* @param	s					Message to display.
* @param	password	Known only to SweepScripts.
* @return	True if successful.
*/
	boolean setexecutorField(final String s, String password);

/**
* Changes	the directory currently specified in main window directory field.
*	@param	theDirectory	The new directory.
* @param	password			Known only to SweepScripts.
* @return	True if successful.
*/
	boolean setcurrentDirectory(final File theDirectory, String password) throws RetroTectorException;
	
/**
* Signal that an executor is running, or none. Also empties infoField.
* @param	ex	The executor, or null.
*/
	void setexecuting(final Executor ex);
		
/**
* Standard way to react to RetroTectorExceptions. 
* Displays it in the error panel if possible, otherwise just prints it.
* @param	rse		The RetroTectorException to display.
* @param	level	0=error; 1=warning; 2=notice.
*/
	void displayerror(RetroTectorException rse, int level);	
	
/**
* Emit standard warning sound.
*/
	void beep();
  
/**
* @param	s	Message to display in infoField.
*/
	void setinfoField(final String s);
		
/**
* Step progress indicator.
*/
	void showprogress() throws RetroTectorException;
			
/**
* Display a ParameterWindow.
*	@param	ex	The Executor whose ParameterWindow is to be shown.
* @param comp	A JComponent to add at the bottom.
*/
	void doparameterWindow(Executor ex, Object comp);
	
/**
* Display a DNAWindow.
*	@return	The DNA input to the DNAWindow.
*/
	DNA doDNAwindow() throws RetroTectorException;
	
/**
* Shows a confirm dialog.
* @param	question	A String to show.
* @return	true if the Yes button was clicked.
*/
	boolean doquestion(String question);
		
/**
* Shows a PadDialog.
* @param	length	Current length of DNA.
* @return	null if the No button was clicked, parameters otherwise.
*/
	String[ ] dopadQuestion(int length) throws RetroTectorException;	

} // end of RetroTectorInterface
