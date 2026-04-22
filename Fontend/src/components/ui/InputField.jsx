import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

const InputField = ({ label, id, type, icon: Icon, value, onChange, error, placeholder }) => {
    const [showPassword, setShowPassword] = useState(false);
    const isPassword = type === 'password';

    return (
        <div className="w-full space-y-1">
            <label htmlFor={id} className="block text-[11px] uppercase tracking-wider font-semibold text-gray-400">
                {label}
            </label>
            <div className="relative group">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    {Icon && <Icon className="h-4 w-4 text-gray-500 group-focus-within:text-red-500 transition-colors" />}
                </div>
                <input
                    id={id}
                    name={id}
                    type={isPassword ? (showPassword ? 'text' : 'password') : type}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    className={`w-full bg-[#0f0f0f] border ${error ? 'border-red-600' : 'border-gray-800'}
                     text-gray-200 text-sm rounded-md py-3 pl-10 pr-10 outline-none
                     focus:border-red-600 focus:ring-1 focus:ring-red-600 transition-all placeholder:text-gray-600`}
                />
                {isPassword && (
                    <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 hover:text-gray-300"
                    >
                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                )}
            </div>
            {error && <p className="text-[11px] text-red-500 mt-1 uppercase">{error}</p>}
        </div>
    );
};

export default InputField;