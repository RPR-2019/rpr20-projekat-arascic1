BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "USERS" (
	"usernameHash"	TEXT,
	"passHash"	TEXT,
	"manager"	INTEGER,
	PRIMARY KEY("usernameHash")
);
CREATE TABLE IF NOT EXISTS "businesses" (
	"Ime"	TEXT NOT NULL,
	"Adresa"	TEXT NOT NULL,
	"brTelefona"	TEXT,
	"slikaURL"	TEXT,
	"posljednjaPosjeta"	TEXT,
	"narednaPosjeta"	TEXT,
	PRIMARY KEY("Ime")
);
INSERT INTO "USERS" VALUES ('0a041b9462caa4a31bac3567e0b6e6fd9100787db2ab433d96f6d178cabfce90','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',0);
INSERT INTO "USERS" VALUES ('6025d18fe48abd45168528f18a82e265dd98d421a7084aa09f61b341703901a3','15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225',0);
INSERT INTO "USERS" VALUES ('5860faf02b6bc6222ba5aca523560f0e364ccd8b67bee486fe8bf7c01d492ccb','65e84be33532fb784c48129675f9eff3a682b27168c0ea744b2cf58ee02337c5',1);
INSERT INTO "businesses" VALUES ('Hotel Grad','Safeta Hadžića 19','+38733848088','https://lh5.googleusercontent.com/p/AF1QipMh6wNW7CdlJZ9pCozjTJYBoPtgq4376TxsQz15=w408-h271-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Koncept Residence Hotel','Briješćanska 23','+38733257257','https://lh6.googleusercontent.com/proxy/m4qwgBnPTHoTKklNkQVNN6qrTrrtGnnL60Fnej5M0AOkSq5uMwAKTMictbMf0fqbssT7hWBSU58itIM36yh7asDgy3bOViMmDToQGCrH_uNfDk2lNAdaGUAIQdZ0OTad_JqjpmzDjpsS-ZzyAjiPrTu4noUK_w=w408-h612-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Hotel ibis Styles Sarajevo','Dzemala Bijedica St 169 A','+38733483900','https://lh4.googleusercontent.com/proxy/xudV9rKrsvOTn6I97GshwyDfpL-GBgw-ugBEnAl27uxWxg40zW__DchOqe9hllOS4S8qefx4qajdk19XmTJyLpsb-wFA7K_jO-OjeZApEeM1C6CdyXWlJ1HYIezofvnv6S3OENzm9-CU9cDD_Hx1--JJblXVcA=w408-h306-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Hotel Ada','Radenka Abazovića 2','+38733473971','https://lh3.googleusercontent.com/proxy/_Y1xcMmbetym3xXDm__vtRHhuXrdijGz86tQ6RWLcQjJs9ArzivIhv15pjuvaBVDgVWhLdhDmLpS-Rs4Rp2p5ikpBzci52Y9baaqiRS4TrA4YF-55du7dHMaXNsxeKF3-JWZ2gsvlF1Nv1sLJCMo8CSBVFrJZA=w596-h240-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Hotel Radon Plaza','Dzemala Bijedica 185','+38733752900','https://lh5.googleusercontent.com/p/AF1QipM8GWh2SKs8-HvHYcVYK3WEyPUrnyL9Q60IW3qz=w408-h272-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Cafe Salon','Malta 25','+38765532607','https://lh5.googleusercontent.com/p/AF1QipMAr8xKyna6R9_uuPIP9ZI3pyeb8YcI1UWzcGC_=w408-h725-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Café Twins','Trg Barcelone',NULL,'https://lh5.googleusercontent.com/p/AF1QipOBtgVLHl7jWfHugpb5FCWxQx9wzceIS-ZBJDhs=w408-h544-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Cafe Ambasador','Dzemala Bijedica 117','+38733677622','https://lh5.googleusercontent.com/p/AF1QipM4Kh7ZBIVyOKiFVcwPwAvHYaBgXd3b14BK0NvE=w408-h544-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Caffe Bar Te Amo','Dzemala Bijedica 129','+38761867192','https://lh5.googleusercontent.com/p/AF1QipNbS7iDvjM_xRWKbywEuPydszwkesQEFefauh1a=w408-h725-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Viennese Cafe','Vladislava Skarića 5','+38733580570','https://lh5.googleusercontent.com/p/AF1QipMWGjI6EZ47PkV1UOrITppQAxvAq44CLWnLbLIP=w408-h305-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Restoran REUFF','Aleja Bosne Srebrene 24','+38733545534','https://lh5.googleusercontent.com/p/AF1QipO71XsOCvo0IpcMITbdOR2UB1LrkRZ1tPpfSQDo=w426-h240-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Restoran Sjaj','Trebinjska 9','+38733617687','https://lh5.googleusercontent.com/p/AF1QipPTbz9jPKYoEURwMYwDsP9aXGqZrUEt_JkqtRHa=w408-h306-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Restoran Baltazar Doner','Dzemala Bijedica 114','+38762907909','https://lh5.googleusercontent.com/p/AF1QipNSiQ3WQx7B_eAT2AkdgMJqGb3XJ8Vf2S11RNqf=w408-h408-k-no',NULL,NULL);
INSERT INTO "businesses" VALUES ('Capital Bars & Restaurants','Dzemala Bijedica 39','+38761768844','https://www.google.com/maps/place/Capital+Bars+%26+Restaurants/@43.8497188,18.3676118,3a,75y,90t/data=!3m8!1e2!3m6!1sAF1QipM0XFyk3nOKqZBC5RUcjC8EILrYO58EqGzhxrgO!2e10!3e12!6shttps:%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipM0XFyk3nOKqZBC5RUcjC8EILrYO58EqGzhxrgO%3Dw203-h152-k-no!7i4624!8i3472!4m5!3m4!1s0x4758c96946059eb7:0xcb7f2b182169a570!8m2!3d43.8497625!4d18.3672315?authuser=0&hl=en#',NULL,NULL);
INSERT INTO "businesses" VALUES ('Fish Restaurant Tisina','Dzemala Bijedica 37','+38733677224','https://lh5.googleusercontent.com/p/AF1QipOdUcB6ydawlVKWlIHnKj7bIqyL3gxWl22qvqUq=w408-h306-k-no',NULL,NULL);
COMMIT;
