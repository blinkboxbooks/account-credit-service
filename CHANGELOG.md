# Changelog

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