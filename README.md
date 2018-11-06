# ClientSPV
Client Java apelare servicii web Spatiul Privat Virtual - acesta este un exemplu de client pentru serviciile web


Prezentare servicii web Spatiul privat virtual

1. Obtinere lista mesaje disponibile
parametru: numarul de zile de cand sunt disponibile mesajele
exemplu apel: https://webserviced.anaf.ro/SPVWS2/rest/listaMesaje?zile=50
(mesajele intrate in ultimele 50 zile, pentru toate persoanele fizice sau juridice pentru care are drepturi posesorul certificatului digital calificat cu care se face autentificarea)
exemplu raspuns cu mesaje:
{"titlu":"Lista Mesaje disponibile din ultimele 50 zile","mesaje":[{"id":"100000000","detalii":"recipisa pentru CIF 8000000000, tip D112, numar_inregistrare INTERNT-130000000-2017\/20-12-2017, perioada raportare 11.2017","cif":"8000000000","data_creare":"20.12.2017 12:00:00","id_solicitare":null,"tip":"RECIPISA"}],"cnp":"1111111111118","cui":"8000000000,8000000001,8000000002","serial":"xxxxxxxxxxxxxxxxxxx"}
unde:
titlu= ce reprezinta raspunsul
cnp = posesorul certificatului cu care se face autentificarea
cui= lista de cui-uri sau cnp-uri pentru care posesorul certificatului are drepturi
serial= SN-ul certificatului cu care s-a facut autentificatea
mesaje= lista de mesaje disponibile
	pentru fiecare mesaj:
	id= indexul de descarcare al mesajului
	detalii = descrierea mesajului
	cif = cui-ul sau cnp-ul pentru care este mesajului
	data_creare= data la care mesajul a ajuns in SPVWS2/rest/listaMesaje
	id_solicitare=indexul solicitarii la care s-a raspuns (daca este cazul)
	tip = tipul mesajului

exemplu raspuns cu eroare:
{"titlu":"Lista Mesaje","eroare":"Nu exista mesaje in ultimele 5 zile"}
unde:
eroare = eroarea aparuta sau motivul pentru care nu se intorc mesaje

2. Descarcare mesaj
parametru: index de descarcare (id mesaj de la metoda anterioara)
exemplu apel: https://webserviced.anaf.ro/SPVWS2/rest/descarcare?id=100000000
intoarce un pdf = mesajul din SPV sau o eroare de genul:
{"titlu":"Descarcare mesaj 100000000","eroare":"Nu aveti dreptul sa descarcati acest mesaj"}

3. Solicitare informarii/ eliberare documente

Lista rapoartelor si documentelor care pot fi solicitate prin SPV:

SOLICITARE				EXPLICATIE															
						
D112Contrib				Informatii privind contributiile sociale conform datelor											
						declarate de angajatori in declaratia D112																
						
Obligatii de plata		Situaţia obligaţiilor fiscale de plată neachitate la sfarsitul 		
						lunii anterioara
						
Nota obligatiilor de 	Cu aceasta nota va puteti prezenta la ghiseele trezoreriei pentru	
plata					achitarea obligatiilor sau puteti folosi informatiile pentru a
						efectua plata prin mijloace de plata la distanta (online banking)		
						
Istoric Spatiu			Istoric activitati in cadrul spatiului privat virtual - modificari	
Virtual					profil, descarcari

Registru				Istoric activitati in cadrul spatiului privat virtual- intrari si  	
intrari-iesiri			iesiri de documente

Bilant anual			Situatii financiare anuale	Luna se alege automat 					

D300					Declaratia 300 - Decont de taxa pe valoare adaugata.				
						Include si declaratia 305	
						
Istoric declaratii		Declaratiile care fac obiectul raportului:D100,D101,D102,D103,		
						D112,D120,D130,D300,D301,D390,D394,D710,D205. Sunt afisate 
						declaratiile valide depuse pentru perioadele de raportare aferente
						anului selectat (lunare, trimestriale, semestriale sau anuale)				
							
D390					Declaratie recapitulativa privind livrarile / achizitiile			
						intracomunitare de bunuri	
						
D100					Declaratie privind obligatiile de plata la bugetul de stat			
						Include declaratiile D100 si D710 valide pentru un anumit
						CUI si perioada	
	
Bilant semestrial		Rapoarte financiare semestriale 										

