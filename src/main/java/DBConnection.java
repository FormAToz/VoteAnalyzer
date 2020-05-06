import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public abstract class DBConnection {
    private static String dbUrl = "jdbc:mysql://localhost:3306/learn?useUnicode=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPass = "testformat";
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement prepStatement;
    private static StringBuilder insertQuery = new StringBuilder();
    private static StringBuilder insertNames = new StringBuilder();
    private static int counter = 0;

    static {
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            statement = connection.createStatement();
            statement.execute("DROP TABLE IF EXISTS voter_count");
            statement.execute("CREATE TABLE voter_count(" +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "name TINYTEXT NOT NULL, " +
                    "birthDate DATE NOT NULL, " +
                    "PRIMARY KEY(id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Statement getStatement() {
        return statement;
    }

    //    // Execiting queries by Statement & multiinsert
//    public static void executeMultiinsert() throws SQLException {
//        String sql = "INSERT INTO voter_count(name, birthDate) " +
//                "VALUES" + insertQuery.toString();
//        getStatement().execute(sql);
//    }
//
//    public static void countVoter(String name, String birthDay) throws SQLException {
//        birthDay = birthDay.replace('.', '-');
//        insertQuery.append((insertQuery.length() == 0 ? "" : ",") + "('" + name + "', '" + birthDay + "')");
//
//        if (insertQuery.length() > 100_000) {
//            executeMultiinsert();
//            insertQuery = new StringBuilder();
//        }
//    }

    // Execiting queries by PrepareStatement
    public static void countVoter(String name, String birthDay) throws SQLException {
        birthDay = birthDay.replace('.', '-');
        insertQuery.append((insertQuery.length() == 0 ? "" : ",") + "(?, ?)");
        insertNames.append(name + "," + birthDay + ",");
        counter++;

        if (insertQuery.length() > 100_000) {
            executeMultiinsert();
            insertQuery = new StringBuilder();
            counter = 0;
        }
    }

    public static void executeMultiinsert() throws SQLException {
        prepStatement = connection.prepareStatement("INSERT INTO voter_count(name, birthDate) VALUES" + insertQuery.toString());
        String[] strArray = insertNames.toString().split(",");

        int j = 0;
        int index = 1;
        for (int i = 0; i < counter; i++) {
            prepStatement.setString(index++, strArray[j++]);
            prepStatement.setString(index++, strArray[j++]);
        }
        prepStatement.execute();
    }

    public static void printVoterCounts() throws SQLException {
        long time = System.currentTimeMillis();

        String sql = "SELECT name, birthDate, COUNT(*) AS count FROM voter_count GROUP BY name, birthDate HAVING count > 1";
        ResultSet rs = getStatement().executeQuery(sql);
        while (rs.next()) {
            System.out.println("\t" + rs.getString("name") + " (" +
                    rs.getString("birthDate") + ") - " + rs.getInt("count"));
        }
        System.out.println("Time for select voter counts: " + (System.currentTimeMillis() - time) + " ms");
    }
}