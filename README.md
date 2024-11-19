# How to use tool to fix Pentaho issues

## Requirements:
- Java 21
- Docker and Docker Compose

### Steps

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
    ```
    jdbc.default.driverClassName=com.mysql.cj.jdbc.Driver
    jdbc.default.url=jdbc:mysql://localhost:3307/lportal?useUnicode=true&characterEncoding=UTF-8&useFastDateParsing=false
    jdbc.default.username=root
    jdbc.default.password=
    ```

5. Now, go to the **tomcat/bin** folder in your bundle, and run:
   ``
   ./catalina.sh jpda run
   `` 

6. After starting the portal, go to the docker container and extract a dump file:
   -  Go to docker container:
    ```
    docker exec -it mysql_master bash
    ```
    - Run the following command to generate a dump
    ```
    mysql -u lportal lportal > [name-file-dump.sql]
    ```
    - Copy the dump out from the container:
    ```
    docker cp mysql_master:/[name-file-dump.slq] [destination folder]
    ```

7. Now, put both dumps (the customer dump, and the extract dump from your bundle version) in the  `/src/main/resources/`

8. Rename the environment variables in `Main.java` to your files, following the rules below:
   - `_DATABASE_TYPE`: Database type supported _(MySQL/Postgresql)_
   - `_SOURCE_FILE_NAME`: Name file dump with the Liferay Scheme.
   - `_TARGET_FILE_NAME`: Name file dump with the Customer Scheme.
   - `_NEW_FILE_NAME`: New file name output.

9. Run the project

### Note
> This application will fix column issues when using the Pentaho tool.
