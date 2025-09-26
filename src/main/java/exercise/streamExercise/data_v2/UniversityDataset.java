package exercise.streamExercise.data_v2;

import java.util.List;

public class UniversityDataset {
    private final List<University> universities;
    private final List<Faculty> faculties;
    private final List<Course> courses;
    private final List<Student> students;
    private final List<Teacher> teachers;
    private final List<Grade> grades;
    private final List<Project> projects;
    private final List<Company> companies;

    public UniversityDataset(List<University> universities, List<Faculty> faculties,
                           List<Course> courses, List<Student> students, List<Teacher> teachers,
                           List<Grade> grades, List<Project> projects, List<Company> companies) {
        this.universities = universities;
        this.faculties = faculties;
        this.courses = courses;
        this.students = students;
        this.teachers = teachers;
        this.grades = grades;
        this.projects = projects;
        this.companies = companies;
    }

    // Getters
    public List<University> getUniversities() { return universities; }
    public List<Faculty> getFaculties() { return faculties; }
    public List<Course> getCourses() { return courses; }
    public List<Student> getStudents() { return students; }
    public List<Teacher> getTeachers() { return teachers; }
    public List<Grade> getGrades() { return grades; }
    public List<Project> getProjects() { return projects; }
    public List<Company> getCompanies() { return companies; }

    // Statistics
    public void printDatasetStatistics() {
        System.out.println("=== University Dataset Statistics ===");
        System.out.println("Universities: " + universities.size());
        System.out.println("Faculties: " + faculties.size());
        System.out.println("Courses: " + courses.size());
        System.out.println("Students: " + students.size());
        System.out.println("Teachers: " + teachers.size());
        System.out.println("Grades: " + grades.size());
        System.out.println("Projects: " + projects.size());
        System.out.println("Companies: " + companies.size());
        System.out.println("====================================");
    }
}