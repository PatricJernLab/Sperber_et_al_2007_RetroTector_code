/*
* Copyright (©) 2000-2008, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 27/4 -08
* Beautified 15/12 -06
*/
package builtins;

import retrotector.*;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.*;

/**
* Superclass of Executors which use an SQL database.
*<PRE>
*     Parameters:
*
*   User
* A user name in the database.
* Default: ""
*
*   Password
* The password for User.
* Default: ""
*
*   SubProtocol
* The SQL implementation to use.
* Default: mysql
*
*   Host
* The host to access.
* Default: localhost.
*
*   JDBCDriver
* The class of the Java driver for SQL.
* Default: com.mysql.jdbc.Driver
*
*   Debugging
* If Yes, most SQL commands are printed.
* Default: No
*
*</PRE>
*/
public class SQLUser extends Executor {

/*
*	Parameters used by various Columns, set by CollectGenome.
*/

/**
* Directory containing eg a chromosome.
*/
	protected File rootDirectory;

/**
* The name of the chromosome being processed.
*/
	protected String currentChromosome;

/**
* The highest position in any chromosome processed so far.
*/
	protected int currentChromosomeLength;

/**
* The comment associated with the chromosome being processed.
*/
	protected String currentLongComment;
	
/**
* The subdirectory of rootDirectory being searched now.
*/
	protected File currentSubdirectory;

/**
* The RetroVID script or Chains file being preocessed.
*/
	protected File currentLTRFile;
	
/**
* The name of the current RetroVID script.
*/
	protected String currentRetroVIDScriptName;

/**
* The DNA file in currentSubdirectory.
*/
	protected File currentDNAFile;

/**
* The sense strand of currentDNAFile.
*/
	protected DNA primaryDNA;

/**
* The antisense strand of currentDNAFile.
*/
	protected DNA secondaryDNA;

/**
* The DNA in use at this moment.
*/
	protected DNA currentDNA;

/**
* The LTRCandidate being processed.
*/
	protected LTRCandidate currentLTRCandidate;

/**
* Hotspot of companion if currentLTRCandidate is paired.
*/
	protected int ltrCompanionHotspot;

/**
* The Putein file being processed.
*/
	protected File currentPuteinFile;

/**
* Hashtable with contents of currentPuteinFile.
*/
 	protected Hashtable currentPuteinTable;

/**
* Putein from currentPuteinFile.
*/
	protected Putein currentPutein;

/**
* id of very similar Putein.
*/
	protected int currentPuteinDuplicator;

/**
* The SelectedChains file being processed.
*/
	protected File currentChainsFile;

/**
* Hashtable with contents currentChainsFile.
*/
	protected Hashtable currentChainTable;

/**
* Name of Chain being processed.
*/
	protected String currentChainName;

/**
* The ChainGraphInfo corresponding to currentChainName.
*/
	protected ChainGraphInfo currentChainGraphInfo;

/**
* The first (internal) position in DNA occurring in currentChainGraphInfo or any Putein.
*/
	protected int currentFirstUsedPos;

/**
* The last (internal) position in DNA occurring in currentChainGraphInfo or any Putein.
*/
	protected int currentLastUsedPos;

/**
* The 5'LTR hit in the Chain being processed.
*/
	protected MotifHitGraphInfo current5LTR;

/**
* The PBS hit in the Chain being processed.
*/
	protected MotifHitGraphInfo currentPBS;

/**
* The PPT hit in the Chain being processed.
*/
	protected MotifHitGraphInfo currentPPT;

/**
* The 3'LTR hit in the Chain being processed.
*/
	protected MotifHitGraphInfo current3LTR;

/**
* id of Chains overlapping the one being processed.
*/
	protected String currentChainOverlapper;
	
/**
* id of last Chain already present.
*/
	protected int latestChainID = 0;

/**
* False if only one set of Puteins is to be used.
*/
	protected boolean multiPuteins = true;
	
	private String subDir() {
		String s = currentSubdirectory.getPath().substring(rootDirectory.getPath().length());
		if (s == null) {
			return "NULL";
		}
// make blips double blips
		int ind = -2;
		while ((ind = s.indexOf(BLIP, ind + 2)) >= 0) {
			s = s.substring(0, ind) + BLIP + s.substring(ind);
		}
// make backslash double backslash
		ind = -2;
		while ((ind = s.indexOf(BACKSLASH, ind + 2)) >= 0) {
			s = s.substring(0, ind) + BACKSLASH + s.substring(ind);
		}
		return s;
	} // end of subDir()

/**
* Class to represent any column in a table.
*/
	abstract protected class Column {
	
/**
* The title of the column.
*/
		protected String columnName;

/**
* The SQL specification of the data type of the column.
*/
		protected String typeName;
		
/**
* +1 for ascending sort, -1 for descending.
*/
		protected int sortDirection = 1;
	
/**
* Suggested column width.
*/
		private int basicColumnWidth;
		
/**
* Constructor.
* @param	positions	Suggested column width in characters.
*/
		private Column(int positions) {
			basicColumnWidth = positions;
		} // end of Column.constructor(int)
		
/**
* @return	The definition of the column in an SQL 'create table' column.
*/
		protected String headerText() {
			return columnName + " " + typeName;
		} // end of Column.headerText()
		
/**
* @return	Column width in pixels.
*/
		protected int columnWidth() {
			return PIXELSPERCHAR * Math.max(basicColumnWidth, columnName.length());
		} // end of Column.columnWidth()
		
/**
* @return	The text for the column in an SQL 'insert' command.
*/
 		abstract protected String valueText() throws RetroTectorException, SQLException;
				
	} // end of Column
	

/**
* Class to represent an integer column in a table.
*/
	abstract protected class IntColumn extends Column {
	
/**
* Sets typeName typically  to 'INT'.
*/
		private IntColumn() throws SQLException {
			super(11);
			typeName = typeNameOf(Types.INTEGER);
			sortDirection = -1;
		} // end of IntColumn.constructor()

	} // end of IntColumn
	

/**
* Class to represent a float column in a table.
*/
	abstract protected class FloatColumn extends Column {
	
/**
* Sets typeName typically  to 'FLOAT'.
*/
		private FloatColumn() throws SQLException {
			super(12);
			if (getSubProtocol().equals(MYSQL)) {
				typeName = "FLOAT";
			} else {
				typeName = typeNameOf(Types.FLOAT);
			}
			sortDirection = -1;
		} // end of FloatColumn.constructor()
		
	} // end of FloatColumn
	

/**
* Class to represent a text column of unlimited width.
*/
	abstract protected class TextColumn extends Column {
	
/**
* Sets typeName typically  to 'TEXT'.
*/
		private TextColumn() throws SQLException {
			super(20);
			if (getSubProtocol().equals(MYSQL)) {
				typeName = "TEXT";
			} else {
				typeName = typeNameOf(Types.LONGVARCHAR);
			}
		} // end of TextColumn.constructor()

/**
* @return	The content of the field.
*/
		abstract protected String vText() throws SQLException, RetroTectorException;

/**
* @return	The blipped output of vText.
*/
		protected final String valueText() throws SQLException, RetroTectorException {
			String s = vText();
			if (s == null) {
				return "NULL";
			}
// make blips double blips
			int ind = -2;
			while ((ind = s.indexOf(BLIP, ind + 2)) >= 0) {
				s = s.substring(0, ind) + BLIP + s.substring(ind);
			}
// make backslash double backslash
			ind = -2;
			while ((ind = s.indexOf(BACKSLASH, ind + 2)) >= 0) {
				s = s.substring(0, ind) + BACKSLASH + s.substring(ind);
			}

			return BLIP + s + BLIP;
		} // end ofTextColumn.valueText()
		
	} // end of TextColumn
	

/**
* Class to represent a DNA or acid sequence.
*/
	abstract protected class SequenceColumn extends TextColumn {

		private SequenceColumn() throws SQLException {
			super();
		} // end of SequenceColumn.constructor()
		
	} // end of SequenceColumn
	
	
/**
* Class to represent a one-character column, normally T-F.
*/
	abstract protected class BooleanColumn extends Column {
	
/**
* Sets typeName typically  to 'CHAR(1)'.
*/
		private BooleanColumn() throws SQLException {
			super(1);
			typeName = typeNameOf(Types.BOOLEAN);
			if (typeName == null) {
				typeName = "CHAR(1)";
			}
		} // end of BooleanColumn.constructor()
		
	} // end of BooleanColumn


/**
* Class to represent a  fixed-width character column.
*/
	abstract protected class CharacterColumn extends Column {
	
/**
* Constructor.
* @param	length	The max number of characters. Column width will be max 20.
* Sets typeName typically  to 'CHAR(|length|)'.
*/
		private CharacterColumn(int length) throws SQLException {
			super(Math.min(20, length));
			typeName = typeNameOf(Types.CHAR);
			if (typeName == null) {
				typeName = "CHAR";
			}
			typeName = typeName + "(" + length + ")";
		} // end of CharacterColumn.constructor(int)
		
/**
* @return	The content of the field.
*/
		abstract protected String vText() throws RetroTectorException;

/**
* @return	The blipped output of vText.
*/
		protected final String valueText() throws RetroTectorException {
			String s = vText();
			if (s == null) {
				return "NULL";
			}
// make blips double blips
			int ind = -2;
			while ((ind = s.indexOf(BLIP, ind + 2)) >= 0) {
				s = s.substring(0, ind) + BLIP + s.substring(ind);
			}
// make backslash double backslash
			ind = -2;
			while ((ind = s.indexOf(BACKSLASH, ind + 2)) >= 0) {
				s = s.substring(0, ind) + BACKSLASH + s.substring(ind);
			}
			return BLIP + s + BLIP;
		} // end of CharacterColumn.valueText()
		
	} // end of CharacterColumn

	
/**
* Class to represent a character column of variable but limited width.
*/
	abstract protected class VarCharColumn extends Column {
	
/**
* @param	length	The max number of characters. Column width will be max 20.
* Sets typeName typically  to 'VARCHAR(|length|)'.
*/
		private VarCharColumn(int length) throws SQLException {
			super(Math.min(20, length));
			typeName = typeNameOf(Types.VARCHAR);
			if (typeName == null) {
				typeName = "VARCHAR";
			}
			typeName = typeName + "(" + length + ")";
		} // end of VarCharColumn.constructor(int)
		
/**
* @return	The content of the field.
*/
		abstract protected String vText() throws RetroTectorException;

/**
* @return	The blipped output of vText.
*/
		protected final String valueText() throws RetroTectorException {
			String s = vText();
			if (s == null) {
				return "NULL";
			}
// make blips double blips
			int ind = -2;
			while ((ind = s.indexOf(BLIP, ind + 2)) >= 0) {
				s = s.substring(0, ind) + BLIP + s.substring(ind);
			}
// make backslash double backslash
			ind = -2;
			while ((ind = s.indexOf(BACKSLASH, ind + 2)) >= 0) {
				s = s.substring(0, ind) + BACKSLASH + s.substring(ind);
			}
			return BLIP + s + BLIP;
		} // end of VarCharColumn.valueText()
		
	} // end of VarCharColumn

	
/**
* Class to represent a timestamp.
*/
	abstract protected class TimestampColumn extends Column {
	
/**
* Sets typeName typically  to 'TIMESTAMP'.
*/
		private TimestampColumn() throws SQLException {
			super(20);
			typeName = "TIMESTAMP";
		} // end of TimestampColumn.constructor()

/**
* @return	The content of the field.
*/
		abstract protected String vText() throws SQLException;

/**
* @return	The blipped output of vText.
*/
		protected final String valueText() throws SQLException {
			String s = vText();
			if (s == null) {
				return "NULL";
			}
// make blips double blips
			int ind = -2;
			while ((ind = s.indexOf(BLIP, ind + 2)) >= 0) {
				s = s.substring(0, ind) + BLIP + s.substring(ind);
			}
// make backslash double backslash
			ind = -2;
			while ((ind = s.indexOf(BACKSLASH, ind + 2)) >= 0) {
				s = s.substring(0, ind) + BACKSLASH + s.substring(ind);
			}
			return BLIP + s + BLIP;
		} // end of TimestampColumn.valueText()
		
	} // end of TimestampColumn
	


/**
* Column for 'ChromosomeLength' column in Chromosomes with length of chromosome.
*/
	protected class ChromosomeLengthColumn extends IntColumn {
	
		ChromosomeLengthColumn() throws SQLException {
			columnName = "ChromosomeLength";
		} // end of ChromosomeLengthColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentChromosomeLength);
		} // end of ChromosomeLengthColumn.valueText()
		
	} // end of ChromosomeLengthColumn


/**
* Column for 'Directory' column in Chromosomes with path of working directory.
*/
	protected class DirectoryColumn extends TextColumn {
	
/**
* Creates TEXT column.
*/
		DirectoryColumn() throws SQLException {
			super();
			columnName = "Directory";
		} // end of DirectoryColumn.constructor()
		
/**
* @return	Subdirectory name.
*/
		protected final String vText() {
			return rootDirectory.getPath();
		} // end of DirectoryColumn.vText()
		
	} // end of DirectoryColumn


/**
* Column for 'LongComment' column in Chromosomes with long comment.
*/
	protected class LongCommentColumn extends TextColumn {
	
/**
* Creates TEXT column.
*/
		LongCommentColumn() throws SQLException {
			super();
			columnName = "LongComment";
		} // end of LongCommentColumn.constructor()
		
/**
* @return	Subdirectory name.
*/
		protected final String vText() {
			return currentLongComment;
		} // end of LongCommentColumn.vText()
		
	} // end of LongCommentColumn


