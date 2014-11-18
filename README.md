# account-credit-service-v2

* [Spec](http://jira.blinkbox.local/confluence/pages/viewpage.action?pageId=21436336)
* [JIRA](http://jira.blinkbox.local/jira/secure/RapidBoard.jspa?rapidView=107)
* [Chat](https://blinkbox.slack.com/messages/books-cust-services/)
* [Teamcity](http://grisham.blinkbox.local:8111/project.html?projectId=Books_Platform_Agora_AccountCreditServiceV2)

# Background
This document provides a high level overview of the technical design for account-credit-service-v2 functionalities. In a nutshell, the account-credit-service-v2 will allow Customer Service Managers and Representatives (CSM & CSR) to reward credits to customers and to retrieve awarded credit history so they can advice them on that matter. CSM and CSR have different privileges on the services where , for example, CSM can grant suspend/delete account role to a CSR.  
CSM is a bbb representative and CSR is an External agency (FIS for now).

# Technical Overview
This diagram shows the key components and actors of the service
![alt tag](http://jira.blinkbox.local/confluence/download/attachments/21439462/accountCreditServiceV2.png?api=v2)

# REST API Guide
* REST API Documentation in YAML format for use with Swagger 2.0 is available here: [account-credit-service Admin facing API Spec](https://git.mobcastdev.com/Agora/account-credit-service-v2/blob/master/admin/src/main/resources/yaml/admin.yaml)
* REST API Documentation in YAML format for use with Swagger 2.0 is available here: [account-credit-service public facing API Spec](https://git.mobcastdev.com/Agora/account-credit-service-v2/blob/master/public/src/main/resources/yaml/public.yaml)

The account-credit-service will be using mysql relation database. 
For crediting and debiting an account, After successful credit_balance table update, a new row has to be entered in net-balance table to maintain the net balance.                                                                   |

# Database modelling
The account-credit-service will be using mysql relation database. It has been designed following [BBB Database Design Guidelines](http://jira.blinkbox.local/confluence/display/PT/MySQL+Database+Design+Guidelines).  
For crediting and debiting an account, After successful credit\_balance table update, a new row has to be entered in net\_balance table to maintain the net balance.    
the schemas for this service can be found in [schemas](https://git.mobcastdev.com/Agora/account-credit-service-v2/tree/master/schemas).
MySQL database can be created using the SQL script in [001_create_schema.sql](https://git.mobcastdev.com/Agora/account-credit-service-v2/tree/master/schemas/001_create_schema.sql)

### Entity Relationship Diagram
![alt tag](http://jira.blinkbox.local/confluence/download/attachments/21439462/accountCreditV2EntityRelationshipDiagram.png?api=v2)