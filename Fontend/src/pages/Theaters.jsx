import React from "react";
import { useNavigate } from "react-router-dom";
import useTheaters from "../hooks/useTheaters";
import TheaterCard from "../components/theater/TheaterCard";
import TheaterFilter from "../components/theater/TheaterFilter";

export default function Theaters() {
    const navigate = useNavigate();
    const { theaters, loading, city, setCity, cities } = useTheaters();

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-black text-white">
                Loading...
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-black text-white p-6">

            {/* HEADER */}
            <div className="flex justify-between mb-8">
                <h1 className="text-2xl font-bold">Rạp phim</h1>
                <TheaterFilter
                    cities={cities}
                    value={city}
                    onChange={setCity}
                />
            </div>

            {/* GRID */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {theaters.map((t) => (
                    <TheaterCard
                        key={t.id}
                        theater={t}
                        onDetail={() => navigate(`/theaters/${t.id}`)}
                        onShowtime={() => navigate(`/movies?theaterId=${t.id}`)}
                    />
                ))}
            </div>

            {theaters.length === 0 && (
                <p className="text-center mt-10 text-gray-500">
                    Không có rạp nào
                </p>
            )}
        </div>
    );
}