/**
* Column for 'FinishedAt' column in Chromosomes with time stamp.
*/
	protected class FinishedAtColumn extends TimestampColumn {
	
/**
* Creates TIMESTAMP column.
*/
		FinishedAtColumn() throws SQLException {
			super();
			columnName = "FinishedAt";
		} // end of FinishedAtColumn.constructor()
		
/**
* @return	Stamp for this time.
*/
		protected final String vText() {
			return (new Timestamp(System.currentTimeMillis())).toString();
		} // end of FinishedAtColumn.vText()
		
	} // end of FinishedAtColumn
	

/**
* Column for 'RTVersion' column in Chromosomes with name of RetroTector version.
*/
	protected class RTVersionColumn extends VarCharColumn {
	
/**
* Creates VARCHAR(150) column.
*/
		RTVersionColumn() throws SQLException {
			super(150);
			columnName = "RTVersion";
		} // end of RTVersionColumn.constructor()
		
/**
* @return	Version info.
*/
		protected final String vText() {
			return RetroTectorEngine.VERSIONSTRING;
		} // end of RTVersionColumn.vText()
		
	} // end of RTVersionColumn


/**
* Column for 'Chromosome' column with contents of ShortComment.
*/
	protected class ChromosomeColumn extends VarCharColumn {
	
		ChromosomeColumn() throws SQLException {
			super(MAXCHROMOSOMELENGTH);
			columnName = CHROMOSOME;
		} // end of ChromosomeColumn.constructor()
		
		protected final String vText() {
			return currentChromosome;
		} // end of ChromosomeColumn.vText()
		
	} // end of ChromosomeColumn


/**
* Column for 'Strand' column with 'P' or 'S'.
*/
	protected class StrandColumn extends CharacterColumn {
	
/**
* Creates CHAR(1) column.
*/
		StrandColumn() throws SQLException {
			super(1);
			columnName = "Strand";
		} // end of StrandColumn.constructor()
		
/**
* @return	'P' or 'S' as appropriate.
*/
		protected final String vText() {
			if (currentDNA == primaryDNA) {
				return "P";
			} else {
				return "S";
			}
		} // end of StrandColumn.vText()
		
	} // end of StrandColumn


/**
* Column for 'Subdirectory' column with name of subdirectory within working directory.
*/
	protected class SubDirectoryColumn extends VarCharColumn {
	
/**
* Creates VARCHAR(100) column.
*/
		SubDirectoryColumn() throws SQLException {
			super(100);
			columnName = "Subdirectory";
		} // end of SubDirectoryColumn.constructor()
		
/**
* @return	Subdirectory name.
*/
		protected final String vText() {
			return currentSubdirectory.getPath().substring(rootDirectory.getPath().length());
		} // end of SubDirectoryColumn.vText()
		
	} // end of SubDirectoryColumn


/**
* Column for 'DNAFile' column with name of DNA file used.
*/
	protected class DNAFileColumn extends VarCharColumn {
	
/**
* Creates VARCHAR(100) column.
*/
		DNAFileColumn() throws SQLException {
			super(100);
			columnName = "DNAFile";
		} // end of DNAFileColumn.constructor()
		
/**
* @return	DNA file name.
*/
		protected final String vText() {
			return currentDNAFile.getName();
		} // end of DNAFileColumn.vText()
		
	} // end of DNAFileColumn


/**
* Column for 'Comment' column with optional comment.
*/
	protected class CommentColumn extends TextColumn {
	
/**
* Creates TEXT column.
*/
		CommentColumn() throws SQLException {
			super();
			columnName = "Comment";
		} // end of CommentColumn.constructor()
		
/**
* @return	Empty field to fill in manually.
*/
		protected final String vText() {
			return "";
		} // end of CommentColumn.vText()
		
	} // end of CommentColumn


/**
* Column for 'ScoreFactor' column in LTRs with LTRCandidate.candidateFactor.
*/
	protected class LTRScoreFactorColumn extends FloatColumn {
	
		LTRScoreFactorColumn() throws SQLException {
			columnName = "ScoreFactor";
		} // end of LTRScoreFactorColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.candidateFactor);
		} // end of LTRScoreFactorColumn.valueText()
		
	} // end of LTRScoreFactorColumn


/**
* Column for 'LTRGenus' column in LTRs with LTRCandidate.candidateVirusGenus.
*/
	protected class LTRGenusColumn extends VarCharColumn {
	
		LTRGenusColumn() throws SQLException {
			super(10);
			columnName = "LTRGenus";
		} // end of LTRGenusColumn.constructor()
		
		protected final String vText() {
			return currentLTRCandidate.candidateVirusGenus;
		} // end of LTRGenusColumn.vTex()t
		
	} // end of LTRGenusColumn


/**
* Column for 'SimilarityStart' column in LTRs with externalized LTRCandidate.candidateSimilarityStart.
*/
	protected class LTRSimilarityStartPosColumn extends IntColumn {
	
		LTRSimilarityStartPosColumn() throws SQLException {
			columnName = "SimilarityStart";
		} // end of LTRSimilarityStartPosColumn.constructor()
		
		protected final String valueText() {
			if (currentLTRCandidate.candidateSimilarityStart >= 0) {
				return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.candidateSimilarityStart));
			} else {
				return "NULL";
			}
		} // end of LTRSimilarityStartPosColumn.valueText()
		
	} // end of LTRSimilarityStartPosColumn

/**
* Column for 'FirstPos' column in LTRs with externalized LTRCandidate.candidateFirst.
*/
	protected class LTRFirstPosColumn extends IntColumn {
	
		LTRFirstPosColumn() throws SQLException {
			columnName = "FirstPos";
		} // end of LTRFirstPosColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.candidateFirst));
		} // end of LTRFirstPosColumn.valueText()
		
	} // end of LTRFirstPosColumn


/**
* Column for 'LastPos' column in LTRs with externalized LTRCandidate.candidateLast.
*/
	protected class LTRLastPosColumn extends IntColumn {
	
		LTRLastPosColumn() throws SQLException {
			columnName = "LastPos";
		} // end of LTRLastPosColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.candidateLast));
		} // end of LTRLastPosColumn.valueText()
		
	} // end of LTRLastPosColumn

/**
* Column for 'SimilarityEnd' column in LTRs with externalized LTRCandidate.candidateSimilarityEnd.
*/
	protected class LTRSimilarityEndPosColumn extends IntColumn {
	
		LTRSimilarityEndPosColumn() throws SQLException {
			columnName = "SimilarityEnd";
		} // end of LTRSimilarityEndPosColumn.constructor()
		
		protected final String valueText() {
			if (currentLTRCandidate.candidateSimilarityEnd >= 0) {
				return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.candidateSimilarityEnd));
			} else {
				return "NULL";
			}
		} // end of LTRSimilarityEndPosColumn.valueText()
		
	} // end of LTRSimilarityEndPosColumn


/**
* Column for 'HotspotPos' column in LTRs with externalized LTRCandidate.hotSpotPosition.
*/
	protected class LTRHotspotPosColumn extends IntColumn {
	
		LTRHotspotPosColumn() throws SQLException {
			columnName = "HotspotPos";
		} // end of LTRHotspotPosColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.hotSpotPosition));
		} // end of LTRHotspotPosColumn.valueText()
		
	} // end of LTRHotspotPosColumn


/**
* Column for 'Hotspot' column in LTRs with LTRCandidate.hotSpotType.
*/
	protected class LTRHotspotColumn extends CharacterColumn {
	
		LTRHotspotColumn() throws SQLException {
			super(6);
			columnName = "Hotspot";
		} // end of LTRHotspotColumn.constructor()
		
		protected final String vText() {
			return currentLTRCandidate.hotSpotType;
		} // end of LTRHotspotColumn.vText()
		
	} // end of LTRHotspotColumn


/**
* Column for 'InChain' column in LTRs with id of containing Chain, if any.
*/
	protected class LTRInChainColumn extends IntColumn {
	
		LTRInChainColumn() throws SQLException {
			columnName = "InChain";
		} // end of LTRInChainColumn.constructor()
		
/**
* Will be set later.
*/
		protected final String valueText() {
			return "NULL";
		} // end of LTRInChainColumn.vText()
		
	} // end of LTRInChainColumn


/**
* Column for 'PairedHotspot' column in LTRs which contains hotspot position of pair companion
* if this LTR is part of an LTR pair, NULL if it is a single LTR.
*/
	protected class LTRPairedHotspotColumn extends IntColumn {
	
		LTRPairedHotspotColumn() throws SQLException {
			columnName = "PairedHotspot";
		} // end of LTRPairedHotspotColumn.constructor()
		
		protected final String valueText() {
			if (ltrCompanionHotspot > Integer.MIN_VALUE) {
				return String.valueOf(ltrCompanionHotspot);
			} else {
				return "NULL";
			}
		} // end of LTRPairedHotspotColumn.valueText()
		
	} // end of LTRPairedHotspotColumn

/**
* Column for 'LTRSequence' column in LTRs with base sequence.
*/
	protected class LTRSequenceColumn extends SequenceColumn {
	
		LTRSequenceColumn() throws SQLException {
			columnName = "LTRSequence";
		} // end of LTRSequenceColumn.constructor()
		
		protected final String vText() {
			return currentLTRCandidate.LTRCANDIDATEDNA.subString(currentLTRCandidate.candidateFirst, currentLTRCandidate.candidateLast, true);
		} // end of LTRSequenceColumn.vText()
		
	} // end of LTRSequenceColumn


/**
* Column for 'RepLTRFinds' column in LTRs, with similarities to sequences in repbasetemplates.
*/
	protected class LTRRepBaseFindsColumn extends TextColumn {
	
		LTRRepBaseFindsColumn() throws SQLException {
			columnName = "RepLTRFinds";
		} // end of LTRRepBaseFindsColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			if (currentLTRCandidate.repBaseFinds != null) {
				return currentLTRCandidate.repBaseFinds.replace('\t', '\n');
			}
			return Utilities.repBaseFind(currentDNA, currentLTRCandidate.getVeryFirst(), currentLTRCandidate.getVeryLast(), repBaseTemplates, true);
		} // end of LTRRepBaseFindsColumn.vText()
		
	} // end of LTRRepBaseFindsColumn


/**
* Column for 'LTRInserts' column in LTRs.
*/
	protected class LTRInsertsColumn extends TextColumn {
	
		LTRInsertsColumn() throws SQLException {
			columnName = "LTRInserts";
		} // end of LTRInsertsColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			return currentDNA.TRANSLATOR.toString(currentDNA.externalize(currentLTRCandidate.getVeryFirst()), currentDNA.externalize(currentLTRCandidate.getVeryLast()));
		} // end of LTRInsertsColumn.vText()
		
	} // end of LTRInsertsColumn


/**
* Column for 'LTRTSD' column in LTRs.
*/
	protected class LTRTSDColumn extends VarCharColumn {
	
		LTRTSDColumn() throws SQLException {
			super(40);
			columnName = "LTRTSD";
		} // end of LTRTSDColumn.constructor()
		
		protected final String vText() {
			String s = currentLTRCandidate.candidateComment.trim();
			int ind1 = s.lastIndexOf(";_");
			int ind2 = s.lastIndexOf("<>");
			if ((ind1 > 0) && (ind2 > ind1)) {
				return s.substring(ind1 + 2).trim();
			} else {
				return "";
			}
		} // end of LTRTSDColumn.vText()
		
	} // end of LTRTSDColumn


/**
* Column for 'U5NNScore' column in LTRs with LTRCandidate.u5Score.
*/
	protected class LTRU5NNScoreColumn extends FloatColumn {
	
		LTRU5NNScoreColumn() throws SQLException {
			columnName = "U5NNScore";
		} // end of LTRU5NNScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.u5Score);
		} // end of LTRU5NNScoreColumn.valueText();
		
	} // end of LTRU5NNScoreColumn


/**
* Column for 'U5NNPos' column in LTRs with externalized LTRCandidate.u5Position.
*/
	protected class LTRU5NNPosColumn extends IntColumn {
	
		LTRU5NNPosColumn() throws SQLException {
			columnName = "U5NNPos";
		} // end of LTRU5NNPosColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.u5Position));
		} // end of LTRU5NNPosColumn.valueText()
		
	} // end of LTRU5NNPosColumn


/**
* Column for 'GTModifierScore' column in LTRs with LTRCandidate.gtScore.
*/
	protected class LTRGTModifierScoreColumn extends FloatColumn {
	
		LTRGTModifierScoreColumn() throws SQLException {
			columnName = "GTModifierScore";
		} // end of LTRGTModifierScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.gtScore);
		} // end of LTRGTModifierScoreColumn.valueText()
		
	} // end of LTRGTModifierScoreColumn


/**
* Column for 'U3NNScore' column in LTRs with LTRCandidate.u3Score.
*/
	protected class LTRU3NNScoreColumn extends FloatColumn {
	
		LTRU3NNScoreColumn() throws SQLException {
			columnName = "U3NNScore";
		} // end of LTRU3NNScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.u3Score);
		} // end of LTRU3NNScoreColumn.valueText()
		
	} // end of LTRU3NNScoreColumn


/**
* Column for 'U3NNPos' column in LTRs with externalized LTRCandidate.u3Position.
*/
	protected class LTRU3NNPosColumn extends IntColumn {
	
		LTRU3NNPosColumn() throws SQLException {
			columnName = "U3NNPos";
		} // end of LTRU3NNPosColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.u3Position));
		} // end of LTRU3NNPosColumn.valueText()
		
	} // end of LTRU3NNPosColumn


