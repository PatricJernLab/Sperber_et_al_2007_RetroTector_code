/*
* @(#)DNA.java	1.0 97/07/22
* 
* Copyright (©) 1997-2006, Tore Eriksson & G. Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Tore Eriksson & Gšran Sperber
* @version 12/10 -06
* Beautified 12/10 -06
*/

package retrotector;

import java.util.*;
import java.io.*;

/**
* Class with methods for loading and accessing sequence information. The useful bases
* are stored one per byte.
* The byte format is:
* 7 (MSB)	set if this or either of next two bases ambiguous
* 6,5,4		code for base at this position (4 for ambiguous base)
* 3,2			code for base at next position
* 1,0			code for base at position+2
* bits 0-3 are meaningless if bit 7 is set, otherwise bits 0-5 form the
* code for the codon starting at this position.
*
* ALUs, LINEs and similar may have been marked as Inserts and not used.
*/
public class DNA extends ParameterUser {

/**
* Class describing a not useful part (ALU, LINE, SINE).
*/
	public static class Insert {
	
/**
* The external position of the first base in the Insert.
*/
		public final int INSERTFIRST;
	
/**
* The external position of the last base in the Insert.
*/
		public final int INSERTLAST;

/**
* A unique String identifying this Insert.
*/
		public final String INSERTIDENTIFIER;

/**
* A String of the (useless) bases contained in this Insert.
*/
		public final String INSERTCONTENTS;
		
		
/**
* Constructor.
* @param	firstpos	See INSERTFIRST.
* @param	lastpos		See INSERTLAST.
* @param	ident			See INSERTIDENTIFIER.
* @param	cont			See INSERTCONTENTS.
*/
		Insert(int firstpos, int lastpos, String ident, String cont) throws RetroTectorException {
			if ((cont != null) && ((Math.abs(firstpos - lastpos) + 1) != cont.length())){
				RetroTectorException.sendError(this, "Insert length mismatch");
			}
			INSERTFIRST = firstpos;
			INSERTLAST = lastpos;
			INSERTIDENTIFIER = ident;
			INSERTCONTENTS = cont;
		} // end of Insert.constructor(int, int, String, String)
		
/**
* Constructor, using output of toStrings (concatenated to a String).
* @param	dna				String containing Insert decription somewhere.
* @param	firstpos	Index in dna of first character in Insert decription.
* @param	lastpos		Index in dna of last character in Insert decription.
* @param	startat		External address of first base in Insert.
*/
		Insert(String dna, int firstpos, int lastpos, int startat) throws RetroTectorException {
			INSERTFIRST = startat;
			if (dna.charAt(firstpos) != INSERTLEADCHAR) {
				throw new RetroTectorException("Insert", "Not starting with " + INSERTLEADCHAR);
			}
			if (dna.charAt(lastpos) != INSERTTRAILCHAR) {
				throw new RetroTectorException("Insert", "Not ending with " + INSERTTRAILCHAR);
			}
			int index1 = dna.indexOf(IDENTDELIMITER, firstpos); // end of leading identifier
			int index2 = dna.lastIndexOf(IDENTDELIMITER, lastpos); // start of trailing identifier
			if ((index1 < 0) | (index2 < 0) | (index2 <= index1)) {
				throw new RetroTectorException("Insert", IDENTDELIMITER + " in wrong place");
			}
			INSERTIDENTIFIER = dna.substring(firstpos + 1, index1);
			if (!INSERTIDENTIFIER.equals(dna.substring(index2 + 1, lastpos))) {
				throw new RetroTectorException("Insert", "Identifier mismatch", INSERTIDENTIFIER, dna.substring(index2 + 1, lastpos));
			}
			StringBuffer sb = new StringBuffer();
			Compactor comp = Compactor.BASECOMPACTOR;
			for (int p=index1+1; p<index2; p++) {
				if (comp.charToIntId(dna.charAt(p)) >= 0) {
					sb.append(dna.charAt(p));
				}
			}
			INSERTCONTENTS = sb.toString();
			INSERTLAST = INSERTFIRST + INSERTCONTENTS.length() - 1;
		} // end of Insert.constructor(String, int, int, int)
		
/**
* @return String array of form:<BR>
*	[INSERTIDENTIFIER:<BR>
* bases 60 per line, with positions<BR>
* :INSERTIDENTIFIER]<BR>
*/
		public final String[ ] toStrings() {
			Stack st = new Stack();
			StringBuffer sb;
			st.push(INSERTLEADCHAR + INSERTIDENTIFIER + IDENTDELIMITER);
			int p = 0;
			while (p < INSERTCONTENTS.length()) {
				sb = new StringBuffer();
				sb.append("   ");
				sb.append(INSERTFIRST + p);
				for (int i1=0; i1<6; i1++) {
					sb.append(" ");
					for (int i2=0; i2<10; i2++) {
						if (p < INSERTCONTENTS.length()) {
							sb.append(INSERTCONTENTS.charAt(p));
							p++;
						}
					}
				}
				st.push(sb.toString());
			}
			st.push(IDENTDELIMITER + INSERTIDENTIFIER + INSERTTRAILCHAR);
			String[ ] result = new String[st.size()];
			st.copyInto(result);
			return result;
		} // end of Insert.toStrings()
		
/**
*	@return	An Insert complementary to this.
*/
		final Insert complementInsert() throws RetroTectorException {
			StringBuffer sb = new StringBuffer();
			Compactor comp = Compactor.BASECOMPACTOR;
			int c;
			for (int i=INSERTCONTENTS.length()-1; i>=0; i--) {
				c = comp.charToIntId(INSERTCONTENTS.charAt(i));
				if (c < 4) {
					c = (~c) & 3;
				}
				sb.append(comp.intToCharId(c));
			}
			return new Insert(INSERTLAST, INSERTFIRST, INSERTIDENTIFIER, sb.toString());
		} // end of Insert.complementInsert()
		
	} // end of Insert
	
	
/**
* Class describing a useful part.
*/
  public static class Contig {
	
/**
* The DNA to which this relates.
*/
		public final DNA PARENTDNA;
  
/**
*	Internal position of first base.
*/
    public final int FIRSTINTERNAL; 
  
/**
*	Internal position of last base.
*/
    public final int LASTINTERNAL;
  
/**
*	External position corresponding to FIRSTINTERNAL.
*/
    public final int FIRSTEXTERNAL;
		  
/**
*	External position corresponding to LASTINTERNAL.
*/
    public final int LASTEXTERNAL;
  
/**
* +1 if LASTEXTERNAL > FIRSTEXTERNAL, otherwise -1.
*/
    public final int DIRECTION;
  
/**
*	Insert in succeeding gap, or null.
*/
    public final Insert AFTERINSERT;
    
/**
* Constructor.
* @param	 firstInt	Internal position of first base.
* @param	 lastInt	Internal position of last base.
* @param	 firstExt	External position corresponding to firstInt.
* @param	 lastExt	External position corresponding to lastInt.
* @param	 insert		Insert in succeeding gap, or null.
*/
    Contig(DNA parentdna, int firstInt, int lastInt, int firstExt, int lastExt, Insert insert) throws RetroTectorException {
			PARENTDNA = parentdna;
      FIRSTINTERNAL = firstInt;
      LASTINTERNAL = lastInt;
      FIRSTEXTERNAL = firstExt;
      LASTEXTERNAL = lastExt;
      AFTERINSERT = insert;
      if ((LASTEXTERNAL >= FIRSTEXTERNAL) & (LASTINTERNAL >= FIRSTINTERNAL)) {
        DIRECTION = 1;
      } else if ((LASTEXTERNAL < FIRSTEXTERNAL) & (LASTINTERNAL < FIRSTINTERNAL)) {
        DIRECTION = 1;
      } else {
        DIRECTION = -1;
      }
      if ((LASTINTERNAL - FIRSTINTERNAL) != ((LASTEXTERNAL - FIRSTEXTERNAL) * DIRECTION)) {
        RetroTectorException.sendError(this, "Incompatible parameters: " + FIRSTINTERNAL + " " + LASTINTERNAL + " " + FIRSTEXTERNAL + " " + LASTEXTERNAL + " " + DIRECTION);
      }
    } // end of Contig.constructor(DNA, int, int, int, int, Insert)
    
/**
*	@param	nextInsert	The Insert of the Contig preceding this, or null.
* @return	A Contig complementary to this.
*/
    final Contig complementContig(Insert nextInsert) throws RetroTectorException {
			if (nextInsert == null) {
				return new Contig(PARENTDNA, PARENTDNA.LENGTH - 1 - LASTINTERNAL, PARENTDNA.LENGTH - 1 - FIRSTINTERNAL, LASTEXTERNAL, FIRSTEXTERNAL, null);
			} else {
				return new Contig(PARENTDNA, PARENTDNA.LENGTH - 1 - LASTINTERNAL, PARENTDNA.LENGTH - 1 - FIRSTINTERNAL, LASTEXTERNAL, FIRSTEXTERNAL, nextInsert.complementInsert());
			}
    } // end of Contig.complementContig(Insert)
    
/**
* Pushes a text description of this (all or part) Contig and succeeding Insert, if any.
* @param	first	First internal position in DNA to use.
* @param	last	Last internal position in DNA to use.
* @param	lineStack	Stack of text lines to push description on.
*/
    final void toStrings(int first, int last, Stack lineStack) {
      if ((first > last) | (last < FIRSTINTERNAL) | (first > LASTINTERNAL)) {
        return;
      }
      int fi = Math.max(first, FIRSTINTERNAL);
      int la = Math.min(last, LASTINTERNAL);
			if (la >= fi) {
				StringBuffer line = new StringBuffer(100);
				line.append(contigExternalize(fi));
				line.append(" ");
				int tencounter = 0; // count bases in group of 10
				int sixcounter = 0; // counts groups of 10 in line
				for (int inte=fi; inte<=la; inte++) {
					if (tencounter == 10) {
						tencounter = 0;
						sixcounter++;
						if (sixcounter==6) {
							sixcounter = 0;
							lineStack.push(line.toString());
							line = new StringBuffer(100);
							line.append(contigExternalize(inte));
							line.append(" ");
						} else {
							line.append(" ");
						}
					}
					line.append(PARENTDNA.getBase(inte));
					tencounter++;
				}
				lineStack.push(line.toString());
			}
      if ((AFTERINSERT != null) & (last >= LASTINTERNAL)) {
				String[ ] ss = AFTERINSERT.toStrings();
				for (int s=0; s<ss.length; s++) {
					lineStack.push(ss[s]);
				}
      }
      return;
    } // end of Contig.toStrings(int, int, Stack)
		
/**
* @param	ipos	An internal position.
* @return	The corresponding external position, or Integer.MIN_VALUE if ipos not within this.
*/
    private final int contigExternalize(int ipos) {
      if ((ipos < FIRSTINTERNAL) | (ipos > LASTINTERNAL)) {
        return Integer.MIN_VALUE;
      } else {
        return FIRSTEXTERNAL + DIRECTION * (ipos - FIRSTINTERNAL);
      }
    } // end of Contig.contigExternalize(int)
    
/**
* @param	epos	An external position.
* @return	The corresponding internal position, or Integer.MIN_VALUE if epos not within this.
*/
    private final int contigInternalize(int epos) {
      if (DIRECTION > 0) {
        if ((epos < FIRSTEXTERNAL) | (epos > LASTEXTERNAL)) {
          return Integer.MIN_VALUE;
        } else {
          return FIRSTINTERNAL + epos - FIRSTEXTERNAL;
        }
      } else {
        if ((epos > FIRSTEXTERNAL) | (epos < LASTEXTERNAL)) {
          return Integer.MIN_VALUE;
        } else {
          return FIRSTINTERNAL + FIRSTEXTERNAL - epos;
        }
      }
    } // end of Contig.contigInternalize(int)
    
/**
*	@param	frompos	An internal base position.
*	@param	topos		An internal base position.
*	@return	A String of the bases in this (including AFTERINSERT) between frompos and topos (inclusive).
*/
		private final String contentString(int frompos, int topos) {
			if (topos < FIRSTINTERNAL) {
				return "";
			} else if (frompos > LASTINTERNAL) {
				return "";
			} else {
				String s = PARENTDNA.subString(Math.max(FIRSTINTERNAL, frompos), Math.min(LASTINTERNAL, topos), true);
				if ((topos > LASTINTERNAL) & (AFTERINSERT != null)) {
					s += AFTERINSERT.INSERTCONTENTS;
				}
				return s;
			}
		} // end of Contig.contentString(int, int)
		
