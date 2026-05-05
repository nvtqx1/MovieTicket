import React, { useState, useEffect } from 'react';
import { Ticket, Plus, Tag, Calendar, Percent } from 'lucide-react';
import { createVoucher } from '../services/api/voucherService';

export default function AdminVouchers() {
    const [loading, setLoading] = useState(false);
    const [successMsg, setSuccessMsg] = useState("");
    const [errorMsg, setErrorMsg] = useState("");

    const [formData, setFormData] = useState({
        code: '',
        discountPercentage: 10,
        maxDiscountAmount: 50000,
        validFrom: '',
        validTo: '',
        maxUsage: 100
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setSuccessMsg("");
        setErrorMsg("");

        try {
            const dataToSubmit = {
                ...formData,
                discountPercentage: Number(formData.discountPercentage),
                maxDiscountAmount: Number(formData.maxDiscountAmount),
                maxUsage: Number(formData.maxUsage),
                // Ensure date format is suitable for backend (e.g. ISO string or LocalDate string)
                startTime: formData.validFrom + "T00:00:00",
                endTime: formData.validTo + "T23:59:59"
            };

            await createVoucher(dataToSubmit);
            setSuccessMsg(`Tạo mã ${formData.code} thành công!`);
            setFormData({
                code: '',
                discountPercentage: 10,
                maxDiscountAmount: 50000,
                validFrom: '',
                validTo: '',
                maxUsage: 100
            });
        } catch (error) {
            setErrorMsg(error.response?.data?.message || "Lỗi khi tạo mã voucher. Có thể mã đã tồn tại.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-10 text-white">
            <header className="mb-10">
                <h1 className="text-2xl font-black uppercase tracking-[0.1em] flex items-center gap-3">
                    <Ticket className="text-red-600" size={28} />
                    Quản lý <span className="text-red-600">Voucher</span>
                </h1>
                <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">
                    Tạo mã giảm giá và khuyến mãi
                </p>
            </header>

            <div className="bg-[#111] p-8 rounded-xl border border-white/5 max-w-2xl">
                <h2 className="text-sm font-black uppercase tracking-widest mb-6 flex items-center gap-2">
                    <span className="w-1 h-4 bg-red-600 rounded-full"></span> Tạo Voucher Mới
                </h2>

                {successMsg && (
                    <div className="mb-6 p-4 bg-green-500/10 border border-green-500/20 text-green-400 rounded-lg text-sm font-bold">
                        {successMsg}
                    </div>
                )}

                {errorMsg && (
                    <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 text-red-400 rounded-lg text-sm font-bold">
                        {errorMsg}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                            Mã Voucher
                        </label>
                        <div className="relative">
                            <Tag className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                            <input
                                type="text"
                                name="code"
                                value={formData.code}
                                onChange={handleChange}
                                required
                                placeholder="VD: FLASH50"
                                className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors uppercase"
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-6">
                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Phần trăm giảm (%)
                            </label>
                            <div className="relative">
                                <Percent className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                <input
                                    type="number"
                                    name="discountPercentage"
                                    value={formData.discountPercentage}
                                    onChange={handleChange}
                                    required
                                    min="1"
                                    max="100"
                                    className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors"
                                />
                            </div>
                        </div>
                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Giảm tối đa (VNĐ)
                            </label>
                            <input
                                type="number"
                                name="maxDiscountAmount"
                                value={formData.maxDiscountAmount}
                                onChange={handleChange}
                                required
                                min="0"
                                className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 px-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors"
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-6">
                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Từ ngày
                            </label>
                            <div className="relative">
                                <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                <input
                                    type="date"
                                    name="validFrom"
                                    value={formData.validFrom}
                                    onChange={handleChange}
                                    required
                                    className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors"
                                />
                            </div>
                        </div>
                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Đến ngày
                            </label>
                            <div className="relative">
                                <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                <input
                                    type="date"
                                    name="validTo"
                                    value={formData.validTo}
                                    onChange={handleChange}
                                    required
                                    className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors"
                                />
                            </div>
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                            Số lượng mã
                        </label>
                        <input
                            type="number"
                            name="maxUsage"
                            value={formData.maxUsage}
                            onChange={handleChange}
                            required
                            min="1"
                            className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 px-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-4 rounded-lg flex items-center justify-center gap-2 transition-colors disabled:opacity-50"
                    >
                        {loading ? (
                            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                        ) : (
                            <>
                                <Plus size={20} />
                                Tạo Voucher
                            </>
                        )}
                    </button>
                </form>
            </div>
        </div>
    );
}
