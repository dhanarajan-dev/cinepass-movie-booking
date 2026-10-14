package com.moviebooking.repository;

import com.moviebooking.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    List<Show> findByMovieIdAndShowDateOrderByTheatreNameAscShowTimeAsc(Long movieId, LocalDate showDate);
    List<Show> findByMovieId(Long movieId);
    List<Show> findByShowDate(LocalDate showDate);
}
