package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.*;
import com.ticketrush.backend.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service xử lý logic cho Admin Dashboard.
 * Cung cấp các phương thức để lấy dữ liệu thống kê từ database.
 * Sử dụng các query JPQL nâng cao để tính toán hiệu quả.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final ShowtimeRepository showtimeRepository;

    // ========== GENERAL STATISTICS ==========

    /**
     * Lấy tổng quan thống kê chung cho Admin Dashboard.
     * Bao gồm: Tổng users, rạp, phim, suất chiếu, doanh thu, đơn đặt vé, vé bán, doanh thu hôm nay
     *
     * @return GeneralStatsDTO chứa các số liệu chính
     */
    public GeneralStatsDTO getGeneralStatistics() {
        try {
            log.info("📊 Lấy tổng quan thống kê chung");

            Long totalUsers = userRepository.getTotalUserCount();
            Long totalTheaters = theaterRepository.count();
            Long totalMovies = movieRepository.count();
            Long totalShowtimes = showtimeRepository.count();
            BigDecimal totalRevenue = reservationRepository.getTotalRevenue();
            Long totalReservations = reservationRepository.getTotalPaidReservations();
            Long totalTickets = reservationRepository.getTotalTicketsSold();

            // Doanh thu hôm nay
            LocalDate today = LocalDate.now();
            BigDecimal todayRevenue = reservationRepository.getRevenueByDate(today);

            // Đơn đặt vé hôm nay
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
            Long todayReservations = 0L; // TODO: Implement if needed

            return GeneralStatsDTO.builder()
                    .totalUsers(totalUsers)
                    .totalTheaters(totalTheaters)
                    .totalMovies(totalMovies)
                    .totalShowtimes(totalShowtimes)
                    .totalRevenue(totalRevenue)
                    .totalReservations(totalReservations)
                    .totalTicketsSold(totalTickets)
                    .todayRevenue(todayRevenue)
                    .todayReservations(todayReservations)
                    .build();

        } catch (Exception e) {
            log.error("❌ Lỗi lấy tổng quan thống kê: {}", e.getMessage(), e);
            throw new RuntimeException("❌ Lỗi lấy tổng quan thống kê", e);
        }
    }

    // ========== GENDER STATISTICS ==========

    /**
     * Lấy thống kê giới tính của người dùng.
     * Dùng để vẽ pie chart hoặc bar chart trên frontend.
     *
     * @return Danh sách GenderStatDTO chứa giới tính và số lượng
     */
    public List<GenderStatDTO> getGenderStatistics() {
        try {
            log.info("📊 Lấy thống kê giới tính");
            return userRepository.getGenderStatistics();
        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê giới tính: {}", e.getMessage(), e);
            throw new RuntimeException("❌ Lỗi lấy thống kê giới tính", e);
        }
    }

    // ========== AGE GROUP STATISTICS ==========

    /**
     * Lấy thống kê người dùng theo nhóm tuổi.
     * Dùng để hiểu đặc điểm khách hàng theo độ tuổi.
     *
     * @return Danh sách AgeGroupStatDTO chứa nhóm tuổi, số lượng, và phần trăm
     */
    public List<AgeGroupStatDTO> getAgeGroupStatistics() {
        try {
            log.info("📊 Lấy thống kê nhóm tuổi");
            return userRepository.getAgeGroupStatistics();
        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê nhóm tuổi: {}", e.getMessage(), e);
            throw new RuntimeException("❌ Lỗi lấy thống kê nhóm tuổi", e);
        }
    }

    // ========== MOVIE REVENUE STATISTICS ==========

    /**
     * Lấy thống kê doanh thu theo phim.
     * JOIN: Movie -> Showtime -> Reservation
     * Tính: tổng doanh thu, số suất chiếu, số vé bán
     *
     * @return Danh sách MovieRevenueDTO chứa doanh thu từng phim
     */
    public List<MovieRevenueDTO> getMovieRevenueStatistics() {
        try {
            log.info("📊 Lấy thống kê doanh thu theo phim");
            return reservationRepository.getMovieRevenueStatistics();
        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê doanh thu phim: {}", e.getMessage(), e);
            throw new RuntimeException("❌ Lỗi lấy thống kê doanh thu phim", e);
        }
    }

    // ========== THEATER REVENUE STATISTICS ==========

    /**
     * Lấy thống kê doanh thu theo rạp.
     * JOIN: Theater -> Showtime -> Reservation
     * Tính: tổng doanh thu, số suất chiếu, số vé bán
     *
     * @return Danh sách TheaterRevenueDTO chứa doanh thu từng rạp
     */
    public List<TheaterRevenueDTO> getTheaterRevenueStatistics() {
        try {
            log.info("📊 Lấy thống kê doanh thu theo rạp");
            return reservationRepository.getTheaterRevenueStatistics();
        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê doanh thu rạp: {}", e.getMessage(), e);
            throw new RuntimeException("❌ Lỗi lấy thống kê doanh thu rạp", e);
        }
    }

    // ========== DAILY REVENUE STATISTICS ==========

    /**
     * Lấy thống kê doanh thu theo ngày trong khoảng thời gian.
     * Dùng để vẽ line chart hoặc bar chart theo thời gian.
     *
     * @param startDate Ngày bắt đầu (inclusive)
     * @param endDate   Ngày kết thúc (exclusive)
     * @return Danh sách DailyRevenueDTO chứa doanh thu mỗi ngày
     */
    public List<DailyRevenueDTO> getDailyRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("📊 Lấy thống kê doanh thu theo ngày từ {} đến {}", startDate, endDate);

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

            return reservationRepository.getDailyRevenueRaw(startDateTime, endDateTime)
                    .stream()
                    .map(row -> new DailyRevenueDTO(
                            row[0],
                            (Number) row[1],
                            (Number) row[2],
                            (Number) row[3]
                    ))
                    .toList();

        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê doanh thu theo ngày: {}", e.getMessage(), e);
            throw new RuntimeException("❌ Lỗi lấy thống kê doanh thu theo ngày", e);
        }
    }

    /**
     * Lấy thống kê doanh thu theo ngày cho tháng hiện tại.
     *
     * @return Danh sách DailyRevenueDTO cho tháng hiện tại
     */
    public List<DailyRevenueDTO> getCurrentMonthDailyRevenue() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

            log.info("📊 Lấy thống kê doanh thu theo ngày cho tháng: {} từ {} đến {}",
                    today.getMonthValue(), firstDayOfMonth, lastDayOfMonth);

            return getDailyRevenueStatistics(firstDayOfMonth, lastDayOfMonth);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy doanh thu tháng hiện tại: {}", e.getMessage(), e);
            throw new RuntimeException("❌ Lỗi lấy doanh thu tháng hiện tại", e);
        }
    }

    /**
     * Lấy thống kê doanh thu của 7 ngày gần nhất.
     *
     * @return Danh sách DailyRevenueDTO cho 7 ngày gần nhất
     */
    public List<DailyRevenueDTO> getLast7DaysRevenue() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(6);

            log.info("📊 Lấy thống kê doanh thu 7 ngày gần nhất từ {} đến {}", sevenDaysAgo, today);

            return getDailyRevenueStatistics(sevenDaysAgo, today);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy doanh thu 7 ngày gần nhất: {}", e.getMessage(), e);
            throw new RuntimeException("❌ Lỗi lấy doanh thu 7 ngày gần nhất", e);
        }
    }

    /**
     * Lấy thống kê doanh thu của 30 ngày gần nhất.
     *
     * @return Danh sách DailyRevenueDTO cho 30 ngày gần nhất
     */
    public List<DailyRevenueDTO> getLast30DaysRevenue() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate thirtyDaysAgo = today.minusDays(29);

            log.info("📊 Lấy thống kê doanh thu 30 ngày gần nhất từ {} đến {}", thirtyDaysAgo, today);

            return getDailyRevenueStatistics(thirtyDaysAgo, today);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy doanh thu 30 ngày gần nhất: {}", e.getMessage(), e);
            throw new RuntimeException("❌ Lỗi lấy doanh thu 30 ngày gần nhất", e);
        }
    }
}

