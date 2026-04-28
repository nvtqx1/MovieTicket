import React from "react";
import { Play, Ticket } from "lucide-react";

export default function Hero({
    title = "MICHAEL",
    description = "Sự kiện điện ảnh tái hiện hành trình vĩ đại của Ông hoàng nhạc Pop - Michael Jackson.",
    bgImage = "https://images.unsplash.com/photo-1598387181032-a3103a2db5b3?q=80&w=2076&auto=format&fit=crop"
}) {
    return (
        <section
            className="relative min-h-[100svh] w-full flex items-end overflow-hidden bg-black"
            aria-label={`Banner phim ${title}`}
        >
            {/* Background Image & Overlay */}
            <div className="absolute inset-0 z-0">
                <img
                    className="w-full h-full object-cover object-center opacity-60"
                    src={bgImage}
                    alt={`Background phim ${title}`}
                />
                {/* Lớp phủ gradient để bảo vệ độ đọc của chữ */}
                <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0a] via-[#0a0a0a]/50 to-transparent" />
                <div className="absolute inset-0 bg-gradient-to-r from-[#0a0a0a]/80 via-transparent to-transparent hidden md:block" />
            </div>

            {/* Content Container - Sửa lỗi mx-auto và px */}
            <div className="relative z-10 w-full max-w-[1440px] mx-auto px-6 md:px-12 lg:px-16 pb-16 md:pb-24">
                <div className="max-w-3xl space-y-4 md:space-y-6">

                    {/* Tagline */}
                    <p className="text-red-600 font-bold tracking-[0.2em] md:tracking-[0.4em] uppercase text-[10px] md:text-sm">
                        Đang khởi chiếu tại TMT CINEMA
                    </p>

                    {/* Title - Responsive Font Size */}
                    <h1 className="text-5xl md:text-8xl lg:text-9xl font-black text-white leading-none tracking-tighter uppercase drop-shadow-2xl">
                        {title}
                    </h1>

                    {/* Description - Giới hạn dòng trên mobile */}
                    <p className="text-gray-300 text-sm md:text-xl max-w-xl leading-relaxed line-clamp-3 md:line-clamp-none drop-shadow-md">
                        {description}
                    </p>

                    {/* Buttons - Chuyển sang full-width trên mobile cực nhỏ */}
                    <div className="flex flex-col sm:flex-row gap-3 md:gap-4 pt-4">
                        <button className="w-full sm:w-auto px-8 py-3 md:py-4 bg-red-600 hover:bg-red-700 active:scale-95 text-white font-bold uppercase tracking-widest rounded transition-all flex items-center justify-center gap-3 shadow-lg shadow-red-600/20 cursor-pointer">
                            <Ticket size={18} />
                            ĐẶT VÉ NGAY
                        </button>

                        <button className="w-full sm:w-auto px-8 py-3 md:py-4 bg-white/10 hover:bg-white/20 active:scale-95 backdrop-blur-md border border-white/20 text-white font-bold uppercase tracking-widest rounded transition-all flex items-center justify-center gap-3 cursor-pointer">
                            <Play size={18} fill="currentColor" />
                            XEM TRAILER
                        </button>
                    </div>
                </div>
            </div>
        </section>
    );
}