####################### update these variables for db versioning purposes ############################################
SET @db_verison_id = '3';
SET @file_name = '003_millisecond_precision_for_datetime';
SET @jira_issue = 'PT-258';
#######################################################################################################################


ALTER TABLE credit_balance CHANGE created_at created_at DATETIME(3) NOT NULL;
ALTER TABLE credit_balance CHANGE updated_at updated_at DATETIME(3);


#######################################################################################################################
INSERT INTO db_version (db_version_id, file_name, jira_issue)
VALUES ( @db_verison_id, @file_name,@jira_issue );
#######################################################################################################################