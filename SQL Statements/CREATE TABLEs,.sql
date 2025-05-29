CREATE TABLE Department( 	did SMALLINT,
name CHAR(20),
PRIMARY KEY(did)
   );

CREATE TABLE Major (	mid SMALLINT,
name CHAR(20),
no_req_electives SMALLINT,
did smallint,
PRIMARY KEY(mid),
FOREIGN KEY (did) REFERENCES Department
);

CREATE TABLE Course(	cno CHAR(7),
enroll_code SMALLINT,
title CHAR(20),
PRIMARY KEY(cno),
UNIQUE(enroll_code)
);

CREATE TABLE Student(
perm CHAR(7),
name CHAR(20),
pin CHAR(4) NOT NULL,
address CHAR(50),
mid SMALLINT,
PRIMARY KEY(perm),
FOREIGN KEY (mid) REFERENCES Major,
CONSTRAINT chk_pin_format CHECK (
	REGEXP_LIKE(pin, '^[0-9]{4}$')
)
);

CREATE TABLE Offering(	oid SMALLINT,
year SMALLINT,
qtr CHAR(6),
cno CHAR(7),
PRIMARY KEY(oid),
FOREIGN KEY (cno) REFERENCES Course
ON DELETE CASCADE,
CONSTRAINT QtrName CHECK (qtr IN ('Winter', 'Spring', 'Fall'))
  );

CREATE TABLE TimeSlot(	tid SMALLINT,
start_time SMALLINT NOT NULL,
end_time SMALLINT NOT NULL,
day CHAR(2),
location CHAR(9),
enroll_lim SMALLINT NOT NULL,
PRIMARY KEY (tid),
CONSTRAINT ValidStart CHECK (start_time BETWEEN 0 AND 1440),
CONSTRAINT ValidEnd CHECK (end_time BETWEEN 0 AND 1440),
CONSTRAINT ValidDay CHECK (day IN ('M', 'Tu', 'W', 'Th', 'F', 'Sa', 'Su'))
);

CREATE TABLE Professor(	pid SMALLINT,
first_name CHAR(15),
last_name CHAR(15),
PRIMARY KEY (pid)
);

CREATE TABLE Scheduled_At(	oid SMALLINT,
tid SMALLINT NOT NULL,
PRIMARY KEY(oid, tid),
FOREIGN KEY (tid) REFERENCES TimeSlot
ON DELETE CASCADE,
FOREIGN KEY (oid) REFERENCES Offering
ON DELETE CASCADE
      );

CREATE TABLE Teaches(	pid SMALLINT NOT NULL,
oid SMALLINT,
PRIMARY KEY (pid, oid),
FOREIGN KEY (pid) REFERENCES Professor
ON DELETE CASCADE,
FOREIGN KEY (oid) REFERENCES Offering
ON DELETE CASCADE
);

CREATE TABLE Took(		perm CHAR(7),
oid SMALLINT,
grade CHAR(2),
PRIMARY KEY (perm, oid),
FOREIGN KEY (perm) REFERENCES Student,
FOREIGN KEY (oid) REFERENCES Offering
);

CREATE TABLE Enrolled_In(	perm CHAR(7),
oid SMALLINT,
PRIMARY KEY (perm, oid),
FOREIGN KEY (perm) REFERENCES Student,
FOREIGN KEY (oid) REFERENCES Offering
    );

CREATE TABLE Has_Mandatory(mid SMALLINT,
cno CHAR(7),
PRIMARY KEY (mid, cno),
FOREIGN KEY (mid) REFERENCES Major
ON DELETE CASCADE,
FOREIGN KEY (cno) REFERENCES Course
ON DELETE CASCADE
);

CREATE TABLE Has_Elective( mid SMALLINT,
cno CHAR(7),
PRIMARY KEY (mid, cno),
FOREIGN KEY (mid) REFERENCES Major
ON DELETE CASCADE,
FOREIGN KEY (cno) REFERENCES Course
ON DELETE CASCADE
);

CREATE TABLE Has_Prerequisite(	cno_parent CHAR(7),
   	cno_req CHAR(7),
PRIMARY KEY (cno_parent, cno_req),
FOREIGN KEY (cno_parent) REFERENCES Course(cno)
ON DELETE CASCADE,
FOREIGN KEY (cno_req) REFERENCES Course(cno) ON DELETE CASCADE
   );