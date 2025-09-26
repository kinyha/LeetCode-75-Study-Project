package exercise.streamExercise.data_v2;

import java.time.LocalDate;

public class Grade {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Double score;
    private GradeType type;
    private LocalDate date;
    private String description;
    private Integer attempt;

    public Grade(Long id, Long studentId, Long courseId, Double score, 
                GradeType type, LocalDate date, String description, Integer attempt) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.score = score;
        this.type = type;
        this.date = date;
        this.description = description;
        this.attempt = attempt;
    }

    // Getters
    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public Long getCourseId() { return courseId; }
    public Double getScore() { return score; }
    public GradeType getType() { return type; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public Integer getAttempt() { return attempt; }

    @Override
    public String toString() {
        return String.format("Grade{studentId=%d, courseId=%d, score=%.2f, type=%s}", 
                           studentId, courseId, score, type);
    }
}