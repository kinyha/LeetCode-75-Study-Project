package exercise.yandex.tg;

import java.util.*;
import java.util.stream.Collectors;

class Scratch {

    public static void main(String[] args) {
        List<Country> countries = Arrays.asList(new Country("country_1", 100, 5000),
                new Country("country_2", 9000, 500000),
                new Country("country_8", 6527, 6324687),
                new Country("country_11", 872321, 765237),
                new Country("country_9", 823743, 63543762),
                new Country("country_3", 800, 40000));

        var c = getTheBiggestCountry(countries);
        System.out.println(c);
    }

    public static Country getTheBiggestCountry(List<Country> countries) {
        //res  = pop / area
        var area = countries
                .stream()
                .collect(Collectors.toMap(country -> country, country -> (double) country.population / country.area))
                .entrySet().stream().min(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .get().getKey();
        countries.stream()
                .max(Comparator.comparingDouble(c -> (double) c.getPopulation() / c.getArea()))
                .orElse(null);
        return area;
    }

    static class Country {

        public final String name;
        public final double area;
        public final long population;

        public Country(String name, double area, long population) {
            this.name = name;
            this.area = area;
            this.population = population;
        }

        public String getName() {
            return name;
        }

        public double getArea() {
            return area;
        }

        public long getPopulation() {
            return population;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Country)) {
                return false;
            }
            Country country = (Country) o;
            return Double.compare(country.area, area) == 0 && population == country.population && name.equals(country.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, area, population);
        }

        @Override
        public String toString() {
            return "Country{" +
                    "name='" + name + '\'' +
                    ", area=" + area +
                    ", population=" + population +
                    '}';
        }
    }
}