import React, { useState, useEffect, useRef } from "react";
import { Search, Bell, LogOut, Menu, X } from "lucide-react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { useAuthContext } from "../../context/AuthContext";
import defaultAvatar from "../../assets/images/avatarDefault.jpeg";

const navLinks = [
    { name: "Home", href: "/" },
    { name: "Phim", href: "/movies" },
    { name: "Rạp", href: "/theaters" },
];

export default function Navbar() {
    const { pathname } = useLocation();
    const navigate = useNavigate();
    const { user, logout } = useAuthContext();

    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userAvatar, setUserAvatar] = useState("");
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    const dropdownRef = useRef(null);

    // Handle user
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

    // Click outside để đóng dropdown
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
                setIsDropdownOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleLogout = () => {
        logout();
        setIsDropdownOpen(false);
        setIsMenuOpen(false);
        navigate("/");
    };

    return (
        <nav className="fixed top-0 left-0 w-full z-[999] bg-neutral-950/90 backdrop-blur-md border-b border-white/5">
            <div className="w-full max-w-[1440px] mx-auto flex justify-between items-center px-4 md:px-12 py-3 md:py-4">

                {/* LEFT */}
                <div className="flex items-center gap-4 lg:gap-10 min-w-0">
                    <Link
                        to="/"
                        className="text-lg md:text-2xl font-black italic text-red-600 tracking-tighter uppercase truncate"
                    >
                        TMT CINEMA
                    </Link>

                    {/* Desktop menu */}
                    <div className="hidden md:flex gap-6">
                        {navLinks.map((link) => (
                            <Link
                                key={link.name}
                                to={link.href}
                                className={`text-[10px] lg:text-xs uppercase tracking-[0.2em] transition-colors ${pathname === link.href
                                    ? "text-red-500 font-bold"
                                    : "text-gray-400 hover:text-white"
                                    }`}
                            >
                                {link.name}
                            </Link>
                        ))}
                    </div>
                </div>

                {/* RIGHT */}
                <div className="flex items-center gap-2 md:gap-6">

                    {/* USER */}
                    {isLoggedIn ? (
                        <div className="relative" ref={dropdownRef}>
                            {/* Avatar */}
                            <div
                                onClick={() => setIsDropdownOpen((prev) => !prev)}
                                className="w-8 h-8 md:w-10 md:h-10 rounded-full overflow-hidden border border-white/10 cursor-pointer"
                            >
                                <img
                                    src={userAvatar}
                                    alt="avatar"
                                    className="w-full h-full object-cover"
                                />
                            </div>

                            {/* Dropdown */}
                            <div
                                className={`absolute right-0 mt-2 w-48 bg-neutral-900 border border-white/10 rounded-xl shadow-2xl z-[1000] transition-all duration-200 ${isDropdownOpen
                                    ? "opacity-100 visible translate-y-0"
                                    : "opacity-0 invisible -translate-y-2"
                                    }`}
                            >
                                <Link
                                    to="/profile"
                                    className="block px-4 py-3 text-sm text-gray-300 hover:bg-red-600"
                                    onClick={() => setIsDropdownOpen(false)}
                                >
                                    Hồ sơ cá nhân
                                </Link>

                                <button
                                    onClick={handleLogout}
                                    className="w-full text-left px-4 py-3 text-sm text-gray-300 hover:bg-red-600 flex items-center gap-2 cursor-pointer"
                                >
                                    <LogOut size={16} /> Đăng xuất
                                </button>
                            </div>
                        </div>
                    ) : (
                        <button
                            onClick={() => navigate("/login")}
                            className="hidden sm:block px-4 py-1.5 bg-red-600 text-white text-[10px] font-bold rounded-full uppercase tracking-widest cursor-pointer"
                        >
                            Đăng nhập
                        </button>
                    )}

                    {/* MOBILE MENU BUTTON */}
                    <button
                        className="md:hidden text-gray-300 hover:text-white p-1"
                        onClick={() => setIsMenuOpen(!isMenuOpen)}
                    >
                        {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
                    </button>
                </div>
            </div>

            {/* MOBILE MENU */}
            <div
                className={`md:hidden overflow-hidden transition-all duration-300 bg-neutral-950 border-t border-white/5 ${isMenuOpen ? "max-h-[300px] opacity-100" : "max-h-0 opacity-0"
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

                    {isLoggedIn ? (
                        <>
                            <Link
                                to="/profile"
                                onClick={() => setIsMenuOpen(false)}
                                className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400 hover:text-red-500"
                            >
                                Hồ sơ cá nhân
                            </Link>

                            <button
                                onClick={handleLogout}
                                className="w-full py-3 bg-zinc-900 text-gray-300 font-bold rounded-lg uppercase text-[10px] flex items-center justify-center gap-2"
                            >
                                <LogOut size={16} /> Đăng xuất
                            </button>
                        </>
                    ) : (
                        <button
                            onClick={() => {
                                navigate("/login");
                                setIsMenuOpen(false);
                            }}
                            className="w-full py-3 bg-red-600 text-white font-bold rounded-lg uppercase text-[10px]"
                        >
                            Đăng nhập
                        </button>
                    )}
                </div>
            </div>
        </nav>
    );
}
