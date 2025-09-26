package exercise.streamExercise;

import exercise.streamExercise.data_v2.Student;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Кастомный Collector для сбора комплексной статистики по студентам
 */
public class StudentStatisticsCollector implements Collector<Student, StudentStatisticsCollector.Accumulator, StudentStatistics> {

    public static class Accumulator {
        private int count = 0;
        private double minGpa = Double.MAX_VALUE;
        private double maxGpa = Double.MIN_VALUE;
        private double sumGpa = 0.0;
        private int scholarshipCount = 0;
        private Map<Integer, Long> yearDistribution = new HashMap<>();

        public void add(Student student) {
            count++;
            double gpa = student.getGpa();
            sumGpa += gpa;
            minGpa = Math.min(minGpa, gpa);
            maxGpa = Math.max(maxGpa, gpa);
            
            if (student.getHasScholarship()) {
                scholarshipCount++;
            }
            
            yearDistribution.merge(student.getYear(), 1L, Long::sum);
        }

        public Accumulator combine(Accumulator other) {
            this.count += other.count;
            this.sumGpa += other.sumGpa;
            this.minGpa = Math.min(this.minGpa, other.minGpa);
            this.maxGpa = Math.max(this.maxGpa, other.maxGpa);
            this.scholarshipCount += other.scholarshipCount;
            
            other.yearDistribution.forEach((year, count) -> 
                this.yearDistribution.merge(year, count, Long::sum));
            
            return this;
        }

        public StudentStatistics finish() {
            double avgGpa = count > 0 ? sumGpa / count : 0.0;
            return new StudentStatistics(count, minGpa, maxGpa, avgGpa, scholarshipCount, yearDistribution);
        }
    }

    @Override
    public Supplier<Accumulator> supplier() {
        return Accumulator::new;
    }

    @Override
    public BiConsumer<Accumulator, Student> accumulator() {
        return Accumulator::add;
    }

    @Override
    public BinaryOperator<Accumulator> combiner() {
        return Accumulator::combine;
    }

    @Override
    public Function<Accumulator, StudentStatistics> finisher() {
        return Accumulator::finish;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}