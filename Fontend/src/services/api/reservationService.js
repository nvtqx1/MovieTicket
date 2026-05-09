import api from './api';

/**
 * POST /api/v1/reservations/init
 * Create a new reservation with PENDING status
 * 
 * @param {{ showtimeId, seatNumbers, voucherCode? }} payload
 * @returns CreateReservationResponse
 */
export const createReservation = async (payload) => {
    const response = await api.post('/reservations/init', payload);
    return response.data;
};

/**
 * POST /api/v1/reservations/confirm
 * Confirm reservation: update payment, mark seats, generate QR
 * 
 * @param {{ reservationId, paymentMethodId, transactionCode, seatNumbers, notes? }} payload
 * @returns ReservationResponse
 */
export const confirmReservation = async (payload) => {
    const response = await api.post('/reservations/confirm', payload);
    return response.data;
};

/**
 * GET /api/v1/reservations/{reservationId}
 * Get reservation details
 * 
 * @returns ReservationResponse
 */
export const getReservation = async (reservationId) => {
    const response = await api.get(`/reservations/${reservationId}`);
    return response.data;
};

export const getTicket = async (ticketId) => {
    const response = await api.get(`/tickets/${ticketId}`);
    return response.data;
};

/**
 * GET /api/v1/reservations/my-tickets
 * Get current user's tickets
 * 
 * @returns TicketResponse[]
 */
export const getMyTickets = async () => {
    const response = await api.get('/reservations/my-tickets');
    return response.data;
};

/**
 * POST /api/v1/reservations/{reservationId}/cancel
 * Cancel a pending reservation
 */
export const cancelReservation = async (reservationId) => {
    const response = await api.post(`/reservations/${reservationId}/cancel`);
    return response.data;
};

export const cancelTicket = async (ticketId) => {
    const response = await api.post(`/tickets/${ticketId}/cancel`);
    return response.data;
};

/**
 * GET /api/v1/reservations/{reservationId}/ticket-detail
 * Task 2.3: Lấy chi tiết vé đầy đủ (phim, rạp, phòng, dãy, ghế, giờ chiếu, QR)
 * @returns TicketDetailResponse
 */
export const getTicketDetail = async (reservationId) => {
    const response = await api.get(`/reservations/${reservationId}/ticket-detail`);
    return response.data;
};
