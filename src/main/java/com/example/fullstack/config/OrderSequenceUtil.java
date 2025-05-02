package com.example.fullstack.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderSequenceUtil {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(OrderSequenceUtil.class);


    @Scheduled(cron = "0 0 0 * * 0")
    @Transactional
    public void resetDailySequence() {
        jdbcTemplate.update(
                "UPDATE order_sequence_counter SET next_val = 1 WHERE id = 'ORDER_SEQ'"
        );
        log.info("Order sequence counter reset for the new day");
    }

    public OrderSequenceUtil(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // Create the sequence table if it doesn't exist
        initSequenceTable();
    }

    private void initSequenceTable() {
        // Create a simple sequence table if it doesn't exist
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS order_sequence_counter (" +
                        "  id VARCHAR(255) PRIMARY KEY, " +
                        "  next_val BIGINT NOT NULL" +
                        ")"
        );

        // Initialize the counter if it doesn't exist
        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_sequence_counter WHERE id = 'ORDER_SEQ'",
                Integer.class
        );

        if (count == 0) {
            // Initialize with current max order number or 0
            Long maxOrderNum = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(order_sequence), 0) FROM orders",
                    Long.class
            );

            jdbcTemplate.update(
                    "INSERT INTO order_sequence_counter (id, next_val) VALUES (?, ?)",
                    "ORDER_SEQ", maxOrderNum + 1
            );
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long getNextOrderSequence() {
        // Get and increment in a single atomic operation with highest isolation level
        Long nextVal = jdbcTemplate.queryForObject(
                "SELECT next_val FROM order_sequence_counter WHERE id = 'ORDER_SEQ' FOR UPDATE",
                Long.class
        );

        jdbcTemplate.update(
                "UPDATE order_sequence_counter SET next_val = next_val + 1 WHERE id = 'ORDER_SEQ'"
        );

        return nextVal;
    }
}