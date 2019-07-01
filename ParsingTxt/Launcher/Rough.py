def symbolReplacement(val):
    val2 = ""
    for i in val:
        asciiVal = ord(i)
        if (47 < asciiVal < 60) or (asciiVal == 0) or 96 < asciiVal < 123 or (
                64 < asciiVal < 91) or asciiVal == 40 or asciiVal == 41 or asciiVal == 44:
            val2 += i
        else:
            val.replace(i, str(asciiVal))
            val2 += "ASC" + str(asciiVal)

    return val2


print(symbolReplacement('((a+1,b-1),c+4);'))
