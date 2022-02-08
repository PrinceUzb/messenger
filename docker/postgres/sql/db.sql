CREATE TABLE "users" (
  "id" UUID PRIMARY KEY,
  "email" VARCHAR NOT NULL,
  "nickname" VARCHAR NOT NULL,
  "created_at" TIMESTAMP NOT NULL,
  "password_hash" VARCHAR NOT NULL,
  unique (email)
);

CREATE TABLE IF NOT EXISTS "messages" (
  "id" UUID PRIMARY KEY,
  "to" UUID CONSTRAINT fk_receiver_id REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE,
  "from" UUID CONSTRAINT fk_sender_id REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE,
  "text" VARCHAR NOT NULL,
  "is_deleted" BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO "users"
VALUES ('404a8277-69d2-4cb1-bf62-55bcf03a17b8', 'test@test.test', 'Prince', '2023-01-01 00:00:00-00',
 '$s0$e0801$+PwRvk1FPEsJuSV0W731Qg==$ghh4S/ZVPATwzasot3PRIQaRg3Dzk+/e5vgDUAWhM1Q=');

INSERT INTO "users"
VALUES ('6563fb88-994a-4836-812a-2d24188304e0', 'test@test.com', 'Neo', '2023-01-01 00:00:00-00',
 '$s0$e0801$+PwRvk1FPEsJuSV0W731Qg==$ghh4S/ZVPATwzasot3PRIQaRg3Dzk+/e5vgDUAWhM1Q=');
