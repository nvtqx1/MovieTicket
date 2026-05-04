package com.ticketrush.backend.seeder;

import com.ticketrush.backend.entity.*;
import com.ticketrush.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final int TARGET_LOAD_TEST_USERS = 1000;
    private static final int TARGET_LOAD_TEST_SEATS = 10000;
    private static final String LOAD_USER_PREFIX = "loaduser_";
    private static final String LOAD_TEST_MOVIE = "Load Test Movie";
    private static final String LOAD_TEST_THEATER = "Load Test Theater";
    private static final String LOAD_TEST_ROOM = "Load Test Room";
    private static final LocalDate LOAD_TEST_DATE = LocalDate.of(2035, 1, 1);
    private static final LocalTime LOAD_TEST_TIME = LocalTime.of(20, 00);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final SeatRepository seatRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Luong hoat dong khi ung dung khoi dong, se tu dong chay mot lan duy nhat
     * @param args
     */
    @Override
    @Transactional
    public void run(String... args) {
        Role userRole = ensureRole("ROLE_USER");
        SeatType normal = ensureSeatType("NORMAL", BigDecimal.ONE);
        SeatType vip = ensureSeatType("VIP", BigDecimal.valueOf(1.5));
        Showtime showtime = ensureLoadTestShowTime();

        seedLoadTestUsers(userRole);
        seedLoadTestSeats(showtime, normal, vip);
    }

    //Ham kiem tra neu role da ton tai thi tra ve, neu chua thi tao moi va tra ve
    private Role ensureRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
    }

    //Ham kiem tra neu seat type da ton tai thi tra ve, neu chua thi tao moi va tra ve
    private SeatType ensureSeatType(String name, BigDecimal multiplier) {
        return seatTypeRepository.findByName(name)
                .orElseGet(()-> seatTypeRepository.save(SeatType.builder()
                        .name(name)
                        .priceMultiplier(multiplier)
                        .build()));
    }

    private Showtime ensureLoadTestShowTime() {
        Movie movie = movieRepository.findByTitle(LOAD_TEST_MOVIE)
                .orElseGet(() -> {
                    Movie newMovie = new Movie();
                    newMovie.setTitle(LOAD_TEST_MOVIE);
                    newMovie.setGenre("Load Test");
                    newMovie.setReleaseYear(2035);
                    newMovie.setDescription("Synthetic movie for load testing seat locks.");
                    newMovie.setPosterImageUrl("https://example.com/load-test-movie.jpg");
                    return movieRepository.save(newMovie);
                });

        Theater theater = theaterRepository.findByName(LOAD_TEST_THEATER)
                .orElseGet(() -> {
                    Theater newTheater = new Theater();
                    newTheater.setName(LOAD_TEST_THEATER);
                    newTheater.setLocation("Load Test City");
                    newTheater.setCapacity(TARGET_LOAD_TEST_SEATS);
                    return theaterRepository.save(newTheater);
                });

        if (theater.getCapacity() < TARGET_LOAD_TEST_SEATS) {
            theater.setCapacity(TARGET_LOAD_TEST_SEATS);
            theater = theaterRepository.save(theater);
        }

        Theater finalTheater = theater;
        Room room = roomRepository.findByTheaterIdAndName(finalTheater.getId(), LOAD_TEST_ROOM)
                .orElseGet(() -> {
                    Room newRoom = new Room();
                    newRoom.setName(LOAD_TEST_ROOM);
                    newRoom.setCapacity(TARGET_LOAD_TEST_SEATS);
                    newRoom.setTheater(finalTheater);
                    return roomRepository.save(newRoom);
                });

        if (room.getCapacity() < TARGET_LOAD_TEST_SEATS) {
            room.setCapacity(TARGET_LOAD_TEST_SEATS);
            room = roomRepository.save(room);
        }

        Room finalRoom = room;
        Showtime showtime = showtimeRepository
                .findFirstByRoomIdAndShowDateAndShowTime(finalRoom.getId(), LOAD_TEST_DATE, LOAD_TEST_TIME)
                .orElseGet(() -> {
                    Showtime newShowtime = new Showtime();
                    newShowtime.setMovie(movie);
                    newShowtime.setRoom(finalRoom);
                    newShowtime.setShowDate(LOAD_TEST_DATE);
                    newShowtime.setShowTime(LOAD_TEST_TIME);
                    newShowtime.setPrice(BigDecimal.valueOf(100_000));
                    newShowtime.setTotalSeats(TARGET_LOAD_TEST_SEATS);
                    newShowtime.setAvailableSeats(TARGET_LOAD_TEST_SEATS);
                    newShowtime.setIsFlashSale(false);
                    return showtimeRepository.save(newShowtime);
                });

        showtime.setMovie(movie);
        showtime.setRoom(finalRoom);
        showtime.setTotalSeats(TARGET_LOAD_TEST_SEATS);
        showtime.setAvailableSeats(TARGET_LOAD_TEST_SEATS);
        showtime.setIsFlashSale(false);
        return showtimeRepository.save(showtime);
    }

    private void seedLoadTestUsers(Role userRole) {
        long existingLoadUsers = userRepository.countByUserNameStartingWith(LOAD_USER_PREFIX);
        if(existingLoadUsers >= TARGET_LOAD_TEST_USERS) {
            log.info("Load test users already exist: {}", existingLoadUsers);
            return;
        }

        List<User> users = new ArrayList<>();
        String encodedPassword = passwordEncoder.encode("Password123");

        for (int i = 0; i < TARGET_LOAD_TEST_USERS; i++) {
            String userName = LOAD_USER_PREFIX + String.format("%04d", i);
            if (userRepository.existsByUserName(userName)) {
                continue;
            }

            User user = new User();
            user.setUserName(userName);
            user.setEmail(String.format("loadtest%04d@ticketrush.test", i));
            user.setPassword(encodedPassword);
            user.setPhoneNumber(String.format("0901%06d", i));
            user.setDateOfBirth(LocalDate.of(1995 + (i % 15), 1 + (i % 12), 1 + (i % 27)));
            user.setGender(i % 2 == 0 ? "MALE" : "FEMALE");
            user.setRole(userRole);
            users.add(user);
        }

        if (!users.isEmpty()) {
            userRepository.saveAll(users);
        }
        log.info("Seeded {} missing load-test users", users.size());
    }

    private void seedLoadTestSeats(Showtime showtime, SeatType normal, SeatType vip) {
        long existingSeatCount = seatRepository.countByShowtimeId(showtime.getId());
        if (existingSeatCount >= TARGET_LOAD_TEST_SEATS) {
            log.info("Load-test seats already seeded: {}", existingSeatCount);
            return;
        }

        Set<String> existingSeatNumbers = new HashSet<>();
        seatRepository.findByShowtimeId(showtime.getId())
                .forEach(seat -> existingSeatNumbers.add(seat.getSeatNumber()));

        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= 100; row++) {
            for (int col = 1; col <= 100; col++) {
                String seatNumber = String.format("R%03dC%03d", row, col);
                if (existingSeatNumbers.contains(seatNumber)) {
                    continue;
                }

                Seat seat = new Seat();
                seat.setShowtime(showtime);
                seat.setSeatNumber(seatNumber);
                seat.setSeatType(row <= 10 ? vip : normal);
                seat.setIsReserved(false);
                seat.setReservation(null);
                seats.add(seat);
            }
        }

        if (!seats.isEmpty()) {
            seatRepository.saveAll(seats);
        }
        log.info("Seeded {} missing load-test seats", seats.size());
    }
}