import React, { useState } from 'react';
import { Mail, Lock, User, ShieldCheck, Loader2, Phone, Calendar } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { validateEmail, validateStrongPassword, validateUsername, validatePhoneNumber } from '../../utils/validation';
import InputField from '../common/InputField';
import { authService } from '../../services/api/authService';

const RegisterForm = ({ onSwitch }) => {
    const [formData, setFormData] = useState({
        userName: '',
        email: '',
        password: '',
        confirmPassword: '',
        phoneNumber: '',
        dateOfBirth: '',
        gender: '',
        agreeTerms: false
    });
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);
    const [serverError, setServerError] = useState("");

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});
        setServerError("");

        let newErrors = {};

        // Validate Username
        if (!validateUsername(formData.userName)) {
            newErrors.userName = "Username chỉ gồm chữ, số, _ và dài 3-20 ký tự";
        }

        // Validate Email
        if (!validateEmail(formData.email)) {
            newErrors.email = "Định dạng email không hợp lệ";
        }

        // Validate Mật khẩu
        const passwordError = validateStrongPassword(formData.password);
        if (passwordError) {
            newErrors.password = passwordError;
        }

        // Validate Xác nhận mật khẩu
        if (!formData.confirmPassword) {
            newErrors.confirmPassword = "Vui lòng xác nhận mật khẩu";
        } else if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = "Mật khẩu xác nhận không khớp";
        }

        // Validate Số điện thoại
        const phoneTrimmed = formData.phoneNumber.trim();
        if (!validatePhoneNumber(phoneTrimmed)) {
            newErrors.phoneNumber = "Số điện thoại không hợp lệ";
        }

        // Validate ngày sinh
        if (formData.dateOfBirth) {
            const dob = new Date(formData.dateOfBirth);
            const today = new Date();

            let age = today.getFullYear() - dob.getFullYear();
            const m = today.getMonth() - dob.getMonth();

            if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) {
                age--;
            }

            if (age < 13) {
                newErrors.dateOfBirth = "Bạn phải từ 13 tuổi trở lên";
            }
        }

        // Validate giới tính
        if (!formData.gender) {
            newErrors.gender = "Vui lòng chọn giới tính";
        }

        // Validate điều khoản
        if (!formData.agreeTerms) {
            newErrors.agreeTerms = "Bạn cần đồng ý với điều khoản";
        }



        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setIsLoading(true);

        try {

            const apiPayload = {
                userName: formData.userName.trim(),
                email: formData.email,
                password: formData.password,
                phoneNumber: formData.phoneNumber.trim(),
                dateOfBirth: formData.dateOfBirth,
                gender: formData.gender
            };

            await authService.register(apiPayload);
            alert("Đăng ký thành công!");
            navigate('/login');
        } catch (err) {
            setServerError(err.message || "Có lỗi xảy ra, vui lòng thử lại sau.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full max-w-md p-6 sm:p-10 bg-[#1a1a1a]/90 backdrop-blur-md rounded-lg shadow-2xl border border-white/5">
            <div className="text-center space-y-2 mb-8">
                <h1 className="text-3xl font-black text-white uppercase tracking-tighter">ĐĂNG KÝ</h1>
                <p className="text-gray-400 text-sm">Trở thành thành viên của TMT CINEMA ngay hôm nay.</p>
            </div>

            {serverError && (
                <div className="mb-4 p-3 bg-red-500/10 border border-red-500/50 rounded text-red-500 text-xs text-center uppercase tracking-widest">
                    {serverError}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5" autoComplete="off">

                <div className="text-left">
                    <InputField
                        label="USERNAME"
                        id="userName"
                        type="text"
                        icon={User}
                        placeholder="buiductrong"
                        value={formData.userName}
                        onChange={(e) => setFormData({ ...formData, userName: e.target.value })}
                        error={errors.userName}
                        autoComplete="name"
                    />
                </div>

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
                        autoComplete="off"
                    />
                </div>

                <div className="text-left">
                    <InputField
                        label="SỐ ĐIỆN THOẠI"
                        id="phoneNumber"
                        type="tel"
                        icon={Phone}
                        placeholder="0912345678"
                        value={formData.phoneNumber}
                        onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                        error={errors.phoneNumber}
                        autoComplete="tel"
                    />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-left">
                    <InputField
                        label="MẬT KHẨU"
                        id="password"
                        type="password"
                        icon={Lock}
                        placeholder="••••••••"
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        error={errors.password}
                        autoComplete="new-password"
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

                <div className="text-left">
                    <InputField
                        label="Ngày sinh"
                        id="dateOfBirth"
                        type="date"
                        icon={Calendar}
                        placeholder="YYYY-MM-DD"
                        value={formData.dateOfBirth}
                        onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                        error={errors.dateOfBirth}
                        autoComplete="bday"
                    />
                </div>

                <InputField
                    label="GIỚI TÍNH"
                    id="gender"
                    variant="select"
                    icon={User} // hoặc icon gender riêng
                    value={formData.gender}
                    onChange={(e) =>
                        setFormData({ ...formData, gender: e.target.value })
                    }
                    options={[
                        { value: "MALE", label: "Nam" },
                        { value: "FEMALE", label: "Nữ" },
                        { value: "OTHER", label: "Khác" }
                    ]}
                    error={errors.gender}
                />

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
                            Tôi đồng ý với <span className="text-yellow-500 hover:underline">Điều khoản & Chính sách bảo mật</span> của TMT CINEMA.
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
                    Đã có tài khoản?{" "}
                    <button
                        type="button"
                        onClick={() => navigate('/login')}
                        className="text-yellow-500 font-bold hover:underline cursor-pointer"
                    >
                        Đăng nhập
                    </button>
                </p>
            </form>
        </div>
    );
};

export default RegisterForm;
