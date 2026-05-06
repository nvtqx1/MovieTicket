export default function SummaryCard({ title, value, icon: Icon, color }) {
    return (
        <div className="bg-[#18181b] p-6 rounded-xl border border-white/5 flex items-center justify-between">
            <div>
                <p className="text-xs text-gray-500 uppercase mb-2">{title}</p>
                <h3 className="text-2xl font-black">{value}</h3>
            </div>

            {Icon && (
                <div className={`p-3 rounded-lg bg-white/5 ${color}`}>
                    <Icon size={22} />
                </div>
            )}
        </div>
    );
}
