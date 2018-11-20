import os
import pydot
import subprocess

# out = subprocess.Popen(['wc', '-l', 'TripCombo.txt'],
#                        stdout=subprocess.PIPE,
#                        stderr=subprocess.STDOUT)
#
# stdout, stderr = out.communicate()
# print(stdout)


os.environ["PATH"] += os.pathsep + 'C:/Program Files (x86)/Graphviz2.38/bin/'


def convertDotToPNG(fileName):
    (graph,) = pydot.graph_from_dot_file(fileName)
    graph.write_png('cExample1.png')


def tripletsToDot(tripletsFName):
    cmd = 'java -jar Lev1athan.jar ' + tripletsFName + ' > cExample1.dot'
    os.system(cmd)


if __name__ == '__main__':
    tripletsToDot('cExample1.trips')
    convertDotToPNG('cExample1.dot')

#  1) Return Parenthetical fiel and return the image
# Download image or text file
# Two buttons one for image other for text
#  Filtering the graph
# Remove Leaf Nodes
# Change Root and checbox to filter leaves
