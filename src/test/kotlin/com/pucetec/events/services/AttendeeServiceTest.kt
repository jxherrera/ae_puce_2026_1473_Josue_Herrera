package com.pucetec.events.services

import com.pucetec.events.dto.AttendeeRequest
import com.pucetec.events.entities.Attendee
import com.pucetec.events.exceptions.BlankFieldException
import com.pucetec.events.repositories.AttendeeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AttendeeServiceTest {

    @Mock
    lateinit var attendeeRepository: AttendeeRepository

    @InjectMocks
    lateinit var attendeeService: AttendeeService

    @Test
    fun `createAttendee should throw BlankFieldException when name is blank`() {
        val request = AttendeeRequest(name = "  ", email = "test@test.com")
        assertThrows<BlankFieldException> {
            attendeeService.createAttendee(request)
        }
    }

    @Test
    fun `createAttendee should throw BlankFieldException when email is blank`() {
        val request = AttendeeRequest(name = "Juan", email = "")
        assertThrows<BlankFieldException> {
            attendeeService.createAttendee(request)
        }
    }

    @Test
    fun `createAttendee should return saved attendee successfully`() {
        val request = AttendeeRequest(name = "Juan", email = "test@test.com")
        val savedEntity = Attendee(id = 1L, name = "Juan", email = "test@test.com")
        
        whenever(attendeeRepository.save(any())).thenReturn(savedEntity)
        
        val response = attendeeService.createAttendee(request)
        
        assertEquals(1L, response.id)
        assertEquals("Juan", response.name)
        assertEquals("test@test.com", response.email)
    }
}
