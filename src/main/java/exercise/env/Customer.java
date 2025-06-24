package exercise.env;

import java.time.LocalDate;

public class Customer {
    private Long id;
    private String name;
    private String email;
    private LocalDate registrationDate;
    private String city;
    private CustomerType type;
    private boolean isActive;

    public Customer(Long id, String name, String email, LocalDate registrationDate,
                    String city, CustomerType type, boolean isActive) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.registrationDate = registrationDate;
        this.city = city;
        this.type = type;
        this.isActive = isActive;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public String getCity() { return city; }
    public CustomerType getType() { return type; }
    public boolean isActive() { return isActive; }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, name='%s', type=%s, active=%s}",
                id, name, type, isActive);
    }
}

