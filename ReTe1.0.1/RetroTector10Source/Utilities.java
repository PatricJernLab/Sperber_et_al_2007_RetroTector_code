/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 1/10 -06
* Beautified 1/10 -06
*/

package retrotector;

import java.io.*;
import java.util.*;
import java.text.*;

/**
* Various methods and fields of general interest.
*/
public final class Utilities {

/**
* To avoid importing java.awt.
*/
	public static class Rectangle {

    public int x;
    public int y;
    public int width;
    public int height;

/**
* Constructs a new Rectangle whose top-left corner is specified as (x, y)
* and whose width and height are specified by the arguments of the same name.
*/
   public  Rectangle(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
    } // end of Utilities.Rectangle.constructor(int, int, int, int)

/**
*	Constructs a new Rectangle, initialized to match the values
* of the specificed Rectangle.
*/
    public Rectangle(Rectangle r) {
    	this(r.x, r.y, r.width, r.height);
    } // end of Utilities.Rectangle.constructor(Rectangle)

/**
* Determines whether or not this Rectangle and the specified Rectangle intersect.
* Two rectangles intersect if their intersection is nonempty.
*/
    public final boolean intersects(Rectangle r) {
			return !((r.x + r.width <= x) ||
				(r.y + r.height <= y) ||
				(r.x >= x + width) ||
				(r.y >= y + height));
    } // end of Utilities.Rectangle.intersects(Rectangle)

	} // end of Rectangle



/**
* A region with discrepance below a specfied level between a template and a DNA.
*/
	public static class TemplateHit {
	
/**
* The template array.
*/
		public final int[ ] TEMPLATE;

/*
* The start of the hit in TEMPLATE.
*/
		public final int POSINTEMPLATE;

/**
* The relevant DNA.Translator.
*/
		public final DNA.Translator THETRANSLATOR;

/**
* Base codes from source DNA.
*/
		public final int[ ] DNABASES;

/*
* The start of the hit in DNABASES.
*/
		public final int POSINTHEDNA;
		
/**
* The short name of the template.
*/
		public final String NAME;
	
/**
* The length of the hit.
*/
		public final int HITLENGTH;
		
/**
* To mark as no longer useful;
*/
		public boolean deleted = false;

/**
* Constructor. Builds fresh instance.
* @param	merLength				Length of window to search with.
* @param	template				Array of base codes in template.
* @param	startIntemplate	Position in template to start building at.
* @param	trans						To handle internal-external conversion.
* @param	dnabases				Base codes for sequence to search in.
* @param	startInDNA			Position in dnabases to start building at.
* @param	firstInDNA			Do not extend below this.
* @param	lastInDNA				Do not extend beyond this.
* @param	errMax					Largest acceptable fraction of non-identity in window.
* @param	name						Identifying String.
*/
		public TemplateHit(int merLength, int[ ] template, int startIntemplate, DNA.Translator trans, int[ ] dnabases, int startInDNA, int firstInDNA, int lastInDNA, float errMax, String name) {
			TEMPLATE = template;
			THETRANSLATOR = trans;
			DNABASES = dnabases;
			NAME = name;
			
			int tempup = startIntemplate;
			int tempdown = startIntemplate;
			int dnaup = startInDNA;
			int dnadown = startInDNA;
			int errup = 0;
			int errdown = 0;
			int length = merLength;
			float threshold = merLength * errMax;
			boolean done;
			
			for (int i=0; i<merLength; i++) { // start error count in original mer position
				if ((template[startIntemplate + i] < 0) | (template[startIntemplate + i] != dnabases[startInDNA + i])) {
					errup++;
				}
			}
			errdown = errup;
			
			if (errup <= threshold) { // search upwards
				done = false;
				while ((tempup + merLength < template.length) && (dnaup + merLength < dnabases.length) && (dnaup + merLength <= lastInDNA) && !done) {
					if ((template[tempup] < 0) | (template[tempup] != dnabases[dnaup])) {
						errup--;
					}
					try {
						if ((template[tempup + merLength] < 0) | (template[tempup + merLength] != dnabases[dnaup + merLength])) {
							errup++;
						}
					} catch (ArrayIndexOutOfBoundsException aie) {
						throw aie;
					}
					if (errup > threshold) {
						done = true;
					} else {
						tempup++;
						dnaup++;
						length++;
					}
				}
				done = false;
				while ((tempdown > 0) && (dnadown > 0) && (dnadown > firstInDNA) && !done) { // search downwards
					if ((template[tempdown - 1] < 0) | (template[tempdown - 1] != dnabases[dnadown - 1])) {
						errdown++;
					}
					if ((template[tempdown + merLength - 1] < 0) | (template[tempdown + merLength - 1] != dnabases[dnadown + merLength - 1])) {
						errdown--;
					}
					if (errdown > threshold) {
						done = true;
					} else {
						tempdown--;
						dnadown--;
						length++;
					}
				}
			}
			POSINTEMPLATE = tempdown;
			POSINTHEDNA = dnadown;
			HITLENGTH = length;
		} // end of Utilities.TemplateHit.constructor(int, int[ ], int, DNA.Translator, int[ ], int, int, int, float, String)

/**
* Constructor for a specific instance.
* @param	template				Array of base codes in template.
* @param	posIntemplate		Start position in template.
* @param	trans						To handle internal-external conversion.
* @param	dnabases				Base codes for sequence to search in.
* @param	posInDNA				Start position in dnabases.
* @param	hitlength				Length of hit.
* @param	name						Identifying String.
*/
		public TemplateHit(int[ ] template, int posIntemplate, DNA.Translator trans, int[ ] dnabases, int posInDNA, int hitlength, String name) {
			TEMPLATE = template;
			DNABASES = dnabases;
			THETRANSLATOR = trans;
			POSINTEMPLATE = posIntemplate;
			POSINTHEDNA = posInDNA;
			HITLENGTH = hitlength;
			NAME = name;
		} // end of Utilities.TemplateHit.constructor(int[ ], int, DNA.Translator, int[ ], int, int, String)	

/**
* Constructor using toString() output.
* @param	s				String as output from toString().
* @param	trans		To handle internal-external conversion.
*/
		public TemplateHit(String s, DNA.Translator trans) throws RetroTectorException {
			THETRANSLATOR = trans;
			DNABASES = null;
			TEMPLATE = null;
			String[ ] ss = Utilities.splitString(s);
			NAME = ss[0];
			POSINTEMPLATE = Utilities.decodeInt(ss[1]);
			POSINTHEDNA = THETRANSLATOR.internalize(Utilities.decodeInt(ss[2]));
			HITLENGTH = Utilities.decodeInt(ss[3]);
		} // end of Utilities.TemplateHit.constructor(String, DNA.Translator)

		
		public String toString() {
			return NAME + " " + POSINTEMPLATE + " " + THETRANSLATOR.externalize(POSINTHEDNA) + " " + HITLENGTH;
		} // end of Utilities.TemplateHit.toString
		
	} // end of Utilities.TemplateHit
	

/**
* A class enveloping template base String and useful derived data.
*/
	public static class TemplatePackage {
	
/**
*	A (short) name for the template.
*/
		public final String TEMPLATENAME;
		
/**
*	Accessory information.
*/
		public final String TEMPLATESECINFO;
		
/**
*	The base String defining the template.
*/
		public final String TEMPLATESTRING;
				
/**
* The base codes for TEMPLATESTRING.
*/
		public final int[ ] TEMPLATECODES;
		
/**
* Mers derived from TEMPLATECODES.
*/
		public final long[ ] TEMPLATEMERS;
		
/**
* True if template was complemented.
*/
		public final boolean TEMPLATECOMPLEMENTED;
		
/**
* Constructor.
* @param	name				->TEMPLATENAME.
* @param	content			->TEMPLATESTRING, possibly after complementation.
* @param	mersize			The length of the mers to get from TEMPLATECODES.
* @param	complement	If true, content is complemented.
* @param	secInfo			Accessory information.
*/
		public TemplatePackage(String name, String content, int mersize, boolean complement, String secInfo) throws RetroTectorException {
			if (complement) {
				content = complementBaseString(content);
			}
			TEMPLATENAME = name;
			TEMPLATESECINFO = secInfo;
			TEMPLATESTRING = content;
			TEMPLATECODES = encodeBaseString(content);
			TEMPLATEMERS = merize(TEMPLATECODES, 0, TEMPLATECODES.length - 1, mersize, true);
			TEMPLATECOMPLEMENTED = complement;
		} // end of TemplatePackage.constructor(String, String, int, boolean, String)
		
	} // end of TemplatePackage

	
/**
* Used by treatFilesIn(). To do essentially anything to a File.
*/
	public static interface FileTreater {
	
/**
* @param	f	The File to act on.
* @return	f, possibly under another name.
*/
		public File treatFile(File f) throws RetroTectorException;
		
	} // end of FileTreater


/**
* Emulates array with arbitrary indexing base.
*/
	abstract public static class OffsetArray {
	
/**
* Indexing base.
*/
		public final int FIRSTINDEX;

/**
* Highest acceptable index.
*/
		public final int LASTINDEX;
		
/**
* LASTINDEX - FIRSTINDEX + 1.
*/
		public final int LENGTH;
		
/**
* Constructor.
* @param	firstindex	->FIRSTINDEX.
* @param	lastindex		->LASTINDEX.
*/
		public OffsetArray(int firstindex, int lastindex) {
			FIRSTINDEX = firstindex;
			LASTINDEX = lastindex;
			LENGTH = LASTINDEX - FIRSTINDEX + 1;
		} // end of OffsetArray.constructor(int, int)

	} // end of OffsetArray
	
	

/**
* Emulates float array with arbitrary indexing base.
*/
	public static class OffsetFloatArray extends OffsetArray {
	
