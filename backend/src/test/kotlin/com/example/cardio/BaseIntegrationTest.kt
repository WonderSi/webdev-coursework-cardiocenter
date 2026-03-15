package com.example.cardio

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Базовый класс для всех интеграционных тестов.
 * Наследуй его вместо того чтобы дублировать аннотации везде.
 *
 * Testcontainers автоматически:
 * 1. Поднимает PostgreSQL контейнер перед тестами
 * 2. Flyway накатывает миграции
 * 3. После тестов контейнер останавливается
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
abstract class BaseIntegrationTest