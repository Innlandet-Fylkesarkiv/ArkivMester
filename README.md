<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://bitbucket.org/magnsus/bachelorinnlandet/">
    <img src="src/main/resources/appicon.png" alt="Logo" width="80" height="80">
  </a>
</p>
<h3 align="center">ArkivMester</h3>

  <p align="center">
    Bachelor oppgave 2021 ved NTNU Gjøvik 
    <br />
    Innlandet Fylkesarkiv/ IKA Opplandene
</p>


<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Innhold</summary>
  <ol>
    <li>
      <a href="#om-prosjektet">Om prosjektet</a>
      <ul>
        <li><a href="#utviklet-med">Utviklet med</a></li>
      </ul>
    </li>
    <li>
      <a href="#komme-i-gang">Komme i gang</a>
      <ul>
        <li><a href="#forutsetninger">Forutsetninger</a></li>
        <li><a href="#installasjon">Installasjon</a></li>
      </ul>
    </li>
    <li><a href="#bruk">Bruk</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## Om prosjektet

Fylkesarkivet i Innlandet holder til på fakkelgården i Lillehammer og er ett av flere fylkesarkiv i Norge. Fylkesarkivet er pålagt å ivareta, sikre og formidle sine arkiver. Arbeidet sikrer kunnskap som kan komme til nytte for privatpersoner, organisasjoner, forskere og andre som søker historiske opplysninger.
Det er derfor store mengder innkommende uttrekk som må bli testet og validert, noe som manuelt kan ta flere måneder for å fullføre bare ett uttrekk. Applikasjonen sin jobb er å automatisere testingen, valideringen og skriving av rapporten for testene.

### Utviklet med

Utviklingsmiljøet som også kompilerte _.java_ filene til _.class_ filer og bygget disse til en kjørbar _.jar_ fil.
* [Intellij IDEA](https://www.jetbrains.com/idea/)

De fire bibliotekene/rammeverkene som blir brukt.

* [Apache Poi](https://poi.apache.org/)
* [JSoup](https://jsoup.org/)
* [docx4j](https://www.docx4java.org/trac/docx4j)
* [JUnit 5](https://junit.org/junit5/)



<!-- GETTING STARTED -->
## Komme i gang

Disse instruksjonene er ment for å kunne kjøre applikasjonen og starte testing av uttrekk. For å forandre på koden, må du bruke _Intellij IDEA_ som kompilator
og byggsystem for å bygge en ny _.jar_ kjørbar fil.

### Forutsetninger

Listen inneholder kjøremiljøet for applikasjonen og dens nødvendige tredjeparts verktøy som må være installert.

* [OpenJDK15](https://openjdk.java.net/projects/jdk/15/)
* [Arkade5 CLI 2.2.1](https://arkade.arkivverket.no/)
* [DROID 6.5](https://www.nationalarchives.gov.uk/information-management/manage-information/preserving-digital-records/droid/)
* [KOST-Val 2.0.4](https://github.com/KOST-CECO/KOST-Val)
* [VeraPDF 1.16.1 Greenfield](https://verapdf.org/)
* [BaseX 9.5](https://basex.org/)
* [7-Zip 19.0](https://www.7-zip.org/)

### Installasjon

1. Sørg for at alle nødvendige verktøy er til stede på maskinen.
2. Klon eller last ned repoet for å få _.jar_ and _XQueries_ katalogen.
   ```sh
   git clone https://bitbucket.org/magnsus/bachelorinnlandet.git
   ```
3. Kjør ArkivMester.jar via terminalen. Sørg for at terminalen enten står i _..\openjdk-15\bin_ eller at den er lagret i _Path_ miljøvariabelen OG at _.jar_ filen er tilstede der du står.
   ```sh
   java -jar ArkivMester.jar
   ```
4. Åpne innstillinger og sørg for at fillokasjonene stemmer med hvor du har plassert de nødvendige verktøyene.
5. Kopier _.xq_ filene fra _XQueries_ katalogen til der _xqueryExtFolder_ katalogen er definert i innstillinger. Til standard er dette _E:\XQuery-Statements_.
6. Lag en katalog for egendefinerte XQueries der den er definert i instillinger. Til standard er dette _E:\XQuery-Statements/Egendefinerte_. Plasser aktuelle egendefinerte XQueries her.
7. Nå er ArkivMester ferdig konfigurert og klar til bruk.

<!-- USAGE EXAMPLES -->
## Bruk

Last opp pakket uttrekk ved å klikke på "Last opp pakket uttrekk" knappen. Velg hvilke deltester som skal være med ved å klikke på "Velg tester" knappen og aktiver deltestene.
Start uttrekkstesten ved å klikke på "Start test" knappen, du vil da se en oversikt over hvilke deltester som venter, kjøres og er deaktiverte. Når testen er fullført aktiveres "Lag rapport" knappen som vil generere
sluttrapporten for testen, deretter vil "Pakk til AIP" knappen bli tilgjengelig som vil pakke uttrekket med delresultatene og rapporten til en AIP.


Definisjonen av et gyldig uttrekk er en katalog som inneholder _.tar_ og en medfølgende metadatafil i _.xml_ format.
For å spesifisere hvilke dokumenter du skal gjøre spørringer imot i de egendefinerte XQuery spørringene bruk:
   ```sh
   db:open("arkivmester", "arkivstruktur.xml")/
   ```
Denne kommandoen vil hente og velge rot noden i dokumentet _arkivstruktur.xml_ og du vil ha tilgang til hele treet som vanlig.

For å kjøre _unit testene_ til applikasjonen kan du bruke _Intellij IDEA_ som utviklingsmiljø til å kjøre testene.

_For en fullverdig dokumentasjon, se [JavaDoc](https://bitbucket.org/magnsus/bachelorinnlandet/src/master/javadoc/) og [Brukermanualen](https://example.com) i rapporten vedlegg B_


