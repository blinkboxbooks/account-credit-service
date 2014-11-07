# Changelog

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

