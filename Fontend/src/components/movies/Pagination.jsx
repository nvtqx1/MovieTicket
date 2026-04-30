import React from "react";

export default function Pagination() {
    return (
        <div className="flex justify-center gap-2 mt-10">
            <button className="px-3 py-1 bg-red-600 rounded">1</button>
            <button className="px-3 py-1 bg-gray-800">2</button>
            <button className="px-3 py-1 bg-gray-800">3</button>
        </div>
    );
}