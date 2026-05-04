import api from "./api";

const USE_MOCK_DASHBOARD = true;

const delay = (data, ms = 500) =>
    new Promise((resolve) => setTimeout(() => resolve(data), ms));

const mockSummary = {
    totalUsers: 1540,
    totalTheaters: 8,
    totalMovies: 24,
    totalShowtimes: 186,
    totalRevenue: 1284500000,
    totalReservations: 3124,
    totalTicketsSold: 8950,
    todayRevenue: 45200000,
    todayReservations: 86,
};

const mockDailyRevenue = [
    { date: "2026-04-29", totalRevenue: 28000000, orderCount: 42, ticketsSold: 118 },
    { date: "2026-04-30", totalRevenue: 36000000, orderCount: 55, ticketsSold: 146 },
    { date: "2026-05-01", totalRevenue: 52000000, orderCount: 74, ticketsSold: 203 },
    { date: "2026-05-02", totalRevenue: 61000000, orderCount: 88, ticketsSold: 231 },
    { date: "2026-05-03", totalRevenue: 47000000, orderCount: 69, ticketsSold: 177 },
    { date: "2026-05-04", totalRevenue: 45200000, orderCount: 86, ticketsSold: 190 },
    { date: "2026-05-05", totalRevenue: 31800000, orderCount: 49, ticketsSold: 132 },
];

const mockTheaterRevenue = [
    { theaterName: "CGV Vincom Landmark 81", totalRevenue: 420000000, ticketsSold: 3250 },
    { theaterName: "Lotte Cinema Nam Sai Gon", totalRevenue: 380000000, ticketsSold: 2810 },
    { theaterName: "BHD Star Thao Dien", totalRevenue: 290000000, ticketsSold: 2105 },
    { theaterName: "Galaxy Nguyen Du", totalRevenue: 210000000, ticketsSold: 1620 },
];

const mockMovieRevenue = [
    { movieTitle: "Dune: Hanh Tinh Cat - Phan 2", totalRevenue: 520000000, showtimeCount: 124 },
    { movieTitle: "Mai", totalRevenue: 380000000, showtimeCount: 96 },
    { movieTitle: "Kung Fu Panda 4", totalRevenue: 265000000, showtimeCount: 72 },
];

const mockGenderStats = [
    { gender: "Female", count: 55 },
    { gender: "Male", count: 43 },
    { gender: "Other", count: 2 },
];

const mockAgeGroupStats = [
    { ageGroup: "<18", count: 150, percentage: 15 },
    { ageGroup: "18-24", count: 450, percentage: 45 },
    { ageGroup: "25-34", count: 280, percentage: 28 },
    { ageGroup: "35+", count: 120, percentage: 12 },
];

const unwrapData = (response) => response.data ?? response;

export const getDashboardSummary = async () => {
    if (USE_MOCK_DASHBOARD) return delay(mockSummary);

    const response = await api.get("/dashboard/summary");
    return unwrapData(response);
};

export const getDailyRevenue = async () => {
    if (USE_MOCK_DASHBOARD) return delay(mockDailyRevenue);

    const response = await api.get("/dashboard/daily-revenue/last-7-days");
    return unwrapData(response);
};

export const getTheaterRevenue = async () => {
    if (USE_MOCK_DASHBOARD) return delay(mockTheaterRevenue);

    const response = await api.get("/dashboard/theater-revenue");
    return unwrapData(response);
};

export const getMovieRevenue = async () => {
    if (USE_MOCK_DASHBOARD) return delay(mockMovieRevenue);

    const response = await api.get("/dashboard/movie-revenue");
    return unwrapData(response);
};

export const getGenderStats = async () => {
    if (USE_MOCK_DASHBOARD) return delay(mockGenderStats);

    const response = await api.get("/dashboard/gender-stats");
    return unwrapData(response);
};

export const getAgeGroupStats = async () => {
    if (USE_MOCK_DASHBOARD) return delay(mockAgeGroupStats);

    const response = await api.get("/dashboard/age-group-stats");
    return unwrapData(response);
};
