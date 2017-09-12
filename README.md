# WorkStealingScheduler

A description of our code and of the analysis performed can be found in WorkStealingAnalysis.pdf.

The src folder contains the source code.

To execute the program using ```java -jar WorkStealingScheduler.jar``` from bash, WorkStealingScheduler.jar must be in the same directory of the "CSVFiles" folder. 

The "CSVFiles" folder contains the following csv files: 
* inputData.csv
* outputData.csv

To set the program input, change inputData.csv file. This file can contain multiple program input, it will be executed in series. 

Each line is an input made of three numbers. 

for example: 
* 1000 5 20
* 2000 10 40	

where 
* 1000 is ARRAY SIZE, 
* 5 is the number of SERVERS, 
* and 20 is the CUTOFF