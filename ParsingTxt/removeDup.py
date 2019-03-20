def isDup(pairs, a, b):

    for x in pairs:
        if a in x and b in x:
            return 0
    return 1



with open("test.txt") as f:
    content = f.readlines()
result=dict()
file = open("testfile.txt","w")

for x in content:
    val = x.split()[2]
    if val in result:
        pairs = result[val]
        if isDup(pairs, x.split()[0], x.split()[1]):
            file.write(x.split()[0] + " " + x.split()[1] + " "+ val+ "\n")
            pairs.append(x.split()[0] + "," + x.split()[1])
        result[val]= pairs
    else:
        file.write(x.split()[0] + " " + x.split()[1] + " " + val + "\n")
        result[val] =[x.split()[0]+ "," + x.split()[1]]
file.close()
print(result)

