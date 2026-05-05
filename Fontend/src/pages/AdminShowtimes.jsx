import React, { useState, useEffect } from 'react';
import { Calendar, Clock, DollarSign, Plus, Video, MapPin, Zap } from 'lucide-react';
import { createShowtime } from '../services/api/showtimeService';
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

    const handleChange = async (e) => {
        const { name, value, type, checked } = e.target;
        const val = type === 'checkbox' ? checked : value;
        
        setFormData(prev => ({
            ...prev,
            [name]: val
        }));

        // If theater changes, fetch its rooms
        if (name === 'theaterId') {
            setFormData(prev => ({ ...prev, roomId: '' })); // Reset room
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
                showTime: formData.showTime + (formData.showTime.length === 5 ? ":00" : ""), // Ensure HH:mm:ss
                price: Number(formData.price),
                isFlashSale: formData.isFlashSale
            };

            await createShowtime(dataToSubmit);
            setSuccessMsg(`Tạo lịch chiếu thành công!`);
            
            // Reset some form fields
            setFormData(prev => ({
                ...prev,
                showTime: '',
                isFlashSale: false
            }));
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
                    Thêm lịch chiếu mới cho các rạp
                </p>
            </header>

            <div className="bg-[#111] p-8 rounded-xl border border-white/5 max-w-3xl">
                <h2 className="text-sm font-black uppercase tracking-widest mb-6 flex items-center gap-2">
                    <span className="w-1 h-4 bg-red-600 rounded-full"></span> Tạo Lịch Chiếu Mới
                </h2>

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

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Phim
                            </label>
                            <div className="relative">
                                <Video className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                <select
                                    name="movieId"
                                    value={formData.movieId}
                                    onChange={handleChange}
                                    required
                                    className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors appearance-none"
                                >
                                    <option value="">-- Chọn phim --</option>
                                    {movies.map(m => (
                                        <option key={m.id} value={m.id}>{m.title}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Rạp Chiếu
                            </label>
                            <div className="relative">
                                <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                <select
                                    name="theaterId"
                                    value={formData.theaterId}
                                    onChange={handleChange}
                                    required
                                    className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors appearance-none"
                                >
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
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Phòng Chiếu
                            </label>
                            <select
                                name="roomId"
                                value={formData.roomId}
                                onChange={handleChange}
                                required
                                disabled={!formData.theaterId || rooms.length === 0}
                                className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 px-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors appearance-none disabled:opacity-50"
                            >
                                <option value="">-- Chọn phòng --</option>
                                {rooms.map(r => (
                                    <option key={r.id} value={r.id}>{r.name} (Sức chứa: {r.capacity})</option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Giá vé cơ bản (VNĐ)
                            </label>
                            <div className="relative">
                                <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                <input
                                    type="number"
                                    name="price"
                                    value={formData.price}
                                    onChange={handleChange}
                                    required
                                    min="0"
                                    className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors"
                                />
                            </div>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Ngày chiếu
                            </label>
                            <div className="relative">
                                <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                <input
                                    type="date"
                                    name="showDate"
                                    value={formData.showDate}
                                    onChange={handleChange}
                                    required
                                    className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors"
                                />
                            </div>
                        </div>
                        <div>
                            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
                                Giờ chiếu
                            </label>
                            <div className="relative">
                                <Clock className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
                                <input
                                    type="time"
                                    name="showTime"
                                    value={formData.showTime}
                                    onChange={handleChange}
                                    required
                                    className="w-full bg-[#1a1a1a] border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-red-600 transition-colors"
                                />
                            </div>
                        </div>
                    </div>

                    <div className="flex items-center gap-3">
                        <input
                            type="checkbox"
                            id="isFlashSale"
                            name="isFlashSale"
                            checked={formData.isFlashSale}
                            onChange={handleChange}
                            className="w-4 h-4 accent-red-600"
                        />
                        <label htmlFor="isFlashSale" className="text-sm font-bold flex items-center gap-1 text-yellow-500 cursor-pointer">
                            <Zap size={16} /> Flash Sale
                        </label>
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-4 rounded-lg flex items-center justify-center gap-2 transition-colors disabled:opacity-50"
                    >
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
        </div>
    );
}