		private final String debugString() {
			return "FIRSTINTERNAL:" + FIRSTINTERNAL + " FIRSTEXTERNAL:" + FIRSTEXTERNAL + " LASTINTERNAL:" + LASTINTERNAL + " LASTEXTERNAL:" + LASTEXTERNAL;
		} // end of debugString()
    
  } // end of Contig
	
	
/**
* Class to handle translation between internal and external positions in DNA.
*/
	public static class Translator {
	
/**
* Defines the translation.
*/
		public final Contig[ ] CONTIGS;
		
		private DNA sourceDNA = null;
		
/**
* @param	contigs	->CONTIGS.
*/
		public Translator(Contig[ ] contigs, DNA dna) throws RetroTectorException {
		
			CONTIGS = contigs;
			sourceDNA = dna;
			checkContigs();
		} // end of Translator.constructor(contig[ ], DNA)

/**
* @param	s	Output from toString().
*/
		public Translator(String s) throws RetroTectorException {
		
			String[ ] ss = Utilities.expandLines(s);
			CONTIGS = new Contig[ss.length];
			int ind = ss[0].indexOf(" ");
// first in next Contig
			int nextfi = Utilities.decodeInt(ss[0].substring(0, ind).trim());
			ss[0] = ss[0].substring(ind).trim();
			int intern = 0; // internal position counter
			int fi; // first in this Contig
			int la; // last in this Contig
			int filadiff; // Abs(la - fi)
			Insert ins; // succeeding Insert
			String insid; // id string ins
			int direction = 1;
			for (int i=0; i<ss.length-1; i++) {
				fi = nextfi;
				ind = ss[i].indexOf(" ");
				la = Utilities.decodeInt(ss[i].substring(0, ind).trim());
				filadiff = la - fi;
				if (filadiff < -1) {
					direction = -1;
					filadiff = -filadiff;
				}
				insid = ss[i].substring(ind).trim();
				ind = ss[i + 1].indexOf(" ");
				nextfi = Utilities.decodeInt(ss[i + 1].substring(0, ind).trim());
				ss[i + 1] = ss[i + 1].substring(ind).trim();
				ins = new Insert(la + direction, nextfi - direction, insid, null);
				CONTIGS[i] = new Contig(null, intern, intern + filadiff, fi, la, ins);
				intern = intern + filadiff + 1;
			}
			fi = nextfi;
			la = Utilities.decodeInt(ss[ss.length - 1].trim());
			filadiff = Math.abs(la - fi);
			CONTIGS[CONTIGS.length - 1] = new Contig(null, intern, intern + filadiff, fi, la, null);
			checkContigs();
		} // end of Translator.constructor(String)

		
// just checking that contigs are compatible. 
		private final void checkContigs() throws RetroTectorException {
			int one = 1;
			if ((CONTIGS[0].PARENTDNA != null) && !CONTIGS[0].PARENTDNA.PRIMARYSTRAND) {
				one = -1;
			} else {
				one = CONTIGS[0].DIRECTION;
			}
			if (CONTIGS[0].FIRSTINTERNAL != 0) {
				RetroTectorException.sendError(this, "First Contig not at 0");
			}
			for (int i=1; i<CONTIGS.length; i++) {
				if ((CONTIGS[i - 1].LASTINTERNAL + 1) != CONTIGS[i].FIRSTINTERNAL) {
					RetroTectorException.sendError(this, "Contig mismatch " + CONTIGS[i - 1].LASTINTERNAL +
								" " + CONTIGS[i].FIRSTINTERNAL);
				}
				if (CONTIGS[i - 1].AFTERINSERT.INSERTFIRST != (CONTIGS[i - 1].LASTEXTERNAL + one)) {
System.err.println(CONTIGS[0].FIRSTEXTERNAL);
System.err.println(CONTIGS[0].LASTEXTERNAL);
System.err.println(one);
System.err.println(i);
					RetroTectorException.sendError(this, "Contig-Insert mismatch " + CONTIGS[i - 1].AFTERINSERT.INSERTFIRST +
								" " + CONTIGS[i - 1].LASTEXTERNAL);
				}
				if (CONTIGS[i - 1].AFTERINSERT.INSERTLAST != (CONTIGS[i].FIRSTEXTERNAL - one)) {
					RetroTectorException.sendError(this, "Insert-Contig mismatch " + CONTIGS[i - 1].AFTERINSERT.INSERTLAST +
								" " + CONTIGS[i].FIRSTEXTERNAL);
				}
			}
		} // end of Translator.checkContigs()
  
/**
*	@param	pos	An external base position.
*	@return	The corresponding internal position, or Integer.MIN_VALUE.
*/
		public final int internalize(int pos) {
			int ipos;
			for (int i=0; i<CONTIGS.length; i++) {
				if ((ipos = CONTIGS[i].contigInternalize(pos)) != Integer.MIN_VALUE) {
					return ipos;
				}
			}
			return Integer.MIN_VALUE;
		} // end of Translator.internalize
		
/**
*	@param	pos	An internal base position.
*	@return	The corresponding external position, or Integer.MIN_VALUE.
*/
		public final int externalize(int pos) {
			int epos;
			for (int i=0; i<CONTIGS.length; i++) {
				if ((epos = CONTIGS[i].contigExternalize(pos)) != Integer.MIN_VALUE) {
					return epos;
				}
			}
			return Integer.MIN_VALUE;
		} // end of Translator.externalize
	
/**
* @return	\n-separated lines of FIRSTEXTERNAL, LASTEXTERNAL and AFTERINSERT.INSERTIDENTIFIER.
*/
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<CONTIGS.length-1; i++) {
				sb.append(CONTIGS[i].FIRSTEXTERNAL);
				sb.append(" ");
				sb.append(CONTIGS[i].LASTEXTERNAL);
				sb.append(" ");
				sb.append(CONTIGS[i].AFTERINSERT.INSERTIDENTIFIER);
				sb.append("\n");
			}
			sb.append(CONTIGS[CONTIGS.length - 1].FIRSTEXTERNAL);
			sb.append(" ");
			sb.append(CONTIGS[CONTIGS.length - 1].LASTEXTERNAL);
			return sb.toString();
		} // end of Translator.toString()
  
