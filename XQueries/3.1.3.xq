xquery version "3.1";

declare namespace xsi="http://www.w3.org/2001/XMLSchema-instance";
declare default element namespace "http://www.arkivverket.no/standarder/noark5/arkivstruktur" ;
declare namespace n5mdk="http://www.arkivverket.no/standarder/noark5/metadatakatalog";


let $arkivtittel := arkiv/arkivdel/tittel/text()

let $arkivID := arkiv/arkivdel/systemID/text()

let $arkivStatus := arkiv/arkivdel/arkivdelstatus/text()

return ($arkivtittel, $arkivID, $arkivStatus)


