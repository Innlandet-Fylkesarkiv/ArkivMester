xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;


(:Skjerming fordelt på skjermingsvarighet - Se også A_Sk3 :)

for $r in distinct-values(//skjermingsvarighet) 

return concat($r,'; ', count(//skjerming[skjermingsvarighet = $r]))
