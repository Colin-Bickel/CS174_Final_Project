package org.ivc.dbms.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Registrar {
    //This finds all students in a class by course number (across all offerings)
    public static void listStudentsInClass(Connection conn, String cno) {
        String query = "SELECT perm, name FROM ClassListOfStudents CLS WHERE TRIM(CLS.cno) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, cno);
            ResultSet rs = pstatement.executeQuery();
            String perm, name;

            System.out.println("Students currently enrolled in course "+cno+":");
            System.out.println("perm | name");
            while(rs.next()) {
                perm = rs.getString("perm");
                name = rs.getString("name");
                System.out.println(perm+" | "+name);
            }
        } catch(SQLException e) {
            System.out.println("ERROR: Could not list students in class "+cno+".");
            System.out.println(e);
        }
    }

    public static ResultSet getCurrentCourses(Connection conn, String perm) throws SQLException{
        String query = "SELECT cno FROM StudentCurrentCourses SCC WHERE TRIM(perm) = TRIM(?)";
        PreparedStatement pstatement = conn.prepareStatement(query);
        pstatement.setString(1, perm);
        return pstatement.executeQuery();
    }

    public static String getName(Connection conn, String perm) {
        String query = "SELECT TRIM(name) AS name FROM Student WHERE TRIM(perm) = TRIM(?)";
        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            ResultSet rs = pstatement.executeQuery();
            if(rs.next()) {
                return rs.getString("name");
            }
        } catch(SQLException e) {
            System.out.println("ERROR: Could not get the name of student for perm: "+perm+".");
            System.out.println(e);
        }

        return "null";
    }

    public static int getNumRequiredMajorCourses(Connection conn, String perm) {
        String query = "SELECT COUNT(*) AS num_courses FROM Student S "
                      +"JOIN Has_Mandatory H ON S.mid = H.mid "
                      +"WHERE TRIM(perm) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            ResultSet rs = pstatement.executeQuery();
            if(rs.next()) {
                return rs.getInt("num_courses");
            }
        } catch(SQLException e) {
            System.out.println("ERROR: Could not query number of required major courses.");
            System.out.println(e);
        }

        return 0;
    }

    public static void requestTranscript(Connection conn, String perm) {
        String studentName = getName(conn, perm);
        System.out.println("==========================================");
        System.out.println("Transcript for "+studentName+" (PERM "+perm+")");
        System.out.println("==========================================");
        System.out.println("Current Courses ("+Config.CURRENT_YEAR+Config.CURRENT_QTR+"):");
        
        Set<String> currentCourses = new HashSet();
        try(ResultSet rs = getCurrentCourses(conn, perm)) {
            String cno;
            while(rs.next()) {
                cno = rs.getString("cno");
                System.out.println("IP "+cno);
                currentCourses.add(cno);
            }
        } catch(SQLException e) {
            System.out.println("ERROR: Could not get current courses for student "+perm+".");
            System.out.println(e);
        }
        System.out.println();

        Gold.listGradeHistory(conn, perm);

        ArrayList<String> requiredCourses = Gold.getRemainingRequiredCourses(conn, perm);
        ArrayList<String> remainingElectives = new ArrayList();
        int num_comp_electives = Gold.getRemainingElectiveCourses(conn, perm, remainingElectives);
        int num_req_electives = Gold.getNumRequiredElectives(conn, perm);
        int num_req_major_courses = getNumRequiredMajorCourses(conn, perm);
        
        System.out.print((num_req_major_courses - requiredCourses.size())+" of "+num_req_major_courses+" required major courses completed.");
        if(!requiredCourses.isEmpty()) {
            System.out.print(" Remaining courses required:");
        }
        System.out.println();
        for(String cno : requiredCourses) {
            if(currentCourses.contains(cno)) {
                System.out.print("IP ");
            }
            else {
                System.out.print("-- ");
            }
            System.out.print(cno+"\n");
        }
        System.out.println();

        if(num_comp_electives > num_req_electives) {
            num_comp_electives = num_req_electives;
        }
        System.out.print(num_comp_electives+" of "+num_req_electives+" required elective courses completed.");
        if(num_comp_electives < num_req_electives) {
            System.out.print(" Remaining course options:");
            System.out.println();
            for(String cno : remainingElectives) {
                if(currentCourses.contains(cno)) {
                    System.out.print("IP ");
                }
                else {
                    System.out.print("-- ");
                }
                System.out.print(cno+"\n");
            }
        }
        else {
            System.out.println();
        }
        System.out.println("==========================================");
    }
}