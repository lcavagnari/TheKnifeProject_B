-- Remove a "user" from the database by ID[cite: 3]
DELETE FROM "user" WHERE id = ?;

-- Remove all favorite restaurants for a specific "user"[cite: 3]
DELETE FROM user_favorites WHERE user_id = ?;

-- Remove one specific restaurant from a "user"'s favorites list[cite: 3]
DELETE FROM user_favorites
WHERE
    user_id = ? AND restaurant_id = ?;

-- Remove a geographic location by its ID[cite: 4]
DELETE FROM location WHERE latitude = ? AND longitude = ?;

-- Remove a restaurant "user" from the database by ID[cite: 5]
DELETE FROM "user" WHERE id = ?;

-- Remove a restaurant from the database by ID[cite: 6]
DELETE FROM restaurant WHERE id = ?;

-- Remove a "user" review from the database by ID[cite: 7]
DELETE FROM review WHERE id = ?;