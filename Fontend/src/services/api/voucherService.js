import api from './api';

export const checkVoucher = async (code) => {
    const response = await api.get(`/vouchers/check?code=${code}`);
    return response.data;
};

export const createVoucher = async (voucherData) => {
    const response = await api.post('/admin/vouchers', voucherData);
    return response.data;
};
