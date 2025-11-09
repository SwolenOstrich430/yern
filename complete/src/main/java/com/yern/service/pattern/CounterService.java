package com.yern.service.pattern;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yern.dto.pattern.CounterLogCreateRequest;
import com.yern.model.pattern.CounterLog;
import com.yern.repository.pattern.CounterLogRepository;

import jakarta.transaction.Transactional;

@Service
public class CounterService {

    private CounterLogRepository counterLogRepository; 

    public CounterService(
        @Autowired CounterLogRepository counterLogRepository
    ) {
        this.counterLogRepository = counterLogRepository;
    }

    public List<CounterLog> createLogs(List<CounterLogCreateRequest> logs) {
        return counterLogRepository.saveAll(
            logs.stream().map(log -> CounterLog.from(log)).toList()
        );
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void aggregateAndDeleteLogs() {
        counterLogRepository.aggregateAndDeleteLogs(100);
    }
}
