import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Reviews {
    private String movieTitle;
    private int movieYear;
    private double rating;
    private String reviewText;

    public Reviews(String movieTitle, int movieYear, double rating, String reviewText) {
        this.movieTitle = movieTitle;
        this.movieYear = movieYear;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public void addReview(Connection connection) {
        String sql = "INSERT INTO Reviews (MovieTitle, MovieYear, rating, ReviewText) VALUES (?, ?, ?, ?)";



        try {
             PreparedStatement pstmt = connection.prepareStatement(sql);

            pstmt.setString(1, this.movieTitle);
            pstmt.setDouble(2, this.movieYear);
            pstmt.setDouble(3, this.rating);
            pstmt.setString(4, this.reviewText);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
