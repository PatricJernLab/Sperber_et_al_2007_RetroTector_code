/*
* Copyright (©) 2000-2009, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0.2
*
* @author  G. Sperber
* @version 15/1 -10
* Beautified 15/1 -10
*/
package builtins;

import retrotector.*;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
* Executor to view genome contents and individual Chains.
* There are no parameters.
*/
public class Genomeview  extends SQLUser {

/**
* A TableModel built on a ResultSet.
*/
	public class SQLTableModel extends AbstractTableModel {
	
/**
* The scrollable ResultSet providing the data.
*/
		public final ResultSet RS;

/**
* The number of rows in RS.
*/
		public final int NROFROWS;

/**
* The number of columns in RS.
*/
		public final int NROFCOLUMNS;

/**
* The ResultSetMetaData from RS.
*/
		public final ResultSetMetaData METADATA;
		
/**
* Constructor.
* @param	rs	A scrollable ResultSet -> RS.
*/
		public SQLTableModel(ResultSet rs) throws SQLException {
			RS = rs;
			METADATA = RS.getMetaData();
			NROFCOLUMNS = METADATA.getColumnCount();
			RS.last();
			NROFROWS = RS.getRow();
		} // end of SQLTableModel.constructor(ResultSet)
		
/**
* As required by AbstractTableModel.
*/
		public final int getColumnCount() {
			return NROFCOLUMNS;
		} // end of SQLTableModel.getColumnCount()
		
/**
* As required by AbstractTableModel.
*/
		public final int getRowCount() {
			return NROFROWS;
		} // end of SQLTableModel.getRowCount()
		
/**
* As required by AbstractTableModel.
*/
		public final Object getValueAt(int rowIndex, int columnIndex) {
			try {
				RS.absolute(rowIndex + 1);
				return RS.getObject(columnIndex + 1);
			} catch (SQLException e) {
			}
			return null;
		} // end of SQLTableModel.getValueAt(int, int)
		
/**
* Overrides AbstractTableModel.getColumnName(int).
* @param	column	A column index (0-based).
* @return	Its name.
*/
		public final String getColumnName(int column) {
			String s = null;
			try {
				s = METADATA.getColumnName(column + 1);
			} catch (SQLException e) {
			}
			return s;
		} // end of SQLTableModel.getColumnName(int)

	} // end of SQLTableModel
	

// since the default TableHeaderRenderer behaves strangely in some cases
// no longer in use
	private static class SQLTableHeaderRenderer extends JLabel implements TableCellRenderer{
		
/**
* See TableCellRenderer API.
*/
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setText((String) value);
			setBorder(BorderFactory.createLineBorder(Color.black, 1));
			setHorizontalAlignment(SwingConstants.CENTER);
			return(this);
		}
	} // end of SQLTableHeaderRenderer
	

/**
* A JTable using SQLTableModel.
*/
	public class SQLTable extends JTable implements MouseListener {
	
/**
* To change SQLTableModel thread-safely.
*/
		private class ModelSetter implements Runnable {
		
			private SQLTableModel model;
		
			private ModelSetter(SQLTableModel mode) {
				model = mode;
			} // end of SQLTable.ModelSetter.constructor(SQLTableModel)
		
/**
* As required by Runnable.
*/
			public void run() {
				setModel(model);
				setAutoResizeMode(AUTO_RESIZE_OFF);
				TableColumnModel colmod = getColumnModel();
				TableColumn tabcol;
				Column col;
				for (int c=0; c<colmod.getColumnCount(); c++) {
					tabcol = colmod.getColumn(c);
					col = (Column) columnTable.get(getColumnName(c));
					if (col != null) {
						tabcol.setPreferredWidth(col.columnWidth());
					}
				}
				setGridColor(Color.black);
				header = getTableHeader();
				header.removeMouseListener(SQLTable.this); // to avoid duplication
				header.addMouseListener(SQLTable.this);
				listSelectionModel = getSelectionModel();
				currentModel = model;
			} // end of SQLTable.ModelSetter.run()
			
		} // end of SQLTable.ModelSetter


/**
* ListSelectionModel of this table.
*/
		private ListSelectionModel listSelectionModel = null;
		
/**
* First, more constant part of underlying SQL statement.
*/
		private String mainCommand = "";

/**
* Second, optional part of underlying SQL statement.
*/
		private String commandExtension = "";
		
/**
* Command channel.
*/
		private Statement tableStatement = null;
		
		private JTableHeader header; // set by ModelSetter.run()
		final private JFrame PARENT;
		private SQLTableModel currentModel; // set by ModelSetter.run()
	
/**
* Constructor for empty table.
*/
		public SQLTable(JFrame parent) {
			super();
			PARENT = parent;
			addMouseListener(this);
		} // end of SQLTable.constructor(JFrame)

/*
* Gives this table a content.
* @param	model	The defining SQLTableModel.
*/
		private synchronized void setNewModel(SQLTableModel model) {
			ModelSetter r = new ModelSetter(model);
			AWTUtilities.doInEventThread(r);
		} // end of SQLTable.setNewModel(SQLTableModel)

/**
* Gives this table a content using the command in mainCommand and commandExtension.
*/
		private void setNewModel() throws SQLException {
			if (tableStatement == null) {
				tableStatement = mainConnection.createStatement();
			}
			String command = (mainCommand + " " + commandExtension).trim();
			commandExtension = "";
			PARENT.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			ResultSet rs = executeSQLQuery(tableStatement, command);
			PARENT.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			setNewModel(new SQLTableModel(rs));
		} // end of SQLTable.setNewModel()

/**
* @return	The defining SQLTableModel, or null.
*/
		private final SQLTableModel getSQLModel() {
			return currentModel;
		} // end of SQLTable.getSQLModel()
		
/**
* @param	row					A row index (1-based).
* @param	columnName	Name of a table column.
* @return	The integer at that position.
*/
		private final int getIntFromTable(int row, String columnName) throws SQLException {
			ResultSet rs = getSQLModel().RS;
			rs.absolute(row);
			return rs.getInt(columnName);
		} // end of SQLTable.getIntFromTable(int, String)
		
/**
* @param	row					A row index (1-based).
* @param	columnName	Name of a table column.
* @return	The String at that position.
*/
		private final String getStringFromTable(int row, String columnName) throws SQLException {
			ResultSet rs = getSQLModel().RS;
			rs.absolute(row);
			return rs.getString(columnName);
		} // end of SQLTable.getStringFromTable(int, String)

/**
* @return			A String[ ] of all the column names.
*/
		private final String[ ] getColumnNames() {
		
			String[ ] s = new String[getColumnCount()];
			for (int i=0; i<getColumnCount(); i++) {
				s[i] = getColumnName(i);
			}
			return s;
		} // end of SQLTable.getColumnNames()

/**
* @return			A String[ ] of all the SequenceColumn names.
*/
		String[ ] getSequenceColumnNames() {
		
			Stack st = new Stack();
			String s;
			Column col;
			for (int i=0; i<getColumnCount(); i++) {
				s = getColumnName(i);
				col = (Column) columnTable.get(s);
				if (col instanceof SequenceColumn) {
					st.push(s);
				}
			}
			String[ ] ss = new String[st.size()];
			st.copyInto(ss);
			return ss;
		} // end of SQLTable.getSequenceColumnNames()

/**
* @return	An int[ ] with indices of all selected rows, or, if none are selected, all rows.
*/
		private final int[ ] getSelectedOrAllRows() {
			int[ ] ii = getSelectedRows();
			if ((ii == null) || (ii.length == 0)) {
				ii = new int[getRowCount()];
				for (int i=0; i<ii.length; i++) {
					ii[i] = i;
				}
			}
			return ii;
		} // end of SQLTable.getSelectedOrAllRows()

/**
* To accumulate shift-clicks.
*/
		private Stack orderStack = null;
		
/**
* As required by MouseListener.
* If the click was in a column header, the table is ordered or reordered on it.
* Otherwise, the row clicked in is selected.
* If the click was in a textual field, its full contents are shown in a separate window.
*/
		public void mouseClicked(MouseEvent e) {
			Point p = e.getPoint();
			int rowi = rowAtPoint(p);
			int coli = columnAtPoint(p);
			if (e.getSource() == header) { // click in column header
				String cna = getColumnName(coli); // which column?
				Column col = (Column) columnTable.get(cna);
				if (col != null) { // sort command will be pushed on orderStack
					if ((e.getModifiers() & MouseEvent.SHIFT_MASK) == 0) {
						orderStack = new Stack();
					} else {
						if (((String) orderStack.peek()).startsWith(cna)) {
							orderStack.pop(); // remove old command
						}
					}
					if (col.sortDirection > 0) {
						orderStack.push(cna + " asc");
					} else {
						orderStack.push(cna + " desc");
					}
					col.sortDirection = -col.sortDirection;
					commandExtension = "order by " + ((String) orderStack.elementAt(0));
					for (int i=0; i<orderStack.size(); i++) {
						commandExtension = commandExtension + "," + ((String) orderStack.elementAt(i));
					}
				}
				try {
					setNewModel();
				} catch (SQLException ex ) {
					RetroTectorEngine.displayError(new RetroTectorException("SQLUser.SQLTable", ex), RetroTectorEngine.ERRORLEVEL);
				}
			} else { // in table body
				Object o = getValueAt(rowi, coli);
				if (o instanceof String) {
					AWTUtilities.showStringInFrame((String) o, getColumnName(coli));
				}
			}
		} // end of SQLTable.mouseClicked(MouseEvent)
	
/**
* As required by MouseListener.
*/
		public void mouseEntered(MouseEvent e) {
		} // end of SQLTable.mouseEntered(MouseEvent)
		
/**
* As required by MouseListener.
*/
		public void mouseExited(MouseEvent e) {
		} // end of SQLTable.mouseExited(MouseEvent)
		
/**
* As required by MouseListener.
*/
		public void mousePressed(MouseEvent e) {
		} // end of SQLTable.mousePressed(MouseEvent)
		
/**
* As required by MouseListener.
*/
		public void mouseReleased(MouseEvent e) {
		} // end of SQLTable.mouseReleased(MouseEvent)

	} // end of SQLTable


/**
* To add tabs to tabPane Thread safely.
*/
	private class Starter implements Runnable {

/**
* As required by Runnable.
*/
		public void run() {
			while (tabPane.getTabCount() > 1) { // remove all panes except firstPanel
				tabPane.remove(1);
			}
			try {
				VIEWWINDOW.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

// make and add chainOverviewPanel
				JScrollPane sp = new JScrollPane(chainOverviewPanel = new ChainOverviewPanel());
				sp.setPreferredSize(TABDIMENSION);
				JPanel jp = new JPanel();
				jp.setLayout(new BorderLayout());
				JPanel tp = new JPanel();
				JButton saveChainButton = new JButton("Save graphics");
				saveChainButton.addActionListener(chainOverviewPanel);
				tp.add(saveChainButton);
				JButton widenChainButton = new JButton(WIDER);
				widenChainButton.addActionListener(chainOverviewPanel);
				tp.add(widenChainButton);
				JButton narrowChainutton = new JButton(NARROWER);
				narrowChainutton.addActionListener(chainOverviewPanel);
				tp.add(narrowChainutton);
				jp.add(tp, BorderLayout.NORTH);
				jp.add(sp);
				tabPane.addTab("Chains overview", jp);

// make and add ltrOverviewPanel
				sp = new JScrollPane(ltrOverviewPanel = new LTROverviewPanel());
				sp.setPreferredSize(TABDIMENSION);
				jp = new JPanel();
				jp.setLayout(new BorderLayout());
				tp = new JPanel();
				JButton saveLTRButton = new JButton("Save graphics");
				saveLTRButton.addActionListener(ltrOverviewPanel);
				tp.add(saveLTRButton);
				JButton widenLTRButton = new JButton(WIDER);
				widenLTRButton.addActionListener(ltrOverviewPanel);
				tp.add(widenLTRButton);
				JButton narrowLTRButton = new JButton(NARROWER);
				narrowLTRButton.addActionListener(ltrOverviewPanel);
				tp.add(narrowLTRButton);
				jp.add(tp, BorderLayout.NORTH);
				jp.add(sp);
				tabPane.addTab("LTRs overview", jp);

// make and add tablesPanel
				tablesPanel = new TablesPanel();
				tabPane.addTab("Tables", tablesPanel);
				tablesPanel.validate();

// make and add chainDetailsPanel
				chainDetailsPanel = new ChainDetailsPanel();
				tabPane.addTab("Chain details", new JScrollPane(chainDetailsPanel));

// make and add graphInfoArea
				sp = new JScrollPane(graphInfoArea = new GraphInfoArea());
				sp.setPreferredSize(TABDIMENSION);
				tabPane.addTab("Chain info", sp);

// make and add htmlPane
				sp = new JScrollPane(htmlPane = new HTMLPane());
				sp.setPreferredSize(TABDIMENSION);
				tabPane.addTab("Ref info", sp);
				VIEWWINDOW.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} catch (SQLException e) {
				RetroTectorEngine.displayError(new RetroTectorException("Genomeview", e));
			}
			tabPane.validate();
		} // end of Starter.run()
		
	} // end of Starter


/**
* First tab panel: allows choosing of database and execution of SQL commands.
*/
	private class FirstPanel extends JPanel implements ActionListener {

/**
* To execute an SQL command.
*/
		private class Querier extends Thread {
		
			private final String Q;
			private final Statement STAT;
	
			private Querier(String query) throws SQLException {
				Q = query;
				STAT = getNewStatement();
			} // end of FirstPanel.Querier.constructor(String)
			
/**
* Executes the SQL command and displays result, if any, in REPLYTABLE.
*/
			public void run() {
				try {
					ResultSet rs = executeSQLQuery(STAT, Q);
					if (rs != null) {
						SQLTableModel model = new SQLTableModel(rs);
						REPLYTABLE.setNewModel(model);
						AWTUtilities.validateContainer(RESULTPANEL);
					}
				} catch (SQLException e) { // command, not query?
					try {
						executeSQLCommand(STAT, Q);
					} catch (SQLException eg) {
						RetroTectorEngine.displayError(new RetroTectorException("Genomeview", eg));
					}
				}
			} // end of FirstPanel.Querier.run()
			
		} // end of FirstPanel.Querier

	
/**
* Blank field + all databases.
*/
		final JComboBox DATABASEBOX = new JComboBox();

/**
* To enter SQL query.
*/
		final JTextArea QUERYAREA = new JTextArea(5, 50);

/**
* To execute SQL query.
*/
		final JButton QUERYBUTTON = new JButton("Execute it");

/**
* To display result of SQL query.
*/
		final SQLTable REPLYTABLE = new SQLTable(null);

/**
* To conatin REPLYTABLE.
*/
		final JPanel RESULTPANEL = new JPanel();

/**
* Constructor.
*/
		private FirstPanel() throws SQLException {
			setPreferredSize(TABDIMENSION);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			DATABASEBOX.addItem("");
			ResultSet rs = executeSQLQuery("show databases");
			String latest = RetroTectorEngine.getProperty("genomeview.latestdatabase");
			String ps;
			while (rs.next()) {
				ps = rs.getString(1);
				if (ps.equals(latest)) {
					ps = "* " + ps;
				}
				DATABASEBOX.addItem(ps);
			}
			DATABASEBOX.addActionListener(this);
			JPanel db = new JPanel();
			db.add(new JLabel("Choose genome:"));
			db.add(DATABASEBOX);
			add(db);
			QUERYAREA.setBorder(BorderFactory.createLineBorder(Color.black));
			JPanel queryPanel = new JPanel();
			queryPanel.add(new JLabel("Enter an SQL command:"));
			queryPanel.add(QUERYAREA);
			QUERYBUTTON.addActionListener(this);
			queryPanel.add(QUERYBUTTON);
			add(queryPanel);
			RESULTPANEL.add(new JScrollPane(REPLYTABLE));
			add(RESULTPANEL);
		} // end of FirstPanel.constructor()
		
/**
* As required by ActionListener.
*/
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource() == DATABASEBOX) {
				currentGenomeName = (String) DATABASEBOX.getSelectedItem();
				if (currentGenomeName.startsWith("* ")) {
					currentGenomeName = currentGenomeName.substring(1).trim();
				}
				try {
					setDatabase(currentGenomeName);
				} catch (RetroTectorException e) {
					RetroTectorEngine.displayError(e);
					return;
				}
				RetroTectorEngine.setProperty("genomeview.latestdatabase", currentGenomeName);
				AWTUtilities.doInEventThread(new Starter());
			} else if (ae.getSource() == QUERYBUTTON) {
				Querier qu = null;
				try {
					qu = new Querier(QUERYAREA.getText());
				} catch (SQLException sqe) {
					RetroTectorEngine.displayError(new RetroTectorException("Genomeview", sqe));
					return;
				}
				qu.start();
			}
		} // end of FirstPanel.actionPerformed(ActionEvent)

	} // end of FirstPanel


