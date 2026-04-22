import React, { useState } from 'react';
import { Mail, Lock, LogIn } from 'lucide-react';
import InputField from '../ui/InputField';
import SocialButton from '../ui/SocialButton';

import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '../../context/AuthContext';

import defaultAvatar from "../../assets/images/avatarDefault.jpeg";// avatar mặc định của người dùng

const LoginForm = ({ onSwitch }) => {
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [errors, setErrors] = useState({});

    const navigate = useNavigate(); // Khởi tạo navigate
    const { login } = useAuthContext(); // Lấy hàm login từ Context

    const handleSubmit = (e) => {
        e.preventDefault();
        // Cần thêm validate
        let newErrors = {};
        // if (!formData.email) {
        //     newErrors.email = "Email là bắt buộc";
        // }
        // if (!formData.password) {
        //     newErrors.password = "Mật khẩu là bắt buộc";
        // }
        // setErrors(newErrors);

        // if (Object.keys(newErrors).length === 0) {
        //     // Gọi hàm login từ AuthContext
        //     login({ email: formData.email, password: formData.password });
        //     navigate('/'); // Chuyển hướng sau khi đăng nhập thành công
        // }
        // Tạm thời bỏ validate để test login
        // GIẢ LẬP ĐĂNG NHẬP THÀNH CÔNG:
        // Tạo một object user giả lập (Sau này lấy từ API backend)

        console.log("Submit:", formData);
        const mockUser = {
            name: "Người Dùng",
            email: formData.email,
            avatar: defaultAvatar
        };

        // 1. Lưu vào Context toàn cục
        login(mockUser);

        // 2. Chuyển hướng về trang chủ
        navigate('/');



    };

    return (
        <div className="w-full max-w-md p-10 bg-[#1a1a1a]/90 backdrop-blur-md rounded-lg shadow-2xl border border-white/5">
            <div className="text-center space-y-2 mb-8">
                <h1 className="text-3xl font-black text-white uppercase tracking-tighter">Đăng nhập</h1>
                <p className="text-gray-400 text-sm">Chào mừng trở lại màn ảnh rộng.</p>
            </div>

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
                    <div className="flex justify-between items-center">
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
                    </div>
                    <div className="flex justify-between items-center pt-2">
                        <label className="flex items-center gap-2 text-xs text-gray-500 cursor-pointer">
                            <input type="checkbox" className="accent-red-600 rounded" /> Ghi nhớ cho lần sau
                        </label>
                        <a href="#" className="text-xs text-yellow-500 hover:underline">Quên mật khẩu?</a>
                    </div>
                </div>

                <button type="submit" className="w-full py-3 bg-red-600 hover:bg-red-700 text-white font-bold uppercase
                           tracking-widest rounded shadow-[0_0_20px_rgba(220,38,38,0.3)]
                           transition-all active:scale-[0.98]">
                    Đăng nhập
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