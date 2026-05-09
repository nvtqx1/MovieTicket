import React, { useState, useEffect, useMemo, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Clock, Tag, Calendar, Ticket, Play } from "lucide-react";
import { getMovieById, getShowtimesByMovieId } from "../services/api/movieService";
import ReviewSection from "../components/movie/ReviewSection";

const formatPrice = (price) =>
    new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price);

const parseLocalDate = (dateStr) => {
    const [year, month, day] = dateStr.split("-").map(Number);
    return new Date(year, month - 1, day);
};

// Backend trả về LocalTime dạng "19:30:00", cắt bỏ giây
const formatTime = (timeStr) => {
    if (!timeStr) return "";
    const parts = timeStr.split(":");
    return `${parts[0]}:${parts[1]}`;
};

// ==========================================
// COMPONENT
// ==========================================
export default function MovieDetail() {
    const { id } = useParams();
    const navigate = useNavigate();

    const bookingPanelRef = useRef(null);

    const [movie, setMovie] = useState(null);
    const [showtimes, setShowtimes] = useState([]);
    const [selectedDate, setSelectedDate] = useState("");
    const [selectedTheater, setSelectedTheater] = useState(""); // Task 2.2: filter by theater
    const [selectedTimeId, setSelectedTimeId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);

                const [movieData, showtimeData] = await Promise.all([
                    getMovieById(id),
                    getShowtimesByMovieId(id),
                ]);

                if (!isMounted) return;

                setMovie(movieData);
                setShowtimes(showtimeData);
                if (showtimeData.length > 0) {
                    const firstTheaterId = showtimeData[0].theater?.id;
                    if (firstTheaterId) setSelectedTheater(String(firstTheaterId));
                    setSelectedDate(showtimeData[0].showDate);
                }
            } catch (err) {
                if (!isMounted) return;
                setError(err.message || "Khôngh thể tải thông tin phim.");
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        fetchData();

        return () => { isMounted = false; };
    }, [id]);

    const uniqueDates = useMemo(() => {
        const dates = showtimes
            .filter((st) => !selectedTheater || String(st.theater?.id) === String(selectedTheater))
            .map((st) => st.showDate);
        return [...new Set(dates)].sort();
    }, [showtimes, selectedTheater]);

    // Task 2.2: Extract unique theaters for filter
    const uniqueTheaters = useMemo(() => {
        const theaterMap = new Map();
        showtimes.forEach((st) => {
            if (st.theater && !theaterMap.has(st.theater.id)) {
                theaterMap.set(st.theater.id, st.theater);
            }
        });
        return Array.from(theaterMap.values());
    }, [showtimes]);

    // Filter showtimes by date AND theater
    const filteredShowtimes = useMemo(() => {
        return showtimes.filter((st) => {
            const dateMatch = st.showDate === selectedDate;
            const theaterMatch = !selectedTheater || (st.theater && String(st.theater.id) === String(selectedTheater));
            return dateMatch && theaterMatch;
        });
    }, [showtimes, selectedDate, selectedTheater]);

    const selectedShowtime = showtimes.find((st) => st.id === selectedTimeId);

    useEffect(() => {
        if (uniqueDates.length > 0 && !uniqueDates.includes(selectedDate)) {
            setSelectedDate(uniqueDates[0]);
            setSelectedTimeId(null);
        }
    }, [uniqueDates, selectedDate]);

    const scrollToBooking = () => {
        bookingPanelRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    if (loading) return (
        <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center">
            <div className="flex flex-col items-center gap-4">
                <div className="w-10 h-10 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
                <p className="text-gray-500 text-sm uppercase tracking-widest">Đang tải...</p>
            </div>
        </div>
    );

    if (error) return (
        <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center">
            <div className="text-center space-y-4">
                <p className="text-red-500 font-bold">{error}</p>
                <button
                    type="button"
                    onClick={() => navigate(-1)}
                    className="text-sm text-gray-400 hover:text-white underline"
                >
                    Quay lại
                </button>
            </div>
        </div>
    );

    if (!movie) return null;

    return (
        <div className="min-h-screen bg-[#0a0a0a] text-white font-sans pb-20">

            {/* HERO */}
            <section className="relative w-full h-[500px] md:h-[520px]">
                <div className="absolute inset-0 overflow-hidden">
                    <img
                        src={movie.posterImageUrl || movie.posterUrl || movie.image || "/fallback.svg"}
                        alt="bg"
                        onError={(e) => { e.target.onerror = null; e.target.src = "/fallback.svg"; }}
                        className="w-full h-full object-cover opacity-30 scale-110 blur-2xl"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0a] via-[#0a0a0a]/70 to-transparent" />
                </div>

                <div className="relative max-w-7xl mx-auto px-6 h-full flex flex-col md:flex-row items-end pb-10 gap-8 z-10">
                    <div className="w-40 md:w-56 flex-shrink-0 rounded-xl overflow-hidden border border-white/10 shadow-2xl translate-y-10">
                        <img
                            src={movie.posterImageUrl || movie.posterUrl || movie.image || "/fallback.svg"}
                            alt={movie.title}
                            onError={(e) => { e.target.onerror = null; e.target.src = "/fallback.svg"; }}
                            className="w-full h-auto"
                        />
                    </div>

                    <div className="flex-grow space-y-4">
                        <h1 className="text-4xl md:text-6xl font-black uppercase tracking-tighter">
                            {movie.title}
                        </h1>

                        <div className="flex flex-wrap items-center gap-4 text-xs text-gray-400">
                            <span className="flex items-center gap-1.5">
                                <Tag size={13} /> {movie.genre}
                            </span>
                            <span className="flex items-center gap-1.5">
                                <Calendar size={13} /> {movie.releaseYear}
                            </span>
                        </div>

                        <div className="flex flex-col sm:flex-row gap-3 pt-2">
                            <button
                                type="button"
                                onClick={scrollToBooking} // âœ… useRef
                                className="px-8 py-3 bg-red-600 hover:bg-red-700 active:scale-95 text-white font-bold rounded-md transition-all flex items-center justify-center gap-2 shadow-lg shadow-red-500/20"
                            >
                                <Ticket size={16} /> Đặt vé ngay
                            </button>
                            <button
                                type="button"
                                className="px-8 py-3 bg-white/10 hover:bg-white/20 active:scale-95 border border-white/10 text-white font-bold rounded-md transition-all flex items-center justify-center gap-2"
                            >
                                <Play size={16} fill="currentColor" /> Xem Trailer
                            </button>
                        </div>
                    </div>
                </div>
            </section>

            {/* MAIN */}
            <main className="max-w-7xl mx-auto px-6 mt-24 grid grid-cols-1 lg:grid-cols-12 gap-12">

                {/* CỘT TRÁI */}
                <div className="lg:col-span-7 space-y-12">
                    <section>
                        <h2 className="text-xl font-bold mb-4 flex items-center gap-3">
                            <span className="w-8 h-[2px] bg-red-600 rounded-full" />
                            Nội dung phim
                        </h2>
                        <p className="text-gray-400 text-sm leading-relaxed text-justify">
                            {movie.description}
                        </p>
                    </section>

                    <section>
                        <div className="w-full aspect-video bg-[#111] rounded-xl border border-white/5 flex items-center justify-center relative overflow-hidden group cursor-pointer">
                            <img
                                src={movie.posterImageUrl || movie.posterUrl || movie.image || "/fallback.svg"}
                                onError={(e) => { e.target.onerror = null; e.target.src = "/fallback.svg"; }}
                                className="absolute inset-0 w-full h-full object-cover opacity-40"
                                alt="trailer"
                            />
                            <div className="w-16 h-12 bg-red-600 rounded-lg flex items-center justify-center z-10 group-hover:scale-110 transition-transform">
                                <Play size={20} fill="white" className="text-white ml-1" />
                            </div>
                            <span className="absolute bottom-4 left-4 bg-red-600 text-white text-[10px] font-bold px-2 py-1 rounded uppercase tracking-widest">
                                Trailer chính thức
                            </span>
                        </div>
                    </section>

                    {/* REVIEWS SECTION */}
                    <ReviewSection movieId={id} />
                </div>

                {/* CỘT PHẢI - BOOKING PANEL */}
                <div className="lg:col-span-5" ref={bookingPanelRef}>
                    <div className="bg-[#111] border border-white/5 rounded-2xl p-6 sticky top-24 shadow-2xl">
                        <h2 className="text-lg font-bold mb-6">Lịch chiếu</h2>

                        {/* Theater Filter */}
                        {uniqueTheaters.length > 0 && (
                            <div className="mb-4">
                                <select
                                    value={selectedTheater}
                                    onChange={(e) => { setSelectedTheater(e.target.value); setSelectedTimeId(null); }}
                                    className="w-full px-4 py-2.5 rounded-xl bg-[#1a1a1a] border border-white/10 text-sm text-white focus:outline-none focus:border-red-500 appearance-none cursor-pointer"
                                >
                                    {uniqueTheaters.map((t) => (
                                        <option key={t.id} value={t.id}>{t.name}</option>
                                    ))}
                                </select>
                            </div>
                        )}

                        {/* Date Strip */}
                        <div className="flex gap-3 overflow-x-auto pb-3 mb-6">
                            {uniqueDates.map((date) => {
                                // âœ… Fix timezone
                                const dateObj = parseLocalDate(date);
                                const day = dateObj.getDate();
                                const dayOfWeek = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"][dateObj.getDay()];
                                const isSelected = selectedDate === date;

                                return (
                                    <button
                                        key={date}
                                        type="button"
                                        onClick={() => { setSelectedDate(date); setSelectedTimeId(null); }}
                                        className={`flex-shrink-0 flex flex-col items-center justify-center w-14 py-3 rounded-xl transition-all ${isSelected
                                            ? "bg-red-600 text-white shadow-lg shadow-red-500/20"
                                            : "bg-[#1a1a1a] text-gray-400 hover:bg-white/10"
                                            }`}
                                    >
                                        <span className="text-[9px] font-bold tracking-widest">{dayOfWeek}</span>
                                        <span className="text-xl font-black mt-1">{day}</span>
                                    </button>
                                );
                            })}
                        </div>


                        <div className="space-y-3 max-h-[300px] overflow-y-auto pr-1">
                            {filteredShowtimes.length === 0 ? (
                                <p className="text-sm text-gray-500 text-center py-6">
                                    Không có suất chiếu nào vào ngày này.
                                </p>
                            ) : (
                                <div className="flex flex-wrap gap-2">
                                    {filteredShowtimes.map((st) => {
                                        const isSelected = selectedTimeId === st.id;

                                        return (
                                            <button
                                                key={st.id}
                                                type="button"
                                                onClick={() => setSelectedTimeId(st.id)}
                                                className={`relative px-4 py-2 rounded text-xs font-bold transition-all ${isSelected
                                                    ? "bg-white text-black"
                                                    : st.isFlashSale
                                                        ? "bg-[#1a1a1a] border border-yellow-500/50 text-yellow-400 hover:bg-yellow-500/10"
                                                        : "bg-[#1a1a1a] border border-white/10 text-gray-300 hover:border-white/30"
                                                    }`}
                                            >
                                                {formatTime(st.showTime)}
                                                {st.isFlashSale && (
                                                    <span className="absolute -top-1.5 -right-1.5 w-2.5 h-2.5 bg-yellow-500 rounded-full animate-pulse" />
                                                )}
                                            </button>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        {/* Summary + CTA */}
                        <div className="mt-6 pt-4 border-t border-white/5 space-y-3">
                            {selectedShowtime && (
                                <>
                                    {selectedShowtime.theater && (
                                        <div className="flex justify-between items-center text-sm">
                                            <span className="text-gray-400">Rạp</span>
                                            <span className="text-white text-right">{selectedShowtime.theater.name}</span>
                                        </div>
                                    )}
                                    {selectedShowtime.roomName && (
                                        <div className="flex justify-between items-center text-sm">
                                            <span className="text-gray-400">Phòng</span>
                                            <span className="text-white">{selectedShowtime.roomName}</span>
                                        </div>
                                    )}
                                    {selectedShowtime.availableSeats != null && (
                                        <div className="flex justify-between items-center text-sm">
                                            <span className="text-gray-400">Ghế trống</span>
                                            <span className="text-white">{selectedShowtime.availableSeats}/{selectedShowtime.totalSeats}</span>
                                        </div>
                                    )}
                                    <div className="flex justify-between items-center text-sm">
                                        <span className="text-gray-400">Giá vé</span>
                                        <span className="font-bold text-white">
                                            {formatPrice(selectedShowtime.price)}
                                        </span>
                                    </div>
                                </>
                            )}

                            <button
                                type="button"
                                disabled={!selectedTimeId}
                                onClick={() => navigate(`/booking/${selectedTimeId}`)}
                                className={`w-full py-3.5 rounded-lg font-black uppercase tracking-widest text-sm transition-all ${selectedTimeId
                                    ? "bg-red-600 text-white hover:bg-red-700 shadow-lg shadow-red-500/20"
                                    : "bg-[#1a1a1a] text-gray-600 cursor-not-allowed"
                                    }`}
                            >
                                {selectedTimeId ? "Tiếp tục chọn ghế" : "Chọn suất chiếu"}
                            </button>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
