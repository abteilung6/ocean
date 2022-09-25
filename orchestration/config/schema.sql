-- Revoke public schema access
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

DROP DATABASE IF EXISTS ocean_dev;

CREATE DATABASE ocean_dev WITH OWNER=postgres;
