package com.yern.repository.pattern;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yern.model.pattern.SectionCounterLog;

public interface SectionCounterLogRepository extends JpaRepository<SectionCounterLog, UUID> {
    @Modifying
    @Query(nativeQuery = true, value = """
        with rows_to_delete as (
            select 
                id 
            from
                yern.section_counter_logs
            order by 
                id 
            limit 
                :limit
        ),
        deleted_rows as (
            delete 
            from 
                yern.section_counter_logs
            using 
                rows_to_delete
            where 
                rows_to_delete.id = section_counter_logs.id
            returning 
                *
        ),
        aggregated_counts as (
            select 
                sum(value) as count,
                section_id
            from 
                deleted_rows 
            group by 
                section_id
        )
        update 
            yern.section_counters 
        set 
            value = (
                value + aggregated_counts.count
            )
        from 
            aggregated_counts 
        where 
            section_counters.section_id = aggregated_counts.section_id
    """)
    public void aggregateAndDeleteLogs(@Param("limit") int limit);
}