/**
* Superclass of JCanvas displaying a histogram relating to a chromosome.
*/
	abstract class ChromosomeOverview extends JCanvas {
	
/**
* The name of the chromosome.
*/
		String CHRNAME;
			
/**
* The length of the chromosome (in bases).
*/
		int CHRLENGTH;

/**
* To indicate that recalculation is needed.
*/
		boolean selectionChanged = true;
		
/**
* Recalculates histogram if required.
* @return	Maximum number of items in any bin.
*/
		abstract int makeHistogram() throws SQLException;
		
	} // end of ChromosomeOverview


/**
* Superclass of panel displaying a number of ChromosomeOverview.
*/
	abstract class OverviewPanel extends JCanvas implements ListSelectionListener {

/**
* The ChromosomeOverviews to show.
*/
		ChromosomeOverview[ ] chOvs; // one for each chromosome

/**
* To indicate that recalculation is needed.
*/
 		boolean selChanged;

/**
* As required by ListSelectionListener. Sets indicators that recalculation is needed.
*/
		public void valueChanged(ListSelectionEvent ev) {
			setSelectionChanged(true);
		} // end of OverviewPanel.valueChanged(ListSelectionEvent)
		
/**
* Sets indicators that recalculation is needed, or not.
*/
		final void setSelectionChanged(boolean b) {
			for (int i=0; i<chOvs.length; i++) {
				chOvs[i].selectionChanged = b;
			}
			selChanged = b;
		} // end of OverviewPanel.setSelectionChanged(boolean)
		
/**
* As required by JComponent.
*/
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
		} // end of OverviewPanel.paintComponent(Graphics)
		
	} // end of OverviewPanel
	
	
/**
* Tab panel for histograms of Chains.
*/
	private class ChainOverviewPanel extends OverviewPanel implements ActionListener {
	
/**
* Canvas for histogram of Chains in one chromosome.
*/
		private class ChromosomeChainOverview extends ChromosomeOverview {
		
/**
* For Chain middle positions.
*/
			final int[ ][ ] CHAINHITS;

			private int[ ] histogram = null; // number of Chains in each bin
			private int latestBinSize = -1; // size of bins
			private int latestMaxInBin = -1; // max number of Chains in one bin
				
/**
* Constructor. Collects Chain positions.
* @param	chrName	Name of chromosome.
*/
			private ChromosomeChainOverview(String chrName) throws SQLException {
			
				CHRNAME = chrName;
				String s = "Select " + getColumnTitle("ChromosomeLengthColumn") + " from " + CHROMOSOMESNAME + " where " + CHROMOSOME + "=" + BLIP + CHRNAME + BLIP;
				ResultSet rs = executeSQLQuery(s);
				rs.next();
				CHRLENGTH = rs.getInt(getColumnTitle("ChromosomeLengthColumn"));
				String chainS = getColumnTitle("ChainStartColumn");
				String chainE = getColumnTitle("ChainEndColumn");
				rs = executeSQLQuery("Select " + chainS + "," + chainE + " from " + CHAINSNAME + " where " + CHROMOSOME + "=" + BLIP + chrName + BLIP + " and " + getColumnTitle("ChainOverlapperColumn") + "=" + BLIP + "NULL" + BLIP);
				Stack st = new Stack();
				int[ ] is;
				while (rs.next()) {
					is = new int[1];
					is[0] = (rs.getInt(chainS) + rs.getInt(chainE)) / 2;
					st.push(is);
				}
				CHAINHITS = new int[st.size()][];
				st.copyInto(CHAINHITS);
				setToolTipText("1");
				ttMan.registerComponent(this);
			} // end of ChainOverviewPanel.ChromosomeChainOverview.constructor(String)
			
/**
* Recalculates histogram if required.
* @return	Max number of Chains in one bin
*/
			final int makeHistogram() throws SQLException {
				if ((latestBinSize == currentBinSize) && !selectionChanged) {
					return latestMaxInBin;
				}
				latestBinSize = currentBinSize;
				histogram = new int[CHRLENGTH / currentBinSize + 1];
				if ((tablesPanel == null) || (tablesPanel.CHAINSPANEL.MAINTABLE == null) || (tablesPanel.CHAINSPANEL.MAINTABLE.getSQLModel() == null) || (tablesPanel.CHAINSPANEL.MAINTABLE.getSQLModel().RS == null)) { // use all Chains
					for (int i=0; i<CHAINHITS.length; i++) {
						histogram[CHAINHITS[i][0] / currentBinSize]++;
					}
				} else {
					ResultSet rs = tablesPanel.CHAINSPANEL.MAINTABLE.getSQLModel().RS;
					int[ ] rownrs = tablesPanel.CHAINSPANEL.MAINTABLE.getSelectedOrAllRows();
					String chainS = getColumnTitle("ChainStartColumn");
					String chainE = getColumnTitle("ChainEndColumn");
					String overL = getColumnTitle("ChainOverlapperColumn");
					int iz;
					if (rownrs.length == 0) { // use all Chains. Shouldn't happen
						rs.beforeFirst();
						while (rs.next()) {
							if (rs.getString(CHROMOSOME).equals(CHRNAME) & rs.getString(overL).equals("NULL")) {
								iz = (rs.getInt(chainS) + rs.getInt(chainE)) / 2;
								try {
									histogram[iz / currentBinSize]++;
								} catch (ArrayIndexOutOfBoundsException abe) {
									throw abe;
								}
							}
						}
					} else { // use selected Chains
						for (int iy=0; iy<rownrs.length; iy++) {
							rs.absolute(rownrs[iy] + 1);
							if (rs.getString(CHROMOSOME).equals(CHRNAME) & rs.getString(overL).equals("NULL")) {
								iz = (rs.getInt(chainS) + rs.getInt(chainE)) / 2;
								histogram[iz / currentBinSize]++;
							}
						}
					}
				}
				latestMaxInBin = 0;
				for(int h=0; h<histogram.length; h++) {
					latestMaxInBin = Math.max(latestMaxInBin, histogram[h]);
				}
				selectionChanged = false;
				return latestMaxInBin;
			} // end of ChainOverviewPanel.ChromosomeChainOverview.makeHistogram()
			
/**
* Returns first and last Chain middle positions in the line at xcoord.
*/
			private final int[ ] bin(int xcoord) {
				int[ ] result = new int[2];
				result[0] = (xcoord - LEFTMARGIN) * currentBinSize;
				result[1] = result[0] + currentBinSize - 1;
				return result;
			} // end of ChainOverviewPanel.ChromosomeChainOverview.bin(int)
			
/**
* As required by JComponent.
*/
			public void paint(Graphics g) {
				paintComponent(g);
			} // end of ChainOverviewPanel.ChromosomeChainOverview.paint(Graphics)
			
/**
* As required by JComponent.
*/
			public void paintComponent(Graphics g) {
				recalculate();
				if (histogram == null) {
					return;
				}
				int basePos = paintHistMax + 1;
				int h;
				int v;
				g.setColor(Color.red);
				if (histMax > 0) {
					for (int i=0; i<histogram.length; i++) {
						h = LEFTMARGIN + i;
						v = paintHistMax * histogram[i] / histMax;
						g.drawLine(h, basePos, h, basePos - v);
					}
				}
				g.setColor(Color.black);
				g.drawLine(LEFTMARGIN, basePos, LEFTMARGIN + histogram.length, basePos);
				g.drawString(CHRNAME, 1, basePos);
				g.drawString(String.valueOf(CHRLENGTH), LEFTMARGIN + 2 + histogram.length, basePos);
				
			} // end of ChainOverviewPanel.ChromosomeChainOverview.paintComponent(Graphics)
			
/**
* @param	event	MouseEvent indicating cursor position.
* @return	String form of approximate position in chromosome.
*/
			public String getToolTipText(MouseEvent event) {
				if (histogram == null) {
					return "";
				}
				int x = event.getX();
				if ((x < LEFTMARGIN) | (x > LEFTMARGIN + histogram.length - 1)) {
					return "";
				}
				if (event.getY() > paintHistMax + 1) {
					return "";
				}
				return String.valueOf(currentBinSize * (event.getX() - LEFTMARGIN));
			} // end of ChainOverviewPanel.ChromosomeChainOverview.getToolTipText(MouseEvent)
		
		} // end of ChainOverviewPanel.ChromosomeChainOverview
		

		private int maxChrLength = 0; // length of longest chromosome
		private int histMax; // size of fullest bin of any
		private int paintHistMax; // height of line for fullest bin of any
		private int currentBinSize = 0;
		private int latestHeight = 0;
		private int latestWidth = 0;
		
		private ChainOverviewPanel() throws SQLException {
			setLayout(new GridLayout(0, 1));
			setOpaque(false); // or Mac Java makes out
			ResultSet rs = executeSQLQuery("Select " + CHROMOSOME + " from " + CHROMOSOMESNAME + " order by " + CHROMOSOME);
			Stack st = new Stack();
			while (rs.next()) {
				st.push(rs.getString(CHROMOSOME));
			}
			chOvs = new ChromosomeChainOverview[st.size()];
			for (int i=0; i<chOvs.length; i++) {
				chOvs[i] = new ChromosomeChainOverview((String) st.elementAt(i));
				maxChrLength = Math.max(maxChrLength, chOvs[i].CHRLENGTH);
				add(chOvs[i]);
			}
			setPreferredSize(new Dimension(TABDIMENSION.width - 10, Math.max(TABDIMENSION.height - 10, 15 * chOvs.length)));
		} // end of ChainOverviewPanel.constructor()
		
/**
* Adjusts to changed dimensions or table change.
*/
		private final void recalculate() {
			if ((latestHeight != getHeight()) | (latestWidth != getWidth()) | selChanged) {
				latestHeight = getHeight();
				latestWidth = getWidth();
				paintHistMax = latestHeight / chOvs.length - VERTMARGIN;
				currentBinSize = maxChrLength / (latestWidth - LEFTMARGIN - RIGHTMARGIN);
				histMax = 0;
				for (int c=0; c<chOvs.length; c++) {
					try {
						histMax = Math.max(histMax, chOvs[c].makeHistogram());
					} catch (SQLException se) {
						RetroTectorEngine.displayError(new RetroTectorException("Genomeview", se));
					}
				}
				selChanged = false;
			}
		} // end of ChainOverviewPanel.recalculate()

/**
* As required by JComponent.
*/
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			recalculate();
			paintChildren(g);
		} // end of ChainOverviewPanel.paintComponent(Graphics)
		
/**"Save graphics"
* As required by ActionListener.
*/
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource() instanceof JButton) {
				Dimension dim = getPreferredSize();
				JButton jb = (JButton) ae.getSource();
				if (jb.getText().equals(WIDER)) {
					setPreferredSize(new Dimension(dim.width * 2, dim.height));
					revalidate();
				} else if (jb.getText().equals(NARROWER)) {
					setPreferredSize(new Dimension(dim.width / 2, dim.height));
					revalidate();
				} else if (jb.getText().equals("Save graphics")) {
					try {
						ChainOverviewPanel comp = new ChainOverviewPanel();
						int h = comp.getHeight();
						int w = comp.getWidth();
						GraphicsJFrame gfr = new GraphicsJFrame("Save form", comp);
					} catch (SQLException e) {
						RetroTectorEngine.displayError(new RetroTectorException("Genomeview", e));
					}
				}																		 
			}										
		} // end of ChainOverviewPanel.actionPerformed(ActionEvent)
		
	} // end of ChainOverviewPanel
	
	
