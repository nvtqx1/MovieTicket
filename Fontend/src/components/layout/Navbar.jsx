import React, { useState, useEffect } from "react";
import { Search, Bell, LogOut, Menu, X } from "lucide-react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { useAuthContext } from "../../context/AuthContext";
import defaultAvatar from "../../assets/images/avatarDefault.jpeg";

const navLinks = [
    { name: "Phim", href: "/movies" },
    { name: "Rạp chiếu", href: "/cinemas" },
    { name: "Ưu đãi", href: "/promotions" },
];

export default function Navbar() {
    const { pathname } = useLocation();
    const navigate = useNavigate();
    const { user, logout } = useAuthContext();

    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userAvatar, setUserAvatar] = useState("");
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    useEffect(() => {
        if (user) {
            setIsLoggedIn(true);
            try {
                const userData = typeof user === "string" ? JSON.parse(user) : user;
                setUserAvatar(userData.avatar || defaultAvatar);
            } catch {
                setUserAvatar(defaultAvatar);
            }
        } else {
            setIsLoggedIn(false);
        }
    }, [user]);

    const handleLogout = () => {
        logout();
        setIsMenuOpen(false);
        navigate("/");
    };

    return (
        <nav className="fixed top-0 left-0 w-full z-[100] bg-neutral-950/90 backdrop-blur-md border-b border-white/5 overflow-x-hidden">

            <div className="w-full max-w-[1440px] mx-auto flex justify-between items-center px-4 md:px-12 py-3 md:py-4 box-border">

                <div className="flex items-center gap-4 lg:gap-10 min-w-0">
                    <Link
                        to="/"
                        className="text-lg md:text-2xl font-black italic text-red-600 tracking-tighter uppercase truncate"
                    >
                        TMT CINEMA
                    </Link>

                    <div className="hidden md:flex gap-6">
                        {navLinks.map((link) => (
                            <Link
                                key={link.name}
                                to={link.href}
                                className={`text-[10px] lg:text-xs uppercase tracking-[0.2em] transition-colors shrink-0 ${pathname === link.href ? "text-red-500 font-bold" : "text-gray-400 hover:text-white"
                                    }`}
                            >
                                {link.name}
                            </Link>
                        ))}
                    </div>
                </div>

                <div className="flex items-center gap-2 md:gap-6 shrink-0">

                    <div className="hidden lg:flex items-center bg-white/5 px-3 py-1.5 rounded-lg border border-white/10 focus-within:border-red-500/50">
                        <Search className="text-gray-500 w-4 h-4" />
                        <input
                            className="bg-transparent outline-none text-[10px] w-24 xl:w-40 ml-2 text-white"
                            placeholder="Tìm phim..."
                        />
                    </div>

                    {/* Ẩn thông báo trên mobile*/}
                    <button className="hidden sm:block text-gray-400 hover:text-white p-1">
                        <Bell size={18} />
                    </button>

                    {isLoggedIn ? (
                        <div className="relative group">
                            <div className="w-8 h-8 md:w-10 md:h-10 rounded-full overflow-hidden border border-white/10 cursor-pointer">
                                <img src={userAvatar} alt="avatar" className="w-full h-full object-cover" />
                            </div>
                            {/* Dropdown Desktop */}
                            <div className="absolute right-0 mt-2 w-48 bg-neutral-900 border border-white/10 rounded-xl shadow-2xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all overflow-hidden">
                                <Link to="/profile" className="block px-4 py-3 text-sm text-gray-300 hover:bg-red-600 transition-colors">Hồ sơ cá nhân</Link>
                                <button onClick={handleLogout} className="w-full text-left px-4 py-3 text-sm text-gray-300 hover:bg-red-600 transition-colors flex items-center gap-2">
                                    <LogOut size={16} /> Đăng xuất
                                </button>
                            </div>
                        </div>
                    ) : (
                        <button
                            onClick={() => navigate("/login")}
                            className="px-4 py-1.5 bg-red-600 text-white text-[10px] font-bold rounded-full uppercase tracking-widest active:scale-95 transition-transform cursor-pointer"
                        >
                            Đăng nhập
                        </button>
                    )}

                    {/* Mobile Menu Toggle */}
                    <button
                        className="md:hidden text-gray-300 hover:text-white p-1 active:scale-90 transition-transform"
                        onClick={() => setIsMenuOpen(!isMenuOpen)}
                        aria-label="Toggle menu"
                    >
                        {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
                    </button>
                </div>
            </div>

            {/* MOBILE MENU ANIMATION */}
            <div
                className={`md:hidden overflow-hidden transition-all duration-300 ease-in-out bg-neutral-950 border-t border-white/5 ${isMenuOpen ? "max-h-[300px] opacity-100" : "max-h-0 opacity-0"
                    }`}
            >
                <div className="p-6 flex flex-col gap-5">
                    {navLinks.map((link) => (
                        <Link
                            key={link.name}
                            to={link.href}
                            onClick={() => setIsMenuOpen(false)}
                            className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400 hover:text-red-500"
                        >
                            {link.name}
                        </Link>
                    ))}
                    {!isLoggedIn && (
                        <button
                            onClick={() => { navigate("/login"); setIsMenuOpen(false); }}
                            className="w-full py-3 bg-red-600 text-white font-bold rounded-lg uppercase text-[10px] tracking-[0.2em]"
                        >
                            Đăng nhập
                        </button>
                    )}
                </div>
            </div>
        </nav>
    );
}