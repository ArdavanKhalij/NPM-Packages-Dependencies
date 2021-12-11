# NPM-Packages-Dependencies
### This project is the first assignment of software architecture in Vrije Universiteit Brussel, and it is under the supervision of Dr. Coen De Roover, Mr. Camilo Velazquez, and Mr. Ahmed Zerouali.
In this project, I got help from solutions of the **WPO exercises** and also **AKKA Stream documentations**.<br><br>
I comment as much as possible to be clear in the code, and I hope this report and the comments would be enough explanation.<br><br>
At first, I made an object and extended it with App to put the main code in it. Then I started with preparing the resource to be ready for the process. The resource file was zipped, so the first step was to unzip the file. After that, I converted the Bytestring type to string, and then because there was a NPM package name in every single line, I separated the main string and put each line in an Item of a list.<br><br>
After making the source to a suitable format, I created a case class called Package. Package has for fields:<br>
1. **Name** (A String).
2. **Version** (A ListBuffer of String).
3. **Dependencies** (A ListBuffer of Int).
4. **DevDependencies** (A ListBuffer of Int).

This case class also contains three methods:<br>
1. **get_json_and_versions:** Gets all of the versions of a NPM package.
2. **get_dependencies:** Gets the number of dependencies of each version of a NPM package.
3. **get_dev_dependencies:** Gets the number of devDependencies of each version of a NPM package.

After defining the case class, I converted the String type to Package type and instantiated it with the Package's name.<br><br>
After that, I use a flow to get the versions with the get_json_and_versions method and two other separate flows for getting dependencies and devDependencies with get_dependencies get_dev_dependencies methods.<br><br>
Two other flows are buffer and request limiter. Buffer is obvious, and request limiter is for sending a request every 3 seconds.
After having all the flows, I made the graph the way it was in the picture in the assignment:<br>
1. **Making a pipeline by using Broadcast and Zip. I made them as a flow as well to use them in the runnableGraph part.**
2. **Making two parallel pipelines with Balance and Merge. I made them as a flow as well to use them in the runnableGraph part.**

In the end, I use these flows to have a result in my Sink flow. The input of Sink flow is a duple of Packages, so I make the printing format in a way that has a suitable output. After that, I run the runnableGraph.<br><br>
I should mention that prepareDataForTheNextSteps flow is a flow that is doing everything before getting the dependencies.<br><br>
You can see the **diagram** of my code down below:<br><br>
![Untitled Diagram drawio-2](https://user-images.githubusercontent.com/44583966/144112866-fafdcba0-382b-4d63-916d-f05ca922fa8c.png)
