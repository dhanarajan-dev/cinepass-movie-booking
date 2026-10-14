package com.moviebooking.config;

import com.moviebooking.model.Movie;
import com.moviebooking.model.Show;
import com.moviebooking.model.Theatre;
import com.moviebooking.model.User;
import com.moviebooking.repository.MovieRepository;
import com.moviebooking.repository.ShowRepository;
import com.moviebooking.repository.TheatreRepository;
import com.moviebooking.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default user if not present
        if (userRepository.count() == 0) {
            User demoUser = new User(
                    "Dhanarajan",
                    "dhanaraj082001@gmail.com",
                    BCrypt.hashpw("Password@123", BCrypt.gensalt(10)),
                    "9876543210"
            );
            userRepository.save(demoUser);
        }

        // Initialize Movies if not present
        if (movieRepository.count() == 0) {
            List<Movie> movies = List.of(
                    new Movie(
                            "Dune: Part Two",
                            "Sci-Fi, Action, Adventure",
                            "2h 46m",
                            8.8,
                            "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                            "https://image.tmdb.org/t/p/original/xOMo8BRK7PfcJv9JCnx7s52002f.jpg",
                            "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the universe, he endeavors to prevent a terrible future.",
                            "English",
                            "March 2024",
                            "Denis Villeneuve",
                            "Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem",
                            true
                    ),
                    new Movie(
                            "Oppenheimer",
                            "Biography, Drama, History",
                            "3h 00m",
                            8.9,
                            "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                            "https://image.tmdb.org/t/p/original/rM5Y09xC99JEapXKN5aq0ceZaqq.jpg",
                            "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.",
                            "English",
                            "July 2023",
                            "Christopher Nolan",
                            "Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr.",
                            true
                    ),
                    new Movie(
                            "Spider-Man: Across the Spider-Verse",
                            "Animation, Action, Adventure",
                            "2h 20m",
                            8.7,
                            "https://image.tmdb.org/t/p/w500/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg",
                            "https://image.tmdb.org/t/p/original/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg",
                            "Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence. When the heroes clash on how to handle a new threat, Miles must redefine what it means to be a hero.",
                            "English",
                            "June 2023",
                            "Joaquim Dos Santos, Kemp Powers",
                            "Shameik Moore, Hailee Steinfeld, Oscar Isaac, Daniel Kaluuya",
                            true
                    ),
                    new Movie(
                            "Interstellar",
                            "Sci-Fi, Drama, Adventure",
                            "2h 49m",
                            8.7,
                            "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                            "https://image.tmdb.org/t/p/original/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
                            "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft, along with a team of researchers, to find a new planet for humans.",
                            "English",
                            "November 2014",
                            "Christopher Nolan",
                            "Matthew McConaughey, Anne Hathaway, Jessica Chastain, Michael Caine",
                            true
                    ),
                    new Movie(
                            "Kalki 2898 AD",
                            "Action, Sci-Fi, Mythological",
                            "3h 01m",
                            8.2,
                            "https://image.tmdb.org/t/p/w500/9PqW31GugUDKFzgvMyrRS5a2qH4.jpg",
                            "https://image.tmdb.org/t/p/original/w4pB4u53Xp4p9h2n8jXk7gq3f2v.jpg",
                            "A modern-day avatar of Vishnu, a Hindu god, who is believed to have descended to the earth to protect the world from evil forces in a post-apocalyptic future.",
                            "Telugu, Hindi, Tamil",
                            "June 2024",
                            "Nag Ashwin",
                            "Prabhas, Amitabh Bachchan, Kamal Haasan, Deepika Padukone",
                            true
                    ),
                    new Movie(
                            "Deadpool & Wolverine",
                            "Action, Comedy, Sci-Fi",
                            "2h 08m",
                            8.0,
                            "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
                            "https://image.tmdb.org/t/p/original/9l1eZiJHmhr5jYmsV42QyeFuaMm.jpg",
                            "A listless Wade Wilson toils away in civilian life with his days as the morally flexible mercenary, Deadpool, behind him. But when his homeworld faces an existential threat, Wade must reluctantly suit-up again with an even more reluctant Wolverine.",
                            "English",
                            "July 2024",
                            "Shawn Levy",
                            "Ryan Reynolds, Hugh Jackman, Emma Corrin, Matthew Macfadyen",
                            false
                    ),
                    new Movie(
                            "The Dark Knight",
                            "Action, Crime, Drama",
                            "2h 32m",
                            9.0,
                            "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
                            "https://image.tmdb.org/t/p/original/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg",
                            "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                            "English",
                            "July 2008",
                            "Christopher Nolan",
                            "Christian Bale, Heath Ledger, Aaron Eckhart, Michael Caine",
                            false
                    ),
                    new Movie(
                            "Inception",
                            "Action, Sci-Fi, Thriller",
                            "2h 28m",
                            8.8,
                            "https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg",
                            "https://image.tmdb.org/t/p/original/s3TBrRGB1iav7gFOCNx3H31MoES.jpg",
                            "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project and his team to disaster.",
                            "English",
                            "July 2010",
                            "Christopher Nolan",
                            "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page, Tom Hardy",
                            false
                    )
            );
            movieRepository.saveAll(movies);
        }

        // Initialize Theatres if not present
        if (theatreRepository.count() == 0) {
            List<Theatre> theatres = List.of(
                    new Theatre("PVR: Forum South Mall", "Kanakapura Road, Bengaluru", "Bengaluru", "Dolby Atmos 7.1, 4K RGB Laser, Recliner Seats"),
                    new Theatre("INOX: Phoenix Marketcity", "Whitefield Main Road, Bengaluru", "Bengaluru", "IMAX with Laser, Dolby Atmos, Gourmet Dining"),
                    new Theatre("Cinepolis: Nexus Mall", "Koramangala 7th Block, Bengaluru", "Bengaluru", "VIP Luxury Lounge, 4DX Motion Seats, Macro XE"),
                    new Theatre("PVR Luxe: Vega City", "Bannerghatta Main Road, Bengaluru", "Bengaluru", "Gold Class Luxury, Dolby Surround, In-Seat Service")
            );
            theatreRepository.saveAll(theatres);
        }

        // Initialize Shows for next 7 days if not present
        if (showRepository.count() == 0) {
            List<Movie> allMovies = movieRepository.findAll();
            List<Theatre> allTheatres = theatreRepository.findAll();

            String[] showTimes = {"10:00 AM", "01:30 PM", "05:00 PM", "08:30 PM", "10:45 PM"};
            String[] screens = {"Audi 1 - 4K Laser", "Audi 2 - IMAX", "Audi 3 - Dolby Atmos", "Audi 4 - Luxe"};

            List<Show> showList = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (Movie movie : allMovies) {
                for (int day = 0; day < 7; day++) {
                    LocalDate showDate = today.plusDays(day);

                    // Alternate theatres for diversity
                    for (int tIdx = 0; tIdx < allTheatres.size(); tIdx++) {
                        Theatre theatre = allTheatres.get(tIdx);

                        // Schedule 2-3 showtimes per theatre per movie
                        int timeOffset = (movie.getId().intValue() + tIdx) % showTimes.length;
                        int timeOffset2 = (timeOffset + 2) % showTimes.length;

                        Double premium = 280.0 + (tIdx * 20.0);
                        Double elite = 180.0 + (tIdx * 10.0);

                        showList.add(new Show(movie, theatre, showDate, showTimes[timeOffset], screens[tIdx % screens.length], premium, elite));
                        showList.add(new Show(movie, theatre, showDate, showTimes[timeOffset2], screens[(tIdx + 1) % screens.length], premium, elite));
                    }
                }
            }

            showRepository.saveAll(showList);
        }
    }
}