/**
* Column for 'TATAAScore' column in LTRs with LTRCandidate.tataaScore.
*/
	protected class LTRTATAAScoreColumn extends FloatColumn {
	
		LTRTATAAScoreColumn() throws SQLException {
			columnName = "TATAAScore";
		} // end of LTRTATAAScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.tataaScore);
		} // end of LTRTATAAScoreColumn.valueText()
		
	} // end of LTRTATAAScoreColumn


/**
* Column for 'TATAAPos' column in LTRs with externalized LTRCandidate.tataaPosition.
*/
	protected class LTRTATAAPosColumn extends IntColumn {
	
		LTRTATAAPosColumn() throws SQLException {
			columnName = "TATAAPos";
		} // end of LTRTATAAPosColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.tataaPosition));
		} // end of LTRTATAAPosColumn.valueText()
		
	} // end of LTRTATAAPosColumn


/**
* Column for 'MEME50Score' column in LTRs with LTRCandidate.mEME50Score.
*/
	protected class LTRMEME50ScoreColumn extends FloatColumn {
	
		LTRMEME50ScoreColumn() throws SQLException {
			columnName = "MEME50Score";
		} // end of LTRMEME50ScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.mEME50Score);
		} // end of LTRMEME50ScoreColumn.valueText()
		
	} // end of LTRMEME50ScoreColumn


/**
* Column for 'MEME50Pos' column in LTRs with externalized LTRCandidate.mEME50Position.
*/
	protected class LTRMEME50PosColumn extends IntColumn {
	
		LTRMEME50PosColumn() throws SQLException {
			columnName = "MEME50Pos";
		} // end of LTRMEME50PosColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.mEME50Position));
		} // end of LTRMEME50PosColumn.valueText()
		
	} // end of LTRMEME50PosColumn


/**
* Column for 'Motifs1Score' column in LTRs with LTRCandidate.mot1Score.
*/
	protected class LTRMotifs1ScoreColumn extends FloatColumn {
	
		LTRMotifs1ScoreColumn() throws SQLException {
			columnName = "Motifs1Score";
		} // end of LTRMotifs1ScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.mot1Score);
		} // end of LTRMotifs1ScoreColumn.valueText()
		
	} // end of LTRMotifs1ScoreColumn


/**
* Column for 'Motifs1Pos' column in LTRs with externalized LTRCandidate.mot1Position.
*/
	protected class LTRMotifs1PosColumn extends IntColumn {
	
		LTRMotifs1PosColumn() throws SQLException {
			columnName = "Motifs1Pos";
		} // end of LTRMotifs1PosColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.mot1Position));
		} // end of LTRMotifs1PosColumn.valueText()
		
	} // end of LTRMotifs1PosColumn


/**
* Column for 'Motifs2Score' column in LTRs with LTRCandidate.mot2Score.
*/
	protected class LTRMotifs2ScoreColumn extends FloatColumn {
	
		LTRMotifs2ScoreColumn() throws SQLException {
			columnName = "Motifs2Score";
		} // end of LTRMotifs2ScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.mot2Score);
		} // end of LTRMotifs2ScoreColumn.valueText()
		
	} // end of LTRMotifs2ScoreColumn


/**
* Column for 'Motifs2Pos' column in LTRs with externalized LTRCandidate.mot2Position.
*/
	protected class LTRMotifs2PosColumn extends IntColumn {
	
		LTRMotifs2PosColumn() throws SQLException {
			columnName = "Motifs2Pos";
		} // end of LTRMotifs2PosColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.LTRCANDIDATEDNA.externalize(currentLTRCandidate.mot2Position));
		} // end of LTRMotifs2PosColumn.valueText()
		
	} // end of LTRMotifs2PosColumn


/**
* Column for 'TranssitesScore' column in LTRs with LTRCandidate.transScore.
*/
	protected class LTRTranssitesScoreColumn extends FloatColumn {
	
		LTRTranssitesScoreColumn() throws SQLException {
			columnName = "TranssitesScore";
		} // end of LTRTranssitesScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.transScore);
		} // end of LTRTranssitesScoreColumn.valueText()
		
	} // end of LTRTranssitesScoreColumn


/**
* Column for 'CpGModifierScore' column in LTRs with LTRCandidate.cpgScore.
*/
	protected class LTRCpGModifierScoreColumn extends FloatColumn {
	
		LTRCpGModifierScoreColumn() throws SQLException {
			columnName = "CpGModifierScore";
		} // end of LTRCpGModifierScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.cpgScore);
		} // end of LTRCpGModifierScoreColumn.valueText()
		
	} // end of LTRCpGModifierScoreColumn


/**
* Column for 'Spl8ModifierScore' column in LTRs with LTRCandidate.spl8Score.
*/
	protected class LTRSpl8ModifierScoreColumn extends FloatColumn {
	
		LTRSpl8ModifierScoreColumn() throws SQLException {
			columnName = "Spl8ModifierScore";
		} // end of LTRSpl8ModifierScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentLTRCandidate.spl8Score);
		} // end of LTRSpl8ModifierScoreColumn.valueText()
		
	} // end of LTRSpl8ModifierScoreColumn


/**
* Column for 'LTRFileName' column in LTRs with the name of the RetroVID script or Chains file containing this LTR.
*/
	protected class LTRFileNameColumn extends CharacterColumn {
	
		LTRFileNameColumn() throws SQLException {
			super(30);
			columnName = "LTRFileName";
		} // end of LTRFileNameColumn.constructor()
		
		protected final String vText() {
			return currentLTRFile.getName();
		} // end of LTRFileNameColumn.vText()
		
	} // end of LTRFileNameColumn


/**
* Column for 'Gene' column in Puteins with Gene name.
*/
	protected class PuteinGeneColumn extends CharacterColumn {
	
		PuteinGeneColumn() throws SQLException {
			super(3);
			columnName = "Gene";
		} // end of PuteinGeneColumn.constructor()
		
		protected final String vText() {
			if (isEnvTrace) {
				return "ENV";
			} else {
				Object o = currentPuteinTable.get(Putein.GENEKEY);
				if (o != null) {
					return (String) o;
				} else {
					return FileNamer.getGeneFromPutein(currentPuteinFile.getName());
				}
			}
		} // end of PuteinGeneColumn.vText()
		
	} // end of PuteinGeneColumn

/**
* Column for 'PuteinGenus' column in Puteins with putein genus.
*/
	protected class PuteinGenusColumn extends CharacterColumn {
	
		PuteinGenusColumn() throws SQLException {
			super(1);
			columnName = "PuteinGenus";
		} // end of PuteinGenusColumn.constructor()
		
		protected final String vText() {
			if (isEnvTrace) {
				return "";
			}
			Object o = currentPuteinTable.get(Putein.GENUSKEY);
			if (o != null) {
				return (String) o;
			} else {
				return FileNamer.getGenusFromPutein(currentPuteinFile.getName());
			}
		} // end of PuteinGenusColumn.vText()
		
	} // end of PuteinGenusColumn


/**
* Column for 'EstimatedFirst' column in Puteins with putein EstimatedStartPosition.
*/
	protected class PuteinEstimatedFirstColumn extends IntColumn {
	
		PuteinEstimatedFirstColumn() throws SQLException {
			columnName = "EstimatedFirst";
		} // end of PuteinEstimatedFirstColumn.constructor()
		
		protected final String valueText() {
			return (String) currentPuteinTable.get(Putein.ESTIMATEDSTARTPOSITIONKEY);
		} // end of PuteinEstimatedFirstColumn.valueText()
		
	} // end of PuteinEstimatedFirstColumn


/**
* Column for 'EstimatedLast' column in Puteins with putein EstimatedLastPosition.
*/
	protected class PuteinEstimatedLastColumn extends IntColumn {
	
		PuteinEstimatedLastColumn() throws SQLException {
			columnName = "EstimatedLast";
		} // end of PuteinEstimatedLastColumn.constructor()
		
		protected final String valueText() {
			return (String) currentPuteinTable.get(Putein.ESTIMATEDLASTPOSITIONKEY);
		} // end of PuteinEstimatedLastColumn.valueText()
		
	} // end of PuteinEstimatedLastColumn


/**
* Column for 'Putein' column in Puteins with acid sequence between EstimatedStartPosition
* and EstimatedLastPosition.
*/
	protected class PuteinColumn extends SequenceColumn {
	
		PuteinColumn() throws SQLException {
			columnName = "Putein";
		} // end of PuteinColumn.constructor()
		
		protected final String vText() {
			String[ ] ss = (String[ ]) currentPuteinTable.get(Putein.PUTEINKEY);
			if (ss == null) {
				ss = (String[ ]) currentPuteinTable.get(Xon.XONKEY);
			}
			String puteinLine = ss[Putein.PUTEINLINEINDEX];
			String fourthLine = ss[Putein.FOURTHLINEINDEX];
			int ind1 = fourthLine.indexOf(">") + 1;
			int ind2 = fourthLine.indexOf("<");
			return puteinLine.substring(ind1, ind2);
		} // end of PuteinColumn.vText()
		
	} // end of PuteinColumn


/**
* Column for 'StopCodons' column in Puteins with StopCodonsInside.
*/
	protected class PuteinStopCodonsColumn extends IntColumn {
	
		PuteinStopCodonsColumn() throws SQLException {
			columnName = "StopCodons";
		} // end of PuteinStopCodonsColumn.constructor()
		
		protected final String valueText() {
			if (isEnvTrace) {
				return (String) currentPuteinTable.get(Xon.STOPSKEY);
			}
			String s = (String) currentPuteinTable.get(Putein.STOPCODONSINSIDEKEY);
			if (s == null) {
				return "NULL";
			} else {
				return s.substring(0, s.indexOf("(")).trim();
			}
		} // end of PuteinStopCodonsColumn.valueText()
		
	} // end of PuteinStopCodonsColumn


/**
* Column for 'Shifts' column in Puteins with ShiftsInside.
*/
	protected class PuteinShiftsColumn extends IntColumn {
	
		PuteinShiftsColumn() throws SQLException {
			columnName = "Shifts";
		} // end of PuteinShiftsColumn.constructor()
		
		protected final String valueText() {
			String s = (String) currentPuteinTable.get(Putein.SHIFTSINSIDEKEY);
			if (s == null) {
				s = (String) currentPuteinTable.get(Xon.SHIFTSKEY);
			}
			return s;
		} // end of PuteinShiftsColumn.valueText()
		
	} // end of PuteinShiftsColumn


/**
* Column for 'LongestORF' column in Puteins with acid sequence in LongestORF.
*/
	protected class PuteinLongestORFColumn extends SequenceColumn {
	
		PuteinLongestORFColumn() throws SQLException {
			columnName = "LongestORF";
		} // end of PuteinLongestORFColumn.constructor()
		
		protected final String vText() {
			String s = (String) currentPuteinTable.get(Putein.LONGESTORFKEY);
			if (s == null) {
				return "";
			} else if (isEnvTrace) {
				return s;
			} else {
				return Utilities.splitString(s)[1];
			}
		} // end of PuteinLongestORFColumn.vText()
		
	} // end of PuteinLongestORFColumn


/**
* Column for 'LongestORFPos' column in Puteins with start position in LongestORF.
*/
	protected class PuteinLongestORFPosColumn extends IntColumn {
	
		PuteinLongestORFPosColumn() throws SQLException {
			columnName = "LongestORFPos";
		} // end of PuteinLongestORFPosColumn.constructor()
		
		protected final String valueText() {
			if (isEnvTrace) {
				return (String) currentPuteinTable.get("LongestORFStart");
			}
			String s = (String) currentPuteinTable.get(Putein.LONGESTORFKEY);
			if (s == null) {
				return "NULL";
			} else {
				return Utilities.splitString(s)[0];
			}
		} // end of PuteinLongestORFPosColumn.valueText()
		
	} // end of PuteinLongestORFPosColumn


/**
* Column for 'ChainId' column in Puteins with id of containing chain.
*/
	protected class PuteinChainIdColumn extends IntColumn {
	
		PuteinChainIdColumn() throws SQLException {
			columnName = "ChainId";
		} // end of PuteinChainIdColumn.constructor()
		
/**
* Filled in later.
*/
		protected final String valueText() {
			return "NULL";
		} // end of PuteinChainIdColumn.valueText()
		
	} // end of PuteinChainIdColumn


/**
* Column for 'Length' column in Puteins with LengthInside.
*/
	protected class PuteinLengthColumn extends IntColumn {
	
		PuteinLengthColumn() throws SQLException {
			columnName = "Length";
		} // end of PuteinLengthColumn.constructor()
		
		protected final String valueText() {
			if (isEnvTrace) {
				return (String) currentPuteinTable.get("Length");
			}
			return (String) currentPuteinTable.get(Putein.LENGTHINSIDEKEY);
		} // end of PuteinLengthColumn.valueText()
		
	} // end of PuteinLengthColumn


/**
* Column for 'AlignedAcids' column in Puteins with AlignedAcids.
*/
	protected class PuteinAlignedAcidsColumn extends IntColumn {
	
		PuteinAlignedAcidsColumn() throws SQLException {
			columnName = "AlignedAcids";
		} // end of PuteinAlignedAcidsColumn.constructor()
		
		protected final String valueText() {
			return (String) currentPuteinTable.get(Putein.ALIGNEDACIDSKEY);
		} // end of PuteinAlignedAcidsColumn.valueText()
		
	} // end of PuteinAlignedAcidsColumn


