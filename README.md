# DATAVIEW
DATAVIEW (www.dataview.org) is a big data workflow management system. It uses Dropbox as the data cloud and Amazon EC2 as the compute cloud. Current research focuses on the performance and cost optimization for running workflows in clouds.



DATAVIEW supports two programing interfaces to develop and run workflows:

1. <b>JAVA API:</b> A programmer can develop various workflow tasks and workflows based on the DATAVIWE libraries. /DATAVIEW/src/test.java shows the six steps to create a customized workflow and execute it in Amazon EC2.
* The external dependecies libraries must be added to the Eclipse project from /DATAVIEW/WebContent/WEB-INF/lib
* The accessKey and secretKey should be updated in config.properties under /DATAVIEW/WebContent/workflowLibDir/
* After finishing the workflow, please terminate all the EC2 instances from your AWS account manually.

2. <b>Visual Programming</b>: DATAVIEW is deployed as a Web site in Tomcat and a user can drag and drop tasks and link them into a workflow in a visual workflow design and execution environment called <b>Webbench</b>. 

* A dropbox accout is necessary to store all the input data,  workflow tasks, the final output files produced by the workflow execution. The user needs to create <b>Three</b> default folders 
Dropbox/DATAVIEW/Tasks,  which stores the task file (class file or jar file); Dropbox/DATAVIEW/Workflows, which stores the mxgraph file for the generated workflow; Dropbox/DATAVIEW-INPUT, which stores the input files for a workflow. 
Four relational algebra tasks (jar files) and input files are already stored under the DATAVIEW/WebContent/workflowTaskDir folder. 
* A local account needs to be registered to show a visualized workflow.
* A dropbox token should be provided in the main interface when you login in, which can be generated based on this tutorial:https://blogs.dropbox.com/developers/2014/05/generate-an-access-token-for-your-own-account/

<h2>Download and configure DATAVIEW as JAVA API</h2>
Check out tutorial: https://youtu.be/xJikeWptYSw or follow the instructions below: 

<OL>
    <li>Download the DATAVIEW package from https://github.com/shiyonglu/DATAVIEW by clicking the "Clone or Download" button.
    </li> 
     <li> Unzip the DATAVIEW-master.zip file and import the DATAVIEW project into Eclipse as an "Existing Projects into Workspace" by selecting "Projects from Folder or Archive".
    </li> 
    <li> The external dependecies libraries must be added to the Eclipse project from /DATAVIEW/WebContent/WEB-INF/lib </li>
    <li>/DATAVIEW/src/test.java shows the six steps to create a new workflow and execute it with local executor.</li>
   <!--
   <li> Create an Access key ID and a Secret access key in Amazon EC2 following the tutotial: https://youtu.be/9741e4CubMQ </li>
    <li>Replace the accessKey(Access key ID) and the secretKey(Secret access key) in config.properties by the Access key ID and Secret access key created in the previous step. File config.properties is under /DATAVIEW/WebContent/workflowLibDir/. </li>
    <li>/DATAVIEW/src/test.java shows the six steps to create a new workflow and execute it in Amazon EC2.</li>
    <li>After the execution of a workflow completes, please terminate all the EC2 instances from your AWS account manually.</li>
    -->
</OL>


<h2>Download, configure, and deploy DATAVIEW as a Website</h2>
Check out tutorial: https://youtu.be/7Sz4PSD6Cs or follow the instructions below: 
<OL>
    <li> Follow the first three steps from <h3>Download and configure DATAVIEW as JAVA API</h3> </li>
    <li>  Create three default folders Dropbox/DATAVIEW/Tasks, which stores the task file (class file or jar file); Dropbox/DATAVIEW/Workflows, which stores the mxgraph file for the generated workflow; Dropbox/DATAVIEW-INPUT, which stores the input files for a workflow in your dropbox. </li>
    <li> Get a dropbox token. </li>
</OL>



<h2>DATAVIEW Tutorials</h2>
<OL>
    <li> Chapter 1: A gentle introduction to DATAVIEW （https://youtu.be/7S4iGKXpaAc) </li>
    <li> How to download, import DATAVIEW into Eclipse as Java API and run a workflow with local executor (https://youtu.be/xJikeWptYSw)</li>
    <li> How to create a relational algebra workflow in DATAVIEW through the interface (https://youtu.be/AQw0S_QO8zg) </li>
    <!--
    <li> How to download and import DATAVIEW into Eclipse as Java API (https://youtu.be/R6A6jreySFc)</li>
    <li> How to create an Access Key ID and a Secret access key in Amazon EC2 (https://youtu.be/9741e4CubMQ)</li>
    <li> How to create a workflow task for DATAVIEW (the linear regression example) (https://youtu.be/BPaoR_zogPA)</li>
    <li> How to create a workflow task in Python (https://youtu.be/3vSx-g9FnZU)</li>
    <li> How to create a workflow task for DATAVIEW (the K-means example) (https://youtu.be/N4jIYbYSFd4) </li>
    <li> How to create a workflow in DATAVIEW (the word count example) (https://youtu.be/x1f8UgyShtI) </li>
    <li> How to create a workflow in DATAVIEW (the distributed K-means workflow example) (https://youtu.be/aQJPzdQQ3Uc)</li>
    <li> How to create a workflow in DATAVIEW (the word count example revisited) (https://youtu.be/U8mhL9vVXlM)</li>
    <li> How to create a workflow in DATAVIEW (the distributed K-means workflow example revisited) (https://youtu.be/QLN8q9Hg1eE)</li>
    <li> How to generate a random workflow and then visualize it (https://youtu.be/aQPIhe2ZnzU)</li>
    <li> How to debug the functionality of a task (https://youtu.be/N4jIYbYSFd4)</li>
    <li> How to use Dataview.debugger to debug your DATAVIEW applications (https://youtu.be/1d1vJRGPBYs) </li>
    <li> How to develop a new workflow planner (https://youtu.be/R0i2s-LkGV8) </li>
    <li> An introduction to WowkrlfowExecutor_Beta (<a href="https://www.youtube.com/watch?v=kBIcxWyJgQA&t=2726s">part 1</a>
        | <a href="https://www.youtube.com/watch?v=Km24otM3rEM&t=582s">part 2</a>)
        -->
</OL>

