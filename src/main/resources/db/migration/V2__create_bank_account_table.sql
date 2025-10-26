CREATE TABLE bank_accounts (
  account_number VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  account_type VARCHAR(50) NOT NULL,
  sort_code VARCHAR(20) NOT NULL,
  balance DOUBLE PRECISION NOT NULL,
  currency VARCHAR(10) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);