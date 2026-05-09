import api from './api';

export const checkVoucher = async (code) => {
    const response = await api.get(`/vouchers/check?code=${code}`);
    return response.data;
};

export const createVoucher = async (voucherData) => {
    const response = await api.post('/vouchers', voucherData);
    return response.data;
};

export const getAllVouchers = async () => {
    const response = await api.get('/vouchers');
    return response.data;
};

export const updateVoucher = async (id, voucherData) => {
    const response = await api.put(`/vouchers/${id}`, voucherData);
    return response.data;
};

export const deleteVoucher = async (id) => {
    const response = await api.delete(`/vouchers/${id}`);
    return response.data;
};
