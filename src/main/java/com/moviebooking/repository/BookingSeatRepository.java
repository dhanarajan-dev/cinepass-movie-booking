package com.moviebooking.repository;

import com.moviebooking.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    @Query("SELECT bs.seatNumber FROM BookingSeat bs WHERE bs.show.id = :showId")
    List<String> findBookedSeatNumbersByShowId(@Param("showId") Long showId);

    @Query("SELECT bs.seatNumber FROM BookingSeat bs WHERE bs.show.id = :showId AND bs.seatNumber IN :seatNumbers")
    List<String> findConflictingSeats(@Param("showId") Long showId, @Param("seatNumbers") List<String> seatNumbers);

    List<BookingSeat> findByBookingId(Long bookingId);
}
