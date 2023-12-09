import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import oracle.jdbc.pool.OracleDataSource;
import oracle.ucp.util.Pair;
import oracle.jdbc.OracleConnection;
import java.sql.DatabaseMetaData;

public class MainInterface {
    final static String DB_URL = "jdbc:oracle:thin:@project174a_tp?TNS_ADMIN=/cs/student/khangvchung/cs174a/Wallet_project174a";
    final static String DB_USER = "ADMIN";
    final static String DB_PASSWORD = "Potato012345";

    static int year = 2023;
    static int month = 10;
    static int day = 16;

    static boolean openMarket = true;

    public static void main(String[] args) throws SQLException{
        Properties info = new Properties();

        System.out.println("Initializing connection properties...");
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        System.out.println("Creating OracleDataSource...");
        OracleDataSource ods = new OracleDataSource();

        System.out.println("Setting connection properties...");
        ods.setURL(DB_URL);
        ods.setConnectionProperties(info);


        try (OracleConnection connection = (OracleConnection) ods.getConnection()) {
            System.out.println("Connection established!");
            // Get JDBC driver name and version
            DatabaseMetaData dbmd = connection.getMetaData();
            System.out.println("Driver Name: " + dbmd.getDriverName());
            System.out.println("Driver Version: " + dbmd.getDriverVersion());
            // Print some connection properties
            System.out.println(
                "Default Row Prefetch Value: " + connection.getDefaultRowPrefetch()
            );
            System.out.println("Database username: " + connection.getUserName());
            System.out.println();

            

            //dataSetup(connection);
            userInterface(connection);

        } catch (Exception e) {
            System.out.println("CONNECTION ERROR:");
            System.out.println(e);
        }


    }
    

