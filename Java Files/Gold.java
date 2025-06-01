package org.ivc.dbms.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

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

    //[TESTED] Returns the number of required electives for the student's major
    public static int getNumRequiredElectives(Connection conn, String perm) {
        String query = "SELECT M.no_req_electives AS num_elect "
                       +"FROM Student S JOIN Major M ON S.mid = M.mid WHERE TRIM(perm) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            ResultSet rs = pstatement.executeQuery();
            rs.next();
            return rs.getInt("num_elect");
        } catch(SQLException e) {
            System.out.println("ERROR: Could not list course schedule.");
            System.out.println(e);
        }

        return 0;
    }

    //[PARTIALLY TESTED] Prints out the remaining required and elective courses for the student's major
    public static void listRequirementCheck(Connection conn, String perm) {

        Set<String> passedCourses = new HashSet();
        Vector<String> requiredCourses = new Vector();
        Vector<String> remainingElectives = new Vector();
        int num_req_electives = getNumRequiredElectives(conn, perm);
        int num_comp_electives = 0;

        String query = "SELECT cno "
                       +"FROM StudentPassedCourses SPC WHERE TRIM(perm) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            ResultSet rs = pstatement.executeQuery();        
            while(rs.next()) {
                passedCourses.add(rs.getString("cno"));
            }

        } catch(SQLException e) {
            System.out.println("ERROR: Could not list course schedule.");
            System.out.println(e);
        }

        query = "SELECT cno "
                       +"FROM StudentMandatoryCourses SMC WHERE TRIM(perm) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            ResultSet rs = pstatement.executeQuery();
            String cno;
        
            while(rs.next()) {
                cno = rs.getString("cno");
                if(!passedCourses.contains(cno)) {
                    requiredCourses.add(cno);
                }
            }

        } catch(SQLException e) {
            System.out.println("ERROR: Could not list course schedule.");
            System.out.println(e);
        }

        query = "SELECT cno "
                       +"FROM StudentElectiveCourses SEC WHERE TRIM(perm) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            ResultSet rs = pstatement.executeQuery();
            String cno;
        
            while(rs.next()) {
                cno = rs.getString("cno");
                if(!passedCourses.contains(cno)) {
                    remainingElectives.add(cno);
                }
                else {
                    num_comp_electives++;
                }
            }

        } catch(SQLException e) {
            System.out.println("ERROR: Could not list course schedule.");
            System.out.println(e);
        }

        System.out.println("Number of required courses remaining: "+requiredCourses.size());
        for(String str : requiredCourses) {
            System.out.print(str+" ");
        }
        System.out.println();
        num_req_electives = (num_comp_electives >= num_req_electives) ? 0 : num_req_electives - num_comp_electives;
        System.out.println("Number of elective courses remaining: "+num_req_electives);
        for(String str : remainingElectives) {
            System.out.print(str+" ");
        }
        System.out.println();
    }
}
