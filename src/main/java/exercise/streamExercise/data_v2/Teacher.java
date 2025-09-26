package exercise.streamExercise.data_v2;

import java.time.LocalDate;
import java.util.List;

public class Teacher {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long facultyId;
    private AcademicRank rank;
    private Double salary;
    private Integer experienceYears;
    private List<String> specializations;
    private LocalDate hireDate;
    private List<Long> courseIds;
    private Boolean isTenured;
    private String officeNumber;

    public Teacher(Long id, String firstName, String lastName, String email,
                  Long facultyId, AcademicRank rank, Double salary, Integer experienceYears,
                  List<String> specializations, LocalDate hireDate, List<Long> courseIds,
                  Boolean isTenured, String officeNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.facultyId = facultyId;
        this.rank = rank;
        this.salary = salary;
        this.experienceYears = experienceYears;
        this.specializations = specializations;
        this.hireDate = hireDate;
        this.courseIds = courseIds;
        this.isTenured = isTenured;
        this.officeNumber = officeNumber;
    }

    // Getters
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public Long getFacultyId() { return facultyId; }
    public AcademicRank getRank() { return rank; }
    public Double getSalary() { return salary; }
    public Integer getExperienceYears() { return experienceYears; }
    public List<String> getSpecializations() { return specializations; }
    public LocalDate getHireDate() { return hireDate; }
    public List<Long> getCourseIds() { return courseIds; }
    public Boolean getIsTenured() { return isTenured; }
    public String getOfficeNumber() { return officeNumber; }

    @Override
    public String toString() {
        return String.format("Teacher{id=%d, name='%s %s', rank=%s, experience=%d years}", 
                           id, firstName, lastName, rank, experienceYears);
    }
}