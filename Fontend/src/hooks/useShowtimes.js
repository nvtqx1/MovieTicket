import { useEffect, useState } from "react";
import { getShowtimes } from "../services/api/showtimeService";

export const useShowtimes = () => {
    const [showtimes, setShowtimes] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let mounted = true;

        getShowtimes().then(data => {
            if (mounted) {
                setShowtimes(data);
                setLoading(false);
            }
        });

        return () => { mounted = false; };
    }, []);

    return { showtimes, loading };
};