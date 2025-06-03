CREATE VIEW ClassListOfStudents AS
SELECT O.oid, O.cno, S.perm, S.name
FROM Student S
JOIN Enrolled_In E ON S.perm = E.perm
JOIN Offering O ON E.oid = O.oid
ORDER BY O.oid, S.perm