[![Actions Status](https://github.com/opifexM/java-project-73/workflows/hexlet-check/badge.svg)](https://github.com/opifexM/java-project-73/actions)
[![Java CI](https://github.com/opifexM/java-project-73/actions/workflows/main.yml/badge.svg)](https://github.com/opifexM/java-project-73/actions/workflows/main.yml)
[![Maintainability](https://api.codeclimate.com/v1/badges/9a95de0a60c38729f54d/maintainability)](https://codeclimate.com/github/opifexM/java-project-73/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/9a95de0a60c38729f54d/test_coverage)](https://codeclimate.com/github/opifexM/java-project-73/test_coverage)


# Task Manager

Web Site: https://taskm.herokuapp.com/  
Swagger: https://taskm.herokuapp.com/swagger-ui/index.html

## Overview

This project is a Spring Boot web application that provides APIs for managing various resources: Labels, Statuses, Tasks, and Users.
It is implemented Spring Security 6 with JWT authentication, ensuring robust security standards and Argon2 for password encoding 
and uses a stack of advanced technologies such as Liquibase, Testcontainers, PostgreSQL, and MapStruct for efficient and reliable database management and object mapping.

## Dependencies

The project uses various dependencies managed by Gradle. Key dependencies include:

-   Spring Boot and its various starters (web, data JPA, actuator, security)
-   Spring Security Crypto Argon2
-   Bouncy Castle for Java cryptography
-   JSON Web Tokens (JJWT)
-   PostgreSQL and H2 Database drivers
-   Liquibase for database migration
-   SpringDoc OpenAPI for API documentation
-   Hibernate Validator for bean validation
-   MapStruct for object mapping
-   Lombok for reducing boilerplate code
-   Testcontainers for integration testing
-   Rollbar error tracking

## APIs

The application exposes the following RESTful APIs:

### Label Management API

-   **GET /labels**: List all labels
-   **GET /labels/{id}**: Get a label by ID
-   **POST /labels**: Create a new label
-   **PUT /labels/{id}**: Update a label by ID
-   **DELETE /labels/{id}**: Delete a label by ID

### Status Management API

-   **GET /statuses**: List all statuses
-   **GET /statuses/{id}**: Get a status by ID
-   **POST /statuses**: Create a new status
-   **PUT /statuses/{id}**: Update a status by ID
-   **DELETE /statuses/{id}**: Delete a status by ID

### Task Management API

-   **GET /tasks**: List all tasks
-   **GET /tasks/{id}**: Get a task by ID
-   **POST /tasks**: Create a new task
-   **PUT /tasks/{id}**: Update a task by ID
-   **DELETE /tasks/{id}**: Delete a task by ID

### User Management API

-   **GET /users**: List all users
-   **GET /users/{id}**: Get a user by ID
-   **POST /users**: Create a new user
-   **PUT /users/{id}**: Update a user by ID
-   **DELETE /users/{id}**: Delete a user by ID