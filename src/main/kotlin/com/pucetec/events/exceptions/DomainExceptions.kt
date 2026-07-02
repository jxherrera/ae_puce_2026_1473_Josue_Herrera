package com.pucetec.events.exceptions

class BlankFieldException(message: String) : RuntimeException(message)
class InvalidCapacityException(message: String) : RuntimeException(message)
class AttendeeNotFoundException(message: String) : RuntimeException(message)
class EventNotFoundException(message: String) : RuntimeException(message)
class ReservationNotFoundException(message: String) : RuntimeException(message)
class SoldOutException(message: String) : RuntimeException(message)
class ReservationLimitExceededException(message: String) : RuntimeException(message)
class ReservationAlreadyCancelledException(message: String) : RuntimeException(message)

data class ExceptionResponse(
    val message: String?,
    val source: String = "events-api"
)