/**
* Tab panel for histograms of LTRs.
*/
	private class LTROverviewPanel extends OverviewPanel implements ActionListener {
	
/**
* Canvas for histogram of LTRs in one chromosome.
*/
		private class ChromosomeLTROverview extends ChromosomeOverview {
		
/**
* For LTR middle positions.
*/
			final int[ ][ ] LTRHITS;

			private int[ ] histogram; // number of LTRs in each bin
			private int latestBinSize = -1; // size of bins
			private int latestMaxInBin = -1; // max number of LTRs in one bin
			
/**
* Constructor. Collects LTR positions.
*/
			private ChromosomeLTROverview(String chrName) throws SQLException {
			
				CHRNAME = chrName;
				String s = "Select " + getColumnTitle("ChromosomeLengthColumn") + " from " + CHROMOSOMESNAME + " where " + CHROMOSOME + "=" + BLIP + CHRNAME + BLIP;
				ResultSet rs = executeSQLQuery(s);
				rs.next();
				CHRLENGTH = rs.getInt(getColumnTitle("ChromosomeLengthColumn"));
				String ltrH = getColumnTitle("LTRHotspotPosColumn");
				rs = executeSQLQuery("Select " + ltrH + " from " + LTRSNAME + " where " + CHROMOSOME + "=" + BLIP + chrName + BLIP);
				Stack st = new Stack();
				int[ ] is;
				while (rs.next()) {
					is = new int[1];
					is[0] = rs.getInt(ltrH);
					st.push(is);
				}
				LTRHITS = new int[st.size()][];
				st.copyInto(LTRHITS);
				setToolTipText("1");
				ttMan.registerComponent(this);
			} // end of LTROverviewPanel.ChromosomeLTROverview.constructor()
			
/**
* Recalculates histogram if required.
@return	Max number of LTRs in one bin.
*/
			final int makeHistogram() throws SQLException {
				if ((latestBinSize == currentBinSize) && !selectionChanged) {
					return latestMaxInBin;
				}
				latestBinSize = currentBinSize;
				histogram = new int[CHRLENGTH / currentBinSize + 1];
				if ((tablesPanel == null) || (tablesPanel.LTRSPANEL.MAINTABLE == null) || (tablesPanel.LTRSPANEL.MAINTABLE.getSQLModel() == null) || (tablesPanel.LTRSPANEL.MAINTABLE.getSQLModel().RS == null)) { // use all LTRs
					for (int i=0; i<LTRHITS.length; i++) {
						histogram[LTRHITS[i][0] / currentBinSize]++;
					}
				} else {
					ResultSet rs = tablesPanel.LTRSPANEL.MAINTABLE.getSQLModel().RS;
					int[ ] rownrs = tablesPanel.LTRSPANEL.MAINTABLE.getSelectedOrAllRows();
					String ltrH = getColumnTitle("LTRHotspotPosColumn");
					int iz;
					if (rownrs.length == 0) { // use all LTRs
						rs.beforeFirst();
						while (rs.next()) {
							if (rs.getString(CHROMOSOME).equals(CHRNAME)) {
								iz = rs.getInt(ltrH);
								try {
									histogram[iz / currentBinSize]++;
								} catch (ArrayIndexOutOfBoundsException abe) {
									throw abe;
								}
							}
						}
					} else { // use selected LTRs
						for (int iy=0; iy<rownrs.length; iy++) {
							rs.absolute(rownrs[iy] + 1);
							if (rs.getString(CHROMOSOME).equals(CHRNAME)) {
								iz = rs.getInt(ltrH);
								histogram[iz / currentBinSize]++;
							}
						}
					}
				}
				latestMaxInBin = 0;
				for(int h=0; h<histogram.length; h++) {
					latestMaxInBin = Math.max(latestMaxInBin, histogram[h]);
				}
				selectionChanged = false;
				return latestMaxInBin;
			} // end of LTROverviewPanel.ChromosomeLTROverview.makeHistogram()

/**
* Returns first and last LTR positions in the line at xcoord.
*/
			private final int[ ] bin(int xcoord) {
				int[ ] result = new int[2];
				result[0] = (xcoord - LEFTMARGIN) * currentBinSize;
				result[1] = result[0] + currentBinSize - 1;
				return result;
			} // end of LTROverviewPanel.ChromosomeLTROverview.bin(int)
			
/**
* As required by JComponent.
*/
			public void paint(Graphics g) {
				paintComponent(g);
			} // end of LTROverviewPanel.ChromosomeChainOverview.paint(Graphics)
			
/**
* As required by JComponent.
*/
			public void paintComponent(Graphics g) {
				recalculate();
				if (histogram == null) {
					return;
				}
				int basePos = paintHistMax + 1;
				int h;
				int v;
				g.setColor(Color.blue);
				if (histMax > 0) {
					for (int i=0; i<histogram.length; i++) {
						h = LEFTMARGIN + i;
						v = paintHistMax * histogram[i] / histMax;
						g.drawLine(h, basePos, h, basePos - v);
					}
				}
				g.setColor(Color.black);
				g.drawLine(LEFTMARGIN, basePos, LEFTMARGIN + histogram.length, basePos);
				g.drawString(CHRNAME, 1, basePos);
				g.drawString(String.valueOf(CHRLENGTH), LEFTMARGIN + 2 + histogram.length, basePos);
				
			} // end of LTROverviewPanel.ChromosomeLTROverview.paintComponent(Graphics)
			
/**
* @param	event	MouseEvent indicating cursor position.
* @return	String form of approximate position in chromosome.
*/
			public final String getToolTipText(MouseEvent event) {
				if (histogram == null) {
					return "";
				}
				int x = event.getX();
				if ((x < LEFTMARGIN) | (x > LEFTMARGIN + histogram.length - 1)) {
					return "";
				}
				if (event.getY() > paintHistMax - VERTMARGIN) {
					return "";
				}
				return String.valueOf(currentBinSize * (event.getX() - LEFTMARGIN));
			} //  end of LTROverviewPanel.ChromosomeLTROverview.getToolTipText(MouseEvent)
		
		} // end of LTROverviewPanel.ChromosomeLTROverview
		

		private int maxChrLength = 0; // length of longest chromosome
		private int histMax; // size of fullest bin of any
		private int paintHistMax; // height of line for fullest bin of any
		private int currentBinSize = 0;
		private int latestHeight = 0;
		private int latestWidth = 0;
	
		private LTROverviewPanel() throws SQLException {
			setLayout(new GridLayout(0, 1));
			setOpaque(false); // or Mac Java makes out
			ResultSet rs = executeSQLQuery("Select " + CHROMOSOME + " from " + CHROMOSOMESNAME + " order by " + CHROMOSOME);
			Stack st = new Stack();
			while (rs.next()) {
				st.push(rs.getString(CHROMOSOME));
			}
			chOvs = new ChromosomeLTROverview[st.size()];
			for (int i=0; i<chOvs.length; i++) {
				chOvs[i] = new ChromosomeLTROverview((String) st.elementAt(i));
				maxChrLength = Math.max(maxChrLength, chOvs[i].CHRLENGTH);
				add(chOvs[i]);
			}
			setPreferredSize(new Dimension(TABDIMENSION.width - 10, Math.max(TABDIMENSION.height - 10, 15 * chOvs.length)));
		} // end of LTROverviewPanel.constructor()

/**
* Adjusts to changed dimensions or table change.
*/
		private final void recalculate() {
			if ((latestHeight != getHeight()) | (latestWidth != getWidth()) | selChanged) {
				latestHeight = getHeight();
				latestWidth = getWidth();
				paintHistMax = latestHeight / chOvs.length - VERTMARGIN;
				currentBinSize = maxChrLength / (latestWidth - LEFTMARGIN - RIGHTMARGIN);
				histMax = 0;
				for (int c=0; c<chOvs.length; c++) {
					try {
						histMax = Math.max(histMax, chOvs[c].makeHistogram());
					} catch (SQLException se) {
						RetroTectorEngine.displayError(new RetroTectorException("Genomeview", se));
					}
				}
			}
		} // end of LTROverviewPanel.recalculate()
		
/**
* As required by JComponent.
*/
		public void paint(Graphics g) {
			super.paintComponent(g);
			recalculate();
			paintChildren(g);
		} // end of LTROverviewPanel.paintComponent(Graphics)
		
/**
* As required by ActionListener.
*/
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource() instanceof JButton) {
				Dimension dim = getPreferredSize();
				JButton jb = (JButton) ae.getSource();
				if (jb.getText().equals(WIDER)) {
					setPreferredSize(new Dimension(dim.width * 2, dim.height));
					revalidate();
				} else if (jb.getText().equals(NARROWER)) {
					setPreferredSize(new Dimension(dim.width / 2, dim.height));
					revalidate();
				} else if (jb.getText().equals("Save graphics")) {
					try {
						LTROverviewPanel comp = new LTROverviewPanel();
						int h = comp.getHeight();
						int w = comp.getWidth();
						GraphicsJFrame gfr = new GraphicsJFrame("Save form", comp);
					} catch (SQLException e) {
						RetroTectorEngine.displayError(new RetroTectorException("Genomeview", e));
					}
				}																		 
			}
		} // end of actionPerformed(ActionEvent)
		
	} // end of LTROverviewPanel


