package exercise.streamExercise;

import java.util.Map;

/**
 * Класс для хранения статистики по студентам
 */
public class StudentStatistics {
    public final int totalCount;
    public final double minGpa;
    public final double maxGpa;
    public final double avgGpa;
    public final int scholarshipCount;
    public final Map<Integer, Long> yearDistribution;

    public StudentStatistics(int totalCount, double minGpa, double maxGpa, double avgGpa, 
                           int scholarshipCount, Map<Integer, Long> yearDistribution) {
        this.totalCount = totalCount;
        this.minGpa = minGpa;
        this.maxGpa = maxGpa;
        this.avgGpa = avgGpa;
        this.scholarshipCount = scholarshipCount;
        this.yearDistribution = yearDistribution;
    }
}