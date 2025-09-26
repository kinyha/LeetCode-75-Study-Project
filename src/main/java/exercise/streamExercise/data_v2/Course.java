package exercise.streamExercise.data_v2;

import java.util.List;

public class Course {
    private Long id;
    private String name;
    private String code;
    private Long facultyId;
    private Long teacherId;
    private Integer credits;
    private Integer semester;
    private CourseLevel level;
    private List<Student> enrolledStudents;
    private Integer maxCapacity;
    private Boolean isElective;

    public Course(Long id, String name, String code, Long facultyId, Long teacherId,
                 Integer credits, Integer semester, CourseLevel level, 
                 List<Student> enrolledStudents, Integer maxCapacity, Boolean isElective) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.facultyId = facultyId;
        this.teacherId = teacherId;
        this.credits = credits;
        this.semester = semester;
        this.level = level;
        this.enrolledStudents = enrolledStudents;
        this.maxCapacity = maxCapacity;
        this.isElective = isElective;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public Long getFacultyId() { return facultyId; }
    public Long getTeacherId() { return teacherId; }
    public Integer getCredits() { return credits; }
    public Integer getSemester() { return semester; }
    public CourseLevel getLevel() { return level; }
    public List<Student> getEnrolledStudents() { return enrolledStudents; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public Boolean getIsElective() { return isElective; }

    @Override
    public String toString() {
        return String.format("Course{id=%d, name='%s', code='%s', level=%s, credits=%d}", 
                           id, name, code, level, credits);
    }
}