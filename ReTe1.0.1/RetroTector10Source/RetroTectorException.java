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

import java.sql.*;
import java.util.*;

/**
* Internal exception class.
* It carries a number of strings with it.
* Normally, the first is the name of the class where
* the trouble occurred.
*/
public class RetroTectorException extends Exception {

/**
*	These sendError throw a RetroTectorException whose first string is the name of thrower.
*/
	public final static void sendError(Object thrower) throws RetroTectorException {
		throw new RetroTectorException(Utilities.className(thrower));
	}
	
	public final static void sendError(Object thrower, String par1) throws RetroTectorException {
		throw new RetroTectorException(Utilities.className(thrower), par1);
	}
	
	public final static void sendError(Object thrower, String par1, String par2) throws RetroTectorException {
		throw new RetroTectorException(Utilities.className(thrower), par1, par2);
	}
	
	public final static void sendError(Object thrower, String par1, String par2, String par3) throws RetroTectorException {
		throw new RetroTectorException(Utilities.className(thrower), par1, par2, par3);
	}
	
	public final static void sendError(Object thrower, String par1, String par2, String par3, String par4) throws RetroTectorException {
		throw new RetroTectorException(Utilities.className(thrower), par1, par2, par3, par4);
	}
	
	public final static void sendError(Object thrower, String par1, String par2, String par3, String par4, String par5) throws RetroTectorException {
		throw new RetroTectorException(Utilities.className(thrower), par1, par2, par3, par4, par5);
	}
	
	public final static void sendError(Object thrower, String[ ] parameters) throws RetroTectorException {
		throw new RetroTectorException(Utilities.className(thrower), parameters);
	}

	public final static void sendError(Object thrower, SQLException e) throws RetroTectorException {
		throw new RetroTectorException(Utilities.className(thrower), e);
	}
	
/**
* @param	oldThrower		An Object, possibly sender of theException.
* @param	newThrower		A String, to replace oldThrower in theException.
* @param	theException	A RetroTectorException.
* Throws theException, with sender (if oldThrower) replaced by newThrower.
*/
	public final static void sendError(Object oldThrower, String newThrower, RetroTectorException theException) throws RetroTectorException {
		if ((oldThrower == null) || (theException.THESTRINGS[0].equals(Utilities.className(oldThrower)))) {
			theException.THESTRINGS[0] = newThrower;
		}
		throw theException;
	} // end of sendError(Object, String, RetroTectorException)

/**
* The strings reporting the exception.
*/
	final String[ ] THESTRINGS;
	
/**
* All constructors specify the strings one way or another.
*/
	public RetroTectorException(String sender) {
		THESTRINGS = new String[1];
		THESTRINGS[0] = sender;
	}
	
	public RetroTectorException(String sender, String par1) {
		THESTRINGS = new String[2];
		THESTRINGS[0] = sender;
		THESTRINGS[1] = par1;
	}
	
	public RetroTectorException(String sender, String par1, String par2) {
		THESTRINGS = new String[3];
		THESTRINGS[0] = sender;
		THESTRINGS[1] = par1;
		THESTRINGS[2] = par2;
	}
	
	
	public RetroTectorException(String sender, String par1, String par2, String par3) {
		THESTRINGS = new String[4];
		THESTRINGS[0] = sender;
		THESTRINGS[1] = par1;
		THESTRINGS[2] = par2;
		THESTRINGS[3] = par3;
	}
	
		
	public RetroTectorException(String sender, String par1, String par2, String par3, String par4) {
		THESTRINGS = new String[5];
		THESTRINGS[0] = sender;
		THESTRINGS[1] = par1;
		THESTRINGS[2] = par2;
		THESTRINGS[3] = par3;
		THESTRINGS[4] = par4;
	}

		
	public RetroTectorException(String sender, String par1, String par2, String par3, String par4, String par5) {
		THESTRINGS = new String[6];
		THESTRINGS[0] = sender;
		THESTRINGS[1] = par1;
		THESTRINGS[2] = par2;
		THESTRINGS[3] = par3;
		THESTRINGS[4] = par4;
		THESTRINGS[5] = par5;
	}

	public RetroTectorException(String sender, String[ ] parameters) {
		THESTRINGS = new String[parameters.length + 1];
		THESTRINGS[0] = sender;
		for (int i=0; i<parameters.length; i++) {
			THESTRINGS[i+1] = parameters[i];
		}
	}
	
	public RetroTectorException(String[ ] parameters) {
		THESTRINGS = new String[parameters.length];
		for (int i=0; i<parameters.length; i++) {
			THESTRINGS[i] = parameters[i];
		}
	}

	public RetroTectorException(String sender, SQLException e) {
		String s = e.toString();
		Stack ss = new Stack();
		ss.push(sender);
		int marker;
		while (s.length() > 100) {
			marker = s.indexOf(" ", 100);
			if (marker >= 0) {
				ss.push(s.substring(0, marker).trim());
				s = s.substring(marker + 1).trim();
			} else {
				ss.push(s);
				s = "";
			}
		}
		if (s.length() > 0) {
			ss.push(s);
		}
		THESTRINGS = new String[ss.size()];
		ss.copyInto(THESTRINGS);
	}
	
/**
* @return	The number of strings in this.
*/
	public final int getSize() {
		return THESTRINGS.length;
	} // end of getSize()
	
/**
* @param	i	Index of one particular string.
* @return	That string, or "".
*/
	public final String messagePart(int i) {
		if ((i<0) | (i>=THESTRINGS.length)) {
			return "";
		} else {
			return THESTRINGS[i];
		}
	} // end of messagePart(int)
	
/**
* @return	The first line in the message.
*/
	public final String getSender() {
		if ((THESTRINGS == null) || (THESTRINGS.length == 0)) {
			return null;
		} else {
			return THESTRINGS[0];
		}
	} // end of getSender()
	
/**
* Prints the contents through Utilities.outputString().
*/	
	public final void printOut() {
		Utilities.outputString("Error message from " + THESTRINGS[0] + ":");
		for (int i=1; i<THESTRINGS.length; i++) {
			Utilities.outputString(THESTRINGS[i]);
		}
	} // end of printOut()
	
/**
* Prints the contents to the error log file.
*/	
	public final void toLogFile() {
		RetroTectorEngine.toLogFile("Error message from " + THESTRINGS[0] + ":");
		for (int i=1; i<THESTRINGS.length; i++) {
			RetroTectorEngine.toLogFile(THESTRINGS[i]);
		}
	} // end of toLogFile()
	
} // end of RetroTectorException
