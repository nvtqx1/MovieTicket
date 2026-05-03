import { Play, Ticket } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function Hero({ movie, loading }) {
    const navigate = useNavigate();

    // Loading state
    if (loading) return (
        <section className="relative min-h-[100svh] w-full bg-black animate-pulse" />
    );

    // Fallback nếu không có data
    if (!movie) return null;

    return (
        <section
            className="relative min-h-[100svh] w-full flex items-end overflow-hidden bg-black"
            aria-label={`Banner phim ${movie.title}`}
        >
            <div className="absolute inset-0 z-0">
                <img
                    src={movie.posterImageUrl || movie.posterUrl || movie.image || "/fallback.svg"}
                    alt={movie.title}
                    className="w-full h-full object-cover object-center opacity-60"
                    onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = "/fallback.svg";
                    }}
                />
                {/* Giữ nguyên gradient */}
                <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0a] via-[#0a0a0a]/50 to-transparent" />
                <div className="absolute inset-0 bg-gradient-to-r from-[#0a0a0a]/80 via-transparent to-transparent hidden md:block" />
            </div>

            <div className="relative w-full max-w-[1440px] mx-auto px-4 sm:px-6 md:px-12 lg:px-16 pb-16 md:pb-24">
                <div className="max-w-3xl space-y-4 md:space-y-6">
                    <p className="text-red-600 font-bold tracking-[0.2em] uppercase text-[10px] md:text-sm">
                        Đang khởi chiếu tại TMT CINEMA
                    </p>

                    <h1 className="text-5xl md:text-8xl lg:text-9xl font-black text-white leading-none tracking-tighter uppercase">
                        {movie.title}
                    </h1>

                    {/* Nếu API trả về description thì dùng, không thì bỏ */}
                    {movie.description && (
                        <p className="text-gray-300 text-sm md:text-xl max-w-xl leading-relaxed line-clamp-3 md:line-clamp-none">
                            {movie.description}
                        </p>
                    )}

                    <div className="flex flex-col sm:flex-row gap-3 md:gap-4 pt-4">
                        <button
                            type="button"
                            onClick={() => navigate(`/booking/${movie.id}`)}
                            className="w-full sm:w-auto px-8 py-3 md:py-4 bg-red-600 hover:bg-red-700 active:scale-95 text-white font-bold uppercase tracking-widest rounded transition-all flex items-center justify-center gap-3 shadow-lg shadow-red-600/20 cursor-pointer"
                        >
                            <Ticket size={18} /> ĐẶT VÉ NGAY
                        </button>

                        <button
                            type="button"
                            className="w-full sm:w-auto px-8 py-3 md:py-4 bg-white/10 hover:bg-white/20 active:scale-95 backdrop-blur-md border border-white/20 text-white font-bold uppercase tracking-widest rounded transition-all flex items-center justify-center gap-3 cursor-pointer"
                        >
                            <Play size={18} fill="currentColor" /> XEM TRAILER
                        </button>
                    </div>
                </div>
            </div>
        </section>
    );
}
