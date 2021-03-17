xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance";
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 

(:Finner alle inngående dokumenter som mangler avskrivninger og merknader. Fjern "and (journalposttype = 'Inngående dokument')" for alle journalposter.:)

let $r := //registrering[not (boolean(avskrivning)) and not (boolean(merknad)) and (journalposttype = 'Inngående dokument')]/concat(systemID, '; ', registreringsID)

return $r