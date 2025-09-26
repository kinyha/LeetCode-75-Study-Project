package exercise.streamExercise.data_v2;

import java.time.LocalDate;
import java.util.List;

public class Project {
    private Long id;
    private String title;
    private String description;
    private List<Long> studentIds;
    private Long courseId;
    private Long companyId; // может быть спонсором или партнером
    private LocalDate startDate;
    private LocalDate dueDate;
    private ProjectStatus status;
    private Double budget;
    private ProjectType type;
    private Integer difficulty; // 1-10

    public Project(Long id, String title, String description, List<Long> studentIds,
                  Long courseId, Long companyId, LocalDate startDate, LocalDate dueDate,
                  ProjectStatus status, Double budget, ProjectType type, Integer difficulty) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.studentIds = studentIds;
        this.courseId = courseId;
        this.companyId = companyId;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
        this.budget = budget;
        this.type = type;
        this.difficulty = difficulty;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Long> getStudentIds() { return studentIds; }
    public Long getCourseId() { return courseId; }
    public Long getCompanyId() { return companyId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public ProjectStatus getStatus() { return status; }
    public Double getBudget() { return budget; }
    public ProjectType getType() { return type; }
    public Integer getDifficulty() { return difficulty; }

    @Override
    public String toString() {
        return String.format("Project{id=%d, title='%s', status=%s, type=%s, students=%d}", 
                           id, title, status, type, studentIds.size());
    }
}