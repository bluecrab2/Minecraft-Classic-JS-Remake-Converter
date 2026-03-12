# Minecraft Classic JS Remake Converter
This is a tool to convert any Minecraft Pre-Classic or Classic Minecraft world into the [2019 JavaScript Remake of Minecraft Classic](https://minecraft.wiki/w/Minecraft_Classic_(JavaScript_remake)). This JavaScript remake was released on May 7, 2019 to celebrate Minecraft's 10th anniversary. It is available at [classic.minecraft.net](classic.minecraft.net) and [https://omniarchive.uk/23a-js/](https://omniarchive.uk/23a-js/).

## Instructions
### Conversion
This program requires [Java 8](https://www.java.com/en/download/manual.jsp) or later to be installed.

Download the JAR file from the Releases section of GitHub.

Place the JAR file in the same folder as your Classic or Pre-Classic .dat or .mine file. (These are saved to your Minecraft instance folder)

Run the JAR by double-clicking it. It will convert all .dat and .mine files in its folder into a corresponding .json file. The .json file contains the world converted to Classic JS format.

### Running World on Web Browser
Once you have your world's JSON file, you need to copy it into your browser. This JSON file will be likely be very large (~112 MB for a 256x64x256 world). Don't attempt to open that file in an advanced text editor like Notepad++ or VS Code because the file size will likely crash these programs. Instead, use a very simple text editor like Notepad on Windows. Select all text (CTRL + A) and copy it (CTRL + C).

Because of the large text size, pasting this string into most web browser results in them crashing. A fix is known **only on the Firefox browser** that allows the browser to handle the large JSON string. On Firefox, visit [about:config](about:config), and change the value for `dom.storage.default_quota`. This value limits how much data a site can use in kilobytes. Set this value to be larger than your JSON file's size. For example, if the JSON is 114,000 kilobytes set to dom.storage.default_quota to over 114,000 (you can add extra space to be safe, so 200,000 would be a valid value).

Visit [classic.minecraft.net](classic.minecraft.net). Open the developer menu (with F12) and enter Storage > Local Storage. The `savedGame` value stores the world. Double-click its value to edit it, and paste in the large JSON. It may take some time for the paste to complete. Once it has finished, refresh the page and when you return you should be able to play on your world!

## Video Tutorial
[![Minecraft Classic JS Remake Converter Tutorial](https://img.youtube.com/vi/UEe7rUwB3E0/0.jpg)](https://www.youtube.com/watch?v=UEe7rUwB3E0)

## Extra Information
### Axis Flip
In the original Pre-Classic and Classic, the coordinate system is right-handed. In the Classic JavaScript remake, a left-handed coordinate system is used. If the original coordinates are converted to Classic JavaScript, the world is mirrored. By default, this program flips the x-axis during conversion to prevent this mirroring. However, in some sense not flipping the axis is a "more faithful/vanilla" conversion since it keeps each block's original coordinates. The `-no_flip` option on the command line can be used to not flip the x-axis. (Difference pictured below)
![Demonstration of the different coordinate system in Java vs. JavaScript.](media/JavaScript%20Axis%20Flip.png)

### Pre-Classic and Classic Reading
This tool was built off the Pre-Classic and Classic file reading from my [ClassicExplorer](https://github.com/bluecrab2/ClassicExplorer) tool. That allows it to handle any Pre-Classic or Classic world from the first available, rd-132211, to the last, Classic 0.30. Only blocks and world size are used during conversion since the Classic JavaScript world does not save entities or any other information contained in the Classic files.

### Classic JavaScript World Format
The Minecraft JavaScript Classic remake world is saved in the `savedGame` local variable in the web browser. This allows the world to persist even after leaving and returning to the website. The world is saved in a JSON with four fields:
| Key | Value |
| --- | ----- |
| worldSeed | The seed that determines world generation |
| changedBlocks | Only the blocks that changed after generation are saved. Each block includes its position and ID. |
| worldSize | The size of the world in the x and z direction. The actual world size is 1 less than the worldSize value. E.g. worldSize=128 -> 127x127 world. |
| version | Unused value always set to 1 |

## Run on Command Line
By default, just running the JAR will convert all worlds in its folder for ease of use. However, if you run the JAR on command line, it gives you a few more options on how to run.

Example usage: `java -jar JavaScriptMCConverter.jar -i level.dat -o level.json -no_flip`

| Parameter      | Description                                                         |
|----------------|---------------------------------------------------------------------|
| -h or -help    | print help menu                                                     |
| -i or -input   | followed by the input file path                                     |
| -o or -output  | followed by the output file path                                    |
| -no_flip       | do not flip the x-axis during conversion, keep original coordinates |