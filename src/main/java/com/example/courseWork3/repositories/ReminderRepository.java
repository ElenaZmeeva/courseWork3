package com.example.courseWork3.repositories;

import com.example.courseWork3.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Integer> {

    List<Reminder> findAllBySendTime(LocalDateTime now);
}
