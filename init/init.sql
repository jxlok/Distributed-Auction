CREATE DATABASE IF NOT EXISTS auction_db;
USE auction_db;

CREATE TABLE current_bids (
      itemID INT AUTO_INCREMENT PRIMARY KEY,
      offerPrice INT,
      userID VARCHAR(255) NOT NULL
);

INSERT INTO current_bids(itemID, offerPrice, userID) VALUES (1, 200, "Jingyi");
INSERT INTO current_bids(itemID, offerPrice, userID) VALUES (2, 500, "Jason");
INSERT INTO current_bids(itemID, offerPrice, userID) VALUES (3, 123, "Haocheng");