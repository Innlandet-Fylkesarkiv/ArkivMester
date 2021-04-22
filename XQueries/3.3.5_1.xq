xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance"; 
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog"; 
declare namespace el="http://www.arkivverket.no/standarder/noark5/endringslogg" ;

/arkiv/arkivdel[count(.//registrering[dokumentflyt]) > 0]/concat(
  count(.//registrering[dokumentflyt]), '; ',
  (count(.//dokumentflyt) div count(.//registrering[dokumentflyt])),'; ', (count(.//registrering[dokumentflyt]) div count(.//registrering)) * 100)