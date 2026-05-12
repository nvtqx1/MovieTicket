import React, { useState } from 'react';
import { Mail, Lock, Loader2 } from 'lucide-react';
import InputField from '../common/InputField';
import SocialButton from '../common/SocialButton';

import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '../../context/AuthContext';

import { validateEmail } from '../../utils/validation';
import { authService } from '../../services/api/authService';

const LoginForm = ({ onSwitch }) => {
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [errors, setErrors] = useState({});

    const [isLoading, setIsLoading] = useState(false);
    const [loginError, setLoginError] = useState("");

    const navigate = useNavigate();
    const { login } = useAuthContext(); // Lấy hàm login từ Context

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});
        setLoginError("");

        let newErrors = {};

        // Validate Email
        if (!formData.email) {
            newErrors.email = "Vui lòng nhập email";
        } else if (!validateEmail(formData.email)) {
            newErrors.email = "Định dạng email không hợp lệ";
        }

        // Validate Password
        if (!formData.password) {
            newErrors.password = "Vui lòng nhập mật khẩu";
        }

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setIsLoading(true);

        try {
            const jwtResponse = await authService.login(formData.email, formData.password);
            login(jwtResponse);

            const roles = jwtResponse.roles || [];
            if (roles.includes("ROLE_ADMIN") || roles.includes("ADMIN")) {
                navigate('/admin/dashboard');
            } else {
                navigate('/');
            }
        } catch (err) {
            setLoginError(err.message || "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full max-w-md p-10 bg-[#1a1a1a]/90 backdrop-blur-md rounded-lg shadow-2xl border border-white/5">
            <div className="text-center space-y-2 mb-8">
                <h1 className="text-3xl font-black text-white uppercase tracking-tighter">ĐĂNG NHẬP</h1>
                <p className="text-gray-400 text-sm">Sẵn sàng săn vé tại TMT CINEMA.</p>
            </div>

            {loginError && (
                <div className="mb-4 p-3 bg-red-500/10 border border-red-500/50 rounded text-red-500 text-xs text-center uppercase tracking-widest">
                    {loginError}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
                <div className="text-left">
                    <InputField
                        label="EMAIL"
                        id="email"
                        type="email"
                        icon={Mail}
                        placeholder="example@email.com"
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        error={errors.email}
                    />
                </div>

                <div className="space-y-1 text-left">
                    <InputField
                        label="MẬT KHẨU"
                        id="password"
                        type="password"
                        icon={Lock}
                        placeholder="••••••••"
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        error={errors.password}
                    />

                    <div className="flex justify-between items-center pt-2">
                        <label className="flex items-center gap-2 text-xs text-gray-500 cursor-pointer">
                            <input type="checkbox" className="accent-red-600 rounded border-gray-700 bg-transparent" />
                            Ghi nhớ cho lần sau
                        </label>
                        <a href="#" className="text-xs text-yellow-500 font-bold hover:underline">Quên mật khẩu?</a>
                    </div>
                </div>

                <button
                    type="submit"
                    disabled={isLoading}
                    className="relative z-10 cursor-pointer w-full py-4 bg-red-600 hover:bg-red-700 text-white font-bold uppercase tracking-widest rounded shadow-[0_0_20px_rgba(220,38,38,0.3)] transition-all active:scale-[0.98] flex justify-center items-center gap-2 disabled:opacity-70"
                >
                    {isLoading ? (
                        <>
                            <Loader2 className="animate-spin" size={20} />
                            ĐANG XỬ LÝ...
                        </>
                    ) : "ĐĂNG NHẬP"}
                </button>

                <div className="relative py-4">
                    <div className="absolute inset-0 flex items-center">
                        <span className="w-full border-t border-gray-800"></span>
                    </div>
                    <div className="relative flex justify-center text-[10px] uppercase tracking-widest text-gray-500">
                        <span className="bg-[#1a1a1a] px-2">Hoặc tiếp tục với</span>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <SocialButton provider="Google" icon="https://www.svgrepo.com/show/475656/google-color.svg" />
                    <SocialButton provider="Facebook" icon="https://www.svgrepo.com/show/475647/facebook-color.svg" />
                </div>

                <p className="text-center text-xs text-gray-500 pt-2">
                    Chưa có tài khoản?{" "}
                    <button
                        type="button"
                        onClick={() => navigate('/register')} // Chuyển URL
                        className="text-yellow-500 font-bold hover:underline cursor-pointer"
                    >
                        Đăng ký ngay
                    </button>
                </p>
            </form>
        </div>
    );
};

export default LoginForm;
