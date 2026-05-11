ALTER TABLE admin_users RENAME COLUMN username TO email;
ALTER TABLE admin_users ALTER COLUMN email TYPE VARCHAR(255);
