package com.example.bookCalender.repository;

import com.example.bookCalender.models.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByOwnerId(Long ownerId);
    List<Meeting> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

}
