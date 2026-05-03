import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';

import MainLayout from './components/layout/MainLayout';
import AdminLayout from './components/layout/AdminLayout';

import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Movies from "./pages/Movies";
import MovieDetail from "./pages/MovieDetail";
import Booking from "./pages/Booking";
import AdminDashboard from "./pages/AdminDashboard";

function App() {
  return (
    <AuthProvider>
      <Routes>

        {/* USER ROUTES */}
        <Route path="/" element={
          <MainLayout>
            <Home />
          </MainLayout>
        } />

        <Route path="/movies" element={
          <MainLayout>
            <Movies />
          </MainLayout>
        } />

        <Route path="/movies/:id" element={
          <MainLayout>
            <MovieDetail />
          </MainLayout>
        } />

        <Route path="/booking/:id" element={
          <MainLayout>
            <Booking />
          </MainLayout>
        } />

        <Route path="/login" element={
          <MainLayout>
            <Login />
          </MainLayout>
        } />

        <Route path="/register" element={
          <MainLayout>
            <Register />
          </MainLayout>
        } />

        {/* ADMIN ROUTES */}
        <Route path="/admin" element={<Navigate to="/admin/dashboard" replace />} />

        <Route path="/admin/dashboard" element={
          <AdminLayout>
            <AdminDashboard />
          </AdminLayout>
        } />

      </Routes>
    </AuthProvider>
  );
}

export default App;
