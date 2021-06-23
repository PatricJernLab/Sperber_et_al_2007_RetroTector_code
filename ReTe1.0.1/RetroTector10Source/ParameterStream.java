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

/**
* Superclass for files handling parameter files,
* textfiles consisting entirely of comments and parameters.
* Comments start with '{'.
* There are two kinds of parameters:
* 	Singleparameters on one line, with syntax:
* (optional blanks) (key string) (optional blanks) (:) (optional blanks) (contents string) (optional blanks)
* 	Multiparameters on several lines, with syntax:
* (optional blanks) (key string) (optional blanks) (::) (\n)
* (any number of lines)
* (::)
* 
*/
public abstract class ParameterStream {

/**
* Character separating key and value in single parameter.
*/
	public static final String SINGLEPARAMTERMINATOR = ":";

/**
* Terminates line starting and constitutes line ending
* multi parameter.
*/
	public static final String MULTIPARAMTERMINATOR = "::";

/**
* Leading character in comment line.
*/
	public static final String COMMENTMARKER = "{";

/**
* Key of parameter specifying excutor in ExecutorScripts = "Executor".
*/
	public static final String EXECUTORKEY = "Executor";
	
} // end of ParameterStream
