declare default element namespace "http://www.arkivverket.no/standarder/noark5/loependeJournal";

(:Finner idenstiske titler og offentlige titler i saksmapper for journalposter
Versjon 2.0 
Skrevet av Pål Mjørlund. 
Henter både sak og journalpostnivå.
:)

distinct-values(//journalregistrering[((boolean(saksmappe/skjermingMetadata)) and 
				      (saksmappe/tittel = saksmappe/offentligTittel))
                                     ]/concat(saksmappe/saksaar,'/',saksmappe/sakssekvensnummer)),

//journalregistrering[((boolean(journalpost/skjermingMetadata)) and 
		      (journalpost/tittel = journalpost/offentligTittel))
                     ]/concat(journalpost/journalsekvensnummer, '/', journalpost/journalaar)