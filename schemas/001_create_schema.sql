####################### update these variables for db versionning purposes ############################################
SET @db_verison_id = '1';
SET @file_name= '001_create_schema';
SET @jira_issue= 'CRED-27';

#######################################################################################################################
INSERT INTO db_version (db_version_id, file_name, jira_issue)
VALUES ( @db_verison_id, @file_name,@jira_issue );
#######################################################################################################################


CREATE TABLE transaction_type (
  transaction_type_id TINYINT UNSIGNED NOT NULL,
  description VARCHAR(50) NOT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (transaction_type_id )
) ENGINE = InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;

CREATE TABLE credit_balance (
 credit_balance_id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
 request_id VARCHAR(55) NOT NULL,
 value FLOAT(7,2) NOT NULL,
 currency varchar(5) NOT NULL,
 transaction_type_id TINYINT UNSIGNED NOT NULL,
 reason varchar(11)  NULL,
 created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 customer_id INT(11) NOT NULL,
 admin_user_id INT(11) NOT NULL,
 admin_user_name VARCHAR(11) NOT NULL,
 admin_user_role VARCHAR(11) NOT NULL,
 PRIMARY KEY (credit_balance_id),
 CONSTRAINT code_to_type_fk FOREIGN KEY (transaction_type_id) REFERENCES transaction_type (transaction_type_id)
 ) ENGINE = InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;

CREATE TABLE net_balance (
 net_balance_id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
 value FLOAT(7,2) NOT NULL,
 currency varchar(5) NOT NULL,
 created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY (net_balance_id) 
 ) ENGINE = InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;

INSERT INTO transaction_type (`transaction_type_id`, `description`) VALUES ('0', 'credit');
INSERT INTO transaction_type (`transaction_type_id`, `description`) VALUES ('1', 'debit');