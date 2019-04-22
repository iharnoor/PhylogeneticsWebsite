stack = []
count = 1000
Graph = {}


def createTree(v1, v2):
    global count
    global Graph

    parent = "internal" + str(count) + ""
    count += 1
    Graph[parent] = (v1, v2)
    # print(Graph)
    return parent[::-1]


def createTreeLabel(v1, v2, label):
    global Graph

    parent = label
    Graph[parent] = (v1, v2)
    return parent[::-1]


def returnDictionary(newick):
    global Graph
    val1 = ""
    val2 = ""
    parent = ""
    length = len(newick) - 1
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
            while j != ",":
                val2 += str(j)
                j = stack.pop()
            val2 = val2[::-1]
            j = stack.pop()  # skip over ","
            while j != "(":
                val1 += j
                j = stack.pop()
            val1 = val1[::-1]

            if increment != 1:
                node = createTreeLabel(val1, val2, parent)
                i += (increment - 1)
            else:
                node = createTree(val1, val2)
            val1, val2 = "", ""
            parent = ""

            stack.append(node)
        else:
            stack.append(newick[i])
        i += 1
    return Graph


def dictToDot(dict):
    print('diction: ', dict)
    dotString = 'strict digraph G1 {' + '\n'

    for key, value in dict.items():
        for i in value:
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
    newick = "(((A,C),D),E);"
    newickToDot(newick)
