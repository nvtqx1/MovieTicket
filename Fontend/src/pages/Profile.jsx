import React, { useEffect, useState } from "react";

// MOCK DATA
const MOCK_USER = {
    id: 1,
    userName: "john",
    email: "john@example.com",
    phoneNumber: "0123456789",
    roles: ["USER"],
    avatarUrl:
        "https://images.unsplash.com/photo-1614113489855-66422ad300a4?auto=format&fit=crop&w=400&q=80",
};

export default function Profile() {
    const [user, setUser] = useState(null);
    const [form, setForm] = useState({});
    const [loading, setLoading] = useState(true);

    // Fetch user
    useEffect(() => {
        const fetchUser = async () => {
            // TODO: replace with real API
            setTimeout(() => {
                setUser(MOCK_USER);
                setForm({
                    userName: MOCK_USER.userName,
                    email: MOCK_USER.email,
                    phoneNumber: MOCK_USER.phoneNumber,
                });
                setLoading(false);
            }, 400);
        };

        fetchUser();
    }, []);

    const handleChange = (e) =>
        setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            // TODO: call PUT /v1/auth/me
            console.log("Update:", form);
        } finally {
            setLoading(false);
        }
    };

    if (loading && !user) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-[#0a0a0a] text-white">
                Loading...
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#0a0a0a] text-white flex items-center justify-center p-6 mt-20">
            <div className="w-full max-w-4xl bg-[#18181b] border border-zinc-800 rounded-2xl p-8 md:p-10 flex flex-col md:flex-row gap-10">

                {/* LEFT */}
                <div className="md:w-1/3 flex flex-col items-center text-center">
                    <div className="w-40 h-40 rounded-xl overflow-hidden border border-zinc-700 mb-5">
                        <img
                            src={user.avatarUrl}
                            alt="avatar"
                            className="w-full h-full object-cover"
                        />
                    </div>

                    <div className="space-y-2">
                        <p className="text-xs text-gray-500 uppercase">Role</p>
                        {user.roles.map((r) => (
                            <span
                                key={r}
                                className="px-3 py-1 text-xs font-bold bg-yellow-500/10 text-yellow-500 border border-yellow-500/20 rounded"
                            >
                                {r}
                            </span>
                        ))}
                    </div>
                </div>

                {/* RIGHT */}
                <form onSubmit={handleSubmit} className="flex-1 space-y-6">
                    <div>
                        <h1 className="text-2xl font-black">Hồ sơ cá nhân</h1>
                        <p className="text-sm text-gray-400">
                            Cập nhật thông tin của bạn
                        </p>
                    </div>

                    <Input
                        label="Tên người dùng"
                        name="userName"
                        value={form.userName}
                        onChange={handleChange}
                    />

                    <Input
                        label="Email"
                        name="email"
                        value={form.email}
                        disabled
                    />

                    <Input
                        label="Số điện thoại"
                        name="phoneNumber"
                        value={form.phoneNumber}
                        onChange={handleChange}
                    />

                    <div className="flex justify-end gap-4 pt-4 border-t border-zinc-800">
                        <button
                            type="button"
                            className="text-gray-400 hover:text-white"
                        >
                            Hủy
                        </button>

                        <button
                            type="submit"
                            disabled={loading}
                            className="px-6 py-2 bg-red-600 hover:bg-red-700 rounded font-bold text-sm"
                        >
                            {loading ? "ĐANG LƯU..." : "LƯU"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

/* Reusable Input */
function Input({ label, ...props }) {
    return (
        <div className="space-y-2">
            <label className="text-xs text-gray-400 uppercase">{label}</label>
            <input
                {...props}
                className={`w-full px-4 py-3 rounded-md bg-[#27272a] border border-transparent focus:outline-none focus:border-red-500 text-sm ${props.disabled ? "opacity-60 cursor-not-allowed" : ""
                    }`}
            />
        </div>
    );
}