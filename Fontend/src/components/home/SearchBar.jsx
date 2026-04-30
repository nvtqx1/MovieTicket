import React from "react";

export default function SearchBar() {
    return (
        <section className="-mt-16 px-8">
            <div className="bg-gray-800 p-6 rounded-xl flex gap-4">
                <input
                    className="flex-1 px-4 py-2 rounded bg-gray-700"
                    placeholder="Tìm phim..."
                />

                <select className="px-4 py-2 bg-gray-700 rounded">
                    <option>Tất cả rạp</option>
                </select>

                <button className="px-6 py-2 bg-red-600 rounded text-white">
                    Tìm
                </button>
            </div>
        </section>
    );
}