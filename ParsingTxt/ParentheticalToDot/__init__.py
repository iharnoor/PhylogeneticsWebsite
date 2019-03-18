import Launcher.ServerAction

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

    with open('actualLabels.dot', 'w+') as fileName:
        fileName.write(dotFormat)
    return dotFormat


def convertNewickToDot(parenthetical):
    cmd = 'echo \"' + parenthetical + '\" | ./a.out > converted.dot'
    os.system(cmd)


if __name__ == '__main__':
    convertNewickToDot('((a_2,b_3),c_4);')
    convertDotLabelsToActualLabels('converted.dot')

    print(returnReducedDotFile('actualLabels.dot'))
