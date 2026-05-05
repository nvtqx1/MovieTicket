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

// Named export for backward compatibility
export const theaterService = {
    getAll: getTheaters,
};