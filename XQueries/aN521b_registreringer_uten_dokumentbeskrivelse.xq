xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;


(:Registrering uten hoveddokument (ACOS/Documaster). Dekker også registreringer uten dokumentbeskrivelse:)

for $r in (//registrering[
                          not (../saksstatus = 'Utgår') and 
                          not (journalstatus = 'Utgår')]) 
    return concat('<tr><td>',$r/systemID, '</td><td>', $r/registreringsID, '</td><td>', $r/journalstatus, '</td></tr>')