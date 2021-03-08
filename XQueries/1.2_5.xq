declare namespace xh="http://www.arkivverket.no/standarder/noark5/arkivstruktur";

let $arkivtittel := xh:arkiv/xh:arkivdel/xh:tittel/text()

let $arkivmedium1 := xh:arkiv/xh:arkivdel/xh:dokumentmedium/text()

let $arkivmedium2 := xh:arkiv/xh:arkivdel/xh:dokumentmedium/text()

return ($arkivtittel, $arkivmedium1, $arkivmedium2)