package com.example.demo;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.Ordered;

@EnableBatchProcessing
@SpringBootApplication
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringBatchApplication implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(SpringBatchApplication.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) throws IOException {
		SpringApplication.run(SpringBatchApplication.class, args);
		System.in.read();
	}

	@Override
	public void run(String... args) throws Exception {
		//log.info("init db");
		//jdbcTemplate.batchUpdate("merge into quotes (id, processed) values (?, false)",
		//		IntStream.range(0, 100000).boxed().map(i -> new Object[] { i }).collect(Collectors.toList()));

		// log.info(jdbcTemplate.query("select * from quotes",
		// Quote.rowMapper()).toString());
	}
}
