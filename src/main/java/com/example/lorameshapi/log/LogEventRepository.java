package com.example.lorameshapi.log;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEventRepository extends JpaRepository<LogEvent, Long> {
}
