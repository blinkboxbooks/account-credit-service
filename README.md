# account-credit-service-v2

* [Spec](http://jira.blinkbox.local/confluence/pages/viewpage.action?pageId=21436336)
* [JIRA](http://jira.blinkbox.local/jira/secure/RapidBoard.jspa?rapidView=107)
* [Chat](https://blinkbox.slack.com/messages/books-cust-services/)
* [Teamcity](http://grisham.blinkbox.local:8111/project.html?projectId=Books_Platform_Agora_AccountCreditServiceV2)

# Background
This document provides a high level overview of the technical design for account-credit-service-v2 functionalities. In a nutshell, the account-credit-service-v2 will allow Customer Service Managers and Representatives (CSM & CSR) to reward credits to customers and to retrieve awarded credit history so they can advice them on that matter. 
CSM is a bbb representative and CSR is an External agency (FIS for now).

# Technical Overview
This diagram shows the key components and actors of the service
![alt tag](https://git.mobcastdev.com/Agora/account-credit-service-v2/tree/master/accountCreditServiceV2.png?api=v2)

# Database modelling
The account-credit-service is using mysql relation database. It has been designed following [BBB Database Design Guidelines](http://jira.blinkbox.local/confluence/display/PT/MySQL+Database+Design+Guidelines).  
the schemas for this service can be found in [schemas](https://git.mobcastdev.com/Agora/account-credit-service-v2/tree/master/schemas).
