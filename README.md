
# Project Title

This project will build a website (Pawn-service)


## Acknowledgements
Backend:
- Java 21
- Spring boot
- Mysql

Front-end
- Reactjs



## Documentation

[Front-end uses anhkiet branch](https://github.com/maituananh/front-app)

[Back-end uses anhkiet-branch branch](https://github.com/maituananh/front-app)


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

- Login

`curl --location 'http://localhost:8080/api/users/token' \
--header 'Content-Type: application/json' \
--data '{
    "username": "admin",
    "password": "admin"
}'
`

- Logout
  `curl --location --request POST 'http://localhost:8080/api/auth/logout' \
  --header 'Authorization: Bearer access-token'
  `

- Renew token
  `
    curl --location 'http://localhost:8080/api/auth/refresh-token' \
  --header 'Content-Type: application/json' \
  --data '{
      "refreshToken": "access-token"
  }'
  `
## Environment Variables

To run this project, you will need to add the following environment variables to your application.yml file

Database:

`DB_HOST`

`DB_PORT`

`DB_USERNAME`

`DB_PASSWORD`


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

