package com.medical.cardio.service

import com.medical.cardio.dto.RowError
import com.medical.cardio.dto.UploadReport
import com.medical.cardio.entity.DiagnosisEntity
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.DiagnosisRepository
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.repository.PatientRepository
import com.medical.cardio.service.upload.CsvParser
import com.medical.cardio.service.upload.ExcelParser
import com.medical.cardio.service.upload.GlossaryResolver
import com.medical.cardio.service.upload.PatientMapper
import com.medical.cardio.service.upload.RowValidator
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

@Service
class UploadService(
    private val patientRepository: PatientRepository,
    private val diagnosisRepository: DiagnosisRepository,
    private val glossaryRepository: GlossaryRepository,
    private val adminUserRepository: AdminUserRepository,
) {

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
                val importData = PatientMapper.map(row, resolver, creator)
                val savedPatient = patientRepository.save(importData.patient)
                if (importData.diagnosisEntries.isNotEmpty()) {
                    diagnosisRepository.saveAll(
                        importData.diagnosisEntries.map { entry ->
                            DiagnosisEntity(
                                patient          = savedPatient,
                                diagnosis        = entry.diagnosis,
                                yearOfDiagnosis  = entry.year,
                            )
                        }
                    )
                }
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

    fun generateTemplate(): ByteArray {
        val workbook = XSSFWorkbook()

        val dataSheet = workbook.createSheet("Данные")
        val boldStyle = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply { bold = true })
        }
        val headers = listOf(
            "gender", "age", "height", "weight", "hip_measurement",
            "glucose", "cholesterol", "non_hdl_cholesterol", "vldl_cholesterol",
            "hdl_cholesterol", "ldl_cholesterol", "apolipoprotein_a", "apolipoprotein_b",
            "triglycerides", "alcohol", "profession", "region",
            "stroke", "stroke_year", "heart_failure", "heart_failure_year",
            "cad", "cad_year", "angina", "angina_year",
            "myocardial_infarction", "mi_year", "arterial_hypertension", "ah_year",
        )
        val headerRow = dataSheet.createRow(0)
        headers.forEachIndexed { i, h ->
            headerRow.createCell(i).apply {
                setCellValue(h)
                cellStyle = boldStyle
            }
            dataSheet.setColumnWidth(i, 5500)
        }

        val refSheet = workbook.createSheet("Справочник")
        val refRows = listOf(
            "Пол (gender)" to "",
            "  1" to "Мужской",
            "  2" to "Женский",
            "" to "",
            "Алкоголь (alcohol)" to "",
            "  1" to "Никогда не употреблял",
            "  2" to "Употреблял ранее",
            "  3" to "Употребляю",
            "" to "",
            "Профессия (profession)" to "",
            "  1"  to "Ведение домашнего хозяйства",
            "  2"  to "Вооруженные силы",
            "  3"  to "Лица свободных профессий",
            "  4"  to "Низкоквалифицированные работники, ручной труд",
            "  5"  to "Операторы и монтажники оборудования",
            "  6"  to "Служащие, сфера обслуживания",
            "  7"  to "Никогда не работающие домохозяйки",
            "  8"  to "Дипломированные специалисты, умственный труд",
            "  9"  to "Другое",
            "  10" to "Квалифицированные специалисты сельского хозяйства",
            "  11" to "Пенсионеры",
            "  12" to "Ремесленники и представители промышленности",
            "  13" to "Техники и младшие специалисты",
            "  14" to "Руководители и законодательные органы",
            "" to "",
            "Регион (region)" to "",
            "  1" to "Рудничный",
            "  2" to "Центральный",
            "  3" to "Заводский",
            "  4" to "Кировский",
            "  5" to "Ленинский",
            "  6" to "Сельская местность",
            "" to "",
            "Диагнозы (stroke, heart_failure, ...)" to "0 = нет, 1 = есть",
            "  stroke"                to "Инсульт",
            "  heart_failure"         to "Сердечная недостаточность",
            "  cad"                   to "Нарушение ритма / ИБС",
            "  angina"                to "Стенокардия",
            "  myocardial_infarction" to "Инфаркт миокарда",
            "  arterial_hypertension" to "Артериальная гипертензия",
            "" to "",
            "Год диагноза (stroke_year, ...)" to "Число 1900–2100, можно оставить пустым",
        )
        refRows.forEachIndexed { i, (key, value) ->
            val row = refSheet.createRow(i)
            row.createCell(0).setCellValue(key)
            row.createCell(1).setCellValue(value)
        }
        refSheet.setColumnWidth(0, 9000)
        refSheet.setColumnWidth(1, 14000)

        val out = ByteArrayOutputStream()
        workbook.write(out)
        workbook.close()
        return out.toByteArray()
    }
}
