package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.CreateShowtimeRequest;
import com.ticketrush.backend.dto.ShowtimeResponse;
import com.ticketrush.backend.entity.Movie;
import com.ticketrush.backend.entity.Room;
import com.ticketrush.backend.entity.Seat;
import com.ticketrush.backend.entity.SeatType;
import com.ticketrush.backend.entity.Showtime;
import com.ticketrush.backend.repository.MovieRepository;
import com.ticketrush.backend.repository.RoomRepository;
import com.ticketrush.backend.repository.SeatRepository;
import com.ticketrush.backend.repository.SeatTypeRepository;
import com.ticketrush.backend.repository.ShowtimeRepository;
import com.ticketrush.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Service Implementation
 * VỀ LỖ HỔNG 3: Admin API thêm suất chiếu với roomId
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final ShowtimeServiceImpl showtimeService;

    /**
     * Tạo suất chiếu mới
     * VỀ LỖ HỔNG 3: Thay vì theaterId, giờ nhận roomId
     * Logic:
     * 1. Validate phim và phòng
     * 2. Tạo showtime mới (lưu room_id)
     * 3. Tạo tất cả ghế cho suất chiếu này
     * 4. Trả về thông tin suất chiếu
     */
    @Override
    @Transactional
    public ShowtimeResponse createShowtime(CreateShowtimeRequest request) throws Exception {
        try {
            log.info("🎬 Admin tạo suất chiếu mới: phim {}, phòng {}, ngày {}", 
                    request.getMovieId(), request.getRoomId(), request.getShowDate());

            // ========== BƯỚC 1: Validate phim ==========
            Movie movie = movieRepository.findById(request.getMovieId())
                    .orElseThrow(() -> new IllegalArgumentException("❌ Phim không tồn tại với ID: " + request.getMovieId()));
            log.info("✅ Phim: {}", movie.getTitle());

            // ========== BƯỚC 2: Validate phòng (VỀ LỖ HỔNG 3: Validate roomId) ==========
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("❌ Phòng chiếu không tồn tại với ID: " + request.getRoomId()));
            log.info("✅ Phòng: {} (Rạp: {})", room.getName(), room.getTheater().getName());

            // ========== BƯỚC 3: Tạo Showtime mới ==========
            Showtime showtime = new Showtime();
            showtime.setMovie(movie);
            showtime.setRoom(room); // VỀ LỖ HỔNG 3: Lưu room_id thẳng vào Showtime
            showtime.setShowDate(request.getShowDate());
            showtime.setShowTime(request.getShowTime());
            showtime.setPrice(request.getPrice());
            showtime.setIsFlashSale(request.getIsFlashSale() != null ? request.getIsFlashSale() : false);
            
            // Tính tổng ghế từ capacity của phòng
            Integer totalSeats = room.getCapacity();
            showtime.setTotalSeats(totalSeats);
            showtime.setAvailableSeats(totalSeats);

            Showtime savedShowtime = showtimeRepository.save(showtime);
            log.info("✅ Suất chiếu đã tạo: ID = {}", savedShowtime.getId());

            // ========== BƯỚC 4: Tạo tất cả ghế cho suất chiếu ==========
            log.info("🪑 Tạo {} ghế cho suất chiếu", totalSeats);
            List<Seat> seats = new ArrayList<>();

            // Lấy tất cả SeatType có sẵn từ database
            List<SeatType> seatTypes = seatTypeRepository.findAll();
            SeatType normalType = seatTypes.stream().filter(t -> "Normal".equalsIgnoreCase(t.getName())).findFirst().orElse(null);
            SeatType vipType = seatTypes.stream().filter(t -> "VIP".equalsIgnoreCase(t.getName())).findFirst().orElse(null);
            SeatType coupleType = seatTypes.stream().filter(t -> "Couple".equalsIgnoreCase(t.getName())).findFirst().orElse(null);

            // Fallback
            if (normalType == null) { normalType = new SeatType(); normalType.setId(1); normalType.setName("Normal"); normalType.setPriceMultiplier(java.math.BigDecimal.ONE); }
            if (vipType == null) { vipType = normalType; }
            if (coupleType == null) { coupleType = normalType; }

            int rowCount = room.getMatrixRows() != null ? room.getMatrixRows() : (totalSeats + 9) / 10;
            int colCount = room.getMatrixCols() != null ? room.getMatrixCols() : 10;
            int actualTotalSeats = rowCount * colCount;
            
            // Cập nhật lại totalSeats của showtime nếu ma trận khác capacity
            showtime.setTotalSeats(actualTotalSeats);
            showtime.setAvailableSeats(actualTotalSeats);

            for (int row = 0; row < rowCount; row++) {
                char rowChar = (char) ('A' + row);
                boolean isLastRow = (row == rowCount - 1);

                for (int col = 0; col < colCount; col++) {
                    Seat seat = new Seat();
                    seat.setShowtime(savedShowtime);
                    seat.setSeatNumber(String.valueOf(rowChar) + (col + 1));
                    
                    SeatType seatType;
                    if (isLastRow) {
                        seatType = coupleType;
                    } else {
                        // 50/50 Normal and VIP cho các hàng còn lại
                        seatType = (row < rowCount / 2) ? normalType : vipType;
                    }
                    
                    seat.setSeatType(seatType);
                    seat.setIsReserved(false);
                    seats.add(seat);
                }
            }

            seatRepository.saveAll(seats);
            log.info("✅ Tạo {} ghế thành công", seats.size());

            return showtimeService.toResponse(savedShowtime);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi validate: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Lỗi tạo suất chiếu: {}", e.getMessage(), e);
            throw new Exception("❌ Lỗi tạo suất chiếu: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════
    // TASK 1.2: Filtered Showtime List + Delete
    // ═══════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getFilteredShowtimes(Long theaterId, Long movieId, LocalDate date) {
        log.info("📋 Lấy danh sách showtime: theaterId={}, movieId={}, date={}", theaterId, movieId, date);

        List<Showtime> showtimes;

        if (theaterId != null && date != null) {
            showtimes = showtimeRepository.findByRoomTheaterIdAndShowDateOrderByShowTimeAsc(theaterId, date);
        } else if (theaterId != null) {
            // Lấy 7 ngày tới cho rạp
            LocalDate start = date != null ? date : LocalDate.now();
            LocalDate end = start.plusDays(7);
            showtimes = showtimeRepository.findByTheaterAndDateRange(theaterId, start, end);
        } else if (movieId != null) {
            LocalDate fromDate = date != null ? date : LocalDate.now();
            showtimes = showtimeRepository.findUpcomingByMovie(movieId, fromDate, null);
        } else {
            // Lấy tất cả (giới hạn 100)
            showtimes = showtimeRepository.findAll();
            if (showtimes.size() > 100) {
                showtimes = showtimes.subList(0, 100);
            }
        }

        return showtimes.stream()
                .map(showtimeService::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch chiếu không tồn tại"));

        // Kiểm tra có vé nào đã đặt chưa
        List<Seat> seats = seatRepository.findByShowtimeId(showtimeId);
        boolean hasReserved = seats.stream().anyMatch(Seat::getIsReserved);
        if (hasReserved) {
            throw new IllegalArgumentException("Không thể xóa lịch chiếu đã có vé được đặt");
        }

        seatRepository.deleteAll(seats);
        showtimeRepository.delete(showtime);
        log.info("✅ Xóa lịch chiếu {} thành công", showtimeId);
    }
}
