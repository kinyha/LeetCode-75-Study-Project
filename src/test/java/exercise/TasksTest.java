package exercise;

import exercise.env.Employee;
import exercise.env.StreamTasksData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static exercise.env.StreamTasksData.employees;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TasksTest {
    //prep data
    @BeforeAll
    static void setUp() {
        List<Employee> employees = StreamTasksData.employees;

    }

    @Test
    void findExperiencedItEmployeeAboveAvg() {

        //date less than now - 2yer
        //assertThat(Tasks.findExperiencedItEmployeeAboveAvg(employees)).map(Employee::getHireDate).allMatch(localDate -> LocalDate.now().minusYears(localDate.getYear()).getYear() > 2);
    }
}