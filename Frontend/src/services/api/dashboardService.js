import api from "./api";

const unwrapData = (response) => response.data ?? response;

/**
 * GET /api/v1/dashboard/summary
 * @returns GeneralStatsDTO
 */
export const getDashboardSummary = async () => {
    const response = await api.get("/dashboard/summary");
    return unwrapData(response);
};

/**
 * GET /api/v1/dashboard/daily-revenue/last-7-days
 * @returns DailyRevenueDTO[]
 */
export const getDailyRevenue = async () => {
    const response = await api.get("/dashboard/daily-revenue/last-7-days");
    return unwrapData(response);
};

/**
 * GET /api/v1/dashboard/daily-revenue/last-30-days
 * @returns DailyRevenueDTO[]
 */
export const getLast30DaysRevenue = async () => {
    const response = await api.get("/dashboard/daily-revenue/last-30-days");
    return unwrapData(response);
};

/**
 * GET /api/v1/dashboard/daily-revenue/current-month
 * @returns DailyRevenueDTO[]
 */
export const getCurrentMonthRevenue = async () => {
    const response = await api.get("/dashboard/daily-revenue/current-month");
    return unwrapData(response);
};

/**
 * GET /api/v1/dashboard/daily-revenue?startDate=&endDate=
 * @returns DailyRevenueDTO[]
 */
export const getDailyRevenueByRange = async (startDate, endDate) => {
    const response = await api.get(
        `/dashboard/daily-revenue?startDate=${startDate}&endDate=${endDate}`
    );
    return unwrapData(response);
};

/**
 * GET /api/v1/dashboard/theater-revenue
 * @returns TheaterRevenueDTO[]
 */
export const getTheaterRevenue = async () => {
    const response = await api.get("/dashboard/theater-revenue");
    return unwrapData(response);
};

/**
 * GET /api/v1/dashboard/movie-revenue
 * @returns MovieRevenueDTO[]
 */
export const getMovieRevenue = async () => {
    const response = await api.get("/dashboard/movie-revenue");
    return unwrapData(response);
};

/**
 * GET /api/v1/dashboard/gender-stats
 * @returns GenderStatDTO[]
 */
export const getGenderStats = async () => {
    const response = await api.get("/dashboard/gender-stats");
    return unwrapData(response);
};

/**
 * GET /api/v1/dashboard/age-group-stats
 * @returns AgeGroupStatDTO[]
 */
export const getAgeGroupStats = async () => {
    const response = await api.get("/dashboard/age-group-stats");
    return unwrapData(response);
};
