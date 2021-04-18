import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;

@SuppressWarnings("serial")
public class JDBCMainWindowContent extends JInternalFrame implements ActionListener {
	String cmd = null;

	// DB Connectivity Attributes
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;

	private Container content;

	private JPanel detailsPanel;
	private JPanel exportButtonPanel;
	// private JPanel exportConceptDataPanel;
	private JScrollPane dbContentsPanel;

	private Border lineBorder;

	private JLabel IDLabel = new JLabel("ID:");
	private JLabel userName = new JLabel("UserName:");
	private JLabel Rank = new JLabel("Rank:");
	private JLabel dateCreated = new JLabel("Date:");
	private JLabel hasPenalties = new JLabel("Penalties:");
	private JLabel riotPoints = new JLabel("Rp:");
	private JLabel mostPlayedServer = new JLabel("Server:");

	private JTextField IDTF = new JTextField(10);
	private JTextField userNameTxt = new JTextField(10);
	private JTextField rankTxt = new JTextField(10);
	private JTextField dateTxt = new JTextField(10);
	private JTextField penaltiesTxt = new JTextField(10);
	private JTextField rpTxt = new JTextField(10);
	private JTextField serverTxt = new JTextField(10);

	private static QueryTableModel TableModel = new QueryTableModel();
	// Add the models to JTabels
	private JTable TableofDBContents = new JTable(TableModel);
	// Buttons for inserting, and updating members
	// also a clear button to clear details panel
	private JButton updateButton = new JButton("Update");
	private JButton insertButton = new JButton("Insert");
	private JButton exportButton = new JButton("Export");
	private JButton deleteButton = new JButton("Delete");
	private JButton clearButton = new JButton("Clear");
	private JButton euwTable = new JButton("EUW Table");
	private JButton eune = new JButton("EUNE Table");
	private JButton euwRiotIncrease = new JButton("Inc_points");

	private JButton numNoPenatlies = new JButton("Number Of Players With/No Penalties:");
	private JTextField noPenTxt = new JTextField(12);
	private JButton deletePenalties = new JButton("Delete on Penalties");
	private JTextField deletePenTxt = new JTextField(12);
	private JButton listOfRanks = new JButton("All Ranks On The Server");
	private JButton euwHasEune = new JButton("List all EUW players on EUNE");

