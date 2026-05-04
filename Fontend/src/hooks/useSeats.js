import { useEffect, useState } from "react";
import { getSeatsByShowtime } from "../services/api/seatService";

export const useSeats = (showtimeId) => {
    const [seats, setSeats] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchSeats = async () => {
        try {
            setLoading(true);
            const data = await getSeatsByShowtime(showtimeId);
            setSeats(data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (showtimeId) fetchSeats();
    }, [showtimeId]);

    return { seats, loading, refetch: fetchSeats };
};