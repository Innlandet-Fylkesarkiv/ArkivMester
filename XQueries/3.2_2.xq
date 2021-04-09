let $r := Report/ReportItems/ReportItem
[(Specification/Description = 'File sizes per extension')]
/Groups/Group/concat('• ' ,
Values/Value/text(), ': ',
ProfileSummaries/ProfileSummary/Count/text())


return $r