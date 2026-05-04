export const generateSeatMatrix = async (payload) => {
    console.log("POST /v1/admin/seats/matrix/generate", payload);

    return new Promise((resolve, reject) => {
        setTimeout(() => {
            if (!payload.showtimeId) {
                reject(new Error("Thiếu showtimeId"));
                return;
            }

            resolve({
                showtimeId: payload.showtimeId,
                totalSeatsGenerated: payload.rows * payload.cols,
                rows: payload.rows,
                cols: payload.cols,
                message: "Successfully generated seats"
            });
        }, 800);
    });

    // real:
    // return axiosInstance.post("/v1/admin/seats/matrix/generate", payload).then(r => r.data);
};