import Navbar from "../layout/Navbar";

export default function MainLayout({ children }) {
    return (
        <div className="overflow-x-hidden">
            <Navbar />
            {children}
        </div>
    );
}