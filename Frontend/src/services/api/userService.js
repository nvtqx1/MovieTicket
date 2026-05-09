import api from './api';

export const userService = {
    getAllUsers: async () => {
        try {
            const response = await api.get('/admin/users');
            return response.data;
        } catch (error) {
            throw error;
        }
    },
    /**
     * PUT /api/v1/admin/users/{id}/ban
     * Toggle ban/unban user
     */
    toggleBanUser: async (id) => {
        try {
            const response = await api.put(`/admin/users/${id}/ban`);
            return response.data;
        } catch (error) {
            throw error;
        }
    },
    deleteUser: async (id) => {
        try {
            const response = await api.delete(`/admin/users/${id}`);
            return response.data;
        } catch (error) {
            throw error;
        }
    }
};