/**
* Like toString()	but restricted to the region firstExternal - lastExternal
*/
		public String toString(int firstExternal, int lastExternal) throws RetroTectorException {
			int ipos;
			int cont1;
			int cont2;
			for (cont1=0; (cont1<CONTIGS.length) && (CONTIGS[cont1].contigInternalize(firstExternal) == Integer.MIN_VALUE); cont1++) {
			}
			for (cont2=0; (cont2<CONTIGS.length) && (CONTIGS[cont2].contigInternalize(lastExternal) == Integer.MIN_VALUE); cont2++) {
			}
			if (cont2 < cont1) {
				String s = "undefined";
				if (sourceDNA != null) {
					s = sourceDNA.filePath;
				}
				throw new RetroTectorException("Translator.toString", "Wrong order of arguments", "  " + cont1 + ">" + cont2, "   " + firstExternal + " " + lastExternal, s);
			} else if (cont1 == cont2) {
				return "" + firstExternal + " " + lastExternal;
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append(firstExternal);
				sb.append(" ");
				sb.append(CONTIGS[cont1].LASTEXTERNAL);
				sb.append(" ");
				sb.append(CONTIGS[cont1].AFTERINSERT.INSERTIDENTIFIER);
				sb.append("\n");
				for (int i=cont1+1; i<cont2; i++) {
					sb.append(CONTIGS[i].FIRSTEXTERNAL);
					sb.append(" ");
					sb.append(CONTIGS[i].LASTEXTERNAL);
					sb.append(" ");
					sb.append(CONTIGS[i].AFTERINSERT.INSERTIDENTIFIER);
					sb.append("\n");
				}
				sb.append(CONTIGS[cont2].FIRSTEXTERNAL);
				sb.append(" ");
				sb.append(lastExternal);
				return sb.toString();
			}
		} // end of Translator.toString(int, int)

	} // end of Translator



/**
* Key for parameter descriping DNA contents = "Sequence".
*/
	public static final String SEQUENCEKEY = "Sequence";
	
/**
* Key for parameter which is Yes if ALUs and/or LINES have been removed = "Processed".
*/
	public static final String PROCESSEDKEY = "Processed";

/**
* Key for number of useful nonambiguous bases = "ValidBases".
*/
	public static final String VALIDBASESKEY = "ValidBases";
		
/**
* Key for the internal position of the first base not created through padding by SweepDNA = "FirstUnPadded".
*/
	public static final String FIRSTUNPADDEDKEY = "FirstUnPadded";
		
/**
* Key for the internal position of the last base not created through padding by SweepDNA = "LastUnPadded".
*/
	public static final String LASTUNPADDEDKEY = "LastUnPadded";
		
/**
* Character marking start of insert in DNA file = '['.
*/
	public static final char INSERTLEADCHAR = '[';

/**
* Character marking end of insert in DNA file = ']'.
*/
	public static final char INSERTTRAILCHAR = ']';
	
/**
* Character marking end or start of insert identifier in DNA file = ':'.
*/
	public static final char IDENTDELIMITER = ':';
	
/**
* @param	pos	An (internal) base position
* @return Reading frame of pos.
*/
	public static final int frameOf(int pos) {
		return (pos % 3) + 1;
	} // end of frameOf


/**
* Number of useful bases in this = strand.length.
*/
  public final int LENGTH;

/**
* Nuber of unknown or ambiguous bases among useful ones.
*/
	public final int NCOUNT;

/**
* Internal position of first non-n base.
*/
	public final int FIRSTVALID;
	
/**
* Internal position of last non-n base.
*/
	public final int LASTVALID;
	
/**
* Identifying name, normally file name.
*/
  public final String NAME;

/**
* The external position of the first base. Read from beginning of file, or 1.
*/
	public final int ORIGIN;
	
/**
* The internal position of the first base not created through padding by SweepDNA, or -1.
*/
	public final int FIRSTUNPADDED;
	
/**
* The internal position of the last base not created through padding by SweepDNA, or -1.
*/
	public final int LASTUNPADDED;
	
/**
* True if primary strand of DNA.
*/
  public final boolean PRIMARYSTRAND;

/**
* The Translator between internal and external positions.
*/
	public final Translator TRANSLATOR;

/**
* Path of file from which this was taken.
*/
	public String filePath = null;

/**
* The Contigs describing the contents of this.
*/
  private final Contig[ ] DNACONTIGS;
	
	private Contig[ ] newcontigs;

  private final Compactor ACIDCOMPACTOR = Compactor.ACIDCOMPACTOR;
  private final Compactor BASECOMPACTOR = Compactor.BASECOMPACTOR;
	
  private byte[ ] strand; // the actual storage place

// internal pos of current base.
	private int mark = 0;

  private int[ ] startcodons = null; // internal positions of start codons
  private int[ ] stopcodons = null; // internal positions of stop codons
  private int[ ] glycosyls = null; // internal positions of glycosylation sites

	private SplitOctamerModifier splitOctamerModifier = null;
	private StopCodonModifier stopCodonModifier = null;
	private GlycSiteModifier glycSiteModifier = null;
	private TransSiteModifier transSiteModifier = null;
	private ORFHexamerModifier oRFHexamerModifier = null;
	private NonORFHexamerModifier nonORFHexamerModifier = null;
	private CpGModifier cpGModifier = null;
	private GTModifier gTModifier = null;
	private PolyASiteModifier polyAModifier = null;
	private REndModifier rEndModifier = null;
	
	private int[ ] basecodes = null;

	private char codonchar = '-';
	
	private int firstvalid = -1;
	private int lastvalid = Integer.MAX_VALUE;
	
/**
* Creates DNA object as specified by a String.
* If the string begins with a number, that is set as the address
* origin (which is otherwise 1).
* After that, only characters in Compactor.BASECODES are used.
* Account is taken of Insert markers.
*
* @param	in			DNA in String form.
* @param	name		Name, normally of file from which it was got.
* @param	primary If false, convert to complementary strand.
*/
  public DNA( String in, String name, boolean primary) throws RetroTectorException {
		this(in, name, primary, -1, -1);
  } // end of constructor(String, String, boolean) {
	
