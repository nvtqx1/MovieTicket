import api from './api';

/**
 * GET /api/v1/theaters
 * @returns TheaterResponse[]: [{ id, name, location, capacity }]
 */
export const getTheaters = async () => {
    const response = await api.get('/theaters');
    return response.data;
};

/**
 * GET /api/v1/theaters/{id}
 * Note: Backend doesn't have this endpoint explicitly, but keeping for future
 */
export const getTheaterById = async (id) => {
    const response = await api.get(`/theaters/${id}`);
    return response.data;
};

/**
 * POST /api/v1/theaters
 * @param {{ name, location, capacity }} theaterData
 * @returns TheaterResponse
 */
export const createTheater = async (theaterData) => {
    const response = await api.post('/theaters', {
        ...theaterData,
        capacity: Number(theaterData.capacity),
    });
    return response.data;
};

/**
 * PUT /api/v1/theaters/{id}
 * Note: Backend may not have this yet
 */
export const updateTheater = async (id, theaterData) => {
    const response = await api.put(`/theaters/${id}`, {
        ...theaterData,
        capacity: Number(theaterData.capacity),
    });
    return response.data;
};

/**
 * DELETE /api/v1/theaters/{id}
 * Note: Backend may not have this yet
 */
export const deleteTheater = async (id) => {
    const response = await api.delete(`/theaters/${id}`);
    return response.data;
};

/**
 * GET /api/v1/theaters/{theaterId}/rooms
 * @returns RoomResponse[]: [{ id, name, capacity }]
 */
export const getRoomsByTheater = async (theaterId) => {
    const response = await api.get(`/theaters/${theaterId}/rooms`);
    return response.data;
};

/**
 * POST /api/v1/theaters/{theaterId}/rooms
 * @param {{ name, capacity }} roomData
 * @returns RoomResponse
 */
export const createRoom = async (theaterId, roomData) => {
    const response = await api.post(`/theaters/${theaterId}/rooms`, roomData);
    return response.data;
};

export const updateRoom = async (theaterId, roomId, roomData) => {
    const response = await api.put(`/theaters/${theaterId}/rooms/${roomId}`, roomData);
    return response.data;
};

export const deleteRoom = async (theaterId, roomId) => {
    const response = await api.delete(`/theaters/${theaterId}/rooms/${roomId}`);
    return response.data;
};

/**
 * GET /api/v1/theaters/{theaterId}/schedule
 * Task 2.2: Lấy lịch chiếu của rạp trong 6 ngày tới, grouped by date
 * @returns { theaterId, theaterName, location, schedule: { "2026-05-07": ShowtimeResponse[] } }
 */
export const getTheaterSchedule = async (theaterId) => {
    const response = await api.get(`/theaters/${theaterId}/schedule`);
    return response.data;
};

// Named export for backward compatibility
export const theaterService = {
    getAll: getTheaters,
};