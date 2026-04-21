import React from "react";

export default function MovieCard({ movie }) {
    return (
        <div className="group cursor-pointer">
            <div className="relative aspect-[150%] rounded-xl overflow-hidden">
                <img src={movie.img} alt={movie.title} />

                <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 flex flex-col justify-end p-4">
                    <button className="bg-red-600 py-2 mb-2">ĐẶT</button>
                    <button className="bg-white/20 py-2">CHI TIẾT</button>
                </div>
            </div>

            <h3 className="mt-2 font-bold">{movie.title}</h3>
            <p className="text-sm text-gray-400">{movie.genre}</p>
        </div>
    );
}