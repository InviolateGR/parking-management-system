package com.gowshick.parking.model;

import com.gowshick.parking.enums.PaymentMethod;

import java.time.LocalDateTime;

public class Bill {

    private final String billId;
    private final Ticket ticket;
    private final LocalDateTime exitTime;
    private final double amount;
    private final PaymentMethod paymentMethod;

    public Bill(String billId, Ticket ticket, LocalDateTime exitTime, double amount, PaymentMethod paymentMethod) {
        if (amount < 0) {
            throw new IllegalArgumentException("Bill amount cannot be negative");
        }
        this.billId = billId;
        this.ticket = ticket;
        this.exitTime = exitTime;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public String getBillId() {
        return billId;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    @Override
    public String toString() {
        return "Bill[" + billId + ", ticket=" + ticket.getTicketId() +
               ", amount=" + amount + ", exit=" + exitTime + ", payment=" + paymentMethod + "]";
    }
}