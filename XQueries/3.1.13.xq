xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;


(:Dokumenter som ikke er ferdigstilt. Avgrenser slik at dokumenter som er vedtatt kassert eller som utgår ikke blir med i listen:)
(:Ephorte trenger ../../mappeID + ../journalpostnummer istedenfor ../registreringsID:)

let $v := //dokumentbeskrivelse[not (dokumentstatus = "Dokumentet er ferdigstilt") 
  and not (../../kassasjon/kassasjonsdato < current-date()) 
  and not (../journalstatus = "Utgår")  
  and not (../../saksstatus = "Utgår")]
  
let $u := //dokumentbeskrivelse[not (dokumentstatus = "Dokumentet er ferdigstilt") 
  and not (../../kassasjon/kassasjonsdato < current-date()) 
  and (../journalstatus = "Utgår")  
  and (../../saksstatus = "Utgår")]
  
let $r := if(count($v) > 0) then $v else $u

return $r/concat(
  ../registreringsID, '; ', 
      ../journalposttype, ../moeteregistreringstype, '; ' , 
      dokumentstatus ,'; -Dokumentnr. ' , 
      dokumentnummer, ' (', 
      tilknyttetRegistreringSom,'); '
)