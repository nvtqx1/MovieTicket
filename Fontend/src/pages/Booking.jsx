import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Armchair, CreditCard, Star } from "lucide-react";
import { getShowtimeById } from "../services/api/showtimeService";
import { getSeatsByShowtime } from "../services/api/seatService";
import { createReservation } from "../services/api/reservationService";
import { useAuthContext } from "../context/AuthContext";

const formatPrice = (price) =>
    new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price);

export default function Booking() {
    const { id } = useParams(); // showtime ID
    const navigate = useNavigate();
    const { isAuthenticated } = useAuthContext();

    const [showtime, setShowtime] = useState(null);
    const [seats, setSeats] = useState([]);
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    // ===== FETCH SHOWTIME + SEATS =====
    useEffect(() => {
        if (!isAuthenticated) {
            navigate("/login");
            return;
        }

        let isMounted = true;

        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);

                const [showtimeData, seatData] = await Promise.all([
                    getShowtimeById(id),
                    getSeatsByShowtime(id),
                ]);

                if (!isMounted) return;

                setShowtime(showtimeData);
                setSeats(Array.isArray(seatData) ? seatData : []);
            } catch (err) {
                if (!isMounted) return;
                setError(err.message || "Không thể tải thông tin suất chiếu.");
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        fetchData();
        return () => { isMounted = false; };
    }, [id]);

    // ===== TOGGLE SEAT =====
    const toggleSeat = (seat) => {
        if (seat.isReserved) return;

        setSelectedSeats((prev) => {
            const exists = prev.find((s) => s.seatNumber === seat.seatNumber);
            if (exists) {
                return prev.filter((s) => s.seatNumber !== seat.seatNumber);
            }
            return [...prev, seat];
        });
    };

    // ===== CALCULATE TOTAL =====
    const total = selectedSeats.reduce((sum, seat) => {
        const price = seat.finalPrice || seat.basePrice || showtime?.price || 0;
        return sum + Number(price);
    }, 0);

    // ===== CHECKOUT =====
    const handleCheckout = async () => {
        if (selectedSeats.length === 0) {
            alert("Vui lòng chọn ghế!");
            return;
        }

        setSubmitting(true);
        try {
            // Call init reservation API
            const seatNumbers = selectedSeats.map((s) => s.seatNumber);
            const result = await createReservation({
                showtimeId: Number(id),
                seatNumbers,
            });

            if (result.apiStatus === "SUCCESS" && result.reservationId) {
                navigate(`/checkout/${result.reservationId}`, {
                    state: {
                        showtimeId: id,
                        seats: seatNumbers,
                        total: result.totalPrice || total,
                        showtime,
                        reservationId: result.reservationId,
                    },
                });
            } else {
                alert(result.message || "Tạo đơn thất bại!");
            }
        } catch (err) {
            alert(err.message || "Lỗi khi tạo đơn đặt vé!");
        } finally {
            setSubmitting(false);
        }
    };

    // ===== LOADING =====
    if (loading) {
        return (
            <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center">
                <div className="flex flex-col items-center gap-4">
                    <div className="w-10 h-10 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
                    <p className="text-gray-500 text-sm uppercase tracking-widest">Đang tải...</p>
                </div>
            </div>
        );
    }

    // ===== ERROR =====
    if (error) {
        return (
            <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center text-white">
                <div className="text-center space-y-4">
                    <p className="text-red-500 font-bold">{error}</p>
                    <button onClick={() => navigate(-1)} className="text-sm text-gray-400 hover:text-white underline">
                        Quay lại
                    </button>
                </div>
            </div>
        );
    }

    if (!showtime) return null;

    // Group seats by row for rendering
    const seatsByRow = seats.reduce((acc, seat) => {
        const row = seat.seatNumber?.charAt(0) || "?";
        if (!acc[row]) acc[row] = [];
        acc[row].push(seat);
        return acc;
    }, {});

    const sortedRows = Object.keys(seatsByRow).sort();
    const maxCols = Math.max(...Object.values(seatsByRow).map((r) => r.length), 1);

    return (
        <div className="min-h-screen bg-[#0a0a0a] text-white pt-28 pb-16">
            <main className="max-w-5xl mx-auto px-4 md:px-8">

                {/* BACK */}
                <button
                    onClick={() => navigate(-1)}
                    className="mb-8 inline-flex items-center gap-2 text-sm text-gray-400 hover:text-white"
                >
                    <ArrowLeft size={16} />
                    Quay lại
                </button>

                {/* HEADER */}
                <header className="mb-10">
                    <p className="text-[10px] text-red-500 font-bold uppercase tracking-[0.3em] mb-3">
                        Booking #{id}
                    </p>
                    <h1 className="text-3xl md:text-5xl font-black uppercase tracking-tighter">
                        {showtime.movie?.title || "Phim"}
                    </h1>
                    <p className="text-sm text-gray-400 mt-2">
                        {showtime.theater?.name || "Rạp"} {showtime.roomName ? `• ${showtime.roomName}` : ""} • {showtime.showDate} {showtime.showTime}
                    </p>
                </header>

                <section className="grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-8">

                    {/* SEAT MAP */}
                    <div className="bg-[#111] border border-white/5 rounded-xl p-6">
                        <div className="w-full h-2 bg-white/20 rounded-full mb-3" />
                        <p className="text-center text-[10px] text-gray-500 uppercase tracking-widest mb-8">
                            Màn hình
                        </p>

                        {seats.length === 0 ? (
                            <p className="text-center text-gray-500 py-12">
                                Chưa có ghế nào được tạo cho suất chiếu này.
                            </p>
                        ) : (
                            <div className="space-y-2 max-w-2xl mx-auto">
                                {sortedRows.map((row) => {
                                    const rowSeats = seatsByRow[row].sort((a, b) => {
                                        const numA = parseInt(a.seatNumber.substring(1));
                                        const numB = parseInt(b.seatNumber.substring(1));
                                        return numA - numB;
                                    });

                                    return (
                                        <div key={row} className="flex items-center gap-2">
                                            <span className="w-6 text-xs text-gray-500 font-bold text-center">{row}</span>
                                            <div className="flex gap-2 flex-1 justify-center">
                                                {rowSeats.map((seat) => {
                                                    const isSelected = selectedSeats.some((s) => s.seatNumber === seat.seatNumber);
                                                    const isOccupied = seat.isReserved;
                                                    const isVip = seat.seatType === "VIP";
                                                    const isCouple = seat.seatType === "COUPLE";

                                                    return (
                                                        <button
                                                            key={seat.seatNumber}
                                                            onClick={() => toggleSeat(seat)}
                                                            disabled={isOccupied}
                                                            title={`${seat.seatNumber} - ${seat.seatType} - ${formatPrice(seat.finalPrice || seat.basePrice || 0)}`}
                                                            className={`w-9 h-9 rounded-md border text-[10px] font-bold flex items-center justify-center transition-all
                                                                ${isOccupied
                                                                    ? "bg-gray-700 text-gray-500 cursor-not-allowed"
                                                                    : isSelected
                                                                        ? "bg-red-600 text-white border-red-500 scale-105"
                                                                        : isVip
                                                                            ? "bg-yellow-900/30 text-yellow-400 border-yellow-600/40 hover:bg-yellow-600/30"
                                                                            : isCouple
                                                                                ? "bg-pink-900/30 text-pink-400 border-pink-600/40 hover:bg-pink-600/30"
                                                                                : "bg-[#1f1f1f] text-gray-300 border-white/10 hover:bg-white/10"
                                                                }
                                                            `}
                                                        >
                                                            {seat.seatNumber.substring(1)}
                                                        </button>
                                                    );
                                                })}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}

                        {/* LEGEND */}
                        <div className="flex flex-wrap justify-center gap-4 mt-8 text-xs text-gray-400">
                            <span className="flex items-center gap-2">
                                <span className="w-4 h-4 bg-[#1f1f1f] border border-white/10 rounded-sm"></span> Thường
                            </span>
                            <span className="flex items-center gap-2">
                                <span className="w-4 h-4 bg-yellow-900/30 border border-yellow-600/40 rounded-sm"></span> VIP
                            </span>
                            <span className="flex items-center gap-2">
                                <span className="w-4 h-4 bg-pink-900/30 border border-pink-600/40 rounded-sm"></span> Couple
                            </span>
                            <span className="flex items-center gap-2">
                                <span className="w-4 h-4 bg-red-600 rounded-sm"></span> Đang chọn
                            </span>
                            <span className="flex items-center gap-2">
                                <span className="w-4 h-4 bg-gray-700 rounded-sm"></span> Đã đặt
                            </span>
                        </div>
                    </div>

                    {/* SIDEBAR */}
                    <aside className="bg-[#111] border border-white/5 rounded-xl p-6 h-fit">
                        <h2 className="text-lg font-bold mb-6">Thông tin vé</h2>

                        <div className="space-y-4 text-sm">
                            <div className="flex justify-between">
                                <span className="text-gray-400">Rạp</span>
                                <span>{showtime.theater?.name || "N/A"}</span>
                            </div>

                            {showtime.roomName && (
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Phòng</span>
                                    <span>{showtime.roomName}</span>
                                </div>
                            )}

                            <div className="flex justify-between">
                                <span className="text-gray-400">Giờ chiếu</span>
                                <span>{showtime.showDate} {showtime.showTime}</span>
                            </div>

                            <div className="flex justify-between">
                                <span className="text-gray-400">Ghế</span>
                                <span className="font-bold text-right max-w-[180px]">
                                    {selectedSeats.length > 0
                                        ? selectedSeats.map((s) => s.seatNumber).join(", ")
                                        : "Chưa chọn"}
                                </span>
                            </div>

                            {selectedSeats.length > 0 && (
                                <div className="space-y-1 pt-2 border-t border-white/5">
                                    {selectedSeats.map((seat) => (
                                        <div key={seat.seatNumber} className="flex justify-between text-xs text-gray-400">
                                            <span>{seat.seatNumber} ({seat.seatType})</span>
                                            <span>{formatPrice(seat.finalPrice || seat.basePrice || 0)}</span>
                                        </div>
                                    ))}
                                </div>
                            )}

                            <div className="flex justify-between border-t border-white/10 pt-4 mt-4">
                                <span className="text-gray-400">Tổng tiền</span>
                                <span className="font-bold text-red-500">
                                    {formatPrice(total)}
                                </span>
                            </div>
                        </div>

                        <button
                            onClick={handleCheckout}
                            disabled={selectedSeats.length === 0 || submitting}
                            className="mt-8 w-full py-3.5 rounded-lg bg-red-600 hover:bg-red-700 text-white font-black uppercase tracking-widest text-sm flex items-center justify-center gap-2 disabled:bg-gray-700 disabled:cursor-not-allowed transition-colors"
                        >
                            {submitting ? (
                                <>
                                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                                    Đang xử lý...
                                </>
                            ) : (
                                <>
                                    <CreditCard size={16} />
                                    Thanh toán
                                </>
                            )}
                        </button>
                    </aside>
                </section>
            </main>
        </div>
    );
}
