import { useEffect, useState } from "react";
import { getMovies } from "../services/api/movieService";

export const useMovies = (params) => {
    const [movies, setMovies] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const data = await getMovies(params);
                setMovies(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [JSON.stringify(params)]);

    return { movies, loading, error };
};