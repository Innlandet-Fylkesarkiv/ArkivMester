xquery version "3.1";

let $non := report/batchSummary/validationReports/data(@nonCompliant)

let $failed := report/batchSummary/validationReports/data(@failedJobs)

return ($non, $failed)

