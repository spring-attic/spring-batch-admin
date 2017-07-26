# NOTE: This project is being moved to the Spring Attic and is not recommended for new projects.  [Spring Cloud Data Flow](https://cloud.spring.io/spring-cloud-dataflow/) is the recommended replacement for managing and monitoring [Spring Batch](https://projects.spring.io/spring-batch/) jobs going forward.  You can read more about migrating to Spring Cloud Data Flow [here](https://github.com/spring-projects/spring-batch-admin/blob/master/MIGRATION.md).  

Web application and API for managing and monitoring [Spring Batch](https://projects.spring.io/spring-batch/) jobs.  The application consists of a web UI using Spring MVC, a Java service API and a RESTful (JSON) API.  The web application is highly customizable, and is an ideal platform for deploying Spring Batch jobs.  It can also be used to monitor jobs running in other processes. See the main [project website](http://static.springsource.org/spring-batch-admin) for more details.

# Getting Started Using SpringSource Tool Suite (STS)

  This is the quickest way to get started.  It requires an internet connection for download, and access to a Maven repository (remote or local).

* Download STS version 3.5.* (or better) from the [SpringSource website](http://spring.io/tools).  STS is a free Eclipse bundle with many features useful for Spring developers.
* Go to `File->New->Spring Template Project` from the menu bar (in the Spring perspective).
* The wizard has a drop down with a list of template projects.  One of them is a "Spring Batch Admin Webapp".  Select it and follow the wizard.
* A project is created with all dependencies and a simple input/output job configuration.  It can be run using a unit test, or on the command line (see instructions in the pom.xml).

# Getting Help

Read the main project [website](http://docs.spring.io/spring-batch-admin/) and the [User Guide](http://docs.spring.io/spring-batch-admin/reference.html). Look at the source code and the Javadocs.  For more detailed questions, use the [forum](http://forum.spring.io/forum/spring-projects/batch).  If you are new to Spring as well as to Spring Batch, look for information about [Spring projects](http://spring.io/projects).

# Contributing to Spring Batch Admin

Here are some ways for you to get involved in the community:

* Create [JIRA](https://jira.spring.io/browse/BATCHADM) tickets for bugs and new features and comment and vote on the ones that you are interested in.  
* Github is for social coding: if you want to write code, we encourage contributions through pull requests from [forks of this repository](http://help.github.com/forking/).
* Watch for upcoming articles on Spring by [subscribing](http://spring.io/blog) to spring.io

Before we accept a non-trivial patch or pull request we will need you to sign the [contributor's agreement](https://support.springsource.com/spring_committer_signup).  Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, and you will get an author credit if we do.  Active contributors might be asked to join the core team, and given the ability to merge pull requests.

# Code of Conduct
 This project adheres to the [Contributor Covenant ](https://github.com/spring-projects/spring-batch-admin/blob/master/CODE_OF_CONDUCT.adoc). By participating, you  are expected to uphold this code. Please report unacceptable behavior to spring-code-of-conduct@pivotal.io.