/**
* Column for 'AverageScore' column in Puteins with AverageScoreInside.
*/
	protected class PuteinAverageScoreColumn extends FloatColumn {
	
		PuteinAverageScoreColumn() throws SQLException {
			columnName = "AverageScore";
		} // end of PuteinAverageScoreColumn.constructor()
		
		protected final String valueText() {
			return (String) currentPuteinTable.get(Putein.AVERAGESCOREINSIDEKEY);
		} // end of PuteinAverageScoreColumn.valueText()
		
	} // end of PuteinAverageScoreColumn


/**
* Column for 'MostUsedRow' column in Puteins with MostUsedRow.
*/
	protected class PuteinMostUsedRowColumn extends VarCharColumn {
	
		PuteinMostUsedRowColumn() throws SQLException {
			super(100);
			columnName = "MostUsedRow";
		} // end of PuteinMostUsedRowColumn.constructor()
		
		protected final String vText() {
			String s = (String) currentPuteinTable.get(Putein.MOSTUSEDROWKEY);
			if (s == null) {
				return "";
			}
			String[ ] ss = Utilities.splitString(s);
			s = ss[1].substring(1);
			return s.substring(0, s.length() - 1);
		} // end of PuteinMostUsedRowColumn.vText()
		
	} // end of PuteinMostUsedRowColumn


/**
* Column for 'LongestRunLength' column in Puteins with length part of LongestRun.
*/
	protected class PuteinLongestRunLengthColumn extends IntColumn {
	
		PuteinLongestRunLengthColumn() throws SQLException {
			columnName = "LongestRunLength";
		} // end of PuteinLongestRunLengthColumn.constructor()
		
		protected final String valueText() {
			String s = (String) currentPuteinTable.get(Putein.LONGESTRUNKEY);
			if (s == null) {
				return "NULL";
			} else {
				return Utilities.splitString(s)[0];
			}
		} // end of PuteinLongestRunLengthColumn.valueText()
		
	} // end of PuteinLongestRunLengthColumn


/**
* Column for 'LongestRunPos' column in Puteins with position part of LongestRun.
*/
	protected class PuteinLongestRunPosColumn extends IntColumn {
	
		PuteinLongestRunPosColumn() throws SQLException {
			columnName = "LongestRunPos";
		} // end of PuteinLongestRunPosColumn.constructor()
		
		protected final String valueText() {
			String s = (String) currentPuteinTable.get(Putein.LONGESTRUNKEY);
			if (s == null) {
				return "NULL";
			} else {
				return Utilities.splitString(s)[2];
			}
		} // end of PuteinLongestRunPosColumn.valueText()
		
	} // end of PuteinLongestRunPosColumn


/**
* Column for 'PuteinSequence' column in Chains.
*/
	protected class PuteinSequenceColumn extends SequenceColumn {
	
		PuteinSequenceColumn() throws SQLException {
			columnName = "PuteinSequence";
		} // end of PuteinSequenceColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			String s = (String) currentPuteinTable.get(Putein.ESTIMATEDSTARTPOSITIONKEY);
			int e1 = Math.max(0, currentDNA.internalize(Utilities.decodeInt(s)));
			s = (String) currentPuteinTable.get(Putein.ESTIMATEDLASTPOSITIONKEY);
			int e2 = currentDNA.internalize(Utilities.decodeInt(s));
			if ((e2 < 0) | (e2 > currentDNA.LENGTH)) {
				e2 = currentDNA.LENGTH - 1;
			}
			if (e1 >= e2) {
				return "";
			}
			return currentDNA.subString(e1, e2, true);
		} // end of PuteinSequenceColumn.vText()
		
	} // end of PuteinSequenceColumn


/**
* Column for 'CodonSequence' column in Chains.
*/
	protected class PuteinCodonSequenceColumn extends SequenceColumn {
	
		PuteinCodonSequenceColumn() throws SQLException {
			columnName = "CodonSequence";
		} // end of PuteinCodonSequenceColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			return (String) currentPuteinTable.get(Putein.CODONSEQUENCEKEY);
		} // end of PuteinCodonSequenceColumn.vText()
		
	} // end of PuteinCodonSequenceColumn


/**
* Column for 'Duplicator' column in Puteins with id of very similar earlier putein.
*/
	protected class PuteinDuplicatorColumn extends IntColumn {
	
		PuteinDuplicatorColumn() throws SQLException {
			columnName = "Duplicator";
		} // end of PuteinDuplicatorColumn.constructor()
		
		protected final String valueText() {
			if (currentPuteinDuplicator < 0) {
				return "NULL";
			} else {
				return String.valueOf(currentPuteinDuplicator);
			}
		} // end of PuteinDuplicatorColumn.valueText()
		
	} // end of PuteinDuplicatorColumn


/**
* Column for 'PuteinFile' column in Puteins with name of containing chain.
*/
	protected class PuteinFileColumn extends VarCharColumn {
	
		PuteinFileColumn() throws SQLException {
			super(40);
			columnName = "PuteinFile";
		} // end of PuteinFileColumn.constructor()
		
		protected final String vText() {
			return currentPuteinFile.getName();
		} // end of PuteinFileColumn.vText()
		
	} // end of PuteinFileColumn


/**
* Column for 'Chainname' column in Puteins with name of containing chain.
*/
	protected class PuteinChainnameColumn extends CharacterColumn {
	
		PuteinChainnameColumn() throws SQLException {
			super(6);
			columnName = "Chainname";
		} // end of PuteinChainnameColumn.constructor()
		
		protected final String vText() {
			if (isEnvTrace) {
				return FileNamer.getChainnameFromEnvTrace(currentPuteinFile.getName());
			}
			return FileNamer.getChainnameFromPutein(currentPuteinFile.getName());
		} // end of PuteinChainnameColumn.vText()
		
	} // end of PuteinChainnameColumn


/**
* Column for 'Comment' column in Puteins empty or with best reference protein.
*/
	protected class PuteinCommentColumn extends TextColumn {
	
		PuteinCommentColumn() throws SQLException {
			super();
			columnName = "PuteinComment";
		} // end of PuteinCommentColumn.constructor()

		protected final String vText() {
			Object o = currentPuteinTable.get(Putein.POLCLASSKEY);
			if (o == null) {
				return "";
			} else {
				return (String) o;
			}
		} // end of PuteinCommentColumn.vText()
		
	} // end of PuteinCommentColumn



/**
* Column for 'id' column in Chains with unique integer.
*/
	protected class ChainIdColumn extends IntColumn {
	
		ChainIdColumn() throws SQLException {
			columnName = "id";
		} // end of ChainIdColumn.constructor()
		
		protected final String valueText() {
			latestChainID++;
			return String.valueOf(latestChainID);
		} // end of ChainIdColumn.valueText()
		
	} // end of ChainIdColumn


/**
* Column for 'ChainInfo' column in Chains.
*/
	protected class ChainInfoColumn extends TextColumn {
	
		ChainInfoColumn() throws SQLException {
			columnName = "ChainInfo";
		} // end of ChainInfoColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			String[ ] ss = currentChainGraphInfo.toStrings(currentDNA);
			return Utilities.collapseLines(ss);
		} // end of ChainInfoColumn.vText()
		
	} // end of ChainInfoColumn


/**
* Column for 'ChainSequence' column in Chains.
*/
	protected class ChainSequenceColumn extends SequenceColumn {
	
		ChainSequenceColumn() throws SQLException {
			columnName = "ChainSequence";
		} // end of .constructor()
		
		protected final String vText() throws RetroTectorException {
			return currentDNA.subString(currentChainGraphInfo.FIRSTBASEPOS, currentChainGraphInfo.LASTBASEPOS, true);
		} // end of ChainSequenceColumn.vText()
		
	} // end of ChainSequenceColumn


/**
* Column for 'Score' column in Chains with ChainGraphInfo.SCORE.
*/
	protected class ChainScoreColumn extends IntColumn {
	
		ChainScoreColumn() throws SQLException {
			columnName = "Score";
		} // end of ChainScoreColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentChainGraphInfo.SCORE);
		} // end of ChainScoreColumn.valueText()
		
	} // end of ChainScoreColumn


/**
* Column for 'Genus' column in Chains with ChainGraphInfo.GENUS.
*/
	protected class ChainGenusColumn extends VarCharColumn {
	
		ChainGenusColumn() throws SQLException {
			super(10);
			columnName = "ChainGenus";
		} // end of ChainGenusColumn.constructor()
		
		protected final String vText() {
			return currentChainGraphInfo.GENUS;
		} // end of ChainGenusColumn.vText()
		
	} // end of ChainGenusColumn


/**
* Column for 'Start' column in Chains with externalized ChainGraphInfo.FIRSTBASEPOS.
*/
	protected class ChainStartColumn extends IntColumn {
	
		ChainStartColumn() throws SQLException {
			columnName = "Start";
		} // end of ChainStartColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentDNA.externalize(currentChainGraphInfo.FIRSTBASEPOS));
		} // end of ChainStartColumn.valueText()
		
	} // end of ChainStartColumn


/**
* Column for 'End' column in Chains with externalized ChainGraphInfo.LASTBASEPOS.
*/
	protected class ChainEndColumn extends IntColumn {
	
		ChainEndColumn() throws SQLException {
			columnName = "End";
		} // end of ChainEndColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentDNA.externalize(currentChainGraphInfo.LASTBASEPOS));
		} // end of ChainEndColumn.valueText()
		
	} // end of ChainStartColumn


/**
* Column for 'SubGenes' column in Chains with list of subgenes.
*/
	protected class ChainSubGenesColumn extends VarCharColumn {
	
		ChainSubGenesColumn() throws SQLException {
			super(100);
			columnName = "SubGenes";
		} // end of ChainSubGenesColumn.constructor()
		
		protected final String vText() {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<currentChainGraphInfo.subgeneinfo.length; i++) {
				sb.append(currentChainGraphInfo.subgeneinfo[i].SUBGENENAME);
				sb.append(" ");
			}
			return sb.toString().trim();
		} // end of ChainSubGenesColumn.vText()
		
	} // end of ChainSubGenesColumn


/**
* Column for 'Breaks' column in Chains with list of subgenes.
*/
	protected class ChainBreaksColumn extends VarCharColumn {
	
		ChainBreaksColumn() throws SQLException {
			super(100);
			columnName = "Breaks";
		} // end of ChainBreaksColumn.constructor()
		
		protected final String vText() {
			return currentChainGraphInfo.BREAKS;
		} // end of ChainBreaksColumn.vText()
		
	} // end of ChainBreaksColumn


/**
* Column for 'PreFlankSwept' column in Chains.
*/
	protected class ChainPreFlankSweptColumn extends SequenceColumn {
	
		ChainPreFlankSweptColumn() throws SQLException {
			columnName = "PreFlankSwept";
		} // end of ChainPreFlankSweptColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			return currentDNA.subString(Math.max(0, currentChainGraphInfo.FIRSTBASEPOS - 2000), Math.max(0, currentChainGraphInfo.FIRSTBASEPOS - 1), true);
		} // end of ChainPreFlankSweptColumn.vText()
		
	} // end of ChainPreFlankSweptColumn


/**
* Column for 'PreFlankRaw' column in Chains.
*/
	protected class ChainPreFlankRawColumn extends SequenceColumn {
	
		ChainPreFlankRawColumn() throws SQLException {
			columnName = "PreFlankRaw";
		} // end of ChainPreFlankRawColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			String s = currentDNA.subStringWithInserts(Math.max(0, currentChainGraphInfo.FIRSTBASEPOS - 2000), (Math.max(0, currentChainGraphInfo.FIRSTBASEPOS - 1)));
			if (s.length() <= 2000) {
				return s;
			} else {
				return s.substring(s.length() - 2000);
			}
		} // end of ChainPreFlankRawColumn.vText()
		
	} // end of ChainPreFlankSweptColumn


/**
* Column for 'LTR5id' column in Chains with id of 5'LTR in LTRs.
*/
	protected class ChainLTR5idColumn extends IntColumn {
	
		ChainLTR5idColumn() throws SQLException {
			columnName = "LTR5id";
		} // end of ChainLTR5idColumn.constructor()
		
/**
* Also updates InChain in relevant LTR.
*/
		protected final String valueText() throws RetroTectorException  {
			if (current5LTR == null) {
				return "NULL";
			}
			CollectGenome.LTRSummary summ = new CollectGenome.LTRSummary(current5LTR, currentDNA);
			CollectGenome.LTRSummary summ2 = (CollectGenome.LTRSummary) candTable.get(summ);
			int irs = -1;
			if (summ2 != null) {
				irs = summ2.id;
				String co = "update " + LTRSNAME + " set InChain = " + latestChainID + " where id = " + irs;
				try {
					executeSQLCommand(co);
				} catch (SQLException e) {
					haltError(e);
				}
			}
			return String.valueOf(irs);
		} // end of ChainLTR5idColumn.valueText()
		
	} // end of ChainLTR5idColumn


/**
* Column for 'PBSStart' column in Chains with first position in PBS, if any.
*/
	protected class ChainPBSStartColumn extends IntColumn {
	
		ChainPBSStartColumn() throws SQLException {
			columnName = "PBSStart";
		} // end of ChainPBSStartColumn.constructor()
		
		protected final String valueText() {
			if (currentPBS == null) {
				return "NULL";
			}
			return String.valueOf(currentDNA.externalize(currentPBS.FIRSTPOS));
		} // end of ChainPBSStartColumn.valueText()
		
	} // end of ChainPBSStartColumn


