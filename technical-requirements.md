## Stack

* Spring Boot 4.0.6
* Postgres 18
* OpenJDK 25

## BTW
1. The application will be deployed to dedicated server with docker compose.


## General
1. Dockerize the application
2. Swagger UI and Postman collection
3. Layer-by-layer code organization
4. Use env for secrets and configurations
5. Implement access and refresh tokens using JWT include these claims (user_id, role) [use auth0 library]
6. Handle all the exceptions and return appropriate error responses but log the errors with stack trace to the logs DEBUG LEVEL.
7. Save the logs to the logs/ and each log file needs to contain the daily logs. Delete the logs older than 7 days.
8. Use UUID v7 for id generation
9. Enable actuator for health checks, logging, metrics and the admin needs to be able to control the logging level.
10. Use Flyway for database migration 
11. Implement tests for only integration tests. NO need unit tests.
12. Use simple pagination [page, limit, total, data]
13. Implement donwload file with token logic.
14. Use seperate dto records for each request and response if they differ from each other. Otherwise use single dto.
15. Passwords needs to be encrypted using bcrypt.
16. The Admin's username and raw password retrived from .env file at startup and inserted in DB.
17. add created_at and updated_at to necessary tables
18. /service needs to include these services: AuthService, UserService, ApplicationService, FileService, ExamService, ContractService, JwtService.
19. Use JpaRepository for database interaction in /repository package.
20. Implement repositories for all the entities.
21. Use @Transactional annotation for updating and deleting records.
22. Use @PreAuthorize annotation for authorization.