		private float[ ] values;
	
/**
* Constructor.
* @param	firstindex	->FIRSTINDEX.
* @param	lastindex		->LASTINDEX.
*/
		public OffsetFloatArray(int firstindex, int lastindex) {
			super(firstindex, lastindex);
			values = new float[LENGTH];
		} // end of OffsetFloatArray.constructor(int, int)
		
/**
* Sets value at index
* @param	index
* @param	value
*/
		public final void setValueAt(int index, float value) {
			values[index - FIRSTINDEX] = value;
		} // end of OffsetFloatArray.setValueAt(int, float)
		
/**
* @param	index
* @return	value at index
*/
		public final float getValueAt(int index) {
			float x = 0;
			try {
				x = values[index - FIRSTINDEX];
			} catch (ArrayIndexOutOfBoundsException ae) {
				throw ae;
			}
			return x;
		} // end of OffsetFloatArray.getValueAt(int)

/**
* @param	fillvalue	Value to put into all slots.
*/
		public final void fillWith(float fillvalue) {
			Arrays.fill(values, fillvalue);
		} // end OffsetFloatArray.fillWith(float)

	} // end of OffsetFloatArray



/**
* The directory for necessary data files.
*/
	public static File databaseDirectory;
	
/**
* Suffix of files to be executed by SweepScripts = "Script_.txt".
*/
	public static final String SWEEPABLESCRIPTFILESUFFIX = "Script_.txt";


/**
* Suffix to which SWEEPABLESCRIPTFILESUFFIX should be changed upon
* execution by SweepScripts = "Script.txt".
*/
	public static final String SWEPTSCRIPTFILESUFFIX = "Script.txt";
	
/**
* = "Out of memory".
*/
	public static String OUTOFMEMORY = "Out of memory";

/**
* For numeric conversion.
*/
	public static final DecimalFormat DECIMALFORMAT = new DecimalFormat();

/**
* @param	object	Any object.
* @return	The name of the class of object (without package name).
*/
	public final static String className(Object object) {
		String s = object.getClass().getName();
		int i = s.lastIndexOf('.');
		return s.substring(i + 1);
	} // end of className(Object)
  
/**
* @param	cl				A Class.
* @param	superName	The name of a class.
* @return	True if cl has a superclass named superName.
*/
  public final static boolean subClassOf(Class cl, String superName) {
    while ((cl != null) && (!cl.getName().equals(superName))) {
      cl = cl.getSuperclass();
    }
    return (cl != null);
  } // end of subClassOf(Class, String)

/**
* @param	theString	Any String.
*	@return	theString split into parts at blanks.
*/
	public final static String[ ] splitString(String theString) {
		theString = theString.trim();
		int index;
		Stack s = new Stack();
		while ((index = theString.indexOf(' ')) >= 0) {
			s.push(theString.substring(0, index).trim());
			theString = theString.substring(index).trim();
		}
		s.push(theString);
		String[ ] result = new String[s.size()];
		s.copyInto(result);
		return result;
	} // end of splitString(String)

/**
* @param		ss	A String array.
* @return	The Strings in ss, concatenated with \n between.
*/
	public final static String collapseLines(String[ ] ss) {
		if ((ss == null) || (ss.length == 0)) {
			return "";
		}
		StringBuffer sb = new StringBuffer(ss[0]);
		for (int i=1; i<ss.length; i++) {
			sb.append("\n");
			sb.append(ss[i]);
		}
		return sb.toString();
	} // end of collapseLines(String[ ])

/**
* @param	s	String, possibly contining \n;s.
* @return	Array of the Strings separated by the \n;s.
*/
	public final static String[ ] expandLines(String s) {
		int ind;
		Stack st = new Stack();
		while ((ind = s.indexOf("\n")) >= 0) {
			st.push(s.substring(0, ind));
			s = s.substring(ind + 1);
		}
		st.push(s);
		String[ ] ss = new String[st.size()];
		st.copyInto(ss);
		return ss;
	} // end of expandLines(String)

/**
* Just prints a String at present. May be extended to do more things.
* @param	s	The String to print.
*/
	public final static void outputString(String s) {
		System.out.println(s);
	} // end of outputString(String)

/**
* For temporary debugging output.
* @param	sender	The object sending the message, or null.
* @param	line		The message to print.
*/
	public final static void debugPrint(Object sender, String line) {
		if (sender == null) {
			System.err.println(line);
		} else {
			System.err.println(className(sender) + ": " + line);
		}
	} // end of debugPrint(Object, String)
	
/**
* Utility method to log stack trace.
* @param	thr	A Throwable.
*/
	public final static void logStackTrace(Throwable thr) {
		StackTraceElement[ ] ate = thr.getStackTrace();
		for (int i=0; i<ate.length; i++) {
			RetroTectorEngine.toLogFile(ate[i].toString());
		}
	} // end of logStackTrace(Throwable)
	
/**
* Utility method to construct a new File to specifications. It has a unique name, and
* also there is not a file with the same name and 'Script.txt' for 'Script_.txt.
* @param	directory		The directory to put the file in.
* @param	firstPart		The leading part of the file name.
* @param	secondPart	The trailing part of the file name.
* @return	A new file with a name consisting of firstPart + a number + secondPart.
*/
	public final static File uniqueFile(File directory, String firstPart, String secondPart) {
		String altSecondPart = null;
		if (secondPart.endsWith(SWEEPABLESCRIPTFILESUFFIX)) {
			altSecondPart = secondPart.substring(0, secondPart.length() - SWEEPABLESCRIPTFILESUFFIX.length()) + SWEPTSCRIPTFILESUFFIX;
		}
		
		int n = 1;
		File newFile = new File(directory, firstPart + "001" + secondPart);
		File newFile2 = newFile;
		if (altSecondPart != null) {
			newFile2 = new File(directory, firstPart + "001" + altSecondPart);
		}
		while (newFile.exists() || newFile2.exists()) {
			n++;
			newFile = new File(directory, firstPart + Executor.zeroLead(n, 900) + secondPart);
			newFile2 = newFile;
			if (altSecondPart != null) {
				newFile2 = new File(directory, firstPart + Executor.zeroLead(n, 900) + altSecondPart);
			}
		}
		return newFile;
	} // end of uniqeFile(File, String, String)

/**
* Utility method to construct two new Files to specifications.
* @param	directory	The directory to put the files in.
* @param	partA1		The leading part of the first file name.
* @param	partA2		The trailing part of the first file name.
* @param	partB1		The leading part of the second file name.
* @param	partB2		The trailing part of the second file name.
* @return	An array of two new files with names as in uniqeFile, with same number.
*/
	public final static File[ ] uniqueFiles(File directory, String partA1, String partA2, String partB1, String partB2) {
		int n = 1;
		File[ ] newFiles = new File[2];
		newFiles[0] = new File(directory, partA1 + "001" + partA2);
		newFiles[1] = new File(directory, partB1 + "001" + partB2);
		while ((newFiles[0].exists()) || (newFiles[1].exists())) {
			n++;
			newFiles[0] = new File(directory, partA1 + Executor.zeroLead(n, 900) + partA2);
			newFiles[1] = new File(directory, partB1 + Executor.zeroLead(n, 900) + partB2);
		}
		return newFiles;
	} // end of uniqueFiles(File, String, String, String, String)
	
/**
* Utility method to construct three new Files to specifications.
* @param	directory	The directory to put the files in.
* @param	partA1		The leading part of the first file name.
* @param	partA2		The trailing part of the first file name.
* @param	partB1		The leading part of the second file name.
* @param	partB2		The trailing part of the second file name.
* @param	partC1		The leading part of the third file name.
* @param	partC2		The trailing part of the third file name.
* @return	An array of three new files with names as in uniqeFile, with same number.
*/
	public final static File[ ] uniqueFiles(File directory, String partA1, String partA2, String partB1, String partB2, String partC1, String partC2) {
		int n = 1;
		File[ ] newFiles = new File[3];
		newFiles[0] = new File(directory, partA1 + "001" + partA2);
		newFiles[1] = new File(directory, partB1 + "001" + partB2);
		newFiles[2] = new File(directory, partC1 + "001" + partC2);
		while ((newFiles[0].exists()) || (newFiles[1].exists()) || (newFiles[2].exists())) {
			n++;
			newFiles[0] = new File(directory, partA1 + Executor.zeroLead(n, 900) + partA2);
			newFiles[1] = new File(directory, partB1 + Executor.zeroLead(n, 900) + partB2);
			newFiles[2] = new File(directory, partC1 + Executor.zeroLead(n, 900) + partC2);
		}
		return newFiles;
	} // end of uniqueFiles(File, String, String, String, String, String, String)
	
/**
* @param	s	A String, presumably representing an integer.
* @return	The represented integer.
*/
	public final static int decodeInt(String s) throws RetroTectorException {
		int i = Integer.MIN_VALUE;
		try {
			i = Integer.parseInt(s.trim());
		} catch (NumberFormatException nfe) {
			throw new RetroTectorException("decodeInt", "Not integer format", s);
		}
		return i;
	} // end of decodeInt(String)

/**
* Utility routine to get presumed float argument.
* @param	s	String to decode.
* @return	Float represented by s.
*/
	public final static float decodeFloat(String s) throws RetroTectorException {
		float f = Float.NaN;
		try {
			f = Float.valueOf(s.trim()).floatValue();
		} catch (NumberFormatException nfe) {
			throw new RetroTectorException("decodeFloat", "Not numerical format", s);
		}
		return f;
	} // end of decodeFloat(String)

/**
* Utility routine to format a number with two decimals.
* Certified for use only with RVectors!
* @param	f	The number to format
* @return	The formatted String
*/
 	public final static String twoDecimals(float f) {
		return formattedNumber(f, 1, 2);
 	} // end of twoDecimals(float)
 	
/**
* To print numbers with limits on number of decimals.
* @param	minDecimals	Smallest number of decimals to use.
* @param	maxDecimals	Largest number of decimals to use.
* @return	Formatted String.
*/
	public final static String formattedNumber(double d, int minDecimals, int maxDecimals) {
		DECIMALFORMAT.setMinimumFractionDigits(minDecimals);
		DECIMALFORMAT.setMaximumFractionDigits(maxDecimals);
    String s = DECIMALFORMAT.format(d);
    int i = s.indexOf(','); // swedish decimal?
    if (i < 0) {
      return s;
    } else {
      return s.substring(0, i) + '.' + s.substring(i + 1);
		}
	} // end of formattedNumber(double, int, int)
	
/**
* @param	da	A Date.
* @return	A corresponding String of the form yy mm dd.
*/
	public final static String swedishDate(Date da) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(da);
		StringBuffer sb = new StringBuffer();
		sb.append(cal.get(Calendar.YEAR));
		sb.append(" ");
		if (cal.get(Calendar.MONTH) < 9) {
			sb.append("0");
		}
		sb.append(cal.get(Calendar.MONTH) + 1);
		sb.append(" ");
		if (cal.get(Calendar.DATE) < 10) {
			sb.append("0");
		}
		sb.append(cal.get(Calendar.DATE));
		return sb.toString();
	} // end of swedishDate(Date)


