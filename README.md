# ProfileService

## About

Default configuration for this microservice (env):

```
PORT=8081

DB_MODE=LOCAL     # LOCAL or gateway
DB_HOST=localhost
DB_PORT=8082

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
LOGGER_ACTIVITY_CHANNEL=logger:activity
LOGGER_ERROR_CHANNEL=logger:error
```

Default port for this service is 8082\. [Application.kt](app/src/main/kotlin/com/mad/profile/Application.kt)/

### Routes:

Profile Routes:

- POST /profiles - Create a new profile
- GET /profiles/{id} - Get a profile by ID
- PUT /profiles/{id} - Update a profile
- DELETE /profiles/{id} - Delete a profile
- GET /profiles?page={page No}&pageSize={page size} - List all profiles

Followers Routes:

- POST /profiles/{id}/follow - follow a user (`{id}` is the followee)
- POST /profiles/{id}/unfollow - unfollow a user
- GET /profiles/{id}/followers - list user s followers (paginated)
- GET /profiles/{id}/following - list users that the user is following (paginated)

### Profiles query examples

**Create a new profile**

URL:

```
POST http://localhost:8082/profiles
```

Body:

```json
{
  "profile": {
    "id": "user123",
    "name": "Alice",
    "email": "user123@example.com",
    "birthdate": {"year": 1990, "month": 5, "day": 20}
  }
}
```

Response:

```json
{
  "id": "user123",
  "name": "Alice",
  "email": "user123@example.com",
  "birthdate": {
    "year": 1990,
    "month": 5,
    "day": 20
  }
}
```

**Get profile**

URL (body is empty):

```
GET http://localhost:8082/profiles/user123
```

Response:

```json
{
  "id": "user123",
  "name": "Alice",
  "email": "user123@example.com",
  "bio": "",
  "birthdate": {
    "year": 1990,
    "month": 5,
    "day": 20
  }
}
```

**Update profile**

URL:

```
PUThttp://localhost:8082/profiles/user123
```

Body:

```json
{
  "profile": {
    "id": "user123",
    "name": "Alice",
    "email": "user123.new@example.com",
    "birthdate": {"year": 1990, "month": 5, "day": 20}
  }
}
```

Response:

```json
{
  "id": "user123",
  "name": "Alice",
  "email": "user123.new@example.com",
  "bio": "",
  "birthdate": {
    "year": 1990,
    "month": 5,
    "day": 20
  }
}
```

**Delete profile**

URL (Body is empty):

```
DELETE http://localhost:8082/profiles/user456
```

Response:

`204 No Content`

**List profiles**

URL:

```
GET http://localhost:8082/profiles?page=1&pageSize=10
```

Response:

```
[
    {
        "id": "user123",
        "name": "Alice",
        "email": "user123.new2@example.com",
        "bio": "",
        "birthdate": {
            "year": 1990,
            "month": 5,
            "day": 20
        }
    },
    {
        "id": "user456",
        "name": "aboba",
        "email": "user456@example.com",
        "bio": "meow",
        "location": {
            "country": "Russia",
            "city": "Moscow"
        },
        "birthdate": {
            "year": 1992,
            "month": 7,
            "day": 15
        },
        "weight": 75.5,
        "height": 180.0
    }
]
```

### Followers query examples

**Follow user**

URL:

```
POST http://localhost:8082/profiles/user456/follow
```

Body:

```json
{
    "follower_id": "user123",
    "followee_id": "user456"
}
```

Response:

`204 No Content`

**Unfollow user**

URL:

```
POST http://localhost:8082/profiles/user456/unfollow
```

Body:

```json
{
    "follower_id": "user123",
    "followee_id": "user456"
}
```

Response:

`204 No Content`

**List followers**

URL (Body is empty):

```
GET http://localhost:8082/profiles/user456/followers?page=1&pageSize=10
```

Response:

```json
{
  "follower_ids": [
    "user123"
  ]
}
```

**List following**

URL (Body is empty):

```
GET http://localhost:8082/profiles/user123/following?page=1&pageSize=10
```

Response:

```json
{
  "following_ids": [
    "user456"
  ]
}
```

## Profiles

```proto
message Location {
  string country = 1;
  string city = 2;
}

message Birthdate {
  int32 year = 1;
  int32 month = 2;
  int32 day = 3;
}

message UserProfile {
  string id = 1;
  string name = 2;
  string email = 3;
  string image_id = 6;
  string bio = 7;
  optional Location location = 8;
  Birthdate birthdate = 9;
  double weight = 10;
  double height = 11;
  int32 follower_count = 12;
  int32 following_count = 13;
}
```

Profile actions:

```proto
service ProfileService {
  rpc CreateProfile(CreateProfileRequest) returns (UserProfile);
  rpc GetProfile(GetProfileRequest) returns (UserProfile);
  rpc ListProfiles(ListProfilesRequest) returns (ListProfilesResponse);
  rpc UpdateProfile(UpdateProfileRequest) returns (UserProfile);
  rpc DeleteProfile(DeleteProfileRequest) returns (google.protobuf.Empty);
}

message CreateProfileRequest {
  UserProfile profile = 1;
}

message GetProfileRequest {
  string id = 1;
}

message ListProfilesRequest {
  int32 page = 1;
  int32 page_size = 2;
}

message ListProfilesResponse {
  repeated UserProfile profiles = 1;
}

message UpdateProfileRequest {
  UserProfile profile = 1;
}

message DeleteProfileRequest {
  string id = 1;
}
```

## Followers

Actions for followers:

```proto
service ProfileService {
  rpc Follow(FollowRequest) returns (google.protobuf.Empty);
  rpc Unfollow(UnfollowRequest) returns (google.protobuf.Empty);
  rpc ListFollowers(ListFollowersRequest) returns (ListFollowersResponse);
  rpc ListFollowing(ListFollowingRequest) returns (ListFollowingResponse);
}

message FollowRequest {
  string follower_id = 1;
  string followee_id = 2;
}

message UnfollowRequest {
  string follower_id = 1;
  string followee_id = 2;
}

message ListFollowersRequest {
  string user_id = 1;
  int32 page = 2;
  int32 page_size = 3;
}

message ListFollowersResponse {
  repeated string follower_ids = 1;
}

message ListFollowingRequest {
  string user_id = 1;
  int32 page = 2;
  int32 page_size = 3;
}

message ListFollowingResponse {
  repeated string following_ids = 1;
}
```

## SQL

```sql
CREATE TABLE profiles (
    id VARCHAR(255) PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    image_id VARCHAR(255),
    bio TEXT,
    country TEXT,
    city TEXT,
    birthdate VARCHAR(10) NOT NULL,
    weight NUMERIC(6,2),
    height NUMERIC(6,2),
    user_id VARCHAR(255)
);

CREATE INDEX idx_profiles_name ON profiles (name);

CREATE TABLE followers (
    follower_id VARCHAR(255) NOT NULL,
    followee_id VARCHAR(255) NOT NULL,
    created_at VARCHAR(255),

    CONSTRAINT pk_follow PRIMARY KEY (follower_id, followee_id),
    CONSTRAINT fk_follower FOREIGN KEY (follower_id) REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_followee FOREIGN KEY (followee_id) REFERENCES profiles(id) ON DELETE CASCADE
);

CREATE INDEX idx_followers_followee ON followers (followee_id);
CREATE INDEX idx_followers_follower ON followers (follower_id);
```
