import { formatCurrency, toNumber } from "../../../utils/number";
import { Trophy } from "lucide-react";

export default function TheaterTable({ data }) {
    return (
        <div className="bg-[#18181b] p-6 rounded-xl flex flex-col h-full">
            <h3 className="text-xs font-black text-gray-500 uppercase tracking-widest mb-6 flex items-center gap-2">
                <span className="w-1 h-4 bg-red-600 rounded-full"></span>
                Top 3 Rạp Doanh Thu Cao Nhất
            </h3>
            
            <div className="space-y-3 flex-1 overflow-auto pr-2 custom-scrollbar">
                {data.map((t, index) => {
                    const isTop1 = index === 0;
                    const isTop2 = index === 1;
                    const isTop3 = index === 2;
                    
                    let rankBg = "bg-white/5 text-gray-400";
                    if (isTop1) rankBg = "bg-yellow-500/20 text-yellow-500 font-black";
                    else if (isTop2) rankBg = "bg-gray-300/20 text-gray-300 font-bold";
                    else if (isTop3) rankBg = "bg-amber-700/20 text-amber-600 font-bold";

                    return (
                        <div key={t.theaterId ?? t.theaterName} className="p-3 rounded-xl flex items-center justify-between gap-4 hover:bg-white/5 transition-colors border border-transparent hover:border-white/5">
                            <div className="flex items-center gap-4">
                                <div className={`w-8 h-8 rounded-lg flex items-center justify-center text-sm shrink-0 ${rankBg}`}>
                                    {isTop1 ? <Trophy size={16} /> : index + 1}
                                </div>
                                <div>
                                    <h3 className="font-bold text-sm line-clamp-1" title={t.theaterName}>{t.theaterName}</h3>
                                    <p className="text-[11px] text-gray-500 mt-1">
                                        {toNumber(t.ticketsSold).toLocaleString("vi-VN")} vé đã bán
                                    </p>
                                </div>
                            </div>
                            <span className="font-black text-yellow-500 text-sm whitespace-nowrap">
                                {formatCurrency(toNumber(t.totalRevenue))}
                            </span>
                        </div>
                    );
                })}
                
                {data.length === 0 && (
                    <div className="flex flex-col items-center justify-center h-40 text-gray-500 text-sm">
                        Không có dữ liệu rạp
                    </div>
                )}
            </div>
        </div>
    );
}
