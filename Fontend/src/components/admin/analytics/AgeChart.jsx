import { toNumber } from "../../../utils/number";

export default function AgeChart({ data, max }) {
    return (
        <div className="bg-[#18181b] p-8 rounded-xl flex flex-col h-full">
            <p className="text-xs text-gray-500 mb-6">Độ tuổi</p>

            <div className="flex gap-4 flex-1 min-h-[160px]">
                {data.map(a => {
                    const height = max > 0 ? (toNumber(a.percentage) / max) * 100 : 0;

                    return (
                        <div key={a.ageGroup} className="flex flex-col items-center justify-end flex-1 h-full group">
                            <div className="w-full flex-1 flex flex-col justify-end items-center relative">
                                <span className="text-[10px] text-gray-300 font-bold mb-1 opacity-0 group-hover:opacity-100 transition-opacity absolute -top-5">
                                    {toNumber(a.percentage).toFixed(1)}%
                                </span>
                                <div
                                    className="w-full max-w-[40px] bg-red-600/60 group-hover:bg-red-500 rounded-t-md transition-all duration-300"
                                    style={{ height: `${height}%` }}
                                />
                            </div>
                            <span className="text-xs mt-3 text-gray-400 group-hover:text-white transition-colors">{a.ageGroup}</span>
                        </div>
                    );
                })}
                
                {(!data || data.length === 0) && (
                    <div className="w-full flex items-center justify-center text-gray-500 text-sm">
                        Không có dữ liệu
                    </div>
                )}
            </div>
        </div>
    );
}
