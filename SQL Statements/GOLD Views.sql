CREATE VIEW StudentCurrentCourses AS
SELECT S.perm, O.cno, P.last_name AS professor, Ts.start_time, Ts.end_time, Ts.day, Ts.location
FROM Student S
JOIN Enrolled_In E ON S.perm = E.perm
JOIN Offering O ON E.oid = O.oid
JOIN Teaches T ON O.oid = T.oid
JOIN Professor P ON T.pid = P.pid
JOIN Scheduled_At Sat ON O.oid = Sat.oid
JOIN Timeslot Ts ON Sat.tid = Ts.tid
ORDER BY S.perm

CREATE VIEW StudentGradeHistory AS
SELECT S.perm, O.year, O.qtr, O.cno, T.grade
FROM Student S
JOIN Took T ON S.perm = T.perm
JOIN Offering O ON T.oid = O.oid
ORDER BY S.perm, O.year, O.qtr

CREATE VIEW StudentPassedCourses AS
SELECT S.perm, O.cno
FROM Student S
JOIN Took T ON S.perm = T.perm
JOIN Offering O ON T.oid = O.oid   
WHERE T.grade >= 2
ORDER BY S.perm

CREATE VIEW StudentMandatoryCourses AS
SELECT S.perm, H.cno
FROM Student S
JOIN Major M ON S.mid = M.mid 
JOIN Has_Mandatory H ON M.mid = H.mid
ORDER BY S.perm

CREATE VIEW StudentElectiveCourses AS
SELECT S.perm, H.cno
FROM Student S
JOIN Major M ON S.mid = M.mid 
JOIN Has_Elective H ON M.mid = H.mid
ORDER BY S.perm