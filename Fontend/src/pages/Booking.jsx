import React, { useEffect, useState, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Armchair, CreditCard, Star, Wifi, WifiOff, Timer } from "lucide-react";
import { getShowtimeById } from "../services/api/showtimeService";
import { getSeatsByShowtime } from "../services/api/seatService";
import { useAuthContext } from "../context/AuthContext";
import SockJS from "sockjs-client/dist/sockjs";
import { Client } from "@stomp/stompjs";
import api from "../services/api/api";

const formatPrice = (price) =>
    new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price);

export default function Booking() {
    const { id } = useParams(); // showtime ID
    const navigate = useNavigate();
    const { isAuthenticated, token } = useAuthContext();

    const [showtime, setShowtime] = useState(null);
    const [seats, setSeats] = useState([]);
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [wsConnected, setWsConnected] = useState(false);
    const [holdCountdown, setHoldCountdown] = useState(null); // Đếm ngược 10 phút

    const stompClientRef = useRef(null);
    const countdownRef = useRef(null);

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

    // ═══════════════════════════════════════════════
    // TASK 2.2: WEBSOCKET REAL-TIME
    // Subscribe /topic/showtimes/{id} để nhận seat updates
    // ═══════════════════════════════════════════════
    useEffect(() => {
        if (!id) return;

        const client = new Client({
            webSocketFactory: () => new SockJS("http://localhost:8080/api/ws"),
            reconnectDelay: 5000,
            onConnect: () => {
                setWsConnected(true);
                console.log("🟢 WebSocket connected");

                // Subscribe to seat updates for this showtime
                client.subscribe(`/topic/showtimes/${id}`, (message) => {
                    try {
                        const payload = JSON.parse(message.body);
                        console.log("📨 Seat update:", payload);

                        // Cập nhật trạng thái ghế real-time
                        setSeats((prevSeats) =>
                            prevSeats.map((seat) => {
                                if (payload.seatNumbers?.includes(seat.seatNumber)) {
                                    return {
                                        ...seat,
                                        isReserved: payload.status === "LOCKED" || payload.status === "SOLD",
                                    };
                                }
                                return seat;
                            })
                        );

                        // Nếu ghế đang selected bị người khác giữ → bỏ chọn
                        if (payload.status === "LOCKED" || payload.status === "SOLD") {
                            setSelectedSeats((prev) =>
                                prev.filter((s) => !payload.seatNumbers?.includes(s.seatNumber))
                            );
                        }
                    } catch (e) {
                        console.error("❌ Parse WS message error:", e);
                    }
                });
            },
            onDisconnect: () => {
                setWsConnected(false);
                console.log("🔴 WebSocket disconnected");
            },
            onStompError: (frame) => {
                console.error("❌ STOMP error:", frame);
                setWsConnected(false);
            },
        });

        client.activate();
        stompClientRef.current = client;

        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.deactivate();
            }
        };
    }, [id]);

    // ═══════════════════════════════════════════════
    // TASK 2.1: COUPLE SEAT TOGGLE
    // Click 1 ghế COUPLE → tự động toggle cả cặp
    // ═══════════════════════════════════════════════
    const toggleSeat = (seat) => {
        if (seat.isReserved) return;

        const seatType = seat.seatType;
        const row = seat.seatNumber.charAt(0);

        if (seatType === "COUPLE") {
            // Couple logic: ghế đi theo cặp (1,2), (3,4), (5,6)...
            const colNum = parseInt(seat.seatNumber.substring(1));
            const isOdd = colNum % 2 !== 0;
            const pairCol = isOdd ? colNum + 1 : colNum - 1;
            const pairNumber = row + pairCol;

            // Tìm ghế đôi
            const pairSeat = seats.find((s) => s.seatNumber === pairNumber);

            // Nếu ghế đôi đã bị người khác giữ → không cho chọn
            if (pairSeat && pairSeat.isReserved) {
                alert(`Ghế đôi ${pairNumber} đã được giữ bởi người khác`);
                return;
            }

            setSelectedSeats((prev) => {
                const exists = prev.find((s) => s.seatNumber === seat.seatNumber);
                if (exists) {
                    // Bỏ chọn cả cặp
                    return prev.filter(
                        (s) => s.seatNumber !== seat.seatNumber && s.seatNumber !== pairNumber
                    );
                }
                // Chọn cả cặp
                const newSelection = [...prev, seat];
                if (pairSeat && !prev.find((s) => s.seatNumber === pairNumber)) {
                    newSelection.push(pairSeat);
                }
                return newSelection;
            });
        } else {
            // Normal / VIP: toggle đơn lẻ
            setSelectedSeats((prev) => {
                const exists = prev.find((s) => s.seatNumber === seat.seatNumber);
                if (exists) {
                    return prev.filter((s) => s.seatNumber !== seat.seatNumber);
                }
                return [...prev, seat];
            });
        }
    };

    // ===== CALCULATE TOTAL =====
    const total = selectedSeats.reduce((sum, seat) => {
        const price = seat.finalPrice || seat.basePrice || showtime?.price || 0;
        return sum + Number(price);
    }, 0);

    // ═══════════════════════════════════════════════
    // TASK 3.1: HOLD SEAT (thay cho createReservation cũ)
    // Gọi POST /v1/booking/hold-seat với Pessimistic Lock
    // ═══════════════════════════════════════════════
    const handleCheckout = async () => {
        if (selectedSeats.length === 0) {
            alert("Vui lòng chọn ghế!");
            return;
        }

        setSubmitting(true);
        try {
            const seatNumbers = selectedSeats.map((s) => s.seatNumber);
            const response = await api.post("/booking/hold-seat", {
                showtimeId: Number(id),
                seatNumbers,
            });

            const result = response.data;

            if (result.apiStatus === "SUCCESS" && result.reservationId) {
                // Start countdown
                startCountdown(result.holdDurationSeconds || 600);

                navigate(`/checkout?ticketId=${result.reservationId}`, {
                    state: {
                        showtimeId: id,
                        seats: seatNumbers,
                        total: total,
                        showtime,
                        reservationId: result.reservationId,
                        expiresAt: result.expiresAt,
                    },
                });
            } else {
                alert(result.message || "Không thể giữ ghế!");
            }
        } catch (err) {
            const msg = err.response?.data?.message || err.message || "Lỗi khi giữ ghế!";
            alert(msg);
        } finally {
            setSubmitting(false);
        }
    };

    // Countdown timer
    const startCountdown = (seconds) => {
        setHoldCountdown(seconds);
        if (countdownRef.current) clearInterval(countdownRef.current);
        countdownRef.current = setInterval(() => {
            setHoldCountdown((prev) => {
                if (prev <= 1) {
                    clearInterval(countdownRef.current);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
    };

    useEffect(() => {
        return () => {
            if (countdownRef.current) clearInterval(countdownRef.current);
        };
    }, []);

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
                <header className="mb-10 flex justify-between items-start">
                    <div>
                        <p className="text-[10px] text-red-500 font-bold uppercase tracking-[0.3em] mb-3">
                            Booking #{id}
                        </p>
                        <h1 className="text-3xl md:text-5xl font-black uppercase tracking-tighter">
                            {showtime.movie?.title || "Phim"}
                        </h1>
                        <p className="text-sm text-gray-400 mt-2">
                            {showtime.theater?.name || "Rạp"} {showtime.roomName ? `• ${showtime.roomName}` : ""} • {showtime.showDate} {showtime.showTime}
                        </p>
                    </div>
                    {/* TASK 2.2: WebSocket indicator */}
                    <div className={`flex items-center gap-2 text-xs px-3 py-1.5 rounded-full ${wsConnected ? "bg-green-500/10 text-green-400" : "bg-red-500/10 text-red-400"}`}>
                        {wsConnected ? <Wifi size={12} /> : <WifiOff size={12} />}
                        {wsConnected ? "Real-time" : "Offline"}
                    </div>
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

                                    // ═══════════════════════════════
                                    // TASK 2.1: COUPLE ROW RENDERING
                                    // Hàng cuối (COUPLE) → gộp 2 ghế thành 1 block
                                    // ═══════════════════════════════
                                    const isCouple = rowSeats[0]?.seatType === "COUPLE";

                                    if (isCouple) {
                                        // Gộp ghế thành cặp: [1,2], [3,4], [5,6]...
                                        const pairs = [];
                                        for (let i = 0; i < rowSeats.length; i += 2) {
                                            const left = rowSeats[i];
                                            const right = rowSeats[i + 1];
                                            pairs.push({ left, right });
                                        }

                                        return (
                                            <div key={row} className="flex items-center gap-2">
                                                <span className="w-6 text-xs text-pink-400 font-bold text-center">💑 {row}</span>
                                                <div className="flex gap-3 flex-1 justify-center">
                                                    {pairs.map(({ left, right }) => {
                                                        const leftSelected = selectedSeats.some((s) => s.seatNumber === left.seatNumber);
                                                        const rightSelected = right && selectedSeats.some((s) => s.seatNumber === right.seatNumber);
                                                        const isSelected = leftSelected || rightSelected;
                                                        const isOccupied = left.isReserved || (right && right.isReserved);

                                                        return (
                                                            <button
                                                                key={left.seatNumber}
                                                                onClick={() => toggleSeat(left)}
                                                                disabled={isOccupied}
                                                                title={`${left.seatNumber}+${right?.seatNumber || ""} - COUPLE - ${formatPrice((left.finalPrice || 0) + (right?.finalPrice || 0))}`}
                                                                className={`w-[76px] h-10 rounded-xl border-2 text-[10px] font-bold flex items-center justify-center gap-1 transition-all
                                                                    ${isOccupied
                                                                        ? "bg-gray-700 text-gray-500 cursor-not-allowed border-gray-600"
                                                                        : isSelected
                                                                            ? "bg-red-600 text-white border-red-500 scale-105 shadow-lg shadow-red-500/20"
                                                                            : "bg-pink-900/30 text-pink-400 border-pink-600/40 hover:bg-pink-600/30 hover:scale-105"
                                                                    }`}
                                                            >
                                                                ❤️ {left.seatNumber.substring(1)}-{right?.seatNumber?.substring(1) || "?"}
                                                            </button>
                                                        );
                                                    })}
                                                </div>
                                            </div>
                                        );
                                    }

                                    // Normal / VIP rows
                                    return (
                                        <div key={row} className="flex items-center gap-2">
                                            <span className="w-6 text-xs text-gray-500 font-bold text-center">{row}</span>
                                            <div className="flex gap-2 flex-1 justify-center">
                                                {rowSeats.map((seat) => {
                                                    const isSelected = selectedSeats.some((s) => s.seatNumber === seat.seatNumber);
                                                    const isOccupied = seat.isReserved;
                                                    const isVip = seat.seatType === "VIP";

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
                                                                            : "bg-[#1f1f1f] text-gray-300 border-white/10 hover:bg-white/10"
                                                                }`}
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
                                <span className="w-8 h-4 bg-pink-900/30 border border-pink-600/40 rounded-lg"></span> Couple (chọn 1 = 2 ghế)
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

                            {/* Countdown timer */}
                            {holdCountdown != null && holdCountdown > 0 && (
                                <div className="flex items-center gap-2 text-yellow-400 text-xs bg-yellow-500/10 p-2 rounded-lg">
                                    <Timer size={14} />
                                    <span>Giữ ghế còn: {Math.floor(holdCountdown / 60)}:{String(holdCountdown % 60).padStart(2, "0")}</span>
                                </div>
                            )}
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
                                    Giữ ghế & Thanh toán
                                </>
                            )}
                        </button>
                    </aside>
                </section>
            </main>
        </div>
    );
}
