xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;

(:
Dokumentbeskrivelser uten dokumentobjekter. 
Vi utelater fysisk medium, kassert materiale og registreringer med status utgår

:)

let $r := //dokumentbeskrivelse[not (boolean(dokumentobjekt)) 
  and not (../journalstatus = "Utgår")  
  and not (../dokumentmedium = 'Fysisk medium')
  and not (../../kassasjon/kassasjonsdato < current-date()) 
  and not (../../saksstatus = "Utgår") ]/concat(
      ../registreringsID, '; ', 
      ../journalposttype, ../moeteregistreringstype, '; ' , 
      dokumentstatus ,'; -Dokumentnr. ' , 
      dokumentnummer, ' (', 
      tilknyttetRegistreringSom,'); ', 
      tittel)
      
return $r