# Task Manager

## Описание
Проект для управления задач пользователей с использованием Spring Boot и хранением в PostgreSQL

## Технологии
- Java 17
- Spring Boot
- PostgreSQL
- Docker
- Hibernate

## Запуск проекта

### 1. Клонировать репозиторий
```bash
git clone https://github.com/xiao9-1/Task_Manager
cd Task_Manager
```

### 2. Запуск PostgreSQL через Docker
```bash
cd docker && docker compose up -d && cd ..
./gradlew bootRun
```

### 2.1 (Опционально) Локальный запуск приложения с базой данных H2
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```
 Для запуска используются настройки из `application-dev.properties`

## Остановка приложения

### 1. Остановить Spring Boot приложение
Перейти в терминал, в котором приложение было запущено и выполнить сочетание клавиш:

```bash
Ctrl + C
```

### 2. Остановить Docker контейнер

```bash
cd docker && docker compose down && cd ..
```
### 3. (Опционально) Если необходимо полностью пересоздать базу данных

```bash
cd docker && docker compose down -v && cd ..
```
Команда `down -v `удаляет volume и все данные PostgreSQL.


## Конфигурация
Переменные окружения docker-compose задаются через файл `.env`. Если он отсутствует, контейнер запустится с настройками по умолчанию.

Для настройки собсвтенных переменных окружения необходимо воспользоваться файлом `.env.example` и подставить его содержимое со своими значениями в `.env`.

## Работа с базой данных

### 1. Подключение к PostgreSQL через терминал

Используйте значения из файла `.env`:

- `POSTGRES_USER`
- `POSTGRES_DB`

Пример подключения:

```bash
docker exec -it task_manager_postgres psql -U <POSTGRES_USER> -d <POSTGRES_DB>
```

### 2. (Опционально) Подключение к H2 через Web интерфейс

Перейдите по ссылке в браузере **http://localhost:8080/h2-console**

Ввести настройки для подключения из `application-dev.properties` и нажать connect

- **JDBC URL:** `jdbc:h2:mem:testdb`
- **User Name:** `sa`
- **Password:** *(оставить пустым)*

## API Эндпоинты

Базовый URL: **http://localhost:8080**

### Пользователи

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/users` | Список всех пользователей |
| GET | `/users?sortBy=taskCount&order=desc` | Топ пользователей по задачам |
| GET | `/users?sortBy=name&order=asc` | Сортировка по имени (А→Я) |
| GET | `/users?sortBy=name&order=desc` | Сортировка по имени (Я→А) |
| GET | `/users/{id}` | Получить пользователя по ID |
| POST | `/users` | Создать пользователя |

### Задачи

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/tasks` | Список всех задач |
| GET | `/tasks/user/{userId}` | Задачи конкретного пользователя |
| GET | `/tasks/{id}` | Получить задачу по ID |
| POST | `/tasks` | Создать задачу |
| PUT | `/tasks/{id}` | Обновить задачу |
| DELETE | `/tasks/{id}` | Удалить задачу |
| POST | `/tasks/{id}/complete` | Отметить задачу как выполненную |







