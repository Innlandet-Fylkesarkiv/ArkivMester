xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;

for $r in distinct-values(//mappe[@xsi:type="moetemappe"]/utvalg)

return concat($r, '; ', count(//mappe[@xsi:type="moetemappe" and utvalg = $r]/moetedeltaker))