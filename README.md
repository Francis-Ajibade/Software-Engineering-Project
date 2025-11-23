✅ README.md — Project Description (Detailed but Short)
Programming Assignment Testing Tool

This application allows professors to automatically evaluate Java programming assignments submitted by students. The system manages test suites and test cases, compiles each student’s code, executes tests, and presents clear pass/fail results with detailed comparisons.

Core Features

Automated Assignment Retrieval
Locates student submissions from a user-specified root folder and identifies source code inside a defined subdirectory (e.g., src/).

Test Suite & Test Case Management
Professors can create, edit, delete, and reuse test suites.
Each test case contains a title, input data, and expected output.

Execution of Test Suites
For each student submission, the system:

Compiles the Java program

Executes each test case

Records output, errors, and pass/fail status

Skips execution for submissions that fail to compile

Results & Reporting
Displays expected vs. actual output side-by-side.
Provides structured results per student and per test suite, including compilation errors and runtime failures.

Robust Error Handling
Detects invalid folder paths, missing src directories, malformed file names, and duplicate submissions.

System Architecture

Coordinator: Central controller that manages execution flow between UI, test cases, test suites, and student programs.

Program & ListOfProgram: Represent each student’s submission and its compilation/ execution state.

TestCase / TestSuite: Define the evaluation logic and expected program behavior.

Results: Captures output, comparison, and final pass/fail status for each test.

Purpose

This tool automates grading workflows, ensures consistency in evaluating student code, and provides professors with fast, reliable feedback for large batches of assignment submissions.