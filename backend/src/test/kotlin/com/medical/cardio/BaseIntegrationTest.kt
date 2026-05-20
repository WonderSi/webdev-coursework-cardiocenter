package com.medical.cardio

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

// Базовый класс для всех интеграционных тестов.
// Поднимает реальный PostgreSQL перед запуском тестов и передаёт его URL в Spring.
@SpringBootTest
@ActiveProfiles("test")
abstract class BaseIntegrationTest {

    companion object {
        private const val DB_NAME = "cardio_test"
        private const val DB_USER = "test_user"
        private const val DB_PASS = "test_pass"

        // Порт и имя контейнера используются только в режиме Docker CLI (запасной путь).
        private const val CLI_PORT = 15432
        private const val CLI_CONTAINER = "cardio-test-pg"

        private val dbUrl: String
        private val dbUser: String
        private val dbPass: String

        init {
            // Сначала пробуем Testcontainers — он сам запускает PostgreSQL через docker-java.
            // На Linux/CI это работает из коробки через /var/run/docker.sock.
            //
            // На Windows с Docker Desktop (WSL2) docker-java не может подключиться
            // к демону: прокси Docker Desktop возвращает HTTP 400 вместо реального ответа.
            // В этом случае tryTestcontainers() вернёт null, и мы запустим
            // PostgreSQL через обычный docker.exe (CLI) — он работает нормально.
            val tc = tryTestcontainers()
            if (tc != null) {
                dbUrl = tc.jdbcUrl
                dbUser = tc.username
                dbPass = tc.password
            } else {
                dbUrl = startWithDockerCli()
                dbUser = DB_USER
                dbPass = DB_PASS
            }
        }

        // Пробуем запустить PostgreSQL через Testcontainers (docker-java).
        // Если docker-java не может найти Docker (например, Windows + Docker Desktop WSL2) —
        // поймаем исключение и вернём null, чтобы перейти к запасному варианту.
        private fun tryTestcontainers(): PostgreSQLContainer<Nothing>? = try {
            PostgreSQLContainer<Nothing>("postgres:16-alpine")
                .apply {
                    withDatabaseName(DB_NAME)
                    withUsername(DB_USER)
                    withPassword(DB_PASS)
                    start()
                }
        } catch (_: Exception) {
            null
        }

        // Запасной вариант: запускаем PostgreSQL через вызов docker.exe из командной строки.
        // Используется когда docker-java (Testcontainers) не может подключиться к Docker Desktop.
        private fun startWithDockerCli(): String {
            // Удаляем старый контейнер с тем же именем, если он остался от предыдущего запуска
            ProcessBuilder("docker", "rm", "-f", CLI_CONTAINER)
                .redirectErrorStream(true).start().waitFor()

            // Запускаем новый контейнер с PostgreSQL в фоне
            ProcessBuilder(
                "docker", "run", "-d",
                "--name", CLI_CONTAINER,
                "-e", "POSTGRES_DB=$DB_NAME",
                "-e", "POSTGRES_USER=$DB_USER",
                "-e", "POSTGRES_PASSWORD=$DB_PASS",
                "-p", "$CLI_PORT:5432",
                "postgres:16-alpine"
            ).redirectErrorStream(true).start().waitFor()

            // Ждём до 30 секунд, пока PostgreSQL не будет готов принимать подключения
            var ready = false
            repeat(30) {
                if (ready) return@repeat
                val rc = ProcessBuilder("docker", "exec", CLI_CONTAINER, "pg_isready", "-U", DB_USER)
                    .redirectErrorStream(true).start().waitFor()
                if (rc == 0) ready = true else Thread.sleep(1000)
            }
            check(ready) { "PostgreSQL container did not become ready in time" }

            // Регистрируем удаление контейнера при завершении JVM
            Runtime.getRuntime().addShutdownHook(Thread {
                ProcessBuilder("docker", "rm", "-f", CLI_CONTAINER)
                    .redirectErrorStream(true).start().waitFor()
            })

            return "jdbc:postgresql://localhost:$CLI_PORT/$DB_NAME"
        }

        // Передаём Spring Test актуальный URL базы данных — независимо от того,
        // Testcontainers её поднял или Docker CLI.
        @DynamicPropertySource
        @JvmStatic
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { dbUrl }
            registry.add("spring.datasource.username") { dbUser }
            registry.add("spring.datasource.password") { dbPass }
        }
    }
}
