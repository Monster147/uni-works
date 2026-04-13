-- Insert Suppliers
INSERT INTO suppliers (suppliername, suppliertype)
VALUES ('Supplier A', 'LOCAL'),
       ('Supplier B', 'INTERNATIONAL'),
       ('Supplier C', 'LOCAL'),
       ('Supplier D', 'INTERNATIONAL'),
       ('Supplier E', 'LOCAL');

-- Insert Products
INSERT INTO products (name, price, category, supplierid)
VALUES ('Product 1', 10.5, 'ELECTRONICS', 1),
       ('Product 2', 20.0, 'FURNITURE', 1),
       ('Product 3', 15.0, 'CLOTHING', 2),
       ('Product 4', 55.0, 'ELECTRONICS', 3),
       ('Product 5', 35.0, 'CLOTHING', 3),
       ('Product 6', 120.0, 'FURNITURE', 4),
       ('Product 7', 5.0, 'CLOTHING', 5),
       ('Product 8', 250.0, 'ELECTRONICS', 4),
       ('Product 9', 80.0, 'FURNITURE', 1),
       ('Product 10', 12.5, 'CLOTHING', 2),
       ('Product 11', 300.0, 'ELECTRONICS', 5),
       ('Product 12', 60.0, 'FURNITURE', 3);

-- Insert Inventories
INSERT INTO inventories (location, name, supplierid)
VALUES ('Warehouse A', 'Main Warehouse Inventory', 1),
       ('Warehouse B', 'Retail Store Inventory', 2),
       ('Warehouse A', 'Regional Distribution Center', 3),
       ('Warehouse C', 'Online Fulfillment Center', 4),
       ('Warehouse D', 'Backup Storage', 5),
       ('Warehouse B', 'Seasonal Inventory', 1),
       ('Warehouse C', 'Outlet Inventory', 2),
       ('Warehouse C', 'Mobile Unit Storage', 3);