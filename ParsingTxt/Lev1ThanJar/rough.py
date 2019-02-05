def returnReducedDotFile(fileName):
    parentheticalFormat = ''
    for line in list(open(fileName)) :
        if line.__contains__('{') or line.__contains__('}') or line.__contains__('->'):
            parentheticalFormat += line
    return parentheticalFormat.rstrip()


if __name__ == '__main__':
    paren = returnReducedDotFile("cExample1.dot")
    # print(paren[:len(paren)])
    print(paren)
