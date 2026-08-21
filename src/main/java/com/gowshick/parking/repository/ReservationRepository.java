package com.gowshick.parking.repository;

import com.gowshick.parking.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    void save(Reservation reservation);
    Optional<Reservation> findById(String reservationId);
    List<Reservation> findAll();
}