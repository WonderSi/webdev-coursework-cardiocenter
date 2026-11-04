package com.medical.cardio.config

import com.medical.cardio.security.AuthCookieService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Import
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

class AuthCookieProfileTest {
    @TestConfiguration(proxyBeanMethods = false)
    @Import(AuthCookieService::class, RefreshTokenConfig::class)
    class CookieConfiguration

    @ParameterizedTest
    @CsvSource("prod,true", "prod;local,false", "dev,false")
    fun `profiles select secure cookie policy`(profiles: String, secure: Boolean) {
        ApplicationContextRunner()
            .withInitializer(ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=${profiles.replace(';', ',')}")
            .withBean(PropertySourcesPlaceholderConfigurer::class.java)
            .withUserConfiguration(CookieConfiguration::class.java)
            .run { context ->
                assertNull(context.startupFailure)
                val cookies = context.getBean(AuthCookieService::class.java)
                assertEquals(secure, cookies.accessToken("access-token", false).isSecure)
                assertEquals(secure, cookies.refreshToken("refresh-token", false).isSecure)
            }
    }
}
