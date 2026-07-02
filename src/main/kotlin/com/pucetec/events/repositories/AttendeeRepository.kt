package com.pucetec.events.repositories

import com.pucetec.events.entities.Attendee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AttendeeRepository : JpaRepository<Attendee, Long>
