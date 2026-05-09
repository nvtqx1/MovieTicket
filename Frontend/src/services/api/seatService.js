import api from './api';

/**
 * POST /api/v1/admin/seats/generate (Admin only)
 * Generate seat matrix for a showtime
 * 
 * @param {{ showtimeId, rows, cols, seatTypes? }} payload
 * @returns GenerateSeatResponse
 */
export const generateSeatMatrix = async (payload) => {
    const response = await api.post('/admin/seats/matrix/generate', payload);
    return response.data;
};

/**
 * GET /api/v1/showtimes/{showtimeId}/seats
 * Get all seats for a showtime (for seat map rendering)
 * 
 * @returns SeatResponse[]: [{ id, seatNumber, seatType, isReserved, basePrice, finalPrice }]
 */
export const getSeatsByShowtime = async (showtimeId) => {
    const response = await api.get(`/showtimes/${showtimeId}/seats`);
    return response.data;
};

export const deleteSeatsByShowtime = async (showtimeId) => {
    const response = await api.delete(`/admin/seats/showtime/${showtimeId}`);
    return response.data;
};