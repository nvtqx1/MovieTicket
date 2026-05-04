import React from "react";

export default function TheaterCard({ theater, onDetail, onShowtime }) {
    return (
        <div className="bg-[#18181b] rounded-xl overflow-hidden border border-zinc-800 flex flex-col">

            <div className="aspect-video bg-zinc-900">
                {theater.image ? (
                    <img
                        src={theater.image}
                        alt={theater.name}
                        className="w-full h-full object-cover"
                    />
                ) : (
                    <div className="flex items-center justify-center h-full text-zinc-600">
                        🏪
                    </div>
                )}
            </div>

            <div className="p-5 flex flex-col flex-grow">
                <h3 className="font-bold text-white mb-2">{theater.name}</h3>
                <p className="text-sm text-gray-400 mb-2">{theater.location}</p>
                <p className="text-sm text-gray-400">
                    {theater.capacity} ghế
                </p>

                <div className="flex gap-3 mt-auto pt-4">
                    <button onClick={onDetail} className="flex-1 border py-2 text-sm">
                        Chi tiết
                    </button>
                    <button
                        onClick={onShowtime}
                        className="flex-1 bg-red-600 py-2 text-sm"
                    >
                        Lịch chiếu
                    </button>
                </div>
            </div>
        </div>
    );
}