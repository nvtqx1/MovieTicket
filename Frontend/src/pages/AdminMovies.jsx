import React, { useState, useCallback, useMemo } from "react";
import { useMovies } from "../hooks/useMovies";
import { createMovie, updateMovie, deleteMovie } from "../services/api/movieService";

export default function AdminMovies() {
    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const { movies, loading, error } = useMovies({ size: 1000 }, refreshTrigger);

    // Filter states
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedGenre, setSelectedGenre] = useState("");
    const [selectedYear, setSelectedYear] = useState("");

    // Compute unique genres and years for dropdowns
    const genres = useMemo(() => {
        return [...new Set(movies.map((movie) => movie.genre).filter(Boolean))].sort();
    }, [movies]);

    const releaseYears = useMemo(() => {
        return [...new Set(movies.map((movie) => movie.releaseYear).filter(Boolean))].sort((a, b) => b - a);
    }, [movies]);

    // Apply filters
    const filteredMovies = useMemo(() => {
        let data = [...movies];
        if (searchQuery) {
            data = data.filter((m) =>
                m.title.toLowerCase().includes(searchQuery.toLowerCase())
            );
        }
        if (selectedGenre) {
            data = data.filter((m) => m.genre === selectedGenre);
        }
        if (selectedYear) {
            data = data.filter((m) => m.releaseYear === Number(selectedYear));
        }
        return data;
    }, [movies, searchQuery, selectedGenre, selectedYear]);

    const [open, setOpen] = useState(false);
    const [editing, setEditing] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const [form, setForm] = useState({
        title: "",
        genre: "",
        releaseYear: "",
        posterImageUrl: "",
        description: "",
    });

    // =========================
    // HANDLERS
    // =========================
    const openCreate = () => {
        setEditing(null);
        setForm({
            title: "",
            genre: "",
            releaseYear: "",
            posterImageUrl: "",
            description: "",
        });
        setOpen(true);
    };

    const openEdit = (movie) => {
        setEditing(movie);
        setForm(movie);
        setOpen(true);
    };

    const handleSubmit = async () => {
        if (!form.title.trim()) return alert("Thiếu tên phim");
        if (!form.genre.trim()) return alert("Thiếu thể loại");
        if (!form.releaseYear) return alert("Thiếu năm phát hành");

        try {
            setSubmitting(true);
            if (editing) {
                await updateMovie(editing.id, form);
            } else {
                await createMovie(form);
            }
            setOpen(false);
            // Refresh danh sách phim
            setRefreshTrigger(prev => prev + 1);
        } catch (err) {
            alert("Lỗi: " + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (movie) => {
        if (!confirm(`Xóa phim "${movie.title}"?`)) return;

        try {
            setSubmitting(true);
            await deleteMovie(movie.id);
            // Refresh danh sách phim
            setRefreshTrigger(prev => prev + 1);
        } catch (err) {
            alert("Lỗi: " + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    // =========================
    // UI STATES
    // =========================
    if (loading) return <div className="p-10 text-white">Đang tải...</div>;
    if (error) return <div className="p-10 text-red-500">Lỗi: {error}</div>;

    return (
        <div className="p-10 text-white space-y-6">

            {/* HEADER */}
            <header className="mb-10 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-black uppercase tracking-[0.1em]">Quản lý <span className="text-red-600">Phim</span></h1>
                    <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">Danh sách phim trong hệ thống TMT.</p>
                </div>
                <button
                    onClick={openCreate}
                    className="bg-red-600 px-4 py-2 rounded-lg text-sm font-bold flex shrink-0"
                >
                    + Thêm phim
                </button>
            </header>

            {/* FILTER BAR */}
            <div className="bg-[#111] p-4 rounded-lg border border-white/5 flex flex-col md:flex-row gap-4">
                <div className="flex-1">
                    <input
                        type="text"
                        placeholder="Tìm kiếm tên phim..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-full bg-black border border-white/10 rounded-lg p-2.5 text-sm outline-none focus:border-red-500 transition-colors"
                    />
                </div>
                <div className="w-full md:w-48">
                    <select
                        value={selectedGenre}
                        onChange={(e) => setSelectedGenre(e.target.value)}
                        className="w-full bg-black border border-white/10 rounded-lg p-2.5 text-sm outline-none focus:border-red-500 transition-colors"
                    >
                        <option value="">Tất cả thể loại</option>
                        {genres.map(g => (
                            <option key={g} value={g}>{g}</option>
                        ))}
                    </select>
                </div>
                <div className="w-full md:w-48">
                    <select
                        value={selectedYear}
                        onChange={(e) => setSelectedYear(e.target.value)}
                        className="w-full bg-black border border-white/10 rounded-lg p-2.5 text-sm outline-none focus:border-red-500 transition-colors"
                    >
                        <option value="">Tất cả năm</option>
                        {releaseYears.map(y => (
                            <option key={y} value={y}>{y}</option>
                        ))}
                    </select>
                </div>
            </div>

            {/* TABLE */}
            <div className="bg-[#111] border border-white/5 rounded-lg overflow-hidden">
                <table className="w-full text-sm">
                    <thead className="text-gray-400 border-b border-white/5">
                        <tr>
                            <th className="p-3 text-left">ID</th>
                            <th>Poster</th>
                            <th>Tên</th>
                            <th>Năm</th>
                            <th>Thể loại</th>
                            <th></th>
                        </tr>
                    </thead>

                    <tbody>
                        {filteredMovies.map((m) => (
                            <tr key={m.id} className="border-t border-white/5 hover:bg-white/5">
                                <td className="p-3">#{m.id}</td>

                                <td>
                                    <img
                                        src={m.posterImageUrl}
                                        className="w-10 h-14 object-cover rounded"
                                    />
                                </td>

                                <td className="font-bold">{m.title}</td>
                                <td>{m.releaseYear}</td>
                                <td className="text-gray-400">{m.genre}</td>

                                <td className="space-x-2 text-right pr-4">
                                    <button onClick={() => openEdit(m)}>✏️</button>
                                    <button
                                        onClick={() => handleDelete(m)}
                                        className="text-red-500"
                                        disabled={submitting}
                                    >
                                        🗑️
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {filteredMovies.length === 0 && (
                            <tr>
                                <td colSpan="6" className="p-8 text-center text-gray-500">
                                    Không có dữ liệu phim phù hợp
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>

            {/* DRAWER */}
            {open && (
                <div className="fixed inset-0 bg-black/60 flex justify-end z-50">
                    <div className="w-[420px] bg-[#111] h-full p-6 space-y-4 overflow-y-auto">

                        <h2 className="text-lg font-bold">
                            {editing ? "Cập nhật phim" : "Thêm phim mới"}
                        </h2>

                        <input
                            placeholder="Tên phim *"
                            value={form.title}
                            onChange={(e) => setForm({ ...form, title: e.target.value })}
                            className="w-full p-3 bg-black border border-white/10 rounded"
                            disabled={submitting}
                        />

                        <input
                            placeholder="Thể loại *"
                            value={form.genre}
                            onChange={(e) => setForm({ ...form, genre: e.target.value })}
                            className="w-full p-3 bg-black border border-white/10 rounded"
                            disabled={submitting}
                        />

                        <input
                            placeholder="Năm phát hành *"
                            type="number"
                            value={form.releaseYear}
                            onChange={(e) =>
                                setForm({ ...form, releaseYear: e.target.value })
                            }
                            className="w-full p-3 bg-black border border-white/10 rounded"
                            disabled={submitting}
                        />

                        <input
                            placeholder="URL Poster"
                            value={form.posterImageUrl}
                            onChange={(e) =>
                                setForm({ ...form, posterImageUrl: e.target.value })
                            }
                            className="w-full p-3 bg-black border border-white/10 rounded"
                            disabled={submitting}
                        />

                        <textarea
                            placeholder="Mô tả phim"
                            value={form.description || ""}
                            onChange={(e) =>
                                setForm({ ...form, description: e.target.value })
                            }
                            className="w-full p-3 bg-black border border-white/10 rounded resize-none"
                            rows={4}
                            disabled={submitting}
                        />

                        <div className="flex justify-end gap-3 pt-4">
                            <button
                                onClick={() => setOpen(false)}
                                disabled={submitting}
                                className="px-4 py-2 border border-white/20 rounded hover:bg-white/5"
                            >
                                Hủy
                            </button>
                            <button
                                onClick={handleSubmit}
                                disabled={submitting}
                                className="bg-red-600 px-4 py-2 rounded hover:bg-red-700 disabled:opacity-50"
                            >
                                {submitting ? "Đang xử lý..." : "Lưu"}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}