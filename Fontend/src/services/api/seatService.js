// ==========================================
// FAKE DATABASE
// ==========================================
let MOCK_SEATS = [];

// ==========================================
// POST: GENERATE SEATS
// ==========================================
export const generateSeatMatrix = async (payload) => {
    console.log("POST /v1/admin/seats/matrix/generate", payload);

    return new Promise((resolve, reject) => {
        setTimeout(() => {
            if (!payload.showtimeId) {
                reject(new Error("Thiếu showtimeId"));
                return;
            }

            const { showtimeId, rows, cols } = payload;

            // ❗ XÓA ghế cũ của showtime
            MOCK_SEATS = MOCK_SEATS.filter(
                s => s.showtimeId !== showtimeId
            );

            const newSeats = [];

            for (let r = 0; r < rows; r++) {
                const rowLabel = String.fromCharCode(65 + r);

                for (let c = 1; c <= cols; c++) {
                    newSeats.push({
                        showtimeId,
                        seatNumber: `${rowLabel}${c}`,
                        type: (r <= 2 && c >= 5 && c <= 8) ? "VIP" : "STANDARD",
                        status: "AVAILABLE"
                    });
                }
            }

            MOCK_SEATS.push(...newSeats);

            resolve({
                showtimeId,
                totalSeatsGenerated: newSeats.length,
                rows,
                cols,
                message: "Successfully generated seats"
            });

        }, 800);
    });
};

// ==========================================
// GET: SEATS BY SHOWTIME
// ==========================================
export const getSeatsByShowtime = async (showtimeId) => {
    console.log("GET /v1/seats/showtime/" + showtimeId);

    return new Promise((resolve) => {
        setTimeout(() => {
            const seats = MOCK_SEATS.filter(
                s => s.showtimeId === Number(showtimeId)
            );
            resolve(seats);
        }, 500);
    });
};