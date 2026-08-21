package com.gowshick.parking.repository;

import com.gowshick.parking.model.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    void save(Ticket ticket);
    Optional<Ticket> findById(String ticketId);
    List<Ticket> findAll();
}