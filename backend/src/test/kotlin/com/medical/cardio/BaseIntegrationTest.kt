package com.medical.cardio

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Базовый класс для всех интеграционных тестов.
 * Наследуй его вместо того чтобы дублировать аннотации везде.
 *
 * Testcontainers автоматически:
 * 1. Поднимает PostgreSQL контейнер перед тестами
 * 2. DynamicPropertySource подставляет URL/креды в Spring контекст
 * 3. Flyway накатывает миграции на чистую БД
 * 4. После тестов контейнер останавливается
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
abstract class BaseIntegrationTest {

    companion object {

        /**
         * @Container — Testcontainers управляет жизненным циклом.
         * companion object + @Container = один контейнер на все тесты (быстрее).
         * Если поставить @Container на поле экземпляра — контейнер
         * будет пересоздаваться перед каждым тестовым классом (медленнее).
         */
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("cardio_test")
            withUsername("test_user")
            withPassword("test_pass")
            withReuse(true)
        }

        /**
         * DynamicPropertySource — подставляем реальные порт/хост контейнера
         * в Spring properties ПОСЛЕ того как контейнер поднялся.
         * Это нужно потому что порт назначается динамически (не 5432!).
         */
        @DynamicPropertySource
        @JvmStatic
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}