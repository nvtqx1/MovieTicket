import React from "react";

export default function Hero() {
    return (
        <section className="relative h-screen flex items-end pb-32">
            <img
                className="absolute inset-0 w-full h-full object-cover"
                src="https://revuecinema.ca/wp-content/uploads/2026/04/true_stories_tmdb-sngf8cskfwz2cig7b63zadnaqxr.jpg"
            />

            <div className="relative z-10 px-8 max-w-5xl">
                <h1 className="text-7xl font-black text-white">
                    NEON <br /> DREAMS
                </h1>

                <p className="text-gray-300 mt-4 max-w-xl">
                    Trong một thế giới nơi ký ức được trao đổi như tiền tệ...
                </p>

                <div className="flex gap-4 mt-6">
                    <button className="px-6 py-3 bg-red-600 text-white rounded">
                        XEM TRAILER
                    </button>
                    <button className="px-6 py-3 border border-white text-white rounded">
                        ĐẶT VÉ
                    </button>
                </div>
            </div>
        </section>
    );
}