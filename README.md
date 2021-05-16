## SYSC 3303 Winter 2021 Term Project
##### L2 Group 6 - Iteration 4

--------------------

#### Setup instructions:
##### Version: Java 14+
* Download file from CuLearn
* Extract project zip folder from submission zip folder
* Use Eclipse to import a new project
  - File -> Import -> General -> Projects from Folder or Archive
* Click Next
* Select Archive next to import source
* Select the project zip folder
* Click Finish
* Run main method in the Scheduler class
* Wait until the Scheduler outputs that it is ready for clients to register
* Run 'n' Elevators where 'n' is the number of elevators you would like the system to use
* Run the Floor to start sending events to the Scheduler

&nbsp;

#### Configuration instructions:
* If you want to run the project faster, open src/util/Config.java
  - Set USE_ZERO_FLOOR_TIME = true
	- This will ignore all real-time timings, which are now implemented in the system as requested in the last project iteration demo

* If you want to see more verbose information in the logs, open the file associated with that subsystem (Floor, Elevator, Scheduler)
  - Set log level to VERBOSE from INFO
	- Level.INFO is the default.
	- It includes all high-level info to show it is designed to spec, however does not show every message being sent back and forth between the subsystems

&nbsp;

#### Elevator Algorithm:
* Elevators store occupancy per floor, assume each request is 1 person
* At most, half of the elevators can be going up or down at a given time (to avoid multiple elevators going up, and having no elevators to service down requests until they are finished)
* An elevator can handle a given request if:
  - If an elevator is moving (up or down) and a received request can be serviced on the way, it is sent (ie, going from floor 1 to 7, and receives request going from floor 3 to 5, which is on the way and is in the same direction)
  - If the elevator is stopped it can be sent a request, provided that less than half of all elevators are currently moving in the direction of that request
* If there are multiple elevators that can service a request:
	- Choose the elevator that is currently the closest to the request source floor
	- If multiple elevators can service the request AND are the same distance from the request source floor
		- Choose the one that minimizes elevator capacity for the floors covered by the request
		- If multiple elevators match all above critera, pick one at random

&nbsp;

#### Error Algorithm:
All errors are coded into input file in the last column, 0 = no error, 1 = fatal floor error, 2 = door error
* Door Error: 
	- Door error is recieved
	- Elevator pauses for 10 seconds, but can continue to recieve requests, as the spec assumes all door errors are recoverable
	- After 10 seconds the elevator leaves in recovery state and continues

* Fatal Floor Error:
	- Floor error is recieved
	- If elevator has recoverable pending tasks, they are sent back to the scheduler to be redistrubted 
	- Elevator de-registers so it recieves no more events
	- Elevator exits

&nbsp;

#### Breakdown of Tasks

Breakdown of Tasks for Iteration 4:
* Baillie Noell: Timing Diagrams, Sequence Diagrams, README
* Ethan Prentice: Error Handling Algorithm Design & Code implementation
* Nicholas Milani: Test classes
* Nikhil Kharbanda: UML Class Diagram, Test classes
	
Breakdown of Tasks for Iteration 3:
* Baillie Noell: Sequence Diagram, READme
* Ethan Prentice: Elevator Algorithm Design & Code implementation, Message Parsing / Handling
* Nicholas Milani: Test classes
* Nikhil Kharbanda: UML Class Diagram, UML Sequence Diagram

Breakdown of Tasks for Iteration 2:
* Baillie Noell: Scheduler, State machine design
* Ethan Prentice: Elevator, Floor, Logging, State machine design & Code implementation
* Nicholas Milani: Test classes
* Nikhil Kharbanda: UML sequence, State machine design
* Sarah Abdallah: UML class, UML sequence, READMe

Breakdown of Tasks for Iteration 1:
* Ethan Prentice: Event data class, ButtonDirection, Floor classes
* Baillie Noell: Scheduler class
* Nicholas Milani: TestCase tests
* Nikhil Kharbanda: Elevator class
* Sarah Abdallah: Documentation (README, Class diagram, Sequence Diagram)
