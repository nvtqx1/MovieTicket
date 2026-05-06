import { useEffect, useState } from "react";
import {
    getAgeGroupStats,
    getGenderStats,
    getMovieRevenue,
    getTheaterRevenue,
} from "../services/api/dashboardService";

export const useRevenueAnalytics = () => {
    const [data, setData] = useState({
        theaterStats: [],
        movieStats: [],
        genderStats: [],
        ageStats: [],
    });

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

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

                setData({
                    theaterStats: Array.isArray(theaterRes) ? theaterRes : [],
                    movieStats: Array.isArray(movieRes) ? movieRes : [],
                    genderStats: Array.isArray(genderRes) ? genderRes : [],
                    ageStats: Array.isArray(ageRes) ? ageRes : [],
                });
            } catch (err) {
                if (isMounted) {
                    setError(err.message || "Không thể tải dữ liệu thống kê.");
                }
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        fetchAll();

        return () => {
            isMounted = false;
        };
    }, []);

    return { ...data, loading, error };
};