/**
* Creates DNA object as specified by a String.
* If the string begins with a number, that is set as the address
* origin (which is otherwise 1).
* After that, only characters in Compactor.BASECODES are used.
* Account is taken of Insert markers.
*
* @param	in				DNA in String form.
* @param	name			Name, normally of file from which it was got.
* @param	primary		If false, convert to complementary strand.
* @param	firstunp	>FIRSTUNPADDED.
* @param	lastunp		>LASTUNPADDED.
*/
  public DNA( String in, String name, boolean primary, int firstunp, int lastunp) throws RetroTectorException {
  	in = in.trim();
  	int ind = in.indexOf(' ');
  	int o = 1;
  	if (ind > 0) {
	  	try { // to read starting position
	  		o = Integer.parseInt(in.substring(0, ind));
	  	} catch (NumberFormatException nfe) {
	  	}
	  }
		mark = 0;
		
  	NAME = name;
  	PRIMARYSTRAND = primary;
  	LENGTH = getData(in, o);
    
    if (PRIMARYSTRAND) {
			ORIGIN = o;
			FIRSTUNPADDED = firstunp;
			LASTUNPADDED = lastunp;
		} else {
    // ---------- Building second, complementary strand ----------
    	complement();
    	ORIGIN = o + LENGTH - 1;
			if (lastunp < 0) {
				FIRSTUNPADDED = -1;
			} else {
				FIRSTUNPADDED = LENGTH - lastunp - 1;
			}
			if (firstunp < 0) {
				LASTUNPADDED = -1;
			} else {
				LASTUNPADDED = LENGTH - firstunp - 1;
			}
    }
		DNACONTIGS = newcontigs;
		newcontigs = null;
    codonify();
    NCOUNT = collectCodons();
		FIRSTVALID = firstvalid;
		LASTVALID = lastvalid;
		TRANSLATOR = new Translator(DNACONTIGS, this);
  } // end of constructor(String, String, boolean, int, int) {

/**
* Creates	a DNA from a parameter file.
* The file should contain a multiparameter with key 'Sequence',
* formatted like in String in the above constructor.
* @param	is			The file in question.
* @param primary	If false, convert to complementary strand.
*/
	public DNA(File is, boolean primary) throws RetroTectorException {
		NAME = is.getName();
		filePath = is.getPath();
		RetroTectorEngine.setInfoField("Building DNA " + NAME);
  	PRIMARYSTRAND = primary;
  	parameters = new Hashtable();
  	ParameterFileReader pfr = new ParameterFileReader(is, parameters);
  	pfr.readParameters();
  	pfr.close();
  	String[ ] ss = getStringArray(SEQUENCEKEY);
  	parameters.remove(SEQUENCEKEY); // to save space
		
// assemble into String
		StringBuffer sb = new StringBuffer(10000);
		for (int i=0; i<ss.length; i++) {
			sb.append(ss[i]);
			sb.append(" ");
			ss[i] = null; // garbage collect
		}
		String in = sb.toString().trim();
		sb = null;
  	int ind = in.indexOf(' ');
  	int o = 1;
  	if (ind > 0) {
  		try { // to read starting position
  			o = Integer.parseInt(in.substring(0, ind));
  		} catch (NumberFormatException nfe) {
  		}
  	}
		mark = 0;
		
  	LENGTH = getData(in, o);
		in = null;
    
    if (PRIMARYSTRAND) {
			ORIGIN = o;
		} else {
    // ---------- Building second, complementary strand ----------
    	complement();
    	ORIGIN = o + LENGTH - 1;
    }
		DNACONTIGS = newcontigs;
		newcontigs = null;
    codonify();
    NCOUNT = collectCodons();
		FIRSTVALID = firstvalid;
		LASTVALID = lastvalid;
		RetroTectorEngine.setInfoField("DNA " + NAME + " built: Number of n bases was " + NCOUNT);
		TRANSLATOR = new Translator(DNACONTIGS, this);
		int p = getInt(FIRSTUNPADDEDKEY, -1);
		if (p >= 0) {
			FIRSTUNPADDED = TRANSLATOR.internalize(p);
		} else {
			FIRSTUNPADDED = -1;
		}
		p = getInt(LASTUNPADDEDKEY, -1);
		if (p >= 0) {
			LASTUNPADDED = TRANSLATOR.internalize(p);
		} else {
			LASTUNPADDED = -1;
		}
	} // end of constructor (File, boolean)
  
/**
* Constructor for DNA with a new insert.
*	@param	oldDNA			The DNA to construct the insert in.
*	@param	firstposint	The first (internal) position in oldDNA included in the insert.
*	@param	lastposint	The last (internal) position in oldDNA included in the insert.
*	@param	ident				The name of the insert.
*/
	public DNA(DNA oldDNA, int firstposint, int lastposint, String ident) throws RetroTectorException {
	
		if (firstposint < oldDNA.firstUnPadded()) {
			throw new RetroTectorException("DNA", "Insert into padded area");
		}
		if ((oldDNA.LASTUNPADDED >= 0) && (lastposint > oldDNA.LASTUNPADDED)) {
			throw new RetroTectorException("DNA", "Insert into padded area");
		}
	
		NAME = oldDNA.NAME;
		PRIMARYSTRAND = oldDNA.PRIMARYSTRAND;
		int insertlength = lastposint - firstposint + 1;
		LENGTH = oldDNA.LENGTH - insertlength;
		ORIGIN = oldDNA.ORIGIN;
		FIRSTUNPADDED = oldDNA.FIRSTUNPADDED;
		LASTUNPADDED = oldDNA.LASTUNPADDED - insertlength;

		Insert insert;
		
		int ext1 = -1; // external position of last base before insert
		int ext2 = -1; // external position of first base after insert
		Stack costack = new Stack(); // for new Contigs
		String internalString = null; // for INSERTCONTENTS of Insert
		int fi = -1; // for first internal position of new Contig
		int fe = -1; // for first external position of new Contig
		strand = new byte[LENGTH];
// copy strand up to insert point
		for (int p=0; p<firstposint; p++) {
			strand[p] = (byte) oldDNA.get2bit(p);
		}

		int iff = 0; // index of Contig containing firstposint
// push preceding Contigs
		while (oldDNA.DNACONTIGS[iff].LASTINTERNAL < firstposint) {
			costack.push(oldDNA.DNACONTIGS[iff++]);
		}
		int il = iff; // index of Contig containing lastposint
		while (oldDNA.DNACONTIGS[il].LASTINTERNAL < lastposint) {
			il++;
		}
		if (il > iff) { // Old Insert was included
			ident = "N" + ident;
		}

// make Insert as specified
		insert = new Insert(oldDNA.DNACONTIGS[iff].contigExternalize(firstposint), oldDNA.DNACONTIGS[il].contigExternalize(lastposint), ident, oldDNA.subStringWithInserts(firstposint, lastposint));
// make new Contig before and including Insert
		costack.push(new Contig(this, oldDNA.DNACONTIGS[iff].FIRSTINTERNAL, firstposint - 1, oldDNA.DNACONTIGS[iff].FIRSTEXTERNAL, oldDNA.DNACONTIGS[iff].contigExternalize(firstposint) - 1, insert));
		
// make new Contig after damage
		costack.push(new Contig(this, firstposint, oldDNA.DNACONTIGS[il].LASTINTERNAL - insertlength, oldDNA.DNACONTIGS[il].contigExternalize(lastposint) + 1, oldDNA.DNACONTIGS[il].LASTEXTERNAL, oldDNA.DNACONTIGS[il].AFTERINSERT));
		il++;
		
// push revised succeeding Contigs
		while (il < oldDNA.DNACONTIGS.length) {
			costack.push(new Contig(this, oldDNA.DNACONTIGS[il].FIRSTINTERNAL - insertlength, oldDNA.DNACONTIGS[il].LASTINTERNAL - insertlength, oldDNA.DNACONTIGS[il].FIRSTEXTERNAL, oldDNA.DNACONTIGS[il].LASTEXTERNAL, oldDNA.DNACONTIGS[il].AFTERINSERT));
			il++;
		}
		for (int p=firstposint; p<LENGTH; p++) {
			strand[p] = (byte) oldDNA.get2bit(p + insertlength);
		}
		
		DNACONTIGS = new Contig[costack.size()];
		costack.copyInto(DNACONTIGS);
		
    codonify();
    NCOUNT = collectCodons();
		FIRSTVALID = firstvalid;
		LASTVALID = lastvalid;
		TRANSLATOR = new Translator(DNACONTIGS, this);
		
	} // end of constructor(DNA, int, int, String)

