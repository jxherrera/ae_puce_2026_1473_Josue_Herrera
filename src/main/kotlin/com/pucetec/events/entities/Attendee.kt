package com.pucetec.events.entities

import jakarta.persistence.*

@Entity
@Table(name = "attendees")
class Attendee(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val email: String
)
