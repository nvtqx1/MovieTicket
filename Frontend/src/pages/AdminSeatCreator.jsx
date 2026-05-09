import React, { useState, useEffect, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../services/api/api";

const SEAT_COLORS = {
    NORMAL: { bg: "bg-blue-600/40", border: "border-blue-500/50", label: "Normal" },
    VIP: { bg: "bg-amber-500/40", border: "border-amber-400/50", label: "VIP" },
    COUPLE: { bg: "bg-pink-500/40", border: "border-pink-400/50", label: "Couple" },
    PATH: { bg: "bg-gray-800", border: "border-gray-700", label: "Lối đi" },
};

export default function AdminSeatCreator() {
    const { id: roomId } = useParams();
    const navigate = useNavigate();

    const [form, setForm] = useState({ rows: 10, cols: 15 });
    const [loading, setLoading] = useState(false);
    const [loadingSeats, setLoadingSeats] = useState(true);
    const [savedSeats, setSavedSeats] = useState([]);
    const [roomInfo, setRoomInfo] = useState(null);
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);

    // Load ghế đã lưu khi vào trang
    useEffect(() => {
        const fetchSeats = async () => {
            try {
                setLoadingSeats(true);
                const res = await api.get(`/admin/rooms/${roomId}/seats`);
                const data = res.data;
                setRoomInfo(data);
                if (data.seats && data.seats.length > 0) {
                    setSavedSeats(data.seats);
                    setForm({ rows: data.rows || 10, cols: data.cols || 15 });
                }
            } catch (err) {
                console.error("Lỗi load ghế:", err);
            } finally {
                setLoadingSeats(false);
            }
        };
        fetchSeats();
    }, [roomId]);

    const handleGenerate = async () => {
        try {
            setLoading(true);
            setError(null);
            const res = await api.post(`/admin/rooms/${roomId}/seats`, {
                rows: Number(form.rows),
                cols: Number(form.cols),
            });
            const data = res.data;
            setSavedSeats(data.seats || []);
            setRoomInfo(data);
            setResult(data);
            alert(`✅ Đã tạo ${data.totalSeats} ghế cho phòng ${data.roomName}!`);
        } catch (err) {
            setError(err.message || "Lỗi lưu cấu hình ghế.");
        } finally {
            setLoading(false);
        }
    };

    // Preview layout (chưa lưu)
    const previewRows = useMemo(() => {
        return Array.from({ length: Number(form.rows) }, (_, i) =>
            String.fromCharCode(65 + i)
        );
    }, [form.rows]);

    const getSeatTypeForPreview = (rowIdx, totalRows) => {
        if (rowIdx === totalRows - 1) return "COUPLE";
        if (rowIdx >= totalRows / 2) return "VIP";
        return "NORMAL";
    };

    // Render ghế đã lưu trong DB
    const renderSavedSeats = () => {
        if (savedSeats.length === 0) return null;
        const rows = roomInfo?.rows || 10;
        const cols = roomInfo?.cols || 15;

        const seatMap = {};
        savedSeats.forEach((s) => {
            const key = `${s.rowIndex}-${s.colIndex}`;
            seatMap[key] = s;
        });

        return (
            <div>
                {/* SCREEN */}
                <div className="mb-8 text-center">
                    <div className="h-5 bg-gradient-to-r from-transparent via-white/20 to-transparent rounded-full w-2/3 mx-auto" />
                    <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">
                        Màn hình
                    </p>
                </div>

                {/* GRID */}
                <div
                    className="grid gap-[6px] justify-center"
                    style={{ gridTemplateColumns: `30px repeat(${cols}, 32px)` }}
                >
                    {/* Column headers */}
                    <div />
                    {Array.from({ length: cols }).map((_, i) => (
                        <div key={i} className="text-[10px] text-gray-500 text-center font-mono">
                            {i + 1}
                        </div>
                    ))}

                    {/* Seat rows */}
                    {Array.from({ length: rows }).map((_, rowIdx) => (
                        <React.Fragment key={rowIdx}>
                            <div className="text-xs text-gray-400 flex items-center justify-center font-bold">
                                {String.fromCharCode(65 + rowIdx)}
                            </div>
                            {Array.from({ length: cols }).map((_, colIdx) => {
                                const seat = seatMap[`${rowIdx}-${colIdx}`];
                                const type = seat?.seatType || "NORMAL";
                                const colors = SEAT_COLORS[type] || SEAT_COLORS.NORMAL;
                                return (
                                    <div
                                        key={colIdx}
                                        className={`w-8 h-7 rounded ${colors.bg} border ${colors.border} flex items-center justify-center cursor-default transition-all hover:scale-110`}
                                        title={`${seat?.seatNumber || ""} (${colors.label})`}
                                    >
                                        <span className="text-[8px] text-white/70 font-mono">
                                            {seat?.seatNumber || ""}
                                        </span>
                                    </div>
                                );
                            })}
                        </React.Fragment>
                    ))}
                </div>
            </div>
        );
    };

    // Render preview (chưa lưu)
    const renderPreview = () => (
        <div>
            <div className="mb-8 text-center">
                <div className="h-5 bg-gradient-to-r from-transparent via-white/20 to-transparent rounded-full w-2/3 mx-auto" />
                <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">
                    Màn hình
                </p>
            </div>
            <div
                className="grid gap-[6px] justify-center"
                style={{ gridTemplateColumns: `30px repeat(${form.cols}, 32px)` }}
            >
                <div />
                {Array.from({ length: Number(form.cols) }).map((_, i) => (
                    <div key={i} className="text-[10px] text-gray-500 text-center font-mono">
                        {i + 1}
                    </div>
                ))}
                {previewRows.map((row, rowIdx) => (
                    <React.Fragment key={row}>
                        <div className="text-xs text-gray-400 flex items-center justify-center font-bold">
                            {row}
                        </div>
                        {Array.from({ length: Number(form.cols) }).map((_, colIdx) => {
                            const type = getSeatTypeForPreview(rowIdx, Number(form.rows));
                            const colors = SEAT_COLORS[type];
                            return (
                                <div
                                    key={colIdx}
                                    className={`w-8 h-7 rounded ${colors.bg} border ${colors.border} transition-all hover:scale-110`}
                                    title={`${row}${colIdx + 1} (${colors.label})`}
                                />
                            );
                        })}
                    </React.Fragment>
                ))}
            </div>
        </div>
    );

    if (loadingSeats) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600" />
            </div>
        );
    }

    return (
        <div className="p-8 text-white space-y-8">
            {/* HEADER */}
            <div>
                <button
                    onClick={() => navigate(-1)}
                    className="text-gray-500 hover:text-white mb-2 text-sm"
                >
                    &larr; Quay lại danh sách Phòng
                </button>
                <h1 className="text-2xl font-black uppercase tracking-wider">
                    Cấu hình <span className="text-red-500">Ghế</span>
                </h1>
                <p className="text-xs text-gray-500 mt-1">
                    {roomInfo
                        ? `Phòng: ${roomInfo.roomName} — ${roomInfo.totalSeats || 0} ghế`
                        : `Phòng ID: ${roomId}`}
                </p>
            </div>

            {/* LEGEND */}
            <div className="flex gap-6 flex-wrap">
                {Object.entries(SEAT_COLORS).filter(([k]) => k !== "PATH").map(([key, val]) => (
                    <div key={key} className="flex items-center gap-2">
                        <div className={`w-5 h-5 rounded ${val.bg} border ${val.border}`} />
                        <span className="text-xs text-gray-400">{val.label}</span>
                    </div>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* LEFT PANEL */}
                <div className="space-y-6">
                    <div className="bg-[#18181b] p-6 rounded-xl border border-white/5">
                        <h2 className="font-bold mb-6">Cấu hình ma trận</h2>
                        <div className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="text-xs text-gray-400">Số hàng (Rows)</label>
                                    <input
                                        type="number"
                                        min="1"
                                        max="26"
                                        value={form.rows}
                                        onChange={(e) => setForm({ ...form, rows: e.target.value })}
                                        className="w-full mt-1 bg-black border border-white/10 rounded-lg px-3 py-2 text-center"
                                    />
                                </div>
                                <div>
                                    <label className="text-xs text-gray-400">Số cột (Cols)</label>
                                    <input
                                        type="number"
                                        min="1"
                                        max="30"
                                        value={form.cols}
                                        onChange={(e) => setForm({ ...form, cols: e.target.value })}
                                        className="w-full mt-1 bg-black border border-white/10 rounded-lg px-3 py-2 text-center"
                                    />
                                </div>
                            </div>

                            <div className="bg-black/50 rounded-lg p-3 text-xs text-gray-400 space-y-1">
                                <p>📐 Tổng ghế: <span className="text-white font-bold">{Number(form.rows) * Number(form.cols)}</span></p>
                                <p>🔵 Normal: Hàng A → {String.fromCharCode(64 + Math.floor(Number(form.rows) / 2))}</p>
                                <p>🟡 VIP: Hàng {String.fromCharCode(65 + Math.floor(Number(form.rows) / 2))} → {String.fromCharCode(64 + Number(form.rows) - 1)}</p>
                                <p>🩷 Couple: Hàng {String.fromCharCode(64 + Number(form.rows))} (cuối)</p>
                            </div>

                            <button
                                onClick={handleGenerate}
                                disabled={loading}
                                className="w-full bg-red-500 hover:bg-red-600 py-3 rounded-lg font-bold transition disabled:opacity-50 uppercase tracking-widest text-sm"
                            >
                                {loading ? "Đang lưu..." : "💾 Lưu sơ đồ ghế"}
                            </button>
                        </div>
                    </div>

                    {result && (
                        <div className="bg-green-500/10 border border-green-500/30 p-4 rounded-lg">
                            <p className="text-sm">
                                ✅ Đã lưu <b>{result.totalSeats}</b> ghế cho phòng <b>{result.roomName}</b>
                            </p>
                        </div>
                    )}

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/30 p-4 rounded-lg">
                            <p className="text-sm text-red-400">{error}</p>
                        </div>
                    )}
                </div>

                {/* RIGHT PANEL - SEAT MAP */}
                <div className="lg:col-span-2 bg-[#111] border border-white/5 rounded-xl p-6 overflow-x-auto">
                    <h2 className="font-bold mb-6">
                        {savedSeats.length > 0
                            ? `🪑 Sơ đồ ghế hiện tại (${savedSeats.length} ghế)`
                            : "👁️ Preview sơ đồ ghế"}
                    </h2>

                    {savedSeats.length > 0 ? renderSavedSeats() : renderPreview()}
                </div>
            </div>
        </div>
    );
}