import { useEffect, useState } from "react";
import { getMovies } from "../services/api/movieService";

export const useMovies = (params = {}) => {
    const [movies, setMovies] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Key ổn định để so sánh theo giá trị
    const paramsKey = JSON.stringify(params);

    useEffect(() => {
        let isMounted = true;

        const fetchData = async () => {
            try {
                setLoading(true);
                const data = await getMovies(params);

                if (!isMounted) return;

                setMovies(data);
                setError(null);
            } catch (err) {
                if (!isMounted) return;
                setError(err.message);
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        fetchData();

        return () => {
            isMounted = false;
        };
    }, [paramsKey]); // chỉ chạy lại khi giá trị params đổi

    return { movies, loading, error };
};