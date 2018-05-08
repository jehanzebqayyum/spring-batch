package com.example.demo;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringBatchApplication implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(SpringBatchApplication.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private JobOperator jobOperator;

	@Autowired
	private ApplicationContext context;

	@Autowired
	JobFactory jobFactory;

	@Autowired
	JobRegistry jobRegistry;

	@Autowired
	Job job1;

	public static void main(String[] args) throws IOException {
		SpringApplication.run(SpringBatchApplication.class, args);
		System.in.read();
	}

	@Override
	public void run(String... args) throws Exception {
		Long count = jdbcTemplate.queryForObject("select count(*) from quotes", Long.class);
		if (count == 0) {
			log.info("init db");
			jdbcTemplate.batchUpdate("insert into quotes (id, processed, version) values (?, false, 0)",
					IntStream.range(0, 1000).boxed().map(i -> new Object[] { i }).collect(Collectors.toList()));
		}

		// TO RESTART EXISTING JOB EXECUTION
		// jobRegistry.register(jobFactory);
		// jobOperator.restart(3);
	}
}
