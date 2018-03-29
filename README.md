# ClientSPV
Client Java apelare servicii web Spatiul Privat Virtual - acesta este un exemplu de client pentru serviciile web


Prezentare servicii web Spatiul privat virtual

1. Obtinere lista mesaje disponibile
- parametru: numarul de zile de cand sunt disponibile mesajele
- exemplu apel: https://webserviced.anaf.ro/SPVWS2/rest/listaMesaje?zile=50 (mesajele intrate in ultimele 50 zile, pentru toate persoanele fizice sau juridice pentru care are drepturi posesorul certificatului digital calificat cu care se face autentificarea)
- exemplu raspuns cu mesaje:
```{"titlu":"Lista Mesaje disponibile din ultimele 50 zile","mesaje":[{"id":"100000000","detalii":"recipisa pentru CIF 8000000000, tip D112, numar_inregistrare INTERNT-130000000-2017\/20-12-2017, perioada raportare 11.2017","cif":"8000000000","data_creare":"20.12.2017 12:00:00","id_solicitare":null,"tip":"RECIPISA"}],"cnp":"1111111111118","cui":"8000000000,8000000001,8000000002","serial":"xxxxxxxxxxxxxxxxxxx"}```
unde:
```titlu= ce reprezinta raspunsul
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
```

- exemplu raspuns cu eroare:
```{"titlu":"Lista Mesaje","eroare":"Nu exista mesaje in ultimele 5 zile"}```
unde:
```eroare = eroarea aparuta sau motivul pentru care nu se intorc mesaje```

2. Descarcare mesaj
- parametru: index de descarcare (id mesaj de la metoda anterioara)
- exemplu apel: https://webserviced.anaf.ro/SPVWS2/rest/descarcare?id=100000000
	- intoarce un pdf = mesajul din SPV sau o eroare de tipul:
```{"titlu":"Descarcare mesaj 100000000","eroare":"Nu aveti dreptul sa descarcati acest mesaj"}```

In masura in care aceste servicii vor fi folosite de un numar semnificativ de persoane, vor fi adaugate metode noi (de exemplu solicitare informatii si solicitare eliberare documente)
