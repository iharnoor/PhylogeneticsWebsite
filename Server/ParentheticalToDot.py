import os


def returnReducedDotFile(fileName):
    dotFormat = ''
    for line in list(open(fileName)):
        if line.__contains__('{') or line.__contains__('}') or line.__contains__('->'):
            dotFormat += line
    return dotFormat.rstrip()


def convertDotLabelsToActualLabels(fileName):
    dotFormat = ''
    dict = {}
    for line in list(open(fileName)):
        dotFormat += line
        if line.__contains__('label=') and not line.__contains__('label=""'):
            key = line[:line.index('[')]
            value = line[line.index("\"") + 1: line.index("]") - 1]
            dict[key] = value

    for (key, value) in dict.items():
        dotFormat = dotFormat.replace(key, value)

    print(dotFormat)

    with open('../ParentheticalToDot/actualLabels.dot', 'w+') as fileName:
        fileName.write(dotFormat)
    return dotFormat


def convertNewickToDot(parenthetical):
    cmd = 'echo \"' + parenthetical + '\" | ../ParentheticalToDot/a.out > ../ParentheticalToDot/converted.dot'
    os.system(cmd)


def newickToActualLabels(newick):
    convertNewickToDot(newick)
    convertDotLabelsToActualLabels('../ParentheticalToDot/converted.dot')
    return returnReducedDotFile('../ParentheticalToDot/actualLabels.dot')


if __name__ == '__main__':
    print(newickToActualLabels('((a_2,b_3),c_4);'))
