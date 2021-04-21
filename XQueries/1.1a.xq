declare namespace xh="http://arkivverket.no/standarder/METS";

let $kunde := xh:mets/xh:metsHdr/xh:agent[@ROLE="OTHER" and @OTHERROLE="SUBMITTER" and @TYPE="ORGANIZATION"]/xh:name/text()
let $kontaktperson := xh:mets/xh:metsHdr/xh:agent[@ROLE="OTHER" and @OTHERROLE="SUBMITTER" and @TYPE="INDIVIDUAL"]/xh:name

let $uttrekksformat1 := xh:mets/xh:metsHdr/xh:agent[@ROLE="OTHER" and @OTHERROLE="PRODUCER" and @TYPE="OTHER" and @OTHERTYPE="SOFTWARE"]/xh:note[2]
let $uttrekksformat2 := xh:mets/xh:metsHdr/xh:agent[@ROLE="OTHER" and @OTHERROLE="PRODUCER" and @TYPE="OTHER" and @OTHERTYPE="SOFTWARE"]/xh:note[3]
let $format1 := if (fn:exists($uttrekksformat1)) then $uttrekksformat1/text() else ""
let $format2 := if (fn:exists($uttrekksformat2)) then $uttrekksformat2/text() else ""

let $produksjonsdato := string(xh:mets/xh:metsHdr/@CREATEDATE)

return ("", $kunde, string-join(($kontaktperson/text()),", "), $format1 || " v" || $format2, $produksjonsdato, "", "", "", "")