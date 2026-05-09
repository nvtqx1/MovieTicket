import { Navigate } from "react-router-dom";
import AdminSidebar from "../admin/AdminSidebar";
import { useAuthContext } from "../../context/AuthContext";

export default function AdminLayout({ children }) {
    const { isAuthenticated, roles } = useAuthContext();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (!roles || !roles.includes("ROLE_ADMIN") && !roles.includes("ADMIN")) {
        // Fallback to home if not admin
        return <Navigate to="/" replace />;
    }

    return (
        <div className="flex min-h-screen bg-[#0a0a0a] text-white">
            <AdminSidebar />
            <main className="flex-1 overflow-y-auto">
                {children}
            </main>
        </div>
    );
}