import React from "react";
import { Search } from "lucide-react";

export default function FilterSidebar({
    search,
    activeFilter,
    onSearchChange,
    onFilterChange,
}) {
    const filters = [
        { value: "all", label: "Tất cả" },
        { value: "nowShowing", label: "Đang chiếu" },
        { value: "comingSoon", label: "Sắp chiếu" },
    ];

    return (
        <div className="bg-[#18181b] p-5 rounded-xl space-y-6">
            <h2 className="text-lg font-bold">Bộ lọc</h2>

            {/* Search */}
            <div className="flex items-center bg-black/40 border border-gray-700 px-3 py-2 rounded gap-2">
                <Search size={14} className="text-gray-500" />
                <input
                    type="text"
                    value={search}
                    onChange={(e) => onSearchChange(e.target.value)}
                    placeholder="Tìm phim..."
                    className="bg-transparent outline-none text-sm w-full"
                />
            </div>

            {/* Filter */}
            <div className="space-y-2 text-sm">
                {filters.map((f) => (
                    <label key={f.value} className="flex items-center gap-2 cursor-pointer">
                        <input
                            type="radio"
                            name="filter"
                            value={f.value}
                            checked={activeFilter === f.value}
                            onChange={() => onFilterChange(f.value)}
                            className="accent-red-600"
                        />
                        {f.label}
                    </label>
                ))}
            </div>
        </div>
    );
}