package com.medical.cardio.controller

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.PatientRepository
import com.medical.cardio.support.ControllerTestBase
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.cache.CacheManager
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.jupiter.api.Assertions.*

class UploadControllerTest : ControllerTestBase() {

    @Autowired
    private lateinit var adminUserRepository: AdminUserRepository

    @Autowired
    private lateinit var patientRepository: PatientRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var jdbc: JdbcTemplate
    @Autowired private lateinit var cacheManager: CacheManager

    private lateinit var testUser: AdminUserEntity

    @BeforeEach
    fun setUp() {
        cacheManager.getCache("dashboard-stats")?.clear()
        testUser = adminUserRepository.save(
            AdminUserEntity(
                email = "upload-test@cardio.ru",
                passwordHash = passwordEncoder.encode("ValidPass@2000"),
                role = Role.DOCTOR
            )
        )
    }

    @AfterEach
    fun tearDown() {
        // Delete patients first (diagnoses cascade via ON DELETE CASCADE), then the admin user
        patientRepository.deleteAll()
        adminUserRepository.delete(testUser)
    }

    @Test
    fun `downloadTemplate with auth returns 200 and xlsx content-disposition`() {
        val cookie = authCookie(testUser)
        mockMvc.get("/api/admin/upload/template") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            header { string("Content-Disposition", containsString("patients_template.xlsx")) }
        }
    }

    @Test
    fun `downloadTemplate without auth returns 401`() {
        mockMvc.get("/api/admin/upload/template").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `upload with wrong file extension returns 400`() {
        val cookie = authCookie(testUser)
        val file = MockMultipartFile(
            "file",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "fake-pdf-content".toByteArray()
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `upload without auth returns 401`() {
        val file = MockMultipartFile(
            "file",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "fake-pdf-content".toByteArray()
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `upload valid CSV returns 200 with upload report`() {
        val cookie = authCookie(testUser)
        val csvContent = csv(validPatient())
        val file = MockMultipartFile(
            "file",
            "patients.csv",
            "text/csv",
            csvContent.toByteArray(Charsets.UTF_8)
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalRows") { value(1) }
            jsonPath("$.savedRows") { value(1) }
            jsonPath("$.skippedRows") { value(0) }
        }
    }

    @Test
    fun `upload empty file returns 400`() {
        val cookie = authCookie(testUser)
        val file = MockMultipartFile(
            "file",
            "empty.csv",
            "text/csv",
            ByteArray(0)
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `upload CSV with invalid gender code skips the row`() {
        val cookie = authCookie(testUser)
        val csvContent = csv(validPatient() + ("gender" to 99))
        val file = MockMultipartFile(
            "file",
            "patients.csv",
            "text/csv",
            csvContent.toByteArray(Charsets.UTF_8)
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalRows") { value(1) }
            jsonPath("$.savedRows") { value(0) }
            jsonPath("$.skippedRows") { value(1) }
        }
    }

    @Test
    fun `upload CSV with age below minimum skips the row`() {
        val cookie = authCookie(testUser)
        val csvContent = csv(validPatient() + ("age" to 10))
        val file = MockMultipartFile(
            "file",
            "patients.csv",
            "text/csv",
            csvContent.toByteArray(Charsets.UTF_8)
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalRows") { value(1) }
            jsonPath("$.savedRows") { value(0) }
            jsonPath("$.skippedRows") { value(1) }
        }
    }

    @Test
    fun `manual city and region preserve model fields and use new profession codes`() {
        for ((kind, year, district) in listOf(Triple("city", 2017, 6), Triple("region", null, 1))) {
            val body = validPatient() + mapOf("modelKind" to kind, "year" to year, "region" to district)
            mockMvc.post("/api/admin/patients") {
                cookie(authCookie(testUser))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(body)
            }.andExpect { status { isOk() } }
        }
        val rows = jdbc.queryForList("""
            SELECT p.model_kind, p.model_year, p.chronic_lung_disease, v.code, g.name, v.value
            FROM patients p JOIN glossary_values v ON v.id = p.profession_id
            JOIN glossaries g ON g.id = v.glossary_id WHERE p.creator_id = ? ORDER BY p.id
        """.trimIndent(), testUser.id)
        assertEquals(2, rows.size)
        assertEquals("city", rows[0]["model_kind"])
        assertEquals(2017, rows[0]["model_year"])
        assertEquals("region", rows[1]["model_kind"])
        assertNull(rows[1]["model_year"])
        rows.forEach { row ->
            assertEquals(0, row["chronic_lung_disease"])
            assertEquals(1, row["code"])
            assertEquals("profession", row["name"])
            assertEquals("Руководители и представители власти", row["value"])
        }
        assertEquals(13L, jdbc.queryForObject("""
            SELECT COUNT(*) FROM glossary_values v JOIN glossaries g ON g.id = v.glossary_id
            WHERE g.name = 'profession'
        """.trimIndent(), Long::class.java))
    }

    @Test
    fun `manual input and CSV reject the same invalid observations without saving them`() {
        val invalid = listOf(
            validPatient() - "chronicLungDisease",
            validPatient() + ("chronicLungDisease" to 2),
            validPatient() - "year",
            validPatient() + ("year" to 2020),
            validPatient() + ("modelKind" to "region"),
            validPatient() + ("modelKind" to "other"),
            validPatient() + ("profession" to 14),
            validPatient() - "profession",
            validPatient() + ("alcohol" to 0),
            validPatient() + ("region" to 7),
            validPatient() - "height",
            validPatient() + ("weight" to 0),
            validPatient() + ("age" to 10),
            validPatient() + ("glucose" to -1),
            validPatient() + ("cholesterol" to "NaN"),
            validPatient() + ("triglycerides" to 10000)
        )
        for (body in invalid) {
            mockMvc.post("/api/admin/patients") {
                cookie(authCookie(testUser))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(body)
            }.andExpect { status { isBadRequest() } }
            mockMvc.multipart("/api/admin/upload") {
                cookie(authCookie(testUser))
                file(MockMultipartFile("file", "invalid.csv", "text/csv", csv(body).toByteArray()))
            }.andExpect {
                status { isOk() }
                jsonPath("$.savedRows") { value(0) }
                jsonPath("$.skippedRows") { value(1) }
                jsonPath("$.errors[0].row") { value(2) }
            }
        }
        assertEquals(0, patientRepository.count())
    }

    @Test
    fun `downloaded template imports city and region and invalidates cached analytics`() {
        mockMvc.get("/api/admin/dashboard") { cookie(authCookie(testUser)) }.andExpect {
            status { isOk() }
            jsonPath("$.totalPatients") { value(0) }
        }
        val template = mockMvc.get("/api/admin/upload/template") {
            cookie(authCookie(testUser))
        }.andReturn().response.contentAsByteArray
        val bytes = XSSFWorkbook(ByteArrayInputStream(template)).use { workbook ->
            val sheet = workbook.getSheet("Данные")
            val headers = sheet.getRow(0).map { it.stringCellValue }
            assertEquals(columns.map { it.first }, headers)
            val reference = workbook.getSheet("Справочник").flatMap { row -> row.map { it.stringCellValue } }
            assertTrue(reference.contains("notebook-13-v1, коды 1–13; старые коды 1–14 не подходят"))
            assertTrue(reference.contains("Руководители и представители власти"))
            assertFalse(reference.contains("Диагнозы (stroke, heart_failure, ...)"))
            listOf(validPatient(), validPatient() + mapOf("modelKind" to "region", "year" to null, "profession" to 13))
                .forEachIndexed { index, body ->
                    val row = sheet.createRow(index + 1)
                    columns.forEachIndexed { column, (_, json) -> row.createCell(column).setCellValue(body[json]?.toString().orEmpty()) }
                }
            ByteArrayOutputStream().use { out -> workbook.write(out); out.toByteArray() }
        }
        mockMvc.multipart("/api/admin/upload") {
            cookie(authCookie(testUser))
            file(MockMultipartFile("file", "patients.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes))
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalRows") { value(2) }
            jsonPath("$.savedRows") { value(2) }
            jsonPath("$.skippedRows") { value(0) }
        }
        mockMvc.get("/api/admin/dashboard") { cookie(authCookie(testUser)) }.andExpect {
            status { isOk() }
            jsonPath("$.totalPatients") { value(2) }
        }
        assertEquals(listOf("city", "region"), jdbc.queryForList("SELECT model_kind FROM patients ORDER BY id", String::class.java))
    }

    @Test
    fun `partial CSV import reports actual row numbers and saves only valid records`() {
        val csv = csv(validPatient()) + "\n" + csv(validPatient() + ("profession" to 14)).substringAfter('\n')
        mockMvc.multipart("/api/admin/upload") {
            cookie(authCookie(testUser))
            file(MockMultipartFile("file", "mixed.csv", "text/csv", csv.toByteArray()))
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalRows") { value(2) }
            jsonPath("$.savedRows") { value(1) }
            jsonPath("$.skippedRows") { value(1) }
            jsonPath("$.errors[0].row") { value(3) }
            jsonPath("$.errors[0].reason") { value(containsString("profession")) }
        }
        assertEquals(1, patientRepository.count())
    }

    @Test
    fun `training CSV and retired diagnosis columns are not silently imported`() {
        for (text in listOf("id,пол 2013\n1,1", csv(validPatient()).replace("triglycerides", "stroke"))) {
            mockMvc.multipart("/api/admin/upload") {
                cookie(authCookie(testUser))
                file(MockMultipartFile("file", "wrong-schema.csv", "text/csv", text.toByteArray(Charsets.UTF_8)))
            }.andExpect {
                status { isOk() }
                jsonPath("$.savedRows") { value(0) }
                jsonPath("$.skippedRows") { value(1) }
            }
        }
        assertEquals(0, patientRepository.count())
    }

    private fun validPatient(): Map<String, Any?> = mapOf(
        "modelKind" to "city", "year" to 2021, "chronicLungDisease" to 0,
        "gender" to 1, "age" to 45, "height" to 175, "weight" to 80,
        "alcohol" to 1, "profession" to 1, "region" to 5
    )

    @Test
    fun `database requires current model fields and rejects invalid combinations`() {
        val gender = jdbc.queryForObject("""
            SELECT v.id FROM glossary_values v JOIN glossaries g ON g.id = v.glossary_id
            WHERE g.name = 'gender' AND v.code = 1
        """.trimIndent(), Long::class.java)
        val profession = jdbc.queryForObject("""
            SELECT v.id FROM glossary_values v JOIN glossaries g ON g.id = v.glossary_id
            WHERE g.name = 'profession' AND v.code = 1
        """.trimIndent(), Long::class.java)
        assertThrows(DataIntegrityViolationException::class.java) {
            jdbc.update("INSERT INTO patients (creator_id, gender_id, age, profession_id) VALUES (?, ?, ?, ?)",
                testUser.id, gender, 45, profession)
        }
        jdbc.update("""
            INSERT INTO patients (creator_id, gender_id, age, profession_id, model_kind, chronic_lung_disease)
            VALUES (?, ?, ?, ?, 'region', 0)
        """.trimIndent(), testUser.id, gender, 45, profession)

        val invalid = listOf<Triple<String?, Int?, Int?>>(
            Triple("city", null, 0), Triple("city", 2020, 0), Triple("region", 2021, 0),
            Triple("other", null, 0), Triple("region", null, null),
            Triple(null, 2021, null), Triple("city", 2017, 2)
        )
        for ((kind, year, lung) in invalid) {
            assertThrows(DataIntegrityViolationException::class.java) {
                jdbc.update("UPDATE patients SET model_kind = ?, model_year = ?, chronic_lung_disease = ? WHERE creator_id = ?",
                    kind, year, lung, testUser.id)
            }
        }
        assertEquals(1, jdbc.update("UPDATE patients SET model_kind = 'city', model_year = 2017, chronic_lung_disease = 1 WHERE creator_id = ?", testUser.id))
        assertEquals(profession, jdbc.queryForObject("SELECT profession_id FROM patients WHERE creator_id = ?", Long::class.java, testUser.id))
    }

    private val columns = listOf(
        "model_kind" to "modelKind", "year" to "year", "chronic_lung_disease" to "chronicLungDisease",
        "gender" to "gender", "age" to "age", "height" to "height", "weight" to "weight",
        "hip_measurement" to "hipMeasurement", "alcohol" to "alcohol", "profession" to "profession",
        "region" to "region", "glucose" to "glucose", "cholesterol" to "cholesterol",
        "hdl_cholesterol" to "hdlCholesterol", "ldl_cholesterol" to "ldlCholesterol", "triglycerides" to "triglycerides"
    )

    private fun csv(body: Map<String, Any?>): String =
        columns.joinToString(",") { it.first } + "\n" + columns.joinToString(",") { (_, json) -> body[json]?.toString().orEmpty() }
}
