import api from './api';

export const authService = {
    // POST /api/v1/auth/login
    login: async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        return response.data; // Thường trả về { token, user }
    },

    // GET /api/v1/auth/me
    getMe: async () => {
        const response = await api.get('/auth/me');
        return response.data; // Trả về thông tin user bao gồm Avatar
    },

    // Đăng ký (nếu Backend đã có endpoint)
    register: async (userData) => {
        const response = await api.post('/auth/register', userData);
        return response.data;
    }
};