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

INSERT INTO current_bids(itemID, startTime, endTime, offerPrice, bidTime, userID) VALUES (1, DATE_SUB(NOW(), INTERVAL 5 MINUTE), Date_ADD(NOW(), INTERVAL 1 DAY), 200, CURRENT_TIMESTAMP, "jingyi");
INSERT INTO current_bids(itemID, startTime, endTime, offerPrice, bidTime, userID) VALUES (2, DATE_SUB(NOW(), INTERVAL 5 MINUTE), Date_ADD(NOW(), INTERVAL 1 DAY), 500, CURRENT_TIMESTAMP, "jason");
INSERT INTO current_bids(itemID, startTime, endTime, offerPrice, bidTime, userID) VALUES (3, DATE_SUB(NOW(), INTERVAL 5 MINUTE), Date_ADD(NOW(), INTERVAL 1 DAY), 123, CURRENT_TIMESTAMP, "haocheng");