# Migrating to Spring Cloud Data Flow

[Spring Cloud Data Flow](https://cloud.spring.io/spring-cloud-dataflow/) is a microservices
orchestration tool used to deploy Spring Boot applications (or Docker images) as either
[streams](http://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#spring-cloud-dataflow-streams) or [tasks](http://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#spring-cloud-dataflow-task).  In the migration of Spring Batch Admin use cases to Spring Cloud Data 
Flow, this document will outline the differences between Spring Batch Admin and Spring
Cloud Data Flow and walk through what is needed to migrate from Spring Batch Admin to 
Spring Cloud Data Flow.

## What's different between Spring Batch Admin and Spring Cloud Data Flow?

Spring Batch Admin is a legacy web application that is used to orchestrate 
[Spring Batch](https://projects.spring.io/spring-batch/) jobs.  It does this by packaging
the batch jobs and the code for Spring Batch Admin into a single WAR file and deploying 
that onto a servlet container.  Running there, a user can navigate a provided web based UI
that allows users to launch jobs that were deployed with the application as well as monitor
them (data provided via Spring Batch's job repository).

Spring Cloud Data Flow is a server (a Spring Boot application by itself) that [orchestrates independent microservices on a 
platform](http://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#architecture).  In the Spring Batch use case, each batch job is packaged as an independent Spring
Boot über jar that is registered with Spring Cloud Data Flow. From there, a user can 
orchestrate their jobs by launching them via the provided web based UI, an interactive 
shell, or via a set of REST endpoints directly.  Spring Cloud data flow [supports a number
of platforms](http://cloud.spring.io/spring-cloud-dataflow/#platform-implementations) for 
running batch jobs on including CloudFoundry, Kubernetes, and YARN. For 
the purpose of migrating existing Spring Batch Admin users Local is supported in limited 
production use cases.

There are a few differences between Spring Batch Admin and Spring Cloud Data Flow when
comparing them for Spring Batch Admin use cases:

1. **Packaging -** Spring Batch Admin packages the Spring Batch jobs you want to run within a
WAR file as a single monolith.  While the ability to upload new XML configuration files is
a feature within Spring Batch Admin, it's use is limited given there is no ability to 
upload additional code.  With Spring Cloud Data Flow, the batch jobs are packaged independently 
from the orchestration server, they are entirely two different entities, which allows for 
more flexibility.  The Spring Cloud Data Flow
 approach does not require a re-deploy every time a batch job is to be added or modified.
2. **Execution Model -** Spring Batch Admin executes the batch jobs within the JVM the web 
application is running in.  All batch jobs share the same memory heap, etc.  This can lead to 
issues like noisy neighbors, etc.  Spring Cloud Data Flow executes each batch job in an 
independent JVM.  When used with CloudFoundry or Kubernetes, each batch job is run within it's 
own container and is completely isolated from not only the other batch jobs, but the 
orchestration server itself as well.  When run with the Local deployer, new JVMs are 
launched on the same machine, again with their own independent memory heap, etc.  These 
finite JVMs and containers in Spring Cloud Data Flow are called tasks.  
[Spring Cloud Task](http://cloud.spring.io/spring-cloud-task/) adds additional 
functionality to Spring Batch that allows Spring Batch jobs to work with Spring Cloud Data 
Flow.
3.  **Interaction options -** Spring Batch Admin has a web based UI and a set of REST 
endpoints that can be used to execute and monitor jobs.  Spring Cloud Data Flow provides 
not only a web based UI and REST API to execute and monitor jobs, but also an [interactive 
shell](http://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#shell) to orchestrate jobs.  It also provides a DSL and drag and drop UI for orchestrating 
complex flows of jobs ([execute jobs B and C after A completes, execute job D after both B
and C complete, etc](http://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#_composed_tasks_dsl)).  
[![Composed Tasks on Spring Cloud Data Flow](https://img.youtube.com/vi/KT_4kVcyfRA/0.jpg)](https://www.youtube.com/watch?v=KT_4kVcyfRA)
4.  **Customization options -** Spring Batch Admin was designed to be customizable.  It 
provided documentation not only on how to configure your own component overrides, but also
customize the UI.  Spring Cloud Data Flow is a bit more rigid in that respect.  There are
some features you can turn off via feature toggles (turn of the stream functionality for 
example), but the Angular based web UI is not intended to be extensively customized 
without forking the repository and making deeper modifications or building your own 
dashboard via the provided REST APIs.

## Migrating from Spring Batch Admin to Spring Cloud Data Flow

With the above in mind, the steps for migrating from Spring Batch Admin to Spring Cloud
Data Flow are rather straightforward.

1. **Read the Spring Cloud Data Flow reference documentation -** Given the differences in 
packaging, execution model, and interaction options it will make the migration much easier
after fully understanding how Spring Cloud Data Flow works.
2. **Repackage your batch jobs as Spring Boot über jars (or Docker containers) -** If using 
either the CloudFoundry or Local derivatives of Spring Cloud Data Flow, you'll want to 
repackage your Spring Batch jobs as Spring Boot über jars with the `@EnableTask` 
annotation from the [Spring Cloud Task](http://cloud.spring.io/spring-cloud-task/) project 
added (this annotation allows Spring Cloud Data Flow to work with Spring Batch natively).
If you are going to be using the Kubernetes variant of Spring Cloud Data Flow, you'll want
to package your batch jobs as über jars that are then wrapped in a Docker image.
3. **Register your batch jobs with Spring Cloud Data Flow -** Once you have Spring Cloud Data Flow
running, you'll need to register the jar files or Docker images with the server.  The 
Spring Cloud Data Flow documentation walks through how to do this 
[here](http://docs.spring.io/spring-cloud-dataflow/docs/1.2.2.RELEASE/reference/htmlsingle/#_registering_a_task_application).
4. **Launch your Spring Batch Jobs as tasks -** Once the batch jobs are registered, you can launch 
them as tasks.  Tasks are nothing more than a microservice that has an expected end (as 
all batch jobs do).  In the Spring Batch Admin use cases, think of them as a separate JVM
that will shut down once your job is complete.


