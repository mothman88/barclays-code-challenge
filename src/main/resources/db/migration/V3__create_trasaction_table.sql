CREATE TABLE transactions (
  id VARCHAR(255) PRIMARY KEY,
  account_number VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  amount DOUBLE PRECISION NOT NULL,
  currency VARCHAR(10) NOT NULL,
  type VARCHAR(50) NOT NULL,
  reference VARCHAR(255),
  FOREIGN KEY (account_number) REFERENCES bank_accounts(account_number),
  FOREIGN KEY (user_id) REFERENCES users(id)
);