## Using VSCode to compile on Windows
Dependencies:
- [VSCode](https://code.visualstudio.com/)
- [Java extension pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Maven](https://maven.apache.org/)

Extract maven to ```C:\Program Files\Java``` and add ```C:\Program Files\Java\apache-maven-X.X.X\bin``` to your path.

Create a new User Variable ```JAVA_HOME``` and point it to ```C:\Program Files\Java\jdk-X.X.X```

Open VSCode and open the project folder. In the lower left corner under maven, right click the plugin, run custom, and run each of the following commands
```
dependency:sources
dependency:resolve -Dclassifier=javadoc
eclipse:eclipse
```
Then run install to download dependencies and build the plugin.

References:
- https://www.spigotmc.org/wiki/creating-a-blank-spigot-plugin-in-vs-code/
- https://stackoverflow.com/questions/2059431/get-source-jars-from-maven-repository
