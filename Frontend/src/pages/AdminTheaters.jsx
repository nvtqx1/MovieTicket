import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTheaters, createTheater, deleteTheater, updateTheater } from '../services/api/theaterService';
import Error from '../components/common/Error';

const AdminTheaters = () => {
    const navigate = useNavigate();
    const [theaters, setTheaters] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    const [isAdding, setIsAdding] = useState(false);
    const [editingTheater, setEditingTheater] = useState(null);
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
            if (editingTheater) {
                await updateTheater(editingTheater.id, formData);
            } else {
                await createTheater(formData);
            }
            setFormData({ name: '', location: '', capacity: 100 });
            setIsAdding(false);
            setEditingTheater(null);
            await fetchTheaters();
        } catch (err) {
            setError(editingTheater ? 'Lỗi khi sửa rạp chiếu' : 'Lỗi khi tạo rạp chiếu');
            setLoading(false);
        }
    };

    const handleEdit = (theater) => {
        setEditingTheater(theater);
        setFormData({ name: theater.name, location: theater.location, capacity: theater.capacity });
        setIsAdding(true);
        // Cuộn lên trên
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const handleDelete = async (id, name) => {
        if (!confirm(`Xóa rạp chiếu "${name}"? Thao tác này có thể lỗi nếu rạp đang chứa dữ liệu.`)) return;
        try {
            setLoading(true);
            await deleteTheater(id);
            await fetchTheaters();
        } catch (err) {
            setError('Lỗi khi xóa rạp chiếu');
            setLoading(false);
        }
    };

    if (loading && theaters.length === 0) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
            </div>
        );
    }

    return (
        <div className="p-10 text-white space-y-6">
            <header className="mb-10 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-black uppercase tracking-[0.1em]">Quản lý <span className="text-red-600">Rạp Chiếu</span></h1>
                    <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">Danh sách các cụm rạp TMT.</p>
                </div>
                <button 
                    onClick={() => {
                        setIsAdding(!isAdding);
                        if (isAdding) setEditingTheater(null);
                        setFormData({ name: '', location: '', capacity: 100 });
                    }}
                    className="px-6 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors text-sm font-bold uppercase tracking-widest"
                >
                    {isAdding ? 'Hủy' : '+ Thêm Rạp'}
                </button>
            </header>

            {error && <Error message={error} />}

            {isAdding && (
                <div className="bg-[#111] p-6 rounded-xl border border-white/5">
                    <h2 className="text-sm font-black uppercase tracking-widest text-white mb-6">
                        {editingTheater ? 'Sửa Rạp Chiếu' : 'Thêm Rạp Mới'}
                    </h2>
                    <form onSubmit={handleAdd} className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-[10px] font-bold text-gray-500 mb-1 uppercase tracking-widest">Tên Rạp</label>
                                <input 
                                    type="text" 
                                    required
                                    value={formData.name}
                                    onChange={e => setFormData({...formData, name: e.target.value})}
                                    className="w-full bg-black border border-white/10 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-red-600 transition-colors"
                                    placeholder="VD: BHD Star The Garden"
                                />
                            </div>
                            <div>
                                <label className="block text-[10px] font-bold text-gray-500 mb-1 uppercase tracking-widest">Vị trí</label>
                                <input 
                                    type="text" 
                                    required
                                    value={formData.location}
                                    onChange={e => setFormData({...formData, location: e.target.value})}
                                    className="w-full bg-black border border-white/10 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-red-600 transition-colors"
                                    placeholder="VD: Tầng 4, TTTM The Garden..."
                                />
                            </div>
                            <div>
                                <label className="block text-[10px] font-bold text-gray-500 mb-1 uppercase tracking-widest">Sức chứa (Người)</label>
                                <input 
                                    type="number" 
                                    required
                                    min="1"
                                    value={formData.capacity}
                                    onChange={e => setFormData({...formData, capacity: e.target.value})}
                                    className="w-full bg-black border border-white/10 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-red-600 transition-colors"
                                />
                            </div>
                        </div>
                        <div className="flex justify-end pt-4">
                            <button 
                                type="submit"
                                disabled={loading}
                                className="px-6 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors text-sm font-bold uppercase tracking-widest disabled:opacity-50"
                            >
                                {editingTheater ? 'Cập nhật' : 'Lưu Rạp'}
                            </button>
                        </div>
                    </form>
                </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {theaters.map(theater => (
                    <div key={theater.id} className="bg-[#111] rounded-xl border border-white/5 overflow-hidden hover:border-red-600/30 transition-colors flex flex-col">
                        <div className="p-6 flex-1">
                            <div className="flex justify-between items-start mb-4">
                                <div>
                                    <h3 className="text-lg font-black text-white mb-1 uppercase">{theater.name}</h3>
                                    <p className="text-xs text-gray-400 line-clamp-2">{theater.location}</p>
                                </div>
                                <span className="px-3 py-1 bg-red-600/10 text-red-500 rounded text-[10px] font-black uppercase tracking-widest border border-red-600/20">
                                    ID: {theater.id}
                                </span>
                            </div>
                            
                            <div className="flex items-center text-sm text-gray-300 mt-6">
                                <span className="font-medium mr-2 text-gray-500">Sức chứa:</span>
                                <span>{theater.capacity} người</span>
                            </div>
                        </div>
                        <div className="bg-black/50 px-6 py-3 border-t border-white/5 flex justify-between items-center">
                            <span className="text-[10px] text-gray-500 uppercase tracking-widest">
                                Quản lý phòng chiếu
                            </span>
                            <div className="space-x-3">
                                <button 
                                    onClick={() => navigate(`/admin/theaters/${theater.id}/rooms`)}
                                    className="text-red-500 hover:text-red-400 text-sm font-bold uppercase"
                                >
                                    Phòng chiếu
                                </button>
                                <button 
                                    onClick={() => handleEdit(theater)}
                                    className="text-gray-500 hover:text-white text-sm font-bold uppercase"
                                >
                                    Sửa
                                </button>
                                <button 
                                    onClick={() => handleDelete(theater.id, theater.name)}
                                    className="text-gray-500 hover:text-red-500 text-sm font-bold uppercase"
                                >
                                    Xóa
                                </button>
                            </div>
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