Istoric bilant			Istoricul situatiilor financiare depus si a raportarilor 			
						semestriale (ultimele valide depuse)	
						
D205					Declaratie  informativa privind impozitul retinut la sursa si		
						castigurile/pierderile realizate, pe beneficiari de venit
						
D120					Decont privind accizele												

D101					Declaratie privind impozitul pe profit								

D130					Decont privind impozitul la titeiul din productia interna			

D112					Declaratia privind obligatiile de plata a contributiilor sociale,	
						impozitului pe venit si evidenta nominala a persoanelor asigurate	
						
DATE IDENTIFICARE		Informatiile privind datele de identificare ale persoanei 			
						juridice. Sunt informatiile existente in baza de date ANAF la
						data generarii raspunsului	
						
VECTOR FISCAL			Informatiile persoanei juridice din vectorul fiscal	Sunt 			
						informatiile existente in baza de date ANAF la data generarii 
						raspunsului	
						
Situatie Sintetica		Informatii privind situatia debitelor persoanei juridice			
						Raportul este generat pana pe 10 ale lunii, pentru luna anterioara	
						
D208					Declaratie informativa privind impozitul pe veniturile din			
						transferul proprietatilor imobiliare din patrimoniul personal
						Declaratia este semestriala - se alege luna 6 pentru semestrul 1 
						si luna 12 pentru semestrul 2
						
D301					Decont special de taxa pe valoarea adaugata							


InterogariBanci			Situatia interogarilor efectuate de banci la ANAF privind 			
						veniturile persoanei fizice	
						
Fisa Rol				Fisa pe platitor, generata din informatiile existente in baza		
						locala a administratiei financiare unde este arondat cui-ul		
						
D394					Declaratie informativa privind livrarile/ prestarile si				
						achizitiile efectuate pe teritoriul national
						
D392					Declaratie informativa privind livrarile de bunuri si				
						prestarile de servicii	
						
D393					Declaratie informativa privind veniturile obtinute din 				
						vanzarea de bilete pentru transportul rutier international
						de persoane	
						
D180					Nota de certificare. Se intocemeste de catre consultantii 			
						fiscali activi pentru contribuabilii care opteaza pentru 
						certificarea de catre un consultant fiscal a declaratiilor
						fiscale	
						
D311					Declaratie privind taxa pe valoare adaugata colectata,				
						datorata de catre persoanele impozibile a caror cod de 
						inregistrare in scopuri de taxa pe valoare adaugata a fost anulat		
						
D106					Declaratie informativa privind dividentele cuvenite actionarilor	

Duplicat Recipisa		Duplicat dupa recipisa declaratiilor care se depun electronic.		
						Este un raport generat la data solicitarii, nu este recipisa
						originala primita la depunere	
						
Adeverinte Venit		Adeverinta de venit pentru persoana fizica							

D212       				Duplicat dupa ultimele declaratii unice persoane fizice depuse		
						(pe capitole - ultimele rectificari, daca este cazul)		

In acest moment mai ramane de implementat cererea de certificat de atestare fiscala (CAF) care prezinta unele dificultati si va fi introdusa ulterior.
						
Lista motivelor acceptate pentru adeverinta de venit (se verifica ca textul sa fie exact la fel):
Sanatate
Cresa
Gradinita
Scoala
Liceu
Facultate
Alocatia pentru copiii nou nascuti
Trusou nou nascuti
Alocatia de stat pentru copii
Indemnizatie ajutor stimulent pentru cresterea copilului
Sprijin financiar acordat la constituirea familiei
Alocatia pentru sustinerea familiei
Alocatia familiala complementara
Somaj si stimularea fortei de munca
Ajutor social
Pensie
Stimulent de insertie
Ajutoare pentru incalzirea locuintei
Ajutoare financiare pentru persoane aflate in extrema dificultate
Cheltuieli cu inmormantarea persoanelor din familiile beneficiare de ajutor social
Ajutoare de urgenta in caz de calamitati naturale
Indemnizatia Bugetul personal complementar pentru persoana cu handicap
Alocatia de plasament
Indemnizatia pentru insotitor
Alocatia lunara de hrana pentru copiii cu handicap de tip HIV SIDA
Ajutor anual pentru veteranii de razboi
Institutie financiar bancara asigurare etc.
Executor judecatoresc
Autoritati straine
Altele					
						

