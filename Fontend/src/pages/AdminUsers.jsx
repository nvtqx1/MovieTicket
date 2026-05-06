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

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-pink-500"></div>
            </div>
        );
    }

    if (error) return <Error message={error} />;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-white">Quản lý Người Dùng</h1>
                <div className="bg-zinc-900 px-4 py-2 rounded-lg border border-white/10 text-gray-400">
                    Tổng số: <span className="text-white font-bold">{users.length}</span>
                </div>
            </div>

            <div className="bg-[#1a1a1a] rounded-xl border border-white/10 overflow-hidden">
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
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                    <td className="px-6 py-4 text-white">#{user.id}</td>
                                    <td className="px-6 py-4 font-medium text-white">{user.userName}</td>
                                    <td className="px-6 py-4">{user.email}</td>
                                    <td className="px-6 py-4">{user.phoneNumber || '-'}</td>
                                    <td className="px-6 py-4">{user.dateOfBirth || '-'}</td>
                                    <td className="px-6 py-4">
                                        {user.gender === 'MALE' ? 'Nam' : user.gender === 'FEMALE' ? 'Nữ' : 'Khác'}
                                    </td>
                                    <td className="px-6 py-4">
                                        <span className={`px-2 py-1 rounded text-xs font-medium ${
                                            user.role === 'ROLE_ADMIN' 
                                            ? 'bg-pink-500/20 text-pink-500' 
                                            : 'bg-zinc-800 text-gray-300'
                                        }`}>
                                            {user.role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER'}
                                        </span>
                                    </td>
                                </tr>
                            ))}
                            {users.length === 0 && (
                                <tr>
                                    <td colSpan="7" className="px-6 py-8 text-center text-gray-500">
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
