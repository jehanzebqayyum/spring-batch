package com.example.demo;

import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableBatchProcessing
@Import(AdditonalBatchConfiguration.class)
public class BatchConfiguration {
	private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Step step1(ItemReader<Quote> reader, ItemProcessor<Quote, Quote> processor, ItemWriter<Quote> writer,
			TaskExecutor taskExecutor) {
		return stepBuilderFactory.get("step1").<Quote, Quote>chunk(10).reader(reader).processor(processor)
				.writer(writer).taskExecutor(taskExecutor).throttleLimit(4).build();
	}

	@Bean
	public Job job(Step step1) {
		return jobBuilderFactory.get("job1").start(step1).build();
	}

	@Bean
	protected JobRepositoryFactoryBean jobRepositoryFactoryBean(PlatformTransactionManager transactionManager)
			throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setDatabaseType("h2");
		factory.setTransactionManager(transactionManager);
		return factory;
	}

	/*
	 * @Bean public ItemReader<Quote> reader() { return new
	 * SynchronizedItemStreamReaderBuilder<Quote>()
	 * .delegate((ItemStreamReader<Quote>) new
	 * JdbcCursorItemReaderBuilder<Quote>().dataSource(this.dataSource)
	 * .name("quoteReader").sql("select * from quotes where processed = false").
	 * saveState(false) .rowMapper(Quote.rowMapper()).build()) .build(); }
	 */

	@Bean
	public JdbcPagingItemReader<Quote> reader(DataSource dataSource, PagingQueryProvider queryProvider) {
		return new JdbcPagingItemReaderBuilder<Quote>().name("quoteReader").dataSource(dataSource)
				.queryProvider(queryProvider)
				.saveState(false)
				.rowMapper(Quote.rowMapper()).pageSize(10).build();
	}

	@Bean
	public PagingQueryProvider queryProvider(DataSource dataSource) throws Exception {
		SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
		provider.setDataSource(dataSource);
		provider.setSelectClause("select *");
		provider.setFromClause("from quotes");
		provider.setWhereClause("where processed=false");
		provider.setSortKey("id");

		return provider.getObject();
	}

	@Bean
	ItemWriter<Quote> writer() {
		return new ItemWriter<Quote>() {

			@Override
			public void write(List<? extends Quote> quotes) throws Exception {
				log.info("writing: {}", quotes.size());
				jdbcTemplate.batchUpdate("update quotes set quote = ?, processed = true where id = ?", quotes.stream()
						.map(q -> new Object[] { q.getQuote(), q.getId() }).collect(Collectors.toList()));
			}
		};
	}

	@Bean
	ItemProcessor<Quote, Quote> processor(RestTemplate restTemplate) {
		return new ItemProcessor<Quote, Quote>() {

			@Override
			public Quote process(Quote q) throws Exception {
				log.info("processing: {}", q);
				ResponseEntity<String> response = restTemplate
						.getForEntity("http://gturnquist-quoters.cfapps.io/api/random", String.class);
				if (response.getStatusCodeValue() == 200) {
					JsonNode root = mapper.readTree(response.getBody());
					q.setQuote(root.path("value").path("quote").asText());
				}
				log.info("processed: {}", q);
				return q;
			}
		};
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setThreadNamePrefix("default_task_executor");
		executor.initialize();
		return executor;
	}

}