package com.gowshick.parking.repository;

import com.gowshick.parking.model.Bill;

import java.util.List;
import java.util.Optional;

public interface BillRepository {
    void save(Bill bill);
    Optional<Bill> findById(String billId);
    List<Bill> findAll();
}