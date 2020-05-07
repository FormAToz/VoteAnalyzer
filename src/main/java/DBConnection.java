import java.sql.*;

public abstract class DBConnection {
    private static String dbUrl = "jdbc:mysql://localhost:3306/learn?useUnicode=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPass = "testformat";
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement prepStatement;
    private static int counter = 0;

    static {
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            statement.execute("DROP TABLE IF EXISTS voter_count");
            statement.execute("CREATE TABLE voter_count(" +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "name TINYTEXT NOT NULL, " +
                    "birthDate DATE NOT NULL, " +
                    "PRIMARY KEY(id))");
            prepStatement = connection.prepareStatement("INSERT INTO voter_count(name, birthDate) VALUES(?,?)");
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
        prepStatement.setString(1, name);
        prepStatement.setString(2, birthDay);
        prepStatement.addBatch();
        counter++;

        if (counter > 100_000) {
            prepStatement.executeBatch();
            counter = 0;
        }
    }

    public static void execute() throws SQLException {
        prepStatement.executeBatch();
        connection.commit();
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