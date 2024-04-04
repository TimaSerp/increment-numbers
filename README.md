***Инструкция по запуску приложения***

```mvn clean install -P client -P server``` - профили нужны, чтобы maven не ругался на два main-класса

Edit Configurations -> + -> Spring boot\
**main class**: ru.serpov.incrementnumbers.ClientApplication\
**active profiles**: client\
run

Edit Configurations -> + -> Spring boot\
**main class**: ru.serpov.incrementnumbers.ServerApplication\
**active profiles**: server\
run

Для удобства я сделал рест-эндпойнт, постучаться к нему можно через курл:\
```curl http://localhost:8082/api/v1/getIncrement/0/30 ```\
Структура эндпойнта выглядит так: ```/getIncrement/{first}/{last}```, поэтому можно подставить любые кастомные границы промежутка