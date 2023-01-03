-- Database
CREATE DATABASE bikeshop;
\c bikeshop;

-- Bikes
CREATE TABLE bikes(
    id serial NOT NULL,
    PRIMARY KEY (id),
    model character varying NOT NULL,
    brand character varying NOT NULL,
    year integer NOT NULL,
    price integer NOT NULL,
    size character varying NOT NULL
    );

  CREATE TABLE brands(
    id serial NOT NULL,
    PRIMARY KEY (id),
    name character varying NOT NULL
    );

-- Bikes
INSERT INTO bikes (model, brand, year, price, size) VALUES ('Trucker','Surly',2020,1100,'XL');
INSERT INTO bikes (model, brand, year, price, size) VALUES ('Racer','Giant',2021,1200,'L');
INSERT INTO bikes (model, brand, year, price, size) VALUES ('Sutra','Kona',2022,1750,'L');
COMMIT;

-- Brands
INSERT INTO brands (name) VALUES ('Surly');
INSERT INTO brands (name) VALUES ('Specialized');
INSERT INTO brands (name) VALUES ('Kona');
INSERT INTO brands (name) VALUES ('Genesis');
INSERT INTO brands (name) VALUES ('Merida');
INSERT INTO brands (name) VALUES ('Giant');
COMMIT;
