create table file_metadata (
    id           uuid primary key,
    user_id      uuid not null,
    filename     varchar(512) not null,
    content_type varchar(255),
    size         bigint not null,
    storage_key  varchar(1024) not null,
    created_at   timestamptz not null
);

create index idx_file_metadata_user on file_metadata (user_id);
