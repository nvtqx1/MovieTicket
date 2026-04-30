import React from "react";
import MovieCard from "./MovieCard";
import { useMovies } from "../../hooks/useMovies";

export default function MovieSection() {
    const { movies, loading, error } = useMovies();

    if (loading) return <p className="mt-20 text-center">Đang tải...</p>;
    if (error) return <p className="mt-20 text-center text-red-500">{error}</p>;

    return (
        <section className="mt-20 px-4 md:px-8">
            <h2 className="text-2xl md:text-3xl font-bold mb-6">
                PHIM ĐANG CHIẾU
            </h2>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
                {movies.map((movie) => (
                    <MovieCard key={movie.id} movie={movie} />
                ))}
            </div>
        </section>
    );
}