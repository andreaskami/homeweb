**About:**  

HomeWeb is an application framework for smart homes, built using Web principles and designed following REST architectural style. By integrating request queues for communicating with home devices, reliability and time efficiency are ensured while prioritized requests can be easily included to the system and multiple simultaneous family members may be supported.

**Citations:**  

If you plan to include this framework in research work, please do not forget to cite the following papers:
1. Andreas Kamilaris, Vlad Trifa, and Andreas Pitsillides. The Smart Home meets the Web of Things. International Journal of Ad Hoc and Ubiquitous Computing (IJAHUC), Special issue on The Smart Digital Home, vol. 7, no. 3, pp. 145-154, April, 2011. 
2. Andreas Kamilaris, Vlad Trifa, and Andreas Pitsillides. HomeWeb: An Application Framework for Web-based Smart Homes. In Proc. of the 18th International Conference on Telecommunications (ICT 2011), Ayia Napa, Cyprus, May 2011.

**Implementation Details:**  

This framework has been developed in Java using the Eclipse development platform
It follows a modular architecture with three main layers:
1. Device layer, which administers embedded home devices.
2. Control layer, which initializes, controls and checks the framework.
3. Presentation layer, which maintains a Web server following the REST principles.

**Documentation & Manuals:**  

The file user_manual.pdf holds specific information about the implementation of the framework, a class diagram of the Java classes used, information about the configuration parameters of the framework as well as installation instructions.

**Getting Started:**  

Since it is an Eclipse project, you may want to import it in Eclipse (not necessarily though). You need to point the project to a valid Java Running Environment (JRE).
The main class for running the Java application is the Core class, located in Control Layer package. Before running the application, the user needs to set some basic parameters, by navigating to Run->Run Configurations and selecting the project. Then, when opening the Arguments tab, he must type the following information:

[Application Framework Name] [Location] [Domain Name or IP address] [Port] {[Device Type] [USB Port]}

Device Type specifies an embedded technology to be included in the current run of the framework and USB Port defines the port of the device that acts as the base station. More than one embedded technologies may be supported. An example setting of the parameters for running the application framework, using Telosb sensor motes is the following:

ApplicationFramewok UniversityOfCyprus localhost 8080 Telosb USB0

Now you should be able to run successfully the application framework and automate your house with reliability and satisfactory performance, with support for all your house members!

**Adding Smart Home Devices:**  

Currently, we provide code in smartdevices/ folder, in order to use Telosb sensor motes in your smart home environment, which are enabled to the Internet/Web through blip and TinyOS 2.x

However, you are encouraged to write your own drivers for interacting with your own home devices through this application framework. In order to do that, you need to implement the abstract class Driver, located in package deviceLayer. Do not forget also to add a call to your class inside the Main method in the Core class, located in package controlLayer.

Happy Home Automation! :)

Dr Andreas Kamilaris
University of Cyprus
