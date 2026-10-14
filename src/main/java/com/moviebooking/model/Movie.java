package com.moviebooking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String genre;

    @Column(nullable = false, length = 50)
    private String duration;

    @Column(nullable = false)
    private Double rating;

    @Column(name = "poster_url", nullable = false, length = 600)
    private String posterUrl;

    @Column(name = "banner_url", length = 600)
    private String bannerUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String language;

    @Column(name = "release_date", length = 50)
    private String releaseDate;

    @Column(length = 100)
    private String director;

    @Column(name = "movie_cast", length = 300)
    private String cast;

    @Column(name = "is_recommended", nullable = false)
    private boolean isRecommended = true;

    public Movie() {
    }

    public Movie(String title, String genre, String duration, Double rating, String posterUrl, String bannerUrl,
                 String description, String language, String releaseDate, String director, String cast, boolean isRecommended) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.bannerUrl = bannerUrl;
        this.description = description;
        this.language = language;
        this.releaseDate = releaseDate;
        this.director = director;
        this.cast = cast;
        this.isRecommended = isRecommended;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public boolean isRecommended() {
        return isRecommended;
    }

    public void setRecommended(boolean recommended) {
        isRecommended = recommended;
    }
}
