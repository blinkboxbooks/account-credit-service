####################### update these variables for db versioning purposes ############################################
SET @db_verison_id = '1';
SET @file_name= '001_create_schema';
SET @jira_issue= 'CRED-27';
#######################################################################################################################

DROP TABLE IF EXISTS transaction_type ;

CREATE TABLE transaction_type (
  transaction_type_id TINYINT UNSIGNED NOT NULL,
  type VARCHAR(50) NOT NULL,
  PRIMARY KEY (transaction_type_id )
) ENGINE = InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;

DROP TABLE IF EXISTS reason ;
CREATE TABLE reason (
  reason_id TINYINT UNSIGNED NOT NULL,
  type VARCHAR(50) NOT NULL,
  PRIMARY KEY (reason_id)
) ENGINE = InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;

DROP TABLE IF EXISTS credit_balance ;
CREATE TABLE credit_balance (
 credit_balance_id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
 request_id VARCHAR(55) NOT NULL COMMENT 'unique identifier to make sure credit/debit gets applyied only once',
 value FLOAT(7,2) NOT NULL,
 transaction_type_id TINYINT UNSIGNED NOT NULL,
 reason_id TINYINT UNSIGNED,
 created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 customer_id INT(11) NOT NULL,
 admin_user_id INT(11) ,
 admin_user_role VARCHAR(11) NOT NULL COMMENT 'Admin user Role at the time of this transaction',
 PRIMARY KEY (credit_balance_id),
 FOREIGN KEY `fk_credit_balance_transaction_type` (`transaction_type_id`) references `transaction_type` (`transaction_type_id`),
 FOREIGN KEY `fk_credit_balance_reason` (`reason_id`) references `reason` (`reason_id`)
 ) ENGINE = InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;


INSERT INTO transaction_type (`transaction_type_id`, `type`) VALUES ('0', 'Credit');
INSERT INTO transaction_type (`transaction_type_id`, `type`) VALUES ('1', 'Debit');

INSERT INTO reason (`reason_id`, `type`) VALUES ('0', 'GoodwillBookIssue');
INSERT INTO reason (`reason_id`, `type`) VALUES ('1', 'GoodwillTechnicalIssue');
INSERT INTO reason (`reason_id`, `type`) VALUES ('2', 'GoodwillServiceIssue');
INSERT INTO reason (`reason_id`, `type`) VALUES ('3', 'GoodwillCustomerRetention');
INSERT INTO reason (`reason_id`, `type`) VALUES ('4', 'CreditRefund');
INSERT INTO reason (`reason_id`, `type`) VALUES ('5', 'StaffCredit');
INSERT INTO reason (`reason_id`, `type`) VALUES ('6', 'CreditVoucherCode');
INSERT INTO reason (`reason_id`, `type`) VALUES ('7', 'Hundle2Promotion');


#######################################################################################################################
INSERT INTO db_version (db_version_id, file_name, jira_issue)
VALUES ( @db_verison_id, @file_name,@jira_issue );
#######################################################################################################################