    public static void userInterface(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        CustomerAccountManager manager = new CustomerAccountManager();

        
        while (true) {
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Admin Login");
            System.out.println("4. Exit");
            System.out.println();
            System.out.println("Other options:");
            System.out.println("5. Set Date");
            System.out.println("6. Set Market Status");
            System.out.println();
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            boolean validInput;
            scanner.nextLine();

            switch (choice) {
                case 1: {
                    String username;
                    boolean usernameExists;
                    do {
                        System.out.print("Enter username: ");
                        username = scanner.nextLine();
                        try {
                            usernameExists = manager.checkUsernameExists(connection, username);
                            if (usernameExists) {
                                System.out.println("Username already exists. Please try a different one.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return; // Or handle the exception as appropriate
                        }
                    } while (usernameExists);

                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    System.out.print("Enter name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter phone number: ");
                    String phoneNumber = scanner.nextLine();
                    String state;
                    do {
                        System.out.print("Enter state (2-letter code): ");
                        state = scanner.nextLine();
                        validInput = state.length() == 2;
                        if (!validInput) {
                            System.out.println("State must be a 2-letter code.");
                        }
                    } while (!validInput);

                    
                    System.out.print("Enter taxID: ");
                    String taxID = scanner.nextLine();

                    System.out.print("Enter amount to add to account: ");
                    double balance = scanner.nextDouble();
                    if (balance < 1000) {
                        System.out.println("Minimum balance is 1000");
                        break;
                    } else {
                        try {
                            createCustomer(connection, name, username, password, state, phoneNumber, email, taxID, balance);
                            System.out.println("Successfully created account. You can now log in with your username and password.");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("Failed to create account due to a database error.");
                        }
                    }
                    
                    break;
                }
                case 2: {
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    if (!manager.authenticateLogin(connection, username, password)) {
                        System.out.println("Invalid username or password");
                        break;
                    }

                    System.out.println();
                    loginInterface(connection, username, scanner);
                    break;
                }
                case 3: {
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();

                    if (!username.equals("admin")) {
                        System.out.println("No access");
                        break;
                    }
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    if (!manager.authenticateLogin(connection, username, password)) {
                        System.out.println("Invalid username or password");
                        break;
                    }

                    managerInterface(connection, scanner);
                }
                case 4:
                    System.out.println("Exiting...");
                    System.exit(0);
                
                case 5:
                    System.out.println("Set date(yyyy-mm-dd format): ");
                    System.out.print("Year: ");
                    year = scanner.nextInt();
                    System.out.print("Month(Number): ");
                    month = scanner.nextInt();
                    System.out.print("Day: ");
                    day = scanner.nextInt();

                    System.out.println("Date set to: " + year + "-" + month + "-" + day);

                    break;
                
                    case 6:
                    System.out.println("Market Status: Open/Close");
                    String status = scanner.nextLine();

                    if(status.equals("Close") || status.equals("close")){
                        openMarket = false;
                        System.out.println("Market is now closed!");
                        break;
                    }

                    openMarket = true;
                    System.out.println("Market is now open!");
                    break;


                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            System.out.println();
        }

        //scanner.close();
    }

    public static void managerInterface(Connection connection, Scanner scanner) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("1. Add Interest");
            System.out.println("2. Generate Monthly Statement");
            System.out.println("3. List Active Customers");
            System.out.println("4. Generate Government DTER");
            System.out.println("5. Customer Report");
            System.out.println("6. Delete Transactions");
            System.out.println("7. Log out");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    if (day != 30) {
                        System.out.println("Not last day of month");
                    } else {
                        MarketAccount.addInterest(connection);
                        System.out.println("Updated balances");
                    }
                    System.out.println();
                    break;
                case 2:
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();

                    try {
                        ArrayList<Pair<Transaction, Integer>> transactions = Transaction.monthlyStatement(connection, username, month);
   
                        if (transactions.isEmpty()) {
                            System.out.println("No transactions found");
                        } else {
                            for (Pair<Transaction, Integer> pair: transactions) {
                                Transaction transaction = pair.get1st();
                                int id = pair.get2nd();
                                System.out.println(
                                    "Transaction ID: " + id + 
                                    " Date: " + transaction.getDate() + 
                                    " Type: " + transaction.getType() + 
                                    " Amount: " + transaction.getAmount() + 
                                    " Stock: " + transaction.getStockSymbol() + 
                                    " Number of Shares: " + transaction.getNumberOfShares() + 
                                    " Price per share: " + transaction.getPricePerShare());
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    break;

                case 3:
                    ArrayList<String> customers = Transaction.activeCustomers(connection, year, month);

                    for (String customer : customers) {
                        System.out.println(customer);
                    }
                    System.out.println();
                    break;

                case 4:
                    break;
                case 5:
                    System.out.print("Enter username: ");
                    String username1 = scanner.nextLine();

                    ArrayList<ArrayList<String>> stockAccounts = Session.listAccounts(connection, username1);

                    for (ArrayList<String> account : stockAccounts) {
                        System.out.println(account);
                    }
                    System.out.println();
                    break;

                case 6:
                    System.out.println("Are you sure? (Yes/No)");
                    String confirm = scanner.nextLine();

                    if(confirm.equals("Yes") || confirm.equals("yes")) {
                        Transaction.deleteAllTransactions(connection);
                        System.out.println("All transactions deleted");
                    }
                    break;


                case 7:
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void loginInterface(Connection connection, String username, Scanner scanner) {
        boolean loggedIn = true;
        while (loggedIn) {
            Session session = new Session(username);
            session.login(connection);
            MarketAccount account = new MarketAccount(session.getId(), session.getBalance(connection), 2.0, 1000);
            ArrayList<Pair<Transaction, Integer>> transactions = session.getTransactions();

            System.out.println("Logged in as: " + username);
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Buy");
            System.out.println("4. Sell");
            System.out.println("5. Cancel");
            System.out.println("6. Show balance");
            System.out.println("7. Show transaction history");
            System.out.println("8. List stock and actor profile");
            System.out.println("9. List movie information");
            System.out.println("10. Log out");
            System.out.print("Choose an option: ");
            

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: {
                    System.out.print("Enter amount to deposit: ");
                    Double amount = scanner.nextDouble();
                    Transaction transaction = new Transaction(session.getId(), java.sql.Date.valueOf(Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day)), "deposit", amount);
                    TransactionProcessor.processTransaction(connection, transaction, account);

                    System.out.println("Deposited " + amount);
                
                    break;
                }
                case 2: {
                    System.out.print("Enter amount to withdraw: ");
                    Double amount = scanner.nextDouble();
                    Transaction transaction = new Transaction(session.getId(), java.sql.Date.valueOf(Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day)), "withdraw", amount);
                    
                    if(!TransactionProcessor.processTransaction(connection, transaction, account)) {
                        System.out.println("Insufficient funds for withdrawal");

                    } else {
                        System.out.println("Withdrew " + amount);
                        transaction.registerTransaction(connection);
                    }

                    break;
                }
                case 3: {
                    if(!openMarket){
                        System.out.println("Market is closed");
                        break;
                    }
                    System.out.println("Enter stock symbol to buy: ");
                    String symbol = scanner.nextLine();
                    
                    if (!Stock.stockExists(connection, symbol)) {
                        System.out.println("Stock symbol does not exist");
                    } else {
                        System.out.println("Enter amount of shares to buy: ");
                        double numberOfShares = scanner.nextInt();

                        double price = Stock.getStockPrice(connection, symbol);
                        StockAccount stockAccount;

                        if (StockAccount.accountExists(connection, username, symbol)) {
                            double stockBalance = StockAccount.getStockAmount(connection, username, symbol);
                            stockAccount = new StockAccount(username, stockBalance / price, symbol);
                            stockAccount.setStockBalance(stockBalance);

                        } else {
                            stockAccount = new StockAccount(username, 0, symbol);
                            stockAccount.registerStockAccount(connection);
                        }

                        Transaction transaction = new Transaction(session.getId(), java.sql.Date.valueOf(Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day)), "buy", symbol, numberOfShares, price);
                        if(!TransactionProcessor.processTransaction(connection, transaction, account, stockAccount)) {
                            System.out.println("Insufficient funds to buy stocks");
                        } else {
                            session.setPrevTransaction(true, account, stockAccount, transaction);
                            System.out.println("Purchased " + numberOfShares + " shares of " + symbol);
                        }
                    }

                    break;
                }
                case 4: {
                    if(!openMarket){
                        System.out.println("Market is closed");
                        break;
                    }
                    System.out.println("Enter stock symbol to sell: ");
                    String symbol = scanner.nextLine();
                    
                    if (!Stock.stockExists(connection, symbol)) {
                        System.out.println("Stock symbol does not exist");
                    } else {
                        if (!StockAccount.accountExists(connection, username, symbol)) {
                            System.out.println("You don't own this stock.");
                        } else {

                            System.out.println("Enter amount of shares to sell: ");
                            double numberOfShares = scanner.nextInt();
    
                            double price = Stock.getStockPrice(connection, symbol);
                            double stockBalance = StockAccount.getStockAmount(connection, username, symbol);
                            StockAccount stockAccount = new StockAccount(username, stockBalance / price, symbol);
                            
                            stockAccount.setStockBalance(stockBalance);
    
                            Transaction transaction = new Transaction(session.getId(), java.sql.Date.valueOf(Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day)), "sell", symbol, numberOfShares, price);
                            if(!TransactionProcessor.processTransaction(connection, transaction, account, stockAccount)) {
                                System.out.println("Insufficient funds to sell stocks");
                            } else {
                                session.setPrevTransaction(true, account, stockAccount, transaction);
                                System.out.println("Sold " + numberOfShares + " shares of " + symbol);
                            }
                        }
                    }

                    break;
                }
                case 5: {
                    if (!session.getPrev()) {
                        System.out.println("No transactions to cancel");
                    } else {
                        TransactionProcessor.processCancel(connection, session.getPrevTransaction(), session.getPrevAccount(), session.getPrevStockAccount());
                        System.out.println("Cancelled most recent transaction");
                        session.setPrev(false);
                    }
                }
                case 6: {
                    System.out.println("Current balance: " + session.getBalance(connection));
                    break;
                }
                case 7: {
                    transactions = session.getTransactions();

                    if (transactions.isEmpty()) {
                        System.out.println("No transactions found");
                    } else {
                        for (Pair<Transaction, Integer> pair: transactions) {
                            Transaction transaction = pair.get1st();
                            int id = pair.get2nd();
                            System.out.println(
                                "Transaction ID: " + id + 
                                " Date: " + transaction.getDate() + 
                                " Type: " + transaction.getType() + 
                                " Amount: " + transaction.getAmount() + 
                                " Stock: " + transaction.getStockSymbol() + 
                                " Number of Shares: " + transaction.getNumberOfShares() + 
                                " Price per share: " + transaction.getPricePerShare());
                        }
                    }
                    break;
                }
                case 8: {
                    System.out.println("Enter stock symbol: ");
                    String symbol = scanner.nextLine();

                    System.out.println(symbol + " " + Stock.showInfo(connection, symbol));
                    System.out.println();
                    break;
                }
                case 9: {
                    System.out.println("Enter movie: ");
                    String movie = scanner.nextLine();

                    System.out.println("Enter year: ");
                    int year = scanner.nextInt();

                    try {
                        ArrayList<String> result = Movie.getInfo(connection, movie, year);
                        
                        for (String r : result) {
                            System.out.println(r);
                        }

                    } catch (SQLException e) {
                        System.out.print(e);
                        System.out.println("Movie not found");
                    }

                    break;

                }
                case 10:
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            System.out.println();
        }
    }


    public static void dataSetup(Connection connection) throws SQLException{

        createCustomer(connection, "Alfred Hitchcock" , "alfred" ,"hi" ,"CA" ,"(805)2574499", "alfred@hotmail.com" ,"000001022", 10000);
        createCustomer(connection, "Billy Clinton" , "billy", "cl", "CA","(805)5629999", "billy@yahoo.com", "000003045", 100000);
        createCustomer(connection, "Cindy Laugher","cindy","la","CA","(805)6930011", "cindy@hotmail.com", "000002034", 50000);
        createCustomer(connection, "David Copperfill","david","co","CA","(805)8240011","david@yahoo.com","000004093", 45000);
        createCustomer(connection, "Elizabeth Sailor","sailor","sa","CA","(805)1234567","sailor@hotmail.com","000001234", 200000);
        createCustomer(connection, "George Brush","brush","br","CA","(805)1357999","george@hotmail.com","000008956", 5000);
        createCustomer(connection, "Ivan Stock","ivan","st","NJ","(805)3223243","ivan@yahoo.com","000002341", 2000);
        createCustomer(connection, "Joe Pepsi","joe","pe","CA","(805)5668123","pepsi@pepsi.com","000000456", 10000);
        createCustomer(connection, "Magic Jordon","magic","jo","NJ","(805)4535539","jordon@jordon.org","000003455", 130200);
        createCustomer(connection, "Olive Stoner","olive","st","CA","(805)2574499","olive@yahoo.com","000001123", 35000);
        createCustomer(connection, "Frank Olson","frank","ol","CA","(805)3456789","frank@gmail.com","000003306", 30500);
        createCustomer(connection, "John Admin","admin","secret","CA","(805)6374632","admin@stock.com","000001000", 999999);

        createStock(connection, "SKB",40.00, "Kim Basinger", java.sql.Date.valueOf("1958-12-08"));
        createMovieContract(connection, "SKB", "L.A. Confidential", "Actor",1997,5000000);
        createRatingReview(connection, "L.A. Confidential", 1997, 10.0, "I loved it - it's almost as good as Chinatown!");
        createRatingReview(connection, "L.A. Confidential", 1997, 10.0, "Super clever story with an amazing cast as well.");


        createStock(connection, "SMD",71.00, "Michael Douglas", java.sql.Date.valueOf("1944-09-25"));
        createMovieContract(connection, "SMD", "A Perfect Murder", "Actor",1998,10000000);
        createRatingReview(connection, "A Perfect Murder", 1998, 6.1, "Truly one of the movies of all time.");

        createStock(connection, "STC",32.50, "Tom Cruise", java.sql.Date.valueOf("1962-07-03"));
        createMovieContract(connection, "STC", "Jerry Maguire", "Actor",1996,5000000);
        createRatingReview(connection, "Jerry Maguire", 1996, 8.3, "What an emotional rollercoaster!");

    

        createStockAccount(connection, "alfred", 100, "SKB");
        
        createStockAccount(connection, "billy", 500, "SMD");
        createStockAccount(connection, "billy", 100, "STC");

    
        createStockAccount(connection, "cindy", 250, "STC");
        
        createStockAccount(connection, "david", 100, "SKB");
        createStockAccount(connection, "david", 500, "SMD");
        createStockAccount(connection, "david", 50, "STC");

        createStockAccount(connection, "sailor", 1000, "SMD");

        createStockAccount(connection, "brush", 100, "SKB");

        createStockAccount(connection, "ivan", 300, "SMD");
        
        createStockAccount(connection, "joe", 500, "SKB");
        createStockAccount(connection, "joe", 100, "STC");
        createStockAccount(connection, "joe", 200, "SMD");

        createStockAccount(connection, "magic", 1000, "SKB");

        createStockAccount(connection, "olive", 100, "SKB");
        createStockAccount(connection, "olive", 100, "SMD");
        createStockAccount(connection, "olive", 100, "STC");

        createStockAccount(connection, "frank", 100, "SKB");
        createStockAccount(connection, "frank", 200, "STC");
        createStockAccount(connection, "frank", 100, "SMD");
        
    
    }





    public static void createCustomer(Connection connection, String name, String username, String password, String state, String phoneNumber, String email, String taxID, double balance) throws SQLException {
      CustomerAccountManager manager = new CustomerAccountManager();
      Customer newCustomer = new Customer(name, username, password, state, phoneNumber, email, taxID);
    
      int customerID = manager.registerCustomer(connection, newCustomer);

      MarketAccount account = new MarketAccount(customerID, balance, 10.0, 0);
      account.registerAccount(connection);
    }

    public static void createStock(Connection connection, String symbol, double currentPrice, String name, Date dob) {
        Stock stock = new Stock(symbol, currentPrice, currentPrice);
        stock.registerStock(connection);

        ActorDirector actordirector = new ActorDirector(name, dob, symbol);
        actordirector.registerActorDirector(connection);
        
    }

    public static void createMovieContract(Connection connection, String symbol, String movieTitle, String role,int year, double totalValue){
        Movie movie = new Movie(movieTitle, year);
        movie.registerMovie(connection);

        MovieContract contract = new MovieContract(symbol, movieTitle, role, year, totalValue);
        contract.addContract(connection);
    }

    public static void createRatingReview(Connection connection, String movieTitle, int year, double rating, String reviewText) {
        Reviews movieReview = new Reviews(movieTitle, year, rating, reviewText);
        movieReview.addReview(connection);
    }

    public static void createStockAccount(Connection connection, String username, double numberOfShares, String symbol) {
        StockAccount stockaccount = new StockAccount(username, numberOfShares, symbol);
        stockaccount.registerStockAccount(connection);
    }
}
