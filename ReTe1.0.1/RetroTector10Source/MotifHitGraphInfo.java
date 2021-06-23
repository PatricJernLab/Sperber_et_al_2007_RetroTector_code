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
* Information about a MotifHit needed by Chainview
*/
public class MotifHitGraphInfo {

	private static final String PAD1 = ": Score=";
	private static final String PAD2 = " at ";
	private static final String PAD3 = " scored against";
	private static final String PAD4 = " (";
	private static final String PAD5 = " bases)";
	private static final String PADF = " frame ";
	private static final String PADL = " [";
	private static final String PAD_ = "-";
	private static final String PADR = "]";

/**
* Description of the Motif of the MotifHit.
*/
	public final String MOTIFDESCRIPTION;
	
/**
* Name of the MotifGroup of the Motif of the MotifHit.
*/
	public final String MOTIFGROUP;
	
/**
* Rounded score of the MotifHit.
*/
	public int SCORE;
	
/**
* Position (internal) of the MotifHit hotspot.
*/
	public int HOTSPOT;
	
/**
* Position (internal) of the MotifHit first base.
*/
	public int FIRSTPOS;
	
/**
* Position (internal) of the MotifHit last base.
*/
	public int LASTPOS;
	
/**
* Number of bases in the Motif.
*/
	public int BASES;
	
/**
* Contents of the Motif.
*/
	public String TEMPLATE;
	
/**
* Part of DNA fitted to template.
*/
	public String FIT;
	
	private DNA.Translator theTranslator;

/**
* Constructor.
* @param	theHit	MotifHit to describe.
*/
	public MotifHitGraphInfo(MotifHit theHit) throws RetroTectorException {
		Motif parent = theHit.PARENTMOTIF;
		MOTIFDESCRIPTION = parent.MOTIFGROUP.MOTIFGROUPNAME + ":" + parent.MOTIFRVGENUS + " (" +  parent.MOTIFORIGIN + ")";
		MOTIFGROUP = parent.MOTIFGROUP.MOTIFGROUPNAME;
		SCORE = Math.round(theHit.MOTIFHITSCORE);
		HOTSPOT = theHit.MOTIFHITHOTSPOT;
		FIRSTPOS = theHit.MOTIFHITFIRST;
		LASTPOS = theHit.MOTIFHITLAST;
		TEMPLATE = parent.MOTIFSTRING;
		BASES = LASTPOS - FIRSTPOS + 1;
		FIT = parent.correspondingDNA(theHit);
		theTranslator = theHit.SOURCEDNA.TRANSLATOR;
	} // end of constructor(MotifHit)
	
/**
* An object, reconstructed from the output of toStrings()
* @param	sourceStrings	Output from toStrings().
*	@param	trans					The DNA.Translator to use.
*/
	public MotifHitGraphInfo(String[ ] sourceStrings, DNA.Translator trans) throws RetroTectorException {
		theTranslator = trans;
		if (sourceStrings.length != 3) {
			RetroTectorException.sendError(this, "Incorrect source");
		}
		int index = sourceStrings[1].indexOf(PAD3);
		TEMPLATE = sourceStrings[1].substring(6, index);
		int index2 = sourceStrings[2].indexOf(PAD4);
		FIT = sourceStrings[2].substring(6, index2);
		index = sourceStrings[2].indexOf(PAD5);
		BASES = Utilities.decodeInt(sourceStrings[2].substring(index2 + PAD4.length(), index));
		index = sourceStrings[0].indexOf(PAD1);
		MOTIFDESCRIPTION = sourceStrings[0].substring(3, index);
		MOTIFGROUP = MOTIFDESCRIPTION.substring(0, MOTIFDESCRIPTION.indexOf(":"));
		index2 = sourceStrings[0].indexOf(PAD2);
		SCORE = Utilities.decodeInt(sourceStrings[0].substring(index + PAD1.length(), index2));
		index = sourceStrings[0].indexOf(PADF);
		int po = Utilities.decodeInt(sourceStrings[0].substring(index2 + PAD2.length(), index));
		HOTSPOT = theTranslator.internalize(po);
		index = sourceStrings[0].indexOf(PADL, index);
		index2 = sourceStrings[0].indexOf(PAD_, index + 3);
		po = Utilities.decodeInt(sourceStrings[0].substring(index + PAD4.length(), index2));
		FIRSTPOS = theTranslator.internalize(po);
		index = sourceStrings[0].indexOf(PADR, index);
		po = Utilities.decodeInt(sourceStrings[0].substring(index2 + PAD_.length(), index));
		LASTPOS = theTranslator.internalize(po);		
		
	} // end of constructor(String[ ], DNA)

/**
* Outputs contents in text form, for display or for file storage.
*/
	public String[ ] toStrings() {
		String[ ] theStrings = new String[3];
		theStrings[0] = "   " + MOTIFDESCRIPTION + PAD1 + SCORE + PAD2 + theTranslator.externalize(HOTSPOT) + PADF + DNA.frameOf(HOTSPOT)
			 + PADL + theTranslator.externalize(FIRSTPOS) + PAD_ + theTranslator.externalize(LASTPOS) + PADR;
		theStrings[1] = "      " + TEMPLATE + PAD3;
		theStrings[2] = "      " + FIT + PAD4 + BASES + PAD5;
		return theStrings;
	} // end of toStrings()
	
} // end of MotifHitGraphInfo
