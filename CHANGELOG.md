# Changelog

## 0.15.2 ([#46](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/46) 2014-12-23 09:59:52)

Credit with reason acceptance

Test Improvement: Updating tests for "Credit with reason" functionality, CRED-62.


## 0.15.1 ([#45](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/45) 2014-12-22 14:41:56)

CRED-73 correct reason from get request

##bugfix
 - correct reason returned from get credit history
 - made add credit idempotent

## 0.15.0 ([#43](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/43) 2014-12-18 14:30:03)

CRED-62 add credit with reason

#### new feature
- add credit with reason
- implemented exception handling
- moved request validation to service implementation

## 0.14.2 ([#41](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/41) 2014-12-17 15:11:38)

Reasons list acceptance tests

Test Improvement: Adding acceptance tests for Reasons List endpoint
http://jira.blinkbox.local/jira/browse/CRED-61


## 0.14.1 ([#44](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/44) 2014-12-16 17:50:27)

Add postman collection

patch

## 0.14.0 ([#42](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/42) 2014-12-16 17:06:54)

CRED-63 Add 'get credit reasons' endpoint.

new feature

No DB yet. Not sure if I'll even bother with the DB.

## 0.13.6 ([#40](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/40) 2014-12-15 13:55:55)

switching on Debit acceptance tests

Test Improvement
Switching on Debit acceptance tests - Credit functionality is now developed and in dev-int which unblocks these tests.

## 0.13.5 ([#39](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/39) 2014-12-15 11:56:01)

Credit history acceptance enhancements

Test Improvement: Credit-history tests have been unblocked from running locally now that Credit functionality is available. I've executed this tests and have made some corrections to them.

## 0.13.4 ([#37](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/37) 2014-12-12 18:26:15)

Cred 54 addredit error400

#### Bugfix
- AddCredit endpoint working with Critical elevation
- Added unittests with TestFixture 
- Changed the request Object  

## 0.13.3 ([#38](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/38) 2014-12-12 13:31:17)

Add credit acceptance improvements

Test Improvement: Now that the Credit endpoint is available and working I have executed the acceptance tests against it and have made some corrections to them.

## 0.13.2 ([#36](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/36) 2014-12-12 12:05:58)

Debit acceptance tests

Test Improvement: Adding admin account debit acceptance tests.
http://jira.blinkbox.local/jira/browse/CRED-19

## 0.13.1 ([#34](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/34) 2014-12-05 12:31:24)

Fix broken test

Fix broken test, that was causing builds to fail.

patch

## 0.13.0 ([#33](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/33) 2014-12-05 12:22:42)

CRED-38: Hook 'Add Debit' and 'Credit History' up to DB

new feature

## 0.12.3 ([#28](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/28) 2014-12-05 12:17:01)

adding credit history feature file and step defs

Patch: Acceptance tests for credit history feature. They're marked as @in-progress at the moment as the functionality isn't available in dev-int yet. If you want to run them anyway then just remove the annotation.

## 0.12.2 ([#32](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/32) 2014-12-04 18:06:28)

Refactoring

patch

## 0.12.1 ([#31](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/31) 2014-12-04 14:55:46)

Rename swagger files & suggest clients use GUID for requestId

patch

This should only be merged after https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/25 has been merged.

## 0.12.0 ([#25](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/25) 2014-12-04 13:24:15)

CRED-38 Add spray layer for Add Debit endpoint (unscrewed)

Requirements: http://jira.blinkbox.local/jira/browse/CRED-19
new feature

## 0.11.0 ([#30](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/30) 2014-12-03 16:05:37)

Cred 29 impl add credit fixed

#### New feature
* implemented add credit endpoint
* implemented persistence layer against the latest schema(slick )

## 0.10.7 ([#29](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/29) 2014-12-02 12:13:33)

Add Gemfile.lock

http://yehudakatz.com/2010/12/16/clarifying-the-roles-of-the-gemspec-and-gemfile/
for why we should add the Gemfile.lock in applications

Some of our other applications also have a Gemfile.lock:
* https://git.mobcastdev.com/Agora/gifting-service/blob/49db46616fd49b455b07a5e3b84d175c7dbc4348/Gemfile.lock
* https://git.mobcastdev.com/Agora/library-service/blob/36e5fe1ef1f91bea74b1b9793c144028c0ad8573/Gemfile.lock

