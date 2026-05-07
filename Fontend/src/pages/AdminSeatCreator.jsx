import React, { useState, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../services/api/api";
import { Trash2 } from "lucide-react";

export default function AdminSeatCreator() {
    const { id: roomId } = useParams();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        rows: 10,
        cols: 15
    });

    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);

    const handleGenerate = async () => {
        try {
            setLoading(true);
            setError(null);
            const res = await api.post(`/admin/rooms/${roomId}/seats`, {
                rows: Number(form.rows),
                cols: Number(form.cols)
            });
            setResult(res.data);
            alert("Lưu ma trận ghế cho phòng thành công!");
            navigate(-1); // Go back after success
        } catch (err) {
            setError(err.message || "Lỗi lưu cấu hình ghế.");
        } finally {
            setLoading(false);
        }
    };

    const rowLabels = useMemo(() => {
        return Array.from({ length: form.rows }, (_, i) =>
            String.fromCharCode(65 + i)
        );
    }, [form.rows]);

    return (
        <div className="p-8 text-white space-y-8">

            {/* HEADER */}
            <div>
                <h1 className="text-2xl font-black uppercase tracking-wider">
                    Seat <span className="text-red-500">Creator</span>
                </h1>
                <p className="text-xs text-gray-500 mt-1">
                    Tạo sơ đồ ghế cho từng suất chiếu
                </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

                {/* ================= LEFT PANEL ================= */}
                <div className="space-y-6">

                    <div className="bg-[#18181b] p-6 rounded-xl border border-white/5">
                        <h2 className="font-bold mb-6">Cấu hình</h2>

                        <div className="space-y-4">



                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="text-xs text-gray-400">Rows</label>
                                    <input
                                        type="number"
                                        value={form.rows}
                                        onChange={(e) =>
                                            setForm({ ...form, rows: e.target.value })
                                        }
                                        className="w-full mt-1 bg-black border border-white/10 rounded-lg px-3 py-2 text-center"
                                    />
                                </div>

                                <div>
                                    <label className="text-xs text-gray-400">Cols</label>
                                    <input
                                        type="number"
                                        value={form.cols}
                                        onChange={(e) =>
                                            setForm({ ...form, cols: e.target.value })
                                        }
                                        className="w-full mt-1 bg-black border border-white/10 rounded-lg px-3 py-2 text-center"
                                    />
                                </div>
                            </div>

                            <button
                                onClick={handleGenerate}
                                disabled={loading}
                                className="w-full bg-red-500 hover:bg-red-600 py-3 rounded-lg font-bold transition disabled:opacity-50"
                            >
                                {loading ? "Đang xử lý..." : "Lưu ma trận ghế"}
                            </button>
                        </div>
                    </div>

                    {/* RESULT */}
                    {result && (
                        <div className="bg-green-500/10 border border-green-500/30 p-4 rounded-lg">
                            <p className="text-sm">
                                ✔ Đã tạo <b>{result.totalSeatsGenerated}</b> ghế
                            </p>
                        </div>
                    )}

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/30 p-4 rounded-lg">
                            <p className="text-sm text-red-400">{error}</p>
                        </div>
                    )}
                </div>

                {/* ================= PREVIEW ================= */}
                <div className="lg:col-span-2 bg-[#111] border border-white/5 rounded-xl p-6">

                    <h2 className="font-bold mb-6">Preview Layout</h2>

                    {/* SCREEN */}
                    <div className="mb-10 text-center">
                        <div className="h-6 bg-white/10 rounded-full w-2/3 mx-auto"></div>
                        <p className="text-xs text-gray-500 mt-2">SCREEN</p>
                    </div>

                    {/* GRID */}
                    <div
                        className="grid gap-2 justify-center"
                        style={{
                            gridTemplateColumns: `auto repeat(${form.cols}, 30px)`
                        }}
                    >
                        <div></div>
                        {Array.from({ length: form.cols }).map((_, i) => (
                            <div key={i} className="text-[10px] text-gray-500 text-center">
                                {i + 1}
                            </div>
                        ))}

                        {rowLabels.map(row => (
                            <React.Fragment key={row}>
                                <div className="text-xs text-gray-400">{row}</div>

                                {Array.from({ length: form.cols }).map((_, i) => (
                                    <div
                                        key={i}
                                        className="w-7 h-7 rounded bg-[#1a1a1a] hover:bg-gray-700 transition"
                                    />
                                ))}
                            </React.Fragment>
                        ))}
                    </div>
                </div>


            </div>
        </div>
    );
}