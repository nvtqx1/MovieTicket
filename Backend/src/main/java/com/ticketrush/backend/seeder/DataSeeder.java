package com.ticketrush.backend.seeder;

import com.ticketrush.backend.entity.*;
import com.ticketrush.backend.entity.enums.ReservationStatus;
import com.ticketrush.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TheaterRepository theaterRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        if (userRepository.count() > 100) {
            System.out.println("Seeder already ran");
            return;
        }

        System.out.println("🚀 Seeding data...");

        // 1. ROLE
        if (!roleRepository.existsByName("USER")) {
            roleRepository.save(
                    Role.builder()
                            .name("USER")
                            .build()
            );
        }

        // 2. SEAT TYPE
        if (!seatTypeRepository.existsByName("NORMAL")) {
            seatTypeRepository.save(
                    SeatType.builder()
                            .name("NORMAL")
                            .priceMultiplier(BigDecimal.ONE)
                            .build()
            );
        }

        if (!seatTypeRepository.existsByName("VIP")) {
            seatTypeRepository.save(
                    SeatType.builder()
                            .name("VIP")
                            .priceMultiplier(BigDecimal.valueOf(1.5))
                            .build()
            );
        }

        // 3. THEATER
        Theater theater = new Theater();
        theater.setName("CGV Ha Noi");
        theater.setLocation("Ha Noi City");
        theater.setCapacity(500);
        theater = theaterRepository.save(theater);

        // 4. ROOM
        Room room = new Room();
        room.setName("Room 1");
        room.setCapacity(100);
        room.setTheater(theater);
        room = roomRepository.save(room);

        // 5. SHOWTIME
        Showtime showtime = new Showtime();
        showtime.setRoom(room);
        showtime.setShowDate(LocalDate.now().plusDays(1));
        showtime.setShowTime(LocalTime.of(18, 30));
        showtime.setPrice(BigDecimal.valueOf(100000));
        showtime.setTotalSeats(100);
        showtime.setAvailableSeats(100);
        showtime.setIsFlashSale(false);
        showtime = showtimeRepository.save(showtime);

        // 6. USERS
        List<User> users = new ArrayList<>();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        for (int i = 1; i <= 200; i++) {
            User u = new User();
            u.setUserName("user_" + i);
            u.setEmail("user" + i + "@gmail.com");
            u.setPassword("Abc12345");
            u.setPhoneNumber("09" + (10000000 + i));
            u.setDateOfBirth(LocalDate.of(2000, 1, 1));
            u.setRole(userRole);
            users.add(u);
        }

        userRepository.saveAll(users);

        // 7. SEATS
        List<Seat> seats = new ArrayList<>();
        SeatType vip = seatTypeRepository.findByName("VIP")
                .orElseThrow(() -> new RuntimeException("VIP not found"));

        SeatType normal = seatTypeRepository.findByName("NORMAL")
                .orElseThrow(() -> new RuntimeException("NORMAL not found"));

        for (int i = 1; i <= 100; i++) {
            Seat seat = new Seat();
            seat.setSeatNumber("A" + i);
            seat.setShowtime(showtime);
            seat.setSeatType(i <= 20 ? vip : normal); // 20 ghế VIP
            seat.setIsReserved(false);
            seats.add(seat);
        }

        seatRepository.saveAll(seats);

        // 8. RESERVATIONS (KHÔNG TRÙNG GHẾ)
        Collections.shuffle(seats);

        int bookingCount = 40;
        List<Reservation> reservations = new ArrayList<>();

        for (int i = 0; i < bookingCount; i++) {
            Seat seat = seats.get(i);

            Reservation r = new Reservation();
            r.setUser(users.get(random.nextInt(users.size())));
            r.setShowtime(showtime);
            r.setStatus(ReservationStatus.LOCKED);
            r.setPaid(true);

            BigDecimal total = showtime.getPrice()
                    .multiply(seat.getSeatType().getPriceMultiplier());

            r.setTotalPrice(total);

            reservations.add(r);

            // gán seat
            seat.setIsReserved(true);
            seat.setReservation(r);
        }

        reservationRepository.saveAll(reservations);
        seatRepository.saveAll(seats);

        // 9. UPDATE AVAILABLE SEATS
        showtime.setAvailableSeats(
                showtime.getTotalSeats() - bookingCount
        );
        showtimeRepository.save(showtime);

        System.out.println("✅ Seeding completed!");
    }
}