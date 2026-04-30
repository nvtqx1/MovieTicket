import React from "react";
import MovieCard from "../components/movies/MovieCard";
import FilterSidebar from "../components/movies/FilterSidebar";
import Pagination from "../components/movies/Pagination";
import ComingSoonCard from "../components/movies/ComingSoonCard";

export default function Movies() {
    // mock data (sau này thay bằng API)
    const moviesData = [
        {
            id: 1,
            title: "The Sands of Time",
            posterUrl: "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=400",
            genre: "Sci-Fi",
        },
        {
            id: 2,
            title: "Midnight Protocol",
            posterUrl: "https://images.unsplash.com/photo-1614113489855-66422ad300a4?q=80&w=400",
            genre: "Thriller",
        },
    ];

    return (
        <div className="min-h-screen bg-[#0d0d0d] pt-24 text-white">
            <main className="max-w-[1440px] mx-auto px-4 md:px-8 py-10 flex flex-col lg:flex-row gap-8">

                {/* Sidebar */}
                <aside className="w-full lg:w-[260px] shrink-0">
                    <FilterSidebar />
                </aside>

                {/* Content */}
                <section className="flex-1">
                    <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-4 gap-6">
                        {moviesData.map((movie) => (
                            <MovieCard key={movie.id} movie={movie} />
                        ))}

                        <ComingSoonCard />
                    </div>

                    <Pagination />
                </section>
            </main>
        </div>
    );
}
