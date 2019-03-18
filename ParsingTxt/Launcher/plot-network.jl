## Julia script to plot a network from parenthetical format
## run this script in the terminal as:
## $ julia plot-network.jl file flag
## Input:
## - file: textfile with the network as parenthetical format
## - flag: could be empty, or a string: "nodes" (prints node numbers), "edges" (prints edge numbers),
##         "both" (prints node and edge numbers)
## Output:
## The script produces the output file net.png with the network plot.

## Note: the plot function has many many options, so it would be ideal to
## find a way to run the function directly from python, instead of using a script
## because it is difficult to have flexibility to the plot function options through
## through command-line arguments

## Note 2: Not sure how to handle potential errors. I am handling them inside
## the script by throwing a warning, not an error, but not sure which is the best
## way to do this given the interaction with python


using PhyloNetworks, PhyloPlots

if isempty(ARGS)
    error("need textfile name containing network in parenthetical format")
else
    file = ARGS[1]
    if length(ARGS) > 1
        flag = ARGS[2]
    else
        flag = ""
    end
end


try
    net = readTopology(file)
catch
    warn("cannot read this parenthetical file!")
end

net = readTopology(file)


showNodeNum = (flag == "nodes") || (flag == "both")
showEdgeNum = (flag == "edges") || (flag == "both")


## using R to plot the network
using RCall
@rput net
R"""
library(ape)
png("net.png")
plot(net, use.edge.length=FALSE,cex=1.6)
"""
if showNodeNum
    R"nodelabels(cex=1.5)"
end
if showEdgeNum
    R"edgelabels(cex=1.5)"
end
R"dev.off()"
