import React from 'react';
import StatCard from '../components/admin/StatCard';

const activeShows = [
    { id: 1, title: 'SINNERS', price: '$ 200', time: 'Thu, Jan 12 10:00 PM', image: 'https://via.placeholder.com/150x200' },
    { id: 2, title: 'LILO & STITCH', price: '$ 120', time: 'Thu, Jan 12 08:30 PM', image: 'https://via.placeholder.com/150x200' },
    // Thêm các show khác...
];

const AdminDashboard = () => {
    return (
        <div className="p-10">
            <header className="mb-10">
                <h1 className="text-2xl font-black uppercase tracking-[0.1em]">Admin <span className="text-red-600">Dashboard</span></h1>
                <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">Chào mừng trở lại, Quản trị viên TMT.</p>
            </header>

            {/* Thống kê nhanh */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
                <StatCard title="Tổng vé đã đặt" value="3,124" icon="🎟️" color="text-blue-500" />
                <StatCard title="Doanh thu" value="$128,450" icon="💰" color="text-green-500" />
                <StatCard title="Khán giả mới" value="42" icon="📈" color="text-yellow-500" />
                <StatCard title="Phim đang chiếu" value="12" icon="🎥" color="text-red-500" />
            </div>

            {/* Danh sách Active Shows */}
            <section>
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-sm font-black uppercase tracking-widest flex items-center gap-2">
                        <span className="w-1 h-4 bg-red-600 rounded-full"></span> Suất chiếu đang hoạt động
                    </h2>
                    <button className="text-[10px] font-bold text-red-500 hover:underline uppercase tracking-widest">Xem tất cả</button>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-6">
                    {activeShows.map(show => (
                        <div key={show.id} className="bg-[#111] rounded-lg border border-white/5 overflow-hidden hover:border-red-600/30 transition-all cursor-pointer group">
                            <div className="aspect-[3/4] overflow-hidden relative">
                                <img src={show.image} alt={show.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                                <div className="absolute bottom-0 left-0 right-0 p-3 bg-gradient-to-t from-black to-transparent">
                                    <p className="text-[10px] font-black text-white truncate">{show.title}</p>
                                    <p className="text-[9px] text-red-500 font-bold">{show.price}</p>
                                </div>
                            </div>
                            <div className="p-3">
                                <p className="text-[8px] text-gray-500 font-bold uppercase">{show.time}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
};

export default AdminDashboard;