/**
* Not in use at present.
* @param start	First position (internal).
* @param stop		Last position (internal).
* @return	A DNA as specified.
*/
	public DNA partialDNA(int start, int stop, String name) throws RetroTectorException {
		String[ ] ss = formSubString(start, stop);
		StringBuffer sb = new StringBuffer(10000);
		for (int i=0; i<ss.length; i++) {
			sb.append(ss[i]);
			sb.append(" ");
			ss[i] = null; // garbage collect
		}
		String in = sb.toString().trim();
		return new DNA(in, name, true);
	} // end of partialDNA
	
/**
*	@param	posit	An integer.
*	@return	True if posit is a valid internal position in this.
*/
	public final boolean insideDNA(int posit) {
    if ((posit < 0) | (posit >= LENGTH)) {
      return false;
    }
    return true;
	} // end of insideDNA(int)
  
/**
*	@param	posit	An internal base position.
*	@return	An internal base position, inside this sequence, as close as possible to posit.
*/
  public final int forceInside(int posit) {
    if (posit < 0) {
      return 0;
    } else if (posit >= LENGTH) {
      return LENGTH - 1;
    } else {
			return posit;
		}
  } // end of forceInside(int)
  
/**
*	@param	pos	An external base position.
*	@return	The corresponding internal position, or Integer.MIN_VALUE.
*/
	public final int internalize(int pos) {
    return TRANSLATOR.internalize(pos);
	} // end of internalize(pos)
	
/**
*	@param	pos	An internal base position.
*	@return	The corresponding external position, or Integer.MIN_VALUE.
*/
	public final int externalize(int pos) {
    return TRANSLATOR.externalize(pos);
	} // end of externalize(pos)
	
/**
* @return	The internal position of the first base not created through padding by SweepDNA.
*/
	public final int firstUnPadded() {
		if (FIRSTUNPADDED < 0) {
			return 0;
		} else {
			return FIRSTUNPADDED;
		}
	} // end of firstUnPadded()
	
/**
* @return	The internal position of the last base not created through padding by SweepDNA.
*/
	public final int lastUnPadded() {
		if (LASTUNPADDED < 0) {
			return LENGTH - 1;
		} else {
			return LASTUNPADDED;
		}
	} // end of lastUnPadded()
	
/**
* @return Position (external) of base pointed at.
*/
	public final int getMarkExt() {
		return externalize(mark);
	} // end of getMarkExt()
	
/**
* @return Position (internal) of base pointed at.
*/
	public final int getMark() {
		return mark;
	} // end of getMark()
	
/**
* @param New position (internal) of base pointed at. Forced inside if outside.
*/
	public final void setMark(int m) {
		mark = forceInside(m);
	} // end of setMark(int)
	
/**
* @return Array of internal positions of all start (methionine) codons.
*/
	public final int[ ] getStartCodons() {
		return startcodons;
	} // end of getStartCodons()

/**
* @return Array of internal positions of all stop codons.
*/
	public final int[ ] getStopCodons() {
		return stopcodons;
	} // end of getStopCodons()

// last position in ORF including startAt
	private final int orfLast(int startAt) {
		for (int i=0; i<stopcodons.length; i++) {
			if ((stopcodons[i] >= startAt) && ((stopcodons[i] - startAt) % 3 == 0)) {
				return stopcodons[i] - 3;
			}
		}
		int j;
		for (j=startAt; j<LENGTH; j+=3) {
		}
		return j - 3;
	} // end of orfLast(int)
	
	private boolean[ ] inorf = null;
	
/**
* @param	minORFlength	Length (in acids) of shortest ORF to consider.
* @return Positions in ORFs at least minORFlength acids long.
*/
		public final boolean[ ] getInORF(int minORFlength) {
			if (inorf != null) {
				return inorf;
			}
			inorf = new boolean[LENGTH];
			int o;
			for (int i=0; i<=2; i++) {
				o = orfLast(i);
				if (o - i >= minORFlength * 3) {
					for (int j=i; j<=o; j+= 3) {
						inorf[j] = true;
					}
				}
			}
			for (int i=0; i<stopcodons.length; i++) {
				o = orfLast(stopcodons[i] + 3);
				if (o - stopcodons[i] >= minORFlength * 3) {
					for (int j=stopcodons[i]; j<=o; j+= 3) {
						inorf[j] = true;
					}
				}
			}
			return inorf;
		} // end of getInORF(int)
		
/**
* @return Array of internal positions of all glycosylation sites.
*/
	public final int[ ] getGlycosyls() {
		return glycosyls;
	} // end of getGlycosyls()

/**
* @param	first	Internal first position to count.
* @param	last	Internal last position to count.
* @return	Array[4] of number of stop codons in each reading frame within above limits. [0] is dummy.
*/
	public final int[ ] stopcodoncounts(int first, int last) {
		int[ ] result = new int[4];
		for (int i=0; i<stopcodons.length; i++) {
			if ((stopcodons[i] >= first) & (stopcodons[i] <= last)) {
				result[frameOf(stopcodons[i])]++;
			}
		}
		return result;
	} // end of stopcodoncounts(int, int)
	
/**
* @return	P if primary strand, otherwise S.
*/
  public final String strandString() {
  	if (PRIMARYSTRAND) {
  		return "P";
  	} else {
  		return "S";
  	}
  } // end of strandString()
  
/*
* @param pos An internal base position. Note: not the amino acid position.
* @return	The amino acid character code at pos.
*/
  public final char getAcid(int pos) {
  	if (strand[pos] < 0) {
  		return 'x';
  	} else {
    	return ACIDCOMPACTOR.intToCharId(strand[pos]);
    }
  } // end of getAcid(int)

/*
* @param pos An internal position.
* @return	The two-bit representation of the base at pos, or 4.
*/
  public final byte get2bit(int pos) {
		return (byte) ((strand[pos] >> 4) & 7);
  } // end of get2bit(int)
  
/**
* @param 	pos An internal position.
* @return	The six-bit representation of an amino acid at pos, or 64. 
*/
  public final int get6bit(int pos) {
  	if (strand[pos] >= 0) {
    	return strand[pos];
  	} else {
  		return 64;
    }
  } // end of get6bit(int)
  
/**
* @param 	pos An internal position.
* @return	An int with the codes of 15 bases starting at pos, or -1 if there is an ambiguous code.
*/
  public final int get30bit(int pos) {
    if ((pos < 0) | ((pos + 14) >= LENGTH)) {
      return -1;
    }
    int r = 0;
    int c;
    for (int i=pos; i<=(pos + 14); i++) {
      if ((c = get2bit(i)) > 3) {
        return -1;
      }
      r = (r | c) << 2;
    }
    return r >> 2;
  } // end of get30bit(int)
  
/**
* Scoring an acid Motif at a specified position. The Motif is specified as
* a byte array with values 0-63 for nonconserved positions, 64-127 for
* conserved positions and -1 for undefined codons.
* @param	motifstrand			Motif to match.
* @param	position				Position (internal) to match at.
* @param	cfact						Bonus for conserved position.
* @param	stopscoreInDNA	Score when encountering stop codon in DNA. At present always zero.
* @return			Score
*/
  public final float acidRawScoreAt(byte[ ] motifstrand, int position, float cfact, float stopscoreInDNA ) {

    float sum = 0;
    int d;
		int c;
    
    AcidMatrix acidMatrix = AcidMatrix.refreshAcidMatrix(cfact);
    for (int i=0; i<motifstrand.length; i++) {
    	if ((d = motifstrand[i]) >= 0) {
				c = get6bit(3 * i + position );
    		if (ACIDCOMPACTOR.intToCharId(c) == ACIDCOMPACTOR.STOPCHAR) {
    			sum += stopscoreInDNA;
    		} else {
    			sum += acidMatrix.floatMatrix[d][c];
				}
			}
    }
    return sum;
  } // end of acidRawScoreAt(byte[ ], int, float, float)

