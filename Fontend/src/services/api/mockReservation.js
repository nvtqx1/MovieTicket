export const confirmReservation = async (payload) => {
    console.log("REQUEST:", payload);

    // Giả lập delay giống call API thật
    await new Promise((resolve) => setTimeout(resolve, 1500));

    // Validate đơn giản giống BE
    if (!payload.reservationId || payload.seatNumbers.length === 0) {
        throw new Error("Invalid data");
    }

    // Mock response giống BE đã định nghĩa
    return {
        totalPrice: payload.seatNumbers.length * 100000,
        status: "CONFIRMED",
        qrCodeDataUri: `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=RES-${payload.reservationId}`
    };
};