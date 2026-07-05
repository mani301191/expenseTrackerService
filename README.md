# Expense Tracking Application with Spring Boot and MongoDB

## Overview
This is an Expense Tracking Application built using **Spring Boot** and **MongoDB**. The application allows users to track their expenses efficiently with features like duplicate record validation and formatted date handling.

## Prerequisites
- **Java 17** or higher
- **Maven** (for dependency management)
- **MongoDB** (running on `localhost:27018` as per the default configuration)

## Features
- **Expense Management**: Add, update, and manage expense records.
- **Duplicate Record Validation**: Ensures no duplicate expense records are added.
- **Date Formatting**: Formats dates in `dd/MM/yyyy` format.
- **Customizable Configuration**: Modify application properties such as MongoDB URI and server port.

## Configuration
The application uses the following default configurations (defined in `application.properties`):
- **MongoDB URI**: `mongodb://localhost:27018/personal`
- **Server Port**: `8003`
- **Spring MVC View Suffix**: `.html`

## Project Structure
- **`src/main/java/com/audit/myexpense/util/ExpenseCommonUtil.java`**: Contains utility methods like `formattedDate` for date formatting.
- **`src/main/java/com/audit/myexpense/util/ExpenseConstant.java`**: Defines constants such as `DUPLICATE_RECORD`.
- **`src/main/resources/application.properties`**: Holds application-level configurations.

## How to Run the Application
1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd <repository-directory>
   ```
2. **Build the Application**:
   ```bash
    mvn clean install
    ```
3. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
4. **Access the Application**:
   Open your web browser and navigate to `http://localhost:8003` to access the Expense Tracking Application.


## Reference Documentation

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MongoDB Documentation](https://www.mongodb.com/docs/)

## License
This project is licensed under the **All Rights Reserved** license. You may use, distribute, and modify this code as per the license terms.

## Author
**Manikandan Narasimhan**