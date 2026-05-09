import React, { useState, useEffect } from 'react';
import { Calendar, Clock, DollarSign, Plus, Video, MapPin, Zap, Trash2, Search, Filter } from 'lucide-react';
import { createShowtime, getAdminShowtimes, deleteShowtime } from '../services/api/showtimeService';
import { getMovies } from '../services/api/movieService';
import { getTheaters, getRoomsByTheater } from '../services/api/theaterService';

export default function AdminShowtimes() {
    const [loading, setLoading] = useState(false);
    const [initialLoading, setInitialLoading] = useState(true);
    const [successMsg, setSuccessMsg] = useState("");
    const [errorMsg, setErrorMsg] = useState("");

    const [movies, setMovies] = useState([]);
    const [theaters, setTheaters] = useState([]);
    const [rooms, setRooms] = useState([]);

    // Task 1.2: Showtime list + filter
    const [showtimeList, setShowtimeList] = useState([]);
    const [listLoading, setListLoading] = useState(false);
    const [filterTheaterId, setFilterTheaterId] = useState('');
    const [filterDate, setFilterDate] = useState(new Date().toISOString().split('T')[0]);
    const [activeTab, setActiveTab] = useState('list'); // 'list' | 'create'

    const [formData, setFormData] = useState({
        movieId: '',
        theaterId: '', // Used for UI only to fetch rooms
        roomId: '',
        showDate: '',
        showTime: '',
        price: 50000,
        isFlashSale: false
    });

    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                const [moviesData, theatersData] = await Promise.all([
                    getMovies(),
                    getTheaters()
                ]);
                setMovies(moviesData || []);
                setTheaters(theatersData || []);
            } catch (error) {
                setErrorMsg("Lỗi khi tải dữ liệu khởi tạo. Vui lòng thử lại.");
            } finally {
                setInitialLoading(false);
            }
        };
        fetchInitialData();
    }, []);

    // Task 1.2: Load showtime list
    useEffect(() => {
        if (!initialLoading) loadShowtimes();
    }, [filterTheaterId, filterDate, initialLoading]);

    const loadShowtimes = async () => {
        setListLoading(true);
        try {
            const data = await getAdminShowtimes({
                theaterId: filterTheaterId || undefined,
                date: filterDate || undefined
            });
            setShowtimeList(data);
        } catch (err) {
            console.error("Lỗi tải danh sách showtime:", err);
        } finally {
            setListLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!confirm("Bạn chắc chắn muốn xóa lịch chiếu này?")) return;
        try {
            await deleteShowtime(id);
            setSuccessMsg("Xóa lịch chiếu thành công!");
            loadShowtimes();
        } catch (err) {
            setErrorMsg(err.response?.data?.message || "Lỗi khi xóa");
        }
    };

    const handleChange = async (e) => {
        const { name, value, type, checked } = e.target;
        const val = type === 'checkbox' ? checked : value;
        
        setFormData(prev => ({
            ...prev,
            [name]: val
        }));

        if (name === 'theaterId') {
            setFormData(prev => ({ ...prev, roomId: '' }));
            if (value) {
                try {
                    const roomsData = await getRoomsByTheater(value);
                    setRooms(roomsData || []);
                } catch (err) {
                    console.error("Lỗi khi tải danh sách phòng", err);
                    setRooms([]);
                }
            } else {
                setRooms([]);
            }
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setSuccessMsg("");
        setErrorMsg("");

        if (!formData.roomId) {
            setErrorMsg("Vui lòng chọn phòng chiếu!");
            setLoading(false);
            return;
        }

        try {
            const dataToSubmit = {
                movieId: Number(formData.movieId),
                roomId: Number(formData.roomId),
                showDate: formData.showDate,
                showTime: formData.showTime + (formData.showTime.length === 5 ? ":00" : ""),
                price: Number(formData.price),
                isFlashSale: formData.isFlashSale
            };

            await createShowtime(dataToSubmit);
            setSuccessMsg(`Tạo lịch chiếu thành công!`);
            
            setFormData(prev => ({
                ...prev,
                showTime: '',
                isFlashSale: false
            }));
            loadShowtimes(); // Refresh list
        } catch (error) {
            setErrorMsg(error.response?.data?.message || "Lỗi khi tạo lịch chiếu.");
        } finally {
            setLoading(false);
        }
    };

    if (initialLoading) {
        return <div className="p-10 text-white">Đang tải dữ liệu...</div>;
    }

    return (
        <div className="p-10 text-white">
            <header className="mb-10">
                <h1 className="text-2xl font-black uppercase tracking-[0.1em] flex items-center gap-3">
                    <Calendar className="text-red-600" size={28} />
                    Quản lý <span className="text-red-600">Lịch Chiếu</span>
                </h1>
                <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">
                    Quản lý, lọc và thêm lịch chiếu cho các rạp
                </p>
            </header>

            {/* TABS */}
            <div className="flex gap-4 mb-8">
                <button
                    onClick={() => setActiveTab('list')}
                    className={`px-6 py-2 rounded-lg text-sm font-bold transition-all ${
                        activeTab === 'list' 
                            ? 'bg-red-600 text-white' 
                            : 'bg-[#1a1a1a] text-gray-400 hover:bg-[#222]'
                    }`}
                >
                    <Filter size={14} className="inline mr-2" /> Danh sách & Lọc
                </button>
                <button
                    onClick={() => setActiveTab('create')}
                    className={`px-6 py-2 rounded-lg text-sm font-bold transition-all ${
                        activeTab === 'create' 
                            ? 'bg-red-600 text-white' 
                            : 'bg-[#1a1a1a] text-gray-400 hover:bg-[#222]'
                    }`}
                >
                    <Plus size={14} className="inline mr-2" /> Tạo mới
                </button>
            </div>

            {successMsg && (
                <div className="mb-6 p-4 bg-green-500/10 border border-green-500/20 text-green-400 rounded-lg text-sm font-bold">
                    {successMsg}
                </div>
            )}
            {errorMsg && (
                <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 text-red-400 rounded-lg text-sm font-bold">
                    {errorMsg}
                </div>
            )}

            {/* ═══ TASK 1.2: LIST TAB ═══ */}
            {activeTab === 'list' && (
                <div className="space-y-6">
                    {/* FILTERS */}
                    <div className="bg-[#111] p-6 rounded-xl border border-white/5 flex flex-wrap gap-4 items-end">
                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Rạp</label>
                            <select
                                value={filterTheaterId}
                                onChange={(e) => setFilterTheaterId(e.target.value)}
                                className="bg-[#1a1a1a] border border-white/10 rounded-lg py-2 px-4 text-sm text-white appearance-none min-w-[200px]"
                            >
                                <option value="">-- Tất cả rạp --</option>
                                {theaters.map(t => (
                                    <option key={t.id} value={t.id}>{t.name}</option>
                                ))}
                            </select>
                        </div>
                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Ngày</label>
                            <input
                                type="date"
                                value={filterDate}
                                onChange={(e) => setFilterDate(e.target.value)}
                                className="bg-[#1a1a1a] border border-white/10 rounded-lg py-2 px-4 text-sm text-white"
                            />
                        </div>
                        <button
                            onClick={() => { setFilterTheaterId(''); setFilterDate(''); }}
                            className="px-4 py-2 text-xs text-gray-400 hover:text-white border border-white/10 rounded-lg"
                        >
                            Xóa bộ lọc
                        </button>
                    </div>

                    {/* TABLE */}
                    <div className="bg-[#111] rounded-xl border border-white/5 overflow-hidden">
                        <div className="p-4 border-b border-white/5 flex justify-between items-center">
                            <span className="text-sm font-bold text-gray-400">
                                {listLoading ? "Đang tải..." : `${showtimeList.length} lịch chiếu`}
                            </span>
                        </div>

                        {showtimeList.length === 0 && !listLoading ? (
                            <div className="p-12 text-center text-gray-500 text-sm">
                                Không tìm thấy lịch chiếu nào. Thử thay đổi bộ lọc hoặc tạo mới.
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full text-sm">
                                    <thead>
                                        <tr className="border-b border-white/5 text-gray-500 text-xs uppercase tracking-widest">
                                            <th className="text-left p-4">ID</th>
                                            <th className="text-left p-4">Phim</th>
                                            <th className="text-left p-4">Rạp / Phòng</th>
                                            <th className="text-left p-4">Ngày</th>
                                            <th className="text-left p-4">Giờ</th>
                                            <th className="text-right p-4">Giá</th>
                                            <th className="text-center p-4">Ghế trống</th>
                                            <th className="text-center p-4">Flash</th>
                                            <th className="text-center p-4">Hành động</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {showtimeList.map(st => (
                                            <tr key={st.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                                <td className="p-4 text-gray-500 font-mono">#{st.id}</td>
                                                <td className="p-4 font-bold">{st.movie?.title || "—"}</td>
                                                <td className="p-4 text-gray-400">
                                                    {st.theater?.name || "—"} <span className="text-gray-600">•</span> {st.roomName || "—"}
                                                </td>
                                                <td className="p-4">{st.showDate}</td>
                                                <td className="p-4">{st.showTime}</td>
                                                <td className="p-4 text-right text-yellow-400 font-mono">
                                                    {Number(st.price || 0).toLocaleString()}đ
                                                </td>
                                                <td className="p-4 text-center">
                                                    <span className={`${st.availableSeats === 0 ? 'text-red-400' : 'text-green-400'}`}>
                                                        {st.availableSeats}/{st.totalSeats}
                                                    </span>
                                                </td>
                                                <td className="p-4 text-center">
                                                    {st.isFlashSale && <Zap size={14} className="text-yellow-500 inline" />}
                                                </td>
                                                <td className="p-4 text-center">
                                                    <button
                                                        onClick={() => handleDelete(st.id)}
                                                        className="p-2 text-red-400 hover:text-red-300 hover:bg-red-500/10 rounded-lg transition-colors"
                                                        title="Xóa lịch chiếu"
                                                    >
                                                        <Trash2 size={14} />
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* ═══ CREATE TAB ═══ */}
            {activeTab === 'create' && (
                <div className="bg-[#111] p-8 rounded-xl border border-white/5 max-w-3xl">
                    <h2 className="text-sm font-black uppercase tracking-widest mb-6 flex items-center gap-2">
                        <span className="w-1 h-4 bg-red-600 rounded-full"></span> Tạo Lịch Chiếu Mới
                    </h2>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Phim</label>
                                <div className="relative">
                                    <Video className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                    <select name="movieId" value={formData.movieId} onChange={handleChange} required
                                        className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors appearance-none">
                                        <option value="">-- Chọn phim --</option>
                                        {movies.map(m => (
                                            <option key={m.id} value={m.id}>{m.title}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Rạp Chiếu</label>
                                <div className="relative">
                                    <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                    <select name="theaterId" value={formData.theaterId} onChange={handleChange} required
                                        className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors appearance-none">
                                        <option value="">-- Chọn rạp --</option>
                                        {theaters.map(t => (
                                            <option key={t.id} value={t.id}>{t.name}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Phòng Chiếu</label>
                                <select name="roomId" value={formData.roomId} onChange={handleChange} required
                                    disabled={!formData.theaterId || rooms.length === 0}
                                    className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 px-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors appearance-none disabled:opacity-50">
                                    <option value="">-- Chọn phòng --</option>
                                    {rooms.map(r => (
                                        <option key={r.id} value={r.id}>{r.name} (Sức chứa: {r.capacity})</option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Giá vé cơ bản (VNĐ)</label>
                                <div className="relative">
                                    <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                    <input type="number" name="price" value={formData.price} onChange={handleChange} required min="0"
                                        className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors" />
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Ngày chiếu</label>
                                <div className="relative">
                                    <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                    <input type="date" name="showDate" value={formData.showDate} onChange={handleChange} required
                                        className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors" />
                                </div>
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Giờ chiếu</label>
                                <div className="relative">
                                    <Clock className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                    <input type="time" name="showTime" value={formData.showTime} onChange={handleChange} required
                                        className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors" />
                                </div>
                            </div>
                        </div>

                        <div className="flex items-center gap-3">
                            <input type="checkbox" id="isFlashSale" name="isFlashSale"
                                checked={formData.isFlashSale} onChange={handleChange}
                                className="w-4 h-4 accent-red-600" />
                            <label htmlFor="isFlashSale" className="text-sm font-bold flex items-center gap-1 text-yellow-500 cursor-pointer">
                                <Zap size={16} /> Flash Sale
                            </label>
                        </div>

                        <button type="submit" disabled={loading}
                            className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-4 rounded-lg flex items-center justify-center gap-2 transition-colors disabled:opacity-50">
                            {loading ? (
                                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                            ) : (
                                <>
                                    <Plus size={20} />
                                    Tạo Lịch Chiếu
                                </>
                            )}
                        </button>
                    </form>
                </div>
            )}
        </div>
    );
}
