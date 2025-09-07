This is template project spring
Spring version: 3.5.4
Gradle
Lombok
Mysql: Change DB, username, password at application.yml

How to do CI/CD?
1. Install docker on VPS
2. Create developer user: sudo addUser developer
3. Allow docker for developer user: sudo usermod -aG docker developer
4. Run github-runner by background: nohup ./run.sh > runner.log 2>&1 &