xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;

for $r in //registrering[.//dokumentobjekt[analyze-string(filstoerrelse, '[0-9]{1,10}')//fn:match = "0"]]/dokumentbeskrivelse

return $r/concat(../systemID, '; ', dokumentnummer)