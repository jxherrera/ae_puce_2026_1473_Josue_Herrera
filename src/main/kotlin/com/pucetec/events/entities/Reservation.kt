package com.pucetec.events.entities

import jakarta.persistence.*
import java.time.LocalDateTime

enum class ReservationStatus {
    ACTIVE,
    CANCELLED
}

@Entity
@Table(name = "reservations")
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", nullable = false)
    val attendee: Attendee,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus = ReservationStatus.ACTIVE,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
