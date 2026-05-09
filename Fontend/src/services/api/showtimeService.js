import api from './api';

/**
 * GET /api/v1/showtimes?movieId=&theaterId=&showDate=&page=0&size=10
 * @returns ShowtimeResponse[]: extracted from Page<ShowtimeResponse>
 * 
 * ShowtimeResponse fields: 
 *   id, showDate, showTime, price, totalSeats, availableSeats, isFlashSale, roomName,
 *   movie: { id, title, posterImageUrl, genre },
 *   theater: { id, name, location }
 */
export const getShowtimes = async (params = {}) => {
    const query = new URLSearchParams();
    if (params.movieId) query.set('movieId', params.movieId);
    if (params.theaterId) query.set('theaterId', params.theaterId);
    if (params.showDate) query.set('showDate', params.showDate);
    if (params.date) query.set('date', params.date);
    query.set('page', params.page || 0);
    query.set('size', params.size || 50);

    const response = await api.get(`/showtimes?${query.toString()}`);
    const data = response.data;

    if (data && Array.isArray(data.content)) {
        return data.content;
    }
    if (Array.isArray(data)) {
        return data;
    }
    return [];
};

/**
 * GET /api/v1/showtimes/{id}
 * @returns ShowtimeResponse
 */
export const getShowtimeById = async (id) => {
    const response = await api.get(`/showtimes/${id}`);
    return response.data;
};

/**
 * POST /api/v1/admin/showtimes (Admin only)
 * Note: Endpoint is AdminShowtimeController
 */
export const createShowtime = async (showtimeData) => {
    const response = await api.post('/admin/showtimes', showtimeData);
    return response.data;
};

/**
 * PUT /api/v1/admin/showtimes/{id} (Admin only)
 */
export const updateShowtime = async (id, showtimeData) => {
    const response = await api.put(`/admin/showtimes/${id}`, showtimeData);
    return response.data;
};

/**
 * DELETE /api/v1/admin/showtimes/{id} (Admin only)
 */
export const deleteShowtime = async (id) => {
    const response = await api.delete(`/admin/showtimes/${id}`);
    return response.data;
};

/**
 * GET /api/v1/admin/showtimes?theaterId=&movieId=&date= (Admin only)
 * Task 1.2: Filtered showtime list
 */
export const getAdminShowtimes = async ({ theaterId, movieId, date } = {}) => {
    const query = new URLSearchParams();
    if (theaterId) query.set('theaterId', theaterId);
    if (movieId) query.set('movieId', movieId);
    if (date) query.set('date', date);
    const response = await api.get(`/admin/showtimes?${query.toString()}`);
    return Array.isArray(response.data) ? response.data : [];
};