/**
* Column for 'PBSEnd' column in Chains with last position in PBS, if any.
*/
	protected class ChainPBSEndColumn extends IntColumn {
	
		ChainPBSEndColumn() throws SQLException {
			columnName = "PBSEnd";
		} // end of ChainPBSEndColumn.constructor()
		
		protected final String valueText() {
			if (currentPBS == null) {
				return "NULL";
			}
			return String.valueOf(currentDNA.externalize(currentPBS.LASTPOS));
		} // end of ChainPBSEndColumn.valueText()
		
	} // end of ChainPBSEndColumn


/**
* Column for 'PBSScore' column in Chains with score of PBS, if any.
*/
	protected class ChainPBSScoreColumn extends IntColumn {
	
		ChainPBSScoreColumn() throws SQLException {
			columnName = "PBSScore";
		} // end of ChainPBSScoreColumn.constructor()
		
		protected final String valueText() {
			if (currentPBS == null) {
				return "NULL";
			}
			return String.valueOf(currentPBS.SCORE);
		} // end of ChainPBSScoreColumn.valueText()
		
	} // end of ChainPBSScoreColumn


/**
* Column for 'PBSType' column in Chains with primer type of PBS.
*/
	protected class ChainPBSTypeColumn extends CharacterColumn {
	
		ChainPBSTypeColumn() throws SQLException {
			super(1);
			columnName = "PBSType";
		} // end of ChainPBSTypeColumn.constructor()
		
		protected final String vText() {
			if (currentPBS == null) {
				return "";
			}
			String s = currentPBS.MOTIFDESCRIPTION;
			int i = s.indexOf("tRNA");
			if (i < 0) {
				return "";
			}
			s = s.substring(i + 4, i + 7).toUpperCase();
			i = "LYS TYR TRP HIS THR SER LEU PRO ARG PHE ASN ASP GLU MET ILE CYS ALA GLY GLN VAL".indexOf(s);
			if (i < 0) {
				return "";
			}
			return String.valueOf("KYWHTSLPRFNDEMICAGQV".charAt(i / 4));
		} // end of ChainPBSTypeColumn.vText()
		
	} // end of ChainPBSTypeColumn


/**
* Column for 'GagPuteinid' column in Chains with a comma-separated list of ids of Gag
* puteins in the Generation in use, belonging to this chain, if any.
*/
	protected class ChainGagPuteinColumn extends VarCharColumn {
	
		ChainGagPuteinColumn() throws SQLException {
			super(15);
			columnName = "GagPuteinid";
		} // end of ChainGagPuteinColumn.constructor()
		
/**
* Also updates ChainId in relevant Puteins.
*/
		protected final String vText() throws RetroTectorException {
			String fir = getColumnTitle("PuteinEstimatedFirstColumn");
			String las = getColumnTitle("PuteinEstimatedLastColumn");
			ResultSet rset;
			String co;
			int ii;
			if (!multiPuteins) {
				co = "select id, " + fir + ", " + las + " from " + PUTEINSNAME + " where Gene = \'Gag\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP + " and Chromosome = \'" + currentChromosome + "\' and Subdirectory = \'" + subDir() + "\' and PuteinGenus = \'" + currentChainGraphInfo.getBestGenus() + BLIP;
				try {
					rset = executeSQLQuery(co);
					rset.first();
					ii = rset.getInt("id");
					executeSQLCommand("update " + PUTEINSNAME + " set ChainId = " + latestChainID + " where id = " + ii);
					return String.valueOf(ii);
				} catch (SQLException e) {
				}
			}
			co = "select id, " + fir + ", " + las + " from " + PUTEINSNAME + " where Gene = \'Gag\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP + " and Chromosome = \'" + currentChromosome + "\' and Subdirectory = \'" + subDir() + "\' order by AverageScore desc";
			StringBuffer sb = new StringBuffer();
			Stack coStack = new Stack();
			try {
				rset = executeSQLQuery(co);
				rset.first();
				for (;;) {
					ii = rset.getInt("id");
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(ii);
					coStack.push("update " + PUTEINSNAME + " set ChainId = " + latestChainID + " where id = " + ii);
					currentFirstUsedPos = Math.min(currentFirstUsedPos, currentDNA.internalize(rset.getInt(fir)));
					currentLastUsedPos = Math.max(currentLastUsedPos, currentDNA.internalize(rset.getInt(las)));
					rset.next();
				}
			} catch (SQLException e) {
			}
			for (int coi=0; coi<coStack.size(); coi++) {
				if (multiPuteins | (coi == 0)) {
					co = (String) coStack.elementAt(coi);
					try {
						executeSQLCommand(co);
					} catch (SQLException sq) {
						haltError(sq);
					}
				}
			}
			return sb.toString();
		} // end of ChainGagPuteinColumn.vText()
		
	} // end of ChainGagPuteinColumn


/**
* Column for 'ProPuteinid' column in Chains with a comma-separated list of ids of Pro
* puteins in the Generation in use, belonging to this chain, if any.
*/
	protected class ChainProPuteinColumn extends VarCharColumn {
	
		ChainProPuteinColumn() throws SQLException {
			super(15);
			columnName = "ProPuteinid";
		} // end of ChainProPuteinColumn.constructor()
		
/**
* Also updates ChainId in relevant Puteins.
*/
		protected final String vText() throws RetroTectorException {
			String fir = getColumnTitle("PuteinEstimatedFirstColumn");
			String las = getColumnTitle("PuteinEstimatedLastColumn");
			ResultSet rset;
			String co;
			int ii;
			if (!multiPuteins) {
				co = "select id, " + fir + ", " + las + " from " + PUTEINSNAME + " where Gene = \'Pro\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP + " and Chromosome = \'" + currentChromosome + "\' and Subdirectory = \'" + subDir() + "\' and PuteinGenus = \'" + currentChainGraphInfo.getBestGenus() + BLIP;
				try {
					rset = executeSQLQuery(co);
					rset.first();
					ii = rset.getInt("id");
					executeSQLCommand("update " + PUTEINSNAME + " set ChainId = " + latestChainID + " where id = " + ii);
					return String.valueOf(ii);
				} catch (SQLException e) {
				}
			}
			co = "select id, " + fir + ", " + las + " from " + PUTEINSNAME + " where Gene = \'Pro\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP + " and Chromosome = \'" + currentChromosome + "\' and Subdirectory = \'" + subDir() + "\' order by AverageScore desc";
			StringBuffer sb = new StringBuffer();
			Stack coStack = new Stack();
			try {
				rset = executeSQLQuery(co);
				rset.first();
				for (;;) {
					ii = rset.getInt("id");
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(ii);
					coStack.push("update " + PUTEINSNAME + " set ChainId = " + latestChainID + " where id = " + ii);
					currentFirstUsedPos = Math.min(currentFirstUsedPos, currentDNA.internalize(rset.getInt(fir)));
					currentLastUsedPos = Math.max(currentLastUsedPos, currentDNA.internalize(rset.getInt(las)));
					rset.next();
				}
			} catch (SQLException e) {
			}
			for (int coi=0; coi<coStack.size(); coi++) {
				if (multiPuteins | (coi == 0)) {
					co = (String) coStack.elementAt(coi);
					try {
						executeSQLCommand(co);
					} catch (SQLException sq) {
						haltError(sq);
					}
				}
			}
			return sb.toString();
		} // end of ChainProPuteinColumn.vText()
		
	} // end of ChainProPuteinColumn


/**
* Column for 'PolPuteinid' column in Chains with a comma-separated list of ids of Pol
* puteins in the Generation in use, belonging to this chain, if any.
*/
	protected class ChainPolPuteinColumn extends VarCharColumn {
	
		ChainPolPuteinColumn() throws SQLException {
			super(15);
			columnName = "PolPuteinid";
		} // end of ChainPolPuteinColumn.constructor()
		
/**
* Also updates ChainId in relevant Puteins.
*/
		protected final String vText() throws RetroTectorException {
			String fir = getColumnTitle("PuteinEstimatedFirstColumn");
			String las = getColumnTitle("PuteinEstimatedLastColumn");
			ResultSet rset;
			String co;
			int ii;
			if (!multiPuteins) {
				co = "select id, " + fir + ", " + las + " from " + PUTEINSNAME + " where Gene = \'Pol\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP + " and Chromosome = \'" + currentChromosome + "\' and Subdirectory = \'" + subDir() + "\' and PuteinGenus = \'" + currentChainGraphInfo.getBestGenus() + BLIP;
				try {
					rset = executeSQLQuery(co);
					rset.first();
					ii = rset.getInt("id");
					executeSQLCommand("update " + PUTEINSNAME + " set ChainId = " + latestChainID + " where id = " + ii);
					return String.valueOf(ii);
				} catch (SQLException e) {
				}
			}
			co = "select id, " + fir + ", " + las + " from " + PUTEINSNAME + " where Gene = \'Pol\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP + " and Chromosome = \'" + currentChromosome + "\' and Subdirectory = \'" + subDir() + "\' order by AverageScore desc";
			StringBuffer sb = new StringBuffer();
			Stack coStack = new Stack();
			try {
				rset = executeSQLQuery(co);
				rset.first();
				for (;;) {
					ii = rset.getInt("id");
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(ii);
					coStack.push("update " + PUTEINSNAME + " set ChainId = " + latestChainID + " where id = " + ii);
					currentFirstUsedPos = Math.min(currentFirstUsedPos, currentDNA.internalize(rset.getInt(fir)));
					currentLastUsedPos = Math.max(currentLastUsedPos, currentDNA.internalize(rset.getInt(las)));
					rset.next();
				}
			} catch (SQLException e) {
			}
			for (int coi=0; coi<coStack.size(); coi++) {
				if (multiPuteins | (coi == 0)) {
					co = (String) coStack.elementAt(coi);
					try {
						executeSQLCommand(co);
					} catch (SQLException sq) {
						haltError(sq);
					}
				}
			}
			return sb.toString();
		} // end of ChainPolPuteinColumn.vText()
		
	} // end of ChainPolPuteinColumn
	

/**
* Column for 'EnvPuteinid' column in Chains with a comma-separated list of ids of Env
* puteins in the Generation in use, belonging to this chain, if any.
*/
	protected class ChainEnvPuteinColumn extends VarCharColumn {
	
		ChainEnvPuteinColumn() throws SQLException {
			super(15);
			columnName = "EnvPuteinid";
		} // end of .constructor()
		
/**
* Also updates ChainId in relevant Puteins.
*/
		protected final String vText() throws RetroTectorException {
			String fir = getColumnTitle("PuteinEstimatedFirstColumn");
			String las = getColumnTitle("PuteinEstimatedLastColumn");
			ResultSet rset;
			String co;
			int ii;
			if (!multiPuteins) {
				co = "select id, " + fir + ", " + las + " from " + PUTEINSNAME + " where Gene = \'Env\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP + " and Chromosome = \'" + currentChromosome + "\' and Subdirectory = \'" + subDir() + "\' and PuteinGenus = \'" + currentChainGraphInfo.getBestGenus() + BLIP;
				try {
					rset = executeSQLQuery(co);
					rset.first();
					ii = rset.getInt("id");
					executeSQLCommand("update " + PUTEINSNAME + " set ChainId = " + latestChainID + " where id = " + ii);
					return String.valueOf(ii);
				} catch (SQLException e) {
				}
			}
			co = "select id, " + fir + ", " + las + " from " + PUTEINSNAME + " where Gene = \'Env\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP +" and Chromosome = \'" + currentChromosome + "\' and Subdirectory = \'" + subDir() + "\' order by AverageScore desc";
			StringBuffer sb = new StringBuffer();
			Stack coStack = new Stack();
			try {
				rset = executeSQLQuery(co);
				rset.first();
				for (;;) {
					ii = rset.getInt("id");
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(ii);
					coStack.push("update " + PUTEINSNAME + " set ChainId = " + latestChainID + " where id = " + ii);
					currentFirstUsedPos = Math.min(currentFirstUsedPos, currentDNA.internalize(rset.getInt(fir)));
					currentLastUsedPos = Math.max(currentLastUsedPos, currentDNA.internalize(rset.getInt(las)));
					rset.next();
				}
			} catch (SQLException e) {
			}
			for (int coi=0; coi<coStack.size(); coi++) {
				if (multiPuteins | (coi == 0)) {
					co = (String) coStack.elementAt(coi);
					try {
						executeSQLCommand(co);
					} catch (SQLException sq) {
						haltError(sq);
					}
				}
			}
			return sb.toString();
		} // end of ChainEnvPuteinColumn.vText()
		
	} // end of ChainEnvPuteinColumn


/**
* Column for 'PPTStart' column in Chains with first position in PPT, if any.
*/
	protected class ChainPPTStartColumn extends IntColumn {
	
		ChainPPTStartColumn() throws SQLException {
			columnName = "PPTStart";
		} // end of ChainPPTStartColumn.constructor()
		
		protected final String valueText() {
			if (currentPPT == null) {
				return "NULL";
			}
			return String.valueOf(currentDNA.externalize(currentPPT.FIRSTPOS));
		} // end of ChainPPTStartColumn.valueText()
		
	} // end of ChainPPTStartColumn


