package exercise;

import exercise.streamExercise.Tasks_v1;
import exercise.streamExercise.data_v1.Customer;
import exercise.streamExercise.data_v1.Employee;
import exercise.streamExercise.data_v1.StreamTasksData;
import exercise.streamExercise.data_v1.Transaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static exercise.streamExercise.data_v1.StreamTasksData.*;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void findCustomersWithAllCategories() {
        // Test with actual data
        List<Customer> result = Tasks_v1.findCustomersWithAllCategories(transactions, customers);
        
        // Get all unique categories from transactions
        Set<String> allCategories = transactions.stream()
                .map(Transaction::getCategory)
                .collect(Collectors.toSet());
        
        // Verify that each returned customer has transactions in all categories
        for (Customer customer : result) {
            Set<String> customerCategories = transactions.stream()
                    .filter(t -> t.getCustomerId().equals(customer.getId()))
                    .map(Transaction::getCategory)
                    .collect(Collectors.toSet());
            
            assertThat(customerCategories)
                    .as("Customer %s should have transactions in all categories", customer.getName())
                    .containsAll(allCategories);
        }
        
        // Verify result is not null
        assertThat(result).isNotNull();
        
        // Log the result for verification
        System.out.println("Customers with" +
                " all categories: " + result.size());
        result.forEach(customer -> System.out.println("- " + customer.getName()));
        System.out.println("Total categories: " + allCategories.size());
        System.out.println("Categories: " + allCategories);
    }
}