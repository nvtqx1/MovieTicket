import api from './api';

export const authService = {
    /**
     * POST /api/v1/auth/login
     * @returns {{ token: string, type: string, email: string, roles: string[] }}
     */
    login: async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        return response.data; // JwtResponse: { token, type, email, roles }
    },

    /**
     * GET /api/v1/auth/me
     * @returns User details from JWT
     */
    getMe: async () => {
        const response = await api.get('/auth/me');
        return response.data;
    },

    /**
     * POST /api/v1/auth/register
     * @param {{ userName, email, password, phoneNumber, dateOfBirth, gender }} userData
     */
    register: async (userData) => {
        const response = await api.post('/auth/register', userData);
        return response.data;
    },
};
