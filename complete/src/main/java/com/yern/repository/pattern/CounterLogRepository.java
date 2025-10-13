package com.yern.repository.pattern;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yern.model.pattern.CounterLog;

public interface CounterLogRepository extends JpaRepository<CounterLog, UUID> {
    @Modifying
    @Query(nativeQuery = true, value = """
        with rows_to_delete as (
            select 
                id 
            from
                yern.counter_logs
            order by 
                id 
            limit 
                :limit
        ),
        deleted_rows as (
            delete 
            from 
                yern.counter_logs
            using 
                rows_to_delete
            where 
                rows_to_delete.id = counter_logs.id
            returning 
                *
        ),
        aggregated_counts as (
            select 
                sum(value) as count,
                counter_id
            from 
                deleted_rows 
            group by 
                counter_id
        )
        update 
            yern.counters 
        set 
            value = (
                value + aggregated_counts.count
            )
        from 
            aggregated_counts 
        where 
            counters.id = aggregated_counts.counter_id
    """)
    public void aggregateAndDeleteLogs(@Param("limit") int limit);
}
