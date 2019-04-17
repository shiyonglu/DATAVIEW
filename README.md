# DATAVIEW
DATAVIEW (www.dataview.org) is a big data workflow management system. It uses Dropbox as the data cloud and Amazon EC2 as the compute cloud. Current research focuses on the performance and cost optimization for running workflows in clouds.



DATAVIEW supports two programing interfaces to develop and run workflows:

1. <b>JAVA API:</b> A programmer can develop various workflow tasks and workflows based on the DATAVIWE libraries. /DATAVIEW/src/test.java shows the six steps to create a customized workflow and execute it in Amazon EC2.
* The external dependecies libraries must be added to the Eclipse project from /DATAVIEW/WebContent/WEB-INF/lib
* The accessKey and secretKey should be updated in config.properties under /DATAVIEW/WebContent/workflowLibDir/
* After finishing the workflow, please terminate all the EC2 instances from your AWS account manually.

2. <b>Visual Programming</b>: DATAVIEW is deployed as a web site in Tomcat and a user can drag and drop tasks and link them into a workflow in a visual workflow design and execution environment called <b>Webbench</b>. 

* A dropbox accout is necessary to store all the input data and each tasks and the final output file when a workflow execution is finished. The user needs to create some default folders 
Dropbox/DATAVIEW/Tasks which stores the task file (class file or jar file); Dropbox/DATAVIEW/Workflows which stores the mxgraph file for the generated workflow; Dropbox/DATAVIEW-INPUT stores the input files for a workflow. 
* A local account needs to be registered to show a visualized workflow.
* A dropbox token should be provided in the main interface when you login in, which can be generated based on this tutorial:https://blogs.dropbox.com/developers/2014/05/generate-an-access-token-for-your-own-account/

<h2>Download and configure DATAVIEW as JAVA API</h2>
Check out tutorial: https://youtu.be/R6A6jreySFc or follow the instructions below:

<OL>
    <li>Download the DATAVIEW package from https://github.com/shiyonglu/DATAVIEW by clicking the "Clone or Download" button.
    </li> 
     <li> Unzip the DATAVIEW-master.zip file and import the DATAVIEW project into Eclipse as an "Existing Projects into Workspace" by selecting "Projects from Folder or Archive".
    </li> 
    <li> The external dependecies libraries must be added to the Eclipse project from /DATAVIEW/WebContent/WEB-INF/lib </li>
    <li>The accessKey(Access key ID) and secretKey(Secret access key) of Amazon EC2 should be updated in config.properties under /DATAVIEW/WebContent/workflowLibDir/ </li>
    <li>/DATAVIEW/src/test.java shows the six steps to create a new workflow and execute it in Amazon EC2.</li>
    <li>After the execution of a workflow completes, please terminate all the EC2 instances from your AWS account manually.</li>
</OL>


<h2>Download, configure, and deploy DATAVIEW as a Website</h2>
<OL>
    <li>Download the DATAVIEW package from https://github.com/shiyonglu/DATAVIEW by clicking the "Clone or Download" button.
    </li> 
   
    
</OL>



<h2>DATAVIEW Tutorials</h2>
<OL>
    <li>An introduction to DATAVIEW （https://youtu.be/cWQ2imhUzRg) </li>
    <li> How to download and import DATAVIEW into Eclipse as Java API (https://youtu.be/R6A6jreySFc)</li>
    <li> How to create a workflow task for DATAVIEW (the linear regression example) (https://youtu.be/BPaoR_zogPA)</li>
    <li> How to create a workflow task in Python (https://youtu.be/3vSx-g9FnZU)</li>
    <li> How to create a workflow task for DATAVIEW (the K-means example) (https://youtu.be/N4jIYbYSFd4) </li>
    <li> How to create a workflow in DATAVIEW (the word count example) (https://youtu.be/73-fyXyImeI) </li>
    <li> How to create a workflow in DATAVIEW (the distributed K-means workflow example) (https://youtu.be/aQJPzdQQ3Uc)</li>
    <li> How to create a workflow in DATAVIEW (the word count example revisited) (https://youtu.be/U8mhL9vVXlM)</li>
    <li> How to create a workflow in DATAVIEW (the distributed K-means workflow example revisited) (https://youtu.be/QLN8q9Hg1eE)</li>
    <li> How to generate a random workflow and then visualize it (https://youtu.be/aQPIhe2ZnzU)</li>
    <li> How to debug the functionality of a task (https://youtu.be/N4jIYbYSFd4)</li>
    <li> How to use Dataview.debugger to debug your DATAVIEW applications (https://youtu.be/1d1vJRGPBYs) </li>
    <li> How to develop a new workflow planner (https://youtu.be/R0i2s-LkGV8) </li>
</OL>