/**
* Tab panel showing SQLTables of Chains, Puteins and LTRs.
*/
	private class TablesPanel extends JPanel {
	
/**
* To handle export of a selection from an SQLTable.
*/
		abstract class ExportThread implements Runnable {

/**
* To indicate target file type.
*/
			protected String targetName;

/**
* Panel to export from.
*/
			protected TablePanel sourcePanel;
			
/**
* Does the actual exporting.
*/
			public void run() {
				try {
					if (targetName.equals(TABDELIMLABEL)) {
						String[ ] ss = sourcePanel.MAINTABLE.getColumnNames();
						ListDialog ld = new ListDialog((Frame) RetroTectorEngine.retrotector, ss);
						String[ ] coln = ld.getResult();
						ld.dispose();
						if ((coln == null) || (coln.length == 0)) {
							return;
						}
						JFileChooser fd = new JFileChooser(RetroTectorEngine.currentDirectory());
						fd.setDialogTitle("File to save in");
						fd.showSaveDialog(null);
						File ff = fd.getSelectedFile();
						if (ff != null) {
							try {
								PrintWriter pw = new PrintWriter(new FileWriter(ff));
								ResultSet rs = sourcePanel.MAINTABLE.getSQLModel().RS;
								int[ ] rownrs = sourcePanel.MAINTABLE.getSelectedOrAllRows();
// output column headers
								System.out.println("Starting tab-delimited file output");
								pw.print(coln[0]);
								for (int co=1; co<coln.length; co++) {
									pw.print("\t" + coln[co]);
								}
								pw.println();
								for (int ro=0; ro<rownrs.length; ro++) {
									rs.absolute(rownrs[ro] + 1);
									pw.print(cleanString(rs.getString(coln[0])));
									for (int co=1; co<coln.length; co++) {
										pw.print("\t" + cleanString(rs.getString(coln[co])));
									}
									pw.println();
								}
								pw.close();
								System.out.println("Tab-delimited file finished");
							} catch (IOException ioe) {
								RetroTectorEngine.displayError(new RetroTectorException("Genomeview", "I/O trouble with " + ff.getName()));
							} catch (SQLException sqle) {
								RetroTectorEngine.displayError(new RetroTectorException("Genomeview", sqle));
							}
						}
					} else if (targetName.equals(HTMLLABEL)) {
						String[ ] ss = sourcePanel.MAINTABLE.getColumnNames();
						ListDialog ld = new ListDialog((Frame) RetroTectorEngine.retrotector, ss);
						String[ ] coln = ld.getResult();
						ld.dispose();
						if ((coln == null) || (coln.length == 0)) {
							return;
						}
						JFileChooser fd = new JFileChooser(RetroTectorEngine.currentDirectory());
						fd.setDialogTitle("File to save in");
						fd.showSaveDialog(null);
						File ff = fd.getSelectedFile();
						Column col;
						if (ff != null) {
							try {
								PrintWriter pw = new PrintWriter(new FileWriter(ff));
								pw.println("<table style=\"text-align: left;\"><tbody>");
								ResultSet rs = sourcePanel.MAINTABLE.getSQLModel().RS;
								int[ ] rownrs = sourcePanel.MAINTABLE.getSelectedOrAllRows();
								pw.println("<tr>");
								for (int co=0; co<coln.length; co++) {
									pw.println("<td>" + coln[co] + "<br></td>");
								}
								pw.println("</tr>");
								for (int ro=0; ro<rownrs.length; ro++) {
									rs.absolute(rownrs[ro] + 1);
									pw.print("<tr>");
									for (int co=0; co<coln.length; co++) {
										col = (Column) columnTable.get(coln[co]);
										if ((col instanceof IntColumn) | (col instanceof FloatColumn)) {
											pw.print("<td style=\"text-align: right;\">");
										} else {
											pw.print("<td>");
										}
										pw.println(cleanString(rs.getString(coln[co])) + "<br></td>");
									}
									pw.println("</tr>");
								}
								pw.println("</tbody></table>");
								pw.close();
							} catch (IOException ioe) {
								RetroTectorEngine.displayError(new RetroTectorException("Genomeview", "I/O trouble with " + ff.getName()));
							} catch (SQLException sqle) {
								RetroTectorEngine.displayError(new RetroTectorException("Genomeview", sqle));
							}
						}
					} else if (targetName.equals(STRUCTLABEL)) {
						String[ ] ss = sourcePanel.MAINTABLE.getColumnNames();
						ListDialog ld = new ListDialog((Frame) RetroTectorEngine.retrotector, ss);
						String[ ] coln = ld.getResult();
						ld.dispose();
						if ((coln == null) || (coln.length == 0)) {
							return;
						}
						JFileChooser fd = new JFileChooser(RetroTectorEngine.currentDirectory());
						fd.setDialogTitle("File to save in");
						fd.showSaveDialog(null);
						File ff = fd.getSelectedFile();
						Column col;
						if (ff != null) {
							try {
								PrintWriter pw = new PrintWriter(new FileWriter(ff));
								pw.println("!$$$ " + ff.getPath());
								ResultSet rs = sourcePanel.MAINTABLE.getSQLModel().RS;
								int[ ] rownrs = sourcePanel.MAINTABLE.getSelectedOrAllRows();
								for (int ro=0; ro<rownrs.length; ro++) {
									rs.absolute(rownrs[ro] + 1);
									pw.println("!$$");
									for (int co=0; co<coln.length; co++) {
										col = (Column) columnTable.get(coln[co]);
										pw.println("!$ " + coln[co] + ": " + cleanString(rs.getString(coln[co])));
									}
									pw.println("!##");
								}
								pw.println("!###");
								pw.close();
							} catch (IOException ioe) {
								RetroTectorEngine.displayError(new RetroTectorException("Genomeview", "I/O trouble with " + ff.getName()));
							} catch (SQLException sqle) {
								RetroTectorEngine.displayError(new RetroTectorException("Genomeview", sqle));
							}
						}
					} else if (targetName.equals(FASTALABEL)) {
						String[ ] ss = sourcePanel.MAINTABLE.getSequenceColumnNames(); // only sequence fields to FASTA
						ListDialog ld = new ListDialog((Frame) RetroTectorEngine.retrotector, ss, ListSelectionModel.SINGLE_SELECTION);
						String[ ] coln = ld.getResult();
						ld.dispose();
						if ((coln == null) || (coln.length == 0)) {
							return;
						}
						JFileChooser fd = new JFileChooser(RetroTectorEngine.currentDirectory());
						fd.setDialogTitle("FASTA file to save in");
						fd.showSaveDialog(null);
						File ff = fd.getSelectedFile();
						if (ff != null) {
							try {
								PrintWriter pw = new PrintWriter(new FileWriter(ff));
								ResultSet rs = sourcePanel.MAINTABLE.getSQLModel().RS;
								int[ ] rownrs = sourcePanel.MAINTABLE.getSelectedOrAllRows();
								String title = null;
								for (int ro=0; ro<rownrs.length; ro++) {
									rs.absolute(rownrs[ro] + 1);
									if (this instanceof ExportChainThread) {
										title = ">CHR" + rs.getString(CHROMOSOME) + "_" + rs.getInt("Start") + "_" + rs.getString("ChainGenus") + "_" + rs.getInt("Score") + "_" + rs.getString("PolClass");
									} else if (this instanceof ExportPuteinThread) {
										title = ">CHR" + rs.getString(CHROMOSOME) + "_" + rs.getInt("EstimatedFirst") + "_" + rs.getString("PuteinGenus") + "_" + Math.round(100 * rs.getFloat("AverageScore"));
									} else if (this instanceof ExportLTRThread) {
										title = ">CHR" + rs.getString(CHROMOSOME) + "_" + rs.getInt("FirstPos") + "_" + rs.getString("LTRGenus") + "_" + Math.round(100 * rs.getFloat("ScoreFactor"));
									}
									pw.println(title);
									String s = rs.getString(coln[0]);
									while (s.length() > 60) {
										pw.println(s.substring(0, 60));
										s = s.substring(60);
									}
									pw.println(s);
									pw.println();
								}
								pw.close();
							} catch (IOException ioe) {
								RetroTectorEngine.displayError(new RetroTectorException("Genomeview", "I/O trouble with " + ff.getName()));
							} catch (SQLException sqle) {
								RetroTectorEngine.displayError(new RetroTectorException("Genomeview", sqle));
							}
						}
					} else if (targetName.indexOf(".") > 0) { // export to specific table
						String[ ] ss = sourcePanel.MAINTABLE.getColumnNames();
						ListDialog ld = new ListDialog((Frame) RetroTectorEngine.retrotector, ss);
						String[ ] coln = ld.getResult();
						ld.dispose();
						if ((coln == null) || (coln.length == 0)) {
							return;
						}
						
						int ind = targetName.lastIndexOf(".");
						String basename = targetName.substring(0, ind);
						String tablename = targetName.substring(ind + 1);
						try {
							executeSQLCommand("create database " + basename);
						} catch (SQLException sqle) {
						}
						try {
							executeSQLCommand("create table " + targetName + " (lid int auto_increment, primary key (id))");
						} catch (SQLException sqle) {
						}
						Column col;
						for (int i=0; i<coln.length; i++) {
							col = (Column) columnTable.get(coln[i]);
							try {
								executeSQLCommand("alter table " + targetName + " add " + col.headerText());
							} catch (SQLException sqle) {
							}
						}
							
						int[ ] rownrs = sourcePanel.MAINTABLE.getSelectedOrAllRows();
						StringBuffer comd;
						for (int i=0; i<rownrs.length; i++) {
							comd = new StringBuffer("insert into ");
							comd.append(targetName);
							comd.append(" (");
							comd.append(coln[0]);
							for (int ic=1; ic<coln.length; ic++) {
								comd.append(",");
								comd.append(coln[ic]);
							}
							comd.append(") select ");
							comd.append(coln[0]);
							for (int ic=1; ic<coln.length; ic++) {
								comd.append(",");
								comd.append(coln[ic]);
							}
							comd.append(" from ");
							comd.append(sourcePanel.SUBJECTSTRING);
							comd.append(" where id = ");
							try {
								comd.append(sourcePanel.MAINTABLE.getIntFromTable(rownrs[i] + 1, "id"));
								executeSQLCommand(comd.toString());
							} catch (SQLException sqle) {
								RetroTectorEngine.displayError(new RetroTectorException("Genomeview", sqle));
							}
						}
					} else { // export to genome database
						makeNewGenome(targetName);
						try {
							goahead();
						} catch (RetroTectorException rte) {
							RetroTectorEngine.displayError(rte);
						} catch (SQLException sqle) {
							RetroTectorEngine.displayError(new RetroTectorException("Genomeview", sqle));
						}
					}
				} catch (RuntimeException re) {
					RetroTectorEngine.displayError(new RetroTectorException("Genomeview", "A Java RuntimeException occurred", "There should be more information in the text window"));
					RetroTectorEngine.toLogFile(re.toString());
					throw re;
				}
			} // end of TablesPanel.ExportThread.run()
			
/**
* @param		s	A String
* @return	s with semicolons substitudet for tabs, CRs and LFs.
*/
			protected String cleanString(String s) {
				if (s == null) {
					return null;
				}
				s = s.replace('\t', ';');
				s = s.replace('\r', ';');
				return s.replace('\n', ';');
			} // end of TablesPanel.ExportThread.cleanString(String)
			
/**
* Utility method to create a new database to export to, if needed.
* @param	genomename	Name of database.
*/
			private final void makeNewGenome(String genomename) {
				try {
					executeSQLCommand("create database " + genomename);
				} catch (SQLException e) {
				}
				StringBuffer command;
				try {
					command = new StringBuffer("create table " + genomename + "." + CHAINSNAME + " (");
					command.append(chainColumns[0].headerText());
					for (int i=1; i<chainColumns.length; i++) {
						command.append(", ");
						command.append(chainColumns[i].headerText());
					}
					command.append(", ");
					command.append((new LongCommentColumn()).headerText());
					command.append(", ");
					command.append((new FinishedAtColumn()).headerText());
					command.append(", ");
					command.append((new RTVersionColumn()).headerText());
					command.append(", primary key (id, " + CHROMOSOME + "))");
					executeSQLCommand(command);
				} catch (SQLException e) {
				}
				try {
					command = new StringBuffer("create table " + genomename + "." + PUTEINSNAME + " (id int");
					for (int i=0; i<puteinColumns.length; i++) {
						command.append(", ");
						command.append(puteinColumns[i].headerText());
					}
					command.append(", ");
					command.append((new LongCommentColumn()).headerText());
					command.append(", ");
					command.append((new FinishedAtColumn()).headerText());
					command.append(", ");
					command.append((new RTVersionColumn()).headerText());
					command.append(", primary key (id, " + CHROMOSOME + "))");
					executeSQLCommand(command);
				} catch (SQLException e) {
				}
				try {
					command = new StringBuffer("create table " + genomename + "." + LTRSNAME + " (id int");
					for (int i=0; i<ltrColumns.length; i++) {
						command.append(", ");
						command.append(ltrColumns[i].headerText());
					}
					command.append(", ");
					command.append((new LongCommentColumn()).headerText());
					command.append(", ");
					command.append((new FinishedAtColumn()).headerText());
					command.append(", ");
					command.append((new RTVersionColumn()).headerText());
					command.append(", primary key (id, " + CHROMOSOME + "))");
					executeSQLCommand(command);
				} catch (SQLException e) {
				}
				try {
					executeSQLCommand("delete from retrotectortemp.Chains");
					executeSQLCommand("delete from retrotectortemp.Puteins");
					executeSQLCommand("delete from retrotectortemp.LTRs");
				} catch (SQLException e) {
				}
			} // end of TablesPanel.ExportThread.makeNewGenome(String)
			
/**
* Utility method to create an empty retrotectortemp database.
*/
			final void makeRetrotectortemp() throws SQLException {
				makeNewGenome("retrotectortemp");
				executeSQLCommand("delete from retrotectortemp.Chains");
				executeSQLCommand("delete from retrotectortemp.Puteins");
				executeSQLCommand("delete from retrotectortemp.LTRs");
			} // end of TablesPanel.ExportThread.makeRetrotectortemp()
		
/**
* To do the work in export to a genome database.
*/
			abstract void goahead() throws RetroTectorException, SQLException;
			
		} // end of TablesPanel.ExportThread
		

/**
* To handle export from CHAINSPANEL to a genome database.
*/
		private class ExportChainThread extends ExportThread {
			
/**
* Exports selected rows in CHAINSPANEL.MAINTABLE and adherent puteins and LTRs via retrotectortemp.
*/
			void goahead() throws RetroTectorException, SQLException {
// transfer to retrotectortemp
				makeRetrotectortemp();
				String comd;
				int[ ] rownrs = CHAINSPANEL.MAINTABLE.getSelectedOrAllRows();
				for (int i=0; i<rownrs.length; i++) {
					comd = "insert into retrotectortemp.Chains select Chains.*, LongComment, FinishedAt, RTVersion from Chains, Chromosomes where Chains.id = " + CHAINSPANEL.MAINTABLE.getIntFromTable(rownrs[i] + 1, "id") + " and Chromosomes.Chromosome = Chains.Chromosome";
					executeSQLCommand(comd);
					comd = "insert into retrotectortemp.Puteins select Puteins.*, LongComment, FinishedAt, RTVersion from Puteins, Chromosomes where Puteins.ChainId = " + CHAINSPANEL.MAINTABLE.getIntFromTable(rownrs[i] + 1, "id") + " and Chromosomes.Chromosome = Puteins.Chromosome";
					executeSQLCommand(comd);
					comd = "insert into retrotectortemp.LTRs select LTRs.*, LongComment, FinishedAt, RTVersion from LTRs, Chromosomes where LTRs.InChain = " + CHAINSPANEL.MAINTABLE.getIntFromTable(rownrs[i] + 1, "id") + " and Chromosomes.Chromosome = LTRs.Chromosome";
					executeSQLCommand(comd);
				}
				
// give LTRs new id and update in chains
				ResultSet rs = executeSQLQuery("select max(id) from " + targetName + "." + LTRSNAME);
				int firstLTRId = 1; // LTR id to start at
				if (rs.next()) {
					firstLTRId = rs.getInt(1) + 1;
				}
				rs = executeSQLQuery("select id from retrotectortemp.LTRs");
				Stack st = new Stack();
				int xi;
				while (rs.next()) {
					st.push(new Integer(rs.getInt(1)));
				}
				for (int i=0; i<st.size(); i++) {
					xi = ((Integer) st.elementAt(i)).intValue();
					executeSQLCommand("update retrotectortemp.LTRs set id = " + firstLTRId + " where id = " + xi);
					executeSQLCommand("update retrotectortemp.Chains set LTR5id = " + firstLTRId + " where LTR5id = " + xi);
					executeSQLCommand("update retrotectortemp.Chains set LTR3id = " + firstLTRId + " where LTR3id = " + xi);
					firstLTRId++;
				}

// give Puteins new id
				rs = executeSQLQuery("select max(id) from " + targetName + "." + PUTEINSNAME);
				int firstPuteinId = 1;
				if (rs.next()) {
					firstPuteinId = rs.getInt(1) + 1;
				}
				rs = executeSQLQuery("select id from retrotectortemp.Puteins");
				st = new Stack();
				while (rs.next()) {
					st.push(new Integer(rs.getInt(1)));
				}
				for (int i=0; i<st.size(); i++) {
					xi = ((Integer) st.elementAt(i)).intValue();
					executeSQLCommand("update retrotectortemp.Puteins set id = " + firstPuteinId + " where id = " + xi);
					firstPuteinId++;
				}

// give Chains new id and update in ltrs and puteins. Also update refernces to puteins
				rs = executeSQLQuery("select max(id) from " + targetName + "." + CHAINSNAME);
				int firstChainId = 1;
				if (rs.next()) {
					firstChainId = rs.getInt(1) + 1;
				}
				rs = executeSQLQuery("select id from retrotectortemp.Chains");
				st = new Stack();
				while (rs.next()) {
					st.push(new Integer(rs.getInt(1)));
				}
				for (int i=0; i<st.size(); i++) {
					xi = ((Integer) st.elementAt(i)).intValue();
					executeSQLCommand("update retrotectortemp.Chains set id = " + firstChainId + " where id = " + xi);
					executeSQLCommand("update retrotectortemp.LTRs set InChain = " + firstChainId + " where InChain = " + xi);
					executeSQLCommand("update retrotectortemp.Puteins set ChainId = " + firstChainId + " where ChainId = " + xi);
					rs = executeSQLQuery("select id from retrotectortemp.Puteins where Gene = 'Gag' and ChainId = " + firstChainId);
					StringBuffer sb = new StringBuffer();
					while (rs.next()) {
						if (sb.length() > 0) {
							sb.append(",");
						}
						sb.append(rs.getInt(1));
					}
					comd = "update retrotectortemp.Chains set GagPuteinid = " + BLIP + sb.toString() + BLIP + " where id = " + firstChainId;
					executeSQLCommand(comd);
					rs = executeSQLQuery("select id from retrotectortemp.Puteins where Gene = 'Pro' and ChainId = " + firstChainId);
					sb = new StringBuffer();
					while (rs.next()) {
						if (sb.length() > 0) {
							sb.append(",");
						}
						sb.append(rs.getInt(1));
					}
					executeSQLCommand("update retrotectortemp.Chains set ProPuteinid = " + BLIP + sb.toString() + BLIP + " where id = " + firstChainId);
					rs = executeSQLQuery("select id from retrotectortemp.Puteins where Gene = 'Pol' and ChainId = " + firstChainId);
					sb = new StringBuffer();
					while (rs.next()) {
						if (sb.length() > 0) {
							sb.append(",");
						}
						sb.append(rs.getInt(1));
					}
					executeSQLCommand("update retrotectortemp.Chains set PolPuteinid = " + BLIP + sb.toString() + BLIP + " where id = " + firstChainId);
					rs = executeSQLQuery("select id from retrotectortemp.Puteins where Gene = 'Env' and ChainId = " + firstChainId);
					sb = new StringBuffer();
					while (rs.next()) {
						if (sb.length() > 0) {
							sb.append(",");
						}
						sb.append(rs.getInt(1));
					}
					executeSQLCommand("update retrotectortemp.Chains set EnvPuteinid = " + BLIP + sb.toString() + BLIP + " where id = " + firstChainId);
					firstChainId++;
				}
				
// transfer to target database
				executeSQLCommand("insert into " + targetName + ".Chains select * from retrotectortemp.Chains");
				executeSQLCommand("insert into " + targetName + ".Puteins select * from retrotectortemp.Puteins");
				executeSQLCommand("insert into " + targetName + ".LTRs select * from retrotectortemp.LTRs");

			} // end of TablesPanel.ExportChainThread.goahead()
			
		} // end of TablesPanel.ExportChainThread
	

/**
* To handle export from PUTEINSPANEL to a genome database.
*/
		private class ExportPuteinThread extends ExportThread {
				
/**
* Exports selected rows in PUTEINSPANEL.MAINTABLE via retrotectortemp.
*/
			void goahead() throws RetroTectorException, SQLException {
				makeRetrotectortemp();
				String comd;
				int[ ] rownrs = PUTEINSPANEL.MAINTABLE.getSelectedOrAllRows();
				for (int i=0; i<rownrs.length; i++) {
					comd = "insert into retrotectortemp.Puteins select Puteins.*, LongComment, FinishedAt, RTVersion from Puteins, Chromosomes where Puteins.id = " + PUTEINSPANEL.MAINTABLE.getIntFromTable(rownrs[i] + 1, "id") + " and Chromosomes.Chromosome = Puteins.Chromosome";
					executeSQLCommand(comd);
				}
				
				ResultSet rs = executeSQLQuery("select max(id) from " + targetName + "." + PUTEINSNAME);
				int firstPuteinId = 1;
				if (rs.next()) {
					firstPuteinId = rs.getInt(1) + 1;
				}
				rs = executeSQLQuery("select id from retrotectortemp.Puteins");
				Stack st = new Stack();
				while (rs.next()) {
					st.push(new Integer(rs.getInt(1)));
				}
				int xi;
				for (int i=0; i<st.size(); i++) {
					xi = ((Integer) st.elementAt(i)).intValue();
					executeSQLCommand("update retrotectortemp.Puteins set id = " + firstPuteinId + " where id = " + xi);
					firstPuteinId++;
				}

				executeSQLCommand("update retrotectortemp.Puteins set ChainId = ''");
				executeSQLCommand("insert into " + targetName + ".Puteins select * from retrotectortemp.Puteins");

			} // end of TablesPanel.ExportPuteinThread.goahead()
			
		} // end of TablesPanel.ExportPuteinThread
		

/**
* To handle export from LTRSPANEL to a genome database.
*/
		private class ExportLTRThread extends ExportThread {
		
/**
* Exports selected rows in LTRSPANEL.MAINTABLE via retrotectortemp.
*/
			void goahead() throws RetroTectorException, SQLException {
				makeRetrotectortemp();
				String comd;
				int[ ] rownrs = LTRSPANEL.MAINTABLE.getSelectedOrAllRows();
				for (int i=0; i<rownrs.length; i++) {
					comd = "insert into retrotectortemp.LTRs select LTRs.*, LongComment, FinishedAt, RTVersion from LTRs, Chromosomes where LTRs.id = " + LTRSPANEL.MAINTABLE.getIntFromTable(rownrs[i] + 1, "id") + " and Chromosomes.Chromosome = LTRs.Chromosome";
					executeSQLCommand(comd);
				}
				
				ResultSet rs = executeSQLQuery("select max(id) from " + targetName + "." + LTRSNAME);
				int firstLTRId = 1;
				if (rs.next()) {
					firstLTRId = rs.getInt(1) + 1;
				}
				rs = executeSQLQuery("select id from retrotectortemp.LTRs");
				Stack st = new Stack();
				while (rs.next()) {
					st.push(new Integer(rs.getInt(1)));
				}
				int xi;
				for (int i=0; i<st.size(); i++) {
					xi = ((Integer) st.elementAt(i)).intValue();
					executeSQLCommand("update retrotectortemp.LTRs set id = " + firstLTRId + " where id = " + xi);
					firstLTRId++;
				}

				executeSQLCommand("update retrotectortemp.LTRs set InChain = -1");
				executeSQLCommand("insert into " + targetName + ".LTRs select * from retrotectortemp.LTRs");

			} // end of TablesPanel.ExportLTRThread.goahead()
			
		} // end of TablesPanel.ExportLTRThread
		
		
/**
* To display and handle contents of an SQLTable.
*/
		private class TablePanel extends JPanel implements MouseListener, ActionListener, ListSelectionListener {
		
/**
* Handles things to be done when selection in Chains table has changed.
*/
			private class ChainReactor extends Thread {
			
				private int[ ] selectedRows;
				
				private ChainReactor() {
					selectedRows = tablesPanel.CHAINSPANEL.MAINTABLE.getSelectedRows();
				} // end of TablesPanel.TablePanel.ChainReactor.constructor()
				
				public void run() {
					if ((selectedRows == null) || (selectedRows.length == 0)) {
						return;
					}
					StringBuffer com = new StringBuffer("select * from ");
					String cs = getColumnTitle("PuteinChainIdColumn");
					String l5 = getColumnTitle("ChainLTR5idColumn");
					String l3 = getColumnTitle("ChainLTR3idColumn");
					Object o;
					int id;
					int id1;
					int id2;
					com.append(PUTEINSNAME);
					com.append(" where (");
					com.append(cs);
					com.append("=");
					o = tablesPanel.CHAINSPANEL.MAINTABLE.getValueAt(selectedRows[0], 0);
					id = ((Integer) o).intValue();
					com.append(id);
					com.append(")");
					for (int i=1; i<selectedRows.length; i++) {
						com.append(" or (");
						com.append(cs);
						com.append("=");
						o = tablesPanel.CHAINSPANEL.MAINTABLE.getValueAt(selectedRows[i], 0);
						id = ((Integer) o).intValue();
						com.append(id);
						com.append(")");
					}
					String s = com.toString();
					tablesPanel.PUTEINSPANEL.updateTableModel(s);

					com = new StringBuffer("select * from ");
					com.append(LTRSNAME);
					try {
						id1 = tablesPanel.CHAINSPANEL.MAINTABLE.getIntFromTable(selectedRows[0] + 1, l5);
						id2 = tablesPanel.CHAINSPANEL.MAINTABLE.getIntFromTable(selectedRows[0] + 1, l3);
						com.append(" where (id=");
						com.append(id1);
						com.append(") or (id=");
						com.append(id2);
						com.append(")");
						for (int i=1; i<selectedRows.length; i++) {
							id1 = tablesPanel.CHAINSPANEL.MAINTABLE.getIntFromTable(selectedRows[i] + 1, l5);
							id2 = tablesPanel.CHAINSPANEL.MAINTABLE.getIntFromTable(selectedRows[i] + 1, l3);
							com.append(" or (id=");
							com.append(id1);
							com.append(") or (id=");
							com.append(id2);
							com.append(")");
						}
					} catch (SQLException se) {
						RetroTectorEngine.displayError(new RetroTectorException("Genomeview", se));
					}
					s = com.toString();
					tablesPanel.LTRSPANEL.updateTableModel(s);
				} // end of TablesPanel.TablePanel.ChainReactor.run()
				
			} // end of TablesPanel.TablePanel.ChainReactor
			

/**
* Handles things to be done when selection in Puteins table has changed.
*/
			private class PuteinReactor extends Thread {
			
				private int[ ] selectedRows;
				
				private PuteinReactor() {
					selectedRows = tablesPanel.PUTEINSPANEL.MAINTABLE.getSelectedRows();
				} // end of TablesPanel.TablePanel.PuteinReactor.constructor()
				
				public void run() {
					if ((selectedRows == null) || (selectedRows.length == 0)) {
						return;
					}
					StringBuffer com = new StringBuffer("select * from ");
					try {
						int id = tablesPanel.PUTEINSPANEL.MAINTABLE.getIntFromTable(selectedRows[0] + 1, "ChainId");
						com.append(CHAINSNAME);
						com.append(" where (id=");
						com.append(id);
						com.append(")");
						for (int i=1; i<selectedRows.length; i++) {
							id = tablesPanel.PUTEINSPANEL.MAINTABLE.getIntFromTable(selectedRows[i] + 1, "ChainId");
							com.append(" or (id=");
							com.append(id);
							com.append(")");
						}
					} catch (SQLException se) {
						RetroTectorEngine.displayError(new RetroTectorException("Genomeview", se));
					}
					String s = com.toString();
					tablesPanel.CHAINSPANEL.updateTableModel(s);
				} // end of TablesPanel.TablePanel.PuteinReactor.run()
				
			} // end of TablesPanel.TablePanel.PuteinReactor
		

/**
* Handles things to be done when selection in LTRs table has changed.
*/
			private class LTRReactor extends Thread {
			
				private int[ ] selectedRows;
				
				private LTRReactor() {
					selectedRows = tablesPanel.LTRSPANEL.MAINTABLE.getSelectedRows();
				} // end of TablesPanel.TablePanel.LTRReactor.constructor()
				
				public void run() {
					if ((selectedRows == null) || (selectedRows.length == 0)) {
						return;
					}
					StringBuffer com = new StringBuffer("select * from ");
					try {
						int id = tablesPanel.LTRSPANEL.MAINTABLE.getIntFromTable(selectedRows[0] + 1, "InChain");
						com.append(CHAINSNAME);
						com.append(" where (id=");
						com.append(id);
						com.append(")");
						for (int i=1; i<selectedRows.length; i++) {
							id = tablesPanel.LTRSPANEL.MAINTABLE.getIntFromTable(selectedRows[i] + 1, "InChain");
							com.append(" or (id=");
							com.append(id);
							com.append(")");
						}
					} catch (SQLException se) {
						RetroTectorEngine.displayError(new RetroTectorException("Genomeview", se));
					}
					String s = com.toString();
					tablesPanel.CHAINSPANEL.updateTableModel(s);
				} // end of TablesPanel.TablePanel.LTRReactor.run()
				
			} // end of TablesPanel.TablePanel.LTRReactor
		

			private final ExportThread EXPORTER;
			private final String SUBJECTSTRING; // CHAINSNAME, PUTEINSNAME or LTRSNAME
			private final OverviewPanel DISPLAYER; // OverviewPanel related to this one
				
			private final SQLTable MAINTABLE; // the actual display panel
			
			private JTextField infoField = new JTextField(18);
			private JButton showButton = new JButton();
			private JComboBox whereBox = new JComboBox();
			private JButton exportButton = new JButton();
			private JComboBox genomesBox = new JComboBox();
		
/**
* Constructor.
* @param	subject		CHAINSNAME, PUTEINSNAME or LTRSNAME.
* @param	disp			OverviewPanel to show histograms, or null.
*^@param	exp				ExportThread to handle export.
*/
			private TablePanel(String subject, OverviewPanel disp, ExportThread exp) throws SQLException {
			
				SUBJECTSTRING = subject;
				DISPLAYER = disp;
				EXPORTER = exp;
			
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				
				MAINTABLE = new SQLTable(VIEWWINDOW);
				MAINTABLE.addMouseListener(this);
				MAINTABLE.getSelectionModel().addListSelectionListener(this);
				if (DISPLAYER != null) {
					MAINTABLE.getSelectionModel().addListSelectionListener(DISPLAYER);
				}
				add(new JScrollPane(MAINTABLE));
				
				JPanel jp1 = new JPanel();

				infoField.setEditable(false);
				jp1.add(infoField);

				showButton.setText("Show " + SUBJECTSTRING + " where");
				showButton.addActionListener(this);
				jp1.add(showButton);

				whereBox.setEditable(true);
				whereBox.addItem("");
				ResultSet rs = executeSQLQuery("Select " + CHROMOSOME + " from " + CHROMOSOMESNAME);
				String s;
				while (rs.next()) {
					s = CHROMOSOME + "=" + BLIP + rs.getString(CHROMOSOME) + BLIP;
					whereBox.addItem(s);
				}
				jp1.add(whereBox);

				exportButton.setText("Export selected " + SUBJECTSTRING + " to");
				exportButton.addActionListener(this);
				jp1.add(exportButton);
				
				genomesBox.setEditable(true);
				genomesBox.addItem(TABDELIMLABEL);
				genomesBox.addItem(FASTALABEL);
				genomesBox.addItem(HTMLLABEL);
				genomesBox.addItem(STRUCTLABEL);
				genomesBox.addItem("");
				rs = executeSQLQuery("show databases");
				while (rs.next()) {
					genomesBox.addItem(rs.getString(1));
				}
				jp1.add(genomesBox);

				add(jp1);

			} // end of TablesPanel.TablePanel.constructor()
			

			private String infoText = "";
			
			private Runnable infoSetter = new Runnable() {
				public void run() {
					infoField.setText(infoText);
				}
			}; // end of infoSetter
			
// show actual number of rows and selected rows in infoField
			private final void updateInfo() {
				infoText = "" + MAINTABLE.getRowCount() + " rows, " + MAINTABLE.getSelectedRowCount() + " selected";
				AWTUtilities.doInEventThread(infoSetter);
			} // end of TablesPanel.TablePanel.updateInfo()

// execute command and clear commandExtension
			private final void updateTableModel(String command) {
				MAINTABLE.mainCommand = command;
				MAINTABLE.commandExtension = "";
				updateTableModel();
			} // end of TablesPanel.TablePanel.updateTableModel(String)
			
			private final void updateTableModel() {
				VIEWWINDOW.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					MAINTABLE.setNewModel();
				} catch (SQLException ex) {
					RetroTectorEngine.displayError(new RetroTectorException("Genomeview", ex));
				}
				if (DISPLAYER != null) {
					DISPLAYER.setSelectionChanged(true);
				}
				updateInfo();
				VIEWWINDOW.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} // end of TablesPanel.TablePanel.updateTableModel()
			
/**
* Reacts to clicks on showButton or exportButton.
*/
			public void actionPerformed(ActionEvent ae) {
				if (ae.getSource() == showButton) {
					MAINTABLE.commandExtension = "";
					if (((String) whereBox.getSelectedItem()).trim().equals("")) {
						MAINTABLE.mainCommand = "select * from " + SUBJECTSTRING;
					} else {
						MAINTABLE.mainCommand = "select * from " + SUBJECTSTRING + " where " + ((String) whereBox.getSelectedItem());
					}
					updateTableModel();
				} else if (ae.getSource() == exportButton) {
					EXPORTER.targetName = (String) genomesBox.getSelectedItem();
					EXPORTER.sourcePanel = this;
					Thread expThread = new Thread(EXPORTER);
					expThread.start();
				}
			} // end of TablesPanel.TablePanel.actionPerformed(ActionEvent)
		
/**
* As required by ListSelectionListener. Updates info field.
*/
			public void valueChanged(ListSelectionEvent ev) {
				updateInfo();
			} // end of TablesPanel.TablePanel.valueChanged(ListSelectionEvent)
		
/**
* As required by MouseListener. Handles clicks in MAINTABLE and related ChainOverviewPanel
*/
			public void mouseClicked(MouseEvent e) {
				String com = null;
				if (e.getSource() instanceof ChainOverviewPanel.ChromosomeChainOverview) { // from Chains overview panel
					ChainOverviewPanel.ChromosomeChainOverview c = (ChainOverviewPanel.ChromosomeChainOverview) e.getSource();
					int[ ] i1 = c.bin(e.getX() - 1);
					int[ ] i2 = c.bin(e.getX() + 1);
					com = "select * from " + CHAINSNAME + " where (" + CHROMOSOME + " = " + BLIP + c.CHRNAME + BLIP + ") and (((" + getColumnTitle("ChainStartColumn") + " + " + getColumnTitle("ChainEndColumn") + ") / 2) between " + i1[0] + " and " + i2[1] + ")";
					MAINTABLE.mainCommand = com;
					MAINTABLE.commandExtension = "";
					updateTableModel();
					tabPane.setSelectedComponent(tablesPanel);
				} else if (e.getSource() instanceof LTROverviewPanel.ChromosomeLTROverview) { // from LTRs pane
					LTROverviewPanel.ChromosomeLTROverview c = (LTROverviewPanel.ChromosomeLTROverview) e.getSource();
					int[ ] i1 = c.bin(e.getX() - 1);
					int[ ] i2 = c.bin(e.getX() + 1);
					com = "select * from " + LTRSNAME + " where (" + CHROMOSOME + " = " + BLIP + c.CHRNAME + BLIP + ") and (" + getColumnTitle("LTRHotspotPosColumn") + " between " + i1[0] + " and " + i2[1] + ")";
					MAINTABLE.mainCommand = com;
					MAINTABLE.commandExtension = "";
					updateTableModel();
					tabPane.setSelectedComponent(tablesPanel);
				} else if (e.getSource() == MAINTABLE) {
					Point p = e.getPoint();
					int rownr = MAINTABLE.rowAtPoint(p);
					Object o = MAINTABLE.getValueAt(MAINTABLE.rowAtPoint(p), MAINTABLE.columnAtPoint(p));
					if (SUBJECTSTRING.equals(CHAINSNAME)) {
						try {
							chainDetailsPanel.setChain(rownr + 1);
							graphInfoArea.setText();
							htmlPane.setText();
						} catch (SQLException se) {
							RetroTectorEngine.displayError(new RetroTectorException("Genomeview", se));
						} catch (RetroTectorException re) {
							RetroTectorEngine.displayError(re);
						}
						ChainReactor cr = new ChainReactor();
						cr.start();
					} else if (SUBJECTSTRING.equals(PUTEINSNAME)) { // from Puteins pane
						PuteinReactor cr = new PuteinReactor();
						cr.start();
					} else if (SUBJECTSTRING.equals(LTRSNAME)) { // from LTRs tab
						LTRReactor cr = new LTRReactor();
						cr.start();
					}
				}

			} // end of TablesPanel.TablePanel.mouseClicked(MouseEvent)

	/**
	* As required by MouseListener.
	*/
			public void mouseEntered(MouseEvent e) {
			} // end of TablesPanel.TablePanel.mouseEntered
			
	/**
	* As required by MouseListener.
	*/
			public void mouseExited(MouseEvent e) {
			} // end of TablesPanel.TablePanel.mouseExited
			
	/**
	* As required by MouseListener.
	*/
			public void mousePressed(MouseEvent e) {
			} // end of TablesPanel.TablePanel.mousePressed
			
	/**
	* As required by MouseListener.
	*/
			public void mouseReleased(MouseEvent e) {
			} // end of TablesPanel.TablePanel.mouseReleased
		
		} // end of TablesPanel.TablePanel
	

		private final TablePanel CHAINSPANEL;
		private final TablePanel PUTEINSPANEL;
		private final TablePanel LTRSPANEL;
		
		private TablesPanel() throws SQLException {
			setPreferredSize(TABDIMENSION);
			setLayout(new GridLayout(0, 1));

			CHAINSPANEL = new TablePanel(CHAINSNAME, chainOverviewPanel, new ExportChainThread());
			add(CHAINSPANEL);
			for (int i=0; i<chainOverviewPanel.chOvs.length; i++) {
				chainOverviewPanel.chOvs[i].addMouseListener(CHAINSPANEL);
			}
			PUTEINSPANEL = new TablePanel(PUTEINSNAME, null, new ExportPuteinThread());
			add(PUTEINSPANEL);
			LTRSPANEL = new TablePanel(LTRSNAME, ltrOverviewPanel, new ExportLTRThread());
			add(LTRSPANEL);
			for (int i=0; i<ltrOverviewPanel.chOvs.length; i++) {
				ltrOverviewPanel.chOvs[i].addMouseListener(LTRSPANEL);
			}
		} // end of TablesPanel.constructor()
		

	} // end of TablesPanel
	
	
