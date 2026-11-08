package com.medical.cardio.config

import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtAuthFilter
import com.medical.cardio.security.JwtService
import com.medical.cardio.security.ProblemResponseWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class PreviewHealthSecurityConfigTest {

    @TestConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = [
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        JpaRepositoriesAutoConfiguration::class,
        RedisAutoConfiguration::class,
        RedisRepositoriesAutoConfiguration::class
    ])
    @Import(
        SecurityConfig::class,
        PreviewHealthSecurityConfig::class,
        JwtAuthFilter::class,
        ProblemResponseWriter::class
    )
    class HealthTestConfiguration {
        @Bean
        fun adminUserDetailsService(): AdminUserDetailsService = mock()

        @Bean
        fun jwtService(): JwtService = mock()

        @Bean
        fun probeHealthIndicator(): ProbeHealthIndicator = ProbeHealthIndicator()
    }

    class ProbeHealthIndicator : HealthIndicator {
        var down: Boolean = false

        override fun health(): Health = (if (down) Health.down() else Health.up())
            .withDetail("internalAddress", "private-database.example")
            .build()
    }

    @ParameterizedTest
    @ValueSource(strings = ["prod", "prod,preview"])
    fun `health access and details are scoped to preview`(profiles: String) {
        WebApplicationContextRunner()
            .withInitializer(ConfigDataApplicationContextInitializer())
            // Read the real main configuration; test/resources/application.yml shadows it otherwise.
            .withPropertyValues(
                "spring.config.location=file:src/main/resources/",
                "spring.profiles.active=$profiles",
                "management.health.diskspace.enabled=false",
                "JWT_SECRET=dGVzdC1zZWNyZXQta2V5LWZvci1jaS10ZXN0cy1vbmx5LTMyLWJ5dGVz"
            )
            .withUserConfiguration(HealthTestConfiguration::class.java)
            .run { context ->
                context.startupFailure?.let { throw it }
                val preview = profiles.contains("preview")
                assertEquals(preview, context.containsBean("previewHealthSecurityFilterChain"))
                assertEquals(
                    if (preview) "never" else "always",
                    context.environment.getProperty("management.endpoint.health.show-details")
                )
                assertEquals(
                    if (preview) "health" else "health,metrics,info",
                    context.environment.getProperty("management.endpoints.web.exposure.include")
                )

                val mvc = MockMvcBuilders.webAppContextSetup(context.sourceApplicationContext)
                    .apply<DefaultMockMvcBuilder>(springSecurity())
                    .build()

                mvc.perform(get("/actuator/health"))
                    .andExpect(status().`is`(if (preview) 200 else 401))

                for (path in listOf(
                    "/actuator/health/db",
                    "/actuator/health/",
                    "/actuator/metrics",
                    "/actuator/info",
                    "/api/admin/probe"
                )) {
                    mvc.perform(get(path)).andExpect(status().isUnauthorized())
                }
                for (method in listOf(HttpMethod.HEAD, HttpMethod.POST)) {
                    mvc.perform(request(method, "/actuator/health"))
                        .andExpect(status().isUnauthorized())
                }

                if (preview) {
                    mvc.perform(get("/actuator/health"))
                        .andExpect(content().json("""{"status":"UP"}""", JsonCompareMode.STRICT))
                        .andExpect(jsonPath("$.components").doesNotExist())
                        .andExpect(jsonPath("$.details").doesNotExist())

                    context.getBean(ProbeHealthIndicator::class.java).down = true
                    mvc.perform(get("/actuator/health"))
                        .andExpect(status().isServiceUnavailable())
                        .andExpect(content().json("""{"status":"DOWN"}""", JsonCompareMode.STRICT))
                } else {
                    mvc.perform(get("/actuator/health").with(user("monitor")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.components.probe.details.internalAddress")
                            .value("private-database.example"))
                }
            }
    }
}
