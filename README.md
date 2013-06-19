Jive SBS Plugin: Jira integration
=========

Jive SBS plugin integrating Jive content and JIRA issues

Features
--------

1. Link and unlink any content in SBS (Document, Discussion, Blog post, Poll) with any JIRA ticket
2. Honor JIRA permissions
3. Synchronize SBS's related Issues and JIRA's JBoss Forum custom field
4. Work with JIRA 5's WSDL and REST API


Parameters
----------

| Parameter                                 | Type              | Description | Example |
| ----------------------------------------- |-------------------| ------------| ------- |
| jboss.jira.baseURL                        | URL               | Public URL of JIRA | https://issues.jboss.org |
| jboss.jira.baseURLInternal                | URL               | Internal URL fo JIRA (can be same as public URL) | https://issues.jboss.org |
| jboss.jira.sbsDomainToCheck               | String            | Value of JBoss Forum Reference is determined as SBS content if contains this domain. | community.jboss.org/ |
| jboss.jira.updateJiraTicketsPeriod        | Integer (minutes) | Period defines last jira tickest which should be synchronized from JIRA to SBS's related issues | 15 (synchronize tickets which were udpated in recent 15 minutes) |
| jboss.jira.updateJiraTicketsInterval      | Integer (minutes) | How often plugin will do synchronization from JIRA tickets to SBS's related issues | 10 (every 10 minutes) |
| jboss.jira.specialUpdateCountTo           | Integer           | Count of regulard updates after them is special update performed. It's useful when it's needed to perform for example big update twice per day | 36 (after 35 regular updates perform special one = 42 * 10 min = 6 hours) |
| jboss.jira.specialUpdateJiraTicketsPeriod	| Integer (minutes) | Period for special update. Functionality is the same like regular update but another (longer) period | 360 (synchronize tickets which were udpated in recent 6 hours) |
| jboss.jira.wsdlUsername                   | String            | Valid username used for accessing JIRA via SOAP WebServices |  |
| jboss.jira.wsdlPassword                   | String            | Password for username (see row above) | |


Installation steps
------------------

1. Install the plugin via admin console
2. Ensure that JIRA provides REST API
3. Create service account in SBS and add Edit Issue privileges in JIRA
4. Add all parameters as SBS's System properties
5. (Optional) Add read privileges to "jiradb.customfieldvalue" and "jiradb.jiraissue" tables for sbs db account
6. Restart SBS
7. Make sure that JIRA conains in JBoss Forum Reference custom field all Related Issues link from SBS (from earlier versions of plugin)


### Data initialization

To perform full reindex you need to pass step no. 5 from Installation steps

1. Start full reindex of Related Issues in SBS admin console > System > Management > JIRA Integration.


Development
-----------

### How to update JIRA's WSDL client

Unfortunately it's not possible to use standard Java 5 JAX-WS wsimport because JIRA's WSDL is rpc/encoded type of WebService.

SBS comes with Axis 1.4 and this tool can be used:

		java org.apache.axis.wsdl.WSDL2Java -n https://issues.jboss.org/rpc/soap/jirasoapservice-v2?wsdl

