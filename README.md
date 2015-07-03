# encrypted-db-field-poc
A proof-of-concept about storing and retrieving encrypted varchar/char database fields through Hibernate.

1.Run liquibase to create the test table in the MySQL database
java -jar liquibase.jar --driver=com.mysql.jdbc.Driver --classpath=mysql-connector-java-5.0.8-bin.jar --changeLogFile=databaseChangeLog.xml --url="jdbc:mysql://example/database" --username=user --password=pass migrate

2.Modify the Project.properties with your MySQL connection info for the JDBC test
mysql.user=user
mysql.password=pass
mysql.url=jdbc:mysql://example/database

3.Modify the hibernate.cfg.xml with your MySQL connection info for the Hibernate test


