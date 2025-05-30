package org.ivc.dbms.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Enrollment {

    public static void addClass(Connection conn, String perm, String oid) throws SQLException{
        String insertSQL = "INSERT INTO Enrolled_In (perm, oid) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, perm);
            pstmt.setString(2, oid);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Inserted row (" + perm + ", " + oid + ") into Enrolled_In");
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert row into Enrolled_In");            
            System.out.println(e);
        }
    }

    public static void printEnrollment(Connection conn) throws Exception {
        try (Statement statement = conn.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM Enrolled_In"
                )
            ) {
                System.out.println("Enrolled Courses:");
                System.out.println("perm\toid");
                while (resultSet.next()) {
                    System.out.println(
                        resultSet.getString("perm") + "\t"
                        + resultSet.getString("oid")
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: Could not fetch currently enrolled courses.");
            System.out.println(e);
        }
    }
}
