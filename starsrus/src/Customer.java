public class Customer {
    private String name;
    private String state;
    private String phoneNumber;
    private String email;
    private String taxID;
    private String username;
    private String password;

    public Customer(String name, String username, String password, String state, String phoneNumber, String email, String taxID) {
        this.name = name;
        this.state = state;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.taxID = taxID;
        this.username = username;
        this.password = password;
    }

    public String getName() { return name; }
    public String getState() { return state; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getTaxID() { return taxID; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

}
