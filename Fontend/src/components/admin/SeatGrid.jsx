import React, { useMemo } from "react";

export default function SeatGrid({ seats }) {

    const grouped = useMemo(() => {
        const map = {};

        seats.forEach(seat => {
            const row = seat.seatNumber[0];
            if (!map[row]) map[row] = [];
            map[row].push(seat);
        });

        return map;
    }, [seats]);

    const rows = Object.keys(grouped).sort();

    if (!rows.length) {
        return <p className="text-gray-500">Chưa có dữ liệu ghế</p>;
    }

    return (
        <div className="space-y-3">
            {rows.map(row => (
                <div key={row} className="flex items-center gap-2">

                    <div className="w-6 text-xs text-gray-400">{row}</div>

                    {grouped[row]
                        .sort((a, b) => {
                            const numA = parseInt(a.seatNumber.slice(1), 10);
                            const numB = parseInt(b.seatNumber.slice(1), 10);
                            return numA - numB;
                        })
                        .map(seat => {

                            const isBooked = seat.status === "BOOKED";
                            const isVip = seat.seatType === "VIP";
                            const isCouple = seat.seatType === "COUPLE";

                            return (
                                <div
                                    key={seat.seatNumber}
                                    title={`${seat.seatNumber} - ${seat.seatType} - ${seat.finalPrice ? seat.finalPrice.toLocaleString() + 'đ' : ''}`}
                                    className={`
                                        w-8 h-8 rounded flex items-center justify-center text-[10px] font-bold cursor-help
                                        ${isBooked ? "bg-zinc-700 text-gray-400" :
                                            isCouple ? "bg-pink-500 text-white" :
                                                isVip ? "bg-yellow-500 text-black" :
                                                    "bg-[#1a1a1a] text-white hover:bg-gray-700"}
                                    `}
                                >
                                    {seat.seatNumber.slice(1)}
                                </div>
                            );
                        })}
                </div>
            ))}

            {/* Chú thích (Legend) */}
            <div className="flex flex-wrap items-center gap-6 mt-8 pt-6 border-t border-white/10">
                <div className="flex items-center gap-2">
                    <div className="w-5 h-5 rounded bg-[#1a1a1a] border border-white/10"></div>
                    <span className="text-xs text-gray-400">NORMAL</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-5 h-5 rounded bg-yellow-500"></div>
                    <span className="text-xs text-gray-400">VIP</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-5 h-5 rounded bg-pink-500"></div>
                    <span className="text-xs text-gray-400">COUPLE</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-5 h-5 rounded bg-zinc-700"></div>
                    <span className="text-xs text-gray-400">Đã đặt</span>
                </div>
            </div>
        </div>
    );
}