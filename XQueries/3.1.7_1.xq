declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 

sort(//mappe[not (mappe or registrering)]/concat(mappeID, '; ', saksstatus, ';  ', tittel))