package com.example.bookCalender.repository;

import com.example.bookCalender.models.Meeting;
import com.example.bookCalender.models.MeetingParticipants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MeetingParticipantsRepository extends JpaRepository<MeetingParticipants,Long> {
    List<MeetingParticipants> findByMeetingId(Long meetingId);
    List<MeetingParticipants> findByParticipantId(Long employeeId);
    @Query("SELECT mp.meeting FROM MeetingParticipants mp WHERE mp.participant.id = :participantId")
    List<Meeting> findMeetingsByParticipantId(@Param("participantId") Long participantId);
}
