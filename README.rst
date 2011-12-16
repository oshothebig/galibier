========
Galibier
========
An open source OpenFlow controller in Java

What's Galibier?
----------------
Galibier is a Java based open source OpenFlow controller. It is available under
the MIT license. The purpose of this project is to provide a flexible and
scalable framework for OpenFlow controller and applications running on OpenFlow
controllers.

OpenFlow controller
-------------------
Galibier is still under development. It provides an OpenFlow controller and 
a benchmark program for evaluating OpenFlow controllers.

How to build
------------
Galibier uses openflowj library, which provides OpenFlow protocol primitives.
openflowj have been developed in Stanford University.

1. Download openflowj library

::

   git clone git://openflow.org/openflowj.git

2. Build and install openflowj library into your local Maven repository

::

   cd openflowj
   mvn install

3. Build Galibier

::

   cd galibier
   mvn package

Act as a repeater hub
----------------------
::

  java -cp target/galibier-controller-0.1.0-devel-jar-with-dependencies.jar \
  org.galibier.example.Hub


Run the benchmark
-----------------
::

  java -cp target/galibier-controller-0.1.0-devel-jar-with-dependencies.jar \
  org.galibier.benchmark.Main localhost

"--help" option is available for printing the help.


Author
------

- Sho SHIMIZU <osho@galibier.org>
