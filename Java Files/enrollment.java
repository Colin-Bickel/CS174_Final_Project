package org.ivc.dbms.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class Enrollment {

    //Modify and use this to test other functions
    public static void testFunctions(Connection conn, String perm, String cno) {
        // try(ResultSet rs = queryOfferingsForCourse(conn, cno)) {
        //     while(rs.next()) {
        //         System.out.println(rs.getString("oid"));
        //     }
        // } catch(SQLException e) {
        //     System.out.println("ERROR: Could not query prereqs.");
        //     System.out.println(e);
        // }
        dropClass(conn, perm, cno);
    }

    //[TESTED] This returns the results for all prerequisites given a course number
    public static ResultSet queryPrereqs(Connection conn, String cno) throws SQLException {
        String queryPrereqs = "SELECT TRIM(cno_req) AS cno FROM Has_Prerequisite WHERE TRIM(cno_parent) = TRIM(?)";

        PreparedStatement pstatement = conn.prepareStatement(queryPrereqs);
        pstatement.setString(1, cno);

        return pstatement.executeQuery();
    }

    //[TESTED] This returns the courses which a student (perm) passed with a C or above
    public static ResultSet queryPassedCourses(Connection conn, String perm) throws SQLException {
        String queryPassedCourses = "SELECT TRIM(O.cno) AS cno "
                            + "FROM Student S, Took T, Offering O "  
                            + "WHERE TRIM(S.perm) = TRIM(?) AND S.perm = T.perm AND T.grade >= 2.0 AND T.oid = O.oid";

        PreparedStatement pstatement = conn.prepareStatement(queryPassedCourses);
        pstatement.setString(1, perm);

        return pstatement.executeQuery();
    }

    //[TESTED] This returns T/F whether a student (perm) met all preres for a course (number)
    public static boolean studentMetPrereqs(Connection conn, String perm, String cno) {
        Set<String> passedCourses = new HashSet<>();

        try (ResultSet rs = queryPassedCourses(conn, perm)) {
            while (rs.next()) {
                passedCourses.add(rs.getString("cno"));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not query prereqs.");
            System.out.println(e);
        }

        try (ResultSet rs = queryPrereqs(conn, cno)) {
            String prereq;
            while (rs.next()) {
                prereq = rs.getString("cno");
                if(!passedCourses.contains(prereq)) {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not query passed courses.");
            System.out.println(e);
        }

        return true;
    }

    //[TESTED] This returns the results of all the offerings (oid) for a given course (number) in the most recent quarter (which student can enroll in)
    public static ResultSet queryOfferingsForCourse(Connection conn, String cno) throws SQLException{
        String year = "2025", qtr = "S"; //This needs to be updated to be most recent qtr/year!!! (only for real implementation)
        String query = "SELECT TRIM(O.oid) AS oid "
                      +"FROM Offering O "
                      +"LEFT JOIN ( "
                          +"SELECT E1.oid, COUNT(*) AS num_enrolled "
                          +"FROM Enrolled_In E1 "
                          +"GROUP BY E1.oid "
                      +") E ON O.oid = E.oid "
                      +"WHERE TRIM(O.cno) = TRIM(?) AND O.year = TRIM(?) AND O.qtr = TRIM(?) "
                      +"AND O.enroll_lim > COALESCE(E.num_enrolled, 0)";

        PreparedStatement pstatement = conn.prepareStatement(query);
        pstatement.setString(1, cno);
        pstatement.setString(2, year);
        pstatement.setString(3, qtr);
        return pstatement.executeQuery();
    }

    //[TESTED] This returns the first available offering (oid) if the student can enroll in the course (cno)
    //--Otherwise it returns the STRING "null"
    public static String oidForStudentToEnrollInCourse(Connection conn, String perm, String cno) {
        if(!studentMetPrereqs(conn, perm, cno) || studentIsInCourse(conn, perm, cno)){
            return "null";
        }
        
        try(ResultSet rs = queryOfferingsForCourse(conn, cno)) {

            if(!rs.isAfterLast()) {
                rs.next();
                return rs.getString("oid");
            }

        } catch(SQLException e) {
            System.out.println("ERROR: Could not fetch current offerings for course "+cno+".");
            System.out.println(e);
        }

        return "null";
    }

    //[TESTED] Returns T/F whether a student has more than one enrolled course (and therefore can drop a course)
    //--Returns False if the student is not enrolled in any courses (there are none to drop)
    public static boolean studentCanDrop(Connection conn, String perm) {
        String query = "SELECT COUNT(*) AS num_courses FROM Enrolled_In WHERE TRIM(perm) = TRIM(?)";

        try (PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            ResultSet rs = pstatement.executeQuery();
            rs.next();
            if(rs.getInt("num_courses") > 1) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not fetch currently enrolled courses.");
            System.out.println(e);
        }

        return false;
    }

    //[TESTED] Returns T/F if the student is currently enrolled in the course
    public static boolean studentIsInCourse(Connection conn, String perm, String cno) {
        String query = "SELECT COUNT(*) AS num_courses FROM Enrolled_In E, Offering O WHERE TRIM(E.perm) = TRIM(?) AND E.oid = O.oid AND TRIM(O.cno) = TRIM(?)";

        try (PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            pstatement.setString(2, cno);
            ResultSet rs = pstatement.executeQuery();
            rs.next();
            if(rs.getInt("num_courses") == 1) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not fetch currently enrolled courses.");
            System.out.println(e);
        }

        return false;
    }

    public static void unenrollInClass(Connection conn, String perm, String oid) {
        String query = "DELETE FROM Enrolled_In E WHERE TRIM(E.perm) = TRIM(?) AND TRIM(E.oid) = TRIM(?)";

        try (PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            pstatement.setString(2, oid);
            int num_rows_updated = pstatement.executeUpdate();
            if(num_rows_updated < 1) {
                System.out.println("INFO: No offering with oid "+oid+" could be unenrolled from student with perm "+perm+".");
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not unenroll a class.");
            System.out.println(e);
        }
    }

    public static void dropClass(Connection conn, String perm, String cno) {
        if(studentCanDrop(conn, perm) && studentIsInCourse(conn, perm, cno)) {
            String query = "SELECT TRIM(O.oid) AS oid FROM Enrolled_In E, Offering O WHERE E.oid = O.oid AND TRIM(E.perm) = TRIM(?) AND TRIM(O.cno) = TRIM(?)";

            try (PreparedStatement pstatement = conn.prepareStatement(query)) {
                pstatement.setString(1, perm);
                pstatement.setString(2, cno);
                ResultSet rs = pstatement.executeQuery();
                rs.next();
                unenrollInClass(conn, perm, rs.getString("oid"));
            } catch (SQLException e) {
                System.out.println("ERROR: Could not drop a class.");
                System.out.println(e);
            }
        }
        else {
            System.out.println("Student with perm "+perm+" can not drop course "+cno+" because they are not in the course or they are not enrolled in more than one course.");
        }
    }

    //[TESTED] Adds a perm,oid row into the Enrolled_In table (does not check any constraints)
    public static void enrollInClass(Connection conn, String perm, String oid) {
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

    //Enrolls a student (perm) in a course (cno) if they meet all requirements and there is an available offering
    public static void addClass(Connection conn, String perm, String cno) {
        String oid = oidForStudentToEnrollInCourse(conn, perm, cno);
        if(!oid.equals("null")) {
            enrollInClass(conn, perm, oid);
        }
        else {
            System.out.println("INFO: Could not enroll "+perm+" in course "+cno+". Either no offering exists or the student in ineligible to add."); 
        }
    }

    public static void printEnrollment(Connection conn) {
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
        } catch (SQLException e) {
            System.out.println("ERROR: Could not fetch currently enrolled courses.");
            System.out.println(e);
        }
    }
}
