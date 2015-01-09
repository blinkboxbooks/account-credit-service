####################### update these variables for db versioning purposes ############################################
SET @db_verison_id = '2';
SET @file_name = '002_rename_request_id';
SET @jira_issue = 'PT-258';
#######################################################################################################################


ALTER TABLE credit_balance CHANGE request_id transaction_id VARCHAR(40) NOT NULL COMMENT 'unique identifier to make sure credit/debit gets applied only once';


#######################################################################################################################
INSERT INTO db_version (db_version_id, file_name, jira_issue)
VALUES ( @db_verison_id, @file_name,@jira_issue );
#######################################################################################################################