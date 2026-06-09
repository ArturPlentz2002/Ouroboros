CREATE TABLE user_profiles (
    user_id      UUID PRIMARY KEY,
    email        VARCHAR(255)  NOT NULL,
    display_name VARCHAR(100)  NOT NULL,
    avatar_url   VARCHAR(500),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);
