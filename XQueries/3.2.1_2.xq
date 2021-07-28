xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;

(:Finner alle filer hvor det angitte filnavnet ikke stemmer overens med det angitte formatet:)
(:NB! Delvis hardkodet!:)

for $r in distinct-values(//dokumentobjekt[(boolean(referanseDokumentfil)) and not(ends-with(referanseDokumentfil, fn:upper-case(format))) 
and not(ends-with(referanseDokumentfil, fn:lower-case(format)))
and not((ends-with(referanseDokumentfil, 'JPG')) and (format = 'JPEG'))
and not((ends-with(referanseDokumentfil, 'SOS')) and (format = 'SOSI'))
and not((ends-with(referanseDokumentfil, 'PDF')) and (ends-with(format, 'pdf')))
and not((ends-with(referanseDokumentfil, 'HTML')) and (ends-with(format, 'html')))
]/concat(
  fn:upper-case(substring-after(referanseDokumentfil, '.')), '; ', 
  fn:upper-case(format)))

return concat($r, '; ', 
count(//dokumentobjekt[fn:upper-case(substring-after(referanseDokumentfil, '.')) = substring-before($r, ';')
and fn:upper-case(format) = substring-after($r, '; ')]), '; ', " ")
