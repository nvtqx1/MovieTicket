import React, { createContext, useContext, useState, useEffect } from 'react';

// 1. Tạo Context
export const AuthContext = createContext();

// 2. Tạo Provider để bọc ứng dụng
export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [roles, setRoles] = useState([]);

    // Kiểm tra xem user đã đăng nhập từ trước chưa (giữ đăng nhập khi F5)
    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            setUser(parsedUser);
            setRoles(parsedUser.roles || []);
        }
    }, []);

    // Hàm xử lý Đăng nhập
    const login = (jwtResponse) => {
        const authData = {
            token: jwtResponse.token,
            type: jwtResponse.type || "Bearer",
            roles: jwtResponse.roles || [],
            avatar: null,
        };

        setUser(authData);
        setRoles(authData.roles);
        localStorage.setItem('user', JSON.stringify(authData));
        localStorage.setItem('token', authData.token);
        localStorage.setItem('tokenType', authData.type);
        localStorage.setItem('roles', JSON.stringify(authData.roles));
    };

    // Hàm xử lý Đăng xuất
    const logout = () => {
        setUser(null);
        setRoles([]);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        localStorage.removeItem('tokenType');
        localStorage.removeItem('roles');
    };

    return (
        <AuthContext.Provider value={{ user, roles, login, logout, isAuthenticated: Boolean(user) }}>
            {children}
        </AuthContext.Provider>
    );
};

// 3. Custom Hook
export const useAuthContext = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuthContext phải được bọc trong AuthProvider");
    }
    return context;
};
