# DATAVIEW
DATAVIEW (www.dataview.org) is a big data workflow management system. It uses Dropbox as the data cloud and Amazon EC2 as the compute cloud. Current research focuses on the performance and cost optimization for running workflows in clouds.



This current project has two methods to create a workflow and execute this workflow on Amazon EC2.
* Pure java version: /DATAVIEW/src/test.java shows the six steps to create a cusomized workflow and execute in Amazon eC2.
⋅⋅⋅The accessKey and secretKey should be updated in config.properties under /DATAVIEW/WebContent/workflowLibDir/
* Interface version: The project can run in tomcat as a dynamic web project with the login.jsp.

⋅⋅⋅ A dropbox accout is necessary to store all the input data and each tasks and the final output file when a workflow execution is finished. The user needs to create some default folders 
Dropbox/DATAVIEW/Tasks which stores the task file (class file or jar file); Dropbox/DATAVIEW/Workflows which stores the mxgraph file for the generated workflow; Dropbox/DATAVIEW-INPUT stores the input files for a workflow.
⋅⋅⋅ A local account needs to be registered to show a visualized workflow.



Check out DATAVIEW tutorials at: https://bigdataworkflow.weebly.com/tutorials.html