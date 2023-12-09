import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Stock {
    private String symbol;
    private double currentPrice;
    private double closingPrice;

    public Stock(String symbol, double currentPrice, double closingPrice) {
        this.symbol = symbol;
        this.currentPrice = currentPrice;
        this.closingPrice = closingPrice;
    }

    public void registerStock(Connection connection) {
        String sql = "INSERT INTO Stock (symbol, currentPrice, closingPrice) VALUES (?, ?, ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, this.symbol);
            pstmt.setDouble(2, this.currentPrice);
            pstmt.setDouble(3, this.closingPrice);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    public void updatePrice(Connection connection, double newPrice) {
        this.currentPrice = newPrice;
        String sql = "UPDATE Stock SET CurrentPrice = ? WHERE Symbol = ?";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setDouble(1, newPrice);
            pstmt.setString(2, this.symbol);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean stockExists(Connection connection, String symbol) {
        String sql = "SELECT 'Value exists' as result FROM DUAL WHERE EXISTS (SELECT 1 FROM Stock WHERE symbol = ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, symbol);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static double getStockPrice(Connection connection, String symbol) {
        String sql = "SELECT currentprice from Stock WHERE symbol = ?";

        try (PreparedStatement sequenceStmt = connection.prepareStatement(sql)) {
            sequenceStmt.setString(1, symbol);
            ResultSet rs = sequenceStmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            } else {
                throw new SQLException("Retrieving generated ID failed, no ID obtained.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static String showInfo(Connection connection, String symbol) {
        String sql = "SELECT * from ActorDirector WHERE stocksymbol = ?";

        String result = "";

        try (PreparedStatement sequenceStmt = connection.prepareStatement(sql)) {
            sequenceStmt.setString(1, symbol);
            ResultSet rs = sequenceStmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString(1);
                String dob = rs.getDate(2).toString();
                
                result += "Price: " + getStockPrice(connection, symbol) + " Actor/Director: " + name + " DOB: " + dob;
            } else {    
                throw new SQLException("Retrieving generated ID failed, no ID obtained.");
            }

            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }
}
