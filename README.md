# How to use tool to fix Pentaho issues

## Requirements:
- Java 21
- Docker and Docker Compose

### How to get a dump from your Liferay's version

1. Download your bundle version from `https://customer.liferay.com/en_US/downloads`
    > **The bundle version must be the same as your project**

2. Create a MySQL|PostgreSQL Docker image spin up the docker service:

   MySQL:
    ```
    docker compose up mysql -d
    ```
   PostgreSQL:
    ```
    docker compose up postgres -d
    ```

4. Go to the bundle you just downloaded, and in the root folder, create a file `portal-ext.propeties` and put the following properties:
    
    MySQL:
    ```
    jdbc.default.driverClassName=com.mysql.cj.jdbc.Driver
    jdbc.default.url=jdbc:mysql://localhost:3307/liferay?useUnicode=true&characterEncoding=UTF-8&useFastDateParsing=false
    jdbc.default.username=liferay
    jdbc.default.password=liferay
    ```
    PostgreSQL: 
    ```
    jdbc.default.driverClassName=com.mysql.cj.jdbc.Driver
    jdbc.default.url=jdbc:postgresql://localhost:5432/liferay
    jdbc.default.username=liferay
    jdbc.default.password=liferay
    ```

6. Now, go to the **tomcat/bin** folder in your downloaded bundle, and run:
   ``
   ./catalina.sh run
   `` 

7. After starting the portal, go to the docker container and extract a dump file:
   -  Go to docker container:
    ```
    docker compose exec mysql|posgres bash
    ```
    - Run the following command to generate a dump
       
    MySQL:
    ```
    mysqldump -u liferay liferay > [name-file-dump.sql]
    ```
    PostgreSQL:
    ```
    pg_dump -U liferay liferay > [name-file-dump.sql]
    ```  

    - Copy the dump out from the container:

    ```
    docker compose cp mysql|postgres:/[name-file-dump.slq] [destination folder]
    ```

9. Now, put both dumps (the customer dump, and the extract dump from your bundle version) in the  `/src/main/resources/`

10. Rename the environment variables in `Main.java` to your files, following the rules below:
   - `_DATABASE_TYPE`: Database type supported _(MySQL/Postgresql)_
   - `_SOURCE_FILE_NAME`: Name file dump with the Liferay Scheme.
   - `_TARGET_FILE_NAME`: Name file dump with the Customer Scheme.
   - `_NEW_FILE_NAME`: New file name output.

11. Run the project

### Note
> This application will fix column issues when using the Pentaho tool.
