declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 

(: Teller opp mapper pr. år basert på opprettetDato, fordeling innenfor arkivdel 
   NB! Denne spørringen kan være svært tung. 
   Alternativ: Benytt arkivperiodeStartDato og arkivperiodeSluttDato for avgrensning.
   :)
let $x := arkiv/arkivdel
   
for $y in min(.//year-from-dateTime(opprettetDato)) to max(.//year-from-dateTime(avsluttetDato))

return concat($y, ": ", string-join(for $x in /arkiv/arkivdel return $x/concat(systemID, "; ", count(.//mappe[year-from-dateTime(opprettetDato) = $y]), "; ")))