/**
* Scoring an acid Motif at a specified position, returning NaN if an ambiguous
* acid is encountered.
* @param	motifstrand			Motif to match (see acidRawScoreAt).
* @param	position				Position (internal) to match at.
* @param	cfact						Bonus for conserved position.
* @param	stopscoreInDNA	Score when encountering stop codon in DNA. At present always zero.
* @return			Score, or NaN
*/
  public final float acidRefreshScoreAt(byte[ ] motifstrand, int position, float cfact, float stopscoreInDNA ) {

    float sum = 0;
    int d;
		int c;
    
    AcidMatrix acidMatrix = AcidMatrix.refreshAcidMatrix(cfact);
    for ( int i=0; i<motifstrand.length; i++) {
    	if ((d = motifstrand[i]) >= 0) {
				c = get6bit(3 * i + position );
				if (c == 64) {
					return Float.NaN;
				}
    		if (ACIDCOMPACTOR.intToCharId(c) == ACIDCOMPACTOR.STOPCHAR) {
    			sum += stopscoreInDNA;
    		} else {
    			sum += acidMatrix.floatMatrix[d][c];
				}
			}
    }
    return sum;
  } // end of acidRefreshScoreAt(byte[ ], int, float, float)

/**
* Scoring an acid at a specified position. Matrix.acidMATRIX must be refreshed.
* @param	acid						Integer code for acid to match.
* @param	position				Position (internal) to match at.
* @param	stopscoreInDNA	Score when encountering stop codon in DNA.
*	@return		Score, or stopscoreInDNA if codon is stopcodon, or 0 if acid is stopcodon, or codon is ambiguous.
*/
  public final float acidRawScoreOne(int acid, int position, int stopscoreInDNA ) {
  
  	if (acid == ACIDCOMPACTOR.STOPINT) {
  		return 0;
  	}
   	if (acid == ACIDCOMPACTOR.UNKNOWNINT) {
  		return 0;
  	}
		int c = get6bit(position);
    if (c > 63) {
    	return Matrix.acidMATRIX.INDEFINITESCORE;
    }
   	if (ACIDCOMPACTOR.intToCharId(c) == ACIDCOMPACTOR.STOPCHAR) {
    	return stopscoreInDNA;
    }
    return Matrix.acidMATRIX.floatMatrix[acid][c];
  } // end of acidRawScoreOne(int, int, int)
  
/**
* Scoring a base Motif at a specified position. The Motif is specified as
* a byte array with values 0-3 for nonconserved positions, 4-7 for
* conserved positions and -1 for undefined bases.
* @param	motifstrand	Motif to match.
* @param	position		Position (internal) to match at.
* @param	cfact				Bonus for conserved position.
*/
  public final float baseRawScoreAt(byte[ ] motifstrand, int position, float cfact) {

    float sum = 0;
    int d;
    
    BaseMatrix baseMatrix = BaseMatrix.refreshBaseMatrix(cfact);
    for (int i=0; i<motifstrand.length; i++) {
    	if ((d = motifstrand[i]) >= 0) {
    		sum += baseMatrix.floatMatrix[d][get2bit(i + position)];
    	}
    }
    return sum;
  } // end of baseRawScoreAt(byte[ ], int, float)

/**
* @param	pattern		An array of base codes.
* @param	position	An internal base position.
* @return	The number of nonidentities betwen pattern and DNA at position, or -1.
*/
  public final int discrepancies(byte[ ] pattern, int position) {
    if ((position < 0) | (position >= (LENGTH - pattern.length))) {
      return -1;
    }
    int disc = 0;
    for (int i=0; i<pattern.length; i++) {
      if (pattern[i] != get2bit(position + i)) {
        disc++;
      }
    }
    return disc;
  } // end of discrepancies(byte[ ], int)
  
/**
* Scoring a base Motif at a specified position, returning NaN if an ambiguous
* base is encountered.
* @param	motifstrand	Motif to match (see baseRawScoreAt).
* @param	position		Position (internal) to match at.
* @param	cfact				Bonus for conserved position.
*/
  public final float baseRefreshScoreAt(byte[ ] motifstrand, int position, float cfact ) {

    float sum = 0;
    int d;
    
    BaseMatrix baseMatrix = BaseMatrix.refreshBaseMatrix(cfact);
    for (int i=0; i<motifstrand.length; i++) {
    	if ((d = motifstrand[i]) >= 0) {
    		int b = get2bit(i + position);
    		if (b > 3) {
    			return Float.NaN;
    		}
    		sum += baseMatrix.floatMatrix[d][b];
    	}
    }
    return sum;
  } // end of baseRefreshScoreAt(byte[ ], int, float)

/**
* @param	theMatrix	Any base BaseWeightMatrix.
* @param	position	The internal position to score at.
* @return	The score of theMatrix at position, or Float.NaN.
*/
	public final float baseMatrixScoreAt(BaseWeightMatrix theMatrix, int position) {
		if ((position < 0) | ((position + theMatrix.LENGTH) >= LENGTH)) {
			return Float.NaN;
		}
		float score = 0.0f;
		int tempi;
		float[ ][ ] mat = theMatrix.WEIGHTS;
		for (int p=0; p<theMatrix.LENGTH; p++) {
			tempi = get2bit(position + p);
			if (tempi < 4) {
				score += mat[tempi][p];
			}
		}
		return score;
	} // end of baseMatrixScoreAt(BaseWeightMatrix, int)
	
/**
* Creates a <code>String</code>-object that displays a given subset of the bases
* or amino acids of the sequence. Amino acids are displayed in the frame defined
* by the <code>start</code> value.
*
* @param start 	First position (internal).
* @param stop 	Last position (internal).
* @param base 	Whether to display bases (<code>true</code>) or amino acids (<code>false</code>).
* @return	A String as specified.
*/
  public final String subString(int start, int stop, boolean base) {
		
    StringBuffer out = new StringBuffer(stop - start + 1);

    for (int i=start; i<=stop; i++ ) {
    	if (i < LENGTH) {
	      if (base) {
					out.append(getBase(i));
	      } else {
					out.append(getAcid(i));
					i += 2;
	      }
      }
    }
    return out.toString();
  } // end of subString(int, int, boolean)
	
/**
*	@param	firstpos	An internal base position.
*	@param	lastpos		An internal base position.
*	@return	A String of all bases between firstpos and lastpos, including Inserts.
*/
	public final String subStringWithInserts(int firstpos, int lastpos) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<DNACONTIGS.length; i++) {
			sb.append(DNACONTIGS[i].contentString(firstpos, lastpos));
		}
		return sb.toString();
	} // end of subStringWithInserts(int, int)

/**
* Similar to subString, but formats into 60-base lines with positions. Insertions are also shown.
* @param start	First position (internal).
* @param stop		Last position (internal).
* @return	A String array as secified.
*/
  public String[ ] formSubString(int start, int stop) {

		Stack lineS = new Stack();
		for (int co=0; co<DNACONTIGS.length; co++) {
			DNACONTIGS[co].toStrings(start, stop, lineS);
		}
		String[ ] result = new String[lineS.size()];
		lineS.copyInto(result);
		return result;
	} // end of formSubString(int, int)

/**
* @return	An int array of length LENGTH, with all the base codes.
*/
	public final int[ ] getBaseCodes() {
		if (basecodes == null) {
			basecodes = new int[LENGTH];
			for (int i=0; i<LENGTH; i++) {
				basecodes[i] = get2bit(i);
			}
		}
    return (int[ ]) basecodes.clone();
	} // end of getBaseCodes()
	
/**
* Writes this, or part of this, to a file.
*	@param	outfile		The File to write to.
*	@param	firstpos	The first internal position to output.
*	@param	lastpos		The last internal position to output.
*	@param	ptable		A Hashtable with parameters to output.
*	@param	processed	True if ALUs and/or LINEs have been removed.
*/
	public final void toFile(File outfile, int firstpos, int lastpos, Hashtable ptable, boolean processed) throws RetroTectorException {
		ParameterFileWriter ofile = new ParameterFileWriter(outfile);
		if (processed) {
			ofile.writeSingleParameter(PROCESSEDKEY, Executor.YES, false);
		}
		ofile.writeSingleParameter(VALIDBASESKEY, String.valueOf(LENGTH - NCOUNT), false);
		if (FIRSTUNPADDED >= 0) {
			ofile.writeSingleParameter(FIRSTUNPADDEDKEY, String.valueOf(externalize(FIRSTUNPADDED)), false);
		}
		if (LASTUNPADDED >= 0) {
			ofile.writeSingleParameter(LASTUNPADDEDKEY, String.valueOf(externalize(LASTUNPADDED)), false);
		}
		ofile.startMultiParameter(SEQUENCEKEY, false);
		int first = forceInside(firstpos);
		int last = forceInside(lastpos);
		String[ ] ss = formSubString(first, last);
		ofile.appendToMultiParameter(ss, false);
		ofile.finishMultiParameter(false);
		if (ptable != null) {
			ofile.listSingleParameters(ptable);
		}
		ofile.close();
	} // end of toFile(File, int, int, Hashtable, boolean)

