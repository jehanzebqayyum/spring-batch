package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdditonalBatchConfiguration {
	@Autowired
	JobRepository jobRepository;

	@Autowired
	JobRegistry jobRegistry;

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	JobExplorer jobExplorer;

	@Autowired
	Job job1;

	@Bean
	public JobFactory jobFactory() {
		return new ReferenceJobFactory(job1);
	}

	@Bean
	public JobOperator jobOperator() {
		SimpleJobOperator jobOperator = new SimpleJobOperator();
		jobOperator.setJobExplorer(jobExplorer);
		jobOperator.setJobLauncher(jobLauncher);
		jobOperator.setJobRegistry(jobRegistry);
		jobOperator.setJobRepository(jobRepository);
		return jobOperator;
	}
}
