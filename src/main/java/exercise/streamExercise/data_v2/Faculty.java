package exercise.streamExercise.data_v2;

import java.util.List;

public class Faculty {
    private Long id;
    private String name;
    private Long universityId;
    private String dean;
    private List<Course> courses;
    private Integer establishedYear;
    private FacultyType type;

    public Faculty(Long id, String name, Long universityId, String dean, 
                  List<Course> courses, Integer establishedYear, FacultyType type) {
        this.id = id;
        this.name = name;
        this.universityId = universityId;
        this.dean = dean;
        this.courses = courses;
        this.establishedYear = establishedYear;
        this.type = type;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getUniversityId() { return universityId; }
    public String getDean() { return dean; }
    public List<Course> getCourses() { return courses; }
    public Integer getEstablishedYear() { return establishedYear; }
    public FacultyType getType() { return type; }

    @Override
    public String toString() {
        return String.format("Faculty{id=%d, name='%s', type=%s}", 
                           id, name, type);
    }
}