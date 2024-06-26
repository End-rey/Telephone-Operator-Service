# Telephone-Operator-Service
Тестовое задание для Nexign Bootcamp. Сервис, генерирующий CDR файлы, и сервис, агрегирующий данные в UDR.

# Описание задачи
## Дано:
Есть коммутатор, на котором фиксируются данные о звонках в CDR (Call Data Record) файлы.
На основе CDR файлов формируются UDR (Usage Data Report) файлы.

## Txt файлы CDR содержат записи следующего вида:
- тип вызова (01 - исходящие, 02 - входящие);
- номер абонента;
- дата и время начала звонка (Unix time);
- дата и время окончания звонка;
- разделитель данных – запятая;
- разделитель записей – перенос строки;
- данные обязательно формируются в хронологическом порядке;

## Json файлы UDR, содержащие записи следующего вида:
- msisdn – номер абонента;
- incomingCall - длительность входящего вызова в формате "HH:MM:SS";
- outcomingCall - длительность исходящего вызова в формате "HH:MM:SS";

## Задача 1:
Написать сервис, эмулирующий работу коммутатора, т.е. генерирующий CDR файлы.
### Условия:
1. 1 CDR = 1 месяц. Тарифицируемый период в рамках задания - 1 год;
2. Данные в CDR идут не по порядку, т.е. записи по одному абоненту могут быть в разных частях файла;
3. Количество и длительность звонков определяется случайным образом;
4. Установленный список абонентов (не менее 10) хранится в локальной БД (h2);
5. После генерации CDR, данные о транзакциях пользователя помещаются в соседнюю таблицу этой БД.

## Задача 2:
Напсиать сервис генерации UDR по полученным файлам CDR. Агрегировать данные по каждому абоненту в отчет.
### Условия:
1. Данные можно брать только из CDR файла. БД с описанием транзакций – тестовая, и доступа к ней, в рамках задания нет.
2. Сгенерированные объекты отчета разместить в /reports.
Шаблон имени: номер_месяц.json (79876543221_1.json);
3. Класс генератора должен содержать методы:
   -  generateReport() – сохраняет все отчеты и выводит в консоль таблицу со всеми абонентами и итоговым временем звонков по всему тарифицируемому периоду каждого абонента;
   - generateReport(msisdn) – сохраняет все отчеты и выводит в консоль таблицу по одному абоненту и его итоговому времени звонков в каждом месяце;
   - generateReport(msisdn, month) – сохраняет отчет и выводит в консоль таблицу по одному абоненту и его итоговому времени звонков в указанном месяце.

## Общие условия:
1. Конечное решение должно быть описано в одном модуле (монолит);
2. Допустимо использовать фреймворк Spring и его модули, но приложение НЕ должно запускаться на локальном веб-сервере;
3. По умолчанию должен срабатывать метод generateReport();
4. В директории /tests должно быть не мене 3 unit тестов;
5. К ключевым классам добавить javadoc описание;
6. Конечное решение размещаете на репозитории в github в виде проекта и jar файла с зависимостями;
7. В репозитории разместить md описание задания и вашего решения.

# Решение:
## Технологии:
- OpenJDK 17
- Maven
- Spring Boot Data JPA
- H2 database
- Jackson
- Junit5

## Архитектура:
Была выбрана слоистая архитектура с такими слоями:
- [Controller](./src/main/java/org/endrey/telephone/operator/controller/MainLoop.java) - В нем происходит основное взаимодействие с пользователем через командную строку.
- [Service](./src/main/java/org/endrey/telephone/operator/service) - Основная бизнес логика сервисов.
- [Repository](./src/main/java/org/endrey/telephone/operator/repository) - Работа с файловой базой данных и с базой данных H2.
- [Entity](./src/main/java/org/endrey/telephone/operator/entity) - Классы с сущностями.

## Запуск проекта:
```cmd
java -jar .\jar\telephone-operator-service-0.0.1-SNAPSHOT.jar
```

## Описание работы проекта:
При первом запуске проекта создается файл базы данных H2 и туда записываются данные по абонентам из файла [phoneNumbers.txt](./src/main/resources/phoneNumbers.txt). При последующих запусках база данных не пересоздается, а используется из файла.

Все взаимодействие просиходит через командную строку, поэтому логи сохраняются в файл.
При запуске проекта сначала срабатывает метод [generateReport()](./src/main/java/org/endrey/telephone/operator/service/serviceImpl/UDRServiceImpl.java). Далее появляется меню:
```cmd
1. Generate CDR
2. Generate Usage Data Report
3. Exit
```
### При вводе `1` выводится меню:
```cmd
Generate CDR
1. Generate CDR for month
2. Generate CDR for period
3. Back
```
При нажатии:
1. Генерирует CDR для месяца, который ввел пользователь
2. Генерирует CDR для всего тарифицируемого периода
3. Возвращает в предыдущее меню

Все файлы CDR сохраняются в директорию /CDR. Имя файла соответствует месяцу начала звонка.

### При вводе `2` выводится меню:
```cmd
1. Generate report
2. Generate report by msisdn
3. Generate report by msisdn and month
4. Exit
```
При нажатии:
1. Сохраняет все отчеты и выводит в консоль информацию о всех абонентах и их итоговое время звонков по всему тарифицируемому периоду.
2. Сохраняет все отчеты и выводит в консоль информацию об одном абоненте и его итоговому времени звонков в каждом месяце.
3. Сохраняет все отчеты и выводит в консоль информацию об одном абоненте и его итоговому времени звонков в указанном месяце.
4. Возвращает в предыдущее меню

Отчеты сохраняются в директорию /reports. Формат файла - JSON. Формат названия файла - номер_месяц.json

### При вводе `3` выходит из программы.