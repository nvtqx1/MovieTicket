import React from "react";
import MovieCard from "../common/MovieCard";

export default function MovieSection() {
    const movies = [
        { title: "THE SILENT SHADOW", genre: "Crime", img: "https://images.unsplash.com/photo-1509281373149-e957c6296406" },
        { title: "VOID VOYAGER", genre: "Sci-Fi", img: "https://images.unsplash.com/photo-1462331940025-496dfbfc7564" },
    ];

    return (
        <section className="mt-20 px-8">
            <h2 className="text-3xl font-bold mb-6">PHIM ĐANG CHIẾU</h2>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                {movies.map((m, i) => (
                    <MovieCard key={i} movie={m} />
                ))}
            </div>
        </section>
    );
}