/**
* Tab panel to show Chain contents graphically etc.
*/
	private class ChainDetailsPanel extends JPanel implements ActionListener {
	
/**
* For transfer of putein information to doPutein.
*/
		private class PuteinData {
		
			private final String GENE;
			private final int FIRST;
			private final int LAST;
			private final int STOPS;
			private final int SHIFTS;
			private final int ORFPOS;
			private final int ORFLENGTH;
			private final float SCORE;
			
			private PuteinData(ResultSet rs) throws SQLException {
				GENE = rs.getString(getColumnTitle("PuteinGeneColumn"));
				FIRST = useTranslator.internalize(rs.getInt(getColumnTitle("PuteinEstimatedFirstColumn")));
				LAST = useTranslator.internalize(rs.getInt(getColumnTitle("PuteinEstimatedLastColumn")));
				STOPS = rs.getInt(getColumnTitle("PuteinStopCodonsColumn"));
				SHIFTS = rs.getInt(getColumnTitle("PuteinShiftsColumn"));
				ORFPOS = useTranslator.internalize(rs.getInt(getColumnTitle("PuteinLongestORFPosColumn")));
				ORFLENGTH = rs.getString(getColumnTitle("PuteinLongestORFColumn")).length();
				SCORE = rs.getFloat(getColumnTitle("PuteinAverageScoreColumn"));
			} // end of ChainDetailsPanel.PuteinData.constructor()
			
		} // end of ChainDetailsPanel.PuteinData
	
			
/**
* Class to handle display of similarity between LTRs and repbase consensuses.
*/
		private class LTRData {
		
/**
* One tab in display.
*/
			private class TemplateHitsPanel extends JPanel {
		
/**
* For the actual drawing.
*/
				private class TemplateHitCanvas extends JCanvas {
				
					private final int LTRLEFTMARGIN = 10;
					private final int LTRRIGHTMARGIN = 10;
					private final int LTRTOPMARGIN = 10;
					private final int LTRBOTTOMMARGIN = 10;
			
					private int posbase; // lowest position involved
					private float posfactor; // pixels per position

					private TemplateHitCanvas() {
						setToolTipText("1");
						ttMan.registerComponent(this);
					} // end of ChainDetailsPanel.TemplateHitsPanel.TemplateHitCanvas.constructor()
			
/**
* As required by JComponent. Draws a rectangle for each hit, with diagonal,
* lower left-upper right if hit is in sense direction,
* lower roght - upper left otherwise.
*/
					public void paintComponent(Graphics g) {
				
						int wi = getWidth() - LTRLEFTMARGIN - LTRRIGHTMARGIN;
						posbase = LTRFIRST;
						posfactor = 1.0f * wi / (LTRLAST - LTRFIRST);
						yfactor = 1.0f * (getHeight() - LTRTOPMARGIN - LTRBOTTOMMARGIN) / MAXY;
						g.setColor(Color.black);
						int x;
						int y;
						int w;
						int h;
						for (int i=0; i<HITS.length; i++) {
							x = posInGraph(HITS[i].POSINTHEDNA);
							w = Math.max(1, posInGraph(HITS[i].POSINTHEDNA + HITS[i].HITLENGTH) - x);
							h = Math.round(HITS[i].HITLENGTH * yfactor);
							if (!SENSE) { // complementary strand
								y = LTRTOPMARGIN + Math.round(HITS[i].POSINTEMPLATE * yfactor);
								g.drawLine(x, y, x + w, y + h);
							} else {
								y = getHeight() - LTRBOTTOMMARGIN - Math.round((HITS[i].POSINTEMPLATE + HITS[i].HITLENGTH) * yfactor);
								g.drawLine(x, y + h, x + w, y);
							}
							g.drawRect(x, y, w, h);
						}
					} // end of ChainDetailsPanel.LTRData.TemplateHitsPanel.TemplateHitCanvas.paintComponent(Graphics)
				
/**
* @return	String with position in DNA and in template.
*/
					public final String getToolTipText(MouseEvent event) {
						int x = event.getX();
						if ((x < LTRLEFTMARGIN) | (x > getWidth() - LTRRIGHTMARGIN)) {
							return "";
						}
						int intx = Math.round((x - LTRLEFTMARGIN) / posfactor) + posbase;
						int extx = TRANSLATOR.externalize(intx);
						int y = event.getY();
						int exty;
						if (SENSE) {
							exty = Math.round((getHeight() - LTRBOTTOMMARGIN - y) / yfactor);
						} else {
							exty = Math.round((y - LTRTOPMARGIN) / yfactor);
						}
						return "" + extx + "," + exty;
					} //  end of ChainDetailsPanel.LTRData.TemplateHitsPanel.TemplateHitCanvas.getToolTipText(MouseEvent)
		
					private final int posInGraph(int posInDNA) {
						return LTRLEFTMARGIN + Math.round((posInDNA - posbase) * posfactor);
					} // end of ChainDetailsPanel.LTRData.TemplateHitsPanel.TemplateHitCanvas.posInGraph(int)
							
				} // end of ChainDetailsPanel.LTRData.TemplateHitsPanel.TemplateHitCanvas
		

/**
* Text in the tab.
*/
				public final String TITLE;

/**
* Hits for this particular template, in this direction.
*/
				public final Utilities.TemplateHit[ ] HITS;

/**
* Direction.
*/
				public final boolean SENSE;

/**
* Highest position in template reached by any hit.
*/
				public final int MAXY;

				private float yfactor;
				private TemplateHitCanvas canv = new TemplateHitCanvas();
		
/**
* @param	title	Text for tab, ending with '<' if in secondary strand.
* @param	st		Stack of Utilities.TemplateHit.
*/
				TemplateHitsPanel(String title, Stack st) {
					TITLE = title;
					if (TITLE.endsWith("<")) {
						SENSE = false;
					} else {
						SENSE = true;
					}
					HITS = new Utilities.TemplateHit[st.size()];
					st.copyInto(HITS);
					int maxy = 0;
					for (int i=0; i<HITS.length; i++) {
						maxy = Math.max(maxy, HITS[i].POSINTEMPLATE + HITS[i].HITLENGTH);
					}
					MAXY = maxy;
					setLayout(new BorderLayout());
					add(canv, BorderLayout.CENTER);
				} // end of constructor(String, Stack)
			
			} // end of ChainDetailsPanel.LTRData.TemplateHitsPanel

	
			private final int LTRFIRST;
			private final int LTRLAST;
			private final float SCOREFACTOR;
			private final String REPBASEFINDS;
			private final Utilities.TemplateHit[ ] TEMPLATEHITS;
			private final DNA.Translator TRANSLATOR;
			
			private LTRData(ResultSet rs) throws RetroTectorException {
				try {
					TRANSLATOR = new DNA.Translator(rs.getString(getColumnTitle("LTRInsertsColumn")));
					LTRFIRST = TRANSLATOR.internalize(rs.getInt(getColumnTitle("LTRFirstPosColumn")));
					LTRLAST = TRANSLATOR.internalize(rs.getInt(getColumnTitle("LTRLastPosColumn")));
					SCOREFACTOR = rs.getFloat(getColumnTitle("LTRScoreFactorColumn"));
					REPBASEFINDS = rs.getString(getColumnTitle("LTRRepBaseFindsColumn"));
					Stack ts = new Stack();
					int ind;
					String[ ] ss = Utilities.expandLines(REPBASEFINDS);
					if (ss[ss.length - 1].length() == 0) {
						TEMPLATEHITS = new Utilities.TemplateHit[ss.length - 1];
					} else {
						TEMPLATEHITS = new Utilities.TemplateHit[ss.length];
					}
					for (int i=0; i<TEMPLATEHITS.length; i++) {
						TEMPLATEHITS[i] = new Utilities.TemplateHit(ss[i], TRANSLATOR);
					}
				} catch (SQLException sqle) {
					throw new RetroTectorException("Genomeview", sqle);
				}
			} // end of ChainDetailsPanel.LTRData.constructor(ResultSet)
			
/**
* @return			JTabbedPane with one tab for each template with hits.
*/

			public final JTabbedPane getTabbedPane() {
				int tabcount = 0;
				detTabPane = new JTabbedPane();
				String su = "";
				Stack stu = null;
				for (int i=0; i<TEMPLATEHITS.length; i++) {
					if (TEMPLATEHITS[i].NAME.equals(su)) {
						stu.push(TEMPLATEHITS[i]);
					} else {
						if (stu != null) {
							detTabPane.add(su, new TemplateHitsPanel(su, stu));
							tabcount++;
						}
						stu = new Stack();
						stu.push(TEMPLATEHITS[i]);
						su = TEMPLATEHITS[i].NAME;
					}
				}
				if (stu != null) {
					detTabPane.add(su, new TemplateHitsPanel(su, stu));
					tabcount++;
				}
				if (tabcount == 0) {
					detTabPane.add("No RepBase template hits", new JPanel());
				}
				return detTabPane;
			} // end of ChainDetailsPanel.LTRData.getTabbedPane()
			
		} // end of ChainDetailsPanel.LTRData
	

/**
* Tab pane to display regions of similarity between Chain and forretrotector.repbasetemplates.
*/
		private class TemplateHitsPanel extends JPanel {
		
/**
* For the actual drawing.
*/
			private class TemplateHitCanvas extends JCanvas {
			
/**
* Top margin height.
*/
				public final int TOPMARGIN = 10;
		
/**
* Bottom margin height.
*/
				public final int BOTTOMMARGIN = 10;
		
				private int margin; // left margin inside this canvas
/**
* To display regions of similarity to forretrotector.repbasetemplates.
*/
				private TemplateHitCanvas() {
					setToolTipText("1");
					ttMan.registerComponent(this);
				} // end of ChainDetailsPanel.TemplateHitsPanel.TemplateHitCanvas.constructor()
			
/**
* As required by JComponent. Draws a rectangle for each hit, with diagonal,
* lower left-upper right if hit is in sense direction,
* lower roght - upper left otherwise.
*/
				public void paintComponent(Graphics g) {
				
// to get same dimension as chain graphics
					int wi = detailsCanvas.getWidth() - HOFFSET - RIGHTMARGIN;
					posbase = overallStart;
					posfactor = 1.0f * wi / (overallEnd - overallStart);
					margin = (detailsCanvas.getWidth() - getWidth()) / 2;
					yfactor = 1.0f * (getHeight() - TOPMARGIN - BOTTOMMARGIN) / MAXY;
					g.setColor(Color.black);
					int x;
					int y;
					int w;
					int h;
					for (int i=0; i<HITS.length; i++) {
						x = posInGraph(HITS[i].POSINTHEDNA) - margin;
						w = Math.max(1, posInGraph(HITS[i].POSINTHEDNA + HITS[i].HITLENGTH) - margin - x);
						h = Math.round(HITS[i].HITLENGTH * yfactor);
						if (!SENSE) {
							y = TOPMARGIN + Math.round(HITS[i].POSINTEMPLATE * yfactor);
							g.drawLine(x, y, x + w, y + h);
						} else {
							y = getHeight() - BOTTOMMARGIN - Math.round((HITS[i].POSINTEMPLATE + HITS[i].HITLENGTH) * yfactor);
							g.drawLine(x, y + h, x + w, y);
						}
						g.drawRect(x, y, w, h);
					}
				} // end of ChainDetailsPanel.TemplateHitsPanel.TemplateHitCanvas..paintComponent(Graphics)
				
/**
* @return	String with position in DNA and in template.
*/
				public final String getToolTipText(MouseEvent event) {
					int x = event.getX() + margin;
					if ((x < detailsCanvas.startPosInGraph) | (x > detailsCanvas.endPosInGraph)) {
						return "";
					}
					int intx = Math.round((x - HOFFSET) / posfactor) + posbase;
					int extx = useTranslator.externalize(intx);
					int y = event.getY();
					int exty;
					if (SENSE) {
						exty = Math.round((getHeight() - BOTTOMMARGIN - y) / yfactor);
					} else {
						exty = Math.round((y - TOPMARGIN) / yfactor);
					}
					return "" + extx + "," + exty;
				} //  end of ChainDetailsPanel.TemplateHitsPanel.TemplateHitCanvas.getToolTipText(MouseEvent)
		
			} // end of ChainDetailsPanel.TemplateHitsPanel.TemplateHitCanvas
		

/**
* Text in the tab.
*/
			public final String TITLE;

/**
* Hits for this particular template, in this direction.
*/
			public final Utilities.TemplateHit[ ] HITS;

/**
* Direction.
*/
			public final boolean SENSE;

/**
* Highest position in template reached by any hit.
*/
			public final int MAXY;

			private float yfactor;
			private TemplateHitCanvas canv = new TemplateHitCanvas();
		
/**
* @param	title	Text for tab, ending with '<' if in secondary strand.
* @param	st		Stack of Utilities.TemplateHit.
*/
			TemplateHitsPanel(String title, Stack st) {
				TITLE = title;
				if (TITLE.endsWith("<")) {
					SENSE = false;
				} else {
					SENSE = true;
				}
				HITS = new Utilities.TemplateHit[st.size()];
				st.copyInto(HITS);
				int maxy = 0;
				for (int i=0; i<HITS.length; i++) {
					maxy = Math.max(maxy, HITS[i].POSINTEMPLATE + HITS[i].HITLENGTH);
				}
				MAXY = maxy;
				setLayout(new BorderLayout());
				add(canv, BorderLayout.CENTER);
			} // end of ChainDetailsPanel.TemplateHitsPanel.constructor(String, Stack)
			
		} // end of ChainDetailsPanel.TemplateHitsPanel
	

/**
* For graphics of a Chain.
*/
		private class DetailsCanvas extends JCanvas implements MouseListener {
		
/**
* Basic height of rectangles.
*/
			public final int HE = 25;

/**
* Position of base line.
*/
 			public final int VOFFSET = 50;
			
			private final char GENUSCHAR;
			private final PuteinData GAGDATA;
			private final PuteinData PRODATA;
			private final PuteinData POLDATA;
			private final PuteinData ENVDATA;
			
			private LTRData ltr5Data;
			private LTRData ltr3Data;
			private Rectangle ltr5Rect;
			private Rectangle ltr3Rect;
						
			private int startPosInGraph;
			private int endPosInGraph;
		
			private int start = currentgraphinfo.FIRSTBASEPOS; // to be overall first position in DNA
			private int end = currentgraphinfo.LASTBASEPOS; // to be overall last position in DNA
			
			private AWTUtilities.MultiLiner multiLiner;
			
/**
* @param		gchar		Genus character.
*/
			private DetailsCanvas(char gchar) throws SQLException, RetroTectorException {
				setPreferredSize(new Dimension(TABDIMENSION.width, 150));
				addMouseListener(this);
				GENUSCHAR = gchar;
				String strand = tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowindex, getColumnTitle("StrandColumn"));

				int chainid = tablesPanel.CHAINSPANEL.MAINTABLE.getIntFromTable(rowindex, "id");
				int ltr5id = tablesPanel.CHAINSPANEL.MAINTABLE.getIntFromTable(rowindex, "LTR5id");
				int ltr3id = tablesPanel.CHAINSPANEL.MAINTABLE.getIntFromTable(rowindex, "LTR3id");
				ResultSet rs = executeSQLQuery("select * from " + PUTEINSNAME + " where " + getColumnTitle("PuteinGeneColumn") + " = " + BLIP + "Gag" + BLIP + " and " + getColumnTitle("PuteinGenusColumn") + " = " + BLIP + GENUSCHAR + BLIP + " and " + getColumnTitle("PuteinChainIdColumn") + " = " + chainid);
				if (rs.next()) {
					GAGDATA = new PuteinData(rs);
					start = Math.min(start, GAGDATA.FIRST);
					end = Math.max(end, GAGDATA.LAST);
				}	else {
					GAGDATA = null;
				}
				rs = executeSQLQuery("select * from " + PUTEINSNAME + " where " + getColumnTitle("PuteinGeneColumn") + " = " + BLIP + "Pro" + BLIP + " and " + getColumnTitle("PuteinGenusColumn") + " = " + BLIP + GENUSCHAR + BLIP + " and " + getColumnTitle("PuteinChainIdColumn") + " = " + chainid);
				if (rs.next()) {
					PRODATA = new PuteinData(rs);
					start = Math.min(start, PRODATA.FIRST);
					end = Math.max(end, PRODATA.LAST);
				}	else {
					PRODATA = null;
				}
				rs = executeSQLQuery("select * from " + PUTEINSNAME + " where " + getColumnTitle("PuteinGeneColumn") + " = " + BLIP + "Pol" + BLIP + " and " + getColumnTitle("PuteinGenusColumn") + " = " + BLIP + GENUSCHAR + BLIP + " and " + getColumnTitle("PuteinChainIdColumn") + " = " + chainid);
				if (rs.next()) {
					POLDATA = new PuteinData(rs);
					start = Math.min(start, POLDATA.FIRST);
					end = Math.max(end, POLDATA.LAST);
				}	else {
					POLDATA = null;
				}
				rs = executeSQLQuery("select * from " + PUTEINSNAME + " where (" + getColumnTitle("PuteinGeneColumn") + " = " + BLIP + "Env" + BLIP + " and " + getColumnTitle("PuteinGenusColumn") + " = " + BLIP + GENUSCHAR + BLIP + " and " + getColumnTitle("PuteinChainIdColumn") + " = " + chainid + ") or (" + getColumnTitle("PuteinGeneColumn") + " = " + BLIP + "ENV" + BLIP + " and " + getColumnTitle("PuteinGenusColumn") + " = " + BLIP + BLIP + " and " + getColumnTitle("PuteinChainIdColumn") + " = " + chainid + ")");
				if (rs.next()) {
					ENVDATA = new PuteinData(rs);
					start = Math.min(start, ENVDATA.FIRST);
					end = Math.max(end, ENVDATA.LAST);
				}	else {
					ENVDATA = null;
				}
				ltr5Data = null;
				if (ltr5id > 0) {
					rs = executeSQLQuery("select * from " + LTRSNAME + " where id=" + ltr5id);
					if (rs.next()) {
						ltr5Data = new LTRData(rs);
					}
				}
				ltr3Data = null;
				if (ltr3id > 0) {
					rs = executeSQLQuery("select * from " + LTRSNAME + " where id=" + ltr3id);
					if (rs.next()) {
						ltr3Data = new LTRData(rs);
					}
				}
				overallStart = Math.min(overallStart, start); // to inform containing ChainDetailsPanel
				overallEnd = Math.max(overallEnd, end); // to inform containing ChainDetailsPanel
				setToolTipText("1");
				ttMan.registerComponent(this);
			} // end of ChainDetailsPanel.DetailsCanvas.constructor(char)
			
/**
* As required by JComponent.
*/
			public void paintComponent(Graphics g) {
				if (rowindex < 0) { // no Chain set
					return;
				}
				int wi = getWidth() - HOFFSET - RIGHTMARGIN;
				posbase = overallStart;
				posfactor = 1.0f * wi / (overallEnd - overallStart);
				
				super.paintComponent(g); // make background white
				
				g.setColor(Color.black);
				g.drawLine(startPosInGraph = posInGraph(start), VOFFSET, endPosInGraph = posInGraph(end), VOFFSET);
				String s = String.valueOf(useTranslator.externalize(start));
				g.drawString(s, startPosInGraph - 2 - SwingUtilities.computeStringWidth(g.getFontMetrics(), s), VOFFSET);
				g.drawString(String.valueOf(useTranslator.externalize(end)), wi + HOFFSET + 3, VOFFSET);
				g.drawString("Genus: " + GENUSCHAR, 1, 20);
	
				if (hit5 == null) {
					ltr5Rect = null;
				} else {
					ltr5Rect = doLTR(g, hit5, "5'LTR");
				}
				
				doPutein(g, GAGDATA);
				doPutein(g, PRODATA);
				doPutein(g, POLDATA);
				doPutein(g, ENVDATA);

				if (hit3 == null) {
					ltr3Rect = null;
				} else {
					ltr3Rect = doLTR(g, hit3, "3'LTR");
				}
	
				multiLiner = new AWTUtilities.MultiLiner(100, VOFFSET + HE + g.getFontMetrics().getHeight() + 1, g);
				for (int r=0; r<motifHits.length; r++) {
					doHit(g, motifHits[r]);
				}

				g.setColor(Color.black);
				for (int co=0; co<(useTranslator.CONTIGS.length-1); co++) {
					int coo = useTranslator.CONTIGS[co].LASTINTERNAL;
					if ((coo >= start) & (coo <= end)) {
						Chainview.dashedVertical(g, 0, VOFFSET + 30, posInGraph(coo), 10);
					}
				}
				
			} // end of ChainDetailsPanel.DetailsCanvas.paintComponent(Graphics)
			
			private final void doHit(Graphics g, MotifHitGraphInfo hit) {
				if ((hit == hit5) | (hit == hit3)) {
					return;
				}
				int x = posInGraph(hit.FIRSTPOS);
				int h = Math.max(1, Math.round(1.0f * HE * hit.SCORE / hitscoremax));
				int y = VOFFSET + 1;
				int w = Math.round((hit.LASTPOS - hit.FIRSTPOS) * posfactor);
				String s = hit.MOTIFGROUP;
				if (Gene.getGeneOf(s) != null) {
					s = Gene.getGeneOf(s).GENENAME;
					g.setColor(geneColor(s));
				} else {
					g.setColor(Color.black);
				}
				g.fillRect(x, y, w, h);
				g.setColor(Color.black);
				multiLiner.enterString(hit.MOTIFGROUP, x);
			} // end of ChainDetailsPanel.DetailsCanvas.doHit(Graphics, MotifHitGraphInfo)
			
			private final Rectangle doLTR(Graphics g, MotifHitGraphInfo hit, String title) {
				int x = posInGraph(hit.FIRSTPOS);
				int h = HE;
				int y = VOFFSET - h;
				int w = Math.round((hit.LASTPOS - hit.FIRSTPOS) * posfactor);
				Rectangle r = new Rectangle(x, y, w, h);
				g.setColor(AWTUtilities.setAlpha(Color.blue, 128));
				g.fillRect(x, y, w, h);
				g.setColor(Color.black);
				g.drawString(title, x, 15);
				g.drawRect(x, y, w, h);
				x = posInGraph(hit.HOTSPOT);
				g.setColor(Color.yellow);
				g.drawLine(x, y, x, y + h);
				return r;
			} // end of ChainDetailsPanel.DetailsCanvas.doLTR(Graphics, MotifHitGraphInfo, String)
			
			private final void doPutein(Graphics g, PuteinData put) {
				if (put == null) {
					return;
				}
				int x = posInGraph(put.FIRST);
				int h = Math.round((put.SCORE - 0.5f) * 2 * HE);
				int y = VOFFSET;
				if (h >= 0) {
					y = VOFFSET - h;
				} else {
					h = -h / 2;
				}
				int w = Math.round((put.LAST - put.FIRST) * posfactor);
				g.setColor(geneColor(put.GENE));
				g.fillRect(x, y, w, h);
				g.setColor(Color.black);
				g.drawRect(x, y, w, h);
				if (y == VOFFSET) {
					g.drawString(put.GENE, x, y + h - 2);
				} else {
					g.drawString(put.GENE, x, y - 2);
				}
				if (put.SHIFTS + put.STOPS > 2) {
					AWTUtilities.hatchRect(g, new Rectangle(x, y, w, h), Color.black, 10);
				}
				x = posInGraph(put.ORFPOS);
				h = h / 2;
				y = VOFFSET - h;
				w = Math.round(put.ORFLENGTH * 3 * posfactor);
				g.fillRect(x, y, w, h);
				return;
			} // end of ChainDetailsPanel.DetailsCanvas.doPutein(Graphics, PuteinData)
			
/**
* @return	String with position in DNA.
*/
			public String getToolTipText(MouseEvent event) {
				int x = event.getX();
				if ((x < startPosInGraph) | (x > endPosInGraph)) {
					return "";
				}
				int intx = Math.round((x - HOFFSET) / posfactor) + posbase;
				int extx = useTranslator.externalize(intx);
				return String.valueOf(extx);
			} //  end of ChainDetailsPanel.DetailsCanvas.getToolTipText(MouseEvent)
		
/**
* As required by MouseListener. Reacts if click in LTR.
*/
			public void mouseClicked(MouseEvent e) {
				int locv = 300;
				int loch = 300;
				String s;
				CloseableJFrame theFrame;
				Point p = getLocationOnScreen();
				p.translate(e.getX(), e.getY());
				if (ltr5Rect.contains(e.getPoint())) {
					theFrame = new CloseableJFrame("5'LTR");
					theFrame.getContentPane().add(new JLabel("ScoreFactor: " + ltr5Data.SCOREFACTOR), BorderLayout.NORTH);
					theFrame.getContentPane().add(ltr5Data.getTabbedPane());
					theFrame.setLocation(p);
					AWTUtilities.showFrame(theFrame, new Dimension(loch, locv));
				}
				if (ltr3Rect.contains(e.getPoint())) {
					p.translate(-loch, 0);
					theFrame = new CloseableJFrame("3'LTR");
					theFrame.getContentPane().add(new JLabel("ScoreFactor: " + ltr3Data.SCOREFACTOR), BorderLayout.NORTH);
					theFrame.getContentPane().add(ltr3Data.getTabbedPane());
					theFrame.setLocation(p);
					AWTUtilities.showFrame(theFrame, new Dimension(loch, locv));
				}
			} // end of ChainDetailsPanel.DetailsCanvas.mouseClicked(MouseEvent)

	/**
	* As required by MouseListener.
	*/
			public void mouseEntered(MouseEvent e) {
			} // end of ChainDetailsPanel.DetailsCanvas.mouseEntered
			
	/**
	* As required by MouseListener.
	*/
			public void mouseExited(MouseEvent e) {
			} // end of ChainDetailsPanel.DetailsCanvas.mouseExited
			
	/**
	* As required by MouseListener.
	*/
			public void mousePressed(MouseEvent e) {
			} // end of ChainDetailsPanel.DetailsCanvas.mousePressed
			
	/**
	* As required by MouseListener.
	*/
			public void mouseReleased(MouseEvent e) {
			} // end of ChainDetailsPanel.DetailsCanvas.mouseReleased

		} // end of ChainDetailsPanel.DetailsCanvas
		

