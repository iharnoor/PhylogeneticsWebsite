import pandas as pd
import numpy as np

# importing the datasets 4,5
# Total 8 Columns

threshold = 0.0005

dataset = pd.read_table("sig.results.txt", delim_whitespace=True, header=None,
                        names=['a0', 'a1', 'a2', 'a3', 'a4', 'a5', 'a6', 'a7'])

datasetLess = dataset.loc[dataset['a7'] < threshold]
datasetMore = dataset.loc[dataset['a7'] > threshold]

# make 2 types of triplets from the dataset less than the threshold
tripletsFromLess1 = datasetLess.values[:, [1, 3, 5]]
tripletsFromLess2 = datasetLess.values[:, [1, 5, 3]]

print(tripletsFromLess1)
print(tripletsFromLess2)

# Writing Triplets from Less in 2 parts
np.savetxt('Triplets1.txt', tripletsFromLess1, fmt='%d', delimiter="\t")
np.savetxt('Triplets2.txt', tripletsFromLess2, fmt='%d', delimiter="\t")

# Working with dataset more than the threshold
tripletsFromMore1 = datasetMore.values[:, [1, 3, 5]]
np.savetxt('Triplets3.txt', tripletsFromMore1, fmt='%d', delimiter="\t")

read_files = ['Triplets1.txt','Triplets2.txt','Triplets3.txt']

with open("result.txt", "wb") as outfile:
    for f in read_files:
        with open(f, "rb") as infile:
            outfile.write(infile.read())

# TODO : append these to the bottom of file1 and make the selection random (135 or 153 randomly) for Triplets more than the threshold.

# np.savetxt('Triplets3.txt', tripletsFromLess1, fmt='%d', delimiter="\t")
