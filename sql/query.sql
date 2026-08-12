-- Retrieve a specific customer by their ID
SELECT * FROM "user" WHERE id = ? AND is_owner = 0;

-- Retrieve a specific customer by their username
SELECT * FROM "user" WHERE username = ? AND is_owner = 0;

-- Retrieve all customers in the database
SELECT id FROM "user";
SELECT * FROM "user";

-- Retrieve all favorite restaurant IDs for a specific customer
SELECT restaurant_id FROM user_favorites WHERE user_id = ?;

-- Retrieve a specific location by its ID
SELECT * FROM location WHERE latitude = ? AND longitude = ?;

-- Retrieve all locations in the database
SELECT latitude,longitude FROM location;


-- Retrieve all restaurants in the database
SELECT id FROM restaurant;

-- Retrieve all restaurants belonging to a specific owner
SELECT id FROM restaurant WHERE owner_id = ?;

-- Retrieve all cuisine types associated with a specific restaurant
SELECT type FROM restaurant_cuisine WHERE restaurant_id = ?;

-- Retrieve all services provided by a specific restaurant
SELECT service FROM restaurant_services WHERE restaurant_id = ?;

-- Retrieve a specific review by its ID
SELECT id FROM review WHERE id = ?;

-- Retrieve all reviews written for a specific restaurant
SELECT id FROM review WHERE restaurant_id = ?;
