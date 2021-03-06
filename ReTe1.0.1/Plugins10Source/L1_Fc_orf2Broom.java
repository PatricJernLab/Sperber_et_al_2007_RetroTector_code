/*
* Copyright (©) 2000-2008, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0.1
*
* @author  Göran Sperber
* @version 13/11 -08
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Broom to detect L1_Fc_orf2 fragments.
*<PRE>
*     Parameters:
*
*   BroomPriority
* Tolerance relative to other Brooms
* Default: -1.
*
*   MerSize
* Length of sequence for initial search.
* Default: 17.
*
*   MerStep
* Step by which sequence for initial search is moved.
* Default: 43.
*
*   Tolerance
* Error tolerance in dynamic programming.
* Default: 10.
*
*   TailMinLength
* Shortest acceptable a-tail length.
* Default: -1.
*</PRE>
*/
public class L1_Fc_orf2Broom extends StandardBroom {

// prototype sequence
	private String l1_Fc_orf2 = "atgactctaaacccatatctttctataataacactgaacgtaaatggactaaatgcgccaaccaaaagacatagggtatcagaatggataaaaaaaacaagacccatctatttgctgtctacaagagactcattttagacctgaggacaccttcagattgaaagtgaggggatggagaactatctatcatgctactggaagtcaaaagaaagctggactagccatacttagatcagacaaactagactttaaattaaaggctgtaacaagagatgaagaagggcattatataacaattacagggtctatccatcaggaagagctaacaattataaatgtctatgcgccaaatacaggagcccccaaatacgtaaaactgttaatcacaaacataagcaaccttattgataagaatgtggtaattgcaggggactttaacactccacttacaacaatggatagatcatctagacacacggtcaataaagaaacaagggccctgaatgatacattggatcagatggacttgacagatatatttagaactctgcatcccaaagcaacagaatatactttcttctcgagtgcacatggaacattctccaagatagatcacatactgggtcacaaaacagcccttcataagtataaaagaattgagatcataccatgcatactttcagaccacaatgctatgaagcttgaaatcaaccacaggaaaaagtctggaaaacctccaaaagcatggaggttaaagaacaccctactaaagaatgagtgggtcaaccaggcaattagagaagaaattaaaaaatatatggaaacaaacgaaaatgaaaatacaacaatccaaacgctttgggatgcagcgaaggcagtcctgagaggaaaatacattgcaatccaggcctatctcaagaaacaagaaaaatccccaatacaaaatctaacagcacacctaaaggaaatagaagcagaacagcaaagacagcctaaacccagcagaagaagagaaataataaagatcagagcagaaataaacaatatagaatctaaaaaaactgtagagcagatcaatgaaaccaagagttggttttttgaaaaaataaacaaaattgataaacctctagccaggcttctcaaaaagaaaagggagatgacccaaatagataaaatcatgaatgaaaatggaattattacaaccaatccctcagagatacaagcaattatcagggaatactatgaaaaattatatgccaacaaactggacaacctggaagaaatggacaaattcctaagcacccacacacttccaaaactcaatcaggaggaaatagaaagcttgaacagacccataaccagcgaagaaattgaatcagttatcaaaaatctcccaacaaataagagtccaggaccagatggcttcccaggggaattctaccagacatttaaagcagagataatacctatccttctcaagctattccaaaaaatagaaagggaaggaaaacttccagactcattctatgaagccagtattactttgattcctaaaccagacagagacccagtaaaaaaagagaactacaggccaatatccctgatgaatatggatgcaaaaattctcaataagatactagcaaatcgaattcaacagcatataaaaagaattattcaccatgatcaagtgggattcattcctgggatgcagggctggttcaacattcgcaaatcaatcaacgtgatacatcacattaataaaagaaaagataagaaccatatgatcctgtcaatcgatgcagaaaaggcatttgacaaaattcagcatcctttcttaataaaaaccctcgagaaagtcgggatagaaggaacatacttaaacatcataaaagccatttatgaaaagcccacagctaacatcatcctcaatggggaaaaactgagagctttttccctgagatcaggaacacgacagggatgtccactctcaccgctgttgtttaacatagtgttggaagttctagcatcagcaatcagacaacaaaaggaaatcaaaggcatcaaaattggcaaagatgaagttaagctttcactttttgcagatgacatgatattatacatggaaaatccgatagactccaccaaaagtctgctagaactgatacatgaattcagcaaagttgcaggatacaaaatcaatgtacagaaatcagttgcattcttatacactaataangaagcaacagaaagacaaataaagaaactgatcccattcacgattgcaccaagaagcataaaatacctagggataaatctaaccaaagatgtaanagatctgtatgctgaaaactatagaaagctgatgaaggaaattgaagaagatataaagaaatggaaaaccattccgtgctcatggattggaagaataaatattgtcaaaatgtcaatactagccaaagctatctacacattcaatgcgatcccaatcaaaattgcaccagcattcttctcgaagctagaacaagcaatcctaaaattcatatggaaccacaaaaggccccgaatagccaaagtaattttgaagaagaagaccaaagcaggaggcatcacaatcccagactttagcctctactacaaagctgtaatcatcaagacagcatggtattggcacaaaaacagacacatagaccaatggaatagaatagaaaccccagaactagacccacaaacgtatggccaactaatctttgacaaagcaggaaagaatatccaatggaaaaaagacagtctctttaacaaatggtgctgggagaactggacagcaacatgcagaaggttgaaactagaccactttctcacaccattcacaaaaataaactcaaaatggataaaggacctgaatgtgagacaggaaaccatcaaaaccctagaggagaaagcaggaaaagacctctctgacctcagccgtagcaatttcttacttgacacatccccaaaggcaagggaattaaaagcaaaaatgaactactgggacctcatgaagataaaaagcttctgcacagcaaaggaaacaaccaacaaaactaaaaggcaaccaacggaatgggaaaagatatttgcaaatgacatatcggacaaagggctagtatccaaaatctataaagagctcaccaaactccacacccgaaaaacaaataacccagtgaagaaatgggcagaaaacatgaatagacacttctctaaagaagacatccggatggccaacaggcacatgaaaagatgctcaacgtcg";

/**
* Constructor.
*/
	public L1_Fc_orf2Broom() {
		parameters = new Hashtable();
		getDefaults();
		broomPriority = getFloat(BROOMPRIORITYKEY, -1.0f);
		merSize = getInt(MERSIZEKEY, 17);
		merStep = getInt(MERSTEPKEY, 23);
		tolerance = getFloat(TOLERANCEKEY, 7.0f);
		tailMin = getInt(TAILMINKEY, -1);
	} // end of constructor
	
/**
* @return	Name base for finds in primary strand.
*/
	public String getDirtName() {
		return "L1_Fc_orf2";
	} // end of getDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return l1_Fc_orf2;
	} // end of  dirtString() 
	
}