	public JDBCMainWindowContent(String aTitle) {
		// setting up the GUI
		super(aTitle, false, false, false, false);
		setEnabled(true);

		initiate_db_conn();
		// add the 'main' panel to the Internal Frame
		content = getContentPane();
		content.setLayout(null);
		content.setBackground(Color.lightGray);
		lineBorder = BorderFactory.createEtchedBorder(15, Color.red, Color.black);

		// setup details panel and add the components to it
		detailsPanel = new JPanel();
		detailsPanel.setLayout(new GridLayout(11, 2));
		detailsPanel.setBackground(Color.lightGray);
		detailsPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "CRUD Actions"));

		detailsPanel.add(IDLabel);
		detailsPanel.add(IDTF);
		detailsPanel.add(userName);
		detailsPanel.add(userNameTxt);
		detailsPanel.add(Rank);
		detailsPanel.add(rankTxt);
		detailsPanel.add(dateCreated);
		detailsPanel.add(dateTxt);
		detailsPanel.add(hasPenalties);
		detailsPanel.add(penaltiesTxt);
		detailsPanel.add(riotPoints);
		detailsPanel.add(rpTxt);
		detailsPanel.add(mostPlayedServer);
		detailsPanel.add(serverTxt);

		// setup details panel and add the components to it
		exportButtonPanel = new JPanel();
		exportButtonPanel.setLayout(new GridLayout(3, 2));
		exportButtonPanel.setBackground(Color.lightGray);
		exportButtonPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Export Data"));
		exportButtonPanel.add(numNoPenatlies);
		exportButtonPanel.add(noPenTxt);
		exportButtonPanel.add(deletePenalties);
		exportButtonPanel.add(deletePenTxt);

		exportButtonPanel.add(listOfRanks);
		exportButtonPanel.add(euwHasEune);
		exportButtonPanel.setSize(500, 200);
		exportButtonPanel.setLocation(3, 300);
		content.add(exportButtonPanel);

		insertButton.setSize(100, 30);
		updateButton.setSize(100, 30);
		exportButton.setSize(100, 30);
		deleteButton.setSize(100, 30);
		clearButton.setSize(100, 30);
		euwTable.setSize(100, 30);
		eune.setSize(100, 30);
		euwRiotIncrease.setSize(100, 30);


		insertButton.setLocation(370, 10);
		updateButton.setLocation(370, 80);
		exportButton.setLocation(370, 110);
		deleteButton.setLocation(370, 40);
		clearButton.setLocation(370, 150);
		euwTable.setLocation(370, 190);
		eune.setLocation(370, 220);
		euwRiotIncrease.setLocation(370, 270);

		insertButton.addActionListener(this);
		updateButton.addActionListener(this);
		exportButton.addActionListener(this);
		deleteButton.addActionListener(this);
		clearButton.addActionListener(this);
		euwTable.addActionListener(this);
		eune.addActionListener(this);
		euwRiotIncrease.addActionListener(this);

		this.listOfRanks.addActionListener(this);
		this.numNoPenatlies.addActionListener(this);
		this.euwHasEune.addActionListener(this);
		this.deletePenalties.addActionListener(this);

		content.add(insertButton);
		content.add(updateButton);
		content.add(exportButton);
		content.add(deleteButton);
		content.add(clearButton);
		content.add(euwTable);
		content.add(eune);
		content.add(euwRiotIncrease);
	

		TableofDBContents.setPreferredScrollableViewportSize(new Dimension(900, 300));

		dbContentsPanel = new JScrollPane(TableofDBContents, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		dbContentsPanel.setBackground(Color.lightGray);
		dbContentsPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Database Content"));

		detailsPanel.setSize(360, 300);
		detailsPanel.setLocation(3, 0);
		dbContentsPanel.setSize(700, 300);
		dbContentsPanel.setLocation(477, 0);

		content.add(detailsPanel);
		content.add(dbContentsPanel);

		setSize(982, 645);
		setVisible(true);

		TableModel.refreshFromDB(stmt);
	}

	public void initiate_db_conn() {
		try {
			// Load the JConnector Driver
			Class.forName("com.mysql.jdbc.Driver");
			// Specify the DB Name
			String url = "jdbc:mysql://localhost:3306/leaguePlayers";
			// Connect to DB using DB URL, Username and password
			con = DriverManager.getConnection(url, "root", "a00252699");
			// Create a generic statement which is passed to the TestInternalFrame1
			stmt = con.createStatement();
		} catch (Exception e) {
			System.out.println("Error: Failed to connect to database\n" + e.getMessage());
		}
	}

	// event handling
	public void actionPerformed(ActionEvent e) {
		Object target = e.getSource();
		if (target == clearButton) {
			IDTF.setText("");
			userNameTxt.setText("");
			rankTxt.setText("");
			dateTxt.setText("");
			penaltiesTxt.setText("");
			rpTxt.setText("");
			serverTxt.setText("");

		}

		if (target == euwTable) {

			try {
				String euwAll = "SELECT * from euw ";

				stmt.executeQuery(euwAll);

			} catch (SQLException sqle) {
				System.err.println("Error with  insert:\n" + sqle.toString());
			} finally {
				TableModel.refreshFromDBeuw(stmt);
			}

		}
		
		if (target == euwRiotIncrease) {

			try {
				String euwAll = "call update_riot_points(1,250)";

				stmt.executeQuery(euwAll);

			} catch (SQLException sqle) {
				System.err.println("Error with  insert:\n" + sqle.toString());
			} finally {
				TableModel.refreshFromDBeuw(stmt);
			}

		}
		if (target == eune) {

			try {
				String euwAll = "SELECT * from eune ";

				stmt.executeQuery(euwAll);

			} catch (SQLException sqle) {
				System.err.println("Error with  insert:\n" + sqle.toString());
			} finally {
				TableModel.refreshFromDB(stmt);
			}

		}

		if (target == insertButton) {
			try {
				String updateTemp = "INSERT INTO eune VALUES ( '" + userNameTxt.getText() + "', '" + rankTxt.getText()
						+ "','" + dateTxt.getText() + "'," + penaltiesTxt.getText() + "," + rpTxt.getText() + ","
						+ IDTF.getText() + ",'" + serverTxt.getText() + "' );";

				stmt.executeUpdate(updateTemp);

			} catch (SQLException sqle) {
				System.err.println("Error with  insert:\n" + sqle.toString());
			} finally {
				TableModel.refreshFromDB(stmt);
			}
		}
		if (target == deleteButton) {

			try {
				String updateTemp = "DELETE FROM eune WHERE uniqueAccountId = " + IDTF.getText() + ";";
				stmt.executeUpdate(updateTemp);

			} catch (SQLException sqle) {
				System.err.println("Error with delete:\n" + sqle.toString());
			} finally {
				TableModel.refreshFromDB(stmt);
			}
		}
		if (target == updateButton) {
			try {
				String updateTemp = "UPDATE eune SET " + "userName = '" + userNameTxt.getText() + "', currentRank = '"
						+ rankTxt.getText() + "', dateCreated = '" + dateTxt.getText() + "', hasAnyPenalties ="
						+ penaltiesTxt.getText() + ", riotPointsOwned = " + rpTxt.getText() + ", mostPlayedServer = '"
						+ serverTxt.getText() + "' where uniqueAccountId = " + IDTF.getText();

				stmt.executeUpdate(updateTemp);
				// these lines do nothing but the table updates when we access the db.
				rs = stmt.executeQuery("SELECT * from eune ");
				rs.next();
				rs.close();
			} catch (SQLException sqle) {
				System.err.println("Error with  update:\n" + sqle.toString());
			} finally {
				TableModel.refreshFromDB(stmt);
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////
		// I have only added functionality of 2 of the button on the lower right of the
		///////////////////////////////////////////////////////////////////////////////////// template
		///////////////////////////////////////////////////////////////////////////////////

		if (target == this.listOfRanks) {

			cmd = "select distinct currentRank from eune;";

			try {
				rs = stmt.executeQuery(cmd);
				writeToFile(rs);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}
		
		if (target == exportButton) {

			cmd = "select * from eune;";

			try {
				rs = stmt.executeQuery(cmd);
				writeToFile(rs);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}

		if (target == this.euwHasEune) {

			cmd = "select distinct euw.userName, euw.currentRank\r\n" + "from euw\r\n" + "inner join eune\r\n"
					+ "on euw.uniqueAccountIdeune=eune.uniqueAccountId;";

			try {
				rs = stmt.executeQuery(cmd);
				writeToFile(rs);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}

		if (target == this.numNoPenatlies) {
			String deptName = this.noPenTxt.getText();

			cmd = "select * " + "from eune " + "where hasAnyPenalties = " + deptName + ";";

			System.out.println(cmd);
			try {
				rs = stmt.executeQuery(cmd);
				writeToFile(rs);

			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}
		if (target == this.deletePenalties) {

			String deptName = this.deletePenTxt.getText();

			cmd = "delete from eune where hasAnyPenalties = " + deptName + ";";

			System.out.println(cmd);
			try {
				stmt.executeUpdate(cmd);
				rs = stmt.executeQuery("select * from removed;");
				writeToFile(rs);

			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}

	}
	///////////////////////////////////////////////////////////////////////////

	private void writeToFile(ResultSet rs) {
		try {
			System.out.println("In writeToFile");
			FileWriter outputFile = new FileWriter("output.csv");
			PrintWriter printWriter = new PrintWriter(outputFile);
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();

			for (int i = 0; i < numColumns; i++) {
				printWriter.print(rsmd.getColumnLabel(i + 1) + ",");
			}
			printWriter.print("\n");
			while (rs.next()) {
				for (int i = 0; i < numColumns; i++) {
					printWriter.print(rs.getString(i + 1) + ",");
				}
				printWriter.print("\n");
				printWriter.flush();
			}
			printWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