/**
* Finds matches	for a motif in DNA.
* Agrep algorithm is used.
* For each hit, a distribution according to weightProfile is added to hits, centered on the hit address.
* @param	dna				The DNA to search in.
* @param	from			Position in dna to start at.
* @param	to				Position in dna to end at.
* @param	pattern		Array of base codes to match.
* @param	hits			String array, normally of length (to - from + 1), to mark hits in.
* @param	faults		Max number of mismatches allowed.
* @return	Number of individual hits found.
*/
	public final static int match(DNA dna, int from, int to, byte[ ] pattern, String[ ] hits, int faults) {
		AgrepContext context = new AgrepContext(pattern, from, to - pattern.length, faults);
		int count = 0;
		int res = -1;
		int p = -1;
		int diff = -1;
		while ((res = dna.agrepFind(context)) != Integer.MIN_VALUE) {
			hits[Math.abs(res) - from] = Compactor.BASECOMPACTOR.bytesToString(pattern);
			count++;
		}
		return count;
	} // end of match(DNA, int, int, byte[], String[], int)

/**
* Finds matches	for a motif in DNA.
* Agrep algorithm is used.
* For each hit, a distribution according to weightProfile is added to hits, centered on the hit address.
* @param	dna				The DNA to search in.
* @param	pattern		Array of base codes to match.
* @param	hits			Array, normally of same length as dna, to mark hits in.
* @param	faults		Max number of mismatches allowed.
* @param	weightProfile	Array indicating influence in hits, depending on distance from hit.
* @return	Number of individual hits found.
*/
	public final static int match(DNA dna, byte[ ] pattern, float[ ] hits, int faults, float[ ] weightProfile) {
		if (pattern.length > 32) {
			return lmatch(dna, pattern, hits, faults, weightProfile);
		}
		AgrepContext context = new AgrepContext(pattern, 0, dna.LENGTH - pattern.length, faults);
		int count = 0;
		int res = -1;
		int p = -1;
		int diff = -1;
		while ((res = dna.agrepFind(context)) != Integer.MIN_VALUE) {
			hits[Math.abs(res)] += weightProfile[0];
			for (diff=1; diff<weightProfile.length; diff++) {
				p = Math.abs(res) - diff;
				if (p >= 0) {
					hits[p] += weightProfile[diff];
				}
				p = Math.abs(res) + diff;
				if (p<hits.length) {
					hits[p] += weightProfile[diff];
				}
			}
			count++;
		}
		return count;
	} // end of match(DNA, byte[], float[], int, float[])


