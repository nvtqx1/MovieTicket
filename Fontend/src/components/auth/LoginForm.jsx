import React, { useState } from 'react';
import { Mail, Lock, Loader2 } from 'lucide-react';
import InputField from '../ui/InputField';
import SocialButton from '../ui/SocialButton';

import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '../../context/AuthContext';

import defaultAvatar from "../../assets/images/avatarDefault.jpeg";// avatar mặc định của người dùng
import { validateEmail, validateStrongPassword } from '../../utils/validation';

const MOCK_USERS = [
    {
        email: "admin@gmail.com",
        password: "Admin@123",
        name: "Admin TMT",
        avatar: "https://i.pravatar.cc/150?img=12"
    },
    {
        email: "user@gmail.com",
        password: "User@123",
        name: "Bùi Trọng Đức",
        avatar: defaultAvatar
    }
];

const LoginForm = ({ onSwitch }) => {
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [errors, setErrors] = useState({});

    const [isLoading, setIsLoading] = useState(false);
    const [loginError, setLoginError] = useState("");

    const navigate = useNavigate(); // Khởi tạo navigate
    const { login } = useAuthContext(); // Lấy hàm login từ Context

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});
        setLoginError("");
        // Thêm validate
        let newErrors = {};
        if (!formData.email) {
            newErrors.email = "Vui lòng nhập email";
        } else if (!validateEmail(formData.email)) {
            newErrors.email = "Định dạng email không hợp lệ";
        }

        if (!formData.password) {
            newErrors.password = "Vui lòng nhập mật khẩu";
        }

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setIsLoading(true);

        //Xử lý Async và Loading

        try {
            await new Promise(resolve => setTimeout(resolve, 1500));

            if (formData.email === "admin@gmail.com" && formData.password === "Admin@123") {
                const mockUser = {
                    name: "Admin TMT",
                    email: formData.email,
                    avatar: defaultAvatar
                };
                login(mockUser);
                navigate('/');
            } else {
                throw new Error("Email hoặc mật khẩu không chính xác");
            }
        } catch (err) {
            setLoginError(err.message);
        } finally {
            setIsLoading(false);
        }

    };

    return (
        <div className="w-full max-w-md p-10 bg-[#1a1a1a]/90 backdrop-blur-md rounded-lg shadow-2xl border border-white/5">
            <div className="text-center space-y-2 mb-8">
                <h1 className="text-3xl font-black text-white uppercase tracking-tighter">Đăng nhập</h1>
                <p className="text-gray-400 text-sm">Chào mừng trở lại màn ảnh rộng.</p>
            </div>

            {loginError && (
                <div className="mb-4 p-3 bg-red-500/20 border border-red-500 text-red-400 text-sm rounded text-center">
                    {loginError}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
                <InputField
                    label="Email hoặc tên đăng nhập"
                    id="email"
                    type="email"
                    icon={Mail}
                    placeholder="username@email.com"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    error={errors.email}
                />

                <div className="space-y-1">
                    <InputField
                        label="Mật khẩu"
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
                            <input type="checkbox" className="accent-red-600 rounded" /> Ghi nhớ cho lần sau
                        </label>
                        <a href="#" className="text-xs text-yellow-500 hover:underline">Quên mật khẩu?</a>
                    </div>
                </div>

                <button type="submit" className="relative z-10 cursor-pointer w-full py-3 bg-red-600 hover:bg-red-700 text-white font-bold uppercase
                           tracking-widest rounded shadow-[0_0_20px_rgba(220,38,38,0.3)]
                           transition-all active:scale-[0.98]">
                    {isLoading ? (
                        <>
                            <Loader2 className="animate-spin" size={20} />
                            Đang xử lý...
                        </>
                    ) : "Đăng nhập"}
                </button>

                <div className="relative py-4">
                    <div className="absolute inset-0 flex items-center"><span className="w-full border-t border-gray-800"></span></div>
                    <div className="relative flex justify-center text-[10px] uppercase tracking-widest text-gray-600">
                        <span className="bg-[#1a1a1a] px-2">Hoặc tiếp tục với</span>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <SocialButton provider="Google" icon="https://www.svgrepo.com/show/475656/google-color.svg" />
                    <SocialButton provider="Facebook" icon="https://www.svgrepo.com/show/475647/facebook-color.svg" />
                </div>

                <p className="text-center text-xs text-gray-500">
                    Chưa có tài khoản? <button onClick={onSwitch} className="text-yellow-500 font-bold hover:underline">Đăng ký ngay</button>
                </p>
            </form>
        </div>
    );
};

export default LoginForm;