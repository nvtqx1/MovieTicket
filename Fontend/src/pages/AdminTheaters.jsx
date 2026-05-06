import { useState, useEffect } from 'react';
import { getTheaters, createTheater } from '../services/api/theaterService';
import Error from '../components/common/Error';

const AdminTheaters = () => {
    const [theaters, setTheaters] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    const [isAdding, setIsAdding] = useState(false);
    const [formData, setFormData] = useState({ name: '', location: '', capacity: 100 });

    useEffect(() => {
        fetchTheaters();
    }, []);

    const fetchTheaters = async () => {
        try {
            setLoading(true);
            const data = await getTheaters();
            setTheaters(data);
            setError(null);
        } catch (err) {
            setError('Lỗi khi tải danh sách rạp chiếu');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleAdd = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            await createTheater(formData);
            setFormData({ name: '', location: '', capacity: 100 });
            setIsAdding(false);
            await fetchTheaters();
        } catch (err) {
            setError('Lỗi khi tạo rạp chiếu');
            setLoading(false);
        }
    };

    if (loading && theaters.length === 0) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-pink-500"></div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-white">Quản lý Rạp chiếu</h1>
                <button 
                    onClick={() => setIsAdding(!isAdding)}
                    className="px-4 py-2 bg-pink-600 hover:bg-pink-700 text-white rounded-lg transition-colors font-medium"
                >
                    {isAdding ? 'Hủy' : '+ Thêm Rạp'}
                </button>
            </div>

            {error && <Error message={error} />}

            {isAdding && (
                <div className="bg-[#1a1a1a] p-6 rounded-xl border border-white/10">
                    <h2 className="text-lg font-semibold text-white mb-4">Thêm Rạp Mới</h2>
                    <form onSubmit={handleAdd} className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-400 mb-1">Tên Rạp</label>
                                <input 
                                    type="text" 
                                    required
                                    value={formData.name}
                                    onChange={e => setFormData({...formData, name: e.target.value})}
                                    className="w-full bg-zinc-900 border border-white/10 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-pink-500"
                                    placeholder="VD: BHD Star The Garden"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-400 mb-1">Vị trí</label>
                                <input 
                                    type="text" 
                                    required
                                    value={formData.location}
                                    onChange={e => setFormData({...formData, location: e.target.value})}
                                    className="w-full bg-zinc-900 border border-white/10 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-pink-500"
                                    placeholder="VD: Tầng 4, TTTM The Garden..."
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-400 mb-1">Sức chứa (Người)</label>
                                <input 
                                    type="number" 
                                    required
                                    min="1"
                                    value={formData.capacity}
                                    onChange={e => setFormData({...formData, capacity: e.target.value})}
                                    className="w-full bg-zinc-900 border border-white/10 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-pink-500"
                                />
                            </div>
                        </div>
                        <div className="flex justify-end pt-2">
                            <button 
                                type="submit"
                                disabled={loading}
                                className="px-6 py-2 bg-pink-600 hover:bg-pink-700 text-white rounded-lg transition-colors font-medium disabled:opacity-50"
                            >
                                Lưu Rạp
                            </button>
                        </div>
                    </form>
                </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {theaters.map(theater => (
                    <div key={theater.id} className="bg-[#1a1a1a] rounded-xl border border-white/10 overflow-hidden hover:border-pink-500/30 transition-colors">
                        <div className="p-6">
                            <div className="flex justify-between items-start mb-4">
                                <div>
                                    <h3 className="text-xl font-bold text-white mb-1">{theater.name}</h3>
                                    <p className="text-sm text-gray-400 line-clamp-2">{theater.location}</p>
                                </div>
                                <span className="px-3 py-1 bg-zinc-800 text-pink-400 rounded-full text-xs font-bold">
                                    ID: {theater.id}
                                </span>
                            </div>
                            
                            <div className="flex items-center text-sm text-gray-300">
                                <span className="font-medium mr-2">Sức chứa:</span>
                                <span>{theater.capacity} người</span>
                            </div>
                        </div>
                        <div className="bg-zinc-900/50 px-6 py-3 border-t border-white/5 flex justify-between items-center">
                            <span className="text-xs text-gray-500">
                                Phòng chiếu cần quản lý từ chi tiết
                            </span>
                            <button className="text-pink-500 hover:text-pink-400 text-sm font-medium">
                                Sửa
                            </button>
                        </div>
                    </div>
                ))}
                {theaters.length === 0 && !loading && (
                    <div className="col-span-full text-center py-12 bg-[#1a1a1a] rounded-xl border border-white/10">
                        <p className="text-gray-500">Chưa có rạp chiếu nào.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminTheaters;
