package com.example.bookCalender.repository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.example.bookCalender.models.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class EmployeeRepositoryTest {


        @Autowired
        private EmployeeRepository employeeRepository;

        private Employee employee;

        @BeforeEach
        public void setUp() {
            employee = new Employee();
            employee.setName("John Doe");
            employee.setEmail("john.doe@example.com");
            employeeRepository.save(employee);
        }

        @Test
        public void testFindById() {
            Optional<Employee> found = employeeRepository.findById(employee.getId());
            assertEquals(found.get().getName(), "John Doe");
        }

        @Test
        public void testSaveEmployee() {
            Employee newEmployee = new Employee();
            newEmployee.setName("Jane Doe");
            newEmployee.setEmail("jane.doe@example.com");
            Employee savedEmployee = employeeRepository.save(newEmployee);
            assertEquals(savedEmployee.getName(), "Jane Doe");
        }
    }
