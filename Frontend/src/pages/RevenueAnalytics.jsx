import React, { useMemo } from "react";
import { useRevenueAnalytics } from "../hooks/useRevenueAnalytics";
import { toNumber } from "../utils/number";

// Components
import Loading from "../components/common/Loading";
import Error from "../components/common/Error";
import RevenueHeader from "../components/admin/analytics/RevenueHeader";
import RevenueSummary from "../components/admin/analytics/RevenueSummary";
import TheaterTable from "../components/admin/analytics/TheaterTable";
import MovieList from "../components/admin/analytics/MovieList";
import GenderChart from "../components/admin/analytics/GenderChart";
import AgeChart from "../components/admin/analytics/AgeChart";

export default function RevenueAnalytics() {
    const {
        theaterStats,
        movieStats,
        genderStats,
        ageStats,
        loading,
        error,
    } = useRevenueAnalytics();

    // ===== DERIVED DATA =====
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

    // Top 3 Rạp Doanh Thu Cao Nhất
    const top3Theaters = useMemo(() => {
        return [...theaterStats]
            .sort((a, b) => toNumber(b.totalRevenue) - toNumber(a.totalRevenue))
            .slice(0, 3);
    }, [theaterStats]);

    // Top 10 Phim Doanh Thu Cao Nhất
    const top10Movies = useMemo(() => {
        return [...movieStats]
            .sort((a, b) => toNumber(b.totalRevenue) - toNumber(a.totalRevenue))
            .slice(0, 10);
    }, [movieStats]);

    const maxAgePercent = useMemo(
        () => Math.max(...ageStats.map(a => toNumber(a.percentage)), 0),
        [ageStats]
    );

    // ===== UI STATES =====
    if (loading) return <Loading />;
    if (error) return <Error message={error} />;



    return (
        <div className="p-6 md:p-10 text-white font-sans">
            <RevenueHeader />

            <RevenueSummary
                totalRevenue={totalRevenue}
                totalTickets={totalTickets}
                totalMovies={totalMovies}
            />

            <div className="grid lg:grid-cols-2 gap-8 mb-10">
                <TheaterTable data={top3Theaters} />
                <MovieList data={top10Movies} />
            </div>

            <div className="grid lg:grid-cols-2 gap-8">
                <GenderChart data={genderChartData} />
                <AgeChart data={ageStats} max={maxAgePercent} />
            </div>
        </div>
    );
}
