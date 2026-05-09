import { useState } from "react";
import { generateSeatMatrix } from "../services/api/seatService";

export const useSeatGenerator = () => {
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);

    const generate = async (payload) => {
        try {
            setLoading(true);
            setError(null);

            const res = await generateSeatMatrix(payload);
            setResult(res);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return { generate, loading, result, error };
};