URL apel: 
https://webserviced.anaf.ro/SPVWS2/rest/cerere

Parametri:
tip = tipul solicitarii
cui = cif-ul pentru care se face solicitarea
an = anul
luna = luna
motiv = motivul solicitarii (doar la adeverinta de venit)
numar_inregistrare = numarul de inregistrare al formularului (doar la duplicat recipisa)
cui_pui = cui-ul punctului de lucru/ sucursalei/ etc (doar la fisa rol)		
				
Exemplu de apeluri:
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Istoric%20bilant&cui=8000000000
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D112Contrib&cui=1111111111118
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=DATE%20IDENTIFICARE&cui=8000000000
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=VECTOR%20FISCAL&cui=8000000000
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Situatie%20Sintetica&cui=8000000000
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Obligatii%20de%20plata&cui=1111111111118
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Nota%20obligatiilor%20de%20plata&cui=1111111111118
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Istoric%20Spatiu%20Virtual&cui=1111111111118
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=InterogariBanci&cui=1111111111118
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Registru%20intrari-iesiri&cui=1111111111118
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Registru%20intrari-iesiri&cui=8000000000
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Fisa%20Rol&cui=8000000000
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Fisa%20Rol&cui=8000000000&cui_pui=8000000001
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Bilant%20anual&cui=35045838&an=2018
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Istoric%20declaratii&cui=8000000000&an=2018
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D205&cui=8000000000&an=2017
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D120&cui=8000000000&an=2017
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D130&cui=8000000000&an=2017
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D101&cui=8000000000&an=2017
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D392&cui=8000000000&an=2015
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D393&cui=8000000000&an=2015
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D106&cui=8000000000&an=2017
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Bilant%20semestrial&cui=8000000000&an=2017
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Duplicat%20Recipisa&cui=8000000000&numar_inregistrare=INTERNT-140000000-2018
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Adeverinte%20Venit&cui=1111111111118&an=2017&motiv=altele
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D300&cui=8000000000&an=2018&luna=1
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D390&cui=8000000000&an=2018&luna=1
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D100&cui=8000000000&an=2018&luna=1
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D112&cui=8000000000&an=2018&luna=1
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D208&cui=8000000000&an=2018&luna=6
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D394&cui=8000000000&an=2018&luna=6
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D301&cui=8000000000&an=2018&luna=6
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D180&cui=8000000000&an=2018&luna=6
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D311&cui=8000000000&an=2018&luna=6
https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=D212&cui=1111111111118&an=2018



Raspuns:
corect - se intoarce id_solicitare care va fi prezent si in listaMesaje
{id_solicitare:260149,parametri:an=2017, cui=8000000000,serial:20A0506B2450015C39C,cnp:1111111111118,titlu:Transmitere cerere tip D101}
eroare drept
{eroare:Nu veti drept sa solictati informatii despre CIF=8000000000,titlu:Cerere}
alte erori de validare
{eroare:CUI-ul introdus= 8000000001 nu este corect. ,titlu:Cerere}
{eroare:Pentru tip raport= D101 parametri cui si an sunt obligatorii,titlu:Cerere}
{eroare:Tip raport= CAF inca nu poate fi solicitat prin WS,titlu:Cerere}
erori tehnice
{eroare:Eroare transmitere cerere. Cod 057,titlu:Cerere}
(in acest caz folositi formularul de contact sau adresa de mail spv.webservice@mfinante.ro pentru a semnala eroarea tehnica, cu precizarea codului de eroare obtinut si, daca este posibil, a apelului efectuat)

06.11.2018 - modificari la WS
1. corectie data creare - se afiseaza si ora, minut, secunda
2. corectie descarcare rapoarte Anliza de risc - se intorcea null pentruu aceasta categorie
3. adaugat parametrul cif la listaMesaje, pentru a obtine doar mesajele pentru un anumit CUI/ CNP - exemplu:
https://webserviced.anaf.ro/SPVWS2/rest/listaMesaje?zile=50&cif=8000000000

Pentru viitor se are in vedere devoltarea de servicii web pentru autentificare cu user si parola (persoane fizice in nume propriu) si adaugarea de noi metode: transmitere declaratii, solicitare CAF, transmitere sesizari prin formularul de contact, modificari profil, etc.

Ne puteti contacta la adresa de mail 
spv.webservice@mfinante.ro

