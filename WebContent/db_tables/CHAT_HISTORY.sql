CREATE TABLE CHAT_HISTORY (
    ID                  bigint(20)           unsigned     	NOT NULL    auto_increment,
    TIME           		BIGINT                          	NOT NULL,
   	SENDER_TYPE			ENUM("kSingleUser","kGroupUser" )	NOT NULL,
   	SENDER_NAME			varchar(64)							NOT NULL,
   	SENDER_ID			char(36)                          	NOT NULL,
   	
   	RECEIVER_TYPE		ENUM("kSingleUser","kGroupUser" )	NOT NULL,
   	RECEIVER_NAME		varchar(64)							NOT NULL,
   	RECEIVER_ID			char(36)                          	NOT NULL,
   	
   	DELETED				tinyint(1)							NOT NULL DEFAULT 0,
   	DENOUNCED			tinyint(1)							NOT NULL DEFAULT 0,
   	REACTIONS			BIGINT,
   	
    CONTENT				varchar(4096)						NOT NULL,
    
    PRIMARY KEY (ID),
    KEY (SENDER_ID),
    KEY (RECEIVER_ID)
) ENGINE=InnoDB CHARACTER SET utf8;