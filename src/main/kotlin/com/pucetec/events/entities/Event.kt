package com.pucetec.events.entities

import jakarta.persistence.*

@Entity
@Table(name = "events")
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val venue: String,

    @Column(nullable = false)
    val totalTickets: Int,

    @Column(nullable = false)
    var availableTickets: Int
)
