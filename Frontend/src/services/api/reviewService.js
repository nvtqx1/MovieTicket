import api from './api';

export const getReviewsByMovie = async (movieId) => {
    const response = await api.get(`/reviews/movie/${movieId}`);
    return response.data?.data || [];
};

export const createReview = async (payload) => {
    const response = await api.post('/reviews', payload);
    return response.data;
};

export const deleteReview = async (reviewId) => {
    const response = await api.delete(`/reviews/${reviewId}`);
    return response.data;
};
