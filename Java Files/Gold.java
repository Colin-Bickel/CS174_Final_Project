package org.ivc.dbms.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public static ResultSet getGradeHistory(Connection conn, String perm) throws SQLException {
        String query = "SELECT year, qtr, cno, grade "
                       +"FROM StudentGradeHistory SGH WHERE TRIM(perm) = TRIM(?)";
        PreparedStatement pstatement = conn.prepareStatement(query);
        pstatement.setString(1, perm);
        return pstatement.executeQuery();  
    }

    //[TESTED] Prints out the grade history of a given student (perm)
    public static void listGradeHistory(Connection conn, String perm) {
        try(ResultSet rs = getGradeHistory(conn, perm)) {
            String year = "", prev_year, qtr = "", prev_qtr, cno;
            double grade, avg_grade = 0.0, total_avg_grade = 0.0;
            int num_courses = 0, total_num_courses = 0;
            System.out.println("Grade History");
            System.out.println("---------------------------");
            while(rs.next()) {
                prev_year = year;
                prev_qtr = qtr;
                year = rs.getString("year");
                qtr = rs.getString("qtr");
                cno = rs.getString("cno");
                grade = Double.valueOf(rs.getString("grade"));

                if((!prev_year.equals(year) || !prev_qtr.equals(qtr))) {
                    if(!prev_year.equals("")) {
                        avg_grade /= num_courses;
                        System.out.println("Average GPA: "+avg_grade);
                        System.out.println("---------------------------");
                        avg_grade = grade;
                        num_courses = 1;
                    }
                    System.out.println("      "+year+qtr);
                }
                else {
                    avg_grade += grade; //Will normalize when printing
                    num_courses++;
                }
                total_avg_grade += grade;
                total_num_courses++;

                System.out.println(cno+" | "+grade);
            }
            avg_grade /= num_courses;
            total_avg_grade /= total_num_courses;
            System.out.println("Average GPA: "+avg_grade);
            System.out.println("---------------------------");
            System.out.println("Total Average GPA: "+total_avg_grade);
            System.out.println();

        } catch(SQLException e) {
            System.out.println("ERROR: Could not get grade history.");
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
            if(rs.next()) {
                return rs.getInt("num_elect");
            }
        } catch(SQLException e) {
            System.out.println("ERROR: Could not query the number of required elective courses.");
            System.out.println(e);
        }

        return 0;
    }

    public static ResultSet getPassedCourses(Connection conn, String perm) throws SQLException {
        String query = "SELECT cno "
                       +"FROM StudentPassedCourses SPC WHERE TRIM(perm) = TRIM(?)";
        PreparedStatement pstatement = conn.prepareStatement(query);
        pstatement.setString(1, perm);
        return pstatement.executeQuery();  
    }

    public static ResultSet getRequiredCourses(Connection conn, String perm) throws SQLException {
        String query = "SELECT cno "
                       +"FROM StudentMandatoryCourses SMC WHERE TRIM(perm) = TRIM(?)";
        PreparedStatement pstatement = conn.prepareStatement(query);
        pstatement.setString(1, perm);
        return pstatement.executeQuery();  
    }

    public static ResultSet getElectiveCourses(Connection conn, String perm) throws SQLException {
        String query = "SELECT cno "
                       +"FROM StudentElectiveCourses SEC WHERE TRIM(perm) = TRIM(?)";
        PreparedStatement pstatement = conn.prepareStatement(query);
        pstatement.setString(1, perm);
        return pstatement.executeQuery();  
    }

    public static ArrayList<String> getRemainingRequiredCourses(Connection conn, String perm) {
        Set<String> passedCourses = new HashSet();
        ArrayList<String> requiredCourses = new ArrayList();

        //Passed courses by student
        try(ResultSet rs = getPassedCourses(conn, perm)) {      
            while(rs.next()) {
                passedCourses.add(rs.getString("cno"));
            }

        } catch(SQLException e) {
            System.out.println("ERROR: Could not list course schedule.");
            System.out.println(e);
        }

        try(ResultSet rs = getRequiredCourses(conn, perm)) {
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

        return requiredCourses;
    }

    //[PARTIALLY TESTED] Returns the number of electives completed (can be greater than number required)
    //Overwrites the remainingElectives array
    public static int getRemainingElectiveCourses(Connection conn, String perm, ArrayList<String> remainingElectives) {
        Set<String> passedCourses = new HashSet();
        int num_comp_electives = 0;

        //Passed courses by student
        try(ResultSet rs = getPassedCourses(conn, perm)) {      
            while(rs.next()) {
                passedCourses.add(rs.getString("cno"));
            }

        } catch(SQLException e) {
            System.out.println("ERROR: Could not list course schedule.");
            System.out.println(e);
        }

        try(ResultSet rs = getElectiveCourses(conn, perm)) {
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

        return num_comp_electives;
    }

    //[PARTIALLY TESTED] Prints out the remaining required and elective courses for the student's major
    public static void listRequirementCheck(Connection conn, String perm) {
        int num_req_electives = getNumRequiredElectives(conn, perm);
        ArrayList<String> remainingElectives = new ArrayList();
        ArrayList<String> requiredCourses = getRemainingRequiredCourses(conn, perm);
        int num_comp_electives = getRemainingElectiveCourses(conn, perm, remainingElectives);

        System.out.println("Number of required courses remaining: "+requiredCourses.size());
        for(String cno : requiredCourses) {
            System.out.print(cno+" ");
        }
        System.out.println();
        num_req_electives = (num_comp_electives >= num_req_electives) ? 0 : num_req_electives - num_comp_electives;
        System.out.println("Number of elective courses remaining: "+num_req_electives);
        for(String cno : remainingElectives) {
            System.out.print(cno+" ");
        }
        System.out.println();
    }

    //[PARTIALLY TESTED] Prints out the courses that a student should take next quarter (to finish major requirements) provided they are able to
    public static void listCoursePlan(Connection conn, String perm) {
        int num_courses = 0;
        int num_req_electives = getNumRequiredElectives(conn, perm);
        ArrayList<String> remainingElectives = new ArrayList();
        ArrayList<String> requiredCourses = getRemainingRequiredCourses(conn, perm);
        int num_comp_electives = getRemainingElectiveCourses(conn, perm, remainingElectives);

        if(!requiredCourses.isEmpty() || (num_req_electives > num_comp_electives)) {
            System.out.println("Recommended course plan for "+Config.CURRENT_YEAR+Config.CURRENT_QTR+":");
        }
        else {
            System.out.println("All major requirements met. No course plan can be offered.");
        }

        for(String cno : requiredCourses) {
            if(Enrollment.studentMetPrereqs(conn, perm, cno) && Enrollment.isCourseOffered(conn, cno) && num_courses < Config.MAX_CLASSES) {
                System.out.println(cno);
                num_courses++;
            }
        }

        int min_num_additional_courses = (num_req_electives - num_comp_electives < Config.MAX_CLASSES - num_courses) ? num_req_electives - num_comp_electives : Config.MAX_CLASSES - num_courses;

        if(min_num_additional_courses >= 1) {
            System.out.println("Choose up to "+min_num_additional_courses+" courses from:");

            for(String cno : remainingElectives) {
                if(Enrollment.studentMetPrereqs(conn, perm, cno) && Enrollment.isCourseOffered(conn, cno)) {
                    System.out.println(cno);
                }
            }
        }
    }

    public static boolean isValidPIN(Connection conn, String perm, String PIN) {
        String query = "SELECT COUNT(*) AS valid FROM Student S WHERE TRIM(S.perm) = TRIM(?) AND TRIM(S.pin) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(query)) {
            pstatement.setString(1, perm);
            pstatement.setString(2, PIN);
            ResultSet rs = pstatement.executeQuery();
            if(rs.next()) {
                return rs.getInt("valid") == 1;
            }
        } catch(SQLException e) {
            System.out.println("ERROR: Could not verify if PIN is correct for student "+perm);
            System.out.println(e);
        }

        return false;
    }

    public static void updatePINValue(Connection conn, String perm, String newPIN) {
        String update = "UPDATE Student S SET S.pin = TRIM(?) WHERE TRIM(S.perm) = TRIM(?)";

        try(PreparedStatement pstatement = conn.prepareStatement(update)) {
            
            pstatement.setString(1, newPIN);
            pstatement.setString(2, perm);
            int rowsUpdated = pstatement.executeUpdate();
            if(rowsUpdated < 1) {
                System.out.println("ERROR: Failed to change the PIN for student "+perm);
            }
        } catch(SQLException e) {
            System.out.println("ERROR: Could not change the PIN for student "+perm);
            System.out.println(e);
        }
    }

    public static void changePIN(Connection conn, String perm, String oldPin, String newPIN) {
        if(isValidPIN(conn, perm, oldPin)) {
            updatePINValue(conn, perm, newPIN);
        }
        else {
            System.out.println("INFO: Unable to change the PIN for student "+perm+" because their current PIN does not match the provided PIN.");
        }
    }
}
