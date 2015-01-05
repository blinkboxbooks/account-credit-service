# account-credit-service-v2 [![Build Status](http://grisham:8111/app/rest/builds/buildType:%28id:Books_Platform_Agora_AccountCreditServiceV2_Build%29/statusIcon)](http://grisham.blinkbox.local:8111/project.html?projectId=Books_Platform_Agora_AccountCreditServiceV2)

[Spec](http://jira.blinkbox.local/confluence/pages/viewpage.action?pageId=21436336) ||
[JIRA](http://jira.blinkbox.local/jira/secure/RapidBoard.jspa?rapidView=107) ||
[Chat](https://blinkbox.slack.com/messages/books-cust-services/) ||
[Teamcity](http://grisham.blinkbox.local:8111/project.html?projectId=Books_Platform_Agora_AccountCreditServiceV2) ||
[Technical Overview](https://git.mobcastdev.com/Agora/account-credit-service-v2/tree/master/accountCreditServiceV2-2.png)

# Background
The account-credit-service-v2 will allow Customer Service Managers and Representatives (CSM & CSR) to reward credits to customers and to retrieve awarded credit history so they can advice them on that matter. 
CSM is a bbb representative and CSR is an External agency (FIS for now).

# Database modelling
The account-credit-service is using mysql relation database. It has been designed following [BBB Database Design Guidelines](http://jira.blinkbox.local/confluence/display/PT/MySQL+Database+Design+Guidelines).  
the schemas for this service can be found in [schemas](https://git.mobcastdev.com/Agora/account-credit-service-v2/tree/master/schemas).

## How to run
### ... the Admin service
`sbt "run-main com.blinkbox.books.credit.admin.Main"`

### ... the Public service
`sbt "run-main com.blinkbox.books.credit.Main"`

# Acceptance Tests
To run all acceptance tests against dev-int execute the following cmd in the project directory:
```
Rake test
```

If you encounter errors about "OpenSSL::X509::StoreError" then follow the instructions on [this page](http://jira.blinkbox.local/confluence/display/PT/StartSSL+Certificates)
