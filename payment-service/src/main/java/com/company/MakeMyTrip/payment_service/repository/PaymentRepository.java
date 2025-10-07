package com.company.MakeMyTrip.payment_service.repository;

import com.company.MakeMyTrip.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    Optional<Payment>  findByBookingId(Long bookingId);

    Optional<Payment> findByTransactionId(String transactionId);



}
