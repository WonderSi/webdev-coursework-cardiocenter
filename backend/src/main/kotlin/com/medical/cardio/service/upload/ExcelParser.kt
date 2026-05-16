package com.medical.cardio.service.upload

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile

object ExcelParser {

    fun parse(file: MultipartFile): List<Map<String, String>> {
        val ext = file.originalFilename?.substringAfterLast('.')?.lowercase()
        val workbook: Workbook = file.inputStream.use { input ->
            when (ext) {
                "xlsx" -> XSSFWorkbook(input)
                "xls"  -> HSSFWorkbook(input)
                else   -> throw IllegalArgumentException("Неподдерживаемый формат Excel: $ext")
            }
        }
        return workbook.use { wb ->
            val sheet = wb.getSheetAt(0)
            val allRows = sheet.rowIterator().asSequence().toList()
            if (allRows.isEmpty()) return emptyList()

            val headers = allRows[0].cellIterator().asSequence()
                .map { cellToString(it).lowercase() }
                .toList()

            allRows.drop(1).mapNotNull { row ->
                val map = headers.indices.associate { idx ->
                    val cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                    headers[idx] to cellToString(cell)
                }
                if (map.values.all { it.isBlank() }) null else map
            }
        }
    }

    private fun cellToString(cell: Cell): String = when (cell.cellType) {
        CellType.NUMERIC -> {
            val d = cell.numericCellValue
            if (d == Math.floor(d) && !d.isInfinite()) d.toLong().toString() else d.toString()
        }
        CellType.STRING  -> cell.stringCellValue.trim()
        CellType.BOOLEAN -> if (cell.booleanCellValue) "1" else "0"
        CellType.FORMULA -> runCatching {
            val d = cell.numericCellValue
            if (d == Math.floor(d) && !d.isInfinite()) d.toLong().toString() else d.toString()
        }.getOrElse { cell.stringCellValue.trim() }
        else -> ""
    }
}
