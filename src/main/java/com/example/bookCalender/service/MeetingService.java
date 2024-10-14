package com.example.bookCalender.service;

import com.example.bookCalender.DTO.MeetingDTO;
import com.example.bookCalender.DTO.SlotDTO;
import com.example.bookCalender.models.Employee;
import com.example.bookCalender.models.Meeting;
import com.example.bookCalender.models.MeetingParticipants;
import com.example.bookCalender.repository.EmployeeRepository;
import com.example.bookCalender.repository.MeetingParticipantsRepository;
import com.example.bookCalender.repository.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MeetingService {
    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MeetingParticipantsRepository participantsRepository;

    public Meeting bookMeeting(MeetingDTO meetingDTO) {
        Employee owner = employeeRepository.findById(meetingDTO.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Meeting meeting = new Meeting();
        meeting.setTitle(meetingDTO.getTitle());
        meeting.setStartTime(meetingDTO.getStartTime());
        meeting.setEndTime(meetingDTO.getEndTime());
        meeting.setOwner(owner);

        meeting = meetingRepository.save(meeting);

        for (Long participantId : meetingDTO.getParticipantIds()) {
            Employee participant = employeeRepository.findById(participantId)
                    .orElseThrow(() -> new RuntimeException("Participant not found"));

            MeetingParticipants participantEntry = new MeetingParticipants();
            participantEntry.setMeeting(meeting);
            participantEntry.setParticipant(participant);
            participantEntry.setStatus("confirmed");

            participantsRepository.save(participantEntry);
        }

        return meeting;
    }

    // Method for finding available slots between two employees
    public List<SlotDTO> findAvailableSlots(Long emp1Id, Long emp2Id, int durationMinutes) {
        List<Meeting> emp1Meetings = meetingRepository.findByOwnerId(emp1Id);
        List<Meeting> emp2Meetings = meetingRepository.findByOwnerId(emp2Id);

        List<Meeting> allMeetings = new ArrayList<>();
        allMeetings.addAll(emp1Meetings);
        allMeetings.addAll(emp2Meetings);

        // Sort meetings by start time
        allMeetings.sort(Comparator.comparing(Meeting::getStartTime));

        // Merge overlapping or adjacent meetings
        List<Meeting> mergedMeetings = mergeMeetings(allMeetings);

        // Find gaps between merged meetings

        return findGaps(mergedMeetings, durationMinutes);
    }

    private List<Meeting> mergeMeetings(List<Meeting> meetings) {
        List<Meeting> merged = new ArrayList<>();

        if (meetings.isEmpty()) return merged;
        Meeting current = meetings.get(0);

        for (int i = 1; i < meetings.size(); i++) {
            Meeting next = meetings.get(i);

            // If next meeting starts after the current one ends, they are not overlapping
            if (next.getStartTime().isAfter(current.getEndTime())) {
                merged.add(current);  // Add the current meeting to merged list
                current = next;  // Move to the next meeting
            } else {
                // Merge meetings by updating the end time of the current meeting
                current.setEndTime(current.getEndTime().isAfter(next.getEndTime()) ? current.getEndTime() : next.getEndTime());
            }
        }

        merged.add(current);  // Add the last meeting to the merged list
        return merged;
    }

    private List<SlotDTO> findGaps(List<Meeting> meetings, int durationMinutes) {
        List<SlotDTO> availableSlots = new ArrayList<>();

        // Define the working hours (9 AM to 5 PM)
        LocalDateTime workStartTime = LocalDateTime.now().withHour(9).withMinute(0);
        LocalDateTime workEndTime = LocalDateTime.now().withHour(17).withMinute(0);

        // Check for a gap before the first meeting
        if (!meetings.isEmpty() && meetings.get(0).getStartTime().isAfter(workStartTime)) {
            availableSlots.addAll(getSlotsBetween(workStartTime, meetings.get(0).getStartTime(), durationMinutes));
        }

        // Find gaps between meetings
        for (int i = 0; i < meetings.size() - 1; i++) {
            LocalDateTime endOfCurrent = meetings.get(i).getEndTime();
            LocalDateTime startOfNext = meetings.get(i + 1).getStartTime();

            if (startOfNext.isAfter(endOfCurrent)) {
                availableSlots.addAll(getSlotsBetween(endOfCurrent, startOfNext, durationMinutes));
            }
        }

        // Check for a gap after the last meeting until the end of the working day
        if (!meetings.isEmpty() && meetings.get(meetings.size() - 1).getEndTime().isBefore(workEndTime)) {
            availableSlots.addAll(getSlotsBetween(meetings.get(meetings.size() - 1).getEndTime(), workEndTime, durationMinutes));
        }

        return availableSlots;
    }

    private List<SlotDTO> getSlotsBetween(LocalDateTime start, LocalDateTime end, int durationMinutes) {
        List<SlotDTO> slots = new ArrayList<>();

        while (start.plusMinutes(durationMinutes).isBefore(end) || start.plusMinutes(durationMinutes).isEqual(end)) {
            SlotDTO slot = new SlotDTO();
            slot.setStartTime(start);
            slot.setEndTime(start.plusMinutes(durationMinutes));

            slots.add(slot);

            // Move to the next possible slot
            start = start.plusMinutes(durationMinutes);
        }

        return slots;
    }

    // Method for checking meeting conflicts for participants
    public List<Employee> findConflicts(MeetingDTO meetingDTO) {
        LocalDateTime requestedStart = meetingDTO.getStartTime();
        LocalDateTime requestedEnd = meetingDTO.getEndTime();

        List<Employee> conflictingParticipants = new ArrayList<>();

        for (Long participantId : meetingDTO.getParticipantIds()) {
            // Get all meetings that the participant is involved in
            List<Meeting> participantMeetings = participantsRepository.findMeetingsByParticipantId(participantId);

            // Check if the participant has a conflict with the requested meeting time
            boolean hasConflict = participantMeetings.stream()
                    .anyMatch(meeting -> isOverlapping(meeting.getStartTime(), meeting.getEndTime(), requestedStart, requestedEnd));

            if (hasConflict) {
                // Add participant to conflicting list if they have a conflict
                Employee conflictingEmployee = employeeRepository.findById(participantId)
                        .orElseThrow(() -> new RuntimeException("Employee not found"));
                conflictingParticipants.add(conflictingEmployee);
            }
        }

        return conflictingParticipants;
    }

    private boolean isOverlapping(LocalDateTime existingStart, LocalDateTime existingEnd, LocalDateTime requestedStart, LocalDateTime requestedEnd) {
        return (requestedStart.isBefore(existingEnd) && requestedEnd.isAfter(existingStart));
    }
}
