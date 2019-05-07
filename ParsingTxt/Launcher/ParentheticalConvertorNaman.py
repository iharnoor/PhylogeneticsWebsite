import re

stack = []
count = 1001
Graph = {}


def createTreeLabel(valueList, label):
    global Graph

    parent = "internal" + label
    Graph[parent] = tuple(valueList)
    return parent


def createTree(valueList):
    global count
    global Graph

    parent = "internal" + str(count) + ""
    count += 1
    Graph[parent] = tuple(valueList)
    # print(Graph)
    return parent


def returnDictionary(newick):
    global Graph
    val = ""
    parent = ""
    i = 0
    while i < len(newick) - 1:
        if newick[i] == ")":
            nextChr = newick[i + 1]
            increment = 1
            while nextChr != ',' and nextChr != ')' and nextChr != ';' and (i + increment) < len(newick):
                parent += nextChr
                increment += 1
                nextChr = newick[i + increment]

            j = stack.pop()
            while j != "(":
                val += j
                j = stack.pop()
            val += j
            val = val[::-1]
            listVals = readParameters(val)

            if increment != 1:
                node = createTreeLabel(listVals, parent)
                i += (increment - 1)
            else:
                node = createTree(listVals)
            listVals = []
            val = ""
            parent = ""

            stack.append(node)
        else:
            stack.append(newick[i])
        i += 1
    return Graph


def readParameters(input):
    # print("input: " + input)
    value = ""
    listOfvals = []
    for i in input[::-1]:
        if i == ",":
            if value.find("internal") < 0:
                value = value[::-1]
            listOfvals.append(value)
            value = ""
        elif i == "(":
            if value.find("internal") < 0:
                value = value[::-1]
            listOfvals.append(value)
            value = ""
        else:
            value += i
    # print(listOfvals)
    return listOfvals


def dictToDot(dict):
    global count
    global Graph
    global stack
    # print("count: " + str(count))
    # print('diction: ', dict)
    dotString = 'strict digraph G1 {' + '\n'

    for key, value in dict.items():
        if key == "internal" + str(count - 1):
            key = "internal1000"
        elif key.find("#") > -1:
            key = "" + key
            key = key.replace("internal#", "Hash")
            key = key.replace("#", "Hash")
        for i in value:
            if i.find("#") > -1:
                i = "" + i
                i = i.replace("internal#", "Hash")
                i = i.replace("#", "Hash")
            dotString += key + ' -> ' + i + '\n'

    dotString += '}'
    print(dotString)

    with open('upload.dot', 'w') as f:
        f.write(dotString)
    stack = []
    count = 1001
    Graph = {}



def newickToDot(newick):
    newick = re.sub(":[0-9]+[eE1\-\.]*[0-9]*", "", newick)
    diction = returnDictionary(newick)
    dictToDot(diction)
    # print(diction)
    global Graph, count, stack
    count = 1000
    Graph = {}
    stack = []


if __name__ == '__main__':
    newick = "((a-2,b-3),c-4);"
    # newick = "((A:1, B:4.1):4, C:5e-1);"

    print(newick)
    newickToDot(newick)
