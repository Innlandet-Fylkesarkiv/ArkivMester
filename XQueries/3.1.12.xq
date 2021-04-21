xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 

(: Registreringer fordelt på journalpost status :)

for $s in distinct-values(//journalstatus)
return 
if($s = ('Arkivert', 'Utgår')) then
    concat ($s, ': ', count(//registrering[journalstatus = $s]))
else
    let $r := //registrering[journalstatus = $s]
    return concat( 
        $s, ': ', 
        count(//$r), '
         ',
         string-join($r[position() lt 6]/registreringsID, ',
         ')
)