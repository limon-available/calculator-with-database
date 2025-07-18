import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/calculator_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";         // your MySQL username
    private static final String PASSWORD = ""; // your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void saveHistory(String expression, String result) {
        String sql = "INSERT INTO calculation_history (expression, result) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expression);
            ps.setString(2, result);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadHistory() {
        List<String> history = new ArrayList<>();
        String sql = "SELECT expression, result FROM calculation_history ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String expr = rs.getString("expression");
                String res = rs.getString("result");
                history.add(expr + " = " + res);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public static void saveMemory(double memoryValue) {
        String sql = "UPDATE memory SET memory_value = ? WHERE id = 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, memoryValue);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double loadMemory() {
        String sql = "SELECT memory_value FROM memory WHERE id = 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("memory_value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
