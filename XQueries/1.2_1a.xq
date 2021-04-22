declare namespace xh="http://www.loc.gov/METS/";

let $kommune := xh:mets/xh:metsHdr/xh:agent[@ROLE="OTHER" and @OTHERROLE="SUBMITTER" and @TYPE="ORGANIZATION"]/xh:name/text()

let $aarmottatt := ""

let $startdato := xh:mets/xh:metsHdr/xh:altRecordID[@TYPE="STARTDATE"]/text()

let $sluttdato := xh:mets/xh:metsHdr/concat(xh:altRecordID[@TYPE="ENDDATE"]/text(), '<ENDDATE>')

return ($kommune, $aarmottatt, $startdato, $sluttdato)
