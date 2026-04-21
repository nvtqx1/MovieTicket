import React from "react";
// Dùng lucide-react để icon ổn định
import { Search, Bell } from "lucide-react";

export default function Navbar() {
    // Tách mảng menu để dễ quản lý và thêm bớt sau này
    const navLinks = [
        { name: "Phim", href: "#", active: true },
        { name: "Rạp chiếu", href: "#", active: false },
        { name: "Ưu đãi", href: "#", active: false },
        { name: "Thành viên VIP", href: "#", active: false },
    ];

    return (
        <nav className="fixed top-0 w-full z-50 bg-neutral-950/60 backdrop-blur-xl shadow-2xl shadow-red-900/10 flex justify-between items-center px-8 py-4 max-w-full">
            <div className="flex items-center gap-12">
                <span className="text-2xl font-black italic text-red-600 font-headline tracking-tighter uppercase cursor-pointer">
                    TMT CINEMA
                </span>

                {/* Sử dụng map để render menu */}
                <div className="hidden md:flex gap-8">
                    {navLinks.map((link) => (
                        <a
                            key={link.name}
                            href={link.href}
                            className={`font-headline tracking-tighter uppercase transition-colors ${link.active
                                ? "text-red-500 font-bold border-b-2 border-red-600 pb-1"
                                : "text-on-surface-variant hover:text-white font-medium"
                                }`}
                        >
                            {link.name}
                        </a>
                    ))}
                </div>
            </div>

            <div className="flex items-center gap-6">
                {/* Ô tìm kiếm sử dụng biến màu custom */}
                <div className="hidden md:flex items-center bg-surface-container px-4 py-2 rounded-xl border border-white/5 focus-within:border-primary/50 transition-colors">
                    <Search className="text-outline w-4 h-4" />
                    <input
                        className="bg-transparent outline-none text-sm w-48 ml-2 text-on-surface placeholder:text-outline"
                        placeholder="Tìm kiếm phim..."
                    />
                </div>

                {/* Nút thông báo */}
                <button className="text-on-surface-variant hover:text-white transition-transform active:scale-90 cursor-pointer">
                    <Bell size={20} />
                </button>

                {/* Profile */}
                <div className="w-10 h-10 rounded-full overflow-hidden border border-outline-variant hover:border-primary transition-colors cursor-pointer">
                    <img src="https://i.pravatar.cc/150?img=11" alt="user" className="w-full h-full object-cover" />
                </div>
            </div>
        </nav>
    );
}