declare namespace xh="http://arkivverket.no/standarder/METS";

let $kommune := xh:mets/xh:metsHdr/xh:agent[@ROLE="OTHER" and @OTHERROLE="SUBMITTER" and @TYPE="ORGANIZATION"]/xh:name/text()

let $aarmottatt := xh:mets/xh:metsHdr/substring-before(data(@CREATEDATE), 'T')

let $startdato := xh:mets/xh:metsHdr/xh:altRecordID[@TYPE="STARTDATE"]/text()

let $sluttdato := xh:mets/xh:metsHdr/xh:altRecordID[@TYPE="ENDDATE"]/text()

return ($kommune, $aarmottatt, $startdato, $sluttdato)
