import React, { createContext, useContext, useState, useEffect } from 'react';

//Chưa xong

// 1. Tạo Context
export const AuthContext = createContext();

// 2. Tạo Provider để bọc ứng dụng
export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);

    // Kiểm tra xem user đã đăng nhập từ trước chưa (giữ đăng nhập khi F5)
    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            setUser(JSON.parse(storedUser));
        }
    }, []);

    // Hàm xử lý Đăng nhập
    const login = (userData) => {
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData)); // Lưu vào LocalStorage
    };

    // Hàm xử lý Đăng xuất
    const logout = () => {
        setUser(null);
        localStorage.removeItem('user'); // Xóa khỏi LocalStorage
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

// 3. Custom Hook (Khớp đúng với import trong Navbar của bạn)
export const useAuthContext = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuthContext phải được bọc trong AuthProvider");
    }
    return context;
};