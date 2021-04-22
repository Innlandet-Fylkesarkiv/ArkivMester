xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance";
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog";
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg";


for $x in //arkivdel
return

for $y in $x/..//mappe
return

for $z in $y/registrering
return


concat( $z/registreringsID, "; ", $z/systemID, "; ", $z/opprettetDato, "; " , $y/mappeID, "; ", $x/tittel)