/**
* Finds matches	for a motif in DNA.
* Agrep algorithm is used.
* For each hit, a distribution according to weightProfile is added to hits, centered on the hit address.
* @param	dna				The DNA to search in.
* @param	pattern		Array of base codes to match.
* @param	hits			Array, normally of same length as dna, to mark hits in.
* @param	faults		Max number of mismatches allowed.
* @param	weightProfile	Array indicating influence in hits, depending on distance from hit.
* @return	Number of individual hits found.
*/
	public final static int lmatch(DNA dna, byte[ ] pattern, float[ ] hits, int faults, float[ ] weightProfile) {
		if (pattern.length > 64) {
			return -2;
		}
		LongAgrepContext context = new LongAgrepContext(pattern, 0, dna.LENGTH - pattern.length, faults);
		int count = 0;
		int res = -1;
		int p = -1;
		int diff = -1;
		while ((res = dna.longAgrepFind(context)) != Integer.MIN_VALUE) {
			hits[Math.abs(res)] += weightProfile[0];
			for (diff=1; diff<weightProfile.length; diff++) {
				p = Math.abs(res) - diff;
				if (p >= 0) {
					hits[p] += weightProfile[diff];
				}
				p = Math.abs(res) + diff;
				if (p<hits.length) {
					hits[p] += weightProfile[diff];
				}
			}
			count++;
		}
		return count;
	} // end of lmatch(DNA, byte[], float[], int, float[])

/**
* Makes current thread sleep for specified interval.
* @param	sleepTime	in millis
*/
	public final static void doSleep(long sleepTime) {
    try {
      Thread.currentThread().sleep(sleepTime);
    } catch (InterruptedException ie) {
    }
  } // end of doSleep(long)

/**
* @param	s	A String.
* @param	c	A shorter String.
* @return	Number of occurrences of c in s.
*/
  public final static int charCount(String s, String c) {
    int count = 0;
    String ss = s;
    int ind = 0;
    while ((ind = ss.indexOf(c, ind)) >= 0) {
      count++;
      ind++;
    }
    return count;
  } // end of charCount(String, String)
	
	private static int treated;  // counter for Files acted on by treatFilesIn()
	
// applies treator to all files anywhere in theDir.
	private final static void sweepDirectory (File theDir, FileTreater treator) throws RetroTectorException {

// get all file names and look through them
		File[ ] files = treator.treatFile(theDir).listFiles();
		for (int n=0; n<files.length; n++) {
			if (files[n].isDirectory()) { // is it a directory?
				sweepDirectory(files[n], treator);
			} else {
				treator.treatFile(files[n]);
				treated++;
			}
		}
	} // end of sweepDirectory(File, FileTreater)
				
/**
* Does something to all files anywhere in a directory.
* @param	directory	The directory to search.
* @param	treater		The FileTreater doing it.
* @return	The number of treated files, or -1 if directory is not one.
*/
	public final static int treatFilesIn(File directory, FileTreater treater) throws RetroTectorException {
		if (!directory.isDirectory()) {
			return -1;
		}
		
		treated = 0;
		sweepDirectory(directory, treater);
		return treated;
	} // end of treatFilesIn(File, FileTreater)

/**
* Retrieves a file for reading. ordinary or zipped.
* @param	pathname			The absolute pathname of the file.
* @param	The specified file, or null.
*/
	public final static File getReadFile(String pathname) throws RetroTectorException {
		if (pathname.equals("/") | pathname.endsWith(":\\")) { // Windows, you know...
			return new File(pathname);
		}
		
		int ind = pathname.length() - 2; // I don't quite get this, but it works...
		while ((ind >= 0) && (pathname.charAt(ind) != '/') & (pathname.charAt(ind) != File.separatorChar)) {
			ind--;
		}
		if (ind < 0) {
			return new File(pathname + File.separator);
		} else {
			File dir = getReadFile(pathname.substring(0, ind + 1));
			if (!dir.isDirectory()) {
				dir = new ZFile(dir.getParentFile(), dir.getName());
			}
			return getReadFile(dir, pathname.substring(ind + 1));
		}
	} // end of getReadFile(String)

/**
* Retrieves a file for reading. ordinary or zipped.
* @param	directory	A directory, ordinary or zipped
* @param	name			The name of the file.
* @param	The specified file, or null.
*/
	public final static File getReadFile(File directory, String name) throws RetroTectorException {
		File f = new File(directory, name);
		if (f.exists()) {
			return f;
		} else if ((directory instanceof ZFile) || name.endsWith(".zip") | name.endsWith(".ZIP")) {
			ZFile zf = new ZFile(directory, name);
			return zf;
		} else {
			throw new RetroTectorException("getReadFile", "File " + directory.getPath() + "/" + name + " does not exist");
		}
	} // end of getReadFile(File, String)
	
