import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authService } from "../services/api/authService";
import { getMyTickets, cancelReservation } from "../services/api/reservationService";
import { useAuthContext } from "../context/AuthContext";
import { Ticket, User as UserIcon, LogOut } from "lucide-react";

const formatPrice = (price) =>
    new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price);

export default function Profile() {
    const navigate = useNavigate();
    const { user: authUser, logout, isAuthenticated } = useAuthContext();

    const [user, setUser] = useState(null);
    const [tickets, setTickets] = useState([]);
    const [form, setForm] = useState({});
    const [loading, setLoading] = useState(true);
    const [ticketsLoading, setTicketsLoading] = useState(true);
    const [activeTab, setActiveTab] = useState("profile");

    // Redirect if not authenticated
    useEffect(() => {
        if (!isAuthenticated) {
            navigate("/login");
        }
    }, [isAuthenticated, navigate]);

    // Fetch user info
    useEffect(() => {
        const fetchUser = async () => {
            try {
                const userData = await authService.getMe();
                setUser(userData);
                setForm({
                    userName: userData.userName || userData.username || "",
                    email: userData.email || "",
                    phoneNumber: userData.phoneNumber || "",
                });
            } catch (err) {
                console.error("Error fetching user:", err);
                // Fallback to auth context data
                if (authUser) {
                    setUser({
                        email: authUser.email || "N/A",
                        roles: authUser.roles || [],
                    });
                    setForm({
                        email: authUser.email || "",
                    });
                }
            } finally {
                setLoading(false);
            }
        };

        if (isAuthenticated) {
            fetchUser();
        }
    }, [isAuthenticated, authUser]);

    // Fetch tickets
    useEffect(() => {
        const fetchTickets = async () => {
            try {
                const data = await getMyTickets();
                setTickets(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Error fetching tickets:", err);
            } finally {
                setTicketsLoading(false);
            }
        };

        if (isAuthenticated) {
            fetchTickets();
        }
    }, [isAuthenticated]);

    const handleCancelTicket = async (reservationId) => {
        if (!window.confirm("Bạn có chắc chắn muốn hủy đơn đặt vé này?")) return;
        
        try {
            const res = await cancelReservation(reservationId);
            if (res.apiStatus === "SUCCESS") {
                alert("Hủy vé thành công");
                // Cập nhật lại danh sách vé
                const data = await getMyTickets();
                setTickets(Array.isArray(data) ? data : []);
            } else {
                alert(res.message || "Không thể hủy vé");
            }
        } catch (error) {
            console.error("Lỗi khi hủy vé:", error);
            alert("Đã xảy ra lỗi khi hủy vé.");
        }
    };

    const handleChange = (e) =>
        setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        // TODO: Backend doesn't have profile update endpoint yet
        alert("Chức năng cập nhật hồ sơ sẽ được bổ sung!");
    };

    const handleLogout = () => {
        logout();
        navigate("/");
    };

    if (loading && !user) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-[#0a0a0a]">
                <div className="w-10 h-10 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
            </div>
        );
    }

    const roles = user?.authorities?.map(a => a.authority) || user?.roles || authUser?.roles || [];

    return (
        <div className="min-h-screen bg-[#0a0a0a] text-white pt-28 pb-16 px-4">
            <div className="max-w-5xl mx-auto">

                {/* Header */}
                <div className="flex justify-between items-center mb-8">
                    <div>
                        <h1 className="text-3xl font-black">Tài khoản</h1>
                        <p className="text-sm text-gray-500 mt-1">{user?.email || ""}</p>
                    </div>
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-2 text-sm text-gray-400 hover:text-red-500 transition-colors"
                    >
                        <LogOut size={16} /> Đăng xuất
                    </button>
                </div>

                {/* Tabs */}
                <div className="flex gap-1 mb-8 border-b border-white/5">
                    <button
                        onClick={() => setActiveTab("profile")}
                        className={`px-5 py-3 text-sm font-bold uppercase tracking-widest transition-colors ${
                            activeTab === "profile"
                                ? "text-white border-b-2 border-red-600"
                                : "text-gray-500 hover:text-gray-300"
                        }`}
                    >
                        <UserIcon size={14} className="inline mr-2" /> Hồ sơ
                    </button>
                    <button
                        onClick={() => setActiveTab("tickets")}
                        className={`px-5 py-3 text-sm font-bold uppercase tracking-widest transition-colors ${
                            activeTab === "tickets"
                                ? "text-white border-b-2 border-red-600"
                                : "text-gray-500 hover:text-gray-300"
                        }`}
                    >
                        <Ticket size={14} className="inline mr-2" /> Vé của tôi
                        {tickets.length > 0 && (
                            <span className="ml-2 px-2 py-0.5 bg-red-600 text-white text-[10px] rounded-full">
                                {tickets.length}
                            </span>
                        )}
                    </button>
                </div>

                {/* Tab Content */}
                {activeTab === "profile" && (
                    <div className="bg-[#111] border border-white/5 rounded-2xl p-8 max-w-2xl">
                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div className="flex items-center gap-4 mb-6">
                                <div className="w-16 h-16 bg-gradient-to-br from-red-600 to-orange-500 rounded-xl flex items-center justify-center text-2xl font-black">
                                    {(user?.email || "U")[0].toUpperCase()}
                                </div>
                                <div>
                                    <p className="font-bold">{form.userName || form.email || "User"}</p>
                                    <div className="flex gap-2 mt-1">
                                        {roles.map((r) => (
                                            <span
                                                key={r}
                                                className="px-2 py-0.5 text-[10px] font-bold bg-yellow-500/10 text-yellow-500 border border-yellow-500/20 rounded"
                                            >
                                                {r}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            <Input label="Tên người dùng" name="userName" value={form.userName || ""} onChange={handleChange} />
                            <Input label="Email" name="email" value={form.email || ""} disabled />
                            <Input label="Số điện thoại" name="phoneNumber" value={form.phoneNumber || ""} onChange={handleChange} />

                            <div className="flex justify-end gap-4 pt-4 border-t border-white/5">
                                <button type="submit" className="px-6 py-2.5 bg-red-600 hover:bg-red-700 rounded-lg font-bold text-sm transition-colors">
                                    LƯU THAY ĐỔI
                                </button>
                            </div>
                        </form>
                    </div>
                )}

                {activeTab === "tickets" && (
                    <div className="space-y-4">
                        {ticketsLoading ? (
                            <div className="flex justify-center py-12">
                                <div className="w-8 h-8 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
                            </div>
                        ) : tickets.length === 0 ? (
                            <div className="bg-[#111] border border-white/5 rounded-2xl p-12 text-center">
                                <Ticket size={48} className="mx-auto text-gray-600 mb-4" />
                                <p className="text-gray-400">Bạn chưa có vé nào.</p>
                                <button
                                    onClick={() => navigate("/movies")}
                                    className="mt-4 px-6 py-2 bg-red-600 hover:bg-red-700 rounded-lg font-bold text-sm transition-colors"
                                >
                                    Đặt vé ngay
                                </button>
                            </div>
                        ) : (
                            tickets.map((ticket, idx) => (
                                <div key={ticket.reservationId || idx} className="bg-[#111] border border-white/5 rounded-xl p-5 flex flex-col sm:flex-row gap-4 items-start sm:items-center">
                                    <div className="flex-1 space-y-1">
                                        <h3 className="font-bold text-lg">{ticket.movieName || "Phim"}</h3>
                                        <p className="text-sm text-gray-400">
                                            {ticket.theaterName} {ticket.roomName ? `• ${ticket.roomName}` : ""}
                                        </p>
                                        <p className="text-sm text-gray-400">
                                            {ticket.showDate} {ticket.showTime}
                                        </p>
                                        <p className="text-sm">
                                            Ghế: <span className="text-red-500 font-bold">{ticket.seatNumbers?.join(", ") || "N/A"}</span>
                                        </p>
                                    </div>
                                    <div className="text-right space-y-2 flex flex-col justify-between items-end">
                                        <div>
                                            <p className="font-bold text-lg">{formatPrice(ticket.totalPrice || 0)}</p>
                                            <span className={`text-xs px-2 py-1 rounded font-bold ${
                                                ticket.status === "PAID" || ticket.status === "CONFIRMED"
                                                    ? "bg-green-500/20 text-green-400"
                                                    : ticket.status === "PENDING" || ticket.status === "LOCKED"
                                                        ? "bg-yellow-500/20 text-yellow-400"
                                                        : "bg-gray-500/20 text-gray-400"
                                            }`}>
                                                {ticket.status === "LOCKED" ? "PENDING" : ticket.status}
                                            </span>
                                        </div>
                                        
                                        {(ticket.status === "PENDING" || ticket.status === "LOCKED") && (
                                            <div className="flex gap-2">
                                                <button
                                                    onClick={() => navigate(`/checkout?reservationId=${ticket.reservationId}`)}
                                                    className="px-3 py-1 bg-red-600 hover:bg-red-700 text-white text-xs font-bold rounded"
                                                >
                                                    Thanh toán
                                                </button>
                                                <button
                                                    onClick={() => handleCancelTicket(ticket.reservationId)}
                                                    className="px-3 py-1 bg-gray-700 hover:bg-gray-600 text-white text-xs font-bold rounded"
                                                >
                                                    Hủy vé
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

/* Reusable Input */
function Input({ label, ...props }) {
    return (
        <div className="space-y-2">
            <label className="text-xs text-gray-400 uppercase tracking-wider">{label}</label>
            <input
                {...props}
                className={`w-full px-4 py-3 rounded-lg bg-[#1a1a1a] border border-white/5 focus:outline-none focus:border-red-500 text-sm transition-colors ${
                    props.disabled ? "opacity-60 cursor-not-allowed" : ""
                }`}
            />
        </div>
    );
}