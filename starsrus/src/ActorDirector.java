import java.util.Date;

import java.sql.Connection;
import java.sql.SQLException;

import java.sql.PreparedStatement;


public class ActorDirector {
    private String name;
    private Date dateOfBirth;
    private String stocksymbol;

    public ActorDirector(String name, Date dateOfBirth, String stocksymbol) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.stocksymbol = stocksymbol;
    }

    public void registerActorDirector(Connection connection) {
        String sql = "INSERT INTO ActorDirector (name, DOB, stocksymbol) VALUES (?, ?, ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, this.name);
            pstmt.setDate(2, (java.sql.Date) this.dateOfBirth);
            pstmt.setString(3, this.stocksymbol);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

