declare namespace xh="http://arkivverket.no/standarder/METS";

let $kunde := xh:mets/xh:metsHdr/xh:agent[@ROLE="OTHER" and @OTHERROLE="SUBMITTER" and @TYPE="ORGANIZATION"]/xh:name/text()

let $kontaktperson := xh:mets/xh:metsHdr/xh:agent[@ROLE="OTHER" and @OTHERROLE="SUBMITTER" and @TYPE="INDIVIDUAL"]/xh:name

let $uttrekksformat := xh:mets/xh:metsHdr/xh:agent[@ROLE="CREATOR" and @TYPE="OTHER" and @OTHERTYPE="SOFTWARE"]/xh:name
let $format := if (fn:exists($uttrekksformat)) then $uttrekksformat/text() else ""

let $produksjonsdato := string(xh:mets/xh:metsHdr/@CREATEDATE)

return ("", $kunde, string-join(($kontaktperson/text()),", "), $format, $produksjonsdato, "", "", "")

