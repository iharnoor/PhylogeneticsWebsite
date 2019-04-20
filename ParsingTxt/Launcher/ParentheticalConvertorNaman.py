string = "(dog,((cat,cow)carrot,bird));"
stack = []
val1 = ""
val2 = ""
count = 1
Graph = {}


def createTree(v1, v2):
    global count
    global Graph

    parent = "internalNode" + str(count) + ""
    count += 1
    Graph[parent]= (v1, v2)
    # print(Graph)
    return parent[::-1]


for i in string:
    if i == ")":
        print(i.index())
        # nextChr = string[i+1]
        # if nextChr != "" || nextChr != ",":
        j = stack.pop()
        while j != ",":
            val2 += str(j)
            j = stack.pop()
        val2 = val2[::-1]
        j = stack.pop()     # skip over ","
        while j != "(":
            val1 += j
            j = stack.pop()
        val1 = val1[::-1]
        # stack.pop() # skip over the ")"
        node = createTree(val1, val2)
        val1, val2 = "", ""
        # if node[0] != str(count-1):
        #     print(node[0] + "   " + str(count-1))
        #     node.replace("internalNode"+ str(count), "")
        #     print(node)
        stack.append(node)
    else:
        stack.append(i)

print(Graph)