#DHSI2 Meta-Data Config

##Dataset


| Dataset Name        | Selected data element | Selected indicator  | Frequncy |
| ----------------------------- |:----------------:| ----------------:| -----:|
| LMIS Commodities Default | ADJUSTMENT_REASON, ADJUSTMENTS, DISPENSED,STOCK_ON_HAND |NONE|Daily|
| LMIS Commodities Allocated | ALLOCATION_ID, ALLOCATED, RECEIVED_DATE, RECEIVED_SOURCE, RECEIVED |NONE|Daily|
| LMIS Commodities Calculated | AMC, MONTH_OF_STOCK_ON_HAND, NUMBERS_OF_STOCK_OUT_DAYS, PROJECTED_ORDER_AMOUNT, SAFTY_STOCK, TMC | BUFFER_STOCK, MAX_STOCK_QUANTITY, MIN_STOCK_QUANTITY |Daily|
| LMIS Commodities Losses | BREAKAGE, EXPIRED, FROZON, LABEL_REMOVED, OTHERS, VVM_CHANGE |LOSSES|Daily|
| LMIS Emergency Order | EMERGENCY_ORDER_AMOUNT, EMERGENCY_REASON_FOR_ORDER |NONE|Daily|
| LMIS EM-Child Health ORDERED ROUTINE | ORDER_AMMOUNT, REASON_FOR_ORDER |NONE|Monthily|
| LMIS EM-Maternal Health ORDERED ROUTINE | ORDER_AMMOUNT, REASON_FOR_ORDER |NONE|Monthily|
| LMIS EM-Neonatal Health ORDERED ROUTINE | EM-Neonatal Health ORDER_ID, ORDER_AMMOUNT, REASON_FOR_ORDER |NONE|Monthily|
| LMIS Essential Medicines ORDERED ROUTINE | Essential Medicines ORDER_ID, ORDER_AMMOUNT, REASON_FOR_ORDER |NONE|Monthily|
| LMIS Family Planning ORDERED ROUTINE | Family Planning ORDER_ID, ORDER_AMMOUNT, REASON_FOR_ORDER |NONE|Monthily|
| LMIS Malaria ORDERED ROUTINE | Malaria ORDER_ID, ORDER_AMMOUNT, REASON_FOR_ORDER |NONE|Monthily|
| LMIS Vaccines ORDERED ROUTINE | Vaccines ORDER_ID, ORDER_AMMOUNT, REASON_FOR_ORDER |NONE|Monthily|



##Date Element Group Set 
Each one represent a program.

- EM-Child Health
- EM-Maternal Health
- EM-Neonatal Health
- Essential Medicines
- Family Planning
- Malaria
- Vaccinup

## Data Element Group
Each one represent a commodity.

