import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getRoomsByTheater, createRoom, updateRoom, deleteRoom } from '../services/api/theaterService';
import Error from '../components/common/Error';

const AdminRooms = () => {
    const { id: theaterId } = useParams();
    const navigate = useNavigate();
    
    const [rooms, setRooms] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    const [isAdding, setIsAdding] = useState(false);
    const [editingRoom, setEditingRoom] = useState(null);
    const [formData, setFormData] = useState({ name: '', capacity: 100 });

    useEffect(() => {
        fetchRooms();
    }, [theaterId]);

    const fetchRooms = async () => {
        try {
            setLoading(true);
            const data = await getRoomsByTheater(theaterId);
            setRooms(data);
            setError(null);
        } catch (err) {
            setError('Lỗi khi tải danh sách phòng chiếu');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleAdd = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            if (editingRoom) {
                await updateRoom(theaterId, editingRoom.id, formData);
            } else {
                await createRoom(theaterId, formData);
            }
            setFormData({ name: '', capacity: 100 });
            setIsAdding(false);
            setEditingRoom(null);
            await fetchRooms();
        } catch (err) {
            setError(editingRoom ? 'Lỗi khi sửa phòng chiếu' : 'Lỗi khi tạo phòng chiếu');
            setLoading(false);
        }
    };

    const handleEdit = (room) => {
        setEditingRoom(room);
        setFormData({ name: room.name, capacity: room.capacity });
        setIsAdding(true);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const handleDelete = async (roomId, name) => {
        if (!confirm(`Xóa phòng chiếu "${name}"?`)) return;
        try {
            setLoading(true);
            await deleteRoom(theaterId, roomId);
            await fetchRooms();
        } catch (err) {
            setError('Lỗi khi xóa phòng chiếu');
            setLoading(false);
        }
    };

    if (loading && rooms.length === 0) {
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
                    <button 
                        onClick={() => navigate('/admin/theaters')}
                        className="text-gray-500 hover:text-white mb-2 text-sm"
                    >
                        &larr; Quay lại danh sách Rạp
                    </button>
                    <h1 className="text-2xl font-black uppercase tracking-[0.1em]">
                        Quản lý <span className="text-red-600">Phòng Chiếu</span>
                    </h1>
                    <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">
                        Danh sách phòng chiếu thuộc rạp {theaterId}
                    </p>
                </div>
                <button 
                    onClick={() => {
                        setIsAdding(!isAdding);
                        if (isAdding) setEditingRoom(null);
                        setFormData({ name: '', capacity: 100 });
                    }}
                    className="px-6 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors text-sm font-bold uppercase tracking-widest"
                >
                    {isAdding ? 'Hủy' : '+ Thêm Phòng'}
                </button>
            </header>

            {error && <Error message={error} />}

            {isAdding && (
                <div className="bg-[#111] p-6 rounded-xl border border-white/5">
                    <h2 className="text-sm font-black uppercase tracking-widest text-white mb-6">
                        {editingRoom ? 'Sửa Phòng Chiếu' : 'Thêm Phòng Mới'}
                    </h2>
                    <form onSubmit={handleAdd} className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-[10px] font-bold text-gray-500 mb-1 uppercase tracking-widest">Tên Phòng</label>
                                <input 
                                    type="text" 
                                    required
                                    value={formData.name}
                                    onChange={e => setFormData({...formData, name: e.target.value})}
                                    className="w-full bg-black border border-white/10 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-red-600 transition-colors"
                                    placeholder="VD: IMAX 01"
                                />
                            </div>
                            <div>
                                <label className="block text-[10px] font-bold text-gray-500 mb-1 uppercase tracking-widest">Sức chứa dự kiến</label>
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
                                {editingRoom ? 'Cập nhật' : 'Lưu Phòng'}
                            </button>
                        </div>
                    </form>
                </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {rooms.map(room => (
                    <div key={room.id} className="bg-[#111] rounded-xl border border-white/5 overflow-hidden hover:border-red-600/30 transition-colors flex flex-col">
                        <div className="p-6 flex-1">
                            <div className="flex justify-between items-start mb-4">
                                <div>
                                    <h3 className="text-lg font-black text-white mb-1 uppercase">{room.name}</h3>
                                </div>
                                <span className="px-3 py-1 bg-red-600/10 text-red-500 rounded text-[10px] font-black uppercase tracking-widest border border-red-600/20">
                                    ID: {room.id}
                                </span>
                            </div>
                            
                            <div className="flex items-center text-sm text-gray-300 mt-6">
                                <span className="font-medium mr-2 text-gray-500">Sức chứa:</span>
                                <span>{room.capacity} ghế</span>
                            </div>
                        </div>
                        <div className="bg-black/50 px-6 py-3 border-t border-white/5 flex justify-between items-center">
                            <span className="text-[10px] text-gray-500 uppercase tracking-widest">
                                Quản lý
                            </span>
                            <div className="space-x-3">
                                <button 
                                    onClick={() => navigate(`/admin/rooms/${room.id}/seats`)}
                                    className="text-red-500 hover:text-red-400 text-sm font-bold uppercase"
                                >
                                    Cấu hình ghế
                                </button>
                                <button 
                                    onClick={() => handleEdit(room)}
                                    className="text-gray-500 hover:text-white text-sm font-bold uppercase"
                                >
                                    Sửa
                                </button>
                                <button 
                                    onClick={() => handleDelete(room.id, room.name)}
                                    className="text-gray-500 hover:text-red-500 text-sm font-bold uppercase"
                                >
                                    Xóa
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
                {rooms.length === 0 && !loading && (
                    <div className="col-span-full text-center py-12 bg-[#1a1a1a] rounded-xl border border-white/10">
                        <p className="text-gray-500">Chưa có phòng chiếu nào.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminRooms;
