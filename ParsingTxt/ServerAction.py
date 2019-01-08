import os
import pydot
import subprocess


# out = subprocess.Popen(['wc', '-l', 'TripCombo.txt'],
#                        stdout=subprocess.PIPE,
#                        stderr=subprocess.STDOUT)
#
# stdout, stderr = out.communicate()
# print(stdout)

def returnParentheticalFormat(fileName):
    parentheticalFormat = ''
    for line in reversed(list(open(fileName))):
        if line.__contains__('eNewick'):
            parentheticalFormat = line[28:]
            # print(parentheticalFormat)
            # print(line.rstrip())
    return parentheticalFormat.rstrip()


def convertDotToPNG(fileName):
    (graph,) = pydot.graph_from_dot_file(fileName)
    graph.write_png('cExample1.png')


def convertDotToPNGJulia(fileName):
    paren = returnParentheticalFormat(fileName)
    with open("NetworkParen.net", "w") as text_file:
        text_file.write(paren)
    # write it to a file
    # then use the julia command
    cmd = 'julia plot-network.jl NetworkParen.net'
    os.system(cmd)


def removeLeaves(leafNodes):
    with open("leaves.txt", "w") as text_file:
        text_file.write(leafNodes)
    # write it to a file
    # then use the julia command
    cmd = 'julia .\\remove-leaves.jl .\\NetworkParen.net .\\leaves.txt'
    os.system(cmd)
    # convert Parenthetical format to png
    cmd = 'julia plot-network.jl reduced-net.txt'
    os.system(cmd)


# assumes that the triplets are in the parenthetical format in hte NetworkParen.net text file
# converts
def changeRoot(flag, newRoot):
    cmd = 'julia change-root.jl NetworkParen.net ' + flag + ' ' + newRoot + ' '
    os.system(cmd)
    # convert Parenthetical format to png
    cmd = 'julia plot-network.jl new-net.txt'
    os.system(cmd)


# julia change-root.jl test.net outgroup 3

def tripletsToDot(tripletsFName):
    cmd = 'java -jar Lev1athan.jar ' + tripletsFName + ' > cExample1.dot'
    os.system(cmd)


if __name__ == '__main__':
    print("Hello")
    tripletsToDot('cExample1.trips')
    # removeLeaves('1\n4')
    convertDotToPNGJulia('cExample1.dot')
    print("Hello")

    changeRoot('outgroup', '3')

#  1) Return Parenthetical fiel and return the image
# Download image or text file
# Two buttons one for image other for text
#  Filtering the graph
# Remove Leaf Nodes
# Change Root and checbox to filter leaves

# checkbox with all the leaves and uncheck the nodes

# last line if you use please site the following
# manuscript
# Download button to

# print parenthetical format instead of triplets
# estimate of the time .
# It won't finish in less than 20 minutes
# Go get a coffee
