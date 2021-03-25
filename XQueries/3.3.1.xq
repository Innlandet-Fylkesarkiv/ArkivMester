xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;


(: Ser etter unike klassifikasjonssystemer - basert p�tittel og beskrivelse :)

let $r := distinct-values(arkiv/arkivdel/klassifikasjonssystem/concat(
  //arkivdel/systemID, '; ', 
  tittel, '; ', 
  if(count(.//mappe) > 0) then "JA" else "NEI", '; ',
  count(klasse), '; ',
  count(klasse/./klasse), '; ',
  count(klasse/./klasse/./klasse)
))

return $r