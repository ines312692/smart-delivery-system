package com.example.monitoringservice.repository;


import com.example.monitoringservice.entity.EventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    List<EventLog> findByEventType(String eventType);

    List<EventLog> findBySourceService(String sourceService);

    Page<EventLog> findByCreatedAtBetween(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT e FROM EventLog e ORDER BY e.createdAt DESC")
    Page<EventLog> findLatestEvents(Pageable pageable);

    @Query("SELECT COUNT(e) FROM EventLog e WHERE e.eventType = ?1")
    Long countByEventType(String eventType);

    @Query("SELECT COUNT(e) FROM EventLog e WHERE e.status = ?1")
    Long countByStatus(String status);

    @Query("SELECT e.eventType, COUNT(e) FROM EventLog e GROUP BY e.eventType")
    List<Object[]> countEventsByType();
}
