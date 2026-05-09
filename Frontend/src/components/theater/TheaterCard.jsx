import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Calendar, MapPin, Clock, X, Film } from "lucide-react";
import { getTheaterSchedule } from "../../services/api/theaterService";
import fallbackImg from "../../assets/images/fallback-theater.jpg";

export default function TheaterCard({ theater }) {
    const navigate = useNavigate();
    const [schedule, setSchedule] = useState(null);
    const [loadingSchedule, setLoadingSchedule] = useState(false);
    const [showSchedule, setShowSchedule] = useState(false);

    const handleShowSchedule = async () => {
        if (schedule) {
            setShowSchedule(true);
            return;
        }
        setLoadingSchedule(true);
        try {
            const data = await getTheaterSchedule(theater.id);
            setSchedule(data);
            setShowSchedule(true);
        } catch (err) {
            console.error("Error loading schedule:", err);
            alert("Lỗi tải lịch chiếu");
        } finally {
            setLoadingSchedule(false);
        }
    };

    const formatDate = (dateStr) => {
        const d = new Date(dateStr + "T00:00:00");
        const days = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];
        return {
            day: days[d.getDay()],
            date: `${d.getDate()}/${d.getMonth() + 1}`,
            full: dateStr,
        };
    };

    return (
        <>
            <div className="bg-[#18181b] rounded-xl overflow-hidden border border-zinc-800 flex flex-col">
                <div className="aspect-video bg-zinc-900">
                    {theater.image ? (
                        <img
                            src={theater.image}
                            alt={theater.name}
                            className="w-full h-full object-cover"
                            onError={(e) => { e.target.onerror = null; e.target.src = fallbackImg; }}
                        />
                    ) : (
                        <img src={fallbackImg} alt="fallback" className="w-full h-full object-cover" />
                    )}
                </div>

                <div className="p-5 flex flex-col flex-grow">
                    <h3 className="font-bold text-white mb-2">{theater.name}</h3>
                    <p className="text-sm text-gray-400 mb-2 flex items-center gap-1">
                        <MapPin size={12} /> {theater.location}
                    </p>
                    <p className="text-sm text-gray-400">
                        {theater.capacity} ghế
                    </p>

                    <div className="flex gap-3 mt-auto pt-4">
                        <button
                            type="button"
                            onClick={handleShowSchedule}
                            disabled={loadingSchedule}
                            className="flex-1 bg-red-600 hover:bg-red-700 py-2.5 text-sm font-bold rounded-lg transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
                        >
                            {loadingSchedule ? (
                                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                            ) : (
                                <><Calendar size={14} /> Lịch chiếu</>
                            )}
                        </button>
                    </div>
                </div>
            </div>

            {/* Schedule Modal */}
            {showSchedule && schedule && (
                <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4" onClick={() => setShowSchedule(false)}>
                    <div className="bg-[#111] border border-white/10 rounded-2xl max-w-2xl w-full max-h-[80vh] overflow-hidden" onClick={(e) => e.stopPropagation()}>
                        {/* Header */}
                        <div className="p-6 border-b border-white/5 flex justify-between items-start">
                            <div>
                                <h2 className="text-xl font-black text-white">{schedule.theaterName}</h2>
                                <p className="text-sm text-gray-400 mt-1 flex items-center gap-1">
                                    <MapPin size={12} /> {schedule.location}
                                </p>
                            </div>
                            <button onClick={() => setShowSchedule(false)} className="text-gray-500 hover:text-white p-1">
                                <X size={20} />
                            </button>
                        </div>

                        {/* Schedule Content */}
                        <div className="p-6 overflow-y-auto max-h-[60vh] space-y-6">
                            {Object.keys(schedule.schedule || {}).length === 0 ? (
                                <p className="text-center text-gray-500 py-8">Không có lịch chiếu trong 6 ngày tới</p>
                            ) : (
                                Object.entries(schedule.schedule).map(([date, showtimes]) => {
                                    const dateInfo = formatDate(date);
                                    return (
                                        <div key={date}>
                                            <h3 className="text-sm font-bold text-red-500 uppercase tracking-wider mb-3 flex items-center gap-2">
                                                <Calendar size={14} />
                                                {dateInfo.day} - {dateInfo.date}
                                            </h3>
                                            <div className="space-y-3">
                                                {showtimes.map((st) => (
                                                    <div
                                                        key={st.id}
                                                        className="bg-white/5 rounded-xl p-4 flex items-center justify-between hover:bg-white/10 transition-colors cursor-pointer"
                                                        onClick={() => {
                                                            setShowSchedule(false);
                                                            navigate(`/booking/${st.id}`);
                                                        }}
                                                    >
                                                        <div className="flex items-center gap-4">
                                                            <div className="text-center">
                                                                <p className="text-white font-bold text-lg">{st.showTime?.substring(0, 5)}</p>
                                                                <p className="text-[10px] text-gray-500">{st.roomName}</p>
                                                            </div>
                                                            <div>
                                                                <p className="text-white font-medium flex items-center gap-2">
                                                                    <Film size={14} className="text-red-500" />
                                                                    {st.movie?.title || "Phim"}
                                                                </p>
                                                                <p className="text-xs text-gray-500 mt-0.5">
                                                                    {st.movie?.genre} • {st.availableSeats || 0} ghế trống
                                                                </p>
                                                            </div>
                                                        </div>
                                                        <div className="text-right">
                                                            <p className="text-red-500 font-bold">
                                                                {new Intl.NumberFormat("vi-VN").format(st.price || 0)}đ
                                                            </p>
                                                            {st.isFlashSale && (
                                                                <span className="text-[10px] bg-yellow-500/20 text-yellow-400 px-2 py-0.5 rounded font-bold">FLASH SALE</span>
                                                            )}
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    );
                                })
                            )}
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}