# Integration Test Instructions

To perform the integration test:

1. Install the Server software provided by the prof on MyCourses, FOLLOW THE INSTRUCTIONS GIVEN WITH IT TO ADD THE JAR TO THE PROJECT.
2. Connect both the server computer (the one running the software) and the robot to the same network
3. Get the code from the integration-test branch on the git repository (don't copy and paste it, you need the whole thing)
4. load on the program on the brick

*To run the server software, open a command line and type:*  
`java -jar [name of program]`

If that doesn't work on windows then google it.

The execution of the program won't start until the data is passed from the server to the robot.

* To use the zipline, set the Green team number to 6 (our number)
* Set green team's starting corner to the desired corner (0 being [1,1])
* Set the ZC_G point to the actual starting point of the zip line
* Set the ZO_G point to the point in front of the zipline, where the robot will localize
* Set the ZC_R point to the actual end point of the zip line
* Set the ZO_R point to the point where the robot will relocalize
* Set the SR_UR point to the point where you want to robot to go after it crossed the zip line
    * This point is the upper right corner of the red team's search zone
* You can write anything for the rest.

*NOTE: The robot will pause and wait for a button press after each major step*
