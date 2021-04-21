declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 

(: Teller opp registreringer pr. journalposttype - fordelt på arkivdel
   :)

for $a in /arkiv/arkivdel/systemID return 
  for $d in distinct-values(/arkiv/arkivdel[systemID = $a]//journalposttype) return
    concat($d, ': ', /arkiv/arkivdel[systemID = $a]/tittel, '; ', count(/arkiv/arkivdel[systemID = $a]//registrering[journalposttype = $d]))