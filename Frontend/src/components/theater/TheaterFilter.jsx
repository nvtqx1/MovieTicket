import React from "react";

export default function TheaterFilter({ cities, value, onChange }) {
    return (
        <select
            value={value}
            onChange={(e) => onChange(e.target.value)}
            className="bg-[#18181b] border border-zinc-800 px-4 py-2 rounded"
        >
            {cities.map((c) => (
                <option key={c}>{c}</option>
            ))}
        </select>
    );
}