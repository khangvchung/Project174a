import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import oracle.ucp.util.Pair;

public class Transaction {
    private int transactionID;
    private int accountID;
    private Date date;
    private String type; // e.g., deposit, withdraw, buy, sell, cancel, accrue-interest
    private double amount;
    private String stockSymbol; // Nullable, used only for stock-related transactions
    private double numberOfShares; // Used for buy/sell transactions
    private double pricePerShare; // Used for buy/sell transactions

    public Transaction(int accountID, Date date, String type, String stockSymbol, double numberOfShares, double pricePerShare) {
        this.accountID = accountID;
        this.date = date;
        this.type = type;
        this.amount = numberOfShares * pricePerShare;
        this.stockSymbol = stockSymbol;
        this.numberOfShares = numberOfShares;
        this.pricePerShare = pricePerShare;
    }

    public Transaction(int accountID, Date date, String type, double amount) {
        this.accountID = accountID;
        this.date = date;
        this.type = type;
        this.amount = amount;
    }
    
    public int getTransactionID() { return transactionID; }
    public int getAccountID() { return accountID; }
    public Date getDate() { return date; }
    public String getType() { return type; }
    public String getStockSymbol() { return stockSymbol; }
    public double getAmount() { return amount; }
    public double getNumberOfShares() { return numberOfShares; }
    public double getPricePerShare() { return pricePerShare; }

    public boolean registerTransaction(Connection conn) {

        String sql = "INSERT INTO Transaction (accountID, transactiondate, transactiontype, amount) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.accountID);
            pstmt.setDate(2, (java.sql.Date) this.date);
            pstmt.setString(3, this.type);
            pstmt.setDouble(4, this.amount);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerTransactionStock(Connection conn) {

        String sql = "INSERT INTO Transaction (accountID, transactiondate, transactiontype, amount, stockSymbol, numberOfShares, pricePerShare) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.accountID);
            pstmt.setDate(2, (java.sql.Date) this.date);
            pstmt.setString(3, this.type);
            pstmt.setDouble(4, this.amount);
            pstmt.setString(5, this.stockSymbol);
            pstmt.setDouble(6, this.numberOfShares);
            pstmt.setDouble(7, this.pricePerShare);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static ArrayList<String> activeCustomers(Connection connection, int year, int month) {
        ArrayList<String> customers = new ArrayList<>();

        String sql = "SELECT c.username FROM customers c INNER JOIN(SELECT accountid FROM transaction WHERE EXTRACT(MONTH FROM transactiondate) = ? AND EXTRACT(YEAR FROM transactiondate) = ? GROUP BY accountid HAVING SUM(numberofshares) >= 1000) t ON c.customerid = t.accountid";


        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, month);
            pstmt.setInt(2, year);

            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String username = rs.getString(1);
                customers.add(username);
            } 
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    public static ArrayList<Pair<Transaction, Integer>> monthlyStatement(Connection connection, String username, int month) throws SQLException{
        ArrayList<Pair<Transaction, Integer>> transactions = new ArrayList<>();

        String sql = "SELECT t.* FROM transaction t JOIN customers c ON t.accountid = c.customerid WHERE c.username = ? AND EXTRACT(MONTH FROM t.transactiondate) = ?";
        
        try (PreparedStatement sequenceStmt = connection.prepareStatement(sql)) {
            sequenceStmt.setString(1, username);
            sequenceStmt.setInt(2, month);

            ResultSet rs = sequenceStmt.executeQuery();
            while (rs.next()) {
                int accountID = rs.getInt("accountid");
                int transactionID = rs.getInt("transactionid");
                java.sql.Date date = rs.getDate("transactiondate");
                String type = rs.getString("transactiontype");
                String symbol = rs.getString("stocksymbol");
                double amount = rs.getDouble("amount");
                double numberOfShares = rs.getDouble("numberofshares");
                double pricePerShare = rs.getDouble("pricepershare");
                
                Transaction transaction;
                
                if (symbol == null) {
                    transaction = new Transaction(accountID, date, type, amount);
                } else {
                    transaction = new Transaction(accountID, date, type, symbol, numberOfShares, pricePerShare);
                }

                Pair<Transaction, Integer> pair = new Pair<>(transaction, transactionID);
                transactions.add(pair);
            }

            return transactions;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public static void deleteAllTransactions(Connection connection) {
        String sql = "TRUNCATE TABLE transaction";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error");
        }



    }

}
