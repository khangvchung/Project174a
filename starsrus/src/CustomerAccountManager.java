import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerAccountManager {

    public int registerCustomer(Connection conn, Customer customer) {
        String sql = "INSERT INTO Customers (Name, State, Phone, Email, TaxID, Username, Password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        String sequenceSql = "SELECT Customers_SEQ.CURRVAL FROM DUAL";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getState());
            pstmt.setString(3, customer.getPhoneNumber());
            pstmt.setString(4, customer.getEmail());
            pstmt.setString(5, customer.getTaxID());
            pstmt.setString(6, customer.getUsername());
            pstmt.setString(7, customer.getPassword());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (PreparedStatement sequenceStmt = conn.prepareStatement(sequenceSql);
                     ResultSet rs = sequenceStmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    } else {
                        throw new SQLException("Retrieving generated ID failed, no ID obtained.");
                    }
                }
            } else {
                return 0;
            }

        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                System.out.println("Registration failed: Username already exists.");
                return -1;
            }
            else{
                e.printStackTrace();
                return 0;
            }
        }
    }

    public boolean authenticateLogin(Connection conn, String username, String hashedPassword) {
        String sql = "SELECT Password FROM Customers WHERE Username = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("Password");
                return storedPassword.equals(hashedPassword);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkUsernameExists(Connection connection, String username) throws SQLException {
        String sql = "SELECT Username FROM Customers WHERE Username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            }
            return false;
        }
    }    
    

    

    // Additional customer-related methods can be added here
    // For example, updating customer information, checking if a customer exists, etc.
}
