xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;


(:Dokumenter som ikke er ferdigstilt. Avgrenser slik at dokumenter som er vedtatt kassert eller som utgår ikke blir med i listen:)
(:Ephorte trenger ../../mappeID + ../journalpostnummer istedenfor ../registreringsID:)

let $r := //dokumentbeskrivelse[not (dokumentstatus = "Dokumentet er ferdigstilt") 
  and not (../../kassasjon/kassasjonsdato < current-date()) 
  and not (../journalstatus = "Utgår")  
  and not (../../saksstatus = "Utgår")]

return $r/concat(
  ../registreringsID, '; ', 
      ../journalposttype, ../moeteregistreringstype, '; ' , 
      dokumentstatus ,'; -Dokumentnr. ' , 
      dokumentnummer, ' (', 
      tilknyttetRegistreringSom,'); '
)