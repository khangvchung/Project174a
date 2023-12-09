import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MarketAccount extends Account {
    private double interestRate;
    private double minDeposit;

    public MarketAccount(int customerID, double balance, double interestRate, double minDeposit) {
        super(customerID, balance);
        this.interestRate = interestRate;
        this.minDeposit = minDeposit;

    }

    // Market account specific methods
    public double getInterestRate() { return interestRate; }

    public boolean registerAccount(Connection conn) {
        String sql = "INSERT INTO Account (customerID, balance) VALUES (?, ?)";
        
        String sequenceSql = "SELECT Account_SEQ.CURRVAL FROM DUAL";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, this.customerID);
            pstmt.setDouble(2, this.balance);

            int affectedRows = pstmt.executeUpdate();
           
            if (affectedRows > 0) {
                try (PreparedStatement sequenceStmt = conn.prepareStatement(sequenceSql);
                     ResultSet rs = sequenceStmt.executeQuery()) {
                    if (rs.next()) {
                        this.id = rs.getInt(1);
                    } else {
                        throw new SQLException("Retrieving generated ID failed, no ID obtained.");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql2 = "INSERT INTO MarketAccount (AccountID, interestRate, MinDeposit) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
            pstmt.setInt(1, this.id);
            pstmt.setDouble(2, this.interestRate);
            pstmt.setDouble(3, this.minDeposit);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addInterest(Connection connection) {
        String sql = "UPDATE account SET balance = balance * (1 + (SELECT interestrate FROM marketAccount WHERE account.id = marketAccount.accountid) * 0.01) WHERE EXISTS (SELECT 1 FROM marketAccount WHERE account.id = marketAccount.accountid)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    } 
}