/**
* @return Text describing base at current position, which is incremented.
*/
	public final String advanceBase() throws FinishedException {
		mark++;
		if (mark >= LENGTH) {
			throw new FinishedException();
		}
		return baseText(mark - 1);
	} // end of advanceBase()
	
/**
* @param	c Ideal acid (lowercase).
* @return Text describing codon at current position, which is incremented by 3.
*/
	public final String[ ] advanceCodon(char c) throws FinishedException {
		mark += 3;
		if (mark >= LENGTH) {
			throw new FinishedException();
		}
		return codonText(mark - 3, c);
	} // end of advanceCodon(char)

/**
* @param pos An internal position.
* @return	The char representing the base at pos.
*/
  public final char getBase(int pos) {
  	int x = get2bit(pos);
  	if (x > 3) {
  		return 'n';
  	} else {
    	return BASECOMPACTOR.intToCharId(x);
    }
  } // end of getBase(int)

/**
* Not in use at present.
* @param	first	An internal position.
* @param	last	A later internal position.
* @return	A String showing the external positions of the Inserts between first and last (inclusive):
*/
	public String insertsBetween(int first, int last) {
		if (last <= first) {
			return "";
		}
		int fc = -1;
		int lc = -1;
		for (int i=0; i<DNACONTIGS.length; i++) {
			if ((first <= DNACONTIGS[i].LASTINTERNAL) && (first >= DNACONTIGS[i].FIRSTINTERNAL)) {
				fc = i;
			}
			if ((last <= DNACONTIGS[i].LASTINTERNAL) & (last >= DNACONTIGS[i].FIRSTINTERNAL)) {
				lc = i;
			}
		}
		if ((lc == -1) | (fc == -1) | (lc <= fc)) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append(DNACONTIGS[fc].AFTERINSERT.INSERTFIRST);
		sb.append("-");
		sb.append(DNACONTIGS[fc].AFTERINSERT.INSERTLAST);
		for (int j=fc+1; j<lc; j++) {
			sb.append(",");
			sb.append(DNACONTIGS[j].AFTERINSERT.INSERTFIRST);
			sb.append("-");
			sb.append(DNACONTIGS[j].AFTERINSERT.INSERTLAST);
		}
		return sb.toString();
	} // end of insertsBetween(int, int)
	
/**
* @param	context	An AgrepContext defining search parameters.
* @return	The (internal) position of the next hit using agrep,
* or its negative if errors encountered, or Integer.MIN_VALUE.
*/
	public final int agrepFind(AgrepContext context) {
		if (context.beginAt >= LENGTH) {
			return Integer.MIN_VALUE;
		}
		int hita = Integer.MIN_VALUE;
		for (int pos=context.beginAt; pos<=Math.min(context.LAST, LENGTH - 1); pos++) {
			for (int rrr=(context.r.length - 1); rrr>=1; rrr--) {
				context.r[rrr] = (context.r[rrr] << 1) | 1;
				context.r[rrr] = context.r[rrr] & context.s[get2bit(pos)];
				context.r[rrr] = context.r[rrr] | ((context.r[rrr - 1] << 1) | 1);
				if ((context.r[rrr] & context.BITMASK) != 0) {
					hita = -(pos - context.PATTERN.length + 1);
				}
			}
			context.r[0] = (context.r[0] << 1) | 1;
			context.r[0] = context.r[0] & context.s[get2bit(pos)];
			if ((context.r[0] & context.BITMASK) != 0) {
				hita = pos - context.PATTERN.length  + 1;
			}
			if (hita != Integer.MIN_VALUE) {
				context.beginAt = pos + 1;
				return hita;
			}
		}
		return Integer.MIN_VALUE;
	} // end of agrepFind(AgrepContext)

/**
* @param	context	A LongAgrepContext defining search parameters.
* @return	The (internal) position of the next hit using agrep, or Integer.MIN_VALUE.
*/
	public final int longAgrepFind(LongAgrepContext context) {
		if (context.beginAt >= LENGTH) {
			return Integer.MIN_VALUE;
		}
		int hita = Integer.MIN_VALUE;
		for (int pos=context.beginAt; pos<=Math.min(context.LAST, LENGTH - 1); pos++) {
			for (int rrr=(context.r.length - 1); rrr>=1; rrr--) {
				context.r[rrr] = (context.r[rrr] << 1) | 1;
				context.r[rrr] = context.r[rrr] & context.s[get2bit(pos)];
				context.r[rrr] = context.r[rrr] | ((context.r[rrr - 1] << 1) | 1);
				if ((context.r[rrr] & context.bitmask) != 0) {
					hita = -(pos - context.PATTERN.length + 1);
				}
			}
			context.r[0] = (context.r[0] << 1) | 1;
			context.r[0] = context.r[0] & context.s[get2bit(pos)];
			if ((context.r[0] & context.bitmask) != 0) {
				hita = pos - context.PATTERN.length  + 1;
			}
			if (hita != Integer.MIN_VALUE) {
				context.beginAt = pos + 1;
				return hita;
			}
		}
		return Integer.MIN_VALUE;
	} // end of longAgrepFind(LongAgrepContext)

/**
* @param	position	An internal position.
* @param	pattern		An array of base codes.
* @return	True if the pattern occurs starting at position.
*/
  public final boolean patternAt(int position, byte[ ] pattern) {
    if ((position < 0) || ((position + pattern.length) >= LENGTH)) {
      return false;
    } else {
      for (int p=0; p<pattern.length; p++) {
        if (pattern[p] != get2bit(position + p)) {
          return false;
        }
      }
      return true;
    }
  } // end of patternAt(int, byte[ ])
    
  private final byte[ ] AATAAASTRAND = {3, 3, 0, 3, 3, 3};
  private final byte[ ] ATTAAASTRAND = {3, 0, 0, 3, 3, 3};
  private final byte[ ] AGTAAASTRAND = {3, 2, 0, 3, 3, 3};

/**
* @param	position	An internal position.
* @return	true if position is the hotspot of a polyadenylation signal.
*/
	public final boolean polyASignalAt(int position) {
		return patternAt(position - 2, AATAAASTRAND) | patternAt(position - 2, AGTAAASTRAND) | patternAt(position - 2, ATTAAASTRAND);
	} // end of polyASignalAt(int)
	
/**
* @return	A SplitOctamerModifier for this DNA.
*/
	public final SplitOctamerModifier getSplitOctamerModifier() throws RetroTectorException {
		if (splitOctamerModifier == null) {
			splitOctamerModifier = new SplitOctamerModifier(this, 20.0f);
		}
		return splitOctamerModifier;
	} // end of getSplitOctamerModifier()

/**
* @return	A StopCodonModifier for this DNA.
*/
	public final StopCodonModifier getStopCodonModifier() throws RetroTectorException {
		if (stopCodonModifier == null) {
			stopCodonModifier = new StopCodonModifier(this);
		}
		return stopCodonModifier;
	} // end of getStopCodonModifier()

/**
* @return	A GlycSiteModifier for this DNA.
*/
	public final GlycSiteModifier getGlycSiteModifier() throws RetroTectorException {
		if (glycSiteModifier == null) {
			glycSiteModifier = new GlycSiteModifier(this);
		}
		return glycSiteModifier;
	} // end of getGlycSiteModifier()

/**
* @return	A TransSiteModifier for this DNA.
*/
	public final TransSiteModifier getTransSiteModifier() throws RetroTectorException {
		if (transSiteModifier == null) {
			transSiteModifier = new TransSiteModifier(this);
		}
		return transSiteModifier;
	} // end of getTransSiteModifier()

/**
* @return	A ORFHexamerModifier for this DNA.
*/
	public final ORFHexamerModifier getORFHexamerModifier() throws RetroTectorException {
		if (oRFHexamerModifier == null) {
			oRFHexamerModifier = new ORFHexamerModifier(this);
		}
		return oRFHexamerModifier;
	} // end of getORFHexamerModifier()

