package com.company.MakeMyTrip.booking_service.repository;


import com.company.MakeMyTrip.booking_service.entity.Inventory;
import com.company.MakeMyTrip.booking_service.enums.BookingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByBookingTypeAndReferenceIdAndTravelDate(
            BookingType bookingType,
            Long referenceId,
            LocalDate travelDate
    );

    @Modifying
    @Query("""
        UPDATE Inventory i
        SET i.reservedQuantity = i.reservedQuantity + :quantity
        WHERE i.id = :inventoryId
          AND i.totalCapacity - i.reservedQuantity - i.confirmedQuantity >= :quantity
        """)
    int reserveCapacity(
            @Param("inventoryId") Long inventoryId,
            @Param("quantity") int quantity
    );
}