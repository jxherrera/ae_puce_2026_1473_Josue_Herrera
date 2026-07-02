package com.pucetec.events.services

import com.pucetec.events.dto.EventRequest
import com.pucetec.events.entities.Event
import com.pucetec.events.exceptions.BlankFieldException
import com.pucetec.events.exceptions.EventNotFoundException
import com.pucetec.events.exceptions.InvalidCapacityException
import com.pucetec.events.repositories.EventRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class EventServiceTest {

    @Mock
    lateinit var eventRepository: EventRepository

    @InjectMocks
    lateinit var eventService: EventService

    @Test
    fun `createEvent should throw BlankFieldException when name is blank`() {
        val request = EventRequest(name = "", venue = "Venue", totalTickets = 100)
        assertThrows<BlankFieldException> {
            eventService.createEvent(request)
        }
    }

    @Test
    fun `createEvent should throw BlankFieldException when venue is blank`() {
        val request = EventRequest(name = "Event", venue = "  ", totalTickets = 100)
        assertThrows<BlankFieldException> {
            eventService.createEvent(request)
        }
    }

    @Test
    fun `createEvent should throw InvalidCapacityException when totalTickets is less than 1`() {
        val request = EventRequest(name = "Event", venue = "Venue", totalTickets = 0)
        assertThrows<InvalidCapacityException> {
            eventService.createEvent(request)
        }
    }

    @Test
    fun `createEvent should return saved event successfully`() {
        val request = EventRequest(name = "Event", venue = "Venue", totalTickets = 100)
        val savedEntity = Event(id = 1L, name = "Event", venue = "Venue", totalTickets = 100, availableTickets = 100)
        
        whenever(eventRepository.save(any())).thenReturn(savedEntity)
        
        val response = eventService.createEvent(request)
        
        assertEquals(1L, response.id)
        assertEquals(100, response.availableTickets)
    }

    @Test
    fun `getAllEvents should return list of events`() {
        val eventList = listOf(Event(id = 1L, name = "Event", venue = "Venue", totalTickets = 100, availableTickets = 100))
        whenever(eventRepository.findAll()).thenReturn(eventList)
        
        val result = eventService.getAllEvents()
        
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    @Test
    fun `getEventById should return event when found`() {
        val event = Event(id = 1L, name = "Event", venue = "Venue", totalTickets = 100, availableTickets = 100)
        whenever(eventRepository.findById(1L)).thenReturn(Optional.of(event))
        
        val response = eventService.getEventById(1L)
        
        assertEquals(1L, response.id)
    }

    @Test
    fun `getEventById should throw EventNotFoundException when not found`() {
        whenever(eventRepository.findById(1L)).thenReturn(Optional.empty())
        
        assertThrows<EventNotFoundException> {
            eventService.getEventById(1L)
        }
    }
}
