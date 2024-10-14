package com.example.bookCalender.repository;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.bookCalender.models.Meeting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.example.bookCalender.models.Employee;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class MeetingRepositoryTest {
        @Autowired
        private MeetingRepository meetingRepository;

        @Autowired
        private EmployeeRepository employeeRepository;

        private Employee owner;
        private Meeting meeting;

        @BeforeEach
        public void setUp() {
            owner = new Employee();
            owner.setName("John Doe");
            owner.setEmail("john.doe@example.com");
            owner = employeeRepository.save(owner);

            meeting = new Meeting();
            meeting.setTitle("Team Meeting");
            meeting.setStartTime(LocalDateTime.now());
            meeting.setEndTime(LocalDateTime.now().plusHours(1));
            meeting.setOwner(owner);
            meetingRepository.save(meeting);
        }

        @Test
        public void testFindByOwnerId() {
            List<Meeting> meetings = meetingRepository.findByOwnerId(owner.getId());
            assertEquals(1, meetings.size());
        }
    }
