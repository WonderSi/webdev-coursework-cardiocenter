package com.medical.cardio.service.upload

import org.apache.commons.csv.CSVFormat
import org.springframework.web.multipart.MultipartFile

object CsvParser {

    fun parse(file: MultipartFile): List<Map<String, String>> {
        val reader = file.inputStream.bufferedReader(Charsets.UTF_8)
        val format = CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .setIgnoreEmptyLines(true)
            .build()

        return format.parse(reader).use { parser ->
            val headers = parser.headerNames
            parser.records.map { record ->
                headers.associateWith { header ->
                    runCatching { record.get(header) }.getOrDefault("")
                }
            }
        }
    }
}
