-- Update a user's password (requires updating both the hash and the salt)
UPDATE "user"
    SET psw_hash = ?, psw_salt = ?
    WHERE id = ?;

UPDATE "user"
    SET psw_hash = ?, psw_salt = ?
    WHERE id = ?;

-- Update a user's basic profile information (first_name, last first_name, and date of birth)
UPDATE "user"
    SET first_name = ?, last_name = ?, birth_date = ?
    WHERE id = ?;

UPDATE "user"
    SET first_name = ?, last_name = ?, birth_date = ?
    WHERE id = ?;

-- Update the geographic location assigned to a user
UPDATE "user" SET latitude = ? WHERE id = ?;

UPDATE "user" SET latitude = ? WHERE id = ?;


-- Update the main content of a review (the user changes their rating and text)
UPDATE review
    SET rating = ?, text = ?, created_at = ?
    WHERE id = ?;

-- Add, modify, or remove an user's reply to a user review
UPDATE review
    SET response = ?, responded_at = ?
    WHERE id = ?;

-- Rename a restaurant[cite: 11, 12]
UPDATE restaurant SET name = ? WHERE id = ?;

-- Update a restaurant's general description[cite: 11, 12]
UPDATE restaurant SET description = ? WHERE id = ?;

-- Update a restaurant's contact information (phone and website)[cite: 11, 12]
UPDATE restaurant
    SET phone_number = ?, web_url = ?
    WHERE id = ?;

-- Update a restaurant's geographic location assignment[cite: 11, 12]
UPDATE restaurant
    SET latitude = ?, longitude = ?
    WHERE id = ?;

-- Update a restaurant's price range category[cite: 11, 12]
UPDATE restaurant SET price_range = ? WHERE id = ?;

-- Update a restaurant's Michelin recognition (Awards and Green Star status)[cite: 11, 12]
UPDATE restaurant
    SET award = ?, green_star = ?
    WHERE id = ?;

-- Update a restaurant's available services and amenities (Delivery and Online Booking)[cite: 11, 12]
UPDATE restaurant
    SET has_delivery = ?, has_booking = ?
    WHERE id = ?;

-- Transfer ownership of a restaurant to a different user
UPDATE restaurant SET owner_id = ? WHERE id = ?;

