CREATE TABLE REACTIONS (
    ID                  bigint(20)           unsigned     	NOT NULL    auto_increment,
    DATE_TIME           TIMESTAMP                         	NOT NULL    DEFAULT CURRENT_TIMESTAMP,
	MESG_OWNER			varchar(36)                         NOT NULL,    
	WHO_REACTS			varchar(36)							NOT NULL,
    MESSAGE_ID			varchar(35)							NOT NULL,
    REACTION			INT									NOT NULL,
    PRIMARY KEY (ID),
    KEY (MESG_OWNER),
    KEY (WHO_REACTS),
    KEY (MESSAGE_ID)
) ENGINE=InnoDB CHARACTER SET utf8;       
        
  
        