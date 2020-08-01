--liquibase formatted mqsql
--changeset cq:1

#OAuth 2.0 客户端
CREATE TABLE IF NOT EXISTS `auth`.`oauth_client_details` (
       client_id VARCHAR(256) PRIMARY KEY,
       resource_ids VARCHAR(256),
       client_secret VARCHAR(256),
       scope VARCHAR(256),
       authorized_grant_types VARCHAR(256),
       web_server_redirect_uri VARCHAR(256),
       authorities VARCHAR(256),
       access_token_validity INTEGER,
       refresh_token_validity INTEGER,
       additional_information VARCHAR(4096),
       autoapprove VARCHAR(256)
);
create table if not exists oauth_client_token (
  token_id VARCHAR(255),
  token LONG VARBINARY,
  authentication_id VARCHAR(255) PRIMARY KEY,
  user_name VARCHAR(255),
  client_id VARCHAR(255)
);

#OAuth 2.0 访问令牌
create table if not exists oauth_access_token (
  token_id VARCHAR(255),
  token LONG VARBINARY,
  authentication_id VARCHAR(255) PRIMARY KEY,
  user_name VARCHAR(255),
  client_id VARCHAR(255),
  authentication LONG VARBINARY,
  refresh_token VARCHAR(255)
);

#OAuth 2.0 刷新令牌
create table if not exists oauth_refresh_token (
  token_id VARCHAR(255),
  token LONG VARBINARY,
  authentication LONG VARBINARY
);

#OAuth 2.0 授权码
create table if not exists oauth_code (
  code VARCHAR(255), authentication LONG VARBINARY
);

create table if not exists oauth_approvals (
 userId VARCHAR(255),
 clientId VARCHAR(255),
 scope VARCHAR(255),
 status VARCHAR(10),
 expiresAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 lastModifiedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO oauth_client_details    (client_id, client_secret, scope, authorized_grant_types,    web_server_redirect_uri, authorities, access_token_validity,    refresh_token_validity, additional_information, autoapprove)
VALUES    ('user-client', '$2a$10$o2l5kA7z.Caekp72h5kU7uqdTDrlamLq.57M1F6ulJln9tRtOJufq', 'all',    'authorization_code,refresh_token,password', null, null, 3600, 36000, null, true);
VALUES    ('auth-client', '$2a$10$o2l5kA7z.Caekp72h5kU7uqdTDrlamLq.57M1F6ulJln9tRtOJufq', 'all',    'authorization_code,refresh_token,password,sms', null, null, 3600, 36000, null, true);

CREATE TABLE IF NOT EXISTS `auth`.`user` (
    id BIGINT UNSIGNED NOT NULL COMMENT "uid",
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间戳",
    nickname VARCHAR(32) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL COMMENT "昵称",
    avatar VARCHAR(256) NULL COMMENT "头像",
    phone VARCHAR(13) CHARACTER SET 'utf8' NOT NULL COMMENT "手机号",
    area VARCHAR(4) CHARACTER SET 'utf8' NOT NULL COMMENT "国际区号",
    password VARCHAR(256) CHARACTER SET 'utf8' NOT NULL COMMENT "密码",
    salt VARCHAR(12) CHARACTER SET 'utf8' NULL COMMENT "盐",
    PRIMARY KEY (id),
    UNIQUE key (phone)
)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `auth`.`role` (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL COMMENT "uid",
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间戳",
    name VARCHAR(32) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL COMMENT "角色名称",
    PRIMARY KEY (id)
)ENGINE = InnoDB;

INSERT INTO `role` (`id`,`name`)VALUE ("1","app");

CREATE TABLE IF NOT EXISTS `auth`.`user_role`(
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间戳",
    uid BIGINT UNSIGNED NOT NULL COMMENT "uid",
    rid BIGINT UNSIGNED NOT NULL COMMENT "role_id",
    UNIQUE key (uid)
)ENGINE = InnoDB;

INSERT INTO `user` (`id`, `create_at`, `nickname`, `phone`, `area`, `password`, `salt`)
VALUES ('305703314103734271', '2020-06-17 15:16:11', 'jdzPD', '13320219007', '86', '$2a$10$GjwmPRczd.9CasC0kYTLWO9PyzgqCfc8vFH7MPrLQcj09ba4JUfi6', NULL);

INSERT INTO `user_role` (`uid`,`rid`)VALUE ("305703314103734271","1");