/**
* Column for 'PPTEnd' column in Chains with last position in PPT, if any.
*/
	protected class ChainPPTEndColumn extends IntColumn {
	
		ChainPPTEndColumn() throws SQLException {
			columnName = "PPTEnd";
		} // end of ChainPPTEndColumn.constructor()
		
		protected final String valueText() {
			if (currentPPT == null) {
				return "NULL";
			}
			return String.valueOf(currentDNA.externalize(currentPPT.LASTPOS));
		} // end of ChainPPTEndColumn.valueText()
		
	} // end of ChainPPTEndColumn


/**
* Column for 'PPTScore' column in Chains with score of PPT, if any.
*/
	protected class ChainPPTScoreColumn extends IntColumn {
	
		ChainPPTScoreColumn() throws SQLException {
			columnName = "PPTScore";
		} // end of ChainPPTScoreColumn.constructor()
		
		protected final String valueText() {
			if (currentPPT == null) {
				return "NULL";
			}
			return String.valueOf(currentPPT.SCORE);
		} // end of ChainPPTScoreColumn.valueText()
		
	} // end of ChainPPTScoreColumn


/**
* Column for 'LTR3id' column in Chains with id of 3'LTR in LTRs.
*/
	protected class ChainLTR3idColumn extends IntColumn {
	
		ChainLTR3idColumn() throws SQLException {
			columnName = "LTR3id";
		} // end of ChainLTR3idColumn.constructor()
		
/**
* Also updates InChain in relevant LTR.
*/
		protected final String valueText() throws RetroTectorException  {
			if (current3LTR == null) {
				return "NULL";
			}
			CollectGenome.LTRSummary summ = new CollectGenome.LTRSummary(current3LTR, currentDNA);
			CollectGenome.LTRSummary summ2 = (CollectGenome.LTRSummary) candTable.get(summ);
			int irs = -1;
			if (summ2 != null) {
				irs = summ2.id;
				String co = "update " + LTRSNAME + " set InChain = " + latestChainID + " where id = " + irs;
				try {
					executeSQLCommand(co);
				} catch (SQLException e) {
					haltError(e);
				}
			}
			return String.valueOf(irs);
		} // end of ChainLTR3idColumn.valueText()
		
	} // end of ChainLTR3idColumn


/**
* Column for 'ChainTSD' column in Chains with ChainGraphInfo.LTRCOMMENT.
*/
	protected class ChainTSDColumn extends VarCharColumn {
	
		ChainTSDColumn() throws SQLException {
			super(24);
			columnName = "ChainTSD";
		} // end of ChainTSDColumn.constructor()
		
		protected final String vText() {
			String s = currentChainGraphInfo.LTRCOMMENT.trim();
			if (s.indexOf("<>") > 0) {
				int ind = s.lastIndexOf("* ");
				if (ind < 0) {
					ind = s.lastIndexOf(" ");
				}
				return s.substring(ind).trim();
			} else {
				return "";
			}
		} // end of ChainTSDColumn.vText()
		
	} // end of ChainTSDColumn


/**
* Column for 'PostFlankSwept' column in Chains.
*/
	protected class ChainPostFlankSweptColumn extends SequenceColumn {
	
		ChainPostFlankSweptColumn() throws SQLException {
			columnName = "PostFlankSwept";
		} // end of ChainPostFlankSweptColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			return currentDNA.subString(Math.min(currentDNA.LENGTH - 2, currentChainGraphInfo.LASTBASEPOS + 1), Math.min(currentDNA.LENGTH - 2, currentChainGraphInfo.LASTBASEPOS + 2000), true);
		} // end of ChainPostFlankSweptColumn.vText()
		
	} // end of ChainPostFlankSweptColumn


/**
* Column for 'PostFlankRaw' column in Chains.
*/
	protected class ChainPostFlankRawColumn extends SequenceColumn {
	
		ChainPostFlankRawColumn() throws SQLException {
			columnName = "PostFlankRaw";
		} // end of ChainPostFlankRawColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			String s = currentDNA.subStringWithInserts(Math.min(currentDNA.LENGTH - 2, currentChainGraphInfo.LASTBASEPOS + 1), Math.min(currentDNA.LENGTH - 2, currentChainGraphInfo.LASTBASEPOS + 2000));
			if (s.length() <= 2000) {
				return s;
			} else {
				return s.substring(0, 2000);
			}
		} // end of ChainPostFlankRawColumn.vText()
		
	} // end of ChainPostFlankRawColumn


/**
* Column for 'LTRDivergence' column in Chains with divergence between LTRs, or NULL.
*/
	protected class ChainLTRDivergenceColumn extends FloatColumn {
	
		ChainLTRDivergenceColumn() throws SQLException {
			columnName = "LTRDivergence";
		} // end of ChainLTRDivergenceColumn.constructor()
		
		protected final String valueText() throws RetroTectorException {
			if ((current5LTR == null) | (current3LTR == null)) {
				return "NULL";
			}
			String arg1 = current5LTR.FIT;
			String arg2 = current3LTR.FIT;
			Utilities.alignBases((" " + arg1).toCharArray(), (" " + arg2).toCharArray(), -1, 20, 0, 40, 2);
			return String.valueOf(100.0f * (1.0f - Utilities.identityCount / (1.0f * (Utilities.identityCount + Utilities.nonIdentityCount))));
		} // end of ChainLTRDivergenceColumn.valueText()
		
	} // end of ChainLTRDivergenceColumn


/**
* Column for 'APerc' column in Chains with percent of a bases.
*/
	protected class ChainAPercentColumn extends IntColumn {
	
		ChainAPercentColumn() throws SQLException {
			columnName = "APerc";
		} // end of ChainAPercentColumn.constructor()
		
		protected final String valueText() {
			String s = currentDNA.subString(currentChainGraphInfo.FIRSTBASEPOS, currentChainGraphInfo.LASTBASEPOS, true).toLowerCase();
			int acount = Utilities.charCount(s, "a");
			return String.valueOf(Math.round((100.0f * acount) / s.length()));
		} // end of ChainAPercentColumn.valueText()
		
	} // end of ChainAPercentColumn


/**
* Column for 'CPerc' column in Chains with percent of c bases.
*/
	protected class ChainCPercentColumn extends IntColumn {
	
		ChainCPercentColumn() throws SQLException {
			columnName = "CPerc";
		} // end of ChainCPercentColumn.constructor()
		
		protected final String valueText() {
			String s = currentDNA.subString(currentChainGraphInfo.FIRSTBASEPOS, currentChainGraphInfo.LASTBASEPOS, true).toLowerCase();
			int ccount = Utilities.charCount(s, "c");
			return String.valueOf(Math.round((100.0f * ccount) / s.length()));
		} // end of ChainCPercentColumn.valueText()
		
	} // end of ChainCPercentColumn


/**
* Column for 'GPerc' column in Chains with percent of g bases.
*/
	protected class ChainGPercentColumn extends IntColumn {
	
		ChainGPercentColumn() throws SQLException {
			columnName = "GPerc";
		} // end of ChainGPercentColumn.constructor()
		
		protected final String valueText() {
			String s = currentDNA.subString(currentChainGraphInfo.FIRSTBASEPOS, currentChainGraphInfo.LASTBASEPOS, true).toLowerCase();
			int gcount = Utilities.charCount(s, "g");
			return String.valueOf(Math.round((100.0f * gcount) / s.length()));
		} // end of ChainGPercentColumn.valueText()
		
	} // end of ChainGPercentColumn


/**
* Column for 'TPerc' column in Chains with percent of t bases.
*/
	protected class ChainTPercentColumn extends IntColumn {
	
		ChainTPercentColumn() throws SQLException {
			columnName = "TPerc";
		} // end of ChainTPercentColumn.constructor()
		
		protected final String valueText() {
			String s = currentDNA.subString(currentChainGraphInfo.FIRSTBASEPOS, currentChainGraphInfo.LASTBASEPOS, true).toLowerCase();
			int tcount = Utilities.charCount(s, "t");
			return String.valueOf(Math.round((100.0f * tcount) / s.length()));
		} // end of ChainTPercentColumn.valueText()
		
	} // end of ChainTPercentColumn


/**
* Column for 'RepBaseFinds' column in Chains, with similarities to sequences in repbasetemplates.
*/
	protected class ChainRepBaseFindsColumn extends TextColumn {
	
		ChainRepBaseFindsColumn() throws SQLException {
			columnName = "RepBaseFinds";
		} // end of ChainRepBaseFindsColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			String key = currentChainName.substring("Chain".length()) + "RepBaseFinds";
			Object o = currentChainTable.get(key);
			if (o != null) {
				String[ ] ss = (String[ ]) o;
				if (ss.length > 0) {
					String s = Utilities.collapseLines(ss);
					return s;
				} else {
					return "";
				}
			}
			
			if (currentChainGraphInfo.SCORE < minProcessScore) {
				return "";
			}
			return Utilities.repBaseFind(currentDNA, currentChainGraphInfo.FIRSTBASEPOS, currentChainGraphInfo.LASTBASEPOS, repBaseTemplates, false);
		} // end of ChainRepBaseFindsColumn.vText()
		
	} // end of ChainRepBaseFindsColumn


/**
* Column for 'PolClass' column with name and score of most similar pol i polproteins.
*/
	protected class ChainPolClassColumn extends TextColumn {
	
/**
* Creates TEXT column.
*/
		ChainPolClassColumn() throws SQLException {
			columnName = "PolClass";
		} // end of ChainPolClassColumn.constructor()
		
/**
* @return	Name and score.
*/
		protected final String vText() throws RetroTectorException {
			String putein;
			String co = "select PuteinComment from " + PUTEINSNAME + " where Chromosome = " + BLIP + currentChromosome + BLIP + " and Gene = \'Pol\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP + " and Subdirectory = \'" + subDir() + "\' order by AverageScore desc";
			try {
				ResultSet rs = executeSQLQuery(co);
				rs.next();
				putein = rs.getString("PuteinComment");
				if ((putein != null) && (putein.length() > 0)) {
					return putein;
				}
			} catch (SQLException e) {
			}

			if (currentChainGraphInfo.SCORE < minProcessScore) {
				return "";
			}
			co = "select Putein from " + PUTEINSNAME + " where Chromosome = " + BLIP + currentChromosome + BLIP + " and Gene = \'Pol\' and Chainname = " + BLIP + currentChainName.substring("Chain".length()) + BLIP + " and Subdirectory = \'" + subDir() + "\' order by AverageScore desc";
			String protein;
			try {
				ResultSet rs = executeSQLQuery(co);
				rs.next();
				putein = rs.getString("Putein");
				return Utilities.findBestProtein(putein, polProteins);
			} catch (SQLException e) {
			}
			return "";
		} // end of ChainPolClassColumn.vText()
		
	} // end of ChainPolClassColumn


/**
* Column for 'BestRefRV' column with name of most similar RV i refrvs.
*/
	protected class ChainBestRefRVColumn extends TextColumn {
	
/**
* Creates TEXT column.
*/
		ChainBestRefRVColumn() throws SQLException {
			columnName = "BestRefRV";
		} // end of ChainBestRefRVColumn.constructor()
		
/**
* @return	Name and score.
*/
		protected final String vText() throws RetroTectorException {
			String key = currentChainName.substring("Chain".length()) + "BestRefRV";
			Object o = currentChainTable.get(key);
			if (o != null) {
				String s = (String) o;
				return s;
			}
			
			if (currentChainGraphInfo.SCORE < minProcessScore) {
				return "";
			}
			return Utilities.findBestRV(currentDNA, currentChainGraphInfo.FIRSTBASEPOS, currentChainGraphInfo.LASTBASEPOS, refRVs);
		} // end of ChainBestRefRVColumn.vText()
		
	} // end of ChainBestRefRVColumn


/**
* Column for 'ChainInserts' column in Chains.
*/
	protected class ChainInsertsColumn extends TextColumn {
	
		ChainInsertsColumn() throws SQLException {
			columnName = "ChainInserts";
		} // end of ChainInsertsColumn.constructor()
		
		protected final String vText() throws RetroTectorException {
			return currentDNA.TRANSLATOR.toString(currentDNA.externalize(currentFirstUsedPos), currentDNA.externalize(currentLastUsedPos));
		} // end of ChainInsertsColumn.vText()
		
	} // end of ChainInsertsColumn


/**
* Column for 'Overlapper' column in Chains, with id of an earlier chain which overlaps
* the core region of this, if any.
*/
	protected class ChainOverlapperColumn extends VarCharColumn {
	
		ChainOverlapperColumn() throws SQLException {
			super(15);
			columnName = "Overlapper";
		} // end of ChainOverlapperColumn.constructor()
		
		protected final String vText() {
			if (currentChainOverlapper == null) {
				return "NULL";
			} else {
				return currentChainOverlapper;
			}
		} // end of ChainOverlapperColumn.valueText()
		
	} // end of ChainOverlapperColumn


/**
* Column for 'CoreFirst' column in Chains with externalized ChainGraphInfo.firstInCore().
*/
	protected class ChainCoreFirstColumn extends IntColumn {
	
		ChainCoreFirstColumn() throws SQLException {
			columnName = "CoreFirst";
		} // end of ChainCoreFirstColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentDNA.externalize(currentChainGraphInfo.firstInCore()));
		} // end of ChainCoreFirstColumn.valueText()
		
	} // end of ChainCoreFirstColumn


