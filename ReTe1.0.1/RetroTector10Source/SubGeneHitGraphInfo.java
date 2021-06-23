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
* Information about a SubGeneHit needed by Chainview.
*/
public class SubGeneHitGraphInfo {

	private static final String PAD1 = "SubGene ";
	private static final String PAD2 = ", type ";
	private static final String PAD3 = ", score=";
  private static final String PAD4 = " , hotspot ";

/**
* Name of the SubGene.
*/
	public final String SUBGENENAME;
	
/**
* Virus type(s) of the SubGene.
*/
	public final String SUBGENEGENUS;
	
/**
* Score of the SubGeneHit.
*/
	public final int HITSCORE;
  
/**
* Position (internal) of SubGeneHit hotspot.
*/
  public final int HITHOTSPOT;
	
/**
* Information about the MotifHits in the SubGeneHit.
*/
	public final MotifHitGraphInfo[ ] motifhitinfo;

	private final DNA.Translator THETRANSLATOR;

/**
* Creates a MotifHitGraphInfo describing a SubGeneHit.
* @param	theHit	The SubGeneHit to describe.
*/
	public SubGeneHitGraphInfo(SubGeneHit theHit) throws RetroTectorException {
		SUBGENENAME = theHit.PARENTSUBGENE.SUBGENENAME;
		SUBGENEGENUS = theHit.RVGENUS;
		HITSCORE = Math.round(theHit.SCORE);
    HITHOTSPOT = theHit.SGHITHOTSPOT;
		THETRANSLATOR = theHit.SOURCEDNA.TRANSLATOR;
		motifhitinfo = new MotifHitGraphInfo[theHit.MOTIFHITS.length];
		for (int m=0; m<motifhitinfo.length; m++) {
			motifhitinfo[m] = new MotifHitGraphInfo(theHit.getMotifHit(m));
		}
	} // end of SubGeneHitGraphInfo(SubGeneHit)
	
/**
* An object, reconstructed from the output of toStrings()
* @param	sourceStrings	Output from toStrings().
* @param	trans					The relevant DNA.Translator.
*/
	public SubGeneHitGraphInfo(String[ ] sourceStrings, DNA.Translator trans) throws RetroTectorException {

		THETRANSLATOR = trans;
		int index = sourceStrings[0].indexOf(PAD2);
		SUBGENENAME = sourceStrings[0].substring(PAD1.length(), index);
		int index2 = sourceStrings[0].indexOf(PAD3);
		SUBGENEGENUS = sourceStrings[0].substring(index + PAD2.length(), index2);
    index = sourceStrings[0].indexOf(PAD4);
		HITSCORE = Utilities.decodeInt(sourceStrings[0].substring(index2 + PAD3.length(), index));
		HITHOTSPOT = THETRANSLATOR.internalize(Utilities.decodeInt(sourceStrings[0].substring(index + PAD4.length())));
		motifhitinfo = new MotifHitGraphInfo[(sourceStrings.length - 1) / 3];
		String[ ] ss = new String[3];
		for (int m=0; m<motifhitinfo.length; m++) {
			ss[0] = sourceStrings[3 * m + 1];
			ss[1] = sourceStrings[3 * m + 2];
			ss[2] = sourceStrings[3 * m + 3];
			motifhitinfo[m] = new MotifHitGraphInfo(ss, THETRANSLATOR);
		}
	} // end of constructor (String[ ])
	
/**
* @return	Contents in text form, for display or for file storage.
*/
	public String[ ] toStrings() {
		String[ ] theStrings = new String[1 + 3 * motifhitinfo.length];
		String[ ] mStrings;
		theStrings[0] = PAD1 + SUBGENENAME + PAD2 + SUBGENEGENUS + PAD3 + HITSCORE + PAD4 + THETRANSLATOR.externalize(HITHOTSPOT);
		for (int m=0; m<motifhitinfo.length; m++) {
			mStrings = motifhitinfo[m].toStrings();
			theStrings[3 * m + 1] = mStrings[0];
			theStrings[3 * m + 2] = mStrings[1];
			theStrings[3 * m + 3] = mStrings[2];
		}
		return theStrings;
	} // end of toStrings()

} // end of SubGeneHitGraphInfo
