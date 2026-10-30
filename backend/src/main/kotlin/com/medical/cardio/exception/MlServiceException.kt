package com.medical.cardio.exception

class MlServiceException(cause: Throwable) :
    RuntimeException("Ошибка обращения к ML-сервису", cause)