/**
* Column for 'CoreLast' column in Chains with externalized ChainGraphInfo.lastInCore().
*/
	protected class ChainCoreLastColumn extends IntColumn {
	
		ChainCoreLastColumn() throws SQLException {
			columnName = "CoreLast";
		} // end of ChainCoreLastColumn.constructor()
		
		protected final String valueText() {
			return String.valueOf(currentDNA.externalize(currentChainGraphInfo.lastInCore()));
		} // end of ChainCoreLastColumn.valueText()
		
	} // end of ChainCoreLastColumn


/**
* Column for 'ChainsFileName' column in Chains with name of containing file.
*/
	protected class ChainsFileNameColumn extends VarCharColumn {
	
		ChainsFileNameColumn() throws SQLException {
			super(30);
			columnName = "ChainsFileName";
		} // end of ChainsFileNameColumn.constructor()
		
		protected final String vText() {
			return currentChainsFile.getName();
		} // end of ChainsFileNameColumn.vText()
		
	} // end of ChainsFileNameColumn
	

/**
* Column for 'ChainName' column in Chains with name of chain.
*/
	protected class ChainNameColumn extends CharacterColumn {
	
		ChainNameColumn() throws SQLException {
			super(6);
			columnName = "ChainName";
		} // end of ChainNameColumn.constructor()
		
		protected final String vText() {
			return currentChainName.substring("Chain".length());
		} // end of ChainNameColumn.vText()
		
	} // end of ChainNameColumn


/**
* Column for 'PseudoGene' column in Chains with contents of Pseudogene file, if any.
*/
	protected class ChainPseudoGeneColumn extends TextColumn {
	
/**
* Creates TEXT column.
*/
		ChainPseudoGeneColumn() throws SQLException {
			super();
			columnName = "PseudoGene";
		} // end of ChainPseudoGeneColumn.constructor()
		
/**
* @return	Subdirectory name.
*/
		protected final String vText() {
			File psFile = new File(currentSubdirectory, "Pseudogene_" + currentChainName.substring("Chain".length()) + "_001.txt");
			if (psFile.exists()) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(psFile));
					StringBuffer sb = new StringBuffer();
					String st;
					while ((st = br.readLine()) != null) {
						sb.append(st);
						sb.append("\n");
					}
					br.close();
					return sb.toString();
				} catch (IOException ioe) {
				}
			}
			return "";
		} // end of ChainPseudoGeneColumn.vText()
		
	} // end of ChainPseudoGeneColumn

	
/**
* = "mysql".
*/
	public static final String MYSQL = "mysql";

/**
* = 'SQLDatabase'.
*/
	public static final String SQLDATABASEKEY = "SQLDatabase";

/**
* = 'User'.
*/
	public static final String USERKEY = "User";

/**
* = 'Password'.
*/
	public static final String PASSWORDKEY = "Password";

/**
* = 'SubProtocol'.
*/
	public static final String SUBPROTOCOLKEY = "SubProtocol";

/**
* = 'JDBCDriver'.
*/
	public static final String DRIVERKEY = "JDBCDriver";
	
/**
* = 'com.mysql.jdbc.Driver'.
*/
	public static final String DEFAULTJDBCDRIVER = "com.mysql.jdbc.Driver";

/**
* = 'Host'.
*/
	public static final String HOSTKEY = "Host";

/**
* = 'Chromosome'.
*/
	public static final String CHROMOSOME = "Chromosome";

/**
* = 'Chromosomes'.
*/
	public static final String CHROMOSOMESNAME = "Chromosomes";

/**
* = 'Chains'.
*/
	public static final String CHAINSNAME = "Chains";

/**
* = 'LTRs'.
*/
	public static final String LTRSNAME = "LTRs";

/**
* = 'Puteins'.
*/
	public static final String PUTEINSNAME = "Puteins";

/**
* Longest acceptable chromosome identifier.
*/
	public static final int MAXCHROMOSOMELENGTH = 50;

/**
* Pixels per character horizontally.
*/
	public static final int PIXELSPERCHAR = 8;
	
/**
* Contents of forretrotector.repbasetemplates.
*/
	protected static Utilities.TemplatePackage[ ] repBaseTemplates;

/**
* Contents of forretrotector.refrfs.
*/
	protected static Utilities.TemplatePackage[ ] refRVs;

/**
* Contents of forretrotector.polproteins. Each element consist of
* sequence and short name.
*/
	protected static String[ ][ ] polProteins;

/**
* True if putein is actually EnvTrace.
*/
	protected boolean isEnvTrace;


/**
* Chains with score lower than this are incompletely processed.
*/
	protected int minProcessScore = 250;

/**
* The URL to use.
*/
	protected static String url;
	
/**
* The Connection to the 'forretrotector' database.
*/
	protected static Connection forretrotectorConnection = null;

/**
* Statement for execution of commands to the 'forretrotector' database.
*/
	protected static Statement forretrotectorStatement;
	
/**
* Hashtable with instances of all Columns, indexed by columnName.
*/
	protected Hashtable columnTable;

/**
* Hashtable with instances of all columnNames, indexed by class name.
*/
	protected Hashtable columnNameTable;

/**
* Columns in LTRs.
*/
	protected Column[ ] ltrColumns;

/**
* Columns in Puteins.
*/
	protected Column[ ] puteinColumns;

/**
* Columns in Chains.
*/
	protected	Column[ ] chainColumns;

/**
* Columns in Chromosomes.
*/
	protected	Column[ ] chromosomeColumns;

	private static boolean sqlStarted = false;;

	private static String user;
	private static String password;
	private static String subprotocol;
	private static String host;
	private static String jdbcDriverName;
	protected static boolean debugging;
	
/**
* True if the database in use already existed.
*/
	protected boolean preexisted;

/**
* The normal Connection opened by this.
*/
	protected Connection mainConnection = null;

/**
* The MetaData of mainConnection. Used to get type info.
*/
	protected DatabaseMetaData mainConnectionMetaData;

/**
* Info about which data types are supported.
*/
	protected ResultSet mainConnectionTypeInfo = null;

/**
* Statement for execution of commands without result, or whose
* ResultSet will not be needed for long.
*/
	protected Statement mainStatement;
	
/**
* To keep track of stored LTRs.
*/
	protected Hashtable candTable = new Hashtable();

	
/**
* @param		The name of a subclass of Column.
* @return The value of columnName in that class, or null.
*/
	public final String getColumnTitle(String columnClass) {
		return (String) columnNameTable.get("SQLUser$" + columnClass);
	} // end of getColumnTitle(String)
	
/**
* Runs this if it has not been done before.
* @return	True if successful.
*/
	protected static boolean startSQL() throws RetroTectorException {
		if (!sqlStarted) {
			SQLUser sql = new SQLUser();
			sql.initialize(null);
			if (sql.runFlag) {
				sql.execute();
				return true;
			} else {
				return false;
			}
		}
		return false;
	} // end of startSQL(
	
/**
* @return	The subprotocol in use.
*/
	protected final static String getSubProtocol() {
		return subprotocol;
	} // end of getSubProtocol()

/**
* Enters column in columnTable and columnNameTable.
*/
	private final void toColumnTable(Object o) throws RetroTectorException {
		Column col = (Column) o;
		String s = Utilities.className(col);
		Object oo = columnTable.get(col.columnName);
		if ((oo != null) && (!s.equals(Utilities.className(oo)))) {
			haltError("Duplicate column name: " + col.columnName);
		}
		columnTable.put(col.columnName, col);
		columnNameTable.put(s, col.columnName);
	} // end of toColumnTable(Object)
	
