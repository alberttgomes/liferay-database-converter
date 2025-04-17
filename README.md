# How to use App to fix Column Issues

## Requirements:
- Java 21
- Docker and Docker Compose
- Gradle
  
### How to get a dump file from Customer's Liferay Database Scheme

1. In your customer liferay's workspace, run: 
   
   a. ``./gradlew initBundle``

   b. deploy all custom modules, and make sure that are in 'bundles/osgi/modules'

2. Create a MySQL|PostgresSQL Docker image using customer workspace:

   a. Create a docker service to postgresSQL, should be something like:

      ```
      database:
         image: postgres:14.13
         environment:
           - POSTGRES_USER=[CUSTOMER_USER_NAME]
           - POSTGRES_PASSWORD=[CUSTOMER_PASSWORD]
           - POSTGRES_DB=[CUSTOMER_SCHEME_NAME]
         healthcheck:
           test: [ "CMD", "pg_isready", "-U", "[CUSTOMER_USER_NAME]", "-d", "[CUSTOMER_SCHEME_NAME]", "-h", "[DOMAIN]" ]
           interval: 10s
           timeout: 5s
           retries: 2
         ports:
           - "[PORT]:5432"
      ```
      > You can follow the something similar to MySQL

3. Spin up Docker database service:
   
   ```
   docker compose up --build [DATABASE-SERVICE-NAME] -d
   ```

4. In your database properties, replace to the new database scheme.  
    MySQL:
    ```
    jdbc.default.driverClassName=com.mysql.cj.jdbc.Driver
    jdbc.default.url=jdbc:mysql://[DOMAIN]:3307/[CUSTOMER_SCHEME_NAME]?useUnicode=true&characterEncoding=UTF-8&useFastDateParsing=false
    jdbc.default.username=[CUSTOMER_USER_NAME]
    jdbc.default.password=[CUSTOMER_PASSWORD]
    ```
    PostgresSQL: 
    ```
    jdbc.default.driverClassName=org.postgresql.Driver
    jdbc.default.url=jdbc:postgresql://[DOMAIN]:5432/[CUSTOMER_SCHEME_NAME]
    jdbc.default.username=[CUSTOMER_USER_NAME]
    jdbc.default.password=[CUSTOMER_PASSWORD]
    ```
   
   Note
   > These configuration can be in Liferay's Docker service as environment variables

5. Start customer portal using docker or catalina, and taken the Liferay create the database scheme in 'PostGreSQL/MySQL'

6. After starting the portal, go to the docker container and extract a dump file:
   -  Go to docker container:
    ```
    docker compose exec mysql|postgres bash
    ```
    - Run the following command to generate a dump
       
    MySQL:
    ```
    mysqldump -u [CUSTOMER_USER_NAME] -p[CUSTOMER_PASSWORD] [CUSTOMER_SCHEME_NAME] > [file-name-dump-with-timestamp.sql]
    ```
    PostgresSQL:
    ```
    pg_dump -U [CUSTOMER_USER_NAME] -d [CUSTOMER_SCHEME_NAME] -f [file-name-dump-with-timestamp.sql]
    ```  

    - Copy the dump out from the container:

    ```
    docker compose cp mysql|postgres:/[file-name-dump-with-timestamp.sql] [destination folder]
    ```

7. Now, put both dumps (the customer dump converted by pentaho, and the extracted dump from your bundle version) in the  same directory.

## Build

- Go to the root project folder, and execute:

    ``./gradlew build``
  
## How to run the project by pipeline

- Flags: 
```
-d --database-type    to reference the database will be converted (must be **postgres|mysql**)
-p --path             to reference the path where the files are located
-sf --source-file     to reference the source file name
-tf --target-file     to refrecne the target file name
```

## Run

``` 
java -jar build/libs/liferay-database-migrate-tools-[current-version]-SNAPSHOT.jar -d [DATABASE-TYPE] -p [FULL-DIRECTORY-FILES-ARE-ALOCATED] -sf [SOURCE-FILE].sql -tf [TARGET-FILE].sql -nf [NEW-DUMP-NAME].sql
```

### Note
> This application will fix column and constraints issues when using the Pentaho tool.
