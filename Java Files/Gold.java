package org.ivc.dbms.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class Gold {

    //[TESTED] Prints out a list of currently enrolled courses (S25) and other information
    //Requires the StudentCurrentCourses SQL view to be instantiated
    public static void listCourseSchedule(Connection conn, String perm) {
        String query = "SELECT cno, professor, start_time, end_time, day, location "
                       +"FROM StudentCurrentCourses SCC WHERE TRIM(perm) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            ResultSet rs = pstatement.executeQuery();
            String cno, professor, start_time, end_time, day, location;
            System.out.println("Course | Professor | Start Time | End Time | Day | Location");
            System.out.println("===========================================================");
            while(rs.next()) {
                cno = rs.getString("cno");
                professor = rs.getString("professor");
                start_time = rs.getString("start_time");
                end_time = rs.getString("end_time");
                day = rs.getString("day");
                location = rs.getString("location");

                System.out.println(cno+" | "+professor+" | "+start_time+" | "+end_time+" | "+day+" | "+location);
            }

        } catch(SQLException e) {
            System.out.println("ERROR: Could not list course schedule.");
            System.out.println(e);
        }
    }

    //[TESTED] Prints out the grade history of a given student (perm)
    public static void listGradeHistory(Connection conn, String perm) {
        String query = "SELECT year, qtr, cno, grade "
                       +"FROM StudentGradeHistory SGH WHERE TRIM(perm) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            ResultSet rs = pstatement.executeQuery();
            String year = "", prev_year = "", qtr = "", prev_qtr = "", cno = "";
            double grade = 0.0, avg_grade = 0.0;
            int num_courses = 0;
            System.out.println("Year | Qtr | Course | Grade");
            System.out.println("===========================");
            while(rs.next()) {
                prev_year = year;
                prev_qtr = qtr;
                year = rs.getString("year");
                qtr = rs.getString("qtr");
                cno = rs.getString("cno");
                grade = Float.valueOf(rs.getString("grade"));

                if((!prev_year.equals(year) || !prev_qtr.equals(qtr)) && !prev_year.equals("")) {
                    avg_grade /= num_courses;
                    System.out.println("Average GPA for "+prev_year+prev_qtr+": "+avg_grade);
                    System.out.println("---------------------------");
                    avg_grade = grade;
                    num_courses = 1;
                }
                else {
                    avg_grade += grade; //Will normalize when printing
                    num_courses++;
                }

                System.out.println(year+" | "+qtr+" | "+cno+" | "+grade);
            }
            avg_grade /= num_courses;
            System.out.println("Average GPA for "+year+qtr+": "+avg_grade);
            System.out.println("---------------------------");

        } catch(SQLException e) {
            System.out.println("ERROR: Could not list course schedule.");
            System.out.println(e);
        }
    }

    //
    public static void listRequirementCheck(Connection conn, String perm) {
        Set<String> courses = new HashSet();
    }
}
