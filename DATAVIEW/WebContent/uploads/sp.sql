-- The following sql file is installed on all the data nodes to compute the data size. 
-- @author  Aravind Mohan

DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `GetDatasetSize`(cdpname varchar(5000), valuecolname varchar(5000), OUT out1 varchar(255))
BEGIN
SET @params1 = concat('`',cdpname,'`');
SET @params2 = "abc";
SET @params3  = concat('`',valuecolname,'`');
SET @sql_text1 = concat('SELECT SUM(Size) as DataSize INTO @params2 from(SELECT table_name AS `Table`, round(((data_length + index_length) / 1024 / 1024 /1024), 2) `Size` FROM information_schema.TABLES WHERE table_schema = "amohan-relationaldps" AND table_name IN (SELECT ',@params3, ' from ',@params1,')) B');
PREPARE stmt1 FROM @sql_text1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;
SET out1 = @params2;
END;;
DELIMITER ;