/**
* For display of RV from forretrotector.refrvs.
*/
	private class RefRVCanvas  extends JCanvas {
		
			private String ervname = null;
			private int ervstartpos;
			private int ervendpos;
			private int ltr5startpos;
			private int ltr5hotspotpos;
			private int ltr5endpos;
			private int rstartpos;
			private int rendpos;
			private int pbsstartpos;
			private int pbsendpos;
			private int gagstartpos;
			private int gagendpos;
			private int prostartpos;
			private int proendpos;
			private int polstartpos;
			private int polendpos;
			private int envstartpos;
			private int envendpos;
			private int pptstartpos;
			private int pptendpos;
			private int ltr3startpos;
			private int ltr3endpos;
			private int refoffset;
			
/**
* Constructor.
* @param		rs				ResultSet from forretrotector.refrvs
* @param		refoffset	Displacement between centers of hit and ref virus.
* @param		perc			Percentage similarity.
*/
			RefRVCanvas(ResultSet rs, int refoffset, String perc) throws SQLException {
				this.refoffset = refoffset;
				setPreferredSize(new Dimension(TABDIMENSION.width, 50));
				ervstartpos = 1;
				if (rs.next()) {
					currentHTMLtext = rs.getString("html");
					ervname = rs.getString("definition") + " " + perc;
					ervendpos = rs.getInt("SEQLEN");
					ltr5startpos = rs.getInt("LTR5POS1");
//					ltr5hotspotpos = rs.getInt("AATAAAPOS"); // A very treacherous field
					ltr5endpos = rs.getInt("LTR5POS2");
					rstartpos = rs.getInt("RPOS1");
					rendpos = rs.getInt("RPOS2");
					pbsstartpos = rs.getInt("PBSPOS1");
					pbsendpos = rs.getInt("PBSPOS2");
					gagstartpos = rs.getInt("GAGPOS1");
					gagendpos = rs.getInt("GAGPOS2");
					prostartpos = rs.getInt("PROPOS1");
					proendpos = rs.getInt("PROPOS2");
					polstartpos = rs.getInt("POLPOS1");
					polendpos = rs.getInt("POLPOS2");
					envstartpos = rs.getInt("ENVPOS1");
					envendpos = rs.getInt("ENVPOS2");
					pptstartpos = rs.getInt("PPTPOS1");
					pptendpos = rs.getInt("PPTPOS2");
					ltr3startpos = rs.getInt("LTR3POS1");
					ltr3endpos = rs.getInt("LTR3POS2");
				}
				setToolTipText("1");
				ttMan.registerComponent(this);
			} // end of ChainDetailsPanel.RefRVCanvas.constructor(ResultSet, int, String)
			
			private final int ervposInGraph(int posInErv) {
				return HOFFSET + Math.round((posInErv - refoffset - 1) * posfactor);
			} // end of ChainDetailsPanel.RefRVCanvas.ervposInGraph(int)
				
/**
* As required by JComponent.
*/
			public void paintComponent(Graphics g) {
				if (ervname == null) {
					return;
				}
				int wi = detailsCanvas.getWidth() - HOFFSET - RIGHTMARGIN;
				posbase = overallStart;
				posfactor = 1.0f * wi / (overallEnd - overallStart);
				super.paintComponent(g);
				int voffset = 40;
				int x, y, w, h;
				if ((ltr5startpos > 0) & (ltr5endpos > 0)) {
					x = ervposInGraph(ltr5startpos);
					y = 20;
					w = ervposInGraph(ltr5endpos) - x;
					h = 19;
					g.setColor(AWTUtilities.setAlpha(Color.blue, 128));
					g.fillRect(x, y, w, h);
					g.setColor(Color.black);
					g.drawRect(x, y, w, h);
				}
				if (ltr5hotspotpos > 0) {
					g.setColor(Color.yellow);
					x = ervposInGraph(ltr5hotspotpos);
					g.drawLine(x, 20, x, voffset);
				}
				if (rstartpos > 0) {
					g.setColor(Color.yellow);
					x = ervposInGraph(rstartpos);
					g.drawLine(x, 20, x, voffset);
				}
				if (rendpos > 0) {
					g.setColor(Color.yellow);
					x = ervposInGraph(rendpos);
					g.drawLine(x, 20, x, voffset);
				}
				if ((pbsstartpos > 0) & (pbsendpos > 0)) {
					x = ervposInGraph(pbsstartpos);
					y = 25;
					w = ervposInGraph(pbsendpos) - x;
					h = 14;
					g.setColor(Color.black);
					g.fillRect(x, y, w, h);
					g.drawRect(x, y, w, h);
				}
				if ((gagstartpos > 0) & (gagendpos > 0)) {
					x = ervposInGraph(gagstartpos);
					y = 20;
					w = ervposInGraph(gagendpos) - x;
					h = 19;
					g.setColor(geneColor("Gag"));
					g.fillRect(x, y, w, h);
					g.setColor(Color.black);
					g.drawRect(x, y, w, h);
				}
				if ((prostartpos > 0) & (proendpos > 0)) {
					x = ervposInGraph(prostartpos);
					y = 25;
					w = ervposInGraph(proendpos) - x;
					h = 14;
					g.setColor(geneColor("Pro"));
					g.fillRect(x, y, w, h);
					g.setColor(Color.black);
					g.drawRect(x, y, w, h);
				}
				if ((polstartpos > 0) & (polendpos > 0)) {
					x = ervposInGraph(polstartpos);
					y = 20;
					w = ervposInGraph(polendpos) - x;
					h = 19;
					g.setColor(geneColor("Pol"));
					g.fillRect(x, y, w, h);
					g.setColor(Color.black);
					g.drawRect(x, y, w, h);
				}
				if ((envstartpos > 0) & (envendpos > 0)) {
					x = ervposInGraph(envstartpos);
					y = 25;
					w = ervposInGraph(envendpos) - x;
					h = 14;
					g.setColor(geneColor("Env"));
					g.fillRect(x, y, w, h);
					g.setColor(Color.black);
					g.drawRect(x, y, w, h);
				}
				if ((pptstartpos > 0) & (pptendpos > 0)) {
					x = ervposInGraph(pptstartpos);
					y = 25;
					w = ervposInGraph(pptendpos) - x;
					h = 14;
					g.setColor(Color.black);
					g.fillRect(x, y, w, h);
					g.drawRect(x, y, w, h);
				}
				if ((ltr3startpos > 0) & (ltr3endpos > 0)) {
					x = ervposInGraph(ltr3startpos);
					y = 20;
					w = ervposInGraph(ltr3endpos) - x;
					h = 19;
					g.setColor(AWTUtilities.setAlpha(Color.blue, 128));
					g.fillRect(x, y, w, h);
					g.setColor(Color.black);
					g.drawRect(x, y, w, h);
				}
				g.setColor(Color.black);
				g.drawLine(ervposInGraph(ervstartpos), voffset, ervposInGraph(ervendpos), voffset);
				g.drawString(ervname, 2, 15);
			} // end of ChainDetailsPanel.RefRVCanvas.paintComponent(Graphics)
			
/**
* @return	String with position in reference sequence.
*/
			public String getToolTipText(MouseEvent event) {
				int x = event.getX();
				if ((x < ervposInGraph(ervstartpos)) | (x > ervposInGraph(ervendpos))) {
					return "";
				}
				int intx = Math.round((x - HOFFSET) / posfactor) + refoffset + 1;
				return String.valueOf(intx);
			} //  end of ChainDetailsPanel.RefRVCanvas.getToolTipText(MouseEvent)
		
		} // end of ChainDetailsPanel.RefRVCanvas
		

