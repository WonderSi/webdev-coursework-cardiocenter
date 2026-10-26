package com.medical.cardio.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun cardioOpenApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Кардиоцентр — Backend API")
                .version("1.0")
                .description(
                    """
                    API для прохождения опроса, работы с данными пациентов и управления врачами.

                    **Как отправить запрос**

                    1. Откройте нужную операцию и нажмите **Try it out**.
                    2. Заполните поля или JSON запроса и нажмите **Execute**.
                    3. Посмотрите код и тело ответа в разделе **Responses**.

                    **Как войти**

                    Для опроса вход не нужен. Для остальных операций сначала выполните
                    **POST /api/auth/login** со своим email и паролем. При ответе **200**
                    браузер сохранит вход и будет автоматически передавать cookie в следующих запросах.
                    Используйте Swagger на том же адресе Backend. Кнопку **Authorize** заполнять не нужно.

                    **Кому что доступно**

                    - **DOCTOR** — данные пациентов, аналитика и ML-модели.
                    - **SUPER_ADMIN** — те же операции и управление врачами.

                    **401** — войдите снова или проверьте, не заблокирована ли учётная запись.
                    **403** — у вашей роли нет доступа к операции.

                    Для выхода выполните **POST /api/auth/logout**.
                    """.trimIndent()
                )
        )
        .components(
            Components().addSecuritySchemes(
                "cookieAuth",
                SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.COOKIE)
                    .name("access_token")
                    .description(
                        "Сначала выполните POST /api/auth/login. Браузер сохранит cookie " +
                            "и будет отправлять её автоматически. Вручную вводить токен не нужно."
                    )
            )
        )
}
