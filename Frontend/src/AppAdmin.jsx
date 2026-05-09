import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import AdminLayout from './components/layout/AdminLayout';
import Login from './pages/Login';
import AdminDashboard from "./pages/AdminDashboard";
import RevenueAnalytics from "./pages/RevenueAnalytics";
import AdminMovies from "./pages/AdminMovies";
import AdminSeatCreator from "./pages/AdminSeatCreator";
import AdminShowtimes from "./pages/AdminShowtimes";
import AdminVouchers from "./pages/AdminVouchers";
import AdminUsers from "./pages/AdminUsers";
import AdminTheaters from "./pages/AdminTheaters";
import AdminRooms from "./pages/AdminRooms";

function AppAdmin() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/admin" element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="/admin/dashboard" element={<AdminLayout><AdminDashboard /></AdminLayout>} />
        <Route path="/admin/revenue-analytics" element={<AdminLayout><RevenueAnalytics /></AdminLayout>} />
        <Route path="/admin/movies" element={<AdminLayout><AdminMovies /></AdminLayout>} />
        <Route path="/admin/showtimes" element={<AdminLayout><AdminShowtimes /></AdminLayout>} />
        <Route path="/admin/vouchers" element={<AdminLayout><AdminVouchers /></AdminLayout>} />
        <Route path="/admin/theaters/:id/rooms" element={<AdminLayout><AdminRooms /></AdminLayout>} />
        <Route path="/admin/rooms/:id/seats" element={<AdminLayout><AdminSeatCreator /></AdminLayout>} />
        <Route path="/admin/users" element={<AdminLayout><AdminUsers /></AdminLayout>} />
        <Route path="/admin/theaters" element={<AdminLayout><AdminTheaters /></AdminLayout>} />
        <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  );
}

export default AppAdmin;
