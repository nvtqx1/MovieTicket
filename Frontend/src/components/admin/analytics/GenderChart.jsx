export default function GenderChart({ data }) {
    return (
        <div className="bg-[#18181b] p-8 rounded-xl">
            <p className="text-xs text-gray-500 mb-6">Giới tính</p>

            <div className="space-y-4">
                {data.map((item) => (
                    <div key={item.gender}>
                        <div className="flex justify-between text-sm mb-2">
                            <span>{item.gender}</span>
                            <span className="font-bold">{item.percentage}%</span>
                        </div>
                        <div className="h-2 rounded bg-zinc-800 overflow-hidden">
                            <div
                                className="h-full bg-red-500"
                                style={{ width: `${item.percentage}%` }}
                            />
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
