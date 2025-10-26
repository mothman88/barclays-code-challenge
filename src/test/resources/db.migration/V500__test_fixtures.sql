-- this is for testing, this code challenge app is not supposed to create users

INSERT INTO users (
  id,
  name,
  email,
  phone_number,
  password,
  line1,
  line2,
  town,
  county,
  postcode
) VALUES (
  'usr-000001',
  'Mahmad Test',
  'email@barclays.com',
  '+44777777777',
  'securepassword',
  '123 Main Street',
  '',
  'Liverpool',
  'Merseyside',
  'L1 1AB'
);