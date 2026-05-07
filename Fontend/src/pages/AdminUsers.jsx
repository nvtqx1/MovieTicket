import { useState, useEffect } from 'react';
import { userService } from '../services/api/userService';
import Error from '../components/common/Error';

const AdminUsers = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            const data = await userService.getAllUsers();
            setUsers(data);
            setError(null);
        } catch (err) {
            setError('Lỗi khi tải danh sách người dùng');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleToggleBan = async (id, userName, currentlyBanned) => {
        const action = currentlyBanned ? 'mở khóa' : 'khóa';
        if (!confirm(`Bạn có chắc muốn ${action} tài khoản "${userName}"?`)) return;
        try {
            setLoading(true);
            await userService.toggleBanUser(id);
            await fetchUsers();
        } catch (err) {
            setError(err.message || `Lỗi khi ${action} người dùng`);
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
            </div>
        );
    }

    if (error) return <Error message={error} />;

    return (
        <div className="p-10 text-white space-y-6">
            <header className="mb-10 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-black uppercase tracking-[0.1em]">Quản lý <span className="text-red-600">Người Dùng</span></h1>
                    <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">Danh sách tài khoản hệ thống.</p>
                </div>
                <div className="flex gap-3">
                    <div className="bg-zinc-900 px-4 py-2 rounded-lg border border-white/10 text-gray-400 text-sm font-medium">
                        Tổng số: <span className="text-red-500 font-bold">{users.length}</span>
                    </div>
                    <div className="bg-zinc-900 px-4 py-2 rounded-lg border border-white/10 text-gray-400 text-sm font-medium">
                        Bị khóa: <span className="text-orange-500 font-bold">{users.filter(u => u.isBanned).length}</span>
                    </div>
                </div>
            </header>

            <div className="bg-[#111] rounded-xl border border-white/5 overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm text-gray-400">
                        <thead className="text-xs text-gray-500 uppercase bg-zinc-900/50 border-b border-white/10">
                            <tr>
                                <th className="px-6 py-4">ID</th>
                                <th className="px-6 py-4">Tên đăng nhập</th>
                                <th className="px-6 py-4">Email</th>
                                <th className="px-6 py-4">SĐT</th>
                                <th className="px-6 py-4">Ngày sinh</th>
                                <th className="px-6 py-4">Giới tính</th>
                                <th className="px-6 py-4">Vai trò</th>
                                <th className="px-6 py-4">Trạng thái</th>
                                <th className="px-6 py-4 text-right">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id} className={`border-b border-white/5 hover:bg-white/5 transition-colors ${user.isBanned ? 'opacity-60' : ''}`}>
                                    <td className="px-6 py-4 text-white">#{user.id}</td>
                                    <td className="px-6 py-4 font-medium text-white">
                                        {user.userName}
                                        {user.isBanned && <span className="ml-2 text-[10px] text-red-400">🔒</span>}
                                    </td>
                                    <td className="px-6 py-4">{user.email}</td>
                                    <td className="px-6 py-4">{user.phoneNumber || '-'}</td>
                                    <td className="px-6 py-4">{user.dateOfBirth || '-'}</td>
                                    <td className="px-6 py-4">
                                        {user.gender === 'MALE' ? 'Nam' : user.gender === 'FEMALE' ? 'Nữ' : 'Khác'}
                                    </td>
                                    <td className="px-6 py-4">
                                        <span className={`px-2 py-1 rounded text-xs font-bold tracking-widest uppercase ${
                                            user.role === 'ROLE_ADMIN' 
                                            ? 'bg-red-600/10 text-red-500' 
                                            : 'bg-zinc-800 text-gray-400'
                                        }`}>
                                            {user.role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4">
                                        <span className={`px-2 py-1 rounded text-xs font-bold ${
                                            user.isBanned 
                                            ? 'bg-red-500/20 text-red-400' 
                                            : 'bg-green-500/20 text-green-400'
                                        }`}>
                                            {user.isBanned ? 'Đã khóa' : 'Hoạt động'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <button 
                                            onClick={() => handleToggleBan(user.id, user.userName, user.isBanned)}
                                            className={`font-bold tracking-widest uppercase text-xs ${
                                                user.isBanned
                                                ? 'text-green-500 hover:text-green-400'
                                                : 'text-orange-500 hover:text-orange-400'
                                            }`}
                                        >
                                            {user.isBanned ? 'Mở khóa' : 'Khóa'}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {users.length === 0 && (
                                <tr>
                                    <td colSpan="9" className="px-6 py-8 text-center text-gray-500">
                                        Không có dữ liệu người dùng
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AdminUsers;
