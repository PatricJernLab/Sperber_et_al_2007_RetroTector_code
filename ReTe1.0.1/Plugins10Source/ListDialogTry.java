/*
* Copyright (©) 2004-2005, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 31/3 -05
*/
package plugins;

import retrotector.*;
import java.util.*;
import java.awt.*;

public class ListDialogTry extends Executor {

	String[ ] s = {"one", "two", "three", "four", "five"};
	
/**
* Standard Executor constructor.
*/
	public ListDialogTry() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
	} // end of constructor

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2005 01 28";
  } //end of version
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		ListDialog ld = new ListDialog((Frame) RetroTectorEngine.retrotector, s);
		String[ ] ss = ld.getResult();
		for (int i=0; i<ss.length; i++) {
			System.out.println(ss[i]);
		}
		ld.dispose();
		return "";
	}
}
