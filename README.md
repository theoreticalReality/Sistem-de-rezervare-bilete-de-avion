# Sistem-de-rezervare-bilete-de-avion
Un proiect realizat in echipa de catre 4 in care dezvoltam o aplicatie de gestionare a zborurilor din cadrul unui aeroport si rezervarea biletelor.

## Baza de date MySQL

Aplicatia este configurata sa foloseasca MySQL pe `localhost:3306`, baza de date `airportdb`, utilizator `airport_user`, parola `airport_pass`.

Workbench nu trebuie pornit pentru ca aplicatia sa functioneze. Workbench este doar o interfata vizuala pentru administrare; serverul MySQL trebuie sa ruleze separat, ca serviciu.

Varianta simpla cu Docker:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Containerul MySQL are `restart: unless-stopped`, deci reporneste automat cand porneste Docker Desktop. Datele raman salvate in volumul `mysql-data`.

Daca folosesti MySQL instalat direct pe Windows, porneste serviciul MySQL din Services si creeaza utilizatorul o singura data:

```powershell
.\scripts\setup-mysql.ps1
.\mvnw.cmd spring-boot:run
```

Scriptul cere parola de root MySQL si executa automat:

```sql
CREATE DATABASE IF NOT EXISTS airportdb;
CREATE USER IF NOT EXISTS 'airport_user'@'%' IDENTIFIED BY 'airport_pass';
GRANT ALL PRIVILEGES ON airportdb.* TO 'airport_user'@'%';
FLUSH PRIVILEGES;
```

Pentru alte date de conectare, seteaza variabilele de mediu inainte de pornire:

```powershell
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:MYSQL_DATABASE="airportdb"
$env:MYSQL_USER="airport_user"
$env:MYSQL_PASSWORD="airport_pass"
.\mvnw.cmd spring-boot:run
```
