package com.medical.cardio.exception

import org.springframework.http.HttpStatus

/** Keep the upstream rejection status without retaining clinical response bodies. */
class MlRetrainRejectedException(val status: HttpStatus) : RuntimeException("ML rejected retraining")
