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
        if key == "internal" + str(count - 1):
            key = "internal1000"
        elif key.find("#") > -1:
            key = "" + key
            key = key.replace("internal", "")
        for i in value:
            if i.find("#") > -1:
                i = i.replace("internal", "")
                i = "" + i
            dotString += key + ' -> ' + i + '\n'

    dotString += '}'
    print(dotString)

    with open('upload.dot', 'w') as f:
        f.write(dotString)


def newickToDot(newick):
    diction = returnDictionary(newick)
    dictToDot(diction)
    # print(diction)
    global Graph, count
    count = 1000
    Graph = {}


if __name__ == '__main__':
    newick = "(((a,(b)#H),(#H,c)),d);"
    # newick = "((A:1, B:4.1):4, C:5e-1);"
    newick = re.sub(":[0-9]+[e1\-\.]*","", newick)
    print(newick)
    newickToDot(newick)
