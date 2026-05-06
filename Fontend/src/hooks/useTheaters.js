import { useEffect, useState } from "react";
import { theaterService } from "../services/api/theaterService";
import { getUniqueCities, filterByCity } from "../utils/theaterUtils";

export default function useTheaters() {
    const [theaters, setTheaters] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [city, setCity] = useState("Tất cả khu vực");

    useEffect(() => {
        let isMounted = true;

        const fetchTheaters = async () => {
            try {
                const data = await theaterService.getAll();

                if (!isMounted) return;
                setTheaters(data);
            } catch (err) {
                if (!isMounted) return;
                console.error("Error fetching theaters:", err);
                setError(err.message || "Không thể tải danh sách rạp");
            } finally {
                if (!isMounted) return;
                setLoading(false);
            }
        };

        fetchTheaters();

        return () => {
            isMounted = false;
        };
    }, []);

    const cities = getUniqueCities(theaters);
    const filtered = filterByCity(theaters, city);

    return {
        theaters: filtered,
        loading,
        error,
        city,
        setCity,
        cities,
    };
}