# Lab 3 Report
## Usage

### Setting up the environment
Several steps are required to set up the application and get it running. After downloading the application, the docker 
containers that contain the components of the complete application should be run. This is done via executing the 
"docker-compose up" command from the command line inside the root folder of this application. This will run all docker
containers in the foreground by default. Since the kafka consumer that resides in the "events" container
writes its input to the command line, the data records that flow through the "events" topic will start to be displayed
on the terminal. Next, the Kafka transformer application should be build. In the root folder of the application, the 
following command should be run on the command line to start an sbt interactive shell in the transformer container:
"sudo docker-compose exec transformer sbt". Inside the interactive session, the "compile" command should be executed to
build the transformer application. 

### Starting the application
To start the application, inside the previously opened sbt interactive shell, the "run" command should be executed.
This will start the Kafka transformer application, which will start consuming data records from the "events" topic, 
transform them, and send the transformed data records to the "updates" topic. Since the consumer inside the "updates" 
container consumes the data records sent through the "updates" topic, and writer their ouput to the terminal, the 
records that flow through the "updates" stream can now be observed on the terminal



## Functional overview

### Approach of problem
The initial step was to familiarize with the lab requirements and the context.
Next, in order to approach the problem, knowledge about the framework and libraries that were used in the lab had to be acquired through spending many hours reading API documentations and about the architecture of Kafka. 
Since this was the first time using Kafka, and because we are new to Scala, even more time was spent on troubleshooting and information gathering. 

### Global steps of the solution
#### Adequate
1. Realize and learn how to use Circe to deserialize the content from the event input-stream in a type same way using case classes.
2. Implement a custom transformer that would enable us to process/transform each and every incoming event. In this solution a TransformerSupplier was used to provide the Transformer instance.
3. In the Transformer instance, we implement the transform function which essentially apply the logic and return a keyvalue with the desired information.
4. Add a keyvalue state-store to store the total number of refugees based on a key. This was done in the TransformerSupplier.
5. Output the result to a output stream called *updates*.

#### Good
1. Circe to extract the additional information (city_id and city_name).
2. Tweak so that the keyvalue-store stores the number of refugees based on city_id. (change the key to city_id)
3. Realize that the map on the webserver now displays circles for each city and the number of refugees.

#### Excellent
1. Realize that we want to use the transformer's schedule function to call a method periodically (within a window of N seconds as stated in the lab manual).
2. In the punctuate method we iterate through the state store and 

TODO

## Kafka topology and setup

### Steams topology
Below is a figure of the steams topology in our transformer application. Except for the source and sink stream processors 
we have a KStream-transform. The logic to transform the input-data to output-data lies in the Kstream-Transform processor.

(TODO: add discussion)
(TODO: update topology picture if we succeed with excellent part)
![Topology picture by the visualizer](transformer/topology.png)

Stateless, as the name suggests, is when a processing element/step does not require a state to execute. 
In our case, it basically means that the records from the event input stream are independent from other records.

Stateful suggests that a state is required, e.g. when joining, aggregating or windowing input data.

The use of the *Transformer* interface and the *transform* method indicates that it is stateful, since a *Transformer* is used for stateful mapping of input record to output records.
It utilizes a state store to preserve the current total number of refugees, which will be fetched when the application has to update the value (so the update is dependent on the stored state).
(TODO more on what in the app is stateful and stateless)

> Describe why specific processing elements and steps are stateful or stateless.

## Result

> Present the result of your program.

>TODO
