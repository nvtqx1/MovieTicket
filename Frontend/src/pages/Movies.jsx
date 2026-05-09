import React, { useState, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import MovieCard from "../components/movies/MovieCard";
import FilterSidebar from "../components/movies/FilterSidebar";
import Pagination from "../components/movies/Pagination";
import { useMovies } from "../hooks/useMovies";

const ITEMS_PER_PAGE = 8;

export default function Movies() {
    const [searchParams] = useSearchParams();
    const [search, setSearch] = useState(searchParams.get("search") || "");
    const [selectedGenre, setSelectedGenre] = useState("");
    const [selectedReleaseYear, setSelectedReleaseYear] = useState("");
    const [currentPage, setCurrentPage] = useState(1);

    const { movies, loading, error } = useMovies();

    const genres = useMemo(() => {
        return [...new Set(movies.map((movie) => movie.genre).filter(Boolean))].sort();
    }, [movies]);

    const releaseYears = useMemo(() => {
        return [...new Set(movies.map((movie) => movie.releaseYear).filter(Boolean))].sort((a, b) => b - a);
    }, [movies]);

    // Filter FE theo search, genre và releaseYear từ MovieResponse
    const filteredMovies = useMemo(() => {
        let data = [...movies];

        if (search) {
            data = data.filter((m) =>
                m.title.toLowerCase().includes(search.toLowerCase())
            );
        }

        if (selectedGenre) {
            data = data.filter((m) => m.genre === selectedGenre);
        }

        if (selectedReleaseYear) {
            data = data.filter((m) => m.releaseYear === Number(selectedReleaseYear));
        }

        return data;
    }, [movies, search, selectedGenre, selectedReleaseYear]);

    // Pagination
    const totalPages = Math.ceil(filteredMovies.length / ITEMS_PER_PAGE);
    const paginatedMovies = useMemo(() => {
        const start = (currentPage - 1) * ITEMS_PER_PAGE;
        return filteredMovies.slice(start, start + ITEMS_PER_PAGE);
    }, [filteredMovies, currentPage]);

    // Reset về trang 1 khi filter thay đổi
    const handleSearchChange = (value) => {
        setSearch(value);
        setCurrentPage(1);
    };

    const handleGenreChange = (genre) => {
        setSelectedGenre(genre);
        setCurrentPage(1);
    };

    const handleReleaseYearChange = (releaseYear) => {
        setSelectedReleaseYear(releaseYear);
        setCurrentPage(1);
    };

    return (
        <div className="min-h-screen bg-[#0d0d0d] pt-24 text-white">
            <main className="max-w-[1440px] mx-auto px-4 md:px-8 py-10 flex flex-col lg:flex-row gap-8">

                <aside className="w-full lg:w-[260px] shrink-0">
                    <FilterSidebar
                        search={search}
                        selectedGenre={selectedGenre}
                        selectedReleaseYear={selectedReleaseYear}
                        genres={genres}
                        releaseYears={releaseYears}
                        onSearchChange={handleSearchChange}
                        onGenreChange={handleGenreChange}
                        onReleaseYearChange={handleReleaseYearChange}
                    />
                </aside>

                <section className="flex-1">
                    {/* Loading */}
                    {loading && (
                        <div className="flex justify-center items-center h-64">
                            <div className="w-10 h-10 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
                        </div>
                    )}

                    {/* Error */}
                    {error && (
                        <p className="text-center text-red-500 mt-20">{error}</p>
                    )}

                    {/* Empty state */}
                    {!loading && !error && filteredMovies.length === 0 && (
                        <div className="flex flex-col items-center justify-center h-64 text-gray-500 gap-2">
                            <p className="text-lg font-bold">Không tìm thấy phim nào</p>
                            <p className="text-sm">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
                        </div>
                    )}

                    {/* Grid */}
                    {!loading && !error && paginatedMovies.length > 0 && (
                        <>
                            <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-4 gap-6">
                                {paginatedMovies.map((movie) => (
                                    <MovieCard key={movie.id} movie={movie} />
                                ))}
                            </div>

                            <Pagination
                                currentPage={currentPage}
                                totalPages={totalPages}
                                onPageChange={setCurrentPage}
                            />
                        </>
                    )}
                </section>
            </main>
        </div>
    );
}
