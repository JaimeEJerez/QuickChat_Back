CREATE TABLE CHAT_GROUPS (
	ID						bigint(20)          	unsigned   	NOT NULL auto_increment,
	GROUP_ID				bigint(20)							NOT NULL,
   
	USER_EMAIL					varchar(36),
		CONSTRAINT fk_CHAT_GROUPS_USERS
			FOREIGN KEY (USER_EMAIL) 
				REFERENCES USERS(EMAIL),
			
   PRIMARY KEY (ID),
   KEY (GROUP_ID)
) ENGINE=InnoDB CHARACTER SET utf8;

INSERT INTO CHAT_GROUPS 
	( GROUP_ID, USER_EMAIL ) 
VALUES 
	( 1, 	"LuisM@quickchat.com" 		),
	( 1, 	"CarlosZ@quickchat.com"  	),
	( 1, 	"PepeH@quickchat.com" 		),
	( 1, 	"ManoloN@quickchat.com"  	),
	( 1, 	"LuizG@quickchat.com"  		),
	( 1, 	"CarlaA@quickchat.com"  	),
	( 1, 	"MartaA@quickchat.com"  	),
	( 1, 	"JaimeJ@quickchat.com"  	),
	( 1, 	"JorgeJ@quickchat.com"  	),
	( 1, 	"NicolasC@quickchat.com"  	),
	( 1, 	"CarlosM@quickchat.com"  	);

