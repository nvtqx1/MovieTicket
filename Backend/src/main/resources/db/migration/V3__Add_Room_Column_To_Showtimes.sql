-- Add missing room column to showtimes table
ALTER TABLE showtimes
ADD COLUMN room VARCHAR(50) AFTER is_flash_sale;

-- Optionally, populate the room column with default values if needed
-- For example, you could set room names based on theater or other logic
UPDATE showtimes SET room = 'Room A' WHERE room IS NULL;

