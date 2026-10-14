package com.moviebooking.service;

import com.moviebooking.model.Show;
import com.moviebooking.repository.BookingSeatRepository;
import com.moviebooking.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ShowService {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    public List<Show> getShowsByMovieAndDate(Long movieId, LocalDate date) {
        return showRepository.findByMovieIdAndShowDateOrderByTheatreNameAscShowTimeAsc(movieId, date);
    }

    public Optional<Show> getShowById(Long id) {
        return showRepository.findById(id);
    }

    public List<String> getBookedSeatsForShow(Long showId) {
        return bookingSeatRepository.findBookedSeatNumbersByShowId(showId);
    }
}
