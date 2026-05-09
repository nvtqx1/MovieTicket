import React, { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { searchMovies } from "../../services/api/movieService";
import { Search, X, Film } from "lucide-react";

export default function SearchBar() {
    const navigate = useNavigate();
    const [keyword, setKeyword] = useState("");
    const [results, setResults] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [searching, setSearching] = useState(false);
    const debounceRef = useRef(null);
    const containerRef = useRef(null);

    // Debounce search
    useEffect(() => {
        if (debounceRef.current) clearTimeout(debounceRef.current);
        if (!keyword.trim()) {
            setResults([]);
            setShowDropdown(false);
            return;
        }

        debounceRef.current = setTimeout(async () => {
            setSearching(true);
            try {
                const data = await searchMovies(keyword.trim());
                setResults(data || []);
                setShowDropdown(true);
            } catch (err) {
                console.error("Search error:", err);
                setResults([]);
            } finally {
                setSearching(false);
            }
        }, 350);

        return () => clearTimeout(debounceRef.current);
    }, [keyword]);

    // Close dropdown on click outside
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (containerRef.current && !containerRef.current.contains(e.target)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleSelect = (movieId) => {
        setShowDropdown(false);
        setKeyword("");
        navigate(`/movies/${movieId}`);
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (keyword.trim()) {
            setShowDropdown(false);
            navigate(`/movies?search=${encodeURIComponent(keyword.trim())}`);
        }
    };

    return (
        <section className="-mt-16 px-4 md:px-8 relative z-20">
            <div ref={containerRef} className="relative max-w-4xl mx-auto">
                <form onSubmit={handleSubmit} className="bg-[#111]/90 backdrop-blur-xl p-4 rounded-2xl border border-white/10 flex gap-3 shadow-2xl shadow-black/50">
                    <div className="relative flex-1">
                        <Search size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500" />
                        <input
                            className="w-full pl-11 pr-10 py-3 rounded-xl bg-white/5 border border-white/5 focus:border-red-500/50 focus:outline-none text-sm text-white placeholder-gray-500 transition-colors"
                            placeholder="Tìm kiếm phim..."
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                            onFocus={() => results.length > 0 && setShowDropdown(true)}
                        />
                        {keyword && (
                            <button
                                type="button"
                                onClick={() => { setKeyword(""); setResults([]); setShowDropdown(false); }}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white"
                            >
                                <X size={14} />
                            </button>
                        )}
                    </div>

                    <button
                        type="submit"
                        className="px-6 py-3 bg-red-600 hover:bg-red-700 rounded-xl text-white font-bold text-sm uppercase tracking-wider transition-colors"
                    >
                        Tìm
                    </button>
                </form>

                {/* Search Results Dropdown */}
                {showDropdown && (
                    <div className="absolute top-full left-0 right-0 mt-2 bg-[#111]/95 backdrop-blur-xl border border-white/10 rounded-xl shadow-2xl overflow-hidden max-h-80 overflow-y-auto z-50">
                        {searching ? (
                            <div className="p-4 text-center text-gray-500 text-sm">Đang tìm...</div>
                        ) : results.length === 0 ? (
                            <div className="p-4 text-center text-gray-500 text-sm">Không tìm thấy phim nào</div>
                        ) : (
                            results.map((movie) => (
                                <button
                                    key={movie.id}
                                    onClick={() => handleSelect(movie.id)}
                                    className="w-full flex items-center gap-4 p-3 hover:bg-white/5 transition-colors text-left border-b border-white/5 last:border-0"
                                >
                                    {movie.posterImageUrl ? (
                                        <img
                                            src={movie.posterImageUrl}
                                            alt={movie.title}
                                            className="w-10 h-14 object-cover rounded-lg flex-shrink-0"
                                            onError={(e) => { e.target.style.display = 'none'; }}
                                        />
                                    ) : (
                                        <div className="w-10 h-14 bg-zinc-800 rounded-lg flex items-center justify-center flex-shrink-0">
                                            <Film size={16} className="text-gray-600" />
                                        </div>
                                    )}
                                    <div className="flex-1 min-w-0">
                                        <p className="text-white font-medium text-sm truncate">{movie.title}</p>
                                        <p className="text-gray-500 text-xs">{movie.genre} • {movie.releaseYear}</p>
                                    </div>
                                </button>
                            ))
                        )}
                    </div>
                )}
            </div>
        </section>
    );
}