-- Add description column to vouchers table for support of voucher details

ALTER TABLE vouchers
ADD COLUMN description TEXT NULL AFTER code;

