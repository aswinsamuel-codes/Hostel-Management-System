-- Hostel Management System Database Schema
-- MySQL 8+

DROP DATABASE IF EXISTS hostel_management;
CREATE DATABASE hostel_management CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE hostel_management;

-- Admin table
CREATE TABLE admin (
  admin_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL
);

-- Students table
CREATE TABLE students (
  student_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  reg_no VARCHAR(20) UNIQUE NOT NULL,
  department VARCHAR(50) NOT NULL,
  year INT NOT NULL,
  room_no INT,
  phone VARCHAR(15),
  email VARCHAR(100),
  address TEXT,
  guardian_name VARCHAR(100),
  guardian_phone VARCHAR(15)
);

-- Rooms table
CREATE TABLE rooms (
  room_no INT PRIMARY KEY,
  block_name VARCHAR(50) NOT NULL,
  capacity INT NOT NULL,
  occupied INT NOT NULL DEFAULT 0,
  availability BOOLEAN AS (occupied < capacity) STORED
);

-- Fees table
CREATE TABLE fees (
  fee_id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  status ENUM('Paid','Pending') NOT NULL DEFAULT 'Pending',
  due_date DATE,
  CONSTRAINT fk_fees_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- Staff table
CREATE TABLE staff (
  staff_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  role VARCHAR(50) NOT NULL,
  phone VARCHAR(15),
  email VARCHAR(100)
);

-- Complaints table
CREATE TABLE complaints (
  complaint_id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  complaint_text TEXT NOT NULL,
  date_filed DATE NOT NULL DEFAULT (CURRENT_DATE),
  status ENUM('Pending','Resolved') NOT NULL DEFAULT 'Pending',
  CONSTRAINT fk_complaints_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- Sample data
INSERT INTO admin (username, password) VALUES
('admin', 'admin123'); -- Replace with hashed in production

INSERT INTO rooms (room_no, block_name, capacity, occupied) VALUES
(101, 'A', 2, 1),
(102, 'A', 2, 0),
(201, 'B', 3, 2),
(202, 'B', 3, 3);

INSERT INTO students (name, reg_no, department, year, room_no, phone, email, address, guardian_name, guardian_phone) VALUES
('Alice Johnson', 'REG1001', 'CS', 2, 101, '9876543210', 'alice@example.com', '123, Street A', 'Mary Johnson', '9876500001'),
('Bob Smith', 'REG1002', 'EE', 3, 201, '9876543211', 'bob@example.com', '456, Street B', 'John Smith', '9876500002');

INSERT INTO fees (student_id, amount, status, due_date) VALUES
(1, 1500.00, 'Pending', DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY)),
(2, 1500.00, 'Paid', DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY));

INSERT INTO staff (name, role, phone, email) VALUES
('Carl Davis', 'Warden', '9876543212', 'carl@example.com'),
('Dana Lee', 'Maintenance', '9876543213', 'dana@example.com');

INSERT INTO complaints (student_id, complaint_text, status) VALUES
(1, 'Leaky faucet in bathroom', 'Pending'),
(2, 'Wi-Fi connectivity issues', 'Resolved');


