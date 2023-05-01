CREATE TABLE SETTINGS (
    ID                  bigint(20)          unsigned     	NOT NULL    auto_increment,
    S_NAME				varchar(32)							NOT NULL,
    S_VALUE				varchar(1024)						NOT NULL,
    PRIMARY KEY (ID),
    KEY (S_NAME)
) ENGINE=InnoDB CHARACTER SET utf8;       
        
