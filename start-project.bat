@echo off
echo [1/3] Pornesc baza de date in Docker...
docker-compose up -d

echo [2/3] Pregatesc deschiderea browserului in Chrome...
:: Porneste un cronometru in fundal care va deschide Chrome peste 15 secunde
start /b "" cmd /c "timeout /t 15 > nul && start chrome http://localhost:8081"

echo [3/3] Pornesc aplicatia Spring Boot...
echo (Aplicatia va fi gata in aproximativ 15-20 de secunde)
.\mvnw.cmd spring-boot:run
