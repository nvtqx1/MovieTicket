import React, { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import StatCard from '../components/admin/StatCard';
import { getDashboardSummary, getDailyRevenue } from '../services/api/dashboardService';

const AdminDashboard = () => {
    const [summary, setSummary] = useState(null);
    const [dailyRevenue, setDailyRevenue] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);

                const [summaryData, revenueData] = await Promise.all([
                    getDashboardSummary(),
                    getDailyRevenue(),
                ]);

                setSummary(summaryData);
                setDailyRevenue(Array.isArray(revenueData) ? revenueData : []);
            } catch (err) {
                setError(err.message || 'Lỗi khi tải dữ liệu');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    // Format currency
    const formatCurrency = (value) =>
        new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            notation: 'compact',
        }).format(value ?? 0);

    if (loading) {
        return (
            <div className="flex items-center justify-center h-96 text-white">
                <div className="w-10 h-10 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center h-96 text-red-500">
                {error}
            </div>
        );
    }

    return (
        <div className="p-10">
            <header className="mb-10">
                <h1 className="text-2xl font-black uppercase tracking-[0.1em]">Admin <span className="text-red-600">Dashboard</span></h1>
                <p className="text-[10px] text-gray-500 mt-2 uppercase tracking-widest">Chào mừng trở lại, Quản trị viên TMT.</p>
            </header>

            {/* Thống kê nhanh từ API */}
            {summary && (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
                    <StatCard
                        title="Tổng vé đã đặt"
                        value={summary.totalTicketsSold?.toLocaleString() || '0'}
                        icon="🎟️"
                        color="text-blue-500"
                    />
                    <StatCard
                        title="Doanh thu"
                        value={formatCurrency(summary.totalRevenue)}
                        icon="💰"
                        color="text-green-500"
                    />
                    <StatCard
                        title="Doanh thu hôm nay"
                        value={formatCurrency(summary.todayRevenue)}
                        icon="📊"
                        color="text-yellow-500"
                    />
                    <StatCard
                        title="Phim đang chiếu"
                        value={summary.totalMovies?.toString() || '0'}
                        icon="🎥"
                        color="text-red-500"
                    />
                </div>
            )}

            {/* Biểu đồ doanh thu 7 ngày */}
            <section className="bg-[#111] border border-white/5 rounded-lg p-6 mb-12">
                <h2 className="text-sm font-black uppercase tracking-widest mb-6 flex items-center gap-2">
                    <span className="w-1 h-4 bg-red-600 rounded-full"></span> Doanh thu 7 ngày gần nhất
                </h2>

                {dailyRevenue.length > 0 ? (
                    <ResponsiveContainer width="100%" height={300}>
                        <LineChart data={dailyRevenue}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#333" />
                            <XAxis
                                dataKey="date"
                                stroke="#999"
                                style={{ fontSize: '12px' }}
                            />
                            <YAxis
                                stroke="#999"
                                style={{ fontSize: '12px' }}
                                tickFormatter={(value) => formatCurrency(value)}
                            />
                            <Tooltip
                                formatter={(value) => formatCurrency(value)}
                                contentStyle={{ backgroundColor: '#222', border: '1px solid #666' }}
                                labelStyle={{ color: '#fff' }}
                            />
                            <Legend />
                            <Line
                                type="monotone"
                                dataKey="totalRevenue"
                                stroke="#dc2626"
                                strokeWidth={2}
                                dot={{ fill: '#dc2626' }}
                                name="Doanh thu"
                            />
                        </LineChart>
                    </ResponsiveContainer>
                ) : (
                    <p className="text-gray-400">Không có dữ liệu doanh thu</p>
                )}
            </section>

            {/* Thông tin tóm tắt */}
            {summary && (
                <section className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="bg-[#111] border border-white/5 rounded-lg p-6">
                        <h3 className="text-sm font-black uppercase tracking-widest mb-4 flex items-center gap-2">
                            <span className="w-1 h-4 bg-red-600 rounded-full"></span> Thống kê chung
                        </h3>
                        <div className="space-y-3 text-sm text-gray-300">
                            <div className="flex justify-between">
                                <span>Tổng rạp:</span>
                                <span className="font-bold text-white">{summary.totalTheaters || 0}</span>
                            </div>
                            <div className="flex justify-between">
                                <span>Tổng lịch chiếu:</span>
                                <span className="font-bold text-white">{summary.totalShowtimes || 0}</span>
                            </div>
                            <div className="flex justify-between">
                                <span>Tổng đơn đặt:</span>
                                <span className="font-bold text-white">{summary.totalReservations?.toLocaleString() || 0}</span>
                            </div>
                            <div className="flex justify-between">
                                <span>Tổng người dùng:</span>
                                <span className="font-bold text-white">{summary.totalUsers?.toLocaleString() || 0}</span>
                            </div>
                        </div>
                    </div>

                    <div className="bg-[#111] border border-white/5 rounded-lg p-6">
                        <h3 className="text-sm font-black uppercase tracking-widest mb-4 flex items-center gap-2">
                            <span className="w-1 h-4 bg-red-600 rounded-full"></span> Hôm nay
                        </h3>
                        <div className="space-y-3 text-sm text-gray-300">
                            <div className="flex justify-between">
                                <span>Doanh thu:</span>
                                <span className="font-bold text-green-400">{formatCurrency(summary.todayRevenue)}</span>
                            </div>
                            <div className="flex justify-between">
                                <span>Đơn đặt:</span>
                                <span className="font-bold text-blue-400">{summary.todayReservations?.toLocaleString() || 0}</span>
                            </div>
                        </div>
                    </div>
                </section>
            )}
        </div>
    );
};

export default AdminDashboard;
