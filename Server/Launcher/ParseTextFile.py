def returnParentheticalFormat(fileName):
    parentheticalFormat = ''
    for line in reversed(list(open(fileName))):
        if (line.__contains__('eNewick')):
            parentheticalFormat = line[28:]
            # print(parentheticalFormat)
            # print(line.rstrip())
    return parentheticalFormat.rstrip()


if __name__ == '__main__':
    paren = returnParentheticalFormat("cExample.dot")
    # print(paren[:len(paren)])
    print(paren)
