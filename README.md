# Project Name

## Description
A brief description of the project.

## Prerequisites
- JDK 23
- Gradle
- PostgreSQL
- Azure Key Vault

## Installation

1. **Clone the repository:**
    ```sh
    git clone https://github.com/yourusername/your-repo-name.git
    cd your-repo-name
    ```

2. **Set up the database:**
    - Ensure PostgreSQL is installed and running.
    - Create a database named `research`.
    - Update the `src/main/resources/database.properties` file with your database credentials if necessary.

3. **Configure Azure Key Vault:**
    - Ensure you have access to Azure Key Vault.
    - Set up the necessary secrets in your Key Vault.

4. **Build the project:**
    ```sh
    ./gradlew build
    ```

## Running the Project

1. **Run the main application:**
    ```sh
    ./gradlew run
    ```

## Dependencies
- Kotlin
- Ktor
- Exposed
- PostgreSQL JDBC Driver
- Azure SDK

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.