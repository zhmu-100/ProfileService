# Profile Service

## Описание

Это микросервис для управления профилями пользователей и их подписками. Он построен с использованием Ktor и Kotlin.

### Конфигурация по умолчанию:

```hocon
ktor {
    deployment {
        port = 8081
        host = "0.0.0.0"
    }

    application {
        modules = [com.mad.profile.ApplicationKt.module]
    }

    database {
        localhost = "localhost"
        localhost_port = "5432"

        GATEWAY = "gateway-host"
        GATEWAY_port = "8080"

        mode = "LOCAL"

        username = "postgres"
        password = "postgres"
        dbName = "postgres"
    }
}
```

Порт по умолчанию для данного сервиса - `8081`.

---

## Роуты

### Роуты профиля

- `GET api/profiles/{id}` — Получить профиль по ID
- `GET /api/profiles?page={page}&pageSize={pageSize}` — Получить список всех пользователей
- `POST api/profiles` — Создать новый профиль
- `PUT api/profiles/{id}` — Обновить профиль по ID
- `DELETE api/profiles/{id}?user_id={userId}` — Удалить профиль

### Роуты подписок

- `GET api/profiles/followers/{id}` — Получить список подписчиков по ID профиля
- `GET api/profiles/following/{id}` — Получить список подписок по ID профиля
- `POST api/profiles/follow` — Подписаться на другой профиль
- `DELETE api/profiles/unfollow` — Отписаться от профиля

---

## Примеры запросов

### Получить профиль по ID

**URL**:  
`GET http://localhost:8081/api/profile/{id}`

**Ответ**:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Alice",
  "email": "alice@example.com",
  "bio": "Tech enthusiast",
  "location": {
    "country": "Germany",
    "city": "Berlin"
  },
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "weight": 60,
  "height": 170,
  "followerCount": 100,
  "followingCount": 50
}
```

### Получить список всех пользователей

**URL**:  
`GET /api/profiles?page=1&pageSize=10`

**Ответ**:

```json
{
  "profiles": [
    {
      "id": "ae607744-267e-4778-8382-3c65adb1a410",
      "name": "Alice Smith",
      "email": "alice.smith@example.com",
      "bio": "This is Alice.",
      "location": {
        "country": "Canada",
        "city": "Toronto"
      },
      "birthdate": {
        "year": 1992,
        "month": 7,
        "day": 20
      },
      "weight": 65.0,
      "height": 170.0,
      "followerCount": 1
    },
    {
      "id": "3d8e4b6b-ba68-4aa5-a156-bc322145c51f",
      "name": "Bob Johnson",
      "email": "bob.johnson@example.com",
      "bio": "This is Bob.",
      "location": {
        "country": "UK",
        "city": "London"
      },
      "birthdate": {
        "year": 1985,
        "month": 12,
        "day": 5
      },
      "weight": 80.0,
      "height": 185.0,
      "followingCount": 1
    }
  ],
  "page": 1,
  "pageSize": 10,
  "total": 2
}
```

### Создать профиль

**URL**:  
`POST http://localhost:8081/api/profiles`

**Тело запроса**:

```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "bio": "Tech enthusiast",
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "location": {
    "country": "Germany",
    "city": "Berlin"
  },
  "weight": 60,
  "height": 170,
  "imageId": "img_789"
}
```

**Ответ**:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Alice",
  "email": "alice@example.com",
  "bio": "Tech enthusiast",
  "location": {
    "country": "Germany",
    "city": "Berlin"
  },
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "weight": 60,
  "height": 170,
  "followerCount": 0,
  "followingCount": 0
}
```

### Обновить профиль

**URL**:  
`PUT http://localhost:8081/api/profiles/{id}`

**Тело запроса**:

```json
{
  "name": "Alice Updated",
  "email": "alice.new@example.com",
  "bio": "Senior developer",
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "location": {
    "country": "Germany",
    "city": "Munich"
  },
  "weight": 59,
  "height": 171,
  "imageId": "img_999"
}
```

**Ответ**:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Alice Updated",
  "email": "alice.new@example.com",
  "bio": "Senior developer",
  "location": {
    "country": "Germany",
    "city": "Munich"
  },
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "weight": 59,
  "height": 171,
  "followerCount": 0,
  "followingCount": 0
}
```

### Удалить профиль

**URL**:  
`DELETE http://localhost:8081/api/profiles/{id}?user_id={userId}`

