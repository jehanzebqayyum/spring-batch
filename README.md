# spring-batch

      ┌──┐          ┌───────────┐          ┌───────┐          
      │DB│          │SpringBatch│          │QuoteWS│          
      └┬─┘          └─────┬─────┘          └───┬───┘          
       │    getQuotes     │                    │              
       │ <────────────────│                    │              
       │                  │                    │              
       │                  │                    │              
       │  ╔═══════╤═══════╪════════════════════╪═════════════╗
       │  ║ LOOP  │  for each item in chunk    │             ║
       │  ╟───────┘       │                    │             ║
       │  ║               │     getQuote       │             ║
       │  ║               │───────────────────>│             ║
       │  ║               │                    │             ║
       │  ║               ────┐                │             ║
       │  ║                   │ updateItem     │             ║
       │  ║               <───┘                │             ║
       │  ╚═══════════════╪════════════════════╪═════════════╝
       │                  │                    │              
       │   writeQuotes    │                    │              
       │ <────────────────│                    │              
      ┌┴─┐          ┌─────┴─────┐          ┌───┴───┐          
      │DB│          │SpringBatch│          │QuoteWS│          
      └──┘          └───────────┘          └───────┘          
      



### Overview
* Spring Batch does chunk processing where single chunk contains multiple items. ItemReader reads a chunk of items from source.
* Once the processing of all items finish, the complete updated chunk is written back to db.
* This example reads quote ids from db, for each quote get randome quote from a webservice and updates in db.

### Threading
* Each chunk is read,processed,written in single thread.
* Spring Batch is by default single threaded. To process in multithreaded manner, requires care.
Most ItemReader/Writers are single threaded (to support restart).
* Multithreading is only suported with datasource job repository i.e. in-memory map job repo should not be used in this case, as per the docs.

### Transaction Processing
* Single chunk will execute in one transaction i.e. reading, processing and writing.
* Make sure datasource connection pool size is appropriately matched with threadpool size while using multi threaded jobs.
* Collecate batch tables with source tables to avoid JTATransactions.

### JdbcCursorItemReader
* It is not thread safe and cannot be used in a multithreaded job as is unless decorated with SynchronizedItemStreamReader (locking overhead).
* It keeps result set open and requires active db connection which may not be suitable for long running jobs.
* It fetches one record at a time from db, which may be round trip overhead in some jobs.
* It supports restart by saving last rownum in execution context in batch schema.

### JdbcPagingItemReader
* It is thread safe. So job can be multithreaded
* It supports restart only in single threaded mode. To support restart in multithreaded mode, need to keep a column e.g. processed = true/false in source table and read data conditionally. Additionally update to processed column is also required.
* In multithreaded mode, since it does not support restart, it makes no sense to save its state in batch schema therefore `.saveState(false)`
* It may require more app memory since it brings the full page (or multiple pages in case of multithreaded job).
* The `.pageSize(10)` and `chunk(10)` should be same.

### Run
* Download and run H2 server separately. `java -cp h2-1.4.196.jar org.h2.tools.Server`
* Run `SpringBatchApplication`, by default it will create its schema (if not exist) and run the job on startup. To stop automatic running of job on startup set `spring.batch.job.enabled=false` in application.properties.
#### Failed Jobs
* To restart failed job first update the execution to failed as 
`update BATCH_JOB_EXECUTION set end_time = sysdate, status = 'FAILED' where job_execution_id = 3;`
* Restart the job programmatically (SpringBatchApplication)
`jobRegistry.register(jobFactory); jobOperator.restart(3);`
 


