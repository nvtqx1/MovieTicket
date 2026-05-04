import React, { useEffect, useMemo, useState } from "react";
import {
    getAgeGroupStats,
    getGenderStats,
    getMovieRevenue,
    getTheaterRevenue,
} from "../services/api/dashboardService";

// ==========================================
// UTILS
// ==========================================
const formatCurrency = (value) =>
    new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
        notation: "compact",
    }).format(value);

const toNumber = (value) => Number(value ?? 0);

// ==========================================
// MAIN COMPONENT
// ==========================================
export default function RevenueAnalytics() {
    const [theaterStats, setTheaterStats] = useState([]);
    const [movieStats, setMovieStats] = useState([]);
    const [genderStats, setGenderStats] = useState([]);
    const [ageStats, setAgeStats] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // ==========================================
    // FETCH DATA
    // ==========================================
    useEffect(() => {
        let isMounted = true;

        const fetchAll = async () => {
            try {
                setLoading(true);
                setError(null);

                const [
                    theaterRes,
                    movieRes,
                    genderRes,
                    ageRes,
                ] = await Promise.all([
                    getTheaterRevenue(),
                    getMovieRevenue(),
                    getGenderStats(),
                    getAgeGroupStats(),
                ]);

                if (!isMounted) return;

                setTheaterStats(Array.isArray(theaterRes) ? theaterRes : []);
                setMovieStats(Array.isArray(movieRes) ? movieRes : []);
                setGenderStats(Array.isArray(genderRes) ? genderRes : []);
                setAgeStats(Array.isArray(ageRes) ? ageRes : []);
            } catch (err) {
                if (isMounted) setError(err.message || "Không thể tải dữ liệu thống kê.");
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        fetchAll();

        return () => {
            isMounted = false;
        };
    }, []);

    // ==========================================
    // DERIVED DATA (NO HARD CODE)
    // ==========================================
    const totalRevenue = useMemo(
        () => theaterStats.reduce((sum, t) => sum + toNumber(t.totalRevenue), 0),
        [theaterStats]
    );

    const totalTickets = useMemo(
        () => theaterStats.reduce((sum, t) => sum + toNumber(t.ticketsSold), 0),
        [theaterStats]
    );

    const totalMovies = movieStats.length;

    const genderChartData = useMemo(() => {
        const total = genderStats.reduce((sum, item) => sum + toNumber(item.count), 0);
        return genderStats.map((item) => ({
            ...item,
            percentage: total > 0 ? Math.round((toNumber(item.count) / total) * 100) : 0,
        }));
    }, [genderStats]);

    const maxAgePercent = useMemo(
        () => Math.max(...ageStats.map(a => toNumber(a.percentage)), 0),
        [ageStats]
    );

    // ==========================================
    // STATES UI
    // ==========================================
    if (loading) return (
        <div className="flex items-center justify-center h-96">
            <div className="w-10 h-10 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
        </div>
    );

    if (error) return (
        <div className="flex items-center justify-center h-96">
            <p className="text-red-500">{error}</p>
        </div>
    );

    // ==========================================
    // RENDER
    // ==========================================
    return (
        <div className="p-6 md:p-10 text-white font-sans">

            {/* HEADER */}
            <header className="mb-10">
                <h1 className="text-3xl font-black mb-2">Thống kê doanh thu</h1>
                <p className="text-gray-400 text-sm">
                    Tổng quan hiệu suất bán vé và doanh thu
                </p>
            </header>

            {/* SUMMARY */}
            <div className="grid md:grid-cols-3 gap-6 mb-10">
                <SummaryCard title="Tổng doanh thu" value={formatCurrency(totalRevenue)} />
                <SummaryCard title="Vé đã bán" value={totalTickets.toLocaleString()} />
                <SummaryCard title="Phim đang chiếu" value={totalMovies} />
            </div>

            {/* TABLE + MOVIE */}
            <div className="grid lg:grid-cols-2 gap-8 mb-10">
                <TheaterTable data={theaterStats} />
                <MovieList data={movieStats} />
            </div>

            {/* AUDIENCE */}
            <div className="grid lg:grid-cols-2 gap-8">
                <GenderChart data={genderChartData} />
                <AgeChart data={ageStats} max={maxAgePercent} />
            </div>
        </div>
    );
}

// ==========================================
// COMPONENTS
// ==========================================

function SummaryCard({ title, value }) {
    return (
        <div className="bg-[#18181b] p-6 rounded-xl border border-white/5">
            <p className="text-xs text-gray-500 uppercase mb-2">{title}</p>
            <h3 className="text-2xl font-black">{value}</h3>
        </div>
    );
}

function TheaterTable({ data }) {
    return (
        <div className="bg-[#18181b] rounded-xl overflow-hidden">
            <table className="w-full text-sm">
                <thead className="text-gray-500 text-xs bg-black/20">
                    <tr>
                        <th className="px-4 py-3">Rạp</th>
                        <th className="px-4 py-3 text-right">Doanh thu</th>
                        <th className="px-4 py-3 text-right">Vé</th>
                    </tr>
                </thead>
                <tbody>
                    {data.map(t => (
                        <tr key={t.theaterId ?? t.theaterName} className="border-t border-white/5">
                            <td className="px-4 py-3">{t.theaterName}</td>
                            <td className="px-4 py-3 text-right text-yellow-500">
                                {formatCurrency(toNumber(t.totalRevenue))}
                            </td>
                            <td className="px-4 py-3 text-right">
                                {toNumber(t.ticketsSold).toLocaleString("vi-VN")}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

function MovieList({ data }) {
    return (
        <div className="space-y-4">
            {data.map((m) => (
                <div key={m.movieId ?? m.movieTitle} className="bg-[#18181b] p-4 rounded-xl flex justify-between gap-4">
                    <div>
                        <h3 className="font-bold">{m.movieTitle}</h3>
                        <p className="text-xs text-gray-500">
                            {toNumber(m.showtimeCount).toLocaleString("vi-VN")} suất
                        </p>
                    </div>
                    <span className="font-black">
                        {formatCurrency(toNumber(m.totalRevenue))}
                    </span>
                </div>
            ))}
        </div>
    );
}

function GenderChart({ data }) {
    return (
        <div className="bg-[#18181b] p-8 rounded-xl">
            <p className="text-xs text-gray-500 mb-6">Giới tính</p>

            <div className="space-y-4">
                {data.map((item) => (
                    <div key={item.gender}>
                        <div className="flex justify-between text-sm mb-2">
                            <span>{item.gender}</span>
                            <span className="font-bold">{item.percentage}%</span>
                        </div>
                        <div className="h-2 rounded bg-zinc-800 overflow-hidden">
                            <div
                                className="h-full bg-red-500"
                                style={{ width: `${item.percentage}%` }}
                            />
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

function AgeChart({ data, max }) {
    return (
        <div className="bg-[#18181b] p-8 rounded-xl">
            <p className="text-xs text-gray-500 mb-6">Độ tuổi</p>

            <div className="flex items-end gap-4 h-40">
                {data.map(a => {
                    const height = max > 0 ? (toNumber(a.percentage) / max) * 100 : 0;

                    return (
                        <div key={a.ageGroup} className="flex flex-col items-center flex-1">
                            <div
                                className="w-full bg-zinc-700 rounded"
                                style={{ height: `${height}%` }}
                            />
                            <span className="text-xs mt-2">{a.ageGroup}</span>
                            <span className="text-[10px] text-gray-500">{toNumber(a.percentage).toFixed(0)}%</span>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
