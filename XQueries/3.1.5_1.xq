xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;

(:Mapper fordelt på år og mappetype:)
(:NB! Tung kjøring!:)

for $f in distinct-values(//saksaar) 
order by ($f) 
return 
concat($f, ': Saksmapper; ', count((.//mappe[(@xsi:type = 'saksmappe') and (saksaar = $f)])), 
'; Møtemapper; ', count((.//mappe[(@xsi:type = 'moetemappe') and (year-from-date(moetedato) = $f)])))