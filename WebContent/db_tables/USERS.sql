CREATE TABLE USERS (
    ID                  bigint(20)          unsigned     	NOT NULL    auto_increment,
    TYPE				char(1)								NOT NULL	DEFAULT "U",
    DISPLAY_NAME		varchar(32)							NOT NULL,
    EMAIL              	varchar(36)			UNIQUE			NOT NULL,
    PASSWORD			varchar(36)							NOT NULL,
    PRIMARY KEY (ID),
    KEY (EMAIL)
) ENGINE=InnoDB CHARACTER SET utf8;       
        
  
INSERT INTO USERS 
	( DISPLAY_NAME, EMAIL, PASSWORD ) 
VALUES 
	( "Luis Manuel", 	"LuisM@quickchat.com", 		"123456"  ),
	( "Carlos Zorro", 	"CarlosZ@quickchat.com", 	"123456"  ),
	( "Pepe Homero", 	"PepeH@quickchat.com", 		"123456"  ),
	( "Manolo Nuka", 	"ManoloN@quickchat.com", 	"123456"  ),
	( "Luiz Grrin", 	"LuizG@quickchat.com", 		"123456"  ),
	( "Carla Aangola", 	"CarlaA@quickchat.com", 	"123456"  ),
	( "Marta Perez", 	"MartaA@quickchat.com", 	"123456"  ),
	( "Jaime Jerez", 	"JaimeJ@quickchat.com", 	"123456"  ),
	( "Jorge Jerez", 	"JorgeJ@quickchat.com", 	"123456"  ),
	( "Nicolas Correa", "NicolasC@quickchat.com", 	"123456"  ),
	( "Carlos Marquez",	"CarlosM@quickchat.com", 	"123456"  );
	   