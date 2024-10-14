package com.example.bookCalender.controller;

import com.example.bookCalender.DTO.MeetingDTO;
import com.example.bookCalender.DTO.SlotDTO;
import com.example.bookCalender.models.Employee;
import com.example.bookCalender.models.Meeting;
import com.example.bookCalender.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
    @Autowired
    private MeetingService meetingService;

    @PostMapping("/book")
    public ResponseEntity<Meeting> bookMeeting(@RequestBody MeetingDTO meetingDTO) {
        Meeting bookedMeeting = meetingService.bookMeeting(meetingDTO);
        return ResponseEntity.ok(bookedMeeting);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<List<SlotDTO>> findAvailableSlots(@RequestParam Long emp1Id,
                                                            @RequestParam Long emp2Id,
                                                            @RequestParam int durationMinutes) {
        List<SlotDTO> availableSlots = meetingService.findAvailableSlots(emp1Id, emp2Id, durationMinutes);
        return ResponseEntity.ok(availableSlots);
    }

    @PostMapping("/conflicts")
    public ResponseEntity<List<Employee>> findConflicts(@RequestBody MeetingDTO meetingDTO) {
        List<Employee> conflictingParticipants = meetingService.findConflicts(meetingDTO);
        return ResponseEntity.ok(conflictingParticipants);
    }
}
