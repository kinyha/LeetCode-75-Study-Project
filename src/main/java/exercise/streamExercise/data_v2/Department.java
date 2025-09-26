package exercise.streamExercise.data_v2;

import java.util.List;

public class Department {
    private Long id;
    private String name;
    private Long companyId;
    private String manager;
    private Double budget;
    private List<String> employees;
    private DepartmentType type;
    private Integer headcount;
    private String location;

    public Department(Long id, String name, Long companyId, String manager,
                     Double budget, List<String> employees, DepartmentType type,
                     Integer headcount, String location) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.manager = manager;
        this.budget = budget;
        this.employees = employees;
        this.type = type;
        this.headcount = headcount;
        this.location = location;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getCompanyId() { return companyId; }
    public String getManager() { return manager; }
    public Double getBudget() { return budget; }
    public List<String> getEmployees() { return employees; }
    public DepartmentType getType() { return type; }
    public Integer getHeadcount() { return headcount; }
    public String getLocation() { return location; }

    @Override
    public String toString() {
        return String.format("Department{id=%d, name='%s', type=%s, headcount=%d}", 
                           id, name, type, headcount);
    }
}