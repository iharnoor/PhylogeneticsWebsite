stack = []
count = 1001
Graph = {}



def createTreeLabel(valueList, label):
    global Graph

    parent = "internalNode_" + label
    Graph[parent] = tuple(valueList)
    return parent

def createTree(valueList):
    global count
    global Graph

    parent = "internal_" + str(count) + ""
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
            listOfvals.append(value)
            value = ""
        elif i == "(":
            listOfvals.append(value)
            value = ""
        else:
            value += i
    # print(listOfvals)
    return listOfvals

def dictToDot(dict):
    global count
    # print("count: " + str(count))
    # print('diction: ', dict)
    dotString = 'strict digraph G1 {' + '\n'

    for key, value in dict.items():
        if key == "internal_" + str(count-1):
            key = "internal_1000"
        for i in value:
            # if i == "internal_" + str(count):
                # i = "internal_1000"
            dotString += key + ' -> ' + i + '\n'

    dotString += '}'
    print(dotString)

    with open('upload.dot', 'w') as f:
        f.write(dotString)


def newickToDot(newick):
    diction = returnDictionary(newick)
    dictToDot(diction)
    global Graph, count
    count = 1000
    Graph = {}


if __name__ == '__main__':
    newick = "(((A,B),C),D);"
    newickToDot(newick)

