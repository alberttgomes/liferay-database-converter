# How to use tool to fix Pentaho issues

### Steps

1. Download your bundle version from `https://customer.liferay.com/en_US/downloads`

2. Create a MySQL Docker image, using the following command:
    ```
    docker run -it --name mysql_master -p 3307:3306 -e MYSQL_ROOT_PASSWORD= -e MYSQL_DATABASE=lportal -e MYSQL_ALLOW_EMPTY_PASSWORD=yes mysql:5.7 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    ```

3. Go to the bundle you just downloaded, and in the root folder, create a file `portal-ext.propeties` and put the following properties:
    ```
    jdbc.default.driverClassName=com.mysql.cj.jdbc.Driver
    jdbc.default.url=jdbc:mysql://localhost:3307/lportal?useUnicode=true&characterEncoding=UTF-8&useFastDateParsing=false
    jdbc.default.username=root
    jdbc.default.password=
    ```

4. Now, go to the **tomcat/bin** folder in your bundle, and run:
   ``
   ./catalina.sh jpda run
   `` 

5. After starting the portal, go to the docker container and extract a dump file:
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

6. Now, put both dumps (the customer dump, and the extract dump from your bundle version) in the  `/src/main/resources/`

7. Rename the environment variables in `Main.java` to your files, following the rules below:
   - `_DATABASE_TYPE`: Database type supported _(MySQL/Postgresql)_
   - `_SOURCE_FILE_NAME`: Name file dump with the Liferay Scheme.
   - `_TARGET_FILE_NAME`: Name file dump with the Customer Scheme.
   - `_NEW_FILE_NAME`: New file name output.

8. Run the project

### Note
> Tool to fix the column constraints issue when using the Pentaho tool.
