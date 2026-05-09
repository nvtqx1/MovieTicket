import React, { useState, useEffect } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import api from "../services/api/api";
import { checkVoucher } from "../services/api/voucherService";
import { cancelTicket, getTicket } from "../services/api/reservationService";
import { useAuthContext } from "../context/AuthContext";
import { CheckCircle, Clock, CreditCard, Wallet, Smartphone, XCircle } from "lucide-react";

const formatPrice = (price) =>
    new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price);

const PAYMENT_METHODS = [
    { id: 1, name: "Tháº» tÃ­n dá»¥ng", icon: CreditCard },
    { id: 2, name: "VÃ­ Ä‘iá»‡n tá»­", icon: Wallet },
    { id: 3, name: "Chuyá»ƒn khoáº£n", icon: Smartphone },
];

export default function Checkout() {
    const { reservationId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const { state } = location;
    const { isAuthenticated } = useAuthContext();

    const query = new URLSearchParams(location.search);
    const currentReservationId = query.get("ticketId") || query.get("reservationId") || reservationId || state?.reservationId;

    const [ticket, setTicket] = useState(null);
    const selectedSeats = ticket?.seatNumbers || state?.seats || [];
    const totalFromState = Number(ticket?.totalPrice ?? state?.total ?? 0);
    const showtime = ticket
        ? {
            movie: { title: ticket.movieName },
            theater: { name: ticket.theaterName },
            roomName: ticket.roomName,
            showDate: ticket.showtimeStartTime?.slice(0, 10),
            showTime: ticket.showtimeStartTime?.slice(11, 16),
        }
        : state?.showtime || null;

    const [timeLeft, setTimeLeft] = useState(0);
    const [loadingTicket, setLoadingTicket] = useState(Boolean(currentReservationId));
    const [paymentMethod, setPaymentMethod] = useState(2);
    const [isProcessing, setIsProcessing] = useState(false);
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);

    // Voucher State
    const [voucherCode, setVoucherCode] = useState("");
    const [voucherResult, setVoucherResult] = useState(null);
    const [voucherError, setVoucherError] = useState(null);
    const [isCheckingVoucher, setIsCheckingVoucher] = useState(false);

    useEffect(() => {
        if (!isAuthenticated) {
            navigate("/login");
        }
    }, [isAuthenticated, navigate]);

    useEffect(() => {
        if (!isAuthenticated || !currentReservationId) {
            setLoadingTicket(false);
            return;
        }
        let ignore = false;
        const fetchTicket = async () => {
            try {
                setLoadingTicket(true);
                setError(null);
                const data = await getTicket(currentReservationId);
                if (!ignore) setTicket(data);
            } catch (err) {
                if (!ignore) setError(err.message || "Khong the tai thong tin ve.");
            } finally {
                if (!ignore) setLoadingTicket(false);
            }
        };
        fetchTicket();
        return () => { ignore = true; };
    }, [currentReservationId, isAuthenticated]);

    useEffect(() => {
        const expiresAt = ticket?.expiresAt || state?.expiresAt;
        if (!expiresAt) {
            setTimeLeft(0);
            return;
        }

        const updateRemaining = () => {
            const remaining = Math.max(0, Math.floor((new Date(expiresAt).getTime() - Date.now()) / 1000));
            setTimeLeft(remaining);
            return remaining;
        };

        updateRemaining();
        const timer = setInterval(() => {
            if (updateRemaining() === 0) clearInterval(timer);
        }, 1000);

        return () => clearInterval(timer);
    }, [ticket?.expiresAt, state?.expiresAt]);

    const formatTime = (sec) => {
        const m = Math.floor(sec / 60);
        const s = sec % 60;
        return `${m.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
    };

    // ======================
    // HANDLE VOUCHER
    // ======================
    const handleCheckVoucher = async () => {
        if (!voucherCode.trim()) {
            setVoucherError("Vui lÃ²ng nháº­p mÃ£ voucher!");
            return;
        }

        setIsCheckingVoucher(true);
        setVoucherError(null);
        setVoucherResult(null);

        try {
            const data = await checkVoucher(voucherCode);
            if (data.isValid) {
                setVoucherResult(data);
            } else {
                setVoucherError(data.message || "MÃ£ voucher khÃ´ng há»£p lá»‡");
            }
        } catch (err) {
            setVoucherError("MÃ£ voucher khÃ´ng há»£p lá»‡ hoáº·c Ä‘Ã£ háº¿t háº¡n");
        } finally {
            setIsCheckingVoucher(false);
        }
    };

    // Calculate Final Total
    const calculateFinalTotal = () => {
        if (!voucherResult || !voucherResult.isValid) return totalFromState;
        
        let discount = (totalFromState * voucherResult.discountPercentage) / 100;
        if (voucherResult.maxDiscountAmount && discount > voucherResult.maxDiscountAmount) {
            discount = voucherResult.maxDiscountAmount;
        }
        return Math.max(0, totalFromState - discount);
    };

    const finalTotal = calculateFinalTotal();
    const hasExpiry = Boolean(ticket?.expiresAt || state?.expiresAt);
    const isExpired = hasExpiry && timeLeft === 0;

    // ======================
    // HANDLE PAYMENT
    // ======================
    const handlePayment = async () => {
        if (selectedSeats.length === 0) {
            alert("ChÆ°a chá»n gháº¿!");
            return;
        }

        if (!currentReservationId) {
            alert("KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n Ä‘áº·t vÃ©!");
            return;
        }

        setIsProcessing(true);
        setError(null);

        const paymentMethodName = PAYMENT_METHODS.find(p => p.id === paymentMethod)?.name || "CASH";

        try {
            // Task 3.3: Call new mock checkout API
            const response = await api.post(`/booking/checkout/${currentReservationId}`, {
                paymentMethod: paymentMethodName,
                voucherCode: voucherResult?.isValid ? voucherCode : null,
            });

            const data = response.data;

            if (data.apiStatus === "SUCCESS") {
                setResult(data);
            } else {
                setError(data.message || "Thanh toÃ¡n tháº¥t báº¡i!");
            }
        } catch (err) {
            setError(err.response?.data?.message || err.message || "Thanh toÃ¡n tháº¥t báº¡i! Vui lÃ²ng thá»­ láº¡i.");
        } finally {
            setIsProcessing(false);
        }
    };

    const handleCancel = async () => {
        if (!currentReservationId) return;
        if (!window.confirm("Ban co chac muon huy ve nay?")) return;

        try {
            await cancelTicket(currentReservationId);
            alert("Da huy ve va giai phong ghe.");
            navigate("/profile");
        } catch (err) {
            setError(err.message || "Khong the huy ve.");
        }
    };

    // ======================
    // SUCCESS UI
    // ======================
    if (loadingTicket) {
        return (
            <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center">
                <div className="w-10 h-10 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
            </div>
        );
    }

    if (result) {
        return (
            <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center text-white p-6">
                <div className="bg-[#111] p-8 rounded-2xl border border-green-500/30 text-center max-w-md w-full shadow-2xl">
                    <div className="w-16 h-16 bg-green-500/20 rounded-full flex items-center justify-center mx-auto mb-4">
                        <CheckCircle size={32} className="text-green-500" />
                    </div>

                    <h2 className="text-2xl font-black mb-2">Thanh toÃ¡n thÃ nh cÃ´ng!</h2>
                    <p className="text-gray-400 text-sm mb-6">VÃ© Ä‘iá»‡n tá»­ Ä‘Ã£ Ä‘Æ°á»£c táº¡o</p>

                    {result.qrCodeDataUri && (
                        <img
                            src={result.qrCodeDataUri}
                            alt="QR Code"
                            className="w-48 h-48 mx-auto bg-white p-3 rounded-xl mb-4"
                        />
                    )}

                    <div className="space-y-2 text-sm text-gray-300 mb-6">
                        {result.movieName && (
                            <p>Phim: <span className="text-white font-bold">{result.movieName}</span></p>
                        )}
                        {result.theaterName && (
                            <p>Ráº¡p: <span className="text-white font-bold">{result.theaterName}</span></p>
                        )}
                        {result.seatNumbers && (
                            <p>Gháº¿: <span className="text-white font-bold">{result.seatNumbers.join(", ")}</span></p>
                        )}
                        {result.totalPrice && (
                            <p>Tá»•ng tiá»n: <span className="text-green-400 font-bold">{formatPrice(result.totalPrice)}</span></p>
                        )}
                    </div>

                    <div className="flex gap-3">
                        <button
                            onClick={() => navigate("/profile")}
                            className="flex-1 py-3 bg-white/10 border border-white/10 rounded-lg font-bold text-sm hover:bg-white/20 transition-colors"
                        >
                            VÃ© cá»§a tÃ´i
                        </button>
                        <button
                            onClick={() => navigate("/movies")}
                            className="flex-1 py-3 bg-red-600 rounded-lg font-bold text-sm hover:bg-red-700 transition-colors"
                        >
                            Vá» trang phim
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    // ======================
    // MAIN UI
    // ======================
    return (
        <div className="min-h-screen bg-[#0a0a0a] text-white pt-24 pb-16 px-4">
            <div className="max-w-6xl mx-auto flex flex-col lg:flex-row gap-10">

                {/* LEFT */}
                <div className="flex-1 space-y-8">
                    <div>
                        <h1 className="text-3xl font-black">Thanh toÃ¡n</h1>
                        <p className="text-sm text-gray-500 mt-1">Chá»n phÆ°Æ¡ng thá»©c vÃ  hoÃ n táº¥t thanh toÃ¡n</p>
                    </div>

                    {/* Payment Methods */}
                    <div>
                        <h3 className="text-sm font-bold uppercase tracking-widest text-gray-400 mb-4">
                            PhÆ°Æ¡ng thá»©c thanh toÃ¡n
                        </h3>
                        <div className="grid grid-cols-3 gap-4">
                            {PAYMENT_METHODS.map((method) => {
                                const Icon = method.icon;
                                const isActive = paymentMethod === method.id;
                                return (
                                    <button
                                        key={method.id}
                                        onClick={() => setPaymentMethod(method.id)}
                                        className={`py-5 px-4 rounded-xl border flex flex-col items-center gap-2 transition-all ${isActive
                                            ? "border-red-600 bg-red-600/10 text-white"
                                            : "border-white/10 text-gray-400 hover:border-white/30"
                                        }`}
                                    >
                                        <Icon size={20} />
                                        <span className="text-xs font-bold">{method.name}</span>
                                    </button>
                                );
                            })}
                        </div>
                    </div>

                    {/* Movie Info */}
                    {showtime && (
                        <div className="bg-[#111] border border-white/5 rounded-xl p-5 space-y-3">
                            <h3 className="text-sm font-bold uppercase tracking-widest text-gray-400">
                                ThÃ´ng tin suáº¥t chiáº¿u
                            </h3>
                            <div className="space-y-2 text-sm">
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Phim</span>
                                    <span className="font-bold">{showtime.movie?.title || "N/A"}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Ráº¡p</span>
                                    <span>{showtime.theater?.name || "N/A"}</span>
                                </div>
                                {showtime.roomName && (
                                    <div className="flex justify-between">
                                        <span className="text-gray-400">PhÃ²ng</span>
                                        <span>{showtime.roomName}</span>
                                    </div>
                                )}
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Giá» chiáº¿u</span>
                                    <span>{showtime.showDate} {showtime.showTime}</span>
                                </div>
                            </div>
                        </div>
                    )}

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/30 rounded-lg p-4 text-sm text-red-400">
                            {error}
                        </div>
                    )}
                </div>

                {/* RIGHT - ORDER SUMMARY */}
                <aside className="w-full lg:w-[350px]">
                    <div className="bg-[#111] p-6 rounded-xl border border-white/5 space-y-6 sticky top-24">

                        {/* Countdown */}
                        <div className="flex items-center justify-between">
                            <span className="text-xs text-gray-400 flex items-center gap-1">
                                <Clock size={14} /> Thá»i gian giá»¯ gháº¿
                            </span>
                            <span className={`font-mono font-bold text-lg ${timeLeft < 120 ? "text-red-500 animate-pulse" : "text-yellow-500"}`}>
                                {formatTime(timeLeft)}
                            </span>
                        </div>

                        {/* Seats */}
                        <div>
                            <p className="text-gray-400 text-sm mb-2">Gháº¿ Ä‘Ã£ chá»n</p>
                            <div className="flex gap-2 flex-wrap">
                                {selectedSeats.length > 0 ? (
                                    selectedSeats.map((seat) => (
                                        <span
                                            key={seat}
                                            className="px-3 py-1.5 bg-zinc-800 rounded-lg text-red-500 font-bold text-sm"
                                        >
                                            {seat}
                                        </span>
                                    ))
                                ) : (
                                    <span className="text-gray-600 text-sm">KhÃ´ng cÃ³ gháº¿</span>
                                )}
                            </div>
                        </div>

                        {/* Voucher */}
                        <div className="border-t border-white/10 pt-4">
                            <p className="text-gray-400 text-sm mb-2">MÃ£ giáº£m giÃ¡</p>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    value={voucherCode}
                                    onChange={(e) => setVoucherCode(e.target.value)}
                                    placeholder="Nháº­p mÃ£ voucher..."
                                    className="flex-1 bg-[#222] border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder-gray-500 focus:border-red-600 focus:outline-none transition-colors uppercase"
                                />
                                <button
                                    onClick={handleCheckVoucher}
                                    disabled={isCheckingVoucher || !voucherCode.trim()}
                                    className="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg text-sm font-bold disabled:opacity-50 transition-colors"
                                >
                                    {isCheckingVoucher ? "..." : "Ãp dá»¥ng"}
                                </button>
                            </div>
                            {voucherError && (
                                <p className="text-red-500 text-xs mt-2">{voucherError}</p>
                            )}
                            {voucherResult && voucherResult.isValid && (
                                <p className="text-green-500 text-xs mt-2 flex items-center gap-1">
                                    <CheckCircle size={12} /> {voucherResult.message}
                                </p>
                            )}
                        </div>

                        {/* Total */}
                        <div className="flex flex-col gap-2 border-t border-white/10 pt-4">
                            <div className="flex justify-between text-sm">
                                <span className="text-gray-400">Táº¡m tÃ­nh</span>
                                <span className="text-gray-300">
                                    {formatPrice(totalFromState)}
                                </span>
                            </div>
                            {voucherResult && voucherResult.isValid && (
                                <div className="flex justify-between text-sm text-green-500">
                                    <span>Giáº£m giÃ¡</span>
                                    <span>-{formatPrice(totalFromState - finalTotal)}</span>
                                </div>
                            )}
                            <div className="flex justify-between text-sm mt-2">
                                <span className="text-gray-400 font-bold">Tá»•ng cá»™ng</span>
                                <span className="font-bold text-xl text-red-500">
                                    {formatPrice(finalTotal)}
                                </span>
                            </div>
                        </div>

                        {/* Pay button */}
                        <button
                            onClick={handlePayment}
                            disabled={isProcessing || isExpired || selectedSeats.length === 0}
                            className="w-full py-3.5 bg-red-600 rounded-lg font-black uppercase tracking-widest text-sm disabled:bg-gray-700 disabled:cursor-not-allowed hover:bg-red-700 transition-colors flex items-center justify-center gap-2"
                        >
                            {isProcessing ? (
                                <>
                                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                                    Äang xá»­ lÃ½...
                                </>
                            ) : (
                                <>
                                    <CreditCard size={16} />
                                    Thanh toÃ¡n {formatPrice(finalTotal)}
                                </>
                            )}
                        </button>

                        <button
                            onClick={handleCancel}
                            disabled={isProcessing || !currentReservationId}
                            className="w-full py-3 bg-white/10 border border-white/10 rounded-lg font-bold text-sm text-gray-200 disabled:opacity-50 hover:bg-white/15 transition-colors flex items-center justify-center gap-2"
                        >
                            <XCircle size={16} />
                            Huy ve
                        </button>
                    </div>
                </aside>
            </div>
        </div>
    );
}
