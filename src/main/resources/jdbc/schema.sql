DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
	`login` VARCHAR(50) NOT NULL PRIMARY KEY,
	`password` VARCHAR(128) NOT NULL,
	`token` CHAR(36) NULL DEFAULT NULL,
	`balance` INT NOT NULL DEFAULT '0',
	`login_attempts` INT NOT NULL DEFAULT '0',
	`blocked` TINYINT(1) NOT NULL DEFAULT '0'
);
