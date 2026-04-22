import React, { useState, useEffect } from "react";
import { Search, Bell, LogOut } from "lucide-react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuthContext } from "../../context/AuthContext";
import { Link } from "react-router-dom";

const navLinks = [
    { name: "Phim", href: "#" },
    { name: "Rạp chiếu", href: "#" },
    { name: "Ưu đãi", href: "#" },
];

export default function Navbar() {
    const { pathname } = useLocation();
    const navigate = useNavigate();

    const { user, logout } = useAuthContext();

    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userAvatar, setUserAvatar] = useState("");

    useEffect(() => {
        if (user) {
            setIsLoggedIn(true);
            try {
                const userData = typeof user === "string" ? JSON.parse(user) : user;
                setUserAvatar(userData.avatar || "../../assets/images/avatarDefault.jpeg");
            } catch {
                setUserAvatar("../../assets/images/avatarDefault.jpeg");
            }
        } else {
            setIsLoggedIn(false);
        }
    }, [user]); // 👈 fix dependency

    const handleLogin = () => navigate("/login");

    const handleLogout = () => {
        logout();
    };

    return (
        <nav className="fixed top-0 w-full z-50 bg-neutral-950/60 backdrop-blur-xl shadow-2xl shadow-red-900/10 flex justify-between items-center px-8 py-4 max-w-full">

            {/* LEFT */}
            <div className="flex items-center gap-12">
                <span className="text-2xl font-black italic text-red-600 font-headline tracking-tighter uppercase cursor-pointer"><Link to="/">TMT CINEMA</Link></span>
                <div className="hidden md:flex gap-8">
                    {navLinks.map((link) => (
                        <a
                            key={link.name}
                            href={link.href}
                            className={`font-headline tracking-tighter uppercase transition-colors ${pathname === link.href
                                ? "text-red-500 font-bold border-b-2 border-red-600 pb-1"
                                : "text-gray-400 hover:text-white font-medium"
                                }`}
                        >
                            {link.name}
                        </a>
                    ))}
                </div>
            </div>

            {/* RIGHT */}
            <div className="flex items-center gap-6">
                <div className="hidden md:flex items-center bg-gray-900 px-4 py-2 rounded-xl border border-gray-700 focus-within:border-red-500 transition-colors">
                    <Search className="text-gray-500 w-4 h-4" />
                    <input
                        className="bg-transparent outline-none text-sm w-48 ml-2 text-white placeholder:text-gray-500"
                        placeholder="Tìm kiếm phim..."
                    />
                </div>

                <button className="text-gray-400 hover:text-white transition-transform active:scale-90 cursor-pointer">
                    <Bell size={20} />
                </button>

                {isLoggedIn ? (
                    <div className="relative group">
                        <div className="w-10 h-10 rounded-full overflow-hidden border border-gray-700 hover:border-red-500 transition-colors cursor-pointer">
                            <img
                                src={userAvatar}
                                alt="user"
                                className="w-full h-full object-cover"
                            />
                        </div>

                        <div className="absolute right-0 mt-2 w-40 bg-neutral-900 border border-gray-700 rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50">
                            <a
                                href="/profile"
                                className="block px-4 py-2 text-sm text-white hover:bg-red-600 rounded-t-lg transition-colors"
                            >
                                Hồ sơ cá nhân
                            </a>
                            <button
                                onClick={handleLogout}
                                className="w-full text-left px-4 py-2 text-sm text-white hover:bg-red-600 rounded-b-lg transition-colors flex items-center gap-2"
                            >
                                <LogOut size={16} />
                                Đăng xuất
                            </button>
                        </div>
                    </div>
                ) : (
                    <button
                        onClick={handleLogin}
                        className="px-6 py-2 bg-red-500 hover:bg-red-600 text-white font-bold rounded-full transition-colors active:scale-95 cursor-pointer"
                    >
                        Đăng nhập
                    </button>
                )}
            </div>
        </nav>
    );
}