import React, { useState, useEffect } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { confirmReservation } from "../services/api/mockReservation";

export default function Checkout() {
    const { reservationId } = useParams();
    const navigate = useNavigate();
    const { state } = useLocation();

    const selectedSeats = state?.seats || [];
    const currentReservationId = reservationId || state?.reservationId || Date.now().toString();

    const [timeLeft, setTimeLeft] = useState(600);
    const [paymentMethod, setPaymentMethod] = useState(2);
    const [isProcessing, setIsProcessing] = useState(false);
    const [result, setResult] = useState(null);

    // ======================
    // COUNTDOWN
    // ======================
    useEffect(() => {
        const timer = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    clearInterval(timer);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, []);

    useEffect(() => {
        if (timeLeft === 0) {
            alert("Hết thời gian giữ ghế!");
            navigate("/movies");
        }
    }, [timeLeft, navigate]);

    const formatTime = (sec) => {
        const m = Math.floor(sec / 60);
        const s = sec % 60;
        return `${m.toString().padStart(2, "0")}:${s
            .toString()
            .padStart(2, "0")}`;
    };

    // ======================
    // HANDLE PAYMENT
    // ======================
    const handlePayment = async () => {
        if (selectedSeats.length === 0) {
            alert("Chưa chọn ghế!");
            return;
        }

        setIsProcessing(true);

        const payload = {
            reservationId: Number(currentReservationId),
            paymentMethodId: paymentMethod,
            transactionCode: `TMT${Date.now()}`,
            seatNumbers: selectedSeats,
        };

        try {
            const data = await confirmReservation(payload);
            setResult(data);
        } catch (err) {
            alert("Thanh toán thất bại!");
        } finally {
            setIsProcessing(false);
        }
    };

    // ======================
    // SUCCESS UI
    // ======================
    if (result) {
        return (
            <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center text-white p-6">
                <div className="bg-[#111] p-8 rounded-2xl border border-green-500/30 text-center max-w-md w-full">
                    <h2 className="text-2xl font-black mb-4">Thanh toán thành công</h2>

                    <img
                        src={result.qrCodeDataUri}
                        alt="QR"
                        className="w-48 h-48 mx-auto bg-white p-2 rounded"
                    />

                    <p className="mt-4 text-gray-400 text-sm">
                        Tổng tiền: {result.totalPrice.toLocaleString()} VND
                    </p>

                    <button
                        onClick={() => navigate("/movies")}
                        className="mt-6 w-full py-3 bg-red-600 rounded-lg font-bold"
                    >
                        Về trang phim
                    </button>
                </div>
            </div>
        );
    }

    // ======================
    // UI
    // ======================
    return (
        <div className="min-h-screen bg-[#0a0a0a] text-white pt-24 pb-16 px-4">
            <div className="max-w-6xl mx-auto flex flex-col lg:flex-row gap-10">

                {/* LEFT */}
                <div className="flex-1 space-y-8">
                    <h1 className="text-3xl font-black">Thanh toán</h1>

                    <div className="grid grid-cols-3 gap-4">
                        {[1, 2, 3].map((id) => (
                            <button
                                key={id}
                                onClick={() => setPaymentMethod(id)}
                                className={`py-4 rounded-lg border ${paymentMethod === id
                                    ? "border-red-600 bg-red-600/10"
                                    : "border-white/10"
                                    }`}
                            >
                                Method {id}
                            </button>
                        ))}
                    </div>
                </div>

                {/* RIGHT */}
                <aside className="w-full lg:w-[350px]">
                    <div className="bg-[#111] p-6 rounded-xl border border-white/5 space-y-6">

                        <div className="text-right text-yellow-500 font-mono">
                            {formatTime(timeLeft)}
                        </div>

                        <div>
                            <p className="text-gray-400 text-sm mb-2">Ghế</p>
                            <div className="flex gap-2 flex-wrap">
                                {selectedSeats.map((seat) => (
                                    <span
                                        key={seat}
                                        className="px-2 py-1 bg-zinc-800 rounded text-red-500 font-bold"
                                    >
                                        {seat}
                                    </span>
                                ))}
                            </div>
                        </div>

                        <div className="flex justify-between text-sm">
                            <span>Tổng</span>
                            <span className="font-bold">
                                {(selectedSeats.length * 100000).toLocaleString()} VND
                            </span>
                        </div>

                        <button
                            onClick={handlePayment}
                            disabled={
                                isProcessing ||
                                timeLeft === 0 ||
                                selectedSeats.length === 0
                            }
                            className="w-full py-3 bg-red-600 rounded-lg font-bold disabled:bg-gray-700"
                        >
                            {isProcessing ? "Đang xử lý..." : "Thanh toán"}
                        </button>
                    </div>
                </aside>
            </div>
        </div>
    );
}
