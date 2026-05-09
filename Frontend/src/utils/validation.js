//Username
/**
 * Kiểm tra tên người dùng (Không chứa số và ký tự đặc biệt)
 */
export const validateUsername = (username) => {
    const re = /^[a-zA-Z0-9_]{3,20}$/;
    return re.test(username);
};

//Email
/**
 * Kiểm tra định dạng Email
 */
export const validateEmail = (email) => {
    const re = /^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/;
    return re.test(String(email).toLowerCase());
};

/**
 * Kiểm tra xem có phải địa chỉ Email không
 */
export const validateGmailOnly = (email) => {
    const re = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
    return re.test(String(email).toLowerCase());
};

//password
/**
 * Kiểm tra độ mạnh của mật khẩu
 * Yêu cầu:
 * - Ít nhất 8 ký tự
 * - 1 chữ Hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt
 */
export const validateStrongPassword = (password) => {
    const requirements = [
        { regex: /.{8,}/, message: "Tối thiểu 8 ký tự" },
        { regex: /[A-Z]/, message: "Ít nhất 1 chữ hoa" },
        { regex: /[a-z]/, message: "Ít nhất 1 chữ thường" },
        { regex: /[0-9]/, message: "Ít nhất 1 chữ số" },
        { regex: /[^A-Za-z0-9]/, message: "Ít nhất 1 ký tự đặc biệt" }
    ];

    // Tìm lỗi đầu tiên gặp phải để báo về UI
    const failed = requirements.find((req) => !req.regex.test(password));

    return failed ? failed.message : null;
};

export const validatePhoneNumber = (phone) => {
    const re = /^(0|\+84)[1-9][0-9]{8,9}$/;
    return re.test(phone.trim());
}