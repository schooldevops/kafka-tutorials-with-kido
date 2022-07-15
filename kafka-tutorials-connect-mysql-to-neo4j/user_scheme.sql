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

-- Role 정보
CREATE TABLE `role` (
    `id` bigint(20) NOT NULL,
    `role_name` varchar(100) NOT NULL COMMENT '롤 이름',
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

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

-- 정책정보
CREATE TABLE `policy` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL COMMENT '정책이름',
    `target` varchar(100) NOT NULL COMMENT '정책대상',
    `permission` varchar(20) NOT NULL COMMENT '정책퍼미션',
    `created_date` timestamp NULL DEFAULT NULL COMMENT '생성일시',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

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
