import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Account {
    protected int id;
    protected int customerID;
    protected double balance;

    public Account(int customerID, double balance) {
        this.customerID = customerID;
        this.id = customerID;
        this.balance = balance;
    }

    public double getBalance() { return balance; }
    public int getID() {return id;}
    
    public boolean setBalance(Connection conn, double balance) { 
        this.balance = balance;

        String sql = "UPDATE Account SET Balance = ? WHERE ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, this.balance);
            pstmt.setInt(2, this.id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
