package com.hcl.javajdbcjpa.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * AbstractDAO.java This DAO class provides CRUD database operations for the
 * table users in the database.
 * 
 * @author sourcecodeexamples
 *
 */
public class UserDao {
	private String jdbcURL = "jdbc:h2:mem:test";
	private String jdbcUsername = "su";
	private String jdbcPassword = "";

	private static final String CREATE_TABLE_SQL = "create table users (" + "  id  int primary key,"
			+ "  name varchar(20)," + "  email varchar(20)," + "  country varchar(20) ) ";

	private static final String INSERT_USERS_SQL = "INSERT INTO users" + "  (id, name, email, country) VALUES "
			+ " (?, ?, ?, ?);";

	private static final String SELECT_USER_BY_ID = "select id,name,email,country from users where id =?";
	private static final String SELECT_ALL_USERS = "select * from users";
	private static final String DELETE_USERS_SQL = "delete from users where id = ?;";
	private static final String UPDATE_USERS_SQL = "update users set name = ?,email= ?, country =? where id = ?;";

	public UserDao() {
	}

	//What is DAO -> Database access object
	public static void main(String[] args) throws SQLException {

		UserDao ud = new UserDao();

		// try with resources
		try (Connection connection = ud.getConnection()) {
			ud.createTable(connection);
			try (Scanner input = new Scanner(System.in);) {
				// main menu
				boolean menu = true;
				//while loop here
				while (menu) {
					System.out.println("What would you like to do: Insert, Update, Delete, Read, ReadAll, Quit?");
					String option = input.nextLine();
					switch(option.toLowerCase()) {
					case "insert":
						User insertUser = createUser(input);
						ud.insertUser(connection, insertUser);
						break;
					case "update":
						User updateUser = createUser(input);
						ud.updateUser(connection, updateUser);
						break;
					case "delete":
						int idDelete = getId(input);
						ud.deleteUser(connection, idDelete);
						break;
					case "read":
						int idRead = getId(input);
						User user = ud.selectUser(connection, idRead);
						System.out.println("ID: " + user.getId() + ", UserName: " + user.getName() + ", email: " + user.getEmail() + ", location: " + user.getCountry());
						break;
					case "readall":
						List<User> list = ud.selectAllUsers(connection);
						readAll(list);
						break;
					case "quit":
						
						break;
					default:
						System.out.println("Invalid option");
						break;
					}
					menu =	menuOperator(input, menu, "Would you like to do anything else? ");
				}
			
			
			
			/* ud.insertUser(connection, new User(1, "Krishna", "k@p.com", "us"));
			List<User> list = ud.selectAllUsers(connection);
			System.out.println("user count: " + list.size());
			System.out.println("user name: " + list.get(0).getName());
			ud.updateUser(connection, new User(1, "Krishna P", "k@p.com", "us"));
			list = ud.selectAllUsers(connection);
			System.out.println("user name: " + list.get(0).getName());
			ud.deleteUser(connection, 1);
			*/
			}
		}
	}

	
	public static User createUser(Scanner input) {
		System.out.println("Enter the ID");
		int id = input.nextInt();
		input.nextLine();
		System.out.println("Enter the UserName");
		String username = input.nextLine();
		System.out.println("Enter the Email");
		String email = input.nextLine();
		System.out.println("Enter the location");
		String location = input.nextLine();
		
		return new User(id, username, email, location);
		
	}
	
	public static void readAll(List<User> list) {
		list.forEach(User -> System.out.println("ID: " + User.getId() + ", UserName: " + User.getName() + ", email: " + User.getEmail() + ", location: " + User.getCountry() ));		
	}
	
	public static int getId(Scanner input) {
		System.out.println("Enter the ID");
		int id = input.nextInt();
		return id;
	}

	
	protected Connection getConnection() {
		Connection connection = null;
		try {
			// Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}

	public void createTable(Connection connection) throws SQLException {

		// Step 1: Establishing a Connection
		try (Statement statement = connection.createStatement()) {

			// Step 3: Execute the query or update query
			statement.execute(CREATE_TABLE_SQL);
		} catch (SQLException e) {
			// print SQL exception information
			printSQLException(e);
		}
	}

	public void insertUser(Connection connection, User user) throws SQLException {
		System.out.println(INSERT_USERS_SQL);
		// try-with-resource statement will auto close the connection.
		try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USERS_SQL)) {
			preparedStatement.setLong(1, user.getId());
			preparedStatement.setString(2, user.getName());
			preparedStatement.setString(3, user.getEmail());
			preparedStatement.setString(4, user.getCountry());
			System.out.println(preparedStatement);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	public User selectUser(Connection connection, int id) {
		User user = null;
		// Step 1: Establishing a Connection
		try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID);) {
			preparedStatement.setInt(1, id);
			System.out.println(preparedStatement);
			// Step 3: Execute the query or update query
			ResultSet rs = preparedStatement.executeQuery();

			// Step 4: Process the ResultSet object.
			while (rs.next()) {
				String name = rs.getString("name");
				String email = rs.getString("email");
				String country = rs.getString("country");
				user = new User(id, name, email, country);
			}
		} catch (SQLException e) {
			printSQLException(e);
		}
		return user;
	}

	public List<User> selectAllUsers(Connection connection) {

		// using try-with-resources to avoid closing resources (boiler plate code)
		List<User> users = new ArrayList<>();
		// Step 1: Establishing a Connection
		try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, 
				ResultSet.CONCUR_UPDATABLE);) {
			System.out.println(statement);
			// Step 3: Execute the query or update query
			ResultSet rs = statement.executeQuery(SELECT_ALL_USERS);
			
			ResultSetMetaData dm = rs.getMetaData();
			System.out.println("dm=" + dm);

			// Step 4: Process the ResultSet object.
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				String email = rs.getString("email");
				String country = rs.getString("country");
				users.add(new User(id, name, email, country));
			}
		} catch (SQLException e) {
			printSQLException(e);
		}
		return users;
	}

	public boolean deleteUser(Connection connection, int id) throws SQLException {
		boolean rowDeleted;
		try (PreparedStatement statement = connection.prepareStatement(DELETE_USERS_SQL);) {
			statement.setInt(1, id);
			rowDeleted = statement.executeUpdate() > 0;
		}
		return rowDeleted;
	}

	public boolean updateUser(Connection connection, User user) throws SQLException {
		boolean rowUpdated;
		try (PreparedStatement statement = connection.prepareStatement(UPDATE_USERS_SQL);) {
			statement.setString(1, user.getName());
			statement.setString(2, user.getEmail());
			statement.setString(3, user.getCountry());
			statement.setInt(4, user.getId());

			rowUpdated = statement.executeUpdate() > 0;
		}
		return rowUpdated;
	}

	private void printSQLException(SQLException ex) {
		for (Throwable e : ex) {
			if (e instanceof SQLException) {
				e.printStackTrace(System.err);
				System.err.println("SQLState: " + ((SQLException) e).getSQLState());
				System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
				System.err.println("Message: " + e.getMessage());
				Throwable t = ex.getCause();
				while (t != null) {
					System.out.println("Cause: " + t);
					t = t.getCause();
				}
			}
		}
	}
	
	private static boolean menuOperator(Scanner input, boolean go, String message) {
		// loops operation menu
		System.out.println(message);
		boolean gogo = true;
		while (gogo) {
			String repeat = input.nextLine();
			if (repeat.toUpperCase().equals("Y")) {
				gogo = false;
			} else if (repeat.toUpperCase().equals("N")) {
				go = false;
				gogo = false;
			} else {
				System.out.println("invalid input, try again. input Y or N: ");
			}
		}
		return go;
	}

}