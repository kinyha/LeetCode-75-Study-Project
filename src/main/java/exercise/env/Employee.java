package exercise.env;

import java.time.LocalDate;
import java.util.List;

public class Employee {
    private Long id;
    private String name;
    private String department;
    private String position;
    private Double salary;
    private LocalDate hireDate;
    private String city;

    public Employee(Long id, String name, String department, String position,
                    Double salary, LocalDate hireDate, String city) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.position = position;
        this.salary = salary;
        this.hireDate = hireDate;
        this.city = city;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    public Double getSalary() { return salary; }
    public LocalDate getHireDate() { return hireDate; }
    public String getCity() { return city; }

//    @Override
//    public String toString() {
//        return String.format("Employee{id=%d, name='%s', dept='%s', salary=%.0f}",
//                id, name, department, salary);
//    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", salary=" + salary +
                ", hireDate=" + hireDate +
                ", city='" + city + '\'' +
                '}';
    }

    //prety print list empl
    public static void printEmployees(List<Employee> employees) {
        for (Employee employee : employees) {
            System.out.println(employee);
        }
    }
}
