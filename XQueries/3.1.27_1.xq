xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance";
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog";
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg";


for $x in //arkiv/arkivdel
  for $id in distinct-values($x/.//dokumentbeskrivelse/systemID/text())
    where count($x/.//dokumentbeskrivelse/systemID[text() = $id]) >= 2

return concat($id , "; ", count($x/.//dokumentbeskrivelse/systemID[text() = $id]))