@DanielL thoughts?

patch

## 0.10.6 ([#22](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/22) 2014-12-02 11:34:54)

Made requestId column unique

##improvement
- made requestId column unique to enforce one requestID per column rule

## 0.10.5 ([#26](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/26) 2014-12-02 11:14:35)

Add credit acceptance tests

Patch - Acceptance tests for add credit functionality  
http://jira.blinkbox.local/jira/browse/CRED-21

## 0.10.4 ([#24](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/24) 2014-11-28 11:31:38)

Add local/dev to cucumber environments

patch

@daniell

## 0.10.3 ([#23](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/23) 2014-11-28 11:17:33)

acceptance test for health check endpoint

Patch: acceptance test for health check endpoint

## 0.10.2 ([#19](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/19) 2014-11-28 10:35:48)

Cucumber skeleton

Test Improvement: Cucumber skeleton for acceptance tests.

## 0.10.1 ([#20](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/20) 2014-11-26 15:00:02)

Fix health check service: it was 404ing

Will add cucumber test, when the cucumber test skeleton is up

patch

## 0.10.0 ([#17](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/17) 2014-11-26 14:57:29)

Suggested "add credit" API changes

... from the review of the "add debit" API changes in
https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/15

new feature

## 0.9.1 ([#18](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/18) 2014-11-26 12:47:32)

Add logging of requests, using monitor directive

patch

## 0.9.0 ([#15](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/15) 2014-11-24 12:08:25)

CRED-36: Outline of add debit endpoint

Acceptance criteria at http://jira.blinkbox.local/jira/browse/CRED-19

new feature

## 0.8.1 ([#14](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/14) 2014-11-24 12:09:05)

Enable -Xfatal-warnings -Xcheckinit -Xlint

patch

## 0.8.0 ([#9](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/9) 2014-11-21 15:10:59)

CRED-27 added db design

#### New feature
added db schemas design for the service.

## 0.7.0 ([#13](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/13) 2014-11-21 14:40:29)

Cred 28 api design fixed

#### New feature
* design updated

## 0.6.0 ([#11](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/11) 2014-11-19 13:41:27)

CRED-5: Change credit history URL

... to `GET /admins/users/{id}/credit/credithistory`

breaking change

## 0.5.0 ([#7](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/7) 2014-11-12 15:04:29)

CRED-5: implement credit history (no DB)

New feature

What it does:
* Adds a new endpoint: `GET /admin/users/{user_id}/credit` that returns credit history
* Returns dummy data for now. Hook up to DB in later PR.
* Have to be a CSM/CSR to call it
* Returns all info if requester is a CSM
* Returns all info (except issuer info) if request is a CSR

## 0.4.1 ([#5](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/5) 2014-11-10 11:58:01)

CRED-5: credit history swagger API

patch

## 0.4.0 ([#4](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/4) 2014-11-07 17:39:27)

Add barebones public endpoint for testing

* Adds `GET /my/credit/foo`
* Changes `GET /admin/credit/foo` to return `bar-admin`

new feature

## 0.3.0 ([#3](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/3) 2014-11-07 17:15:01)

Add GET /admin/credit/foo endpoint

The purpose of this endpoint is so that we can test the endpoint is
accessible in DevInt.

new feature

## 0.2.0 ([81b17a4ea6](https://git.mobcastdev.com/Agora/account-credit-service-v2/commit/81b17a4ea68aa531bac9e975225016e89f0c9aa6) 2014-11-07 14:49:00)

Walking skeleton of Admin API

New feature

(I committed this directly to master, rather than do it via a PR (because it
was just a copy-paste of the skeleton of the public API), so I have had to
manually do what Proteus do: construct a Changelog entry and update the VERSION.
Last time I do that!)

## 0.1.0 ([#1](https://git.mobcastdev.com/Agora/account-credit-service-v2/pull/1) 2014-11-06 12:20:09)

Walking skeleton

These are the new feature :
* Returns `pong` on `GET /health/ping`
* Logs to greylog
* Reads some config

