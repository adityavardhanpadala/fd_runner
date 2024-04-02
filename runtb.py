import subprocess
import glob

# java -jar .\out\artifacts\flowdroid_pt_jar\flowdroid_pt.jar "C:\Users\padal\android-platforms" "tbss.txt" "tw.txt" "taintbench\backflash.apk"^C


#Ruun FlowDroid on all files in taintbench dir

def main():
    for file in glob.glob("taintbench/*.apk"):
        print("Running FlowDroid on " + file)
        subprocess.run(["java", "-jar", ".\\out\\artifacts\\flowdroid_pt_jar\\flowdroid_pt.jar", "C:\\Users\\padal\\android-platforms", "tbss.txt", "tw.txt", file])


main()