import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MovieContract {
    private String actorDirectorID;
    private String movieTitle;
    private String role; // Actor, Director, or Both
    private int year;
    private double totalValue;


    public MovieContract(String actorDirectorID, String movieTitle, String role, int year, double totalValue){
        this.actorDirectorID = actorDirectorID;
        this.movieTitle = movieTitle;
        this.role = role;
        this.year = year;
        this.totalValue = totalValue;
    }

    public void addContract(Connection connection) {
        String sql = "INSERT INTO Contract (ActorDirectorID, MovieTitle, Role, Year, TotalValue) VALUES (?, ?, ?, ?, ?)";

        try {
             PreparedStatement pstmt = connection.prepareStatement(sql);

            pstmt.setString(1, this.actorDirectorID);
            pstmt.setString(2, this.movieTitle);
            pstmt.setString(3, this.role);
            pstmt.setInt(4, this.year);
            pstmt.setDouble(5, this.totalValue);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