/**
* Opens a new mainConnection and performs some Connection-dependent operations.
* @return	True if successful.
*/
	protected final boolean connect() throws RetroTectorException {
		startSQL();

		try {
			if (mainConnection != null) {
				mainConnection.close();
			}
			mainConnection = DriverManager.getConnection(url, user, password);
			mainConnectionMetaData = mainConnection.getMetaData();
			mainConnectionTypeInfo = mainConnectionMetaData.getTypeInfo();
			mainStatement = getNewStatement();

			columnTable = new Hashtable();
			columnNameTable = new Hashtable();
			
	// Make LTRs columns
			Stack ltrfStack = new Stack();
			try {
				toColumnTable(ltrfStack.push(new LTRScoreFactorColumn()));
				toColumnTable(ltrfStack.push(new LTRGenusColumn()));
				toColumnTable(ltrfStack.push(new ChromosomeColumn()));
				toColumnTable(ltrfStack.push(new StrandColumn()));
				toColumnTable(ltrfStack.push(new LTRSimilarityStartPosColumn()));
				toColumnTable(ltrfStack.push(new LTRFirstPosColumn()));
				toColumnTable(ltrfStack.push(new LTRLastPosColumn()));
				toColumnTable(ltrfStack.push(new LTRSimilarityEndPosColumn()));
				toColumnTable(ltrfStack.push(new LTRHotspotPosColumn()));
				toColumnTable(ltrfStack.push(new LTRHotspotColumn()));
				toColumnTable(ltrfStack.push(new LTRInChainColumn()));
				toColumnTable(ltrfStack.push(new LTRPairedHotspotColumn()));
				toColumnTable(ltrfStack.push(new LTRSequenceColumn()));
				toColumnTable(ltrfStack.push(new LTRRepBaseFindsColumn()));
				toColumnTable(ltrfStack.push(new LTRInsertsColumn()));
				toColumnTable(ltrfStack.push(new LTRTSDColumn()));
				toColumnTable(ltrfStack.push(new LTRU5NNScoreColumn()));
				toColumnTable(ltrfStack.push(new LTRU5NNPosColumn()));
				toColumnTable(ltrfStack.push(new LTRGTModifierScoreColumn()));
				toColumnTable(ltrfStack.push(new LTRU3NNScoreColumn()));
				toColumnTable(ltrfStack.push(new LTRU3NNPosColumn()));
				toColumnTable(ltrfStack.push(new LTRTATAAScoreColumn()));
				toColumnTable(ltrfStack.push(new LTRTATAAPosColumn()));
				toColumnTable(ltrfStack.push(new LTRMEME50ScoreColumn()));
				toColumnTable(ltrfStack.push(new LTRMEME50PosColumn()));
				toColumnTable(ltrfStack.push(new LTRMotifs1ScoreColumn()));
				toColumnTable(ltrfStack.push(new LTRMotifs1PosColumn()));
				toColumnTable(ltrfStack.push(new LTRMotifs2ScoreColumn()));
				toColumnTable(ltrfStack.push(new LTRMotifs2PosColumn()));
				toColumnTable(ltrfStack.push(new LTRTranssitesScoreColumn()));
				toColumnTable(ltrfStack.push(new LTRCpGModifierScoreColumn()));
				toColumnTable(ltrfStack.push(new LTRSpl8ModifierScoreColumn()));
				toColumnTable(ltrfStack.push(new CommentColumn()));
				toColumnTable(ltrfStack.push(new SubDirectoryColumn()));
				toColumnTable(ltrfStack.push(new DNAFileColumn()));
				toColumnTable(ltrfStack.push(new LTRFileNameColumn()));
			} catch (SQLException e) {
				haltError(e);
			}
			ltrColumns = new Column[ltrfStack.size()];
			ltrfStack.copyInto(ltrColumns);
			
	// Make Puteins columns
			Stack puteinfStack = new Stack();
			try {
				toColumnTable(puteinfStack.push(new PuteinGeneColumn()));
				toColumnTable(puteinfStack.push(new PuteinGenusColumn()));
				toColumnTable(puteinfStack.push(new ChromosomeColumn()));
				toColumnTable(puteinfStack.push(new StrandColumn()));
				toColumnTable(puteinfStack.push(new PuteinEstimatedFirstColumn()));
				toColumnTable(puteinfStack.push(new PuteinEstimatedLastColumn()));
				toColumnTable(puteinfStack.push(new PuteinColumn()));
				toColumnTable(puteinfStack.push(new PuteinStopCodonsColumn()));
				toColumnTable(puteinfStack.push(new PuteinShiftsColumn()));
				toColumnTable(puteinfStack.push(new PuteinLongestORFColumn()));
				toColumnTable(puteinfStack.push(new PuteinLongestORFPosColumn()));
				toColumnTable(puteinfStack.push(new PuteinChainIdColumn()));
				toColumnTable(puteinfStack.push(new PuteinLengthColumn()));
				toColumnTable(puteinfStack.push(new PuteinAlignedAcidsColumn()));
				toColumnTable(puteinfStack.push(new PuteinAverageScoreColumn()));
				toColumnTable(puteinfStack.push(new PuteinMostUsedRowColumn()));
				toColumnTable(puteinfStack.push(new PuteinLongestRunLengthColumn()));
				toColumnTable(puteinfStack.push(new PuteinLongestRunPosColumn()));
				toColumnTable(puteinfStack.push(new PuteinSequenceColumn()));
				toColumnTable(puteinfStack.push(new PuteinCodonSequenceColumn()));
				toColumnTable(puteinfStack.push(new PuteinFileColumn()));
				toColumnTable(puteinfStack.push(new PuteinDuplicatorColumn()));
				toColumnTable(puteinfStack.push(new PuteinChainnameColumn()));
				toColumnTable(puteinfStack.push(new PuteinCommentColumn()));
				toColumnTable(puteinfStack.push(new SubDirectoryColumn()));
				toColumnTable(puteinfStack.push(new DNAFileColumn()));
			} catch (SQLException e) {
				haltError(e);
			}
			puteinColumns = new Column[puteinfStack.size()];
			puteinfStack.copyInto(puteinColumns);
			
	// Make Chains columns
			Stack chainfStack = new Stack();
			try {
				toColumnTable(chainfStack.push(new ChainIdColumn()));
				toColumnTable(chainfStack.push(new ChainInfoColumn()));
				toColumnTable(chainfStack.push(new ChainSequenceColumn()));
				toColumnTable(chainfStack.push(new ChainNameColumn()));
				toColumnTable(chainfStack.push(new ChromosomeColumn()));
				toColumnTable(chainfStack.push(new StrandColumn()));
				toColumnTable(chainfStack.push(new ChainScoreColumn()));
				toColumnTable(chainfStack.push(new ChainGenusColumn()));
				toColumnTable(chainfStack.push(new ChainStartColumn()));
				toColumnTable(chainfStack.push(new ChainEndColumn()));
				toColumnTable(chainfStack.push(new ChainSubGenesColumn()));
				toColumnTable(chainfStack.push(new ChainBreaksColumn()));
				toColumnTable(chainfStack.push(new ChainLTR5idColumn()));
				toColumnTable(chainfStack.push(new ChainPreFlankRawColumn()));
				toColumnTable(chainfStack.push(new ChainPreFlankSweptColumn()));
				toColumnTable(chainfStack.push(new ChainPBSStartColumn()));
				toColumnTable(chainfStack.push(new ChainPBSEndColumn()));
				toColumnTable(chainfStack.push(new ChainPBSScoreColumn()));
				toColumnTable(chainfStack.push(new ChainPBSTypeColumn()));
				toColumnTable(chainfStack.push(new ChainGagPuteinColumn()));
				toColumnTable(chainfStack.push(new ChainProPuteinColumn()));
				toColumnTable(chainfStack.push(new ChainPolPuteinColumn()));
				toColumnTable(chainfStack.push(new ChainEnvPuteinColumn()));
				toColumnTable(chainfStack.push(new ChainPPTStartColumn()));
				toColumnTable(chainfStack.push(new ChainPPTEndColumn()));
				toColumnTable(chainfStack.push(new ChainPPTScoreColumn()));
				toColumnTable(chainfStack.push(new ChainLTR3idColumn()));
				toColumnTable(chainfStack.push(new ChainTSDColumn()));
				toColumnTable(chainfStack.push(new ChainPostFlankRawColumn()));
				toColumnTable(chainfStack.push(new ChainPostFlankSweptColumn()));
				toColumnTable(chainfStack.push(new ChainLTRDivergenceColumn()));
				toColumnTable(chainfStack.push(new ChainAPercentColumn()));
				toColumnTable(chainfStack.push(new ChainTPercentColumn()));
				toColumnTable(chainfStack.push(new ChainCPercentColumn()));
				toColumnTable(chainfStack.push(new ChainGPercentColumn()));
				toColumnTable(chainfStack.push(new ChainRepBaseFindsColumn()));
				toColumnTable(chainfStack.push(new ChainPolClassColumn()));
				toColumnTable(chainfStack.push(new ChainBestRefRVColumn()));
				toColumnTable(chainfStack.push(new ChainInsertsColumn()));
				toColumnTable(chainfStack.push(new ChainOverlapperColumn()));
				toColumnTable(chainfStack.push(new ChainCoreFirstColumn()));
				toColumnTable(chainfStack.push(new ChainCoreLastColumn()));
				toColumnTable(chainfStack.push(new CommentColumn()));
				toColumnTable(chainfStack.push(new SubDirectoryColumn()));
				toColumnTable(chainfStack.push(new DNAFileColumn()));
				toColumnTable(chainfStack.push(new ChainsFileNameColumn()));
				toColumnTable(chainfStack.push(new ChainPseudoGeneColumn()));
			} catch (SQLException e) {
				haltError(e);
			}
			chainColumns = new Column[chainfStack.size()];
			chainfStack.copyInto(chainColumns);
			
	// Make Chromosomes columns
			Stack chrfStack = new Stack();
			try {
				toColumnTable(chrfStack.push(new ChromosomeColumn()));
				toColumnTable(chrfStack.push(new ChromosomeLengthColumn()));
				toColumnTable(chrfStack.push(new DirectoryColumn()));
				toColumnTable(chrfStack.push(new LongCommentColumn()));
				toColumnTable(chrfStack.push(new FinishedAtColumn()));
				toColumnTable(chrfStack.push(new RTVersionColumn()));
			} catch (SQLException e) {
				haltError(e);
			}
			chromosomeColumns = new Column[chrfStack.size()];
			chrfStack.copyInto(chromosomeColumns);
			
		} catch (SQLException e) {
			haltError(e);
		}
		
		try {
// collect forretrotector.repbasetemplates
			Stack sts = new Stack();
			Utilities.TemplatePackage template;
			String cl;
			String repsh;
			String seq;
			ResultSet rs = forretrotectorStatement.executeQuery("select seq, repshort, class from repbasetemplates");
			while (rs.next()) {
				repsh = rs.getString("repshort");
				seq = rs.getString("seq");
				cl = "";
				try {
					cl = rs.getString("class");
				} catch (SQLException sqle) {
				}
				template = new Utilities.TemplatePackage(repsh, seq, 11, false, cl);
				sts.push(template);
				template = new Utilities.TemplatePackage(repsh + "<", seq, 11, true, cl);
				sts.push(template);
			}
			rs.close();
			repBaseTemplates = new Utilities.TemplatePackage[sts.size()];
			sts.copyInto(repBaseTemplates);
			
// collect forretrotector.polproteins
			String[ ] s2;
			rs = forretrotectorStatement.executeQuery("select sequence, name from polproteins");
			sts = new Stack();
			while (rs.next()) {
				s2 = new String[2];
				s2[0] = rs.getString("sequence");
				s2[1] = rs.getString("name");
				sts.push(s2);
			}
			rs.close();
			polProteins = new String[sts.size()][ ];
			sts.copyInto(polProteins);

// collect forretrotector.refrvs
			sts = new Stack();
			String def;
			rs = forretrotectorStatement.executeQuery("select sequence, definition from refrvs");
			while (rs.next()) {
				seq = rs.getString("sequence");
				def = rs.getString("definition");
				template = new Utilities.TemplatePackage(def, seq, 11, false, null);
				sts.push(template);
			}
			rs.close();
			refRVs = new Utilities.TemplatePackage[sts.size()];
			sts.copyInto(refRVs);

			return true;
		} catch (SQLException se) {
			haltError(se);
		}
		return false;
	} // end of connect()

/**
* Breaks current Connection.
*/
	protected final void disconnect()  throws SQLException {
		mainConnection.close();
		mainStatement.close();
	} // end of disconnect()

/**
* @return A new Statement.
*/
	protected final Statement getNewStatement() throws SQLException {
			return mainConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	} // end of getNewStatement()
	
/**
* Executes a non-resultgenerating SQL command. Outputs the command if debugging.
* @param	st	The Statement to execute the command.
* @param	com	The command to execute.
*/
	protected final void executeSQLCommand(Statement st, String com) throws SQLException {
		if (debugging) {
			System.out.println(com);
			System.out.println();
		}
		st.execute(com);
	} // end of executeSQLCommand(Statement, String)
	
/**
* Executes a non-resultgenerating SQL command in mainStatement. Outputs the command if debugging.
* @param	com	The command to execute.
*/
	protected final void executeSQLCommand(String com) throws SQLException {
		executeSQLCommand(mainStatement, com);
	} // end of executeSQLCommand(String)

/**
* Executes a non-resultgenerating SQL command. Outputs the command if debugging.
* @param	st	The Statement to execute the command.
* @param	sb	A StringBuffer containing the command to execute.
*/
	protected final void executeSQLCommand(Statement st, StringBuffer sb) throws SQLException {
		executeSQLCommand(st, sb.toString());
	} // end of executeSQLCommand(Statement, StringBuffer)

/**
* Executes a non-resultgenerating SQL command in mainStatement. Outputs the command if debugging.
* @param	sb	A StringBuffer containing the command to execute.
*/
	protected final void executeSQLCommand(StringBuffer sb) throws SQLException {
		executeSQLCommand(sb.toString());
	} // end of executeSQLCommand(StringBuffer)

/**
* Executes a resultgenerating SQL command. Outputs the command if debugging.
* @param	st	The Statement to execute the command.
* @param	com	The command to execute.
* @return	The result.
*/
	protected final ResultSet executeSQLQuery(Statement st, String com) throws SQLException {
		if (debugging) {
			System.out.println(com);
			System.out.println();
		}
		return st.executeQuery(com);
	} // end of executeSQLQuery(Statement, String)

/**
* Executes a resultgenerating SQL command in mainStatement. Outputs the command if debugging.
* @param	com	The command to execute.
* @return	The result.
*/
	protected final ResultSet executeSQLQuery(String com) throws SQLException {
		return executeSQLQuery(mainStatement, com);
	} // end of executeSQLQuery(String)

/**
* Executes a resultgenerating SQL command. Outputs the command if debugging.
* @param	st	The Statement to execute the command.
* @param	sb	A StringBuffer containing the command to execute.
* @return	The result.
*/
	protected final ResultSet executeSQLQuery(Statement st, StringBuffer sb) throws SQLException {
		return executeSQLQuery(st, sb.toString());
	} // end of executeSQLQuery(Statement, StringBuffer)

/**
* Executes a resultgenerating SQL command in mainStatement. Outputs the command if debugging.
* @param	sb	A StringBuffer containing the command to execute.
* @return	The result.
*/
	protected final ResultSet executeSQLQuery(StringBuffer sb) throws SQLException {
		return executeSQLQuery(sb.toString());
	} // end of executeSQLQuery(StringBuffer)

/**
* Opens a database for use.
* @param	databaseName	The name of it.
* @return	True if the database has the properties of a genome database.
*/
	protected final boolean setDatabase(String databaseName) throws RetroTectorException {
		if ((databaseName == null) || (databaseName.length() == 0)) {
			haltError("No database name specified");
		}
		preexisted = true;
		try {
			executeSQLCommand("create database " + databaseName);
			preexisted = false;
		} catch (SQLException e) {
		}
		try {
			executeSQLCommand("use " + databaseName);
			return isDatabaseGenome();
		} catch (SQLException e) {
			haltError(e);
		}
		return true;
	} // end of setDatabase(String)

/**
* @param	ty	An int according to java.sql.Types.
* @return	The name associated with that type, or null.
*/
	protected final String typeNameOf(int ty) throws SQLException {
		int t;
		mainConnectionTypeInfo.beforeFirst();
		while (mainConnectionTypeInfo.next()) {
			t = mainConnectionTypeInfo.getInt(2);
			if (t == ty) {
				return mainConnectionTypeInfo.getString(1);
			}
		}
		return null;
	} // end of typeNameOf(int)
	
/**
* @return	True if the database in use has the properties of a genome database.
*/
	protected final boolean isDatabaseGenome() throws SQLException {
		ResultSet rs = executeSQLQuery("show tables");
		Stack st = new Stack();
		while (rs.next()) {
			st.push(rs.getString(1));
		}
		if (st.indexOf(CHROMOSOMESNAME) < 0) {
			return false;
		}
		if (st.indexOf(CHAINSNAME) < 0) {
			return false;
		}
		if (st.indexOf(PUTEINSNAME) < 0) {
			return false;
		}
		if (st.indexOf(LTRSNAME) < 0) {
			return false;
		}
		return true;
	} // end of isDatabaseGenome()
	

/**
* Standard Executor constructor.
*/
	public SQLUser() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(USERKEY, "");
		explanations.put(USERKEY, "Your user name");
		orderedkeys.push(USERKEY);
		parameters.put(PASSWORDKEY, "");
		explanations.put(PASSWORDKEY, "Your password");
		orderedkeys.push(PASSWORDKEY);
		parameters.put(SUBPROTOCOLKEY, MYSQL);
		explanations.put(SUBPROTOCOLKEY, "The SQL subprotocol to use");
		orderedkeys.push(SUBPROTOCOLKEY);
		parameters.put(HOSTKEY, "localhost");
		explanations.put(HOSTKEY, "The host to access");
		orderedkeys.push(HOSTKEY);
		parameters.put(DRIVERKEY, DEFAULTJDBCDRIVER);
		explanations.put(DRIVERKEY, "The JDBC driver to use");
		orderedkeys.push(DRIVERKEY);
		parameters.put(DEBUGGINGKEY, NO);
		explanations.put(DEBUGGINGKEY, "Yes to debug");
		orderedkeys.push(DEBUGGINGKEY);
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2008 04 27";
  } // end of version()
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		password = getString(PASSWORDKEY, "");
		user = getString(USERKEY, "");
		subprotocol = getString(SUBPROTOCOLKEY, MYSQL);
		host = getString(HOSTKEY, "localhost");
		jdbcDriverName = getString(DRIVERKEY, DEFAULTJDBCDRIVER);
		debugging = getString(DEBUGGINGKEY, NO).equals(YES);
		if (jdbcDriverName.length() > 0) {
			System.setProperty("jdbc.drivers", jdbcDriverName);
		}
		url = "jdbc:" + subprotocol + "://" + host + "/";
		System.out.println("url=" + url);
		System.out.println("jdbc.drivers=" + System.getProperty("jdbc.drivers"));
		try {
			if (forretrotectorConnection != null) {
				forretrotectorConnection.close();
			}
			forretrotectorConnection = DriverManager.getConnection(url, user, password);
			forretrotectorStatement = forretrotectorConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			forretrotectorStatement.execute("use forretrotector");
		} catch (SQLException e) {
			haltError(e);
		}
		
		sqlStarted = true;
		return "";
	} // end of execute()
	
}
