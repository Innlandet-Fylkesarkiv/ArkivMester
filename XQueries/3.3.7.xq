declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 



for $f in distinct-values(//administrativEnhet)
order by count(//registrering[administrativEnhet = $f]) + count(//korrespondansepart[administrativEnhet = $f]) + count(//mappe[administrativEnhet = $f]) descending
return (concat($f, '; ', count(//registrering[administrativEnhet = $f]) + count(//korrespondansepart[administrativEnhet = $f]) + count(//mappe[administrativEnhet = $f])))
