import React from "react";

export default function VipPromo() {
    return (
        <section className="mt-20 px-8 grid md:grid-cols-2 gap-10">
            <div>
                <h3 className="text-3xl font-bold">VIP CINEMATIC</h3>
                <p className="mt-4 text-gray-400">
                    Nhận ưu đãi độc quyền và trải nghiệm IMAX.
                </p>

                <button className="mt-6 px-6 py-3 bg-yellow-500 rounded">
                    Nâng cấp VIP
                </button>
            </div>

            <img
                className="rounded-xl"
                src="https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c"
            />
        </section>
    );
}