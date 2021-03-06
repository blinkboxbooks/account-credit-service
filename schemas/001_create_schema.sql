####################### update these variables for db versioning purposes ############################################
SET @db_verison_id = '1';
SET @file_name= '001_create_schema';
SET @jira_issue= 'CRED-27';
#######################################################################################################################


CREATE TABLE transaction_types (
  transaction_type_id  TINYINT UNSIGNED NOT NULL,
  type VARCHAR(15) NOT NULL,
  PRIMARY KEY (transaction_type_id  )
) ENGINE = InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;

CREATE TABLE reasons (
  reason_id TINYINT UNSIGNED NOT NULL,
  type VARCHAR(40) NOT NULL,
  PRIMARY KEY (reason_id)
) ENGINE = InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;

CREATE TABLE credit_balance (
 credit_balance_id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
 request_id VARCHAR(40) NOT NULL COMMENT 'unique identifier to make sure credit/debit gets applied only once',
 value FLOAT(7,2) NOT NULL,
 transaction_type_id  TINYINT UNSIGNED NOT NULL,
 reason_id TINYINT UNSIGNED,
 created_at datetime  NOT NULL,
 updated_at  datetime ,
 customer_id INT(11) NOT NULL,
 admin_user_id INT(11) ,
 UNIQUE (request_id),
 PRIMARY KEY (credit_balance_id),
 FOREIGN KEY `fk_credit_balance_transaction_types` (`transaction_type_id`) references `transaction_types` (`transaction_type_id`),
 FOREIGN KEY `fk_credit_balance_reasons` (`reason_id`) references `reasons` (`reason_id`)
 ) ENGINE = InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;


INSERT INTO transaction_types (`transaction_type_id`, `type`) VALUES ('0', 'Credit');
INSERT INTO transaction_types (`transaction_type_id`, `type`) VALUES ('1', 'Debit');

INSERT INTO reasons (`reason_id`, `type`) VALUES ('0', 'GoodwillBookIssue');
INSERT INTO reasons (`reason_id`, `type`) VALUES ('1', 'GoodwillTechnicalIssue');
INSERT INTO reasons (`reason_id`, `type`) VALUES ('2', 'GoodwillServiceIssue');
INSERT INTO reasons (`reason_id`, `type`) VALUES ('3', 'GoodwillCustomerRetention');
INSERT INTO reasons (`reason_id`, `type`) VALUES ('4', 'CreditRefund');
INSERT INTO reasons (`reason_id`, `type`) VALUES ('5', 'StaffCredit');
INSERT INTO reasons (`reason_id`, `type`) VALUES ('6', 'CreditVoucherCode');
INSERT INTO reasons (`reason_id`, `type`) VALUES ('7', 'Hudl2Promotion');


#######################################################################################################################
INSERT INTO db_version (db_version_id, file_name, jira_issue)
VALUES ( @db_verison_id, @file_name,@jira_issue );
#######################################################################################################################