/**
* @param	putPos1	An array such as output py Putein.getAcidPositions().
* @param	putPos2	Another one.
* @return	The number of integers occurring in both arrays.
*/
	public final static int	puteinPositionsCompare(int[ ] putPos1, int[ ] putPos2) {
		int result = 0;
		int p1 = 0;
		int p2 = 0;
		int val1 = putPos1[p1];
		int val2 = putPos2[p2];
		try {
			for (;;) {
				while (val1 < val2) {
					p1++;
					val1 = putPos1[p1];
				}
				if (val1 == val2) {
					result++;
					p1++;
					val1 = putPos1[p1];
				}
				while (val2 < val1) {
					p2++;
					val2 = putPos2[p2];
				}
				if (val1 == val2) {
					result++;
					p2++;
					val2 = putPos2[p2];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		return result;
	} // end of puteinPositionsCompare(int[ ], int[ ])
	
/**
* Replaces blank characters in a String Buffer with corresponding characters in a String.
* @param	buf			The StringBuffer.
* @param	source	The String.
*/
	public final static void updateStringBuffer(StringBuffer buf, String source) {
		for (int p=0; (p<buf.length()) && (p<source.length()); p++) {
			if (buf.charAt(p) == ' ') {
				buf.setCharAt(p, source.charAt(p));
			}
		}
	} // end of updateStringBuffer(StringBuffer, String)

/**
* @param	tab	A Hashtable.
* @return	A sorted array of the keys in tab which are Strings.
*/
	public final static String[ ] keyList(Hashtable tab) {
		Iterator it = tab.keySet().iterator();
		Stack st = new Stack();
		Object o;
		while (it.hasNext()) {
			o = it.next();
			if (o instanceof String) {
				st.push((String) o);
			}
		}
		String[ ] ss = new String[st.size()];
		st.copyInto(ss);
		Arrays.sort(ss);
		return ss;		
	} // end of keyList(Hashtable)

/**
* @param	hit1	A TemplateHit.
* @param	hit2	Another TemplateHit.
* @return	A merger of the two, or null if they are not colinear and overlapping.
*/
	public final static TemplateHit mergeTemplateHits(TemplateHit hit1, TemplateHit hit2) {
		if (hit1.POSINTEMPLATE - hit2.POSINTEMPLATE != hit1.POSINTHEDNA - hit2.POSINTHEDNA) {
			return null; // not colinear
		}
		if (!hit1.NAME.equals(hit2.NAME)) {
			return null; // not of the same template
		}
		int diff = hit1.POSINTEMPLATE - hit2.POSINTEMPLATE;
		if (diff <= 0) {
			if (hit1.HITLENGTH + diff < 0) {
				return null; // not overlapping
			} else {
				return new TemplateHit(hit1.TEMPLATE, hit1.POSINTEMPLATE, hit1.THETRANSLATOR, hit1.DNABASES, hit1.POSINTHEDNA, Math.max(hit1.HITLENGTH, hit2.HITLENGTH - diff), hit1.NAME);
			}
		} else {
			if (hit2.HITLENGTH - diff < 0) {
				return null; // not overlapping
			} else {
				return new TemplateHit(hit2.TEMPLATE, hit2.POSINTEMPLATE, hit2.THETRANSLATOR, hit2.DNABASES, hit2.POSINTHEDNA, Math.max(hit2.HITLENGTH, hit1.HITLENGTH + diff), hit1.NAME);
			}
		}
	} // end of mergeTemplateHits(TemplateHit, TemplateHit)

/**
* @param	baseArray	An array of base codes.
* @return	baseArray complemented and reversed.
*/
	public final static int[ ] complementBaseArray(int[ ] baseArray) throws RetroTectorException {
  	int[ ] complement = new int[baseArray.length];
		int b;
		for (int ct = 0; ct < baseArray.length; ct++ ) {
			b = baseArray[baseArray.length - ct - 1];
  		if (b == 4) {
  			complement[ct] = 4;
			} else if (b < 0) {
				throw new RetroTectorException("Utilities", "base code < 0");
  		} else {
  			complement[ct] = 3 - b;
  		}
		}
		return complement;
	} // end of complementBaseArray(int[ ])
	
/**
* @param	baseString	A String of base codes.
* @return	baseString complemented and reversed.
*/
	public final static String complementBaseString(String baseString) throws RetroTectorException {
		StringBuffer sb = new StringBuffer();
		Compactor bcomp = Compactor.BASECOMPACTOR;
		int b;
		for (int ct = baseString.length() - 1; ct >= 0; ct-- ) {
			b = bcomp.charToIntId(baseString.charAt(ct));
  		if (b >= 4) {
  			sb.append('n');
			} else if (b < 0) {
				throw new RetroTectorException("Utilities.complementBaseString", "illegal base:" + baseString.charAt(ct));
  		} else {
  			sb.append(bcomp.intToCharId(3 - b));
  		}
		}
		return sb.toString();
	} // end of complementBaseString(String)
	
/**
* @param	baseString	A String of bases.
* @return	An int array of the corresponding codes.
*/
	public final static int[ ] encodeBaseString(String baseString) throws RetroTectorException {
		int[ ] result = new int[baseString.length()];
		Compactor bcomp = Compactor.BASECOMPACTOR;
		int b;
		for (int ct = baseString.length() - 1; ct >= 0; ct-- ) {
			b = bcomp.charToIntId(baseString.charAt(ct));
  		if (b < 0) {
				throw new RetroTectorException("Utilities.encodeBaseString", "illegal base:" + baseString.charAt(ct));
  		} else {
  			result[ct] = b;
  		}
		}
		return result;
	} // end of encodeBaseString(String)
	
/**
* @param	acidString	A String of acids.
* @return	An int array of the corresponding codes.
*/
	public final static int[ ] encodeAcidString(String acidString) throws RetroTectorException {
		int[ ] result = new int[acidString.length()];
		Compactor bcomp = Compactor.ACIDCOMPACTOR;
		int b;
		for (int ct = acidString.length() - 1; ct >= 0; ct-- ) {
			b = bcomp.charToIntId(acidString.charAt(ct));
  		if (b < 0) {
				throw new RetroTectorException("Utilities.encodeAcidString", "illegal acid:" + acidString.charAt(ct));
  		} else {
  			result[ct] = b;
  		}
		}
		return result;
	} // end of encodeAcidString(String)
	
/**
* @param	s	A String, presumably of acid codes.
* @return	The number of glycosylation sites in it.
*/
	public final static int glycSitesInString(String s) {
		int count = 0;
		for (int i=0; i<s.length()-2; i++) {
			if (s.substring(i, i + 1).equalsIgnoreCase("n")) {
				if (s.substring(i + 2, i + 3).equalsIgnoreCase("s") | s.substring(i + 2, i + 3).equalsIgnoreCase("t")) {
					count++;
				}
			}
		}
		return count;
	} // end of glycSitesInString(String)
	
/**
* @param	basecodes		Int array to merize. Mers containing any ambiguous base are invalid (=-1).
* @param	startAt			First (internal) position in this to merize.
* @param	endAt				Last (internal) position in this to merize.
* @param	merLength		The length of the mers.
* @param	killAT			If true, mers consisting of only 'a' or only 't' base are invalid (=-1).
* @return	A long array of same length as basecodes, with mers between startAt and endAt. 
*/
	public final static long[ ] merize(int[ ] basecodes, int startAt, int endAt, int merLength, boolean killAT) throws RetroTectorException {
		if (merLength > 30) {
			throw new RetroTectorException("Utilities.merize()", "mer longer than 30");
		}
		
		long[ ] theMers = new long[basecodes.length];
		long mask = 0;
		long aaaa = 0;
		long tttt = 0;
		for (int m=0; m<merLength; m++) {
			mask = (mask << 2) + 3;
			aaaa = (mask << 2) + Compactor.BASECOMPACTOR.charToIntId('a');
			tttt = (mask << 2) + Compactor.BASECOMPACTOR.charToIntId('t');
		}
		long mer = 0;
		int ba;
		int shitcount = merLength;
		for (int i=endAt+merLength-1; i>=startAt; i--) {
			shitcount--;
			mer = (mer << 2) & mask;
			if (i >= basecodes.length) {
				shitcount = merLength;
			} else {
				ba = basecodes[i];
				if (ba > 3) {
					shitcount = merLength;
				} else {
					mer = mer | ba;
				}
				if (shitcount > 0) {
					theMers[i] = -1;
				} else if (killAT && (mer == aaaa)) {
					theMers[i] = -1;
				} else if (killAT && (mer == tttt)) {
					theMers[i] = -1;
				} else {
					theMers[i] = mer;
				}
			}
		}
		return theMers;
	} // end of merize(int[ ], int, int, int, boolean)
	
	
/**
* @param	template		The TemplatePackage to find hits by.
* @param	dnaMers			Merized DNA between firstInDNA and lastInDNA.
* @param	theDNA			The DNA to search in.
* @param	dnabases		Base codes of theDNA.
* @param	firstInDNA	Internal position in theDNA to start at.
* @param	lastInDNA		Internal position in theDNA to end at.
* @param	errMax			Largest acceptable fraction of non-identity in window.
* @param	merLength		Length of window to search with.
* @param	merStep			Step length in search.
* @param	minLength		Shorter hits than this are not recorded.
*/
	public final static TemplateHit[ ] hitsByTemplate(TemplatePackage template, long[ ] dnaMers, DNA theDNA, int[ ] dnabases, int firstInDNA, int lastInDNA, float errMax, int merLength, int merStep, int minLength) throws RetroTectorException {
// long timer = System.currentTimeMillis();
		if ((lastInDNA - firstInDNA) < minLength) {
			return null;
		}
		
		int ba;
		int templatep = 0;
		int chainp;
		TemplateHit hit;
		Stack hits = new Stack();
		long searchMer;
		int result;
		boolean shit;
		for (int merpos = merLength; merpos<(template.TEMPLATEMERS.length-2*merLength); merpos += merStep) {
			if (merpos > templatep) {
				searchMer = template.TEMPLATEMERS[merpos];
				if (searchMer >= 0) {
					chainp = 0;
					for (int p=firstInDNA; p<=lastInDNA-merLength*2; p++) {
						if ((p >= chainp) && (dnaMers[p] == searchMer)) {
							hit = new TemplateHit(merLength, template.TEMPLATECODES, merpos, theDNA.TRANSLATOR, dnabases, p, firstInDNA, lastInDNA, errMax, template.TEMPLATENAME);
							if (hit != null) {
								chainp = Math.max(chainp, hit.POSINTHEDNA + hit.HITLENGTH + 1);
								templatep = Math.max(templatep, hit.POSINTEMPLATE + hit.HITLENGTH + 1);
								if (hit.HITLENGTH >= minLength) {
									hits.push(hit);
								}
							}
						}
					}
				}
			}
		}
		
		boolean nothingdone = false;
		while (!nothingdone) {
			nothingdone = true;
			for (int i=0; i<hits.size(); i++) {
				for (int j=i+1; j<hits.size(); j++) {
					hit = mergeTemplateHits((TemplateHit) hits.elementAt(i), (TemplateHit) hits.elementAt(j));
					if (hit != null) {
						hits.remove(j);
						hits.remove(i);
						hits.add(i, hit);
						nothingdone = false;
					}
				}
			}
		}
		TemplateHit[ ] hita = new TemplateHit[hits.size()];
		hits.copyInto(hita);
		return hita;
	} // end of hitsByTemplate(TemplatePackage, long[ ], DNA, int[ ], int, int, float, int, int, int)
	

/**
* @param	ar	Array of Scorable, which will be sorted in descending order of fetchScore().
*/
	public final static void sort(Scorable[ ] ar) throws RetroTectorException {
		quicksort(ar, 0, ar.length - 1);
		if (!checkArrayOrder(ar)) {
			throw new RetroTectorException("Utilities", "Array out of order");
		}
	} // end of sort(Scorable[ ])
	
/**
* @param	arr	Array of Scorable, supposed to be sorted in descending order of fetchScore().
* @return	True if so ordered.
*/
	public final static boolean checkArrayOrder(Scorable[ ] arr) {
		if ((arr == null) || (arr.length == 0)) {
			return true;
		}
		float x = arr[0].fetchScore();
		float y;
		for (int i=1; i<arr.length; i++) {
			y = arr[i].fetchScore();
			if (x < y) {
				return false;
			} else {
				x = y;
			}
		}
		return true;
	} // end of checkArrayOrder(Scorable[ ])
	
// sorts according to Quicksort
	private final static void quicksort(Scorable[ ] a, int p, int r) throws RetroTectorException {
		int q;
		if (p < r) {
			q = partition(a, p, r);
			quicksort(a, p, q);
			quicksort(a, q + 1, r);
		}
	} // end of quicksort(Scorable[ ], int, int)
	
	private final static int partition(Scorable[ ] a, int p, int r) throws RetroTectorException {
		float x = a[p].fetchScore();
		if (Float.isNaN(x)) {
			throw new RetroTectorException("quicksort", "NaN score at " + p);
		}
		Scorable temp;
		int i = p - 1;
		int j = r + 1;
		while (true) {
			do {
				j--;
			} while (a[j].fetchScore() < x);
			do {
				i++;
			} while (a[i].fetchScore() > x);
			if (i < j) {
				temp = a[i];
				a[i] = a[j];
				a[j] = temp;
			} else {
				return j;
			}
		}
	} // end of partition(Scorable[ ], int, int)
	
/**
* For output of alignBases.
* seq1 parameter with gaps provided by align.
*/
	public static String aline1;
	
/**
* For output of alignBases.
* seq2 parameter with gaps provided by align.
*/
	public static String aline2;
	
/**
* For output of alignBases.
* Number of identities between aline1 and aline2.
*/
	public static int identityCount;
		
/**
* For output of alignBases.
* Number of nonidentities between aline1 and aline2.
*/
	public static int nonIdentityCount;
		
/**
* For output of alignBases.
* Midpoint of identities in aline1.
*/
	public static int midpoint1;
		
/**
* For output of alignBases.
* Midpoint of identities in aline2.
*/
	public static int midpoint2;
		
/**
* For Huang-style alignment of two base sequences.
* @param	seq1				Array of base codes. seq1[0] is dummy.
* @param	seq2				Array of base codes. seq2[0] is dummy.
* @param	mid					Sum of indices of anchor points. If <0, no bonus for tg or ca pairing.
* @param	identscore	Score for one identity between sequences.
* @param	pairscore		Bonus for 'tg' or 'ca' in both sequences
* @param	gappenalty	Penalty for starting a gap.
* @param	extpenalty	Penalty for extending a gap by one.
* @return Score for alignment.
*/
	public final static int alignBases(char[ ] seq1, char[ ] seq2, int mid, int identscore, int pairscore, int gappenalty, int extpenalty) throws RetroTectorException {
	
		int n = seq1.length - 1;
		int m = seq2.length - 1;
	
		int bestv = Integer.MIN_VALUE;
		int besti = -1;
		int bestj = -1;
		int g;
		try {
			int[ ][ ] v = new int[n + 1][m + 1];
			short[ ][ ] links = new short[n + 1][m + 1];
			int[ ][ ] e = new int[n + 1][ ];
			int[ ][ ] f = new int[n + 1][ ];
			f[0] = new int[m + 1];

			for (int fi=0; fi<=m; fi++) {
				f[0][fi] = -gappenalty - fi * extpenalty;
			}
		
			for (int i=1; i<=n; i++) {
				e[i - 1] = null;
				e[i] = new int[m + 1];
				e[i][0] =  -gappenalty - i * extpenalty;
				f[i] = new int[m + 1];
				if (i > 1) {
					f[i - 2] = null;
				}
					
				for (int j=1; j<=m; j++) {
					e[i][j] = Math.max(e[i][j - 1], v[i][j - 1] - gappenalty) - extpenalty;
					f[i][j] = Math.max(f[i - 1][j], v[i - 1][j] - gappenalty) - extpenalty;
					if (seq1[i] != seq2[j]) {
						g = v[i - 1][j - 1];
					} else if ((mid >= 0) && (seq1[i] == 'g') && (i + j <= mid) && (links[i - 1][j - 1] == 1) && (seq1[i - 1] == 't')) {
						g =  v[i - 1][j - 1] + pairscore;
					} else if ((mid >= 0) && (seq1[i] == 'a') && (i + j > mid) && (links[i - 1][j - 1] == 1) && (seq1[i - 1] == 'c')) {
						g =  v[i - 1][j - 1] + pairscore;
					} else {
						g =  v[i - 1][j - 1] + identscore;
					}
					if ((g >= e[i][j]) & (g >= f[i][j])) {
						v[i][j] = g;
						links[i][j] = 1;
					} else if (e[i][j] >= f[i][j]) {
						v[i][j] = e[i][j];
						links[i][j] = 2;
					} else {
						v[i][j] = f[i][j];
						links[i][j] = 3;
					}
				} // j loop
				if (v[i][m] > bestv) {
					bestv = v[i][m];
					besti = i;
					bestj = m;
				}
			} // i loop
		
			for (int bj=1; bj<=m; bj++) {
				try {
					if (v[n][bj] > bestv) {
						bestv = v[n][bj];
						besti = n;
						bestj = bj;
					}
				} catch (ArrayIndexOutOfBoundsException ae) {
					throw ae;
				}
			}
			
			int ii = n;
			int jj = m;
			int vv;
			StringBuffer sb1 = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();
			identityCount = 0;
			nonIdentityCount = 0;
			int possum1 = 0;
			int possum2 = 0;
			while (ii > besti) {
				sb1.insert(0, seq1[ii]);
				sb2.insert(0, '-');
				ii--;
			}
			while (jj > bestj) {
				sb1.insert(0, '-');
				sb2.insert(0, seq2[jj]);
				jj--;
			}
			while ((ii > 0) | (jj > 0)) {
				if (links[ii][jj] == 1) {
					sb1.insert(0, seq1[ii]);
					sb2.insert(0, seq2[jj]);
					if (seq1[ii] == seq2[jj]) {
						identityCount++;
						possum1 += ii;
						possum2 += jj;
					} else {
						nonIdentityCount++;
					}
					ii--;
					jj--;
				} else if (links[ii][jj] == 2) {
					vv = v[ii][jj] + gappenalty;
					do {
						sb1.insert(0, '-');
						sb2.insert(0, seq2[jj]);
						jj--;
						vv += extpenalty;
					} while (v[ii][jj] != vv);
				} else if (links[ii][jj] == 3) {
					vv = v[ii][jj] + gappenalty;
					do {
						sb1.insert(0, seq1[ii]);
						sb2.insert(0, '-');
						ii--;
						vv += extpenalty;
					} while (v[ii][jj] != vv);
				} else if (ii == 0) {
					sb1.insert(0, '-');
					sb2.insert(0, seq2[jj]);
					jj--;
				} else {
					sb1.insert(0, seq1[ii]);
					sb2.insert(0, '-');
					ii--;
				}
			}
		
			aline1 = sb1.toString();
			midpoint1 = possum1 / identityCount;
			aline2 = sb2.toString();
			midpoint2 = possum2 / identityCount;
		} catch (OutOfMemoryError me) {
			throw new RetroTectorException("Utilities.alignBases", OUTOFMEMORY);
		}
		return bestv;
	} // end of alignBases(char[ ], char[ ], int, int, int, int, int)

/**
* For Huang-style alignment of two acid sequences.
* @param	seq1				Array of acid codes. seq1[0] is dummy.
* @param	seq2				Array of acid codes. seq2[0] is dummy.
* @param	gappenalty	Penalty for starting a gap.
* @param	extpenalty	Penalty for extending a gap by one.
* @return Score for alignment.
*/
	public final static int alignAcids(int[ ] seq1, int[ ] seq2, int gappenalty, int extpenalty) {
	
		AcidMatrix acidMatrix = AcidMatrix.refreshAcidMatrix(2.0f);
	
		int n = seq1.length - 1;
		int m = seq2.length - 1;
	
		int g;
		int[ ][ ] v = new int[n + 1][m + 1];
		short[ ][ ] links = new short[n + 1][m + 1];
		int[ ][ ] e = new int[n + 1][ ];
		int[ ][ ] f = new int[n + 1][ ];
		f[0] = new int[m + 1];

		for (int fi=0; fi<=m; fi++) {
			f[0][fi] = -gappenalty - fi * extpenalty;
		}
	
		for (int i=1; i<=n; i++) {
			e[i - 1] = null;
			e[i] = new int[m + 1];
			e[i][0] =  -gappenalty - i * extpenalty;
			f[i] = new int[m + 1];
			if (i > 1) {
				f[i - 2] = null;
			}
				
			for (int j=1; j<=m; j++) {
				e[i][j] = Math.max(e[i][j - 1], v[i][j - 1] - gappenalty) - extpenalty;
				f[i][j] = Math.max(f[i - 1][j], v[i - 1][j] - gappenalty) - extpenalty;
				g =  v[i - 1][j - 1] + Math.round(acidMatrix.floatMatrix[seq1[i]][seq2[j]]);
				if ((g >= e[i][j]) & (g >= f[i][j])) {
					v[i][j] = g;
					links[i][j] = 1;
				} else if (e[i][j] >= f[i][j]) {
					v[i][j] = e[i][j];
					links[i][j] = 2;
				} else {
					v[i][j] = f[i][j];
					links[i][j] = 3;
				}
			} // j loop
		} // i loop
		
		int bestv = Integer.MIN_VALUE;
		int besti = -1;
		int bestj = -1;
		for (int bi=1; bi<=n; bi++) {
			if (v[bi][m] > bestv) {
				bestv = v[bi][m];
				besti = bi;
				bestj = m;
			}
		}
		for (int bj=1; bj<=m; bj++) {
			try {
				if (v[n][bj] > bestv) {
					bestv = v[n][bj];
					besti = n;
					bestj = bj;
				}
			} catch (ArrayIndexOutOfBoundsException ae) {
				throw ae;
			}
		}
		
		int ii = n;
		int jj = m;
		int vv;
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		identityCount = 0;
		int possum1 = 0;
		int possum2 = 0;
		while (ii > besti) {
			sb1.insert(0, seq1[ii]);
			sb2.insert(0, '-');
			ii--;
		}
		while (jj > bestj) {
			sb1.insert(0, '-');
			sb2.insert(0, seq2[jj]);
			jj--;
		}
		while ((ii > 0) | (jj > 0)) {
			if (links[ii][jj] == 1) {
				sb1.insert(0, seq1[ii]);
				sb2.insert(0, seq2[jj]);
				if (seq1[ii] == seq2[jj]) {
					identityCount++;
					possum1 += ii;
					possum2 += jj;
				}
				ii--;
				jj--;
			} else if (links[ii][jj] == 2) {
				vv = v[ii][jj] + gappenalty;
				do {
					sb1.insert(0, '-');
					sb2.insert(0, seq2[jj]);
					jj--;
					vv += extpenalty;
				} while (v[ii][jj] != vv);
			} else if (links[ii][jj] == 3) {
				vv = v[ii][jj] + gappenalty;
				do {
					sb1.insert(0, seq1[ii]);
					sb2.insert(0, '-');
					ii--;
					vv += extpenalty;
				} while (v[ii][jj] != vv);
			} else if (ii == 0) {
				sb1.insert(0, '-');
				sb2.insert(0, seq2[jj]);
				jj--;
			} else {
				sb1.insert(0, seq1[ii]);
				sb2.insert(0, '-');
				ii--;
			}
		}
		
		aline1 = sb1.toString();
		midpoint1 = possum1 / identityCount;
		aline2 = sb2.toString();
		midpoint2 = possum2 / identityCount;
		return bestv;
	} // end of alignAcids(int[ ], int[ ], int, int)

/**
* @param	putein		A Putein
* @param	proteins	From database.getPolProteins() or forretrotector.polproteins
* @return	String describing element in proteins most similar to putein
*/
	public final static String findBestProtein(String putein, String[ ][ ] proteins) throws RetroTectorException {
		RetroTectorEngine.setInfoField("Searching for best Pol protein");
		String bestname = null;
		float score;
		float scoresum = 0;
		int n = 0;
		float bestscore = 0;
		for (int i=0; i<proteins.length; i++) {
			RetroTectorEngine.showProgress();
			score = Utilities.alignAcids(Utilities.encodeAcidString("x" + proteins[i][0]), Utilities.encodeAcidString("x" + putein), 10, 2);
			scoresum += score;
			n++;
			if (score > bestscore) {
				bestscore = score;
				bestname = proteins[i][1];
			}
		}
		RetroTectorEngine.setInfoField("");
		return bestname + " " + bestscore + "/" + (scoresum / n);
	} // end of findBestProtein(String, String[ ][ ])

/**
* @param	dna								DNA to search in between
* @param	firstpos					and
* @param	lastpos						for similarities to elements in
* @param	repbasetemplates	consensuses from RepBase, from database.getRepBaseTemplates() or forretrotector.repbasetemplates
* @return	String describing finds
*/
	public final static String repBaseFind(DNA dna, int firstpos, int lastpos, Utilities.TemplatePackage[ ] repbasetemplates, boolean ltrsOnly) throws RetroTectorException {
		RetroTectorEngine.setInfoField("Searching for RepBase hits");
		int merLength = 11;
		int[ ] basecodes = dna.getBaseCodes();
		long[ ] dnaMers = Utilities.merize(basecodes, firstpos, lastpos, merLength, true);
		StringBuffer sb = new StringBuffer();
		String tes;
		Utilities.TemplatePackage template;
		Utilities.TemplateHit[ ] hits;
		int ci;
		Compactor comp = Compactor.BASECOMPACTOR;
		for (int te=0; te<repbasetemplates.length; te++) {
			if (!ltrsOnly | ((repbasetemplates[te].TEMPLATESECINFO != null) && (repbasetemplates[te].TEMPLATESECINFO.contains("LTR")))) {
				RetroTectorEngine.showProgress();
				template = repbasetemplates[te];				
				hits = Utilities.hitsByTemplate(template, dnaMers, dna, basecodes, firstpos, lastpos, 0.38f, merLength, 23, 50);
				for (int hiti=0; hiti<hits.length; hiti++) {
					sb.append(hits[hiti].toString());
					sb.append("\n");
				}
			}
		}
		RetroTectorEngine.setInfoField("");
		return sb.toString();
	} // end of repBaseFind(DNA, int, int, Utilities.TemplatePackage[ ])

/**
* @param	dna								DNA to search in between
* @param	firstpos					and
* @param	lastpos						for similarities to elements in
* @param	repbasetemplates	reference virus sequences, from database.getRefRVs() or forretrotector.refrvs
* @return	String describing best fitting reference virus
*/
	public final static String findBestRV(DNA dna, int firstpos, int lastpos, Utilities.TemplatePackage[ ] refrvs) throws RetroTectorException {
		RetroTectorEngine.setInfoField("Searching for best reference RV");
		String thisseq = dna.subString(firstpos, lastpos, true);
		int merLength = 11;
		int[ ] basecodes = dna.getBaseCodes();
		long[ ] dnaMers = Utilities.merize(basecodes, firstpos, lastpos, merLength, true);
		int bestscore = 0;
		int f;
		int besti = -1;
		Utilities.TemplatePackage template;
		Utilities.TemplateHit[ ] hits;
		for (int i=0; i<refrvs.length; i++) {
			RetroTectorEngine.showProgress();
			template = refrvs[i];				
			hits = Utilities.hitsByTemplate(template, dnaMers, dna, basecodes, firstpos, lastpos, 0.38f, merLength, 23, 50);
			f = 0;
			for (int h=0; h<hits.length; h++) {
				f += hits[h].HITLENGTH;
			}
			if (f > bestscore) {
				bestscore = f;
				besti = i;
			}
		}
		if (besti < 0) {
			return "";
		}
		try {
			Utilities.alignBases((" " + thisseq).toCharArray(), (" " + refrvs[besti].TEMPLATESTRING).toCharArray(), -1, 20, 0, 40, 2);
		} catch (RetroTectorException rte2) {
			if (rte2.messagePart(1).equals(Utilities.OUTOFMEMORY)) {
				RetroTectorEngine.setInfoField("");
				return refrvs[besti].TEMPLATENAME + " ?% " + (Utilities.midpoint2 - Utilities.midpoint1);
			} else {
				throw rte2;
			}
		}
		float ff = 100.0f * Utilities.identityCount / (Utilities.identityCount + Utilities.nonIdentityCount);
		RetroTectorEngine.setInfoField("");
		return refrvs[besti].TEMPLATENAME + " " + Math.round(ff) + "% " + (Utilities.midpoint2 - Utilities.midpoint1);
	} // end of findBestRV(DNA, int, int, Utilities.TemplatePackage[ ])
	
} // end of Utilities
