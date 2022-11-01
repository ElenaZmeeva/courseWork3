--liquibase formatted sql

--changed elenazmeeva:1
CREATE TABLE notification_task(
id BIGSERIAL PRIMARY KEY,
chat_id BIGINT NOT NULL,
task_text TEXT NOT NULL,
sendTime TIME WITH TIME ZONE
);