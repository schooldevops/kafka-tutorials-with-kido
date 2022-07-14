DROP TABLE IF EXISTS `group_policy`;
DROP TABLE IF EXISTS `user_policy`;
DROP TABLE IF EXISTS `role_policy`;
DROP TABLE IF EXISTS `policy`;
DROP TABLE IF EXISTS `group_role`;
DROP TABLE IF EXISTS `user_role`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `group_user`;
DROP TABLE IF EXISTS `group`;
DROP TABLE IF EXISTS `user`;


-- 사용자 정보
CREATE TABLE `user` (
    `id` bigint(20) NOT NULL,
    `empno` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '사원번호',
    `login` varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT '로그인아이디',
    `password` varchar(60) COLLATE utf8_unicode_ci NOT NULL COMMENT '비밀번호 해시',
    `first_name` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '이름',
    `last_name` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '성',
    `email` varchar(191) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '메일',
    `activated` bit(1) NOT NULL COMMENT '활성여부',
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_login` (`login`),
    UNIQUE KEY `user_empno` (`empno`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- user 정보 입력
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (1, '10000', 'kido', 'kido123', '기도', '배', 'kido@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (2, '10001', 'mario', '123', '리오', '마', 'mario@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (3, '10002', 'jone', '123', '존', '존', 'jone@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (4, '10003', 'ksoo', '123', '수', '김', 'ksoo@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (5, '10004', 'doctor.s', '123', '닥터', '스트레인지', 'doctor.s@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (6, '10005', 'hulk', '123', '헐크', '김', 'hulk@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (7, '10006', 'batman', '123', '배트', '맨', 'batman@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (8, '10007', 'torr', '123', '토루', '마', 'torr@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (9, '10008', 'odine', '123', '딘', '오', 'odine@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (10, '10009', 'spider.m', '123', '파이더', '스', 'spider@gail.com', 1, now());
INSERT INTO `user`(id, empno, login, password, first_name, last_name, email, activated, created_date) VALUE (11, '10010', 'momo', '123', '모', '모', 'momo@gail.com', 1, now());

-- 그룹 정보
CREATE TABLE `group` (
    `id` bigint(20) NOT NULL,
    `groupno` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '그룹번호',
    `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT '그룹이름',
    `group_email` varchar(191) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '그룹메일',
    `activated` bit(1) NOT NULL COMMENT '활성여부',
    `created_by` varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT '생성자',
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    `parent_group_id` bigint(20) DEFAULT NULL COMMENT '부모그룹아이디',
    PRIMARY KEY (`id`),
    UNIQUE KEY `group_no` (`groupno`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- group 정보 입력
INSERT INTO `group` (id, groupno, name, group_email, activated, created_by, created_date, parent_group_id) VALUES
(1, '00001', 'CEO', 'ceo@cc.com', 1, 0, now(), -1),
(2, '00002', 'HR', 'hr@cc.com', 1, 0, now(), 1),
(3, '00003', 'DEV_OFFICE', 'dev1@cc.com', 1, 0, now(), 1),
(4, '00004', 'DEV1', 'dev1@cc.com', 1, 0, now(), 3),
(5, '00005', 'DEV2', 'dev2@cc.com', 1, 0, now(), 3),
(6, '00006', 'PMO', 'pmo@cc.com', 1, 0, now(), 1),
(7, '00007', 'PM1', 'pm1@cc.com', 1, 0, now(), 6),
(8, '00008', 'PM2', 'pm2@cc.com', 1, 0, now(), 6),
(9, '00009', 'DESIGN', 'design@cc.com', 1, 0, now(), 1),
(10, '00010', 'DESIGN1', 'design1@cc.com', 1, 0, now(), 9);

-- 그룹 사용자 정보
CREATE TABLE `group_user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `group_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`group_id`, `user_id`),
    CONSTRAINT `group_user_FK1` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`),
    CONSTRAINT `group_user_FK2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO group_user (group_id, user_id, created_date) VALUES
(1, 1, now()),
(2, 2, now()),
(3, 3, now()),
(4, 4, now()),
(5, 5, now()),
(6, 6, now()),
(7, 7, now()),
(8, 8, now()),
(9, 9, now()),
(10, 10, now());

-- Role 정보
CREATE TABLE `role` (
    `id` bigint(20) NOT NULL,
    `role_name` varchar(100) NOT NULL COMMENT '롤 이름',
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO role (id, role_name, created_date) VALUES
(1, 'Super', now()),
(2, 'Admin', now()),
(3, 'Developer', now()),
(4, 'PM', now()),
(5, 'Designer', now());

-- user_role 정보
CREATE TABLE `user_role` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL,
    `role_id` bigint(20) NOT NULL,
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`user_id`, `role_id`),
    CONSTRAINT `user_role_FK1` FOREIGN KEY (`user_id`) REFERENCES `group` (`id`),
    CONSTRAINT `user_role_FK2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO user_role (user_id, role_id, created_date) VALUES
(1, 1, now()),
(2, 2, now()),
(3, 3, now()),
(4, 3, now()),
(5, 3, now()),
(6, 4, now()),
(7, 4, now()),
(8, 4, now()),
(9, 5, now()),
(10, 5, now());

-- group_role 정보
CREATE TABLE `group_role` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `group_id` bigint(20) NOT NULL,
    `role_id` bigint(20) NOT NULL,
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`group_id`, `role_id`),
    CONSTRAINT `group_role_FK1` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`),
    CONSTRAINT `group_role_FK2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO group_role (group_id, role_id, created_date) VALUES
(1, 1, now()),
(2, 2, now()),
(3, 3, now()),
(4, 3, now()),
(5, 3, now()),
(6, 4, now()),
(7, 4, now()),
(8, 4, now()),
(9, 5, now()),
(10, 5, now());

-- 정책정보
CREATE TABLE `policy` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL COMMENT '정책이름',
    `target` varchar(100) NOT NULL COMMENT '정책대상',
    `permission` varchar(20) NOT NULL COMMENT '정책퍼미션',
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO policy (id, name, target, permission, created_date) VALUES
(1, 'FullAccess', 'Session', 'FA', now()),
(2, 'READ/WRITE', 'Session', 'RW', now()),
(3, 'READ', 'Session', 'R', now());

-- 롤 정책정보
CREATE TABLE `role_policy` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `role_id` bigint(20) NOT NULL,
    `policy_id` bigint(20) NOT NULL,
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`role_id`, `policy_id`),
    CONSTRAINT `role_policy_FK1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
    CONSTRAINT `role_policy_FK2` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO role_policy (role_id, policy_id, created_date) VALUES
(1, 1, now()),
(2, 1, now()),
(3, 2, now()),
(4, 2, now()),
(5, 3, now());

-- 사용자 정책정보
CREATE TABLE `user_policy` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL,
    `policy_id` bigint(20) NOT NULL,
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`user_id`, `policy_id`),
    CONSTRAINT `user_policy_FK1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `user_policy_FK2` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO user_policy (user_id, policy_id, created_date) VALUES
(1, 1, now()),
(2, 2, now()),
(3, 1, now());

-- 그룹 정책정보
CREATE TABLE `group_policy` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `group_id` bigint(20) NOT NULL,
    `policy_id` bigint(20) NOT NULL,
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`group_id`, `policy_id`),
    CONSTRAINT `group_policy_FK1` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`),
    CONSTRAINT `group_policy_FK2` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO group_policy (group_id, policy_id, created_date) VALUES
(1, 1, now()),
(2, 2, now()),
(3, 2, now()),
(4, 3, now()),
(5, 3, now()),
(6, 2, now()),
(7, 2, now()),
(8, 3, now()),
(9, 2, now()),
(10, 3, now());

