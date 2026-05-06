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
                            const isVip = seat.type === "VIP";

                            return (
                                <div
                                    key={seat.seatNumber}
                                    className={`
                                        w-8 h-8 rounded flex items-center justify-center text-[10px] font-bold
                                        ${isBooked ? "bg-zinc-700 text-gray-400" :
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
        </div>
    );
}