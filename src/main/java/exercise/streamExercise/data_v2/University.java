package exercise.streamExercise.data_v2;

import java.util.List;

public class University {
    private Long id;
    private String name;
    private String city;
    private String country;
    private Integer ranking;
    private List<Faculty> faculties;
    private Integer foundedYear;
    private Double tuitionFee;

    public University(Long id, String name, String city, String country, Integer ranking, 
                     List<Faculty> faculties, Integer foundedYear, Double tuitionFee) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.country = country;
        this.ranking = ranking;
        this.faculties = faculties;
        this.foundedYear = foundedYear;
        this.tuitionFee = tuitionFee;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public Integer getRanking() { return ranking; }
    public List<Faculty> getFaculties() { return faculties; }
    public Integer getFoundedYear() { return foundedYear; }
    public Double getTuitionFee() { return tuitionFee; }

    @Override
    public String toString() {
        return String.format("University{id=%d, name='%s', city='%s', ranking=%d}", 
                           id, name, city, ranking);
    }
}