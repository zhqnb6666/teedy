insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('DEFAULT_LANGUAGE', 'eng');
create table T_USER_REGISTRATION ( URG_ID_C varchar(36) not null, URG_USERNAME_C varchar(50) not null, URG_PASSWORD_C varchar(100) not null, URG_EMAIL_C varchar(100) not null, URG_STATUS_C varchar(10) not null, URG_CREATEDATE_D datetime not null, URG_PROCESSDATE_D datetime, URG_DELETEDATE_D datetime, primary key (URG_ID_C) );
update T_CONFIG set CFG_VALUE_C = '16' where CFG_ID_C = 'DB_VERSION';
