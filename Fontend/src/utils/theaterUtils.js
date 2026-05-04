export const getCity = (location) => location || "Khác";

export const getUniqueCities = (theaters) => {
    const cities = theaters.map((t) => getCity(t.location));
    return ["Tất cả khu vực", ...new Set(cities)];
};

export const filterByCity = (theaters, city) => {
    if (city === "Tất cả khu vực") return theaters;
    return theaters.filter((t) => getCity(t.location) === city);
};