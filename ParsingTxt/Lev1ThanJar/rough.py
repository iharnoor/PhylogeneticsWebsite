import os
import json
import queue
import argparse
import threading
import pygraphviz as pgv

try:
    import queue
except ImportError:
    import Queue as queue


class Dot2JSON_BFS(threading.Thread):
    # ----------------------------------------------------------------------
    def __init__(self, file_queue):
        threading.Thread.__init__(self)
        self.file_queue = file_queue
        self.nodeq = queue.Queue()
        self.traversed_nodes = []

    def do_bfs(self, fname):
        self.G = pgv.AGraph(fname)
        self.cur_node = self.G.graph_attr['root']

        nodeq = self.nodeq
        cur_node = self.cur_node
        traversed_nodes = self.traversed_nodes
        G = self.G

        self.d = {"name": cur_node, "children": []}

        # self.d[cur_node] = {}
        # cur_list = self.d[cur_node]
        cur_list = self.d["children"]
        nodeq.put((cur_node, cur_list))

        while not nodeq.empty():
            (cur_node, cur_list) = nodeq.get(block=True)
            if cur_node not in traversed_nodes:

                traversed_nodes.append(cur_node)

                for n in G.iterneighbors(cur_node):
                    if n not in traversed_nodes:
                        cur_list.append({"name": n, "children": []})
                        nodeq.put((n, cur_list[-1]["children"]), block=True)

        print(json.dumps(self.d, indent=4, separators=(',', ': ')).replace('"children": []', '"size": 1'))

    def run(self):
        while True:
            # worker retrieving job from thread-safe queue
            fname = self.file_queue.get()

            # do task
            self.do_bfs(fname)

            # signal task completion
            self.file_queue.task_done()


def main(args):
    fnames = args.filenames

    file_queue = queue.Queue()

    # init thread pool
    for i in range(0, len(fnames)):  # changed xrange to range
        d = Dot2JSON_BFS(file_queue)
        d.setDaemon(True)
        d.start()

    # distribute work throughout thread pool
    for fname in fnames:
        file_queue.put(fname)

    # wait for all workers to finish
    file_queue.join()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Parse a Graphviz dot/gv file and convert to JSON format.')
    parser.add_argument('filenames', metavar='filenames', type=str, nargs='+',
                        help='One or more dot/gv filenames for parsing.')
    args = parser.parse_args()
    main(args)
