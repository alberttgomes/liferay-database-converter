# How to use the App to fix Column Issues

## Requirements:
- Java 21
- Docker and Docker Compose
- Gradle
  
### How to get a dump file from Customer's Liferay Database Scheme

1. In your customer liferay's workspace, run: 
   
   a. ``./gradlew initBundle``

   b. Deploy all custom modules, and make sure that they are in 'bundles/osgi/modules'

2. Create a MySQL|PostgreSQL Docker image using the customer workspace:

   a. Create a Docker service for PostgreSQL, something like:

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
      > You can follow something similar to MySQL

3. Spin up Docker database service:
   
   ```
   docker compose up --build [DATABASE-SERVICE-NAME] -d
   ```

4. In your database properties, replace the new database scheme.  
    MySQL:
    ```
    jdbc.default.driverClassName=com.mysql.cj.jdbc.Driver
    jdbc.default.url=jdbc:mysql://[DOMAIN]:3307/[CUSTOMER_SCHEME_NAME]?useUnicode=true&characterEncoding=UTF-8&useFastDateParsing=false
    jdbc.default.username=[CUSTOMER_USER_NAME]
    jdbc.default.password=[CUSTOMER_PASSWORD]
    ```
    PostgreSQL: 
    ```
    jdbc.default.driverClassName=org.postgresql.Driver
    jdbc.default.url=jdbc:postgresql://[DOMAIN]:5432/[CUSTOMER_SCHEME_NAME]
    jdbc.default.username=[CUSTOMER_USER_NAME]
    jdbc.default.password=[CUSTOMER_PASSWORD]
    ```
   
   Note
   > These configurations can be in Liferay's Docker service as environment variables

5. Start the customer portal using Docker or Catalina, and take the Liferay database scheme in 'PostgreSQL/MySQL'

6. After starting the portal, go to the Docker container and extract a dump file:
   -  Go to Docker container:
    ```
    docker compose exec mysql|postgresql bash
    ```
    - Run the following command to generate a dump
       
    MySQL:
    ```
    mysqldump -u [CUSTOMER_USER_NAME] -p[CUSTOMER_PASSWORD] [CUSTOMER_SCHEME_NAME] > [file-name-dump-with-timestamp.sql]
    ```
    PostgreSQL:
    ```
    pg_dump -U [CUSTOMER_USER_NAME] -d [CUSTOMER_SCHEME_NAME] -f [file-name-dump-with-timestamp.sql]
    ```  

    - Copy the dump out from the container:

    ```
    docker compose cp mysql|postgres:/[file-name-dump-with-timestamp.sql] [destination folder]
    ```

7. Put both dumps (the customer dump converted by Pentaho and the extracted dump from your bundle version) in the  same directory.

## Build

- Go to the root project folder, and execute:

    ``./gradlew build``
  
## How to run the project by pipeline

- Flags: 
```
-d --database-type    to reference the database will be converted (must be **postgresql|mysql**)
-in --index-name      to reference the unique index(es) to skip
-p --path             to reference the path where the files are located
-sf --source-file     to reference the source file name
-tf --target-file     to reference the target file name
```

## Run

``` 
java -jar build/libs/liferay-database-migrate-tools-[current-version]-SNAPSHOT.jar -d [DATABASE-TYPE] -in [Index(es)](Optional flag) -p [FULL-DIRECTORY-FILES-ARE-ALOCATED] -sf [SOURCE-FILE].sql -tf [TARGET-FILE].sql -nf [NEW-DUMP-NAME].sql
```

### Note
> This application will fix column and constraint issues using the Pentaho tool.
