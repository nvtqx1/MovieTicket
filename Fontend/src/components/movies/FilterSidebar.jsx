import React from "react";

export default function FilterSidebar() {
    return (
        <div className="bg-[#18181b] p-5 rounded-xl space-y-6">
            <h2 className="text-lg font-bold">Bộ lọc</h2>

            <input
                type="text"
                placeholder="Tìm phim..."
                className="w-full bg-black/40 border border-gray-700 px-3 py-2 rounded text-sm"
            />

            <div className="space-y-2 text-sm">
                <label className="flex gap-2">
                    <input type="checkbox" /> Đang chiếu
                </label>
                <label className="flex gap-2">
                    <input type="checkbox" /> Sắp chiếu
                </label>
            </div>
        </div>
    );
}