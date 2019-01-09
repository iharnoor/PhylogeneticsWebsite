import os

import pydot


def returnParentheticalFormat(fileName):
    parentheticalFormat = ''
    for line in reversed(list(open(fileName))):
        if line.__contains__('eNewick'):
            parentheticalFormat = line[28:]
            # print(parentheticalFormat)
            # print(line.rstrip())
    return parentheticalFormat.rstrip()


"""Tested with the nopostprocess option even with the direct Heuristic files but doesn't seem to work"""


def tripletsToDot(tripletsFName):
    # cmd = 'java -jar Lev1athan.jar ' + tripletsFName + ' > cExample1.dot --nopostprocess'
    cmd = 'java Heuristic  cExample1.trips > example.out --nopostprocess'
    os.system(cmd)


def convertDotToPNG(fileName):
    (graph,) = pydot.graph_from_dot_file(fileName)
    graph.write_png('cExample1.png')


if __name__ == '__main__':
    tripletsToDot('cExample1.trips')
    convertDotToPNG('cExample1.dot')
