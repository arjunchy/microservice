CREATE DATABASE IF NOT EXISTS employee_db;
CREATE DATABASE IF NOT EXISTS address_db;
CREATE DATABASE IF NOT EXISTS auth_db;

CREATE USER IF NOT EXISTS 'employee_user'@'%' IDENTIFIED BY 'employee_pwd';
CREATE USER IF NOT EXISTS 'address_user'@'%' IDENTIFIED BY 'address_pwd';
CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth_pwd';
CREATE USER IF NOT EXISTS 'employee_address_ro'@'%' IDENTIFIED BY 'employee_ro_pwd';

GRANT ALL PRIVILEGES ON employee_db.* TO 'employee_user'@'%';
GRANT ALL PRIVILEGES ON address_db.* TO 'address_user'@'%';
GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'%';
GRANT SELECT ON address_db.* TO 'employee_address_ro'@'%';

FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS auth_db.users (
  id bigint NOT NULL AUTO_INCREMENT,
  created_at datetime(6) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  password varchar(255) NOT NULL,
  role enum('ADMIN','USER') NOT NULL,
  username varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;

INSERT INTO auth_db.users (id, username, email, password, role, created_at) VALUES
  (1, 'alice',  'alice@example.com',  '$2a$10$6he70r2WaTplMBjNQdAXsuV.QMf8b3f8Jrm7ZlvCNV8FFggK.B/Ru', 'USER',  '2026-09-20 16:23:41.096275'),
  (2, 'root',   'root@example.com',   '$2a$10$i/VpXM5hrBNqsr/1mkx.G.ryFLHWCdzjIUDsvEbdZUQczS4AbrVv.', 'ADMIN', '2026-09-20 16:23:42.061210'),
  (5, 'john',   'john@example.com',   '$2a$10$gpafTQTluY5s9dDKriXEluyrDRw1GJZa7c8Xn/WBgUvQvu.l7LX3y', 'USER',  '2026-09-21 04:24:16.782471'),
  (6, 'newbie', 'newbie@example.com', '$2a$10$RUFMjOmivOr5d5bdPSObOOSD2JLR5zVnjxKfJJVhriiCeLrIg3RBC', 'USER',  '2026-09-21 04:31:35.628804');

CREATE TABLE IF NOT EXISTS employee_db.employee (
  id bigint NOT NULL AUTO_INCREMENT,
  company_name varchar(255) NOT NULL,
  created_at datetime(6) DEFAULT NULL,
  designation varchar(255) NOT NULL,
  emp_department varchar(255) NOT NULL,
  emp_email varchar(255) NOT NULL,
  emp_name varchar(255) NOT NULL,
  status enum('ACTIVE','INACTIVE','ON_LEAVE','TERMINATED') DEFAULT NULL,
  updated_at datetime(6) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_employee_email (emp_email)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;

INSERT INTO employee_db.employee (id, emp_name, emp_email, designation, emp_department, company_name, status, created_at, updated_at) VALUES
  (1, 'Alice Smith', 'alice@example.com', 'Software Engineer', 'Engineering', 'Acme Corp', 'ON_LEAVE',   '2026-09-20 16:23:27.732144', '2026-09-20 16:24:54.961501'),
  (2, 'Bob Jones',   'bob@example.com',   'DevOps Engineer',   'Operations',  'Globex',     'ACTIVE',    '2026-09-20 16:23:27.881889', '2026-09-20 16:23:27.881911'),
  (4, 'Test Admin',  'testadmin1790057867@example.com', 'Tester', 'QA', 'TestCorp', 'ACTIVE', '2026-09-22 06:17:48.354919', '2026-09-22 06:17:48.354957');

CREATE TABLE IF NOT EXISTS address_db.address (
  id bigint NOT NULL AUTO_INCREMENT,
  address_type enum('PERMANENT','TEMPORARY') NOT NULL,
  city varchar(255) NOT NULL,
  country varchar(255) NOT NULL,
  created_at datetime(6) DEFAULT NULL,
  employee_id bigint NOT NULL,
  updated_at datetime(6) DEFAULT NULL,
  zip_code varchar(255) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

INSERT INTO address_db.address (id, employee_id, city, country, zip_code, address_type, created_at, updated_at) VALUES
  (1, 1, 'New York', 'USA', '10001', 'PERMANENT', '2026-09-20 16:23:30.010669', '2026-09-21 11:21:18.416085'),
  (2, 2, 'London',   'UK',   'EC1A',  'TEMPORARY', '2026-09-20 16:23:30.748508', '2026-09-20 16:23:30.748539');