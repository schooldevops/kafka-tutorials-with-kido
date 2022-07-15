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
INSERT INTO role (id, role_name, created_date) VALUES
(1, 'Super', now()),
(2, 'Admin', now()),
(3, 'Developer', now()),
(4, 'PM', now()),
(5, 'Designer', now());

-- user_role 정보
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
INSERT INTO policy (id, name, target, permission, created_date) VALUES
(1, 'FullAccess', 'Session', 'FA', now()),
(2, 'READ/WRITE', 'Session', 'RW', now()),
(3, 'READ', 'Session', 'R', now());

-- 롤 정책정보
INSERT INTO role_policy (role_id, policy_id, created_date) VALUES
(1, 1, now()),
(2, 1, now()),
(3, 2, now()),
(4, 2, now()),
(5, 3, now());

-- 사용자 정책정보
INSERT INTO user_policy (user_id, policy_id, created_date) VALUES
(1, 1, now()),
(2, 2, now()),
(3, 1, now());

-- 그룹 정책정보
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