- 0.05ml Syringe x 1
- 0.5ml Syringes x 1
- 2ml AD Syringe x 1
- 3 month Injectable Contraceptive (Depo Provera) x 1 vial
- 5 month Injectable Contraceptive (Noristerat) x 1 vial
- 5ml AD Syringe x 1
- AA (Artemether/Lumefantrine 20mg/120mg) Tablets x 24
- AA (Artesunate/Amodiaqine 100mg/270mg) Tablets x 3
- AA (Artesunate/Amodiaqine 100mg/270mg) Tablets x 6
- AA (Artesunate/Amodiaqine 25mg/67.5 mg) Tablets x 3
- AA (Artesunate/Amodiaqine 50mg/135mg) Tablets x 3
- AL (Artemether/Lumefantrine 20mg/120mg) Tablets x 12
- AL (Artemether/Lumefantrine 20mg/120mg) Tablets x 18
- AL (Artemether/Lumefantrine 20mg/120mg) Tablets x 6
- Albendazole 200mg Tablet x 2
- Amoxycillin 100ml Suspension x 1
- Amoxycillin 250g Capsule x 1
- Ampicillin/Cloxacillin 100ml Suspension x 1
- Ampicillin/Cloxacillin 8ml Drops x 1
- Ampicillin/Cloxacillin Capsule x 1
- Artesunate Injections x 1
- BCG Antigen x 20_doses/vial
- BCG Diluent x 1
- Chlorhexidine Gel 4% 20g x 1
- Cotrimoxazole 50ml Suspension x 1
- Cotrimoxazole Tablet x 1
- Dexamethasone 4mg Injection x 1
- Female Condoms x 1
- Ferrous Sulfate Tablet x 1
- Folic Acid Tablet x 1
- Gentamicin 80mg Injection x 1
- Hep. B Antigen x 10_doses/vial
- Implants 3 yrs (Implanon) x 1
- Implants 5 yrs (Jadelle) x 1
- IUCD x 1
- Magnesium Sulphate 50% 10ml
- Male Condoms x 1
- Measles Antigen x 10_doses/vial
- Measles Diluent x 1
- Misoprostol 200mcg Tablet x 3
- Multivitamin 100ml Syrup x 1
- Multivitamin Tablet x 1
- OPV Antigen x 20_doses/vial
- Oral Conraceptive Pill Progesterone & Oestrogen (Microgynon) x 1
- Oral Conraceptive Pill Progesterone (Excluton) x 1
- ORS Satchet x 1
- Oxytocin 10ui/ml x 1ml Ampoule
- Oxytocin 5ui/ml x 1ml Ampoule
- Paracetamol 125g Tablet x 6
- PCV Antigen x 2_doses/vial
- Paracetamol 15ml Drops x 1
- Paracetamol 60ml Syrup x 1
- Paracetamol Injection x 1
- Penta Antigen x 10_doses/vial
- Rapid Diagnostic Test (RDT) x 1
- SP (Sulphadozine/Pyrimethamine 500mg/25 mg) Tablets x 3
- Tetanus Toxoid Antigen x 10_doses/vial
- Yellow Fever Antigen x 10_doses/vial
- Yellow Fever Antigen x 5_doses/vial
- Yellow Fever Diluent x 1
- Zinc Tablets x 10
- Zinc+ORS Copack Satchet x 1

##Data Element


###Type

Name | Value Type | 
------------|--------|
ADJUSTMENTS | number |
ADJUSTMENT_REASON | number |
ALLOCATED | number |
ALLOCATION_ID | string |
AMC | number |
BREAKAGE | number |
BUFFER_STOCK | number |
DISPENSED | number |
EMERGENCY_ORDERED_AMOUNT | number |
EMERGENCY_REASON_FOR_ORDER | string |
EXPIRED | number |
FROZEN | number |
LABEL_REMOVED | number |
MAX_STOCK_QUANTITY | number |
MIN_STOCK_QUANTITY | number |
MONTHS_OF_STOCK_ON_HAND | number |
NUMBER_OF_STOCK_OUT_DAYS | number |
ORDERED_AMOUNT | number |
ORDER_ID | string |
OTHERS | number |
PROJECTED_ORDER_AMOUNT | number |
REASON_FOR_ORDER | string |
RECEIVED | number |
RECEIVE_DATE | string |
RECEIVE_SOURCE | string |
SAFETY_STOCK | number |
STOCK_ON_HAND | number |
TMC | number |
VVM_CHANGE | number |
WASTED | number |

#### In DHIS2 we have three kind of data elements. 

#### 1. CmmotityName + " " + Type

- 0.05ml Syringe x 1 ADJUSTMENTS
- 0.05ml Syringe x 1 ADJUSTMENT_REASON
- 0.05ml Syringe x 1 ALLOCATED
- 0.05ml Syringe x 1 ...
- 2ml AD Syringe x 1 ADJUSTMENTS
- 2ml AD Syringe x 1 ADJUSTMENT_REASON
- 2ml AD Syringe x 1 ALLOCATED
- 2ml AD Syringe x 1 ...
- ....

####2. Program Name + " " + ORDER_ID
- EM-Neonatal Health ORDER_ID
- Essential Medicines ORDER_ID
- Family Planning ORDER_ID
- Malaria ORDER_ID
- Vaccines ORDER_ID


####3. Special Data element
- ALLOCATION_ID



#Indicator Group
- BUFFER STOCK
- LOSSES
- MAX_STOCK_QUANTITY
- MIN_STOCK_QUANTITY

#Indicator
####CommtityName + " " + Indicator Goup Name

- 0.05ml Syringe x 1 BUFFER STOCK
- 0.05ml Syringe x 1 LOSSES
- 0.05ml Syringe x 1 MAX_STOCK_QUANTITY
- 0.05ml Syringe x 1 MIN_STOCK_QUANTITY
- 0.5ml Syringes x 1 ...
- 2ml AD Syringe x 1 ...



