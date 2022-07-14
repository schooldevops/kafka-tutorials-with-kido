-- Source_User
CREATE USER 'source_user'@'%' IDENTIFIED WITH mysql_native_password BY '1234';
CREATE USER 'replicator'@'%' IDENTIFIED BY 'replpass';

GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT  ON *.* TO 'source_user';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'replicator';

create database traceability;

GRANT ALL PRIVILEGES ON traceability.* TO 'source_user'@'%';

-- Sink_User
CREATE USER 'sink_user'@'%' IDENTIFIED WITH mysql_native_password BY '1234';

GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT  ON *.* TO 'sink_user';

create database sink;

GRANT ALL PRIVILEGES ON sink.* TO 'sink_user'@'%';