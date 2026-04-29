import React from 'react';
import RegisterForm from '../components/auth/RegisterForm';

const Register = () => {
    return (
        <div className="min-h-screen bg-[#0a0a0a] bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-gray-900 via-black to-black flex flex-col items-center justify-between pt-28 pb-10 px-4">
            {/* Brand Logo */}
            <div className="mb-10 text-center">
                <h2 className="text-2xl font-black text-red-600 tracking-[0.2em] uppercase">TMT CINEMA</h2>
                <p className="text-[10px] text-gray-500 tracking-[0.5em] mt-1">JOIN THE REVOLUTION</p>
            </div>

            {/* Render duy nhất RegisterForm */}
            <div className="w-full flex justify-center">
                <RegisterForm />
            </div>

            {/* Footer */}
            <footer className="w-full max-w-6xl mt-10 pt-6 border-t border-white/5 flex flex-col md:flex-row justify-between items-center gap-4 text-[10px] text-gray-600 uppercase tracking-widest">
                <p>© 2026 TMT CINEMA. All rights reserved.</p>
                <div className="flex gap-6">
                    <a href="#" className="hover:text-gray-300">Hỗ trợ khách hàng</a>
                    <a href="#" className="hover:text-gray-300">Quy định sự kiện</a>
                </div>
            </footer>
        </div>
    );
};

export default Register;
