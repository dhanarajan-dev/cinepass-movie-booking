package com.moviebooking.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @Column(name = "show_time", nullable = false, length = 30)
    private String showTime;

    @Column(name = "screen_name", length = 50)
    private String screenName = "Audi 1";

    @Column(name = "premium_price", nullable = false)
    private Double premiumPrice = 250.00;

    @Column(name = "elite_price", nullable = false)
    private Double elitePrice = 150.00;

    public Show() {
    }

    public Show(Movie movie, Theatre theatre, LocalDate showDate, String showTime, String screenName, Double premiumPrice, Double elitePrice) {
        this.movie = movie;
        this.theatre = theatre;
        this.showDate = showDate;
        this.showTime = showTime;
        this.screenName = screenName;
        this.premiumPrice = premiumPrice;
        this.elitePrice = elitePrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public Double getPremiumPrice() {
        return premiumPrice;
    }

    public void setPremiumPrice(Double premiumPrice) {
        this.premiumPrice = premiumPrice;
    }

    public Double getElitePrice() {
        return elitePrice;
    }

    public void setElitePrice(Double elitePrice) {
        this.elitePrice = elitePrice;
    }
}