**Ответ**:

```
Профиль удален
```

### Подписаться на пользователя

**URL**:  
`POST http://localhost:8081/api/profiles/follow`

**Тело запроса**:

```json
{
  "followerId": "123e4567-e89b-12d3-a456-426614174001",
  "followeeId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Ответ**:

```json
{
  "status": "success",
  "message": "Теперь вы следите за этим пользователем"
}
```

### Отписаться от пользователя

**URL**:  
`POST http://localhost:8081/api/profiles/unfollow`

**Тело запроса**:

```json
{
  "followerId": "123e4567-e89b-12d3-a456-426614174001",
  "followeeId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Ответ**:

```json
{
  "status": "success",
  "message": "Вы больше не следите за этим пользователем"
}
```

### Получить список подписчиков

**URL**:  
`GET http://localhost:8081/api/profiles/followers/{id}?page=1&pageSize=10`

**Ответ**:

```json
{
  "followerIds": ["123e4567-e89b-12d3-a456-426614174001"],
  "page": 1,
  "pageSize": 10,
  "total": 1
}
```

### Получить список подписок

**URL**:  
`GET http://localhost:8081/api/profiles/following/{id}?page=1&pageSize=10`

**Ответ**:

```json
{
  "followingIds": ["123e4567-e89b-12d3-a456-426614174000"],
  "page": 1,
  "pageSize": 10,
  "total": 1
}
```

---

## Структура данных

### Профиль

```kotlin
data class ProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val imageId: String? = null,
    val bio: String? = null,
    val location: LocationResponse? = null,
    val birthdate: BirthdateResponse? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0
)
```

---

## SQL

### Таблица профилей

```sql
create table profiles (
    id varchar(36) PRIMARY KEY,
    name varchar(255) not null,
    email varchar(255) not null,
    image_id varchar(255),
    bio text,
    country varchar(255),
    city varchar(255),
    birthdate varchar(255),
    weight double,
    height double,
    created_at timestamp,
    updated_at timestamp
);
```

---

## Profile Service

## Description

This is a microservice for managing user profiles and their subscriptions. It is built using Ktor and Kotlin.

### Default Configuration:

```hocon
ktor {
    deployment {
        port = 8081
        host = "0.0.0.0"
    }

    application {
        modules = [com.mad.profile.ApplicationKt.module]
    }

    database {
        localhost = "localhost"
        localhost_port = "5432"

        GATEWAY = "gateway-host"
        GATEWAY_port = "8080"

        mode = "LOCAL"

        username = "postgres"
        password = "postgres"
        dbName = "postgres"
    }
}
```

The default port for this service is `8081`.

---

## Routes

### Profile Routes

- `GET api/profiles/{id}` - Get profile by ID
- `GET /api/profiles?page={page}&pageSize={pageSize}` - Get a list of all users
- `POST api/profiles` - Create a new profile
- `PUT api/profiles/{id}` - Update profile by ID
- `DELETE api/profiles/{id}?user_id={userId}` - Delete profile

### Subscription Routes

- `GET api/profiles/followers/{id}` - Get a list of subscribers by profile ID
- `GET api/profiles/following/{id}` - Get a list of subscriptions by profile ID
- `POST api/profiles/follow` - Subscribe to another profile
- `DELETE api/profiles/unfollow` - Unsubscribe from a profile

---

## Sample Queries

### Get profile by ID

**URL**:  
`GET http://localhost:8081/api/profile/{id}`

**Response**:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Alice",
  "email": "alice@example.com",
  "bio": "Tech enthusiast",
  "location": {
    "country": "Germany",
    "city": "Berlin"
  },
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "weight": 60,
  "height": 170,
  "followerCount": 100,
  "followingCount": 50
}
```

### Get a list of all users

**URL**:  
``GET /api/profiles?page=1&pageSize=10`.

**Response**:

