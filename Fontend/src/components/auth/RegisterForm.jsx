import React, { useState } from 'react';
import { Mail, Lock, User, ShieldCheck, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '../../context/AuthContext';
import { validateEmail, validateStrongPassword, validateFullName } from '../../utils/validation';
import InputField from '../ui/InputField';

const RegisterForm = ({ onSwitch }) => {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        confirmPassword: '',
        agreeTerms: false
    });
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);
    const [serverError, setServerError] = useState("");

    const navigate = useNavigate();
    const { login } = useAuthContext();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});
        setServerError("");

        let newErrors = {};

        // 1. Validate Họ và Tên
        if (!validateFullName(formData.fullName)) {
            newErrors.fullName = "Họ tên không hợp lệ (tối thiểu 2 ký tự)";
        }

        // 2. Validate Email
        if (!validateEmail(formData.email)) {
            newErrors.email = "Định dạng email không hợp lệ";
        }

        // 3. Validate Mật khẩu mạnh
        const passwordError = validateStrongPassword(formData.password);
        if (passwordError) {
            newErrors.password = passwordError;
        }

        // 4. Validate Xác nhận mật khẩu
        if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = "Mật khẩu xác nhận không khớp";
        }

        // 5. Validate Checkbox
        if (!formData.agreeTerms) {
            newErrors.agreeTerms = "Bạn cần đồng ý với điều khoản";
        }

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setIsLoading(true);

        try {
            // Giả lập gọi API đăng ký (1.5s)
            await new Promise(resolve => setTimeout(resolve, 1500));

            // Giả lập đăng ký thành công và tự động đăng nhập luôn
            const newUser = {
                name: formData.fullName,
                email: formData.email,
                avatar: null // Sẽ dùng avatar mặc định ở Navbar
            };

            login(newUser);
            alert("Đăng ký thành công!");
            navigate('/');
        } catch (err) {
            setServerError("Có lỗi xảy ra, vui lòng thử lại sau.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full max-w-md p-10 bg-[#1a1a1a]/90 backdrop-blur-md rounded-lg shadow-2xl border border-white/5">
            <div className="text-center space-y-2 mb-8">
                <h1 className="text-3xl font-black text-white uppercase tracking-tighter">ĐĂNG KÝ</h1>
                <p className="text-gray-400 text-sm">Trở thành thành viên của Lumière Noir ngay hôm nay.</p>
            </div>

            {serverError && (
                <div className="mb-4 p-3 bg-red-500/10 border border-red-500/50 rounded text-red-500 text-xs text-center uppercase tracking-widest">
                    {serverError}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5" autoComplete="off">
                {/* Họ và Tên */}
                <div className="text-left">
                    <InputField
                        label="HỌ VÀ TÊN"
                        id="fullName"
                        type="text"
                        icon={User}
                        placeholder="Nguyễn Văn A"
                        value={formData.fullName}
                        onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                        error={errors.fullName}
                        autoComplete="off" // Không tự điền password
                    />
                </div>

                {/* Email */}
                <div className="text-left">
                    <InputField
                        label="EMAIL"
                        id="email"
                        type="email" // Có thể thử đổi thành type="text" nếu trình duyệt vẫn cố chấp điền email
                        icon={Mail}
                        placeholder="example@email.com"
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        error={errors.email}
                        autoComplete="off"
                    />
                </div>

                {/* Mật khẩu và Xác nhận */}
                <div className="grid grid-cols-2 gap-4 text-left">
                    <InputField
                        label="MẬT KHẨU"
                        id="password"
                        type="password"
                        icon={Lock}
                        placeholder="••••••••"
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        error={errors.password}
                        autoComplete="new-password" // Gợi ý trình duyệt không tự điền mật khẩu cũ
                    />
                    <InputField
                        label="XÁC NHẬN"
                        id="confirmPassword"
                        type="password"
                        icon={ShieldCheck}
                        placeholder="••••••••"
                        value={formData.confirmPassword}
                        onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                        error={errors.confirmPassword}
                        autoComplete="new-password"
                    />
                </div>

                {/* Checkbox điều khoản */}
                <div className="space-y-1">
                    <label className="flex items-start gap-3 text-xs text-gray-400 cursor-pointer group">
                        <input
                            type="checkbox"
                            className="mt-0.5 accent-red-600 rounded border-gray-700 bg-transparent w-4 h-4"
                            checked={formData.agreeTerms}
                            onChange={(e) => setFormData({ ...formData, agreeTerms: e.target.checked })}
                        />
                        <span className="leading-relaxed">
                            Tôi đồng ý với <span className="text-yellow-500 hover:underline">Điều khoản & Chính sách bảo mật</span> của Lumière Noir.
                        </span>
                    </label>
                    {errors.agreeTerms && <p className="text-[10px] text-red-500 uppercase italic tracking-wider">{errors.agreeTerms}</p>}
                </div>

                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full py-4 bg-red-600 hover:bg-red-700 text-white font-bold uppercase tracking-widest rounded shadow-[0_0_20px_rgba(220,38,38,0.3)] transition-all active:scale-[0.98] flex justify-center items-center gap-2 disabled:opacity-70 cursor-pointer relative z-10"
                >
                    {isLoading ? <Loader2 className="animate-spin" size={20} /> : "ĐĂNG KÝ"}
                </button>

                <p className="text-center text-xs text-gray-500">
                    Đã có tài khoản? <button type="button" onClick={onSwitch} className="text-yellow-500 font-bold hover:underline cursor-pointer">Đăng nhập</button>
                </p>
            </form>
        </div>
    );
};

export default RegisterForm;