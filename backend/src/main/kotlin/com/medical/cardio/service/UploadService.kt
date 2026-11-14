package com.medical.cardio.service

import com.medical.cardio.dto.PatientManualCreateRequest
import com.medical.cardio.dto.RowError
import com.medical.cardio.dto.UploadReport
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.repository.PatientRepository
import com.medical.cardio.service.upload.CsvParser
import com.medical.cardio.service.upload.ExcelParser
import com.medical.cardio.service.upload.GlossaryResolver
import com.medical.cardio.service.upload.PatientMapper
import com.medical.cardio.service.upload.PatientImportSchema
import com.medical.cardio.service.upload.RowValidator
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

@Service
class UploadService(
    private val patientRepository: PatientRepository,
    private val glossaryRepository: GlossaryRepository,
    private val adminUserRepository: AdminUserRepository,
) {

    @CacheEvict(value = ["dashboard-stats"], allEntries = true)
    @Transactional
    fun upload(file: MultipartFile, creatorEmail: String): UploadReport {
        val creator = adminUserRepository.findByEmail(creatorEmail)
            ?: error("Пользователь не найден: $creatorEmail")

        val resolver = GlossaryResolver(glossaryRepository.findAllWithValues())

        val ext = file.originalFilename?.substringAfterLast('.')?.lowercase()
        val rawRows: List<Map<String, String>> = when (ext) {
            "xlsx", "xls" -> ExcelParser.parse(file)
            "csv"         -> CsvParser.parse(file)
            else          -> throw IllegalArgumentException("Допустимые форматы: .xlsx, .xls, .csv")
        }

        val errors = mutableListOf<RowError>()
        var savedCount = 0

        rawRows.forEachIndexed { index, row ->
            val rowNum = index + 2 // строка 1 — заголовки
            try {
                val validation = RowValidator.validate(row, resolver)
                if (!validation.valid) {
                    errors.add(RowError(rowNum, validation.reason))
                    return@forEachIndexed
                }
                patientRepository.save(PatientMapper.map(row, resolver, creator))
                savedCount++
            } catch (e: Exception) {
                errors.add(RowError(rowNum, e.message ?: "Ошибка обработки строки"))
            }
        }

        return UploadReport(
            totalRows    = rawRows.size,
            savedRows    = savedCount,
            skippedRows  = errors.size,
            errors       = errors,
        )
    }

    @CacheEvict(value = ["dashboard-stats"], allEntries = true)
    @Transactional
    fun savePatient(request: PatientManualCreateRequest, creatorEmail: String) {
        val creator = adminUserRepository.findByEmail(creatorEmail)
            ?: error("Пользователь не найден: $creatorEmail")
        val resolver = GlossaryResolver(glossaryRepository.findAllWithValues())

        val row = PatientImportSchema.toRow(request)
        val validation = RowValidator.validate(row, resolver)
        require(validation.valid) { validation.reason }
        patientRepository.save(PatientMapper.map(row, resolver, creator))
    }

    fun generateTemplate(): ByteArray = XSSFWorkbook().use { workbook ->
        val dataSheet = workbook.createSheet("Данные")
        val boldStyle = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply { bold = true })
        }
        val headerRow = dataSheet.createRow(0)
        PatientImportSchema.headers.forEachIndexed { index, name ->
            headerRow.createCell(index).apply {
                setCellValue(name)
                cellStyle = boldStyle
            }
            dataSheet.setColumnWidth(index, 5500)
        }
        dataSheet.createFreezePane(0, 1)

        val refRows = mutableListOf(
            "Назначение" to "Импорт обследований в БД и аналитику. Это не обучающий CSV patients_wide.",
            "model_kind (обязательно)" to "city — отдельное обследование; region — за период наблюдения",
            "year" to "Для city: 2013, 2017, 2019 или 2021. Для region: пустая ячейка",
            "chronic_lung_disease (обязательно)" to "Хронические заболевания лёгких: 0 — нет, 1 — есть; неизвестное не заменять нулём",
            "Обязательные поля" to "model_kind, chronic_lung_disease, gender, age, height, weight, alcohol, profession, region; year обязателен для city",
            "Необязательные поля" to "hip_measurement, glucose, cholesterol, hdl_cholesterol, ldl_cholesterol, triglycerides",
            "Кодировка профессий" to "notebook-13-v1, коды 1–13; старые коды 1–14 не подходят",
            "Числа" to "Возраст — целое число; десятичный разделитель в CSV — точка; неизвестные анализы оставлять пустыми",
            "Анализы" to "Неотрицательные значения, не больше 9999.99 (ограничение хранения NUMERIC(6,2))"
        )
        val glossaries = glossaryRepository.findAllWithValues().associateBy { it.name }
        for ((name, column) in listOf(
            "gender" to "gender", "alcohol" to "alcohol",
            PatientImportSchema.PROFESSION_GLOSSARY to "profession", "region" to "region"
        )) {
            val glossary = requireNotNull(glossaries[name]) { "Справочник импорта не найден: $name" }
            refRows += "" to ""
            refRows += column to "Коды, а не ID записей БД"
            glossary.values.sortedBy { it.code }.forEach { value ->
                refRows += value.code.toString() to value.value
            }
        }
        val refSheet = workbook.createSheet("Справочник")
        refRows.forEachIndexed { index, (key, value) ->
            refSheet.createRow(index).apply {
                createCell(0).setCellValue(key)
                createCell(1).setCellValue(value)
            }
        }
        refSheet.setColumnWidth(0, 11000)
        refSheet.setColumnWidth(1, 24000)
        ByteArrayOutputStream().use { out ->
            workbook.write(out)
            out.toByteArray()
        }
    }
}
