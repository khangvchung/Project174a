import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import oracle.ucp.util.Pair;

public class Session {
    private int id;
    private String username;
    private double balance;
    private ArrayList<Pair<Transaction, Integer>> transactions;
    private boolean prev = false;
    private Account prevAccount;
    private StockAccount prevStockAccount;
    private Transaction prevTransaction;

    public Session(String username) {
        this.username = username;
        transactions = new ArrayList<>();
    }

    public void login(Connection connection) {
        String sql = "SELECT customerid from Customers WHERE username = ?";
        
        try (PreparedStatement sequenceStmt = connection.prepareStatement(sql)) {
            sequenceStmt.setString(1, this.username);
            ResultSet rs = sequenceStmt.executeQuery();
            if (rs.next()) {
                this.id = rs.getInt(1);
                // System.out.println(this.id);
            } else {
                throw new SQLException("Retrieving generated ID failed, no ID obtained.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle specific exceptions as needed
        }
        setTransactions(connection);
    }

    public double getBalance(Connection connection) {
        String sql = "SELECT balance from Account WHERE customerid = ?";

        try (PreparedStatement sequenceStmt = connection.prepareStatement(sql)) {
            sequenceStmt.setInt(1, this.id);
            ResultSet rs = sequenceStmt.executeQuery();
            if (rs.next()) {
                this.balance = rs.getDouble(1);
                //System.out.println(this.id);
                return this.balance;
            } else {
                throw new SQLException("Retrieving generated ID failed, no ID obtained.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle specific exceptions as needed
        }
        return 0.0;
    }

    public double getStockBalance(Connection connection, String symbol) {
        String sql = "SELECT stockbalance FROM stockaccount WHERE symbol = ?";

        try (PreparedStatement sequenceStmt = connection.prepareStatement(sql)) {
            sequenceStmt.setString(1, symbol);
            ResultSet rs = sequenceStmt.executeQuery();
            if (rs.next()) {
                this.balance = rs.getDouble(1);
                //System.out.println(this.id);
                return this.balance;
            } else {
                throw new SQLException("Retrieving generated ID failed, no ID obtained.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public void setTransactions(Connection connection) {
        String sql = "SELECT * FROM transaction WHERE accountid = ?";
        try (PreparedStatement sequenceStmt = connection.prepareStatement(sql)) {
            sequenceStmt.setInt(1, this.id);

            ResultSet rs = sequenceStmt.executeQuery();
            while (rs.next()) {
                int transactionID = rs.getInt("transactionid");
                java.sql.Date date = rs.getDate("transactiondate");
                String type = rs.getString("transactiontype");
                String symbol = rs.getString("stocksymbol");
                double amount = rs.getDouble("amount");
                double numberOfShares = rs.getDouble("numberofshares");
                double pricePerShare = rs.getDouble("pricepershare");
                
                Transaction transaction;
                
                if (symbol == null) {
                    transaction = new Transaction(this.id, date, type, amount);
                } else {
                    transaction = new Transaction(this.id, date, type, symbol, numberOfShares, pricePerShare);
                }

                Pair<Transaction, Integer> pair = new Pair<>(transaction, transactionID);
                this.transactions.add(pair);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Pair<Transaction, Integer>> getTransactions() {return this.transactions;}

    public String getUsername() {return this.username;}
    public int getId() {return this.id;}
    public boolean getPrev() {return this.prev;}
    public Account getPrevAccount() {return this.prevAccount;}
    public StockAccount getPrevStockAccount() {return this.prevStockAccount;}
    public Transaction getPrevTransaction() {return this.prevTransaction;}

    public void setPrev(boolean prev) {
        this.prev = prev;
    }

    public void setPrevTransaction(boolean prev, Account account, StockAccount stockAccount, Transaction transaction) {
        this.prev = prev;
        this.prevAccount = account;
        this.prevStockAccount = stockAccount;
        this.prevTransaction = transaction;
    }

    public static ArrayList<ArrayList<String>> listAccounts(Connection connection, String username) {
        String query = "SELECT stockaccountid, symbol, stockbalance, username FROM stockaccount WHERE username = ?";
        ArrayList<ArrayList<String>> stockAccounts = new ArrayList<>();
        
        ArrayList<String> header = new ArrayList<>();
        header.add("stockaccountid");
        header.add("username");
        header.add("stockbalance");
        header.add("symbol");

        stockAccounts.add(header);

        try (PreparedStatement sequenceStmt = connection.prepareStatement(query)) {
            sequenceStmt.setString(1, username);

            ResultSet rs = sequenceStmt.executeQuery();
            while (rs.next()) {
                ArrayList<String> row = new ArrayList<>();


                row.add(String.valueOf(rs.getInt("stockaccountid")));
                row.add(rs.getString("username"));
                row.add(String.valueOf(rs.getDouble("stockbalance")));
                row.add(rs.getString("symbol"));

                stockAccounts.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stockAccounts;
    }
}
