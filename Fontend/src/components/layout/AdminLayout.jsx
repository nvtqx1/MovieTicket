import AdminSidebar from "../admin/AdminSidebar";

export default function AdminLayout({ children }) {
    return (
        <div className="flex min-h-screen bg-[#0a0a0a] text-white">
            <AdminSidebar />
            <main className="flex-1 overflow-y-auto">
                {children}
            </main>
        </div>
    );
}