```json
{
  "profiles": [
    {
      "id": "ae607744-267e-4778-8382-3c65adb1a410",
      "name": "Alice Smith",
      "email": "alice.smith@example.com",
      "bio": "This is Alice.",
      "location": {
        "country": "Canada",
        "city": "Toronto"
      },
      "birthdate": {
        "year": 1992,
        "month": 7,
        "day": 20
      },
      "weight": 65.0,
      "height": 170.0,
      "followerCount": 1
    },
    {
      "id": "3d8e4b6b-ba68-4aa5-a156-bc322145c51f",
      "name": "Bob Johnson",
      "email": "bob.johnson@example.com",
      "bio": "This is Bob.",
      "location": {
        "country": "UK",
        "city": "London"
      },
      "birthdate": {
        "year": 1985,
        "month": 12,
        "day": 5
      },
      "weight": 80.0,
      "height": 185.0,
      "followingCount": 1
    }
  ],
  "page": 1,
  "pageSize": 10,
  "total": 2
}
```


### Create a profile

**URL**:  
`POST http://localhost:8081/api/profiles`.

**Request Body**:

```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "bio": "Tech enthusiast",
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "location": {
    "country": "Germany",
    "city": "Berlin"
  },
  "weight": 60,
  "height": 170,
  "imageId": "img_789"
}
```

**Response**:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Alice",
  "email": "alice@example.com",
  "bio": "Tech enthusiast",
  "location": {
    "country": "Germany",
    "city": "Berlin"
  },
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "weight": 60,
  "height": 170,
  "followerCount": 0,
  "followingCount": 0
}
```

### Update Profile

**URL**:  
`PUT http://localhost:8081/api/profiles/{id}`.

**Request Body**:

```json
{
  "name": "Alice Updated",
  "email": "alice.new@example.com",
  "bio": "Senior developer",
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "location": {
    "country": "Germany",
    "city": "Munich"
  },
  "weight": 59,
  "height": 171,
  "imageId": "img_999"
}
```

**Response**:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Alice Updated",
  "email": "alice.new@example.com",
  "bio": "Senior developer",
  "location": {
    "country": "Germany",
    "city": "Munich"
  },
  "birthdate": {
    "year": 1995,
    "month": 7,
    "day": 20
  },
  "weight": 59,
  "height": 171,
  "followerCount": 0,
  "followingCount": 0
}
```

### Delete Profile

**URL**:  
`DELETE http://localhost:8081/api/profiles/{id}?user_id={userId}`

**Response**:

```
Profile deleted
```


### Subscribe to user

**URL**:
`POST http://localhost:8081/api/profiles/follow`.

**Request Body**:

```json
{
  "followerId": "123e4567-e89b-12d3-a456-426614174001",
  "followeeId": "123e4567-e89b-12d3-a456-426614174000"
}
```


**Response**:

```json
{
  "status": "success",
  "message": "Теперь вы следите за этим пользователем"
}
```

### Unsubscribe from user

**URL**:  
`POST http://localhost:8081/api/profiles/unfollow`.

**Request Body**:

```json
{
  "followerId": "123e4567-e89b-12d3-a456-426614174001",
  "followeeId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response**:

```json
{
  "status": "success",
  "message": "Вы больше не следите за этим пользователем"
}
```

### Get a list of subscribers

**URL**:  
`GET http://localhost:8081/api/profiles/followers/{id}?page=1&pageSize=10`.

**Response**:

```json
{
  "followerIds": ["123e4567-e89b-12d3-a456-426614174001"],
  "page": 1,
  "pageSize": 10,
  "total": 1
}
```

### Get a list of subscriptions

**URL**:  
`GET http://localhost:8081/api/profiles/following/{id}?page=1&pageSize=10`

**Response**:

```json
{
  "followingIds": ["123e4567-e89b-12d3-a456-426614174000"],
  "page": 1,
  "pageSize": 10,
  "total": 1
}
```

---

## Data structure

### Profile

```kotlin
data class ProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val imageId: String? = null,
    val bio: String? = null,
    val location: LocationResponse? = null,
    val birthdate: BirthdateResponse? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0
)
```

---

## SQL

### Profile table

```sql
create table profiles (
    id varchar(36) PRIMARY KEY,
    name varchar(255) not null,
    email varchar(255) not null,
    image_id varchar(255),
    bio text,
    country varchar(255),
    city varchar(255),
    birthdate varchar(255),
    weight double,
    height double,
    created_at timestamp,
    updated_at timestamp
);
```

---




