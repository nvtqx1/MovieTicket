import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Armchair, CreditCard } from "lucide-react";

// ===== MOCK API =====
const mockFetchShowtime = async (id) => {
    // giả lập delay
    await new Promise((res) => setTimeout(res, 500));

    return {
        id,
        movieTitle: "Avengers: Endgame",
        theater: {
            name: "CGV Vincom Bà Triệu",
            location: "Hà Nội",
        },
        time: "20:00 - 12/01/2026",
        price: 90000,
        availableSeats: [
            "A1", "A2", "A3", "A4", "A5", "A6",
            "B1", "B2", "B3", "B4", "B5", "B6",
            "C1", "C2", "C3", "C4", "C5", "C6",
        ],
        occupiedSeats: ["A1", "B2", "C3"], // ghế đã đặt
    };
};

export default function Booking() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [showtime, setShowtime] = useState(null);
    const [selectedSeats, setSelectedSeats] = useState([]);

    // ===== FETCH DATA =====
    useEffect(() => {
        const fetchData = async () => {
            const data = await mockFetchShowtime(id);
            setShowtime(data);
        };
        fetchData();
    }, [id]);

    // ===== TOGGLE SEAT =====
    const toggleSeat = (seat) => {
        if (showtime.occupiedSeats.includes(seat)) return;

        setSelectedSeats((prev) =>
            prev.includes(seat)
                ? prev.filter((s) => s !== seat)
                : [...prev, seat]
        );
    };

    // ===== CHECKOUT =====
    const handleCheckout = () => {
        if (selectedSeats.length === 0) {
            alert("Vui lòng chọn ghế!");
            return;
        }

        //Cần đưa thông tin showtime, ghế đã chọn, tổng tiền... sang trang thanh toán(Chưa hoàn thiện)
        const reservationId = Date.now();

        navigate(`/checkout/${reservationId}`, {
            state: {
                showtimeId: id,
                seats: selectedSeats,
                total: selectedSeats.length * showtime.price,
            },
        });
    };

    if (!showtime) {
        return (
            <div className="min-h-screen flex items-center justify-center text-white">
                Loading...
            </div>
        );
    }

    const total = selectedSeats.length * showtime.price;

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
                        {showtime.movieTitle}
                    </h1>
                    <p className="text-sm text-gray-400 mt-2">
                        {showtime.theater.name} • {showtime.time}
                    </p>
                </header>

                <section className="grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-8">

                    {/* SEAT MAP */}
                    <div className="bg-[#111] border border-white/5 rounded-xl p-6">
                        <div className="w-full h-2 bg-white/20 rounded-full mb-3" />
                        <p className="text-center text-[10px] text-gray-500 uppercase tracking-widest mb-8">
                            Màn hình
                        </p>

                        <div className="grid grid-cols-6 gap-3 max-w-xl mx-auto">
                            {showtime.availableSeats.map((seat) => {
                                const isSelected = selectedSeats.includes(seat);
                                const isOccupied = showtime.occupiedSeats.includes(seat);

                                return (
                                    <button
                                        key={seat}
                                        onClick={() => toggleSeat(seat)}
                                        disabled={isOccupied}
                                        className={`aspect-square rounded-md border text-xs font-bold flex items-center justify-center gap-1 transition
                                            ${isOccupied
                                                ? "bg-gray-700 text-gray-500 cursor-not-allowed"
                                                : isSelected
                                                    ? "bg-red-600 text-white border-red-500"
                                                    : "bg-[#1f1f1f] text-gray-300 border-white/10 hover:bg-red-600"
                                            }
                                        `}
                                    >
                                        <Armchair size={14} />
                                        {seat}
                                    </button>
                                );
                            })}
                        </div>

                        {/* LEGEND */}
                        <div className="flex justify-center gap-6 mt-8 text-xs text-gray-400">
                            <span className="flex items-center gap-2">
                                <span className="w-3 h-3 bg-[#1f1f1f] border border-white/10"></span> Trống
                            </span>
                            <span className="flex items-center gap-2">
                                <span className="w-3 h-3 bg-red-600"></span> Đang chọn
                            </span>
                            <span className="flex items-center gap-2">
                                <span className="w-3 h-3 bg-gray-600"></span> Đã đặt
                            </span>
                        </div>
                    </div>

                    {/* SIDEBAR */}
                    <aside className="bg-[#111] border border-white/5 rounded-xl p-6 h-fit">
                        <h2 className="text-lg font-bold mb-6">Thông tin vé</h2>

                        <div className="space-y-4 text-sm">
                            <div className="flex justify-between">
                                <span className="text-gray-400">Rạp</span>
                                <span>{showtime.theater.name}</span>
                            </div>

                            <div className="flex justify-between">
                                <span className="text-gray-400">Giờ chiếu</span>
                                <span>{showtime.time}</span>
                            </div>

                            <div className="flex justify-between">
                                <span className="text-gray-400">Ghế</span>
                                <span className="font-bold">
                                    {selectedSeats.length > 0
                                        ? selectedSeats.join(", ")
                                        : "Chưa chọn"}
                                </span>
                            </div>

                            <div className="flex justify-between border-t border-white/10 pt-4 mt-4">
                                <span className="text-gray-400">Tổng tiền</span>
                                <span className="font-bold text-red-500">
                                    {total.toLocaleString()}đ
                                </span>
                            </div>
                        </div>

                        <button
                            onClick={handleCheckout}
                            className="mt-8 w-full py-3.5 rounded-lg bg-red-600 hover:bg-red-700 text-white font-black uppercase tracking-widest text-sm flex items-center justify-center gap-2"
                        >
                            <CreditCard size={16} />
                            Thanh toán
                        </button>
                    </aside>
                </section>
            </main>
        </div>
    );
}
