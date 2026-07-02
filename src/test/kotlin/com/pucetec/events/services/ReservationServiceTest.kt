package com.pucetec.events.services

import com.pucetec.events.dto.ReservationRequest
import com.pucetec.events.entities.Attendee
import com.pucetec.events.entities.Event
import com.pucetec.events.entities.Reservation
import com.pucetec.events.entities.ReservationStatus
import com.pucetec.events.exceptions.*
import com.pucetec.events.repositories.AttendeeRepository
import com.pucetec.events.repositories.EventRepository
import com.pucetec.events.repositories.ReservationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReservationServiceTest {

    @Mock
    lateinit var reservationRepository: ReservationRepository

    @Mock
    lateinit var attendeeRepository: AttendeeRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @InjectMocks
    lateinit var reservationService: ReservationService

    @Test
    fun `createReservation should throw AttendeeNotFoundException if attendee not found`() {
        val request = ReservationRequest(attendeeId = 1L, eventId = 1L)
        whenever(attendeeRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<AttendeeNotFoundException> {
            reservationService.createReservation(request)
        }
    }

    @Test
    fun `createReservation should throw EventNotFoundException if event not found`() {
        val request = ReservationRequest(attendeeId = 1L, eventId = 1L)
        val attendee = Attendee(id = 1L, name = "Juan", email = "test@test.com")
        whenever(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee))
        whenever(eventRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<EventNotFoundException> {
            reservationService.createReservation(request)
        }
    }

    @Test
    fun `createReservation should throw SoldOutException if availableTickets is zero`() {
        val request = ReservationRequest(attendeeId = 1L, eventId = 1L)
        val attendee = Attendee(id = 1L, name = "Juan", email = "test@test.com")
        val event = Event(id = 1L, name = "Event", venue = "Venue", totalTickets = 100, availableTickets = 0)
        
        whenever(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee))
        whenever(eventRepository.findById(1L)).thenReturn(Optional.of(event))

        assertThrows<SoldOutException> {
            reservationService.createReservation(request)
        }
    }

    @Test
    fun `createReservation should throw ReservationLimitExceededException if user has 4 active reservations`() {
        val request = ReservationRequest(attendeeId = 1L, eventId = 1L)
        val attendee = Attendee(id = 1L, name = "Juan", email = "test@test.com")
        val event = Event(id = 1L, name = "Event", venue = "Venue", totalTickets = 100, availableTickets = 10)
        
        whenever(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee))
        whenever(eventRepository.findById(1L)).thenReturn(Optional.of(event))
        whenever(reservationRepository.countByAttendeeIdAndStatus(1L, ReservationStatus.ACTIVE)).thenReturn(4)

        assertThrows<ReservationLimitExceededException> {
            reservationService.createReservation(request)
        }
    }

    @Test
    fun `createReservation should successfully save reservation and decrease available tickets`() {
        val request = ReservationRequest(attendeeId = 1L, eventId = 1L)
        val attendee = Attendee(id = 1L, name = "Juan", email = "test@test.com")
        val event = Event(id = 1L, name = "Event", venue = "Venue", totalTickets = 100, availableTickets = 10)
        
        val reservation = Reservation(id = 1L, attendee = attendee, event = event, status = ReservationStatus.ACTIVE, createdAt = LocalDateTime.now())
        
        whenever(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee))
        whenever(eventRepository.findById(1L)).thenReturn(Optional.of(event))
        whenever(reservationRepository.countByAttendeeIdAndStatus(1L, ReservationStatus.ACTIVE)).thenReturn(0)
        whenever(reservationRepository.save(any())).thenReturn(reservation)

        val response = reservationService.createReservation(request)

        assertEquals(1L, response.id)
        assertEquals(9, event.availableTickets) // Checked that tickets decreased
    }

    @Test
    fun `cancelReservation should throw ReservationNotFoundException if not found`() {
        whenever(reservationRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ReservationNotFoundException> {
            reservationService.cancelReservation(1L)
        }
    }

    @Test
    fun `cancelReservation should throw ReservationAlreadyCancelledException if already cancelled`() {
        val attendee = Attendee(id = 1L, name = "Juan", email = "test@test.com")
        val event = Event(id = 1L, name = "Event", venue = "Venue", totalTickets = 100, availableTickets = 10)
        val reservation = Reservation(id = 1L, attendee = attendee, event = event, status = ReservationStatus.CANCELLED, createdAt = LocalDateTime.now())
        
        whenever(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation))

        assertThrows<ReservationAlreadyCancelledException> {
            reservationService.cancelReservation(1L)
        }
    }

    @Test
    fun `cancelReservation should successfully cancel and increase available tickets`() {
        val attendee = Attendee(id = 1L, name = "Juan", email = "test@test.com")
        val event = Event(id = 1L, name = "Event", venue = "Venue", totalTickets = 100, availableTickets = 10)
        val reservation = Reservation(id = 1L, attendee = attendee, event = event, status = ReservationStatus.ACTIVE, createdAt = LocalDateTime.now())
        
        whenever(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation))

        reservationService.cancelReservation(1L)

        assertEquals(ReservationStatus.CANCELLED, reservation.status)
        assertEquals(11, event.availableTickets) // Checked that tickets increased
    }

    @Test
    fun `getAllReservations should return list`() {
        val attendee = Attendee(id = 1L, name = "Juan", email = "test@test.com")
        val event = Event(id = 1L, name = "Event", venue = "Venue", totalTickets = 100, availableTickets = 10)
        val reservation = Reservation(id = 1L, attendee = attendee, event = event, status = ReservationStatus.ACTIVE, createdAt = LocalDateTime.now())
        
        whenever(reservationRepository.findAll()).thenReturn(listOf(reservation))

        val result = reservationService.getAllReservations()
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }
}
