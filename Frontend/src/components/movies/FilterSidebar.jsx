import React from "react";
import { Search } from "lucide-react";

export default function FilterSidebar({
    search,
    selectedGenre,
    selectedReleaseYear,
    genres = [],
    releaseYears = [],
    onSearchChange,
    onGenreChange,
    onReleaseYearChange,
}) {
    return (
        <div className="bg-[#18181b] p-5 rounded-xl space-y-6">
            <h2 className="text-lg font-bold">Bộ lọc</h2>

            {/* Search */}
            <div className="flex items-center bg-black/40 border border-gray-700 px-3 py-2 rounded gap-2">
                <Search size={14} className="text-gray-500" />
                <input
                    type="text"
                    value={search}
                    onChange={(e) => onSearchChange(e.target.value)}
                    placeholder="Tìm phim..."
                    className="bg-transparent outline-none text-sm w-full"
                />
            </div>

            <div className="space-y-2">
                <label htmlFor="genre" className="text-xs font-bold uppercase tracking-widest text-gray-400">
                    Thể loại
                </label>
                <select
                    id="genre"
                    value={selectedGenre}
                    onChange={(e) => onGenreChange(e.target.value)}
                    className="w-full rounded bg-black/40 border border-gray-700 px-3 py-2 text-sm outline-none focus:border-red-500"
                >
                    <option value="">Tất cả</option>
                    {genres.map((genre) => (
                        <option key={genre} value={genre}>{genre}</option>
                    ))}
                </select>
            </div>

            <div className="space-y-2">
                <label htmlFor="releaseYear" className="text-xs font-bold uppercase tracking-widest text-gray-400">
                    Năm phát hành
                </label>
                <select
                    id="releaseYear"
                    value={selectedReleaseYear}
                    onChange={(e) => onReleaseYearChange(e.target.value)}
                    className="w-full rounded bg-black/40 border border-gray-700 px-3 py-2 text-sm outline-none focus:border-red-500"
                >
                    <option value="">Tất cả</option>
                    {releaseYears.map((year) => (
                        <option key={year} value={year}>{year}</option>
                    ))}
                </select>
            </div>
        </div>
    );
}
