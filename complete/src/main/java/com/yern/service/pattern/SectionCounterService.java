package com.yern.service.pattern;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yern.dto.pattern.SectionCounterLogCreateRequest;
import com.yern.model.pattern.SectionCounterLog;
import com.yern.repository.pattern.SectionCounterLogRepository;

import jakarta.transaction.Transactional;

@Service
public class SectionCounterService {

    private SectionCounterLogRepository counterLogRepository; 

    public SectionCounterService(
        @Autowired SectionCounterLogRepository counterLogRepository
    ) {
        this.counterLogRepository = counterLogRepository;
    }

    public List<SectionCounterLog> createLogs(List<SectionCounterLogCreateRequest> logs) {
        return counterLogRepository.saveAll(
            logs.stream().map(log -> SectionCounterLog.from(log)).toList()
        );
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void aggregateAndDeleteLogs() {
        counterLogRepository.aggregateAndDeleteLogs(100);
    }
}
