/*
* Copyright (©) 2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector
*
* @author  G. Sperber
* @version 4/12 -06
* Beautified 4/12 -06
*/
package plugins;

import retrotector.*;
import builtins.*;

import java.io.*;
import java.sql.*;
import java.util.*;


/**
* Executor which makes a histogram from an SQL database.
*<PRE>
*     Parameters:
*
*   Database
* Database to use.
* Default: "".
*
*   Table
* Table in database to use.
* Default: "Chains".
*
*   Column
* Column to use.
* Default: "Score".
*
*		Extension
* To be added at end of SQL commands.
*	Default: " and Overlapper = 'NULL'"
*
*		BinSize
* Width of bins.
* Default: 100.
*
*</PRE>	
*/
public class GenomeHistogram extends SQLUser {

	
	private String dbname;
	private String tablename;
	private String columnname;
	private String extension;
	
	private String tab = "\t";
	
	private float binSize;
		
/**
* Standard Executor constructor.
*/
	public GenomeHistogram() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put("Database", "");
		explanations.put("Database", "Database to use");
		orderedkeys.push("Database");
		parameters.put("Table", "Chains");
		explanations.put("Table", "Table in database to use");
		orderedkeys.push("Table");
		parameters.put("Column", "Score");
		explanations.put("Column", "Column to use");
		orderedkeys.push("Column");
		parameters.put("Extension", " and Overlapper = 'NULL'");
		explanations.put("Extension", "To be added at end of SQL commands");
		orderedkeys.push("Extension");
		parameters.put("BinSize", "100");
		explanations.put("BinSize", "Width of bins");
		orderedkeys.push("BinSize");
	} // end of constructor

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 11 30";
  } // end of version
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		dbname = getString("Database", "");
		tablename = getString("Table", "Chains");
		columnname = getString("Column", "Score");
		extension = getString("Extension", " and Overlapper = 'NULL'");
		binSize = getFloat("BinSize", 100.0f);
		
		connect();
		setDatabase(dbname);
		try {
			ResultSet rs = executeSQLQuery("select max(" + columnname + ") from " + tablename);
			rs.next();
			float colmax = rs.getFloat(1);
			
			float binbase = 0;
			String query;
			while (binbase < colmax) {
				query = "select count(*) from " + tablename + " where (" + columnname + " >= " + binbase + ") and (" + columnname + " < " + (binbase + binSize) + ")" + extension;
				rs = executeSQLQuery(query);
				rs.next();
				System.out.println("" + binbase + "-" + (binbase + binSize) + tab + rs.getInt(1));
				binbase += binSize;
			}
			disconnect();
		} catch (SQLException se) {
			se.printStackTrace(System.err);
		}

		return "";
	} // end of execute

} // end of GenomeHistogram
