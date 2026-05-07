import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authService } from "../services/api/authService";
import { getMyTickets, cancelReservation, getTicketDetail } from "../services/api/reservationService";
import { useAuthContext } from "../context/AuthContext";
import { Ticket, User as UserIcon, LogOut, Eye, X, QrCode, MapPin, Clock, Armchair } from "lucide-react";

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

    // Ticket Detail Modal
    const [ticketDetail, setTicketDetail] = useState(null);
    const [detailLoading, setDetailLoading] = useState(false);
    const [showDetailModal, setShowDetailModal] = useState(false);

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

    // Task 2.3: Xem chi tiết vé
    const handleViewTicketDetail = async (reservationId) => {
        setDetailLoading(true);
        setShowDetailModal(true);
        try {
            const data = await getTicketDetail(reservationId);
            setTicketDetail(data);
        } catch (err) {
            console.error("Error fetching ticket detail:", err);
            alert("Lỗi tải chi tiết vé");
            setShowDetailModal(false);
        } finally {
            setDetailLoading(false);
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
                                        
                                        <div className="flex gap-2">
                                            {/* Task 2.3: Nút xem chi tiết vé */}
                                            <button
                                                onClick={() => handleViewTicketDetail(ticket.reservationId)}
                                                className="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded flex items-center gap-1"
                                            >
                                                <Eye size={12} /> Chi tiết
                                            </button>

                                            {(ticket.status === "PENDING" || ticket.status === "LOCKED") && (
                                                <>
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
                                                </>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                )}
            </div>

            {/* ===== TICKET DETAIL MODAL (Task 2.3) ===== */}
            {showDetailModal && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4" onClick={() => setShowDetailModal(false)}>
                    <div className="bg-[#0a0a0a] border border-white/10 rounded-3xl max-w-md w-full overflow-hidden shadow-2xl" onClick={(e) => e.stopPropagation()}>
                        {detailLoading ? (
                            <div className="p-16 flex justify-center">
                                <div className="w-10 h-10 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
                            </div>
                        ) : ticketDetail ? (
                            <>
                                {/* Ticket Header - giống vé giấy */}
                                <div className="bg-gradient-to-br from-red-600 to-red-800 p-6 relative">
                                    <button onClick={() => setShowDetailModal(false)} className="absolute top-4 right-4 text-white/60 hover:text-white">
                                        <X size={20} />
                                    </button>
                                    <p className="text-red-200 text-[10px] uppercase tracking-[0.2em] font-bold">TMT Cinema</p>
                                    <h2 className="text-2xl font-black text-white mt-2 leading-tight">{ticketDetail.movieTitle}</h2>
                                    <p className="text-red-200 text-xs mt-1">{ticketDetail.movieGenre}</p>
                                </div>

                                {/* Ticket Divider */}
                                <div className="relative">
                                    <div className="absolute -left-4 -top-4 w-8 h-8 bg-[#0a0a0a] rounded-full" />
                                    <div className="absolute -right-4 -top-4 w-8 h-8 bg-[#0a0a0a] rounded-full" />
                                    <div className="border-t-2 border-dashed border-white/10 mx-8" />
                                </div>

                                {/* Ticket Body */}
                                <div className="p-6 space-y-4">
                                    <div className="grid grid-cols-2 gap-4">
                                        <InfoBlock icon={<MapPin size={14} />} label="Rạp" value={ticketDetail.theaterName} />
                                        <InfoBlock icon={<Armchair size={14} />} label="Phòng" value={ticketDetail.roomName} />
                                        <InfoBlock icon={<Clock size={14} />} label="Ngày chiếu" value={ticketDetail.showDate} />
                                        <InfoBlock icon={<Clock size={14} />} label="Giờ chiếu" value={ticketDetail.showTime?.substring(0, 5)} />
                                    </div>

                                    {/* Seats Detail */}
                                    <div className="bg-white/5 rounded-xl p-4">
                                        <p className="text-[10px] text-gray-500 uppercase tracking-wider mb-2">Ghế đã chọn</p>
                                        <div className="flex flex-wrap gap-2">
                                            {ticketDetail.seats?.map((seat) => (
                                                <span key={seat.seatNumber} className={`px-3 py-1.5 rounded-lg text-xs font-bold ${
                                                    seat.seatType === "VIP"
                                                        ? "bg-yellow-500/20 text-yellow-400 border border-yellow-500/30"
                                                        : seat.seatType === "COUPLE"
                                                            ? "bg-pink-500/20 text-pink-400 border border-pink-500/30"
                                                            : "bg-white/10 text-white border border-white/10"
                                                }`}>
                                                    {seat.row}{seat.col}
                                                    <span className="text-[9px] ml-1 opacity-60">{seat.seatType}</span>
                                                </span>
                                            ))}
                                        </div>
                                    </div>

                                    {/* Price & Status */}
                                    <div className="flex justify-between items-center pt-2">
                                        <div>
                                            <p className="text-[10px] text-gray-500 uppercase tracking-wider">Tổng tiền</p>
                                            <p className="text-2xl font-black text-red-500">{formatPrice(ticketDetail.totalPrice || 0)}</p>
                                        </div>
                                        <span className={`px-3 py-1.5 rounded-lg text-xs font-bold ${
                                            ticketDetail.status === "PAID" || ticketDetail.status === "CONFIRMED"
                                                ? "bg-green-500/20 text-green-400"
                                                : ticketDetail.status === "PENDING" || ticketDetail.status === "LOCKED"
                                                    ? "bg-yellow-500/20 text-yellow-400"
                                                    : ticketDetail.status === "CANCELED"
                                                        ? "bg-red-500/20 text-red-400"
                                                        : "bg-gray-500/20 text-gray-400"
                                        }`}>
                                            {ticketDetail.status}
                                        </span>
                                    </div>

                                    {/* QR Code */}
                                    {ticketDetail.qrCodeDataUri && (
                                        <div className="flex flex-col items-center pt-4 border-t border-white/5">
                                            <div className="bg-white p-3 rounded-xl">
                                                <img
                                                    src={ticketDetail.qrCodeDataUri}
                                                    alt="QR Code"
                                                    className="w-40 h-40"
                                                />
                                            </div>
                                            <p className="text-[10px] text-gray-500 mt-2 flex items-center gap-1">
                                                <QrCode size={12} /> Quét mã để check-in
                                            </p>
                                        </div>
                                    )}
                                </div>
                            </>
                        ) : null}
                    </div>
                </div>
            )}
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

/* Info Block for Ticket Detail */
function InfoBlock({ icon, label, value }) {
    return (
        <div>
            <p className="text-[10px] text-gray-500 uppercase tracking-wider flex items-center gap-1 mb-1">{icon} {label}</p>
            <p className="text-white font-bold text-sm">{value || "-"}</p>
        </div>
    );
}