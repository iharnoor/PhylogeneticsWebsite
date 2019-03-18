## Julia script to reduce the leaf set of a network
## by deleting leaves.
## run this script in the terminal as:
## $ julia plot-network.jl file leaves
## Input:
## - file: textfile with the network as parenthetical format
## - leaves: textfile with list of leaves to include (one per row)
##
## Output:
## The script produces a new text file with the parenthetical format of the
## new network: reduced-net.txt

## Note: Not sure how to handle potential errors. I am handling them inside
## the script by throwing an error, but not sure which is the best
## way to do this given the interaction with python



using PhyloNetworks, PhyloPlots

if isempty(ARGS)
    error("need textfile name containing network in parenthetical format")
else
    length(ARGS) == 2 || error("need two arguments for this script: textfile with parenthetical formal, and outgroup/node number for the new root")
    file = ARGS[1]
    leaves = ARGS[2]
end


try
    net = readTopology(file)
catch
    error("cannot read this parenthetical file!")
end

net = readTopology(file)


try
    lines = readlines(leaves)
catch
    error("cannot read file with list of leaves")
end

lines = readlines(leaves)

leafset = tipLabels(net)
removeleaves = setdiff(leafset,lines)

for l in removeleaves
    deleteleaf!(net,l)
end

writeTopology(net,"reduced-net.txt")
