

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
    created_at VARCHAR(255) NOT NULL,
    updated_at VARCHAR(255) NOT NULL
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

--------------------------------------------------------------------------------