/**
* @return	A NonORFHexamerModifier for this DNA.
*/
	public final NonORFHexamerModifier getNonORFHexamerModifier() throws RetroTectorException {
		if (nonORFHexamerModifier == null) {
			nonORFHexamerModifier = new NonORFHexamerModifier(this);
		}
		return nonORFHexamerModifier;
	} // end of getNonORFHexamerModifier()

/**
* @return	A CpGModifier for this DNA.
*/
	public final CpGModifier getCpGModifier() throws RetroTectorException {
		if (cpGModifier == null) {
			cpGModifier = new CpGModifier(this);
		}
		return cpGModifier;
	} // end of getCpGModifier()

/**
* @return	A GTModifier for this DNA.
*/
	public final GTModifier getGTModifier() throws RetroTectorException {
		if (gTModifier == null) {
			gTModifier = new GTModifier(this);
		}
		return gTModifier;
	} // end of getGTModifier
	
/**
* @return	A PolyASiteModifier for this DNA.
*/
	public final PolyASiteModifier getPolyASiteModifier() throws RetroTectorException {
		if (polyAModifier == null) {
			polyAModifier = new PolyASiteModifier(this);
		}
		return polyAModifier;
	} // end of getPolyASiteModifier()
	
/**
* @return	A REndModifier for this DNA.
*/
	public final REndModifier getREndModifier() throws RetroTectorException {
		if (rEndModifier == null) {
			rEndModifier = new REndModifier(this);
		}
		return rEndModifier;
	} // end of getREndModifier()
	

// read raw base codes from string, taking account of Inserts
	private final int getData(String in, int origin) throws RetroTectorException {
		int length = in.length();
    int extpos = origin; // current external position
    int ct = 0; // next position in strand
    int cifirst = 0; // to be FIRSTINTERNAL in current Contig
    int cefirst = origin; // to be FIRSTEXTERNAL in current Contig
    int temp;
		int ind;
		int epo;
    
    Stack contigStack = new Stack();
    
 
    // ---------- Building primary strand ----------
    strand = new byte[length];
		String ns;
    for (int inp=0; inp<length; inp++) {
			temp = in.charAt(inp);
      if (temp == INSERTLEADCHAR) { // insert
        ind = in.indexOf(INSERTTRAILCHAR, inp + 1);
        if (ind < 0) {
          RetroTectorException.sendError(this, "Unpaired [");
        }
				Insert insert = new Insert(in, inp, ind, extpos);
        contigStack.push(new Contig(this, cifirst, ct - 1, cefirst, extpos - 1, insert));
        cifirst = ct;
        extpos = 1 + insert.INSERTLAST;
        cefirst = extpos;
				inp = ind;
			} else if (temp == ' ') { // check position
				ind = in.indexOf(' ', inp + 1);
				if (ind >= 0) {
					ns = in.substring(inp + 1, ind);
					epo = extpos;
					try {
						epo = Utilities.decodeInt(ns);
					} catch (RetroTectorException e) {
					}
					if (epo != extpos) {
						RetroTectorException.sendError(this, "Base position mismatch: " + epo + " " + extpos, "in " + filePath);
					}
				}
      } else if ((temp = BASECOMPACTOR.charToIntId((char) temp)) >= 0 ) { // Valid character
				if (temp > 3) {
					strand[ct] = (byte) 4;
				} else {
					strand[ct] = (byte) (temp & 3);
				}
				ct++;
        extpos++;
			}
		}
		byte[ ] newstrand = new byte[ct];
		System.arraycopy(strand, 0, newstrand, 0, ct);
		strand = newstrand;
// make last Contig
    contigStack.push(new Contig(this, cifirst, ct - 1, cefirst, extpos - 1, null));
    newcontigs = new Contig[contigStack.size()];
    contigStack.copyInto(newcontigs);
    return ct;
  } // end of getData(String, int)
  
/*
* Turns result of getData into its complement, starting at other end.
*/
	private void complement() throws RetroTectorException {
  	byte[ ] complement = new byte[strand.length];
		for (int ct = 0; ct < LENGTH; ct++ ) {
			byte b = strand[(LENGTH-ct-1)];
  		if (b == 4) {
  			complement[ct] = (byte) 4;
  		} else {
  			complement[ct] = (byte) ((~b) & 3);
  		}
		}
		strand = complement;
    Contig[ ] oldcontigs = newcontigs;
    newcontigs = new Contig[oldcontigs.length];
    for (int i=0; i<(newcontigs.length - 1); i++) {
      newcontigs[i] = oldcontigs[oldcontigs.length - 1 - i].complementContig(oldcontigs[oldcontigs.length - 2 - i].AFTERINSERT);
    }
		newcontigs[newcontigs.length - 1] = oldcontigs[0].complementContig(null);
	} // end of complement()

// Finalizes strand.
  private final void codonify() {
		for (int ctt=0; ctt<(strand.length-2); ctt++) {
  		int x3 = (strand[ctt] | strand[ctt + 1] | strand[ctt + 2]) & 0xfc;
  		if (x3 != 0) { // is any base ambiguous?
  			strand[ctt] = (byte) (0x80 | (strand[ctt] << 4));
  		} else {
				strand[ctt] = (byte) ((strand[ctt] << 4) | (strand[ctt + 1] << 2) | strand[ctt + 2]);
			}
		}
		strand[strand.length - 2] = (byte) (0x80 | (strand[strand.length - 2] << 4));
		strand[strand.length - 1] = (byte) (0x80 | (strand[strand.length - 1] << 4));
	} // end of codonify()

// makes arrays containing internal positions of all start and stop codons and return number of 'n'.
	private final int collectCodons() {
		Vector startc = new Vector(strand.length / 20, strand.length / 20);
		Vector stopc = new Vector(strand.length / 20, strand.length / 20);
		Vector glycos = new Vector(1000, 1000);
		int ncount = 0;
		for (int pos=0; pos<LENGTH-3; pos++) {
			if (get2bit(pos) > 3) {
				ncount++;
			} else {
				lastvalid = pos;
				if (firstvalid < 0) {
					firstvalid = pos;
				}
			}
			if (getAcid(pos) == Compactor.STOPCHAR) {
				stopc.addElement(new Integer(pos));
			}
			if (getAcid(pos) == 'm') {
				startc.addElement(new Integer(pos));
			}
			if ((getAcid(pos) == 'n') & (pos < (LENGTH - 9))) {
				if ((getAcid(pos + 6) == 's') | (getAcid(pos + 6) == 't')) {
					glycos.addElement(new Integer(pos));
				}
			}
		}
		for (int pos=LENGTH-3; pos<LENGTH; pos++) {
			if (get2bit(pos) > 3) {
				ncount++;
			} else {
				lastvalid = pos;
				if (firstvalid < 0) {
					firstvalid = pos;
				}
			}
		}
		startcodons = new int[startc.size()];
		for (int p=startcodons.length - 1; p>=0; p--) {
			startcodons[p] = ((Integer) startc.elementAt(p)).intValue();
		}
		startc = null;
    	 	
		stopcodons = new int[stopc.size()];
		for (int p=stopcodons.length - 1; p>=0; p--) {
			stopcodons[p] = ((Integer) stopc.elementAt(p)).intValue();
		}
		stopc = null;

		glycosyls = new int[glycos.size()];
		for (int p=glycosyls.length - 1; p>=0; p--) {
			glycosyls[p] = ((Integer) glycos.elementAt(p)).intValue();
		}
		glycos = null;
		return ncount;
	} // end of collectCodons()

//Text describing base at position pos (internal).
	private final String baseText(int pos) {
		char theBase = getBase(pos);
		char codonchar = getAcid(pos);
		String temp;
		int f = frameOf(pos);
		if (f == 1) {
			temp = theBase + "       " + codonchar;
		} else if (f == 2) {
			temp = "  " + theBase + "     " + codonchar;
		} else {
			temp = "    " + theBase + "   " + codonchar;
		}
		return temp;
	} // end of baseText(int)

// Text describing codon at position pos (internal).
// Acid character uppercase if == c
	private final String[ ] codonText(int pos, char c) {
		String[ ] temp = new String[3];
		codonchar = getAcid(pos);
		if (codonchar == c) {
			codonchar = Character.toUpperCase(codonchar);
		}
		temp[0] = baseText(pos) + " " + codonchar;
		temp[1] = baseText(pos + 1);
		temp[2] = baseText(pos + 2);
		return temp;
	} // end of codonText(int, char)

} // end of DNA
