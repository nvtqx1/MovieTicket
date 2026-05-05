import api from './api';

/**
 * GET /api/v1/movies?page=0&size=10
 * Backend returns Page<MovieResponse>: { content: [...], totalPages, totalElements, ... }
 * 
 * @param {{ page?: number, size?: number, search?: string, genre?: string }} params
 * @returns {{ content: MovieResponse[], totalPages: number, totalElements: number }}
 */
export const getMovies = async (params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.set('page', params.page);
    if (params.size !== undefined) query.set('size', params.size);

    const queryStr = query.toString();
    const path = queryStr ? `/movies?${queryStr}` : '/movies';
    const response = await api.get(path);

    // Backend returns Page<MovieResponse>, extract content array
    const data = response.data;

    // If paginated response, return content array for backward compat
    if (data && Array.isArray(data.content)) {
        return data.content;
    }

    // If it's already an array (fallback)
    if (Array.isArray(data)) {
        return data;
    }

    return [];
};

/**
 * GET /api/v1/movies?page=0&size=10 (returns full page object)
 * Use this when you need pagination info
 */
export const getMoviesPaginated = async (page = 0, size = 10) => {
    const response = await api.get(`/movies?page=${page}&size=${size}`);
    return response.data; // Page<MovieResponse>
};

/**
 * GET /api/v1/movies/{id}
 * @returns MovieResponse: { id, title, description, releaseYear, genre, posterImageUrl }
 */
export const getMovieById = async (id) => {
    const response = await api.get(`/movies/${id}`);
    return response.data;
};

/**
 * GET /api/v1/showtimes?movieId={id}
 * @returns Page<ShowtimeResponse>
 */
export const getShowtimesByMovieId = async (movieId) => {
    const response = await api.get(`/showtimes?movieId=${movieId}&size=50`);
    const data = response.data;

    // Backend returns Page<ShowtimeResponse>, extract content
    if (data && Array.isArray(data.content)) {
        return data.content;
    }
    if (Array.isArray(data)) {
        return data;
    }
    return [];
};

/**
 * POST /api/v1/movies
 * @param {{ title, description, releaseYear, genre, posterImageUrl }} movieData
 * @returns MovieResponse
 */
export const createMovie = async (movieData) => {
    const response = await api.post('/movies', {
        ...movieData,
        releaseYear: Number(movieData.releaseYear),
    });
    return response.data;
};

/**
 * PUT /api/v1/movies/{id}
 * Note: Backend may not have PUT endpoint yet, keeping for future use
 */
export const updateMovie = async (id, movieData) => {
    const response = await api.put(`/movies/${id}`, {
        ...movieData,
        releaseYear: Number(movieData.releaseYear),
    });
    return response.data;
};

/**
 * DELETE /api/v1/movies/{id}
 * Note: Backend may not have DELETE endpoint yet, keeping for future use
 */
export const deleteMovie = async (id) => {
    const response = await api.delete(`/movies/${id}`);
    return response.data;
};
