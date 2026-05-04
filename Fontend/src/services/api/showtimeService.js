const MOCK_SHOWTIMES = [
    { id: 1, movieTitle: "Dune", showDate: "2026-10-24", showTime: "19:30" },
    { id: 2, movieTitle: "Oppenheimer", showDate: "2026-10-24", showTime: "20:00" },
];

// GET /v1/showtimes
export const getShowtimes = async () => {
    return new Promise((resolve) => {
        setTimeout(() => resolve([...MOCK_SHOWTIMES]), 500);
    });
};

// GET /v1/showtimes/{id}
export const getShowtimeById = async (id) => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const showtime = MOCK_SHOWTIMES.find(s => s.id === Number(id));
            if (!showtime) reject(new Error("Không tìm thấy lịch chiếu."));
            else resolve(showtime);
        }, 500);
    });
};

// POST /v1/showtimes
export const createShowtime = async (showtimeData) => {
    return new Promise((resolve) => {
        setTimeout(() => {
            const newShowtime = {
                id: Math.max(...MOCK_SHOWTIMES.map(s => s.id), 0) + 1,
                ...showtimeData,
            };
            MOCK_SHOWTIMES.push(newShowtime);
            console.log("Showtime created:", newShowtime);
            resolve(newShowtime);
        }, 500);
    });
};

// PUT /v1/showtimes/{id}
export const updateShowtime = async (id, showtimeData) => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const index = MOCK_SHOWTIMES.findIndex(s => s.id === Number(id));
            if (index === -1) {
                reject(new Error("Không tìm thấy lịch chiếu để cập nhật."));
                return;
            }
            const updatedShowtime = {
                id: Number(id),
                ...showtimeData,
            };
            MOCK_SHOWTIMES[index] = updatedShowtime;
            console.log("Showtime updated:", updatedShowtime);
            resolve(updatedShowtime);
        }, 500);
    });
};

// DELETE /v1/showtimes/{id}
export const deleteShowtime = async (id) => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const index = MOCK_SHOWTIMES.findIndex(s => s.id === Number(id));
            if (index === -1) {
                reject(new Error("Không tìm thấy lịch chiếu để xóa."));
                return;
            }
            const deletedShowtime = MOCK_SHOWTIMES.splice(index, 1)[0];
            console.log("Showtime deleted:", deletedShowtime);
            resolve(deletedShowtime);
        }, 500);
    });
};