/**
* Left margin width.
*/
		public final int HOFFSET = 100;
		
/**
* Right margin width.
*/
		public final int RIGHTMARGIN = 100;

		private int rowindex = -1; // index (1-based) of this Chain in CHAINSPANEL.MAINTABLE 
	
		private String directoryName;
		private String subDirectoryName;
		private String chainsFileName;
		private ChainGraphInfo currentgraphinfo;
		private String currentgraphinfotext;
		private String currentHTMLtext;

		private MotifHitGraphInfo hit5 = null;
		private MotifHitGraphInfo hit3 = null;
		
		private int overallStart = Integer.MAX_VALUE;
		private int overallEnd = 0;
		
		private MotifHitGraphInfo[ ] motifHits;
		private int hitscoremax;

		private DNA.Translator useTranslator;
		
		private Utilities.TemplateHit[ ] templateHits;

		private JTabbedPane detTabPane;
		
		private int posbase; // lowest position involved
		private float posfactor; // pixels per position
		private DetailsCanvas detailsCanvas;

		private ChainDetailsPanel() {
			setPreferredSize(TABDIMENSION);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		} // end of ChainDetailsPanel.constructor()

		private final int posInGraph(int posInDNA) {
			return HOFFSET + Math.round((posInDNA - posbase) * posfactor);
		} // end of ChainDetailsPanel.DetailsCanvas.posInGraph(int)
				
		private final Color geneColor(String geneName) {
			if (geneName == null) {
				return Color.black;
			} else if (geneName.equalsIgnoreCase("Gag")) {
				return AWTUtilities.setAlpha(Color.yellow, 128);
			} else if (geneName.equalsIgnoreCase("Pro")) {
				return AWTUtilities.setAlpha(Color.red, 128);
			} else if (geneName.equalsIgnoreCase("Pol")) {
				return AWTUtilities.setAlpha(Color.green, 128);
			} else if (geneName.equalsIgnoreCase("Env")) {
				return AWTUtilities.setAlpha(Color.orange, 128);
			}
			return Color.black;
		} // end of ChainDetailsPanel.DetailsCanvas.geneColor(String)
			
		private final void setChain(int rowIndex) throws SQLException, RetroTectorException {
			rowindex = rowIndex;
			String chromosomeName = tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowIndex, CHROMOSOME);
			String templateHitsS = tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowIndex, getColumnTitle("ChainRepBaseFindsColumn"));
			subDirectoryName = tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowIndex, getColumnTitle("SubDirectoryColumn"));
			chainsFileName = tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowIndex, getColumnTitle("ChainsFileNameColumn"));
			ResultSet rs = executeSQLQuery("select " + getColumnTitle("DirectoryColumn") + " from " + CHROMOSOMESNAME + " where " + CHROMOSOME + " = " + BLIP + chromosomeName + BLIP);
			rs.next();
			directoryName = rs.getString(getColumnTitle("DirectoryColumn"));
			useTranslator = new DNA.Translator(tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowIndex, getColumnTitle("ChainInsertsColumn")));
			String[ ] ss = Utilities.expandLines(templateHitsS);
			if (ss[ss.length - 1].length() == 0) { // last line may be empty
				templateHits = new Utilities.TemplateHit[ss.length - 1];
			} else {
				templateHits = new Utilities.TemplateHit[ss.length];
			}
			for (int i=0; i<templateHits.length; i++) {
				templateHits[i] = new Utilities.TemplateHit(ss[i], useTranslator);
			}
			currentgraphinfotext = tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowIndex, getColumnTitle("ChainInfoColumn"));
			currentgraphinfo = new ChainGraphInfo(Utilities.expandLines(currentgraphinfotext), useTranslator);
			Stack gst = new Stack();
			MotifHitGraphInfo inf;
			hitscoremax = 0;
			for (int gr=0; gr<currentgraphinfo.subgeneinfo.length; gr++) {
				for (int hi=0; hi<currentgraphinfo.subgeneinfo[gr].motifhitinfo.length; hi++) {
					inf = currentgraphinfo.subgeneinfo[gr].motifhitinfo[hi];
					gst.push(inf);
				}
			}
			motifHits = new MotifHitGraphInfo[gst.size()];
			gst.copyInto(motifHits);
			hit5 = null;
			hit3 = null;
			for (int gt=0; gt<motifHits.length; gt++) {
				if (database.getMotifGroup(motifHits[gt].MOTIFGROUP).SUBGENE.SUBGENENAME.equals(LTR5KEY)) {
					hit5 = motifHits[gt];
				} else if (database.getMotifGroup(motifHits[gt].MOTIFGROUP).SUBGENE.SUBGENENAME.equals(LTR3KEY)) {
					hit3 = motifHits[gt];
				} else {
					hitscoremax = Math.max(hitscoremax, motifHits[gt].SCORE);
				}
			}
			
			removeAll();
			overallStart = Integer.MAX_VALUE;
			overallEnd = 0;
			JButton chainViewButton;
			JPanel buttonPanel;
			for (int i=0; i<currentgraphinfo.GENUS.length(); i++) {
				add((detailsCanvas = new DetailsCanvas(currentgraphinfo.GENUS.charAt(i))));
				buttonPanel = new JPanel();
				buttonPanel.setPreferredSize(new Dimension(1, 1));
				buttonPanel.add(new AWTUtilities.SaveGraphicsButton(new DetailsCanvas(currentgraphinfo.GENUS.charAt(i))));
				chainViewButton = new JButton("Open in Chainview");
				chainViewButton.addActionListener(this);
				buttonPanel.add(chainViewButton);
				try {
					String comm = tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowIndex, getColumnTitle("CommentColumn"));
					if ((comm != null) && (comm.length() > 0)) {
						buttonPanel.add(new AWTUtilities.CommentButton(comm));
					}
				} catch (SQLException e) {
				}
				add(buttonPanel);
			}
				
			int tabcount = 0;
			String brv = null;
			int ind;
			try {
				brv = tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowIndex, getColumnTitle("ChainBestRefRVColumn")).trim();
			} catch (SQLException sq) {
				brv = tablesPanel.CHAINSPANEL.MAINTABLE.getStringFromTable(rowIndex, "BestRefERV"); // for backward compatibility
			}
			if (brv.length() > 0) {
				ind = brv.lastIndexOf(" ");
				int refrvoffset = Utilities.decodeInt(brv.substring(ind + 1)) - currentgraphinfo.FIRSTBASEPOS + overallStart;
				brv = brv.substring(0, ind).trim();
				ind = brv.lastIndexOf(" ");
				String percent = brv.substring(ind + 1);
				if (percent.endsWith("%")) {
					brv = brv.substring(0, ind).trim();
				} else {
					percent = "";
				}
				rs = forretrotectorStatement.executeQuery("select * from refrvs where definition = " + BLIP + brv + BLIP);
				add(new RefRVCanvas(rs, refrvoffset, percent));
				rs.beforeFirst();
				add(new AWTUtilities.SaveGraphicsButton(new RefRVCanvas(rs, refrvoffset, percent)));
				addCommentButton(this, rs);
			}
			
			detTabPane = new JTabbedPane();
			String su = "";
			Stack stu = null;
			for (int i=0; i<templateHits.length; i++) {
				if (templateHits[i].NAME.equals(su)) {
					stu.push(templateHits[i]);
				} else {
					if (stu != null) {
						detTabPane.add(su, new TemplateHitsPanel(su, stu));
						tabcount++;
					}
					stu = new Stack();
					stu.push(templateHits[i]);
					su = templateHits[i].NAME;
				}
			}
			if (stu != null) {
				detTabPane.add(su, new TemplateHitsPanel(su, stu));
				tabcount++;
			}
			if (tabcount == 0) {
				detTabPane.add("No RepBase template hits", new JPanel());
			}
			add(detTabPane);
		} // end of ChainDetailsPanel.setChain(int)
				
