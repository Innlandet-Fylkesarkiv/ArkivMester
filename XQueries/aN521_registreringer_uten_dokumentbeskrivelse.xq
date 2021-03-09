xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;

(:Registrering uten hoveddokument (ACOS/Documaster). Dekker også registreringer uten dokumentbeskrivelse
  Spørringen brukes i arbeidsflyt ved IKA Opplandene og skriver resultatet til fil.
:)

//registrering[not (dokumentbeskrivelse) and not (../saksstatus = 'Utgår') and not (journalstatus = 'Utgår')]/file:append-text-lines("./repository_operations/baseX/n5_21.txt", concat('<tr><td>',systemID, '</td><td>', registreringsID, '</td><td>', journalstatus, '</td></tr>'))
