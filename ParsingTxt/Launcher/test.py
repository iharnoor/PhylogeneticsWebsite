string = "(dog,((cat,cow),bird));"
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

# def traceBack():
#
#
#
# def recursion (index, symb):
#     if string[index] == symb:
#         traceBack()
#     else:
#         stack.append(string[index])
#         recursion(index+1, symb)



for i in string:
    if i == ")":

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
        stack.append(node)
    else:
        stack.append(i)

print(Graph)