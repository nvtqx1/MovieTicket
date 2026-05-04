import React, { useState, useMemo } from 'react';
import { generateSeatMatrix } from '../services/api/seatService';

// ==========================================
// 1. MOCK DATA
// ==========================================
const MOCK_SHOWTIMES = [
    { id: 1, label: "Dune: Part Two - 19:30, IMAX 1" },
    { id: 2, label: "Oppenheimer - 20:00, Hall 02" },
    { id: 3, label: "Mai - 18:00, VIP 3" },
];

export default function AdminSeatCreator() {
    // ==========================================
    // 2. STATE MANAGEMENT (Khớp với API)
    // ==========================================
    const [showtimeId, setShowtimeId] = useState(MOCK_SHOWTIMES[0].id);
    const [rows, setRows] = useState(10);
    const [cols, setCols] = useState(15);

    const [isGenerating, setIsGenerating] = useState(false);
    const [generateResult, setGenerateResult] = useState(null);
    const [error, setError] = useState(null);

    // ==========================================
    // 3. LOGIC GỌI API
    // ==========================================
    const handleGenerateMatrix = async () => {
        setIsGenerating(true);
        setGenerateResult(null);
        setError(null);

        const payload = {
            showtimeId: parseInt(showtimeId),
            rows: parseInt(rows),
            cols: parseInt(cols)
        };

        try {
            // Gọi API POST /v1/admin/seats/matrix/generate
            const response = await generateSeatMatrix(payload);
            setGenerateResult(response);
        } catch (err) {
            setError(err.message || "Lỗi khởi tạo ghế");
            console.error("Lỗi khởi tạo ghế:", err);
        } finally {
            setIsGenerating(false);
        }
    };

    // Hàm tiện ích tạo mảng chữ cái cho tên Hàng (A, B, C...)
    const rowLabels = useMemo(() => {
        return Array.from({ length: rows }, (_, i) => String.fromCharCode(65 + i));
    }, [rows]);

    return (
        <div className="text-white font-sans min-h-screen pb-10">

            {/* HEADER */}
            <div className="mb-8">
                <h1 className="text-sm font-black text-red-500 uppercase tracking-[0.2em]">Seat Matrix Creator</h1>
            </div>

            <div className="flex flex-col lg:flex-row gap-8 items-start">

                {/* ========================================== */}
                {/* CỘT TRÁI: BẢNG ĐIỀU KHIỂN (CONTROLS) */}
                {/* ========================================== */}
                <div className="w-full lg:w-80 flex-shrink-0 space-y-6">

                    {/* Card: Cấu hình chính */}
                    <div className="bg-[#18181b] p-6 rounded-xl border border-white/5 shadow-2xl">
                        <h2 className="text-xl font-bold mb-6">Cấu hình Sơ đồ Ghế</h2>

                        <div className="space-y-5">
                            {/* Chọn suất chiếu */}
                            <div>
                                <label className="block text-[11px] text-gray-400 mb-2">Chọn suất chiếu</label>
                                <select
                                    value={showtimeId}
                                    onChange={(e) => setShowtimeId(e.target.value)}
                                    className="w-full bg-[#0a0a0a] border border-white/10 rounded-md py-3 px-4 text-sm focus:outline-none focus:border-red-500 appearance-none text-gray-200"
                                >
                                    {MOCK_SHOWTIMES.map(st => (
                                        <option key={st.id} value={st.id}>{st.label}</option>
                                    ))}
                                </select>
                            </div>

                            {/* Rows & Cols Inputs */}
                            <div className="flex gap-4">
                                <div className="flex-1">
                                    <label className="block text-[11px] text-gray-400 mb-2">Số hàng</label>
                                    <input
                                        type="number" min="1" max="26"
                                        value={rows}
                                        onChange={(e) => setRows(e.target.value)}
                                        className="w-full bg-[#0a0a0a] border border-white/10 rounded-md py-3 px-4 text-sm font-bold text-center focus:outline-none focus:border-red-500"
                                    />
                                </div>
                                <div className="flex-1">
                                    <label className="block text-[11px] text-gray-400 mb-2">Số cột</label>
                                    <input
                                        type="number" min="1" max="50"
                                        value={cols}
                                        onChange={(e) => setCols(e.target.value)}
                                        className="w-full bg-[#0a0a0a] border border-white/10 rounded-md py-3 px-4 text-sm font-bold text-center focus:outline-none focus:border-red-500"
                                    />
                                </div>
                            </div>

                            {/* Submit Button */}
                            <button
                                onClick={handleGenerateMatrix}
                                disabled={isGenerating || !rows || !cols}
                                className={`w-full py-3.5 rounded-lg text-sm font-bold flex justify-center items-center gap-2 transition-all mt-4 ${isGenerating ? 'bg-zinc-800 text-gray-500 cursor-not-allowed' : 'bg-[#ff5a5f] hover:bg-red-600 text-white shadow-lg shadow-red-500/20'
                                    }`}
                            >
                                <span>{isGenerating ? 'Đang tạo...' : '⚙️ Tạo sơ đồ'}</span>
                            </button>
                        </div>
                    </div>

                    {/* Card: Status Alert */}
                    {generateResult && (
                        <div className="bg-[#18181b] p-5 rounded-xl border border-green-500/30 flex items-center gap-4 shadow-[0_0_15px_rgba(34,197,94,0.1)]">
                            <div className="w-8 h-8 rounded-full bg-green-500/20 text-green-500 flex items-center justify-center">✓</div>
                            <div>
                                <p className="text-[10px] text-gray-400">Trạng thái</p>
                                <p className="text-sm">Tổng số ghế đã tạo: <span className="text-green-400 font-bold">{generateResult.totalSeatsGenerated}</span></p>
                            </div>
                        </div>
                    )}

                    {/* Card: Error Alert */}
                    {error && (
                        <div className="bg-[#18181b] p-5 rounded-xl border border-red-500/30 flex items-center gap-4 shadow-[0_0_15px_rgba(239,68,68,0.1)]">
                            <div className="w-8 h-8 rounded-full bg-red-500/20 text-red-500 flex items-center justify-center">!</div>
                            <div>
                                <p className="text-[10px] text-gray-400">Lỗi</p>
                                <p className="text-sm text-red-400">{error}</p>
                            </div>
                        </div>
                    )}

                    {/* Card: Legend */}
                    <div className="bg-[#18181b] p-6 rounded-xl border border-white/5">
                        <h3 className="text-sm font-bold mb-4">Legend</h3>
                        <div className="space-y-3">
                            <div className="flex items-center gap-3">
                                <div className="w-5 h-5 rounded bg-[#1a1a1a] border border-white/10"></div>
                                <span className="text-xs text-gray-400">Available</span>
                            </div>
                            <div className="flex items-center gap-3">
                                <div className="w-5 h-5 rounded bg-yellow-500 shadow-[0_0_8px_rgba(234,179,8,0.5)]"></div>
                                <span className="text-xs text-gray-400">Selected / VIP</span>
                            </div>
                            <div className="flex items-center gap-3">
                                <div className="w-5 h-5 rounded bg-zinc-800 border border-zinc-700 opacity-50"></div>
                                <span className="text-xs text-gray-400">Occupied / Blocked</span>
                            </div>
                        </div>
                    </div>

                </div>

                {/* ========================================== */}
                {/* CỘT PHẢI: PREVIEW GRID (MÔ PHỎNG SƠ ĐỒ) */}
                {/* ========================================== */}
                <div className="flex-grow bg-[#111] border border-white/5 rounded-2xl p-8 overflow-x-auto min-h-[600px] flex flex-col items-center shadow-2xl relative">

                    {/* Màn hình (Screen) */}
                    <div className="w-full max-w-3xl mb-16 flex flex-col items-center">
                        <div className="w-full h-8 border-t-4 border-white/20 rounded-[50%] shadow-[0_-10px_20px_rgba(255,255,255,0.05)] relative">
                            <div className="absolute inset-0 bg-gradient-to-b from-white/5 to-transparent rounded-[50%]"></div>
                        </div>
                        <span className="mt-4 text-[10px] font-black text-gray-500 tracking-[0.5em] uppercase">Màn hình</span>
                    </div>

                    {/* Ma trận ghế */}
                    <div
                        className="grid gap-2 mb-10"
                        // CSS Grid động: Thêm 1 cột cho tên Hàng (A, B, C...)
                        style={{ gridTemplateColumns: `auto repeat(${cols}, minmax(0, 1fr))` }}
                    >
                        {/* Hàng Số Cột (Header) */}
                        <div className="w-6"></div> {/* Cột trống góc trái trên */}
                        {Array.from({ length: cols }, (_, i) => (
                            <div key={`header-${i}`} className="text-[10px] text-gray-500 flex items-center justify-center h-6 w-6 sm:w-8">
                                {i + 1}
                            </div>
                        ))}

                        {/* Render từng Hàng */}
                        {rowLabels.map((rowLabel, rowIndex) => (
                            <React.Fragment key={rowLabel}>
                                {/* Tên Hàng */}
                                <div className="text-xs font-bold text-gray-400 flex items-center justify-center w-6 h-6 sm:h-8">
                                    {rowLabel}
                                </div>

                                {/* Các Ghế trong Hàng */}
                                {Array.from({ length: cols }, (_, colIndex) => {
                                    // Giả lập một vài ghế VIP/Selected để UI giống ảnh mẫu
                                    const isVip = (rowIndex >= 0 && rowIndex <= 4) && (colIndex >= 6 && colIndex <= 7);

                                    return (
                                        <div
                                            key={`${rowLabel}${colIndex + 1}`}
                                            className={`w-6 h-6 sm:w-8 sm:h-8 rounded-[4px] border border-white/10 flex items-center justify-center cursor-pointer transition-all ${isVip ? 'bg-yellow-500 shadow-[0_0_10px_rgba(234,179,8,0.3)] border-yellow-400' : 'bg-[#1a1a1a] hover:bg-[#2a2a2a] hover:border-white/30 inner-shadow-seat'}`}
                                            title={`Ghế ${rowLabel}${colIndex + 1}`}
                                        >
                                            {/* Tạo hiệu ứng lõm vào cho ghế bằng CSS box-shadow */}
                                            <div className="w-full h-full rounded-[3px] border-t border-white/5 bg-gradient-to-b from-transparent to-black/30 pointer-events-none"></div>
                                        </div>
                                    );
                                })}
                            </React.Fragment>
                        ))}
                    </div>

                </div>
            </div>
        </div>
    );
}