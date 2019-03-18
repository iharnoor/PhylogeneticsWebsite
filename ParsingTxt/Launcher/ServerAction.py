import os
import pandas as pd
import pydot
import subprocess
import numpy as np


def parseHydeToTriplets(fileName, threshold):
    dataset = pd.read_table(fileName, delim_whitespace=True, header=None,
                            names=['a0', 'a1', 'a2', 'a3', 'a4', 'a5', 'a6', 'a7'])

    datasetLess = dataset.loc[dataset['a7'] < threshold]
    datasetMore = dataset.loc[dataset['a7'] > threshold]

    # make 2 types of triplets from the dataset less than the threshold
    tripletsFromLess1 = datasetLess.values[:, [1, 3, 5]]
    tripletsFromLess2 = datasetLess.values[:, [1, 5, 3]]

    print(tripletsFromLess1)
    print(tripletsFromLess2)

    # Writing Triplets from Less in 2 parts
    np.savetxt('Triplets1.txt', tripletsFromLess1, fmt='%d', delimiter=" ")
    np.savetxt('Triplets2.txt', tripletsFromLess2, fmt='%d', delimiter=" ")

    # Working with dataset more than the threshold
    tripletsFromMore1 = datasetMore.values[:, [1, 3, 5]]
    np.savetxt('Triplets3.txt', tripletsFromMore1, fmt='%d', delimiter=" ")

    print(tripletsFromMore1)

    read_files = ['Triplets1.txt', 'Triplets2.txt', 'Triplets3.txt']

    with open("HydeToTriplets.txt", "wb") as outfile:
        for f in read_files:
            with open(f, "rb") as infile:
                outfile.write(infile.read())


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


def convertDotToPNGJulia(fileName, flag=''):
    paren = returnParentheticalFormat(fileName)
    with open("NetworkParen.net", "w") as text_file:
        text_file.write(paren)
    # write it to a file
    # then use the julia command
    cmd = 'julia plot-network.jl NetworkParen.net ' + flag
    os.system(cmd)


def removeLeavesJulia(leafNodes):
    with open("leaves.txt", "w") as text_file:
        text_file.write(leafNodes)
    # write it to a file
    # then use the julia command
    cmd = 'julia remove-leaves.jl  NetworkParen.net  leaves.txt'
    os.system(cmd)
    # convert Parenthetical format to png
    cmd = 'julia plot-network.jl reduced-net.txt'
    os.system(cmd)


def parentheticalFormatToPNG(parentheticalFormat, flag=''):
    with open("NetworkParen.net", "w") as text_file:
        text_file.write(parentheticalFormat)
    # write it to a file
    # then use the julia command
    cmd = 'julia plot-network.jl NetworkParen.net ' + flag
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

"""There is a problem in the following method. I tried the same commmand via the terminal and that generates a different
dot file as compared the same command via Python.
"""


def tripletsToDot(tripletsFName):
    cmd = 'java -jar Lev1athan.jar ' + tripletsFName + ' > cExample1.dot --nopostprocess'
    # cmd = 'java -jar Lev1athan.jar cExample1.trips > cExample1.dot --nopostprocess'
    os.system(cmd)


def removeNodes(nodes):
    output = ''
    nodes = nodes.split(',')

    with open("upload.dot", "r") as text_file:
        for line in text_file:
            try:
                for n in nodes:
                    if line.__contains__(" " + n + "\n"):
                        raise Exception()
            except Exception:
                continue
            output += line

    print(output)
    with open("upload.dot", "w+") as text_file:
        text_file.write(output)


if __name__ == '__main__':
    print("Hello")
    # tripletsToDot('cExample1.trips')
    # # convertDotToPNG('cExample1.dot')
    # # removeLeaves('1\n5')
    # convertDotToPNGJulia('cExample1.dot')
    # print("Hello")

    # lines_seen = set()  # holds lines already seen
    # outfile = open('HydeToTriplets.txt', "w")
    # for line in open('HydeToTriplets.txt', "r"):
    #     if line not in lines_seen:  # not a duplicate
    #         outfile.write(line)
    #         lines_seen.add(line)
    # outfile.close()

    # parseHydeToTriplets("results.txt", 0.05)
    # tripletsToDot('HydeToTriplets.txt')
    convertDotToPNG('cExample1.dot')

    # removeNodes('5,4,3')

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
