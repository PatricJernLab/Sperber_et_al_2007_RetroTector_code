/*
* Copyright (©) 2000-2005, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 25/5 -05
* Beautified 25/5 -05
*/
package plugins;

import retrotector.*;
import java.io.*;
import java.util.*;

/**
* Executor that constructs a SubGenesCandidate.txt file.
* It requires a script with a parameter something like:
*<PRE>
*SubGenes::
* 5LTR         0<0       50<598          ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            ?
* PBS          ?            0<0          26<750       ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            ?
* MA           ?            ?            0<0          428<2006     ?            ?            ?            ?            ?            ?            ?            ?            ?            ?
* CA           ?            ?            ?            0<0          173<578      ?            ?            ?            ?            ?            ?            ?            ?            ?
* NC           ?            ?            ?            ?            0<0          255<986      86<1305      ?            ?            ?            ?            ?            ?            ?
* DU           ?            ?            ?            ?            ?            0<0          189<319      ?            ?            ?            ?            ?            ?            ?
* Prot         ?            ?            ?            ?            ?            ?            0<0          346<1707     ?            ?            ?            ?            ?            ?
* RT           ?            ?            ?            ?            ?            ?            ?            0<0          889<2250     988<2753     ?            ?            ?            ?
* DL           ?            ?            ?            ?            ?            ?            ?            ?            0<0          133<503      ?            ?            ?            ?
* IN           ?            ?            ?            ?            ?            ?            ?            ?            ?            0<0          1074<4054    ?            ?            ?
* SU           ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            0<0          135<668      ?            ?
* TM           ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            0<0          271<2092     ?
* PPT          ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            0<0          50<1600
* 3LTR         ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            ?            0<0
*::
*</PRE>
*<BR>
*	The diagonal and diagonal + 1 must be filled. Other positions higher up may also be filled.<BR>
* The Database subdirectory where this is done must first be selected as working directory.
*/
public class MakeSubGenes extends Executor {

/**
* Standard Executor constructor.
*/
	public MakeSubGenes() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
	} // end of constructor
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2005 05 25";
  } //end of version
	
	private DistanceRange[ ][ ] ranges;
	private StringBuffer sb;
	private int cw = 13; // column width
	
	private void sbAppend(String s) {
	  sb.append(s);
		for (int i = s.length(); i<cw - 1; i++) {
			sb.append(" ");
		}
		sb.append(" ");
	} // end of sbAppend
  
// x and y are indices in ranges. ranges[y][x] will also be calculated
// x > y is assumed
	private void makeRange(int x, int y) {
		if (ranges[x][y] != null) {
			ranges[y][x] = ranges[x][y].inverse();
		} else {
			DistanceRange dr = DistanceRange.rangeSum(ranges[y + 1][y], ranges[x][y + 1]);
			for (int z=2; z<x-y; z++) {
				dr = DistanceRange.rangeUnion(dr, DistanceRange.rangeSum(ranges[y + z][y], ranges[x][y + z]));
			}
			ranges[x][y] = dr;
			ranges[y][x] = dr.inverse();
		}
	} // end of makeRange
	
/**
* Execute as specified above.
*/
	public String execute() throws RetroTectorException {
	
		String[ ] ss = getStringArray("SubGenes");
		ranges = new DistanceRange[ss.length][ss.length];
		int i;
		String[ ] names = new String[ss.length];
		String[ ] sss;
		for (i=0; i<ss.length; i++) { // make diagonal
			sss = Utilities.splitString(ss[i]);
			names[i] = sss[0];
			ranges[i][i] = new DistanceRange(0, 0);
			for (int ii=i+1; ii<ss.length; ii++) {
				if (sss[ii + 1].equals("?")) {
					ranges[ii][i] = null;
					ranges[i][ii] = null;
				} else {
					ranges[ii][i] = new DistanceRange(sss[ii + 1]);;
					ranges[i][ii] = ranges[ii][i].inverse();
				}
			}
		}
		for (int diagonal=2; diagonal<ss.length; diagonal++) {
			for (int dipos=0; dipos<ss.length-diagonal; dipos++) {
				makeRange(diagonal + dipos, dipos);
			}
		}
		
		sb = new StringBuffer(); // recreate sb with spaced names
		for (int s1=0; s1<names.length; s1++) {
			sbAppend(names[s1]);
		}

		ParameterFileWriter sgWriter = new ParameterFileWriter(new File(RetroTectorEngine. currentDirectory(), "SubGenesCandidate.txt"));
		sgWriter.writeComment(" " + Database.LASTCHANGED + " " + Utilities.swedishDate(new Date()));
		sgWriter.writeSingleParameter("Names", sb.toString(), false);
		for (int s2=0; s2<names.length; s2++) {
			sb = new StringBuffer();
			for (int r=4; r>names[s2].length(); r--) { // to make columns even
				sb.append(" ");
			}
			for (int s3=0; s3<ranges.length; s3++) {
				sbAppend(ranges[s3][s2].toString());
			}
			sgWriter.writeSingleParameter(names[s2], sb.toString(), false);
		}
		sgWriter.close();
		return "";
	} // end of execute
	
} // end of MakeSubGenes
