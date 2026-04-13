-- Enums
CREATE TYPE product_category AS ENUM ('ELECTRONICS', 'CLOTHING', 'FURNITURE');
CREATE TYPE supplier_type AS ENUM ('LOCAL', 'INTERNATIONAL');

-- Create Suppliers table with SERIAL id
CREATE TABLE suppliers (
                           supplierid SERIAL PRIMARY KEY,
                           suppliername VARCHAR NOT NULL,
                           suppliertype supplier_type NOT NULL
);

-- Create Products table with correct supplier_id as INT (matching SERIAL)
CREATE TABLE products (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR NOT NULL,
                          price DOUBLE PRECISION NOT NULL,
                          category product_category NOT NULL,
                          supplierid INT NOT NULL REFERENCES suppliers(supplierid)
);

-- Create Inventories table with correct supplier_id as INT
CREATE TABLE inventories (
                             id SERIAL PRIMARY KEY,
                             location VARCHAR(255) NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             supplierid INT NOT NULL REFERENCES suppliers(supplierid)
);