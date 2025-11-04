
# Project Title

This project will build a website (Pawn-service)

## Quick start
```bash
 docker-compose up -d
```

## Swagger
1. http://localhost:8080/swagger-ui/index.html
2. /api/auth/token with username and password default admin/admin

## Acknowledgements
Backend:
- Java 21
- Spring boot
- Mysql

Front-end
- Reactjs

## Documentation

[Front-end](https://github.com/maituananh/pawn-service-fe.git)

[Back-end uses anhkiet-branch branch](https://github.com/maituananh/backend.git)

## Installation

Clone the project

```bash
  git clone git@github.com:maituananh/backend.git
```

Go to the project directory

```bash
  cd backend
```

Install dependencies

```bash
  ./gradlew build
```

Start the server

```bash
  ./gradlew bootRun
```
## Run Locally
ADMIN account: admin/admin

CUSTOMER account: user/user

AUTHENTICATION API

- Login:
`curl --location --request POST 'http://localhost:8080/api/auth/token' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=14C1B55026808D21AEA99858E483CEA8' \
--data '{
    "username": "admin",
    "password": "admin"
}'
`

- Logout:
  `curl --location --request POST 'http://localhost:8080/api/auth/logout' \
--header 'Authorization: access-token'
  `

- Renew token:
  `
  curl --location --request POST 'http://localhost:8080/api/auth/refresh-token' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=14C1B55026808D21AEA99858E483CEA8' \
--data '{
    "refreshToken": "refresh-token"
}'

PRODUCT API

- Create product
`curl --location --request POST 'http://localhost:8080/api/products' \
--header 'Content-Type: application/json' \
--header 'Authorization: Authorization: access-token' \
--header 'Cookie: JSESSIONID=23F6878A229C193A3B655A821147BB9C' \
--data '{
    "name": "product1",
    "price": 12.2,
    "startDay": "2025-08-09T00:00:00Z",
    "endDate": "2025-08-09T00:00:00Z",
    "type": "abc"
}'
`
- Get Product
`curl --location 'http://localhost:8080/api/products' \
--header 'Authorization: access-token' \
--header 'Cookie: JSESSIONID=23F6878A229C193A3B655A821147BB9C'
`
## Environment Variables

To run this project, you will need to add the following environment variables to your application.yml file

Database:

`DB_HOST`

`DB_PORT`

`DB_USERNAME`

`DB_PASSWORD`

`AWS_S3_ACCESS_KEY`

`AWS_S3_SECRET_KEY`

`AWS_S3_ENDPOINT`

## Running Tests

To run tests, run the following command

```bash
  npm run test
```


## Deployment

```bash
How to do CI/CD?
1. Install docker on VPS
2. Create developer user: sudo addUser developer
3. Allow docker for developer user: sudo usermod -aG docker developer
4. Run github-runner by background: nohup ./run.sh > runner.log 2>&1 &
```

