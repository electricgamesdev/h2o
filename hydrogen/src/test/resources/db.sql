mysql -u root -p

hydrodb
h2o

sudo -u hdfs hdfs dfsadmin -safemode leave

hdfs datanode -format

sudo service hadoop-hdfs-namenode start

hdfs dfs -rm /user/hydrogen/source/*
hdfs dfs -rm /user/cloudera/.Trash/Current/user/hydrogen/source/*

drop table h2o;

create table h2o (processId bigint,basename varchar(100),descrption varchar(300), 
status varchar(10),workflowId varchar(100),source varchar(200),groupkey varchar(200),
step varchar(200));

create table hcl (processId bigint,process varchar(100),status varchar(200));

select * from hcl;

select * from h2o;

delete from h2o;

update h2o set status='PPP' where groupkey='_201312031234'

select * from h2o where groupkey='_201312031234' and status='INIT'
