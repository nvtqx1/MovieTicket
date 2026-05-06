export const toNumber = (value) => Number(value ?? 0);

export const formatCurrency = (value) =>
    new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
        notation: "compact",
    }).format(value ?? 0);
