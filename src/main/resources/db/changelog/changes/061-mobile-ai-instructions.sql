--liquibase formatted sql

--changeset jeff:add-mobile-ai-instructions
ALTER TABLE mobiles
ADD COLUMN ai_instructions TEXT;
