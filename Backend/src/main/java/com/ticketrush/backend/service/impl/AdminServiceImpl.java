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
            if (seatTypes.isEmpty()) {
                log.warn("⚠️ Không có SeatType nào trong database");
            }

            // Tạo ghế với pattern hàng A, B, C, ... và loại ghế xen kẽ
            int seatIndex = 0;
            int rowCount = (totalSeats + 9) / 10; // Mỗi hàng 10 ghế
            
            for (int row = 0; row < rowCount; row++) {
                char rowChar = (char) ('A' + row);
                for (int col = 0; col < 10 && seatIndex < totalSeats; col++) {
                    Seat seat = new Seat();
                    seat.setShowtime(savedShowtime);
                    seat.setSeatNumber(String.valueOf(rowChar) + (col + 1));
                    
                    // Xen kẽ các loại ghế: Normal, VIP, Premium
                    SeatType seatType;
                    if (seatTypes.isEmpty()) {
                        // Fallback: Tạo seat type mặc định nếu database trống
                        log.warn("⚠️ Database không có SeatType, xài mặc định");
                        seatType = new SeatType();
                        seatType.setId(1); // Normal seat
                        seatType.setName("Normal");
                        seatType.setPriceMultiplier(java.math.BigDecimal.ONE);
                    } else {
                        // Xen kẽ: ghế vị trí lẻ là Normal (type 0), chẵn là VIP (type 1), ...
                        int typeIndex = col % seatTypes.size();
                        seatType = seatTypes.get(typeIndex);
                    }
                    
                    seat.setSeatType(seatType);
                    seat.setIsReserved(false);
                    seats.add(seat);
                    seatIndex++;
                }
            }

            seatRepository.saveAll(seats);
            log.info("✅ Tạo {} ghế thành công", seats.size());

            // ========== BƯỚC 5: Trả về thông tin suất chiếu ==========
            return showtimeService.toResponse(savedShowtime);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi validate: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Lỗi tạo suất chiếu: {}", e.getMessage(), e);
            throw new Exception("❌ Lỗi tạo suất chiếu: " + e.getMessage(), e);
        }
    }
}

