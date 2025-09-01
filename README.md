This is template project spring
Spring version: 3.5.4
Gradle
Lombok
Mysql: Change DB, username, password at application.yml

How to login?
1: Request by POST: http://localhost:8080/api/auth/token
2: Enter body { "username": "admin", "password": "admin" }
3: Get accessToken and refreshToken