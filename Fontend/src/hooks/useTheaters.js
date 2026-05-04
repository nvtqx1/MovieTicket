import { useEffect, useState } from "react";
import { theaterService } from "../services/api/theaterService";
import { getUniqueCities, filterByCity } from "../utils/theaterUtils";

export default function useTheaters() {
    const [theaters, setTheaters] = useState([]);
    const [loading, setLoading] = useState(true);
    const [city, setCity] = useState("Tất cả khu vực");

    useEffect(() => {
        const fetch = async () => {
            const data = await theaterService.getAll();
            setTheaters(data);
            setLoading(false);
        };
        fetch();
    }, []);

    const cities = getUniqueCities(theaters);
    const filtered = filterByCity(theaters, city);

    return {
        theaters: filtered,
        loading,
        city,
        setCity,
        cities,
    };
}