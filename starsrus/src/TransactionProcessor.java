import java.sql.Connection;

public class TransactionProcessor {
    private static final double COMMISSION_FEE = 20.00;

    public static boolean processTransaction(Connection connection, Transaction transaction, Account account) {
        switch (transaction.getType().toLowerCase()) {
            case "deposit":
                return processDeposit(connection, transaction, account);
            case "withdraw":
                return processWithdrawal(connection, transaction, account);
            // case "accrue-interest":
            //     break;
            default:
                throw new IllegalArgumentException("Unknown transaction type.");
        }
    }

    public static boolean processTransaction(Connection connection, Transaction transaction, Account account, StockAccount stockAccount) {
        switch (transaction.getType().toLowerCase()) {
            case "buy":
                return processBuy(connection, transaction, account, stockAccount, true);
            case "sell":
                return processSell(connection, transaction, account, stockAccount, true);
            default:
                throw new IllegalArgumentException("Unknown transaction type.");
        }
        // Add logic to record the transaction in the database
    }

    private static boolean processDeposit(Connection connection, Transaction transaction, Account account) {
        double amount = transaction.getAmount();
        transaction.registerTransaction(connection);
        return account.setBalance(connection, account.getBalance() + amount);
    }

    private static boolean processWithdrawal(Connection connection, Transaction transaction, Account account) {
        double amount = transaction.getAmount();
        if (account.getBalance() - amount < 0) {
            return false;
        }
        transaction.registerTransaction(connection);
        return account.setBalance(connection, account.getBalance() - amount);
    }

    private static boolean processBuy(Connection connection, Transaction transaction, Account account, StockAccount stockaccount, boolean log) {
        double totalCost = transaction.getAmount() + COMMISSION_FEE;
        if (account.getBalance() < totalCost) {
            return false;
        }

        stockaccount.setStockAccountBalance(connection, stockaccount.getStockBalance() + transaction.getAmount());
        account.setBalance(connection, account.getBalance() - totalCost);
        if (log) { transaction.registerTransactionStock(connection); }
        return true;
    }

    private static boolean processSell(Connection connection, Transaction transaction, Account account, StockAccount stockaccount, boolean log) {
        if (stockaccount.getStockBalance() < transaction.getAmount()) {
            return false;
        }
        double totalSaleAmount = transaction.getAmount() - COMMISSION_FEE;
        
        stockaccount.setStockAccountBalance(connection, stockaccount.getStockBalance() - transaction.getAmount());
        account.setBalance(connection, account.getBalance() + totalSaleAmount);
        if (log) { transaction.registerTransactionStock(connection); }
        return true;
    }

    // private static void processAccrueInterest(Connection connection, Transaction transaction, MarketAccount account) {
    //     double interest = account.getBalance() * (account.getInterestRate() / 100 / 12); // Assuming monthly interest rate calculation
    //     account.setBalance(connection, account.getBalance() + interest);
    //     // Record transaction
    // }

    public static boolean processCancel(Connection connection, Transaction prevTransaction, Account account, StockAccount stockaccount) {
        if (prevTransaction.getType() == "buy") {
            return processSell(connection, prevTransaction, account, stockaccount, false);
        } else {
            return processBuy(connection, prevTransaction, account, stockaccount, false);
        }
    }
}
