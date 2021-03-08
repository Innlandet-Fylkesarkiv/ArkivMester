declare namespace xh="http://www.arkivverket.no/standarder/addml";

let $antallMapper := xh:addml/xh:dataset/xh:dataObjects/xh:dataObject/xh:dataObjects/xh:dataObject/xh:properties/xh:property/xh:properties/xh:property[xh:value="mappe"]/xh:properties/xh:property[@name="value" and @dataType="integer"]/xh:value/text()

let $antallRegistreringer := xh:addml/xh:dataset/xh:dataObjects/xh:dataObject/xh:dataObjects/xh:dataObject/xh:properties/xh:property/xh:properties/xh:property[xh:value="registrering"]/xh:properties/xh:property[@name="value" and @dataType="integer"]/xh:value/text()

let $antallDokumenter := xh:addml/xh:dataset/xh:dataObjects/xh:dataObject/xh:properties/xh:property/xh:properties/xh:property/xh:properties/xh:property[@name="antallDokumentfiler" and @dataType="integer"]/xh:value/text()

return ($antallMapper, $antallRegistreringer, $antallDokumenter)