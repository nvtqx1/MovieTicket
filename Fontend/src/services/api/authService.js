import api from './api';

const USE_MOCK_AUTH = true;

const mockAccounts = [
    {
        email: "admin@ticketrush.com",
        password: "Admin@123",
        response: {
            token: "mock-admin-jwt-token",
            type: "Bearer",
            roles: ["ROLE_ADMIN"],
        },
    },
    {
        email: "user@gmail.com",
        password: "User@123",
        response: {
            token: "mock-user-jwt-token",
            type: "Bearer",
            roles: ["ROLE_USER"],
        },
    },
];

export const authService = {
    // POST /api/v1/auth/login
    login: async (email, password) => {
        if (USE_MOCK_AUTH) {
            await new Promise((resolve) => setTimeout(resolve, 800));

            const account = mockAccounts.find(
                (item) => item.email === email && item.password === password
            );

            if (!account) {
                throw new Error("Email hoặc mật khẩu không chính xác");
            }

            return account.response;
        }

        const response = await api.post('/auth/login', { email, password });
        return response.data; // JwtResponse: { token, type, roles }
    },

    // GET /api/v1/auth/me
    getMe: async () => {
        const response = await api.get('/auth/me');
        return response.data; // Trả về thông tin user bao gồm Avatar
    },

    // Đăng ký (nếu Backend đã có endpoint)
    register: async (userData) => {
        if (USE_MOCK_AUTH) {
            await new Promise((resolve) => setTimeout(resolve, 800));
            console.log("Mock POST /v1/auth/register:", userData);
            return {
                userName: userData.userName,
                email: userData.email,
                phoneNumber: userData.phoneNumber,
            };
        }

        const response = await api.post('/auth/register', userData);
        return response.data;
    }
};
