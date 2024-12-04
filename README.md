# Pharmacy Store Management System

The `Pharmacy Store Management System` is a `console-based` application designed to facilitate the efficient management of drug inventory and customer transactions in a pharmacy. The system allows an admin to perform various tasks, including managing drugs and customer records, processing sales, and generating invoices.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Database Schema](#database-schema)
- [Usage Instructions](#usage-instructions)
- [Conclusion](#conclusion)

## Features

- **Drug Management**: Add, update, delete, view, and list expired drugs.
- **Customer Management**: Register, delete, and update customer information.
- **Cart Management**: Add drugs to cart, view cart, and checkout.
- **Action Logging**: All user actions are stored in a stack data structure for review.
- **Sales Report** Generates sales report between current and admin defined date.
- **Alerts** Display all the expired and drugs having quantities less than 20.
- **Help**: Get information about drugs using descriptions, tags, names, or IDs.


## System Requirements

### Software Requirements

- Java Development Kit (JDK) 8 or above
- Database Management System (PostgreSQL)
- Java IDE (Eclipse, IntelliJ IDEA, or NetBeans)

### Hardware Requirements

- Processor: Intel Core i3 or above
- RAM: 4 GB or above
- Storage: 10 GB of free disk space

## Installation

1. **Clone the repository**:
    ```sh
    git clone https://github.com/AnkushGitRepo/Pharmacy-Management-System.git
    ```

2. **Navigate to the project directory**:
    ```sh
    cd pharmacy-store-management-system
    ```

3. **Set up the database**:
    - Create a PostgreSQL database named `pharmacy`.
    - Run the SQL scripts in the `sql/` directory to create the necessary tables.

4. **Update database connection settings**:
    - Update the database connection settings in the `DatabaseHandler` class to match your PostgreSQL configuration.

5. **Compile and run the application**:
    ```sh
    javac -d bin src/org/example/*.java
    java -cp bin org.example.Main
    ```

## Usage

### Main Menu

1. **Drug Management**: Manage drug inventory.
2. **Customer Management**: Manage customer records.
3. **Manage Cart**: Add drugs to cart, view cart, and checkout.
4. **Help**: Get information about drugs.
5. **Print Actions Stack**: View logged user actions.
6. **Exit**: Exit the application.

### Drug Management

- **Add Drug**: Add a new drug to the inventory.
- **Update Drug**: Update details of an existing drug.
- **Delete Drug**: Remove a drug from the inventory.
- **View Drug Inventory**: Display all drugs in the inventory.
- **List Expired Drugs**: List drugs that have expired.
- **Help**: Get information about drugs.

### Customer Management

- **Register Customer**: Register a new customer.
- **Delete Customer**: Remove a customer.
- **Update Customer**: Update customer information.
- **Manage Cart**: Add drugs to cart, view cart, and checkout for a customer.

## Database Schema

### Tables

```sql
CREATE TABLE Drugs (
    drug_id INT PRIMARY KEY CHECK (drug_id >= 1000 AND drug_id <= 9999),
    drug_name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    description TEXT,
    tags VARCHAR(255)
);

CREATE TABLE Customers (
    email VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL CHECK (name !~ '[0-9]'),
    address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) NOT NULL
);

ALTER TABLE Customers
ADD allergy TEXT;


CREATE TABLE Cart (
    cart_id SERIAL PRIMARY KEY,
    email VARCHAR(255),
    drug_id INT,
    quantity INT NOT NULL,
    FOREIGN KEY (email) REFERENCES Customers(email),
    FOREIGN KEY (drug_id) REFERENCES Drugs(drug_id),
    UNIQUE (email, drug_id)
);

CREATE TABLE Orders (
    order_id SERIAL PRIMARY KEY,
    email VARCHAR(255),
    order_date DATE NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (email) REFERENCES Customers(email)
);

CREATE TABLE OrderItems (
    order_item_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    drug_id INT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (drug_id) REFERENCES Drugs(drug_id)
);

CREATE TABLE Drugs_Audit (
    audit_id SERIAL PRIMARY KEY,
    operation VARCHAR(10) NOT NULL,
    drug_id INT NOT NULL,
    old_drug_name VARCHAR(255),
    old_manufacturer VARCHAR(255),
    old_expiry_date DATE,
    old_quantity INT,
    old_price DOUBLE PRECISION,
    old_description TEXT,
    old_tags VARCHAR(255),
    new_drug_name VARCHAR(255),
    new_manufacturer VARCHAR(255),
    new_expiry_date DATE,
    new_quantity INT,
    new_price DOUBLE PRECISION,
    new_description TEXT,
    new_tags VARCHAR(255),
    operation_time VARCHAR(50)
);
```

### Trigger for Any Update or Insert Operation On Drug Table. This Trigger Will Insert Respective Data Into Drugs_Audit Table
```sql
CREATE OR REPLACE FUNCTION log_drug_changes() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        INSERT INTO Drugs_Audit (
            operation, drug_id, old_drug_name, old_manufacturer, old_expiry_date, old_quantity, old_price, old_description, old_tags,
            new_drug_name, new_manufacturer, new_expiry_date, new_quantity, new_price, new_description, new_tags, operation_time
        ) VALUES (
            'DELETE', OLD.drug_id, OLD.drug_name, OLD.manufacturer, OLD.expiry_date, OLD.quantity, OLD.price, OLD.description, OLD.tags,
            NULL, NULL, NULL, NULL, NULL, NULL, NULL,
            TO_CHAR(CURRENT_TIMESTAMP, 'DD/MM/YYYY HH12:MI:SS AM')
        );
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO Drugs_Audit (
            operation, drug_id, old_drug_name, old_manufacturer, old_expiry_date, old_quantity, old_price, old_description, old_tags,
            new_drug_name, new_manufacturer, new_expiry_date, new_quantity, new_price, new_description, new_tags, operation_time
        ) VALUES (
            'UPDATE', OLD.drug_id, OLD.drug_name, OLD.manufacturer, OLD.expiry_date, OLD.quantity, OLD.price, OLD.description, OLD.tags,
            NEW.drug_name, NEW.manufacturer, NEW.expiry_date, NEW.quantity, NEW.price, NEW.description, NEW.tags,
            TO_CHAR(CURRENT_TIMESTAMP, 'DD/MM/YYYY HH12:MI:SS AM')
        );
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_log_drug_changes
AFTER UPDATE OR DELETE ON Drugs
FOR EACH ROW
EXECUTE FUNCTION log_drug_changes();
```
### Dummy Data For Drugs Table
```sql
-- Insert non-expired drugs with descriptions and tags
INSERT INTO Drugs (drug_id, drug_name, manufacturer, expiry_date, quantity, price, description, tags) VALUES
(1001, 'Aspirin', 'PharmaCorp', '2025-12-31', 100, 5.99, 'Pain reliever and anti-inflammatory', 'pain,fever,inflammation'),
(1002, 'Ibuprofen', 'HealthPlus', '2026-11-30', 200, 10.99, 'Anti-inflammatory medication used to reduce fever and treat pain or inflammation', 'pain,fever,inflammation'),
(1003, 'Paracetamol', 'MediLife', '2025-10-15', 150, 3.99, 'Pain reliever and a fever reducer', 'pain,fever'),
(1004, 'Amoxicillin', 'Antibiotix', '2025-09-20', 75, 15.49, 'Antibiotic used to treat various bacterial infections', 'infection,antibiotic,bacteria'),
(1005, 'Ciprofloxacin', 'BioPharma', '2026-01-25', 80, 12.89, 'Antibiotic used to treat bacterial infections', 'infection,antibiotic,bacteria'),
(1006, 'Lisinopril', 'HeartCare', '2025-08-30', 60, 8.75, 'Medication used to treat high blood pressure and heart failure', 'blood pressure,heart,hypertension'),
(1007, 'Metformin', 'Diabeat', '2025-07-14', 90, 4.50, 'Medication used to treat type 2 diabetes', 'diabetes,blood sugar'),
(1008, 'Omeprazole', 'StomachEase', '2025-11-25', 120, 7.20, 'Medication used to treat gastroesophageal reflux disease (GERD)', 'acid reflux,GERD,stomach'),
(1009, 'Atorvastatin', 'CholestrolFix', '2025-12-01', 110, 9.30, 'Medication used to lower cholesterol and triglyceride levels', 'cholesterol,heart,lipid'),
(1010, 'Levothyroxine', 'ThyroMed', '2026-02-14', 100, 6.40, 'Medication used to treat hypothyroidism', 'thyroid,hormone'),
(1011, 'Amlodipine', 'CardioSafe', '2025-06-20', 95, 5.10, 'Medication used to treat high blood pressure and coronary artery disease', 'blood pressure,heart,hypertension'),
(1012, 'Simvastatin', 'LipControl', '2026-03-10', 85, 6.90, 'Medication used to control high cholesterol', 'cholesterol,heart,lipid'),
(1013, 'Clopidogrel', 'BloodThinner', '2025-08-22', 70, 11.00, 'Medication used to prevent blood clots', 'blood clots,heart'),
(1014, 'Losartan', 'HyperTensionRelief', '2025-09-14', 65, 7.75, 'Medication used to treat high blood pressure', 'blood pressure,heart,hypertension'),
(1015, 'Gabapentin', 'NerveRelief', '2025-10-05', 130, 14.20, 'Medication used to treat nerve pain and seizures', 'nerve pain,seizures,neuropathy'),
(1016, 'ExpiredDrug1', 'ExpiredMeds', '2023-12-31', 50, 5.50, 'Expired pain reliever', 'expired,pain relief'),
(1017, 'ExpiredDrug2', 'OldPharma', '2022-11-30', 40, 6.60, 'Expired anti-inflammatory medication', 'expired,anti-inflammatory'),
(1018, 'ExpiredDrug3', 'PastMeds', '2021-10-15', 30, 7.70, 'Expired fever reducer', 'expired,fever reducer'),
(1019, 'ExpiredDrug4', 'OutdatedPharma', '2022-09-20', 25, 8.80, 'Expired antibiotic', 'expired,antibiotic'),
(1020, 'ExpiredDrug5', 'OldStock', '2023-01-25', 20, 9.90, 'Expired heart medication', 'expired,heart'),
(1021, 'Doxycycline', 'Antibiotix', '2025-12-31', 100, 13.45, 'Antibiotic used to treat bacterial infections', 'infection,antibiotic,bacteria'),
(1022, 'Metoprolol', 'HeartCare', '2025-11-30', 200, 10.75, 'Medication used to treat high blood pressure and chest pain', 'blood pressure,heart,angina'),
(1023, 'Albuterol', 'BreathEasy', '2026-10-15', 150, 12.00, 'Medication used to treat breathing problems such as asthma', 'asthma,breathing,bronchodilator'),
(1024, 'Pantoprazole', 'GastroCare', '2025-09-20', 75, 14.55, 'Medication used to treat stomach and esophagus problems', 'acid reflux,GERD,stomach'),
(1025, 'Zolpidem', 'SleepWell', '2025-01-25', 80, 8.25, 'Medication used to treat insomnia', 'insomnia,sleep aid'),
(1026, 'Hydrochlorothiazide', 'DiureticCo', '2025-08-15', 15, 5.60, 'Medication used to treat high blood pressure and fluid retention', 'blood pressure,diuretic'),
(1027, 'Furosemide', 'WaterPill', '2025-07-10', 10, 4.50, 'Diuretic used to reduce swelling and fluid retention', 'diuretic,fluid retention'),
(1028, 'Warfarin', 'AntiCoag', '2026-05-30', 18, 12.30, 'Anticoagulant used to prevent blood clots', 'anticoagulant,blood clots'),
(1029, 'Hydrocodone', 'PainRelief', '2025-09-05', 8, 15.00, 'Pain relief medication for severe pain', 'pain relief,opioid'),
(1030, 'Lorazepam', 'CalmMeds', '2025-11-20', 12, 9.80, 'Medication used to treat anxiety disorders', 'anxiety,calm');

```

INSERT INTO Drugs (drug_id, drug_name, manufacturer, expiry_date, quantity, price, description, tags, allergy) VALUES
(1031, 'Penicillin', 'Antibiotix', '2025-12-31', 50, 10.99, 'Antibiotic used to treat bacterial infections', 'infection,antibiotic,bacteria', 'Penicillin allergy'),
(1032, 'Sulfasalazine', 'ImmunePharma', '2026-03-15', 30, 15.25, 'Medication used to treat rheumatoid arthritis and inflammatory bowel disease', 'inflammation,arthritis,bowel', 'Sulfa allergy'),
(1033, 'Neomycin', 'EarCare', '2025-10-20', 75, 5.50, 'Antibiotic used to treat skin infections and prevent bacterial contamination', 'antibiotic,infection,skin', 'Neomycin allergy'),
(1034, 'Cephalexin', 'BioPharma', '2025-11-30', 60, 9.75, 'Antibiotic used to treat bacterial infections', 'infection,antibiotic,bacteria', 'Cephalosporin allergy'),
(1035, 'Aspirin', 'PainKillersInc', '2025-08-15', 150, 4.20, 'Pain reliever and anti-inflammatory', 'pain,fever,inflammation', 'Aspirin allergy'),
(1036, 'Carbamazepine', 'NeuroPharma', '2026-06-01', 40, 14.50, 'Medication used to treat seizures and nerve pain', 'seizures,nerve pain,neuropathy', 'Carbamazepine allergy'),
(1037, 'Trimethoprim', 'UTICare', '2025-09-25', 50, 12.80, 'Antibiotic used to treat urinary tract infections', 'antibiotic,UTI,infection', 'Sulfa allergy'),
(1038, 'Iodine', 'ThyroidMeds', '2025-12-10', 20, 7.20, 'Used as a disinfectant and for thyroid conditions', 'disinfectant,thyroid', 'Iodine allergy'),
(1039, 'Paclitaxel', 'OncoPharma', '2026-05-20', 15, 80.50, 'Chemotherapy drug used to treat cancer', 'cancer,chemotherapy', 'Paclitaxel allergy'),
(1040, 'Allopurinol', 'GoutRelief', '2025-11-05', 100, 6.70, 'Medication used to treat gout and kidney stones', 'gout,kidney stones,arthritis', 'Allopurinol allergy'),
(1041, 'Phenytoin', 'SeizureControl', '2025-10-15', 50, 8.90, 'Medication used to control seizures', 'seizures,neurology', 'Phenytoin allergy'),
(1042, 'Ceftriaxone', 'Antibiotix', '2025-09-30', 45, 19.50, 'Antibiotic used to treat bacterial infections', 'infection,antibiotic,bacteria', 'Cephalosporin allergy'),
(1043, 'Loratadine', 'AllergyFix', '2026-02-20', 200, 5.50, 'Antihistamine used to treat allergies', 'allergy,antihistamine', 'None'),
(1044, 'Paclitaxel', 'OncoMed', '2026-03-15', 25, 85.99, 'Chemotherapy drug used to treat various types of cancer', 'cancer,chemotherapy', 'Paclitaxel allergy'),
(1045, 'Epinephrine', 'LifeSaverMeds', '2025-07-10', 10, 25.30, 'Used for emergency treatment of severe allergic reactions (anaphylaxis)', 'emergency,allergy,anaphylaxis', 'None'),
(1046, 'Doxycycline', 'BioPharma', '2025-10-05', 80, 14.90, 'Antibiotic used to treat bacterial infections', 'antibiotic,infection,bacteria', 'None'),
(1047, 'Methotrexate', 'ImmuneCare', '2025-11-15', 60, 12.60, 'Used to treat cancer and autoimmune diseases', 'cancer,autoimmune', 'Methotrexate allergy'),
(1048, 'Hydroxyzine', 'CalmCare', '2025-12-01', 150, 5.90, 'Used to treat anxiety, nausea, and allergies', 'anxiety,allergy,nausea', 'None'),
(1049, 'Chloramphenicol', 'EyeRelief', '2026-01-20', 70, 11.30, 'Antibiotic used to treat eye infections', 'eye infections,antibiotic', 'Chloramphenicol allergy'),
(1050, 'Lidocaine', 'PainRelief', '2025-09-15', 40, 10.25, 'Local anesthetic and antiarrhythmic drug', 'local anesthetic,pain relief', 'Lidocaine allergy');


# Usage Instructions
## Prerequisites
1. Ensure you have Java JDK 8 or above installed.
2. Ensure you have PostgreSQL installed and running.
## Setup
1. Clone the repository
2. Create the `PostgreSQL database` and tables using the provided schema.
3. Import the sample data into the database.
4. Open the project in your preferred Java IDE.
5. Update the database connection settings in the `DatabaseHandler` class if necessary.

## Running the Application
1. Run the `Main` class.
2. Follow the on-screen instructions to manage drugs, customers, and orders.

# Conclusion
The Pharmacy Store Management System will streamline the management of drug inventory and customer transactions. The use of Java for backend development, a robust DBMS for data storage, and efficient data structures such as ArrayList, Stacks, and LinkedList will ensure the system is both reliable and scalable. The final system will be a console-based application with all outputs printed to the terminal or files, ensuring simplicity and ease of use.
