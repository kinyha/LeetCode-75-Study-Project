package exercise.streamExercise.data_v2;

import java.time.LocalDate;
import java.util.List;

public class Student {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private Long facultyId;
    private Integer year;
    private Double gpa;
    private String city;
    private String country;
    private StudentStatus status;
    private List<Grade> grades;
    private List<Project> projects;
    private Boolean hasScholarship;
    private Double scholarshipAmount;

    public Student(Long id, String firstName, String lastName, String email,
                  LocalDate birthDate, Long facultyId, Integer year, Double gpa,
                  String city, String country, StudentStatus status,
                  List<Grade> grades, List<Project> projects, Boolean hasScholarship,
                  Double scholarshipAmount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.facultyId = facultyId;
        this.year = year;
        this.gpa = gpa;
        this.city = city;
        this.country = country;
        this.status = status;
        this.grades = grades;
        this.projects = projects;
        this.hasScholarship = hasScholarship;
        this.scholarshipAmount = scholarshipAmount;
    }

    // Getters
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public LocalDate getBirthDate() { return birthDate; }
    public Long getFacultyId() { return facultyId; }
    public Integer getYear() { return year; }
    public Double getGpa() { return gpa; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public StudentStatus getStatus() { return status; }
    public List<Grade> getGrades() { return grades; }
    public List<Project> getProjects() { return projects; }
    public Boolean getHasScholarship() { return hasScholarship; }
    public Double getScholarshipAmount() { return scholarshipAmount; }

    @Override
    public String toString() {
        return String.format("Student{id=%d, name='%s %s', year=%d, gpa=%.2f, status=%s}", 
                           id, firstName, lastName, year, gpa, status);
    }
}