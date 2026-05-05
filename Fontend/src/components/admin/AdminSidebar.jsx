import React from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuthContext } from "../../context/AuthContext";
import { BarChart3, LayoutDashboard, Film, Grid2X2, Calendar, Ticket } from "lucide-react";

const menuItems = [
    { name: "Dashboard", path: "/admin/dashboard", icon: LayoutDashboard },
    { name: "Thống kê", path: "/admin/revenue-analytics", icon: BarChart3 },
    { name: "Phim", path: "/admin/movies", icon: Film },
    { name: "Lịch Chiếu", path: "/admin/showtimes", icon: Calendar },
    { name: "Voucher", path: "/admin/vouchers", icon: Ticket },
    { name: "Ghế", path: "/admin/seat-creator", icon: Grid2X2 },
];

export default function AdminSidebar() {
    const navigate = useNavigate();
    const { logout } = useAuthContext();

    const handleLogout = async () => {
        await logout();
        navigate("/");
    };

    return (
        <aside className="w-64 bg-[#0d0d0d] border-r border-white/5 flex flex-col h-dvh sticky top-0">

            {/* Logo */}
            <div className="p-6 border-b border-white/5 text-center">
                <h2 className="text-lg font-black text-red-600 uppercase">
                    TMT ADMIN
                </h2>
            </div>

            {/* Menu */}
            <nav className="flex-grow py-6 px-3 space-y-2">
                {menuItems.map((item) => (
                    <NavLink
                        key={item.name}
                        to={item.path}
                        end
                        className={({ isActive }) =>
                            `flex items-center gap-3 px-4 py-3 rounded-lg transition-all
              ${isActive
                                ? "bg-red-600/10 text-red-500 border border-red-600/20"
                                : "text-gray-500 hover:bg-white/5 hover:text-white"
                            }`
                        }
                    >
                        <item.icon size={18} />
                        <span className="text-xs font-bold uppercase tracking-widest">
                            {item.name}
                        </span>
                    </NavLink>
                ))}
            </nav>

            {/* Logout */}
            <div className="p-4 border-t border-white/5">
                <button
                    onClick={handleLogout}
                    className="w-full py-2 bg-zinc-900 text-gray-400 text-[10px] font-black uppercase rounded hover:bg-red-600 hover:text-white transition-colors"
                >
                    Đăng xuất
                </button>
            </div>
        </aside>
    );
}
