package exercise.streamExercise.data_v2;

import java.util.List;

public class Company {
    private Long id;
    private String name;
    private String industry;
    private String city;
    private String country;
    private CompanySize size;
    private List<Department> departments;
    private Integer employeeCount;
    private Double revenue;
    private Integer foundedYear;
    private Boolean isPublic;

    public Company(Long id, String name, String industry, String city, String country,
                  CompanySize size, List<Department> departments, Integer employeeCount,
                  Double revenue, Integer foundedYear, Boolean isPublic) {
        this.id = id;
        this.name = name;
        this.industry = industry;
        this.city = city;
        this.country = country;
        this.size = size;
        this.departments = departments;
        this.employeeCount = employeeCount;
        this.revenue = revenue;
        this.foundedYear = foundedYear;
        this.isPublic = isPublic;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getIndustry() { return industry; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public CompanySize getSize() { return size; }
    public List<Department> getDepartments() { return departments; }
    public Integer getEmployeeCount() { return employeeCount; }
    public Double getRevenue() { return revenue; }
    public Integer getFoundedYear() { return foundedYear; }
    public Boolean getIsPublic() { return isPublic; }

    @Override
    public String toString() {
        return String.format("Company{id=%d, name='%s', industry='%s', size=%s, employees=%d}", 
                           id, name, industry, size, employeeCount);
    }
}