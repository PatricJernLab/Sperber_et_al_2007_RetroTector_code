/*
* Copyright (©) 2000-2006 Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 16/9 -06
* Beautified 16/9 -06
*/
package plugins;

import retrotector.*;
import java.util.*;
import java.io.*;

/**
*	Executor that collects info about the raw scores obtained by a OrdinaryMotif in a DNA.
*<PRE>
*     Parameters:
*
*
*   DNAFile
* The name of the DNA file in the current directory to search in.
* Default: name of current directory + '.txt'.
*
*   Parameter
* The parameter in Motif.txt where the Motif is defined.
* Default: Motifs
*
*   MotifIDNumber
* The number of the Motif. May consist of two numbers (first and last),
* or 0, in which case all Motifs are run.
* Default: 0
*
*   ConservationFactor
* Bonus factor for highly conserved positions.
* Default: 2
*</PRE>
*
* The value 5.5 is used for SDFactor.
*
*/
public class RawScoreStatistics extends Executor {

/**
*	Key for parameter in Motifs.txt = "Parameter".
*/
	public static final String PARAMETERLABEL = "Parameter";

/**
* Key for the Motif number(s) = "MotifIDNumber".
*/
	public static final String MOTIFIDLABEL = "MotifIDNumber";
  
/**
* Number of classes in histogram = 20;
*/
  static final int HISTSIZE = 20;

/**
* To multiply SD with when setting threshold = 5.5.
*/
		static final float sdFactor = 5.5f;
		
		private Motif[ ] allMotifs;

/**
* Standard Executor constructor.
*/
	public RawScoreStatistics() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put(PARAMETERLABEL, "Motifs");
		explanations.put(PARAMETERLABEL, "Parameter in Motifs.txt");
		orderedkeys.push(PARAMETERLABEL);
		parameters.put(MOTIFIDLABEL, "0");
		explanations.put(MOTIFIDLABEL, "ID number of Motif");
		orderedkeys.push(MOTIFIDLABEL);
		parameters.put(CFACTKEY, "2");
		explanations.put(CFACTKEY, "Bonus factor for highly conserved positions");
		orderedkeys.push(CFACTKEY);
	} // end of constructor.
	
	private float cfact;
	private int id;
	private OrdinaryMotif theMotif;
  private float bestscore;
	
// search in one strand
	private void doStrand(DNA targetDNA) throws RetroTectorException {
		theMotif.refresh(new Motif.RefreshInfo(targetDNA, cfact, sdFactor, null));
    bestscore = theMotif.getBestRawScore();
		int counter = 0;
		float score;
    float sum = 0;
    int n = 0;
    float aver;
    float sd;
    float skew;
		int index;
    int[ ] histo = new int[HISTSIZE + 1];
		for (int pos=0; pos<targetDNA.LENGTH; pos++) {
      score = theMotif.getRawScoreAt(pos);
      if (!Float.isNaN(score)) {
        n++;
        sum += score;
				index = (int) (score / bestscore * HISTSIZE);
				index = Math.min(index, HISTSIZE);
        try {
					histo[index]++;
				} catch (ArrayIndexOutOfBoundsException ae) {
					throw ae;
				}
      }
		}
    histo[HISTSIZE - 1] += histo[HISTSIZE];
    int hmax = 0;
    for (int h=0; h<HISTSIZE; h++) {
      if (histo[h] > hmax) {
        hmax = histo[h];
      }
    }
    hmax /= 60;
    aver = sum / n;
    sum = 0;
    float sum3 = 0;
		for (int pos=0; pos<targetDNA.LENGTH; pos++) {
      score = theMotif.getRawScoreAt(pos);
      if (!Float.isNaN(score)) {
        sum += (score - aver) * (score - aver);
        sum3 += (score - aver) * (score - aver) * (score - aver);
      }
		}
		Utilities.outputString("  " + n + " valid raw scores");
		Utilities.outputString("  Average=" + aver);
		Utilities.outputString("  SD=" + Math.sqrt(sum / n));
		Utilities.outputString("  skewness=" + sum3 / n);
    Utilities.outputString("0");
    for (int hh=0; hh<HISTSIZE; hh++) {
      for (int a=0; a<histo[hh]; a+=hmax) {
        System.out.print("*");
      }
      System.out.println();
    }
    Utilities.outputString("" + theMotif.getBestRawScore());
  } // end of doStrand
	
// do one Motif
	private void makeOne(int nr) throws RetroTectorException {
		Motif aMotif = allMotifs[0];
		
		int m;
		for (m=1; (m<allMotifs.length) & (aMotif.MOTIFID != nr); m++) {
			aMotif = allMotifs[m]; // find a Motif iwith id
		}
		if (m>=allMotifs.length) {
			aMotif = allMotifs[allMotifs.length - 1];
			if (aMotif.MOTIFID != nr) {
				return;
			}
		}
    if (!(aMotif instanceof OrdinaryMotif)) {
      Utilities.outputString("  Motif nr " + aMotif.MOTIFID + " is not an OrdinaryMotif");
      return;
    }
    theMotif = (OrdinaryMotif) aMotif;
		Utilities.outputString("  Motif nr " + theMotif.MOTIFID);
		DNA primDNA = getDNA(getString(DNAFILEKEY, ""), true);
		DNA secDNA = getDNA(getString(DNAFILEKEY, ""), false);
		Utilities.outputString(" Primary strand");
		doStrand(primDNA);
		
		Utilities.outputString(" Secondary strand");
		doStrand(secDNA);
	} // end of makeOne
	
/**
* Collects instances of Motif subclasses defined by Motifs.txt.
*/
	private void collectMotifs() throws RetroTectorException {
	  Hashtable table = new Hashtable();
    Database database = RetroTectorEngine.getCurrentDatabase();
		ParameterFileReader reader = new ParameterFileReader(database.getFile(Database.MOTIFFILENAME), table);
		reader.readParameters();
		reader.close();
		String[ ] mlines = (String[ ]) table.get(getString(PARAMETERLABEL, "Motifs"));
		allMotifs = new Motif[mlines.length];
		for (int i=0; i<mlines.length; i++) {
			Motif.dataLine = mlines[i];
			Motif.motifDataBase = database;
      String n = Motif.seq_typePart(); //get seq_type field
      Class c = (Class) Motif.motifClassTable.get(n);
      if (c == null) {
        haltError("Motif type not identified", n);
      }
      Motif result = null;
      try {
        result = (Motif) c.newInstance();
      } catch (Exception e) {
        if (e instanceof RetroTectorException) {
          haltError((RetroTectorException) e);
        } else {
          haltError("Could not make Motif from", Motif.dataLine);
        }
      }
      allMotifs[i] = result;
		}
	} // end of collectMotifs
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 09 16";
  } //end of version
  
/**
* Executes, using parameters above.
*/
	public String execute() throws RetroTectorException {
	
		cfact = getFloat(CFACTKEY, 2.0f);
    collectMotifs();
		Utilities.outputString("Database is " + RetroTectorEngine.getCurrentDatabase().DATABASENAME);

    String mo = getString(MOTIFIDLABEL, "");
    int ind = mo.trim().indexOf(' ');
    if (ind >= 0) {
      int id1 = Utilities.decodeInt(mo.trim().substring(0, ind));
      int id2 = Utilities.decodeInt(mo.trim().substring(ind + 1).trim());
      for (int id3=id1; id3<=id2; id3++) {
        makeOne(id3);
      }
    } else {
			id = getInt(MOTIFIDLABEL, -1);
			if (id >= 0) {
				makeOne(id);
			} else {
				for (int i=1; i<=9999; i++) {
					makeOne(i);
				}
			}
		}
		
		return "";
	} // end of execute
	
} // end of RawScoreStatistics
