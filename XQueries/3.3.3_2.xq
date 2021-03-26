xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;

for $f in distinct-values(//merknadstype) 
order by count(.//merknad[merknadstype = $f]) descending
return concat($f, '; ', count(.//merknad[merknadstype = $f]), '; ', " ")