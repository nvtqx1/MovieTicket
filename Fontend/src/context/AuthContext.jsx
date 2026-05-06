import React, { createContext, useContext, useState, useEffect } from 'react';

// 1. Tạo Context
export const AuthContext = createContext();

// 2. Tạo Provider để bọc ứng dụng
export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(() => {
        const storedUser = localStorage.getItem('user');
        return storedUser ? JSON.parse(storedUser) : null;
    });
    const [roles, setRoles] = useState(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            return parsedUser.roles || [];
        }
        return [];
    });

    // Lắng nghe sự kiện auto-logout khi token hết hạn (401)
    useEffect(() => {
        const handleAutoLogout = () => {
            setUser(null);
            setRoles([]);
            localStorage.removeItem('user');
            localStorage.removeItem('token');
            localStorage.removeItem('tokenType');
            localStorage.removeItem('roles');
        };
        window.addEventListener('auth:logout', handleAutoLogout);
        return () => window.removeEventListener('auth:logout', handleAutoLogout);
    }, []);

    // Hàm xử lý Đăng nhập
    const login = (jwtResponse) => {
        const authData = {
            token: jwtResponse.token,
            type: jwtResponse.type || "Bearer",
            email: jwtResponse.email || null,
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
