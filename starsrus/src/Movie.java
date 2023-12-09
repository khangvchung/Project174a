import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.ArrayList;

public class Movie {
    private String title;
    private int productionYear;

    public Movie(String title, int productionYear){
        this.title = title;
        this.productionYear = productionYear;
    }

    public void registerMovie(Connection connection){
        String sql = "INSERT INTO Movie (title, year) VALUES (?, ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);

           pstmt.setString(1, this.title);
           pstmt.setInt(2, this.productionYear);
           pstmt.executeUpdate();
       } catch (SQLException e) {
           e.printStackTrace();
       }
    }

    public static ArrayList<String> getInfo(Connection connection, String movie, int year) throws SQLException {
        String sql = "SELECT * from Reviews WHERE movietitle = ? and movieyear = ?";

        ArrayList<String> result = new ArrayList<>();

        try (PreparedStatement sequenceStmt = connection.prepareStatement(sql)) {
            sequenceStmt.setString(1, movie);
            sequenceStmt.setInt(2, year);
            ResultSet rs = sequenceStmt.executeQuery();
            while (rs.next()) {
                String review = rs.getString(4);
                double rating = rs.getDouble(5);
                
                result.add("Review: " + review + " Rating: " + rating);
            }

            return result;

        } catch (SQLException e) {
            throw e;
        }
    }
}
