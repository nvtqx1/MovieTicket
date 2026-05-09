import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import MainLayout from './components/layout/MainLayout';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import Movies from "./pages/Movies";
import MovieDetail from "./pages/MovieDetail";
import Theaters from "./pages/Theaters";
import Booking from "./pages/Booking";
import Checkout from "./pages/Checkout";

function AppUser() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<MainLayout><Home /></MainLayout>} />
        <Route path="/movies" element={<MainLayout><Movies /></MainLayout>} />
        <Route path="/movies/:id" element={<MainLayout><MovieDetail /></MainLayout>} />
        <Route path="/theaters" element={<MainLayout><Theaters /></MainLayout>} />
        <Route path="/booking/:id" element={<MainLayout><Booking /></MainLayout>} />
        <Route path="/checkout/:reservationId" element={<MainLayout><Checkout /></MainLayout>} />
        <Route path="/checkout" element={<MainLayout><Checkout /></MainLayout>} />
        <Route path="/login" element={<MainLayout><Login /></MainLayout>} />
        <Route path="/register" element={<MainLayout><Register /></MainLayout>} />
        <Route path="/profile" element={<MainLayout><Profile /></MainLayout>} />
        <Route path="/admin/*" element={<Navigate to="/" replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  );
}

export default AppUser;
