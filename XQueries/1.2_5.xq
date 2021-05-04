declare namespace xh="http://www.arkivverket.no/standarder/noark5/arkivstruktur";

for $arkivdel in xh:arkiv/xh:arkivdel

return $arkivdel/concat(xh:tittel, "; ", xh:dokumentmedium)
