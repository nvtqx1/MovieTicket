import { TrendingUp, Ticket, Film } from "lucide-react";
import SummaryCard from "./SummaryCard";
import { formatCurrency } from "../../../utils/number";

export default function RevenueSummary({ totalRevenue, totalTickets, totalMovies }) {
    return (
        <div className="grid md:grid-cols-3 gap-6 mb-10">
            <SummaryCard
                title="Tổng doanh thu"
                value={formatCurrency(totalRevenue)}
                icon={TrendingUp}
                color="text-green-500"
            />

            <SummaryCard
                title="Vé đã bán"
                value={totalTickets.toLocaleString()}
                icon={Ticket}
                color="text-blue-500"
            />

            <SummaryCard
                title="Phim đang chiếu"
                value={totalMovies}
                icon={Film}
                color="text-red-500"
            />
        </div>
    );
}
