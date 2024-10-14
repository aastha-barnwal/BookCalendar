package com.example.bookCalender.service;
import com.example.bookCalender.DTO.MeetingDTO;
import com.example.bookCalender.DTO.SlotDTO;
import com.example.bookCalender.models.Employee;
import com.example.bookCalender.repository.EmployeeRepository;
import com.example.bookCalender.repository.MeetingParticipantsRepository;
import com.example.bookCalender.repository.MeetingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import com.example.bookCalender.models.Meeting;
import com.example.bookCalender.models.MeetingParticipants;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class MeetingServiceTest {

    @InjectMocks
    private MeetingService meetingService;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private MeetingParticipantsRepository participantsRepository;

    @Test
    public void testBookMeeting() {
        // Given
        Employee owner = new Employee();
        owner.setId(1L);
        owner.setName("Alice");

        MeetingDTO meetingDTO = new MeetingDTO();
        meetingDTO.setTitle("Team Sync");
        meetingDTO.setStartTime(LocalDateTime.of(2024, 10, 16, 10, 0));
        meetingDTO.setEndTime(LocalDateTime.of(2024, 10, 16, 11, 0));
        meetingDTO.setOwnerId(1L);
        meetingDTO.setParticipantIds(Arrays.asList(2L, 3L));

        // When
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Meeting bookedMeeting = meetingService.bookMeeting(meetingDTO);

        // Then
        assertNotNull(bookedMeeting);
        assertEquals("Team Sync", bookedMeeting.getTitle());
        verify(participantsRepository, times(2)).save(any(MeetingParticipants.class));
    }

    @Test
    public void testFindAvailableSlots() {
        // Given
        Long emp1Id = 1L;
        Long emp2Id = 2L;
        int durationMinutes = 30;

        List<Meeting> emp1Meetings = Arrays.asList(
                new Meeting(1L, "Meeting 1", LocalDateTime.of(2024, 10, 15, 10, 0), LocalDateTime.of(2024, 10, 15, 11, 0), null),
                new Meeting(2L, "Meeting 2", LocalDateTime.of(2024, 10, 15, 12, 0), LocalDateTime.of(2024, 10, 15, 13, 0), null)
        );
        List<Meeting> emp2Meetings = Arrays.asList(
                new Meeting(3L, "Meeting 3", LocalDateTime.of(2024, 10, 15, 9, 0), LocalDateTime.of(2024, 10, 15, 10, 30), null)
        );

        // When
        when(meetingRepository.findByOwnerId(emp1Id)).thenReturn(emp1Meetings);
        when(meetingRepository.findByOwnerId(emp2Id)).thenReturn(emp2Meetings);

        // Act
        List<SlotDTO> availableSlots = meetingService.findAvailableSlots(emp1Id, emp2Id, durationMinutes);

        // Then
        assertFalse(availableSlots.isEmpty());
        assertEquals(1, availableSlots.size());
        assertEquals(LocalDateTime.of(2024, 10, 15, 11, 0), availableSlots.get(0).getStartTime());
    }

    @Test
    public void testFindConflicts() {
        // Given
        LocalDateTime meetingStart = LocalDateTime.of(2024, 10, 16, 10, 0);
        LocalDateTime meetingEnd = LocalDateTime.of(2024, 10, 16, 11, 0);

        MeetingDTO meetingDTO = new MeetingDTO();
        meetingDTO.setStartTime(meetingStart);
        meetingDTO.setEndTime(meetingEnd);
        meetingDTO.setParticipantIds(Arrays.asList(2L, 3L));

        List<Meeting> participant1Meetings = Arrays.asList(
                new Meeting(1L, "Conflict 1", LocalDateTime.of(2024, 10, 16, 10, 30), LocalDateTime.of(2024, 10, 16, 11, 30), null)
        );

        List<Meeting> participant2Meetings = Arrays.asList();

        Employee participant1 = new Employee(2L, "John Doe", "john.doe@example.com");
        Employee participant2 = new Employee(3L, "Jane Smith", "jane.smith@example.com");

        // When
        when(meetingRepository.findByOwnerId(2L)).thenReturn(participant1Meetings);
        when(meetingRepository.findByOwnerId(3L)).thenReturn(participant2Meetings);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(participant1));
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(participant2));

        // Act
        List<Employee> conflicts = meetingService.findConflicts(meetingDTO);

        // Then
        assertFalse(conflicts.isEmpty());
        assertEquals(1, conflicts.size());
        assertEquals(participant1.getName(), conflicts.get(0).getName());
    }
}