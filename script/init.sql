DROP DATABASE IF EXISTS auction_db;
CREATE DATABASE IF NOT EXISTS auction_db;
USE auction_db;

CREATE TABLE current_bids (
      itemID INT AUTO_INCREMENT PRIMARY KEY,
      startTime TIMESTAMP NOT NULL,
      endTime TIMESTAMP NOT NULL,
      offerPrice INT NOT NULL,
      bidTime TIMESTAMP NOT NULL,
      userID VARCHAR(255) NOT NULL
);

INSERT INTO current_bids(itemID, startTime, endTime, offerPrice, bidTime, userID) VALUES (1, DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_ADD(NOW(), INTERVAL 1 HOUR), 200, NOW(), "jingyi");
INSERT INTO current_bids(itemID, startTime, endTime, offerPrice, bidTime, userID) VALUES (2, DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_ADD(NOW(), INTERVAL 1 HOUR), 500, NOW(), "jason");
INSERT INTO current_bids(itemID, startTime, endTime, offerPrice, bidTime, userID) VALUES (3, DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_ADD(NOW(), INTERVAL 1 HOUR), 123, NOW(), "haocheng");
INSERT INTO current_bids(itemID, startTime, endTime, offerPrice, bidTime, userID) VALUES (4, DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_ADD(NOW(), INTERVAL 2 MINUTE), 123, NOW(), "finish");