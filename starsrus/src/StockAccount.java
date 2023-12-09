import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StockAccount {
    private int stockAccountID;
    private double stockBalance;
    private String username;
    private String symbol;
    private double numberOfShares;

    public StockAccount(String username, double numberOfShares, String symbol) {
        this.username = username;
        this.numberOfShares = numberOfShares;
        this.symbol = symbol;
    }


    public boolean registerStockAccount(Connection conn) {
        String sql = "INSERT INTO StockAccount (username, stockBalance, symbol) VALUES (?, ?, ?)";
        
        String sequenceSql = "SELECT StockAccount_SEQ1.CURRVAL FROM DUAL";

        this.stockBalance = this.numberOfShares * Stock.getStockPrice(conn, this.symbol);

        try (PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.username);
            pstmt.setDouble(2, this.stockBalance);
            pstmt.setString(3, this.symbol);

            int affectedRows = pstmt.executeUpdate();


            if (affectedRows > 0) {
                try (PreparedStatement sequenceStmt = conn.prepareStatement(sequenceSql);
                     ResultSet rs = sequenceStmt.executeQuery()) {
                    if (rs.next()) {
                        this.stockAccountID = rs.getInt(1);
                    } else {
                        throw new SQLException("Retrieving generated ID failed, no ID obtained.");
                    }
                }
                return true;
            } else {
                return false;
            }


        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setStockAccountBalance(Connection conn, double stockBalance) { 
        this.stockBalance = stockBalance;

        String sql = "UPDATE StockAccount SET stockbalance = ? WHERE username = ? AND symbol = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, stockBalance);
            pstmt.setString(2, this.username);
            pstmt.setString(3, this.symbol);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean accountExists(Connection connection, String username, String symbol) {
        String sql = "SELECT 'Value exists' as result FROM DUAL WHERE EXISTS (SELECT 1 FROM StockAccount WHERE username = ? AND symbol = ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, symbol);

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

    public static double getStockAmount(Connection connection, String username, String symbol) {
        String sql = "SELECT stockbalance from StockAccount WHERE username = ? AND symbol = ?";

        try (PreparedStatement sequenceStmt = connection.prepareStatement(sql)) {
            sequenceStmt.setString(1, username);
            sequenceStmt.setString(2, symbol);
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

    public double getStockBalance() {return this.stockBalance;}
    public void setStockBalance(double amount) {
        this.stockBalance = amount;
    }

}
