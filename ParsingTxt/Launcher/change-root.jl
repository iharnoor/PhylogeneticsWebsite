## Julia script to change the root of a network
## run this script in the terminal as:
## $ julia change-root.jl file flag newroot
## Input:
## - file: textfile with the network as parenthetical format
## - flag: "node", "edge", or "outgroup" depending whether the next argument corresponds to the
##          node number, edge number or leaf name respectively
## - newroot: string or integer denoting the leaf name to serve as outgroup, or the node/edge number
##            of the new root
## Output:
## The script produces a new text file with the parenthetical format of the newly rooted network: new-net.txt

## Example:
## julia change-root.jl test.net outgroup 3

## Note: This script does not allow to choose an edge number for now, but it would be a good
## option for the future

## Note 2: Not sure how to handle potential errors. I am handling them inside
## the script by throwing a warning, not an error, but not sure which is the best
## way to do this given the interaction with python


using PhyloNetworks, PhyloPlots

if isempty(ARGS)
    error("need textfile name containing network in parenthetical format")
else
    length(ARGS) == 3 || error("need two arguments for this script: textfile with parenthetical formal, outgroup/node/edge flag, and outgroup/node/edge number for the new root")
    file = ARGS[1]
    flag = ARGS[2]
    newroot = ARGS[3]
end

if flag == "outgroup"
    newroot = string(newroot)
end

try
    net = readTopology(file)
catch
    warn("cannot read this parenthetical file!")
end

net = readTopology(file)

try
    if flag == "edge" ##rooting on edge number
        rootonedge!(net,newroot)
    else ##rooting on node number, or outgroup
        rootatnode!(net,newroot)
    end
catch
    warn("cannot place this new root: maybe not compatible with hybrid edges?")
end

writeTopology(net, "new-net.txt")