/**
* Activates Chainview.
*/
		public void actionPerformed(ActionEvent ae) {
			(new ChainviewActivator()).start();
		} // end of ChainDetailsPanel.actionPerformed(ActionEvent)
		
/**
* To react to Chainview button.
*/
		private class ChainviewActivator extends Thread {
		
			public void run() {
				File dir;
				try {
					try {
						dir = Utilities.getReadFile(directoryName + subDirectoryName + "/");
					} catch (RetroTectorException re) {
						ZFileChooser.ZFileDialog zd = new ZFileChooser.ZFileDialog(RetroTectorEngine.currentDirectory(), true);
						if ((dir = zd.getChosenFile()) == null) {
							return;
						}
						directoryName = dir.getPath();
						dir = Utilities.getReadFile(directoryName + subDirectoryName + "/");
					}
					RetroTectorEngine.setCurrentDirectory(dir, "swps");
					Chainview chv = new Chainview();
					chv.getParameterTable().put(INPUTFILEKEY, chainsFileName);
					chv.runFlag = true;
					chv.execute();
				} catch (RetroTectorException rte) {
					RetroTectorEngine.displayError(rte);
				}
			} // end of ChainDetailsPanel.ChainviewActivator.run()
			
		} // end of ChainDetailsPanel.ChainviewActivator
		
	} // end of ChainDetailsPanel
	

/**
* To display ChainGraphInfo.
*/
	private class GraphInfoArea extends JTextArea {
		
		private void setText() {
			setText(chainDetailsPanel.currentgraphinfotext);
		} // end of GraphInfoArea.setText()
		
	} // end of GraphInfoArea
	

/**
* To display description ov reference RV
.
*/
	private class HTMLPane extends JEditorPane {
		
		private void setText() {
			setContentType("text/html");
			String s = chainDetailsPanel.currentHTMLtext;
			if ((s != null) && (s.length() > 0)) {
				int ind = s.indexOf("<PRE>");
				s = "<HTML>" + s.substring(ind);
				ind = s.indexOf("</PRE>");
				s = s.substring(0, ind) + "</PRE></HTML>";
				setText(s);
			} else {
				setText("");
			}
		} // end of HTMLPane.setText()
		
	} // end of HTMLPane
	
	
/**
* Standard dimension of table.
*/
	public final Dimension TABDIMENSION = new Dimension(950, 550);

/**
* Width of empty left margin.
*/
	public final int LEFTMARGIN = 100;

/**
* Width of empty right margin.
*/
	public final int RIGHTMARGIN = 100;

/**
* Height of empty top margin.
*/
	public final int VERTMARGIN = 5;
	
/**
* = "FASTA file".
*/
	public final String FASTALABEL = "FASTA file";
	
/**
* = "Tab-delimited file".
*/
	public final String TABDELIMLABEL = "Tab-delimited file";
	
/**
* = "HTML file".
*/
	public final String HTMLLABEL = "HTML file";
	
/**
* = "Structured text file".
*/
	public final String STRUCTLABEL = "Structured text file";
	
/**
* = "Wider".
*/
	public final String WIDER = "Wider";
	
/**
* = "Narrower".
*/
	public final String NARROWER = "Narrower";
	

	private final CloseableJFrame VIEWWINDOW = new CloseableJFrame("");

	private ToolTipManager ttMan = ToolTipManager.sharedInstance();
	
	private String currentGenomeName;
	
	private JTabbedPane tabPane = new JTabbedPane();
	
	private FirstPanel firstPanel;
	private ChainOverviewPanel chainOverviewPanel;
	private LTROverviewPanel ltrOverviewPanel;
	private TablesPanel tablesPanel;
	private ChainDetailsPanel chainDetailsPanel;
	private GraphInfoArea graphInfoArea;
	private HTMLPane htmlPane;
	
	private Database database = RetroTectorEngine.getCurrentDatabase();

/**
* Standard Executor constructor without parameters.
*/
	public Genomeview() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2010 01 16";
  } // end of version()
	
/**
* Dummy.
*/
	public void initialize(ParameterFileReader script) {
		runFlag = true;
	} // end of initialize(ParameterFileReader)
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		ttMan.setInitialDelay(1);
		ttMan.setDismissDelay(100000);
		connect();
		try {
			tabPane.addTab("Choose genome", firstPanel = new FirstPanel());
		} catch (SQLException e) {
			haltError(e);
		}
		VIEWWINDOW.getContentPane().add(tabPane, BorderLayout.CENTER);
		AWTUtilities.showFrame(VIEWWINDOW, new Dimension(TABDIMENSION.width + 100, TABDIMENSION.height + 100));
		return "";
	} // end of execute()
	
	private void addCommentButton(Container cont, ResultSet rs) {
		try {
			String comm = rs.getString("comment");
			if ((comm != null) && (comm.length() > 0)) {
				cont.add(new AWTUtilities.CommentButton(comm));
			}
		} catch (SQLException e) {
		}
	} // end of addCommentButton(Container, ResultSet)
	
} // end of Genomeview
