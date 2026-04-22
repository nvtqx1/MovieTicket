import React, { useState } from 'react';
import LoginForm from '../components/auth/LoginForm';
import RegisterForm from '../components/auth/RegisterForm';

const Login = () => {
    const [isLogin, setIsLogin] = useState(true);

    return (
        <div className="min-h-screen bg-[#0a0a0a] bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-gray-900 via-black to-black flex flex-col items-center justify-between py-10 px-4">
            {/* Brand Logo */}
            <div className="mb-10">
                <h2 className="text-2xl font-black text-red-600 tracking-[0.2em] uppercase">Lumière Noir</h2>
            </div>

            {/* Logic chuyển đổi giữa Login và Register */}
            <div className="w-full flex justify-center">
                {isLogin ? (
                    <LoginForm onSwitch={() => setIsLogin(false)} />
                ) : (
                    <div className="text-white text-center">
                        <RegisterForm onSwitch={() => setIsLogin(true)} />
                    </div>
                )}
            </div>

            {/* Footer */}
            <footer className="w-full max-w-6xl mt-10 pt-6 border-t border-white/5 flex flex-col md:flex-row justify-between items-center gap-4 text-[10px] text-gray-600 uppercase tracking-widest">
                <p>© 2026 TMT CINEMA. All rights reserved.</p>
                <div className="flex gap-6">
                    <a href="#" className="hover:text-gray-300">Chính sách bảo mật</a>
                    <a href="#" className="hover:text-gray-300">Điều khoản dịch vụ</a>
                </div>
            </footer>
        </div>
    );
};

export default Login;