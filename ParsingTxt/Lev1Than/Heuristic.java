import java.io.*;
import java.util.*;

class Sim1 {
    int best;
    int retic;
    int lookup[];
    int firstleaf[];
    boolean bestBi[];
    boolean bestNeg[];
}


//! -------- KSet code is stablised---------------------------------------------------
//! -----------------------------------------------------------------------------------
//! Ksets begin with index 1


class KSet {
    private int leafMax;
    private boolean array[];
    private int leafCount;

    public KSet(int leaves) {
        this.leafMax = leaves;
        array = new boolean[leafMax];
    }

    public void addLeaf(int l) {
        if (array[l - 1] == false) leafCount++;
        array[l - 1] = true;
    }

    public void removeLeaf(int l) {
        if (array[l - 1] == true) leafCount--;
        array[l - 1] = false;
    }

    public boolean containsLeaf(int l) {
        if (l > leafMax) return false;
        return array[l - 1];
    }

    //! Returns true if this is a subset of 'two'

    public boolean isSubsetOf(KSet two) {
        if (this.leafCount > two.leafCount) return false;

        for (int x = 0; x < array.length; x++) {
            if ((array[x] == true) && (two.array[x] == false)) return false;
        }
        return true;
    }

    public int size() {
        return leafCount;
    }

    public boolean empty() {
        return (leafCount == 0);
    }

    public int getFirstElement() {
        for (int x = 0; x < array.length; x++) {
            if (array[x] == true) return (x + 1);
        }

        System.out.println("ERROR#1: Set was empty!");
        return -1;
    }

    public int[] getFirstTwoElements() {
        if (leafCount < 2) return null;

        int vec[] = new int[2];
        int at = 0;

        for (int x = 0; x < array.length; x++) {
            if (array[x]) {
                vec[at++] = (x + 1);
                if (at == 2) return vec;
            }

        }
        return null;
    }

    public void dump() {
        if (Heuristic.DEBUG) {
            System.out.print("[");
            for (int x = 0; x < leafMax; x++) {
                if (array[x] == true) System.out.print((x + 1) + " ");
            }
            System.out.println("]");
        }
    }

    public boolean sameAs(KSet second) {
        int smaller, bigger;
        boolean longer[] = null;

        if (this.leafCount != second.leafCount) return false;

        if (leafMax < second.leafMax) {
            smaller = leafMax;
            bigger = second.leafMax;
            longer = second.array;
        } else {
            smaller = second.leafMax;
            bigger = leafMax;
            longer = this.array;
        }

        for (int x = 0; x < smaller; x++) {
            if (array[x] != second.array[x]) return false;
        }

        for (int x = smaller; x < bigger; x++) {
            if (longer[x]) return false;
        }

        return true;
    }


    public int getMaxSize() {
        return leafMax;
    }


}

//! convention: ab|c where a<b
//! Assumes that leaves are numbered 1...n
//! !!!Assumes that there are no missing leaves!!!

class TripletSet {
    public static int DEFAULT_WEIGHT_MATRIX = 200;

    private Vector tripVec;
    private int numLeaves;

    //! This is dirty and hackish...
    private int weight[][][];
    private int fullWeight;

    private int happyTrips;    //! triplets that were definitely gained when building this set
    private int unhappyTrips;    //! triplets that were definitely lost when building this set

    //! ----------------------------

    public TripletSet() {
        tripVec = new Vector();
        numLeaves = 0;

        weight = null;
        happyTrips = 0;
        unhappyTrips = 0;
    }

    //! Returns the weight of triplets that are destroyed, created by switching from first to second

    public int[] computeBrokenGained(genExplore first, genExplore second) {
        Enumeration e = tripVec.elements();

        int totBroken = 0;

        int totGained = 0;

        while (e.hasMoreElements()) {
            int t[] = (int[]) e.nextElement();

            int w = this.getWeight(t[0], t[1], t[2]);
            if (w <= 0) continue;

            //! CHECK WHETHER WE NEED TO GET T[0] AND T[1] IN THE RIGHT ORDER HERE!

            if (first.consistent(t[0], t[1], t[2])) {
                //! if( Heuristic.MISSING) System.out.println("// "+Heuristic.getLeafName(t[0])+" "+Heuristic.getLeafName(t[1])+" "+Heuristic.getLeafName(t[2])+" (weight "+this.getWeight(t[0],t[1],t[2])+")");

                if (!second.consistent(t[0], t[1], t[2])) totBroken += w;
            }

            if (second.consistent(t[0], t[1], t[2]) && !first.consistent(t[0], t[1], t[2])) totGained += w;
        }

        return new int[]{totBroken, totGained};
    }

    //! -------------- this is a special routine, not for general use! ------------------------------

    //! 'this' is assumed to be the set of all triplets in the network, unweighted, probably 'pure'
    //! dagExplore de is assumed to be the final network
    //! only makes sense with zero pre-processing I think

    public int computeSD(dagExplore de) {
        int mysd = 0;

        int left = this.dumpMissing(de);
        System.out.println("// COMMENT: Final network missed " + left + " triplets from the original complete network.");

        int right = this.dumpSurplus(de);
        System.out.println("// COMMENT: Final network contained " + right + " triplets not in the original complete network.");

        return (left + right);
    }

    //! --------------------------------------------------------------------------------
    //! Computes WeightSum( triplets in pure AND in this. AND in the final network)

    public void getUncorrupted(dagExplore de, TripletSet pure) {
        Enumeration e = pure.elements();

        int totWeightDenom = 0;
        int totWeightNumer = 0;

        while (e.hasMoreElements()) {
            int t[] = (int[]) e.nextElement();

            int w = pure.getWeight(t[0], t[1], t[2]);
            if (w <= 0) continue;

            if (this.containsTriplet(t[0], t[1], t[2])) {
                totWeightDenom += w;
                if (de.consistent(t[0], t[1], t[2]) != de.consistent(t[1], t[0], t[2])) {
                    System.out.println("// FATAL INTERNAL ERROR: de.consistent non-symmetrical on input.");
                    System.exit(0);
                }
                if (de.consistent(t[0], t[1], t[2])) totWeightNumer += w;
            }

        }

        double pct = ((int) (((totWeightNumer * 1.0) / (totWeightDenom * 1.0)) * 10000)) / 100.0;

        //! double pct = ((totWeightNumer*1.0)/(totWeightDenom*1.0))*100;
        System.out.println("// CORRUPT: Got " + totWeightNumer + " units of potentially " + totWeightDenom + " units of uncorrupted weight, that's " + pct);
        System.out.println("// STAT: PERCENTAGE UNCORRUPTED = " + pct);
    }


    //! --------------------------------------------------------------------------------

    public int dumpMissing(dagExplore de) {
        Enumeration e = tripVec.elements();

        int totWeight = 0;

        while (e.hasMoreElements()) {
            int t[] = (int[]) e.nextElement();

            int w = this.getWeight(t[0], t[1], t[2]);
            if (w <= 0) continue;

            //! CHECK WHETHER WE NEED TO GET T[0] AND T[1] IN THE RIGHT ORDER HERE!

            if (de.consistent(t[0], t[1], t[2]) == false) {
                if (Heuristic.MISSING)
                    System.out.println("// " + Heuristic.getLeafName(t[0]) + " " + Heuristic.getLeafName(t[1]) + " " + Heuristic.getLeafName(t[2]) + " (weight " + this.getWeight(t[0], t[1], t[2]) + ")");
                totWeight += w;
            }
        }

        return totWeight;
    }


    public void dumpConsistent(genExplore ge) {

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("consistent.trips"));

            out.write("// File: consistent.trips\n");
            out.write("// -----------------------------------------\n");
            out.write("// Generated in response to --dumpconsistent setting.\n");
            out.write("// Writing out every input triplet that is consistent with the network output by Heuristic.\n");
            out.write("// Triplets are also displayed with their weight, unless it is 1.\n");
            out.write("// -----------------------------------------\n");


            Enumeration e = tripVec.elements();

            int totWeight = 0;

            while (e.hasMoreElements()) {
                int t[] = (int[]) e.nextElement();

                int w = this.getWeight(t[0], t[1], t[2]);
                if (w <= 0) continue;

                //! CHECK WHETHER WE NEED TO GET T[0] AND T[1] IN THE RIGHT ORDER HERE!

                if (ge.consistent(t[0], t[1], t[2]) == true) {
                    if (w != 1)
                        out.write(Heuristic.getLeafName(t[0]) + " " + Heuristic.getLeafName(t[1]) + " " + Heuristic.getLeafName(t[2]) + " " + this.getWeight(t[0], t[1], t[2]) + "\n");
                    else
                        out.write(Heuristic.getLeafName(t[0]) + " " + Heuristic.getLeafName(t[1]) + " " + Heuristic.getLeafName(t[2]) + "\n");
                }
            }

            out.close();
        } catch (IOException e) {
            System.out.println("ERROR! There was a problem writing to the file 'consistent.trips'.");
        }

    }


//! ---- and now the genExplore version

    public int dumpMissing(genExplore ge, int tripStat[]) {
        Enumeration e = tripVec.elements();

        int totWeight = 0;

        while (e.hasMoreElements()) {
            int t[] = (int[]) e.nextElement();

            int w = this.getWeight(t[0], t[1], t[2]);
            if (w <= 0) continue;

            //! CHECK WHETHER WE NEED TO GET T[0] AND T[1] IN THE RIGHT ORDER HERE!

            if (ge.consistent(t[0], t[1], t[2]) == false) {
                if (Heuristic.MISSING)
                    System.out.println("// " + Heuristic.getLeafName(t[0]) + " " + Heuristic.getLeafName(t[1]) + " " + Heuristic.getLeafName(t[2]) + " (weight " + this.getWeight(t[0], t[1], t[2]) + ")");

                totWeight += w;
            } else {
                tripStat[t[0]] += w;
                tripStat[t[1]] += w;
                tripStat[t[2]] += w;
            }
        }

        return totWeight;
    }

//! --------------------------------------------------------------------------------------------

    public int computeConsistency(genExplore ge) {

        Enumeration e = tripVec.elements();

        int totWeight = 0;

        while (e.hasMoreElements()) {
            int t[] = (int[]) e.nextElement();

            int w = this.getWeight(t[0], t[1], t[2]);
            if (w <= 0) continue;

            //! CHECK WHETHER WE NEED TO GET T[0] AND T[1] IN THE RIGHT ORDER HERE!

            if (ge.consistent(t[0], t[1], t[2]) == true) totWeight += w;
        }

        return totWeight;
    }


//! -------------------------------------------------------------------------------------------

    public int dumpSurplus(dagExplore de) {
        int surplus = 0;

        for (int a = 1; a <= numLeaves; a++)
            for (int b = (a + 1); b <= numLeaves; b++)
                for (int c = (b + 1); c <= numLeaves; c++) {

                    if (de.consistent(a, b, c) && (this.getTripletWeight(a, b, c) <= 0)) {
                        if (Heuristic.SURPLUS)
                            System.out.println("// " + Heuristic.getLeafName(a) + " " + Heuristic.getLeafName(b) + " " + Heuristic.getLeafName(c));
                        surplus++;
                    }

                    if (de.consistent(a, c, b) && (this.getTripletWeight(a, c, b) <= 0)) {
                        if (Heuristic.SURPLUS)
                            System.out.println("// " + Heuristic.getLeafName(a) + " " + Heuristic.getLeafName(c) + " " + Heuristic.getLeafName(b));
                        surplus++;
                    }

                    if (de.consistent(b, c, a) && (this.getTripletWeight(b, c, a) <= 0)) {
                        if (Heuristic.SURPLUS)
                            System.out.println("// " + Heuristic.getLeafName(b) + " " + Heuristic.getLeafName(c) + " " + Heuristic.getLeafName(a));
                        surplus++;
                    }
                }
        return surplus;
    }

    //! The genExplore version

    public int dumpSurplus(genExplore ge) {
        int surplus = 0;

        for (int a = 1; a <= numLeaves; a++)
            for (int b = (a + 1); b <= numLeaves; b++)
                for (int c = (b + 1); c <= numLeaves; c++) {
                    if (ge.consistent(a, b, c) && (this.getTripletWeight(a, b, c) <= 0)) {
                        if (Heuristic.SURPLUS)
                            System.out.println("// " + Heuristic.getLeafName(a) + " " + Heuristic.getLeafName(b) + " " + Heuristic.getLeafName(c));
                        surplus++;
                    }

                    if (ge.consistent(a, c, b) && (this.getTripletWeight(a, c, b) <= 0)) {
                        if (Heuristic.SURPLUS)
                            System.out.println("// " + Heuristic.getLeafName(a) + " " + Heuristic.getLeafName(c) + " " + Heuristic.getLeafName(b));
                        surplus++;
                    }

                    if (ge.consistent(b, c, a) && (this.getTripletWeight(b, c, a) <= 0)) {
                        if (Heuristic.SURPLUS)
                            System.out.println("// " + Heuristic.getLeafName(b) + " " + Heuristic.getLeafName(c) + " " + Heuristic.getLeafName(a));
                        surplus++;
                    }
                }
        return surplus;
    }


//! -----------------------------------------------------------------------------------------------------

    //! If this is NOT called immediately after initialisation,
    //! the default will be used

    public void setWeightMatrix(int n) {
        weight = new int[n + 1][n + 1][n + 1];
        for (int x = 1; x <= n; x++)
            for (int y = 1; y <= n; y++)
                for (int z = 1; z <= n; z++)
                    weight[x][y][z] = -1;
    }

    public void recalculateTotalWeight() {
        fullWeight = 0;

        Enumeration e = tripVec.elements();
        while (e.hasMoreElements()) {
            int triplet[] = (int[]) e.nextElement();

            int a = triplet[0];
            int b = triplet[1];
            int c = triplet[2];

            fullWeight += weight[a][b][c];
        }
    }

    public int getTotalWeight() {
        return fullWeight;
    }

    public Enumeration elements() {
        return tripVec.elements();
    }

    //! Nasty....don't use except in dire circumstances
    public int tellLeaves() {
        return numLeaves;
    }


    //! ----------------------------------------------------------
    //! experimental!! ignores zero weight triplets...

    public boolean isGenuinelyDense() {
        for (int x = 1; x <= numLeaves; x++)
            for (int y = 1; y <= numLeaves; y++)
                for (int z = 1; z <= numLeaves; z++) {
                    if ((x == y) || (x == z) || (z == y)) continue;
                    if (!(getWeight(x, y, z) > 0) && !(getWeight(x, z, y) > 0) && !(getWeight(z, y, x) > 0)) {
                        return false;
                    }
                }
        return true;
    }
    //! ---------------------------------------------------------


    //! This is currently very slooow.
    public boolean isDense(int errorTrip[]) {
        for (int x = 1; x <= numLeaves; x++)
            for (int y = 1; y <= numLeaves; y++)
                for (int z = 1; z <= numLeaves; z++) {
                    if ((x == y) || (x == z) || (z == y)) continue;
                    if (!containsTriplet(x, y, z) && !containsTriplet(x, z, y) && !containsTriplet(z, y, x)) {
                        errorTrip[0] = x;
                        errorTrip[1] = y;
                        errorTrip[2] = z;
                        return false;
                    }
                }
        return true;
    }


    public void makeDense() {
        Heuristic.report("Padding the non-dense set to a dense set with zero-weight triples.");

        for (int x = 1; x <= numLeaves; x++)
            for (int y = (x + 1); y <= numLeaves; y++)
                for (int z = (y + 1); z <= numLeaves; z++) {
                    if (!containsTriplet(x, y, z) && !containsTriplet(x, z, y) && !containsTriplet(z, y, x)) {
                        addTriplet(x, y, z, 0);
                        //! Heuristic.report("Adding zero-weight triplet "+x+" "+y+" "+z);
                    }
                }
    }


    //! This should actually NEVER be called

    public void addTriplet(int a, int b, int c) {
        System.out.println("// WARNING: addTriplet with default weight called.");
        addTriplet(a, b, c, 1);
    }

    //! If weighting is switched on then this will increment the weight
    //! of the triplet by w, if it is already in...

    public void addTriplet(int a, int b, int c, int w) {
        if (w < 0) {
            System.out.println("// ERROR: Triplet with negative weight " + w + " appeared.");
            System.exit(0);
        }

        if (weight == null) {
            //! System.out.println("// WARNING: Using a default size weight matrix of "+DEFAULT_WEIGHT_MATRIX+".");
            this.setWeightMatrix(DEFAULT_WEIGHT_MATRIX);
        }

        int swap = 0;

        //! swap a and b around, if necessary...

        if (a > b) {
            swap = a;
            a = b;
            b = swap;
        }

        boolean wasIn = this.containsTriplet(a, b, c);

        if (wasIn) {
            weight[a][b][c] += w;
            fullWeight += w;
        } else {
            //! This is necessary to make zero weight triplets possible...

            weight[a][b][c] = w;
            fullWeight += w;
        }

        if (wasIn) return;    //! no need to add it to the list

        //! What is the highest leaf seen so far?

        int highest = 0;

        if ((a > b) && (a > c)) highest = a;
        else if ((b > a) && (b > c)) highest = b;
        else
            highest = c;

        if (highest > numLeaves) {
            numLeaves = highest;
        }

        int myTriplet[] = new int[3];
        myTriplet[0] = a;
        myTriplet[1] = b;
        myTriplet[2] = c;

        tripVec.add((int[]) myTriplet);
    }

    public int countTriplets() {
        return tripVec.size();
    }


    //! ---------------------------------------------------


    public boolean containsTriplet(int a, int b, int c) {
        int swap = 0;

        if ((a == b) || (b == c) || (a == c)) {
            System.out.println("// ERROR: duplicate leaves in call to containsTriplet(). Terminating.");
            System.exit(0);
        }

        //! swap a and b around, if necessary...

        if (a > b) {
            swap = a;
            a = b;
            b = swap;
        }

        if (weight == null) return false;
        else {
            if ((a >= weight.length) || (b >= weight.length) || (c >= weight.length)) return false;

            return (weight[a][b][c] > -1);
        }

    }


    public void dumpTriplets() {
        System.out.println(tripVec.size() + " elements on " + numLeaves + " leaves.");

        Enumeration e = tripVec.elements();
        while (e.hasMoreElements()) {
            int triplet[] = (int[]) e.nextElement();
            System.out.println(Heuristic.getLeafName(triplet[0]) + " " + Heuristic.getLeafName(triplet[1]) + " | " + Heuristic.getLeafName(triplet[2]) + " weight: " + weight[triplet[0]][triplet[1]][triplet[2]]);
        }
    }

    //! ----------------------------------------------------------------------------------------
    //! If the leaves have been using some other names...only used by external programs
    public void dumpTripletsWithHash(Hashtable h, boolean weighted) {
        Enumeration e = tripVec.elements();
        while (e.hasMoreElements()) {
            int triplet[] = (int[]) e.nextElement();

            String na = (String) h.get(new Integer(triplet[0]));
            String nb = (String) h.get(new Integer(triplet[1]));
            String nc = (String) h.get(new Integer(triplet[2]));

            System.out.print(na + " " + nb + " " + nc);
            int x = weight[triplet[0]][triplet[1]][triplet[2]];

            if ((x != 1) && (weighted)) System.out.println(" " + x);
            else System.out.println();
        }
    }


    //! ---------------------------------------------------------------------------------------------------------------

    //! Give it an array of which leaves you want to do the inducing
    //! backMap says what the original labellings of the new leaves were...

    //! This now correctly transfers the weights to the new triplet set.

    public TripletSet induceTripletSet(KSet k, int backMap[]) {
        //! System.out.println("Inducing triplets from a kset with "+k.size()+" leaves.");

        TripletSet ts = new TripletSet();
        ts.setWeightMatrix(k.size());

        boolean in[] = new boolean[numLeaves + 1];

        int leafMap[] = new int[numLeaves + 1];
        int mapCount = 1;
        //! This will map {1...numLeaves} -> {1...size(KSet)}

        for (int x = 1; x <= numLeaves; x++) {
            in[x] = k.containsLeaf(x);
            if (in[x]) {
                leafMap[x] = mapCount;

                backMap[mapCount] = x;

                mapCount++;
            }
        }

        Enumeration e = this.tripVec.elements();

        while (e.hasMoreElements()) {
            int t[] = (int[]) e.nextElement();

            if (in[t[0]] && in[t[1]] && in[t[2]]) {
                int hisWeight = this.getWeight(t[0], t[1], t[2]);

                //! Remove this once we are confident of correctness, will slow things down

                if (ts.containsTriplet(leafMap[t[0]], leafMap[t[1]], leafMap[t[2]])) {
                    System.out.println("Fatal error: repeated triplet!");
                    System.exit(0);
                }

                ts.addTriplet(leafMap[t[0]], leafMap[t[1]], leafMap[t[2]], hisWeight);
            }

        }

        return ts;
    }


    //! ----------------------------------------------------------------------------------------
    //! A variant of FastComputeSN that (hopefully) works for non-dense sets too????
    //! hmmm, something seems to break here, STILL NEED TO FIX THIS!
    //! ----------------------------------------------------------------------------

    public KSet NDFastComputeSN(int x, int y) {
        int n = numLeaves;

        KSet X = new KSet(n);
        KSet Z = new KSet(n);

        X.addLeaf(x);
        Z.addLeaf(y);

        while (Z.empty() == false) {
            int z = Z.getFirstElement();

            for (int a = 1; a <= n; a++) {
                if (X.containsLeaf(a) == false) continue;

                for (int c = 1; c <= n; c++) {
                    if (X.containsLeaf(c) || Z.containsLeaf(c)) continue;

                    if ((a == c) || (a == z) || (z == c)) continue;

                    if (this.getWeight(a, c, z) > 0 || this.getWeight(z, c, a) > 0) {
                        Z.addLeaf(c);

                        //! I think their algorithm mistakenly implies that we can 'break' here
                    }

                }
                X.addLeaf(z);
                Z.removeLeaf(z);
            }

        }
        return X;
    }


    //! Not sure whether this is a correct implementation, try and verify the
    //! correctness of this...I think it should be approximately cubic time...
    //! Ah I now understand why this works. At any one iteration, X cup Z is
    //! the current SN set, and X cup Z contains all leaves which were
    //! added because of triplets of the form x1 * | x2

    public KSet FastComputeSN(int x, int y) {
        int n = numLeaves;

        KSet X = new KSet(n);
        KSet Z = new KSet(n);

        X.addLeaf(x);
        Z.addLeaf(y);

        while (Z.empty() == false) {
            int z = Z.getFirstElement();

            for (int a = 1; a <= n; a++) {
                if (X.containsLeaf(a) == false) continue;

                for (int c = 1; c <= n; c++) {
                    if (X.containsLeaf(c) || Z.containsLeaf(c)) continue;

                    //! The following line was added april 9th 2009 !
                    if ((a == c) || (a == z) || (z == c)) continue;

                    if (this.containsTriplet(a, c, z) || this.containsTriplet(z, c, a)) {
                        Z.addLeaf(c);

                        //! I think their algorithm mistakenly implies that we can 'break' here
                    }

                }
                X.addLeaf(z);
                Z.removeLeaf(z);
            }

        }
        return X;
    }


    //! NOTE: This won't work properly (YET) for non-dense sets! <-- is that so?
    //! Do all the other non-dense functions actually work for non-dense sets?!?! CHECK THIS!

    //! localPerfection[0] gets the value true iff this was locally dense and a perfect simple level-1 was possible

    public Vector getPartition(boolean localPerfection[]) {
        Graph aho = new Graph(numLeaves);

        int trips[][] = this.getArray();

        for (int p = 0; p < trips.length; p++) {
            if (this.getWeight(trips[p][0], trips[p][1], trips[p][2]) == 0) {
                Heuristic.report("Partitioning strategy: Ignoring zero-weight triplets in constructing Aho graph.");
                continue;
            }
            aho.addEdge(trips[p][0], trips[p][1]);
            //! So vertices in the Aho graph range from 1...totLeaves, but are internally 0...totLeaves-1
        }

        boolean status[] = aho.exploreComponent();

        int count = 0;
        for (int scan = 0; scan < status.length; scan++) {
            if (status[scan] == Graph.EXP_VISITED) count++;
        }

        if (count != numLeaves) {
            Heuristic.report("Partitioning strategy: Aho graph not connected, DOES help here.");

            KSet left = new KSet(numLeaves);
            KSet right = new KSet(numLeaves);

            for (int scan = 0; scan < status.length; scan++) {
                if (status[scan] == Graph.EXP_VISITED) left.addLeaf(scan + 1);
                else right.addLeaf(scan + 1);
            }

            Vector magic = new Vector();
            magic.addElement(left);
            magic.addElement(right);

            return magic;
        } else {
            Heuristic.report("Partitioning strategy: Aho graph is connected, DOES NOT help here.");
        }


        if (Heuristic.NOPERFECTION == false) {

            //! Here we could insert a thing that tests if it makes sense to compute tha maximum SN-sets, checks
            //! if the output is dense, and if it fits into a simple level-1 network........ do we really want to
            //! do that?

            Vector v = this.NDGetMaxSNSets();

            if (this.verifyPartition(v) == true) {
                //! Ok, so the max SN sets are a true partition

                Heuristic.report("Max SN-sets did make a clean partition, seeing if they induce density...");

                TripletSet tp = this.buildTPrime(v);

                if (tp.isGenuinelyDense()) {
                    Heuristic.report("Yes, max SN-sets did induce dense triplet set: trying for exact fit with simple level-1 network.");
                    if (tp.buildSimpleLevel1() != null) {
                        localPerfection[0] = true;
                        return v;
                    }
                }

            }
        }

        //! OK, Aho failed so do a normal partitioning...

        Heuristic.report("Having to do custom partitioning of leaf set...");

        KSet blocks[] = new KSet[Heuristic.BLOCKS];

        Vector answer = new Vector();

        int n = numLeaves;

        if (n == 0) return null;

        if (n == 1) {
            System.out.println("WARNING: Ran getPartition() with only one leaf...");
            KSet k = new KSet(1);
            k.addLeaf(1);
            answer.add(k);
            return answer;
        }

        if (n == 2) {
            KSet k1 = new KSet(2);
            KSet k2 = new KSet(2);
            k1.addLeaf(1);
            k2.addLeaf(2);
            return answer;
        }

        //! this.dumpTriplets();

        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new KSet(n);
            answer.add(blocks[x]);
        }

        //! start by putting all leaves in the zeroth kset

        for (int x = 1; x <= n; x++) {
            blocks[0].addLeaf(x);
        }

        //! Ok, we're going to move leaves around until the function hits a local maximum

        boolean increased = true;

        //! This is the mapping that maps leaves to blocks;

        int map[] = new int[n + 1];

        leafloop:
        for (int scan = 1; scan <= n; scan++) {
            for (int loop = 0; loop < blocks.length; loop++) {
                if (blocks[loop].containsLeaf(scan)) {
                    map[scan] = loop;
                    continue leafloop;
                }
            }
            System.out.println("ERROR! Should never get here!");
            System.exit(0);
        }

        //! ===========================================================
        //! This accelerates the partitioning strategy because it allows
        //! us to focus on only the triplets affected by a move

        int tripLeafCount[] = new int[n + 1];
        int tripAt[] = new int[n + 1];

        Enumeration e = tripVec.elements();

        while (e.hasMoreElements()) {
            int t[] = (int[]) e.nextElement();
            tripLeafCount[t[0]]++;
            tripLeafCount[t[1]]++;
            tripLeafCount[t[2]]++;
        }

        int tripIndex[][][] = new int[n + 1][][];

        for (int spin = 1; spin <= n; spin++) {
            tripIndex[spin] = new int[tripLeafCount[spin]][4];
        }

        e = tripVec.elements();

        while (e.hasMoreElements()) {
            int t[] = (int[]) e.nextElement();

            int a = t[0];

            tripIndex[a][tripAt[a]][0] = t[0];
            tripIndex[a][tripAt[a]][1] = t[1];
            tripIndex[a][tripAt[a]][2] = t[2];
            tripIndex[a][tripAt[a]][3] = this.getWeight(t[0], t[1], t[2]);
            tripAt[a]++;

            int b = t[1];

            tripIndex[b][tripAt[b]][0] = t[0];
            tripIndex[b][tripAt[b]][1] = t[1];
            tripIndex[b][tripAt[b]][2] = t[2];
            tripIndex[b][tripAt[b]][3] = this.getWeight(t[0], t[1], t[2]);
            tripAt[b]++;

            int c = t[2];

            tripIndex[c][tripAt[c]][0] = t[0];
            tripIndex[c][tripAt[c]][1] = t[1];
            tripIndex[c][tripAt[c]][2] = t[2];
            tripIndex[c][tripAt[c]][3] = this.getWeight(t[0], t[1], t[2]);
            tripAt[c]++;

            //! Ok, so now we have our leaf vectors...
        }

        //! ===============================================================================

        //! int function = computeFunction( blocks, map, Heuristic.INTERNAL_COEFF, Heuristic.EXTERNAL_COEFF, Heuristic.HAPPY_COEFF );

        Heuristic.report("Starting main loop of partitioning function.");

        boolean firstMove = true;

        long incCount = 0;

        majorloop:
        while (increased) {
            increased = false;

            Heuristic.report("We increased the partition score (" + (incCount++));

            for (int leaf = 1; leaf <= n; leaf++) {
                int currentBlock = map[leaf];

                boolean seenEmpty = false;

                for (int toblock = 0; toblock < blocks.length; toblock++) {
                    if (toblock == currentBlock) continue;

                    //! we only need to try moving to an empty block ONCE!
                    if (seenEmpty && blocks[toblock].empty()) continue;

                    if (blocks[toblock].empty()) seenEmpty = true;

                    //! Try moving from currentBlock to toBlock

                    int oldfunc = computeFunction(tripIndex[leaf], blocks, map, Heuristic.INTERNAL_COEFF, Heuristic.EXTERNAL_COEFF, Heuristic.HAPPY_COEFF);

                    blocks[currentBlock].removeLeaf(leaf);

                    boolean becameEmpty = false;
                    if (Heuristic.EXPAND) {
                        if (blocks[currentBlock].empty()) becameEmpty = true;
                    }

                    blocks[toblock].addLeaf(leaf);
                    map[leaf] = toblock;

                    int newfunc = computeFunction(tripIndex[leaf], blocks, map, Heuristic.INTERNAL_COEFF, Heuristic.EXTERNAL_COEFF, Heuristic.HAPPY_COEFF);

                    int diff = newfunc - oldfunc;

                    if ((diff > 0) || (diff >= 0 && firstMove) || ((Heuristic.EXPAND) && (diff == 0) && (!becameEmpty) && (seenEmpty))) {
                        increased = true;
                        firstMove = false;
                        //! System.out.println("We moved out of the initial state.");
                        continue majorloop;
                    } else {
                        //! move it back, fix stuff...
                        blocks[toblock].removeLeaf(leaf);
                        blocks[currentBlock].addLeaf(leaf);
                        map[leaf] = currentBlock;
                        //! System.out.println("Was inferior: "+newfunc+" versus "+function);
                    }
                }


            }


        } //! end majorloop while loop


        //! Throw away any empty blocks

        for (int z = 0; z < blocks.length; z++) {
            if (blocks[z].size() == 0) {
                answer.remove(blocks[z]);
            }

        }

        Heuristic.report("Finished constructing the partition.");

        return answer;
    }

    //! internal = coeff for N_1
    //! external = coeff for N_2
    //! happy = coeff for N_3

    //! map is the mapping from leaves to blocks

    private int computeFunction(int tripKijk[][], KSet blocks[], int map[], int internal, int external, int happy) {
        int total = 0;

        int weightDone = 0;

        for (int loop = 0; loop < tripKijk.length; loop++) {
            int w = tripKijk[loop][3];

            int a = map[tripKijk[loop][0]];
            int b = map[tripKijk[loop][1]];
            int c = map[tripKijk[loop][2]];

            if ((a == b) && (b == c)) {
                total += (internal * w);
                weightDone += w;
            } else if ((a != b) && (b != c) && (a != c)) {
                total += (external * w);
                weightDone += w;
            } else if ((a == b) && (b != c)) {
                total += (happy * w);
                weightDone += w;
            } else if ((b == c) && (a != b)) {
                weightDone += w;
            } else if ((a == c) && (a != b)) {
                weightDone += w;
            } else {
                System.out.println("FATAL ERROR.");
                System.exit(0);
            }

        }

		/*
		if( weightDone != this.getTotalWeight() )
			{
			System.out.println("FATAL ERROR (WEIGHT SUM).");
			System.exit(0);
			}
		*/

        return total;
    }

    //! -------------------------------------------------------------------------------
    //! returns true iff this is a partitition of the leaves i.e. no overlaps, no gaps

    public boolean verifyPartition(Vector maxsn) {
        int n = numLeaves;

        int count[] = new int[n + 1];

        Enumeration msn = maxsn.elements();
        while (msn.hasMoreElements()) {
            KSet k = (KSet) msn.nextElement();

            for (int l = 1; l <= n; l++) {
                if (k.containsLeaf(l)) count[l]++;
                if (count[l] > 1) return false;    //! overlap!
            }

        }

        for (int x = 1; x <= n; x++) if (count[x] == 0) return false;    //! gap!

        return true;
    }

    //! ------------------------------------------------------------------------------

    //! non dense version of getMaxSNSets() ----------------------------------
    //! ----------------------------------------------------------------------

    public Vector NDGetMaxSNSets() {
        Vector temp = new Vector();

        int n = numLeaves;

        //! Build all singleton SN-sets i.e. of the form SN(x)
        //! These are obviously all disjoint...

        for (int x = 1; x <= n; x++) {
            KSet K = new KSet(n);
            K.addLeaf(x);
            temp.add((KSet) K);
        }

        //! Build all sets of the form SN(x,y)
        //!

        for (int x = 1; x <= n; x++) {
            for (int y = (x + 1); y <= n; y++) {
                KSet K = NDFastComputeSN(x, y);

                //! Don't need to add SN sets that are trivial
                if (K.size() != n) temp.add((KSet) K);

            }
        }

        boolean somethingRemoved = true;

        while (somethingRemoved) {
            somethingRemoved = false;

            int el = temp.size();

            escape:
            for (int x = 0; x < el; x++) {
                for (int y = (x + 1); y < el; y++) {
                    KSet a = (KSet) temp.elementAt(x);
                    KSet b = (KSet) temp.elementAt(y);

                    if (a.isSubsetOf(b)) {
                        temp.removeElementAt(x);
                        somethingRemoved = true;
                        break escape;
                    }
                    if (b.isSubsetOf(a)) {
                        temp.removeElementAt(y);
                        somethingRemoved = true;
                        break escape;
                    }
                }
            }

        }

        return temp;
    }


    public Vector getMaxSNSets() {
        Vector temp = new Vector();

        int n = numLeaves;

        //! Build all singleton SN-sets i.e. of the form SN(x)
        //! These are obviously all disjoint...

        for (int x = 1; x <= n; x++) {
            KSet K = new KSet(n);
            K.addLeaf(x);
            temp.add((KSet) K);
        }

        //! Build all sets of the form SN(x,y)
        //!

        for (int x = 1; x <= n; x++) {
            for (int y = (x + 1); y <= n; y++) {
                KSet K = FastComputeSN(x, y);

                //! Don't need to add SN sets that are trivial
                if (K.size() != n) temp.add((KSet) K);

            }
        }


        boolean somethingRemoved = true;

        while (somethingRemoved) {
            somethingRemoved = false;

            int el = temp.size();

            escape:
            for (int x = 0; x < el; x++) {
                for (int y = (x + 1); y < el; y++) {
                    KSet a = (KSet) temp.elementAt(x);
                    KSet b = (KSet) temp.elementAt(y);

                    if (a.isSubsetOf(b)) {
                        temp.removeElementAt(x);
                        somethingRemoved = true;
                        break escape;
                    }
                    if (b.isSubsetOf(a)) {
                        temp.removeElementAt(y);
                        somethingRemoved = true;
                        break escape;
                    }
                }
            }

        }

        return temp;
    }


    public TripletSet buildTPrime(Vector maxSN) {
        //! I've removed this line because it can be useful to have some of the side effects here
        //! if( maxSN.size() == 2 ) return null;

        int nprime = maxSN.size();

        KSet fastSN[] = new KSet[nprime + 1];

        //! We'll build a lookup-table here to make the mapping fast
        //! Maps {1,n} -> {1,n'}

        int map[] = new int[this.numLeaves + 1];

        int index = 1;

        Enumeration e = maxSN.elements();
        while (e.hasMoreElements()) {
            fastSN[index] = (KSet) e.nextElement();
            for (int scan = 1; scan <= numLeaves; scan++) {
                if (fastSN[index].containsLeaf(scan)) map[scan] = index;
            }
            index++;
        }

        //! Ok, the maxSN sets are now in the array fastSN, we don't
        //! use the zero element...

        //! Simply iterate through all the triplets in 'this', and induce the corresponding set
        //! of new triplets...

        TripletSet tprime = new TripletSet();
        tprime.setWeightMatrix(nprime);

        Enumeration f = tripVec.elements();
        while (f.hasMoreElements()) {
            int t[] = (int[]) f.nextElement();

            int a = map[t[0]];
            int b = map[t[1]];
            int c = map[t[2]];

            int w = this.getWeight(t[0], t[1], t[2]);

            if ((b == a) && (c != b)) tprime.happyTrips += w;        //! These triplets will definitely be in!

            if (((b == c) && (a != b)) || ((a == c) && (b != a))) tprime.unhappyTrips += w;

            if ((c == a) || (c == b) || (b == a)) {
                continue;
            }

            tprime.addTriplet(map[t[0]], map[t[1]], map[t[2]], w);
        }

        return tprime;
    }


    private Graph buildAhoGraph() {
        //! Cycle through all triplets...

        Graph aho = new Graph(numLeaves);

        Enumeration e = tripVec.elements();
        while (e.hasMoreElements()) {
            int t[] = (int[]) e.nextElement();

            aho.addEdge(t[0], t[1]);
        }

        return aho;
    }


    //! private constructor
    //! builds a new TripletSet equal to the current tripletset, with the
    //! difference that all triplets containing leaf s are suppressed;
    //! can be optimised, if needed.

    //! All leaves with an index bigger than s drop their index by 1!!!

    private TripletSet(TripletSet ts, int s) {
        tripVec = new Vector();
        numLeaves = 0;

        weight = null;        //! consistency with normal constructor
        happyTrips = 0;
        unhappyTrips = 0;

        setWeightMatrix(ts.tellLeaves() - 1);

        Enumeration e = ts.tripVec.elements();
        while (e.hasMoreElements()) {
            int v[] = (int[]) e.nextElement();

            //! If it contains s, throw it away...
            if ((v[0] == s) || (v[1] == s) || (v[2] == s)) continue;

            if (v[0] > v[1]) {
                System.out.println("Fatal error. Indexes got twisted.");
                System.exit(0);
            }

            int w = ts.getWeight(v[0], v[1], v[2]);

            //! Otherwise, put the triple back in (with, where
            //! necessary, adjusted indexing

            int ap = v[0] < s ? v[0] : v[0] - 1;
            int bp = v[1] < s ? v[1] : v[1] - 1;
            int cp = v[2] < s ? v[2] : v[2] - 1;

            addTriplet(ap, bp, cp, w);
        }

    }

    //! If you have a vector of leaf nodes, makes space for the
    //! re-insertion of leaf 'correction'

    private void correctLeafNodes(Vector nodes, int correction) {
        Enumeration e = nodes.elements();
        while (e.hasMoreElements()) {
            biDAG b = (biDAG) e.nextElement();
            if (b.data < correction) continue;
            b.data++;
        }

    }

    //! ---------------------------------------------------
    //! THIS IS EXPERIMENTAL, DO NOT USE!!!
    //! UNLESS YOU KNOW WHAT YOU ARE DOING!

    public int getTripletWeight(int a, int b, int c) {
        int swap = 0;

        //! swap a and b around, if necessary...

        if (a > b) {
            swap = a;
            a = b;
            b = swap;
        }
        return weight[a][b][c];
    }

    //! ----------------------------------------------------
    //! Umm, not sure what the diff is between this and the above function......

    public int getWeight(int a, int b, int c) {
        if ((a == b) || (b == c) || (a == c)) {
            System.out.println("FATAL ERROR in getWeight(), NOT ALL LEAVES DISTINCT.");
            System.exit(0);
        }

        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }

        return (this.weight[a][b][c]);
    }


    //! ------------------------------------------------------------------


    public int[][] getArray() {
        int myt[][] = new int[tripVec.size()][3];
        int at = 0;

        Enumeration e = tripVec.elements();

        while (e.hasMoreElements()) {
            int g[] = (int[]) e.nextElement();
            myt[at][0] = g[0];
            myt[at][1] = g[1];
            myt[at][2] = g[2];
            at++;
        }

        return myt;
    }

    //! This also ensures that the returned biDAG is equal to the total
    //! weight of the input triplets.

    public biDAG buildSimpleLevel1() {
        if (numLeaves <= 2) {
            Heuristic.report("Tried to build simple level-1 with only two leaves.");
            return null;
        }

        //! Non-skew network. Is more complicated than skew level-1, but
        //! the chance of them being time-consistent is higher so better
        //! to start with non-skew than skew.

        Heuristic.report("Trying non-skew network level-1.");

        for (int l = 1; l <= numLeaves; l++) {
            //! System.out.println("Suppressing leaf "+l);
            TripletSet ts = new TripletSet(this, l);

            //! So now we have a triplet set where leaf l was suppressed.
            //! We'll have to do all that relabelling stuff, *zucht*

            biDAG splits = null;

            if (ts.countTriplets() != 0) {
                splits = ts.buildTree();
            } else {
                //! This can only happen if numLeaves == 3 I think

                splits = biDAG.cherry12();
            }

            if (splits == null) continue;

            //! We need splits to have a weird shape...if it doesn't have
            //! that shape it can't be good...it's that old classic again...
            //! I think there are (at most) 4 possibilities here...we can
            //! assume that there is at least one leaf on both sides of the
            //! root

            Vector left = new Vector();
            Vector right = new Vector();

            boolean isSplit = splits.testSplit(left, right);

            if (!isSplit) continue;

            //! Makes space for leaf l to be added back
            splits.oneLeafAdjustTree(l);

            //! So...let's do this...there could be up to four glueings...

            //! try all additions possible...grrrr...this is extremely annoying...

            int leftLookUp[] = new int[left.size()];
            int rightLookUp[] = new int[right.size()];

            //! This because the leaves may be divided across different sides...

            int leftDfr[] = new int[numLeaves + 1];
            int rightDfr[] = new int[numLeaves + 1];

            int leftTry = 1;
            if (left.size() > 1) leftTry = 2;

            int rightTry = 1;
            if (right.size() > 1) rightTry = 2;

            for (int lscan = 1; lscan <= leftTry; lscan++)
                for (int rscan = 1; rscan <= rightTry; rscan++) {
                    int leafSide[] = new int[this.numLeaves + 1];

                    biDAG leftGraft, rightGraft = null;

                    for (int x = 0; x < left.size(); x++) {
                        leftLookUp[x] = ((biDAG) left.elementAt(x)).data;
                        leftDfr[leftLookUp[x]] = x;
                        leafSide[leftLookUp[x]] = Graph.LEFT;
                    }
                    leftGraft = ((biDAG) left.elementAt(left.size() - 1));

                    if (lscan == 2) {
                        //! swap the order of the last two leaves on the left side

                        int last = leftLookUp[leftLookUp.length - 1];
                        int penum = leftLookUp[leftLookUp.length - 2];

                        int posLast = leftDfr[last];
                        int posPenum = leftDfr[penum];

                        leftDfr[last] = posPenum;
                        leftDfr[penum] = posLast;
                        leftGraft = ((biDAG) left.elementAt(left.size() - 2));
                    }

                    for (int x = 0; x < right.size(); x++) {
                        rightLookUp[x] = ((biDAG) right.elementAt(x)).data;
                        rightDfr[rightLookUp[x]] = x;
                        leafSide[rightLookUp[x]] = Graph.RIGHT;
                    }

                    rightGraft = ((biDAG) right.elementAt(right.size() - 1));

                    if (rscan == 2) {
                        //! swap the order of the last two leaves on the left side

                        int last = rightLookUp[rightLookUp.length - 1];
                        int penum = rightLookUp[rightLookUp.length - 2];

                        int posLast = rightDfr[last];
                        int posPenum = rightDfr[penum];

                        rightDfr[last] = posPenum;
                        rightDfr[penum] = posLast;

                        rightGraft = ((biDAG) right.elementAt(right.size() - 2));
                    }

                    //! Now we're ready to rock...

                    boolean success = true;

                    for (int s = 0; s < tripVec.size(); s++) {
                        int t[] = (int[]) tripVec.elementAt(s);

                        int x = t[0];
                        int y = t[1];
                        int z = t[2];

                        //! Case z == l

                        if ((z == l) && (leafSide[x] != leafSide[y])) {
                            success = false;
                            break;
                        }

                        //! Case x == l

                        if ((x == l) && (leafSide[y] == leafSide[z])) {
                            if (leafSide[y] == Graph.LEFT) {
                                if (leftDfr[z] > leftDfr[y]) {
                                    success = false;
                                    break;
                                }
                            } else if (leafSide[y] == Graph.RIGHT) {
                                if (rightDfr[z] > rightDfr[y]) {
                                    success = false;
                                    break;
                                }
                            } else System.out.println("Error!");
                        }


                        if ((y == l) && (leafSide[x] == leafSide[z])) {
                            if (leafSide[x] == Graph.LEFT) {
                                if (leftDfr[z] > leftDfr[x]) {
                                    success = false;
                                    break;
                                }
                            } else if (leafSide[x] == Graph.RIGHT) {
                                if (rightDfr[z] > rightDfr[x]) {
                                    success = false;
                                    break;
                                }
                            } else System.out.println("Error!");
                        }


                    }    //! triplet loop

                    if (success) {
                        //! Nou, dit wordt spannend....

                        biDAG.glueLeafBetween(leftGraft, rightGraft, l);

                        splits.tripsWithin = this.getTotalWeight();

                        return splits;
                    }


                }    //! lscan/rscan


        }    //! leafSuppression loop...

        //! Skew network...

        Heuristic.report("Was not a non-skew simple level-1 network.");

        Heuristic.report("Trying to build a skew simple level-1 network.");

        for (int l = 1; l <= numLeaves; l++) {
            //! System.out.println("Suppressing leaf "+l);
            TripletSet ts = new TripletSet(this, l);

            //! So now we have a triplet set where leaf l was suppressed.
            //! We'll have to do all that relabelling rubbish, *zucht*

            biDAG cat = null;

            if (ts.countTriplets() != 0) {
                cat = ts.buildTree();
            } else {
                //! This can only happen if numLeaves == 3

                cat = biDAG.cherry12();
            }

            if (cat == null) continue;

            Vector leafOrder = new Vector();

            if (cat.testCaterpillar(leafOrder) != false) {
                //! We now actually have enough information to test if this
                //! works...the leaf nodes are in leafOrder. Let's fix the
                //! leaves first...

                correctLeafNodes(leafOrder, l);

                //! Now there is space for leaf l to be added back in...
                //! There are two possibilities, depending on which of the two
                //! last leaves in the caterpillar we subdivide...

                int catLeaves = leafOrder.size();

                int lookup[] = new int[catLeaves];
                int dfr[] = new int[numLeaves + 1];

                for (int scan = 0; scan < catLeaves; scan++) {
                    lookup[scan] = ((biDAG) leafOrder.elementAt(scan)).data;
                    //! System.out.println("lookup["+scan+"] = "+lookup[scan]);
                    dfr[lookup[scan]] = scan;
                    //! System.out.println("dfr["+lookup[scan]+"] = "+scan);
                }

                //! So dfr is kind of the distance from the root, used for fast triplet check

                //! There are two things to try, depending on which bottom edge we subdivide

                //! Let's check the triplets.

                boolean success = true;

                for (int s = 0; s < tripVec.size(); s++) {
                    int t[] = (int[]) tripVec.elementAt(s);

                    int x = t[0];
                    int y = t[1];
                    int z = t[2];

                    if (z == l) continue;        //! is always in
                    if ((x == l) && (dfr[z] < dfr[y])) continue;
                    if ((y == l) && (dfr[z] < dfr[x])) continue;

                    if ((dfr[z] < dfr[y]) && (dfr[z] < dfr[x])) continue;
                    success = false;
                    break;
                }


                if (success) {
                    //! Build the thing and then finish
                    Heuristic.report("Built a simple skew level-1 network (version 1).");

                    Heuristic.report("Got here.");

                    biDAG newnode = ((biDAG) leafOrder.elementAt(catLeaves - 1)).insertAbove(0);

                    biDAG newroot = new biDAG();
                    newroot.child1 = cat;
                    cat.parent = newroot;        //! ADDED THIS FEB 2009 !

                    newroot.child2 = newnode;
                    newnode.secondParent = newroot;

                    biDAG newleaf = new biDAG();
                    newleaf.parent = newnode;
                    newleaf.data = l;
                    newnode.child1 = newleaf;

                    newroot.tripsWithin = this.getTotalWeight();

                    return newroot;
                }

                success = true;

                //! Try swapping the last two elements round, and repeat
                int lastA = lookup[catLeaves - 1];
                int lastB = lookup[catLeaves - 2];
                int swap = dfr[lastA];
                dfr[lastA] = dfr[lastB];
                dfr[lastB] = swap;

                for (int s = 0; s < tripVec.size(); s++) {
                    int t[] = (int[]) tripVec.elementAt(s);

                    int x = t[0];
                    int y = t[1];
                    int z = t[2];

                    if (z == l) continue;        //! is always in
                    if ((x == l) && (dfr[z] < dfr[y])) continue;
                    if ((y == l) && (dfr[z] < dfr[x])) continue;

                    if ((dfr[z] < dfr[y]) && (dfr[z] < dfr[x])) continue;
                    success = false;
                    break;
                }

                if (success) {
                    //! Build the thing and then finish
                    Heuristic.report("Built a simple skew level-1 network (version 2).");


                    biDAG newnode = ((biDAG) leafOrder.elementAt(catLeaves - 2)).insertAbove(0);

                    biDAG newroot = new biDAG();
                    newroot.child1 = cat;
                    cat.parent = newroot;        //! ADDED THIS FEB 2009!

                    newroot.child2 = newnode;
                    newnode.secondParent = newroot;

                    biDAG newleaf = new biDAG();
                    newleaf.parent = newnode;
                    newleaf.data = l;
                    newnode.child1 = newleaf;

                    newroot.tripsWithin = this.getTotalWeight();

                    return newroot;

                }


            }    //! end if-caterpillar...

        }    //! end leaf suppression loop

        Heuristic.report("Was not skew level-1.");

        return null;
    }


    //! This uses a short-cut to get the weights correct by
    //! updating tripsWithin only at the end

    public biDAG buildTree() {
        biDAG papa = new biDAG();

        //! Build the Aho graph...
        Graph g = buildAhoGraph();

        //! Split into two cliques...

        int partition[] = g.getDoubleClique();

        if (partition == null) return null;

		/*
		if(Heuristic.DEBUG)
			{
			System.out.println("Had the double-clique structure:");
			for(int k=1; k<partition.length; k++ )
				{
				System.out.print(partition[k]+" ");
				}
			System.out.println();
			}
		*/

        //! The partition will have only values LEFT and RIGHT...

        //! At some point we have to stop the recursion. We finish once
        //! one of the two cliques has size <= 2;

        int lCount = 0;
        int rCount = 0;

        //! The 0 element of these guys will not be used...
        int leftList[] = new int[partition.length];
        int rightList[] = new int[partition.length];

        //! The 0 element of these guys will not be used...
        int leftBackMap[] = new int[partition.length];
        int rightBackMap[] = new int[partition.length];

        //! Note here that we begin at 1
        for (int scan = 1; scan < partition.length; scan++) {
            if (partition[scan] == Graph.LEFT) {
                lCount++;

                leftList[lCount] = scan;

                //! This is the new leaf numbering of leaf scan...

                leftBackMap[scan] = lCount;
            } else if (partition[scan] == Graph.RIGHT) {
                rCount++;
                rightList[rCount] = scan;
                rightBackMap[scan] = rCount;
            } else
                System.out.println("ERROR#23");
        }

        //! -------------------------

        biDAG leftChild = null;
        biDAG rightChild = null;

        //! Here it gets messy...

        if (lCount == 1) {
            //! In this case it's just a simple leaf...

            leftChild = new biDAG();

            leftChild.data = leftList[1];

			/*
			if(Heuristic.DEBUG)
				{
				System.out.println("Setting only data as "+leftList[1]);
				}
			*/

        } else if (lCount == 2) {
            leftChild = new biDAG();

            //! Here it's a cherry...
            biDAG gchild1 = new biDAG();
            biDAG gchild2 = new biDAG();

            leftChild.child1 = gchild1;
            leftChild.child2 = gchild2;

            gchild1.data = leftList[1];
            gchild2.data = leftList[2];

			/*
			if(Heuristic.DEBUG)
				{
				System.out.println("Setting gchild1 as "+leftList[1]);
				System.out.println("Setting gchild2 as "+leftList[2]);
				}
			*/

            gchild1.parent = leftChild;
            gchild2.parent = leftChild;

        } else {
			/*
			if( Heuristic.DEBUG )
				{
				System.out.println("Recursing left...");
				}
			*/

            //! and here we recurse...
            //! get all the triplets in the LEFT partition...
            //! and continue...

            TripletSet leftBranch = new TripletSet();
            leftBranch.setWeightMatrix(lCount);

            Enumeration e = tripVec.elements();
            while (e.hasMoreElements()) {
                int v[] = (int[]) e.nextElement();

                //! We want all 3 of its elements to be in the left
                //! partition...so for each element check whether it

                if ((partition[v[0]] == Graph.RIGHT) || (partition[v[1]] == Graph.RIGHT) || (partition[v[2]] == Graph.RIGHT))
                    continue;

                //! the backscan variable ensures that the indices are correct...i.e. in
                //! the range [1...leftCount]

                int w = this.getWeight(v[0], v[1], v[2]);

                leftBranch.addTriplet(leftBackMap[v[0]], leftBackMap[v[1]], leftBackMap[v[2]], w);
            }

            leftChild = leftBranch.buildTree();

            if (leftChild == null) return null;

			/*
			if( Heuristic.DEBUG ) System.out.println("Past left recursion");
			*/

            //! Fix labelling in the tree...very crude but zucht let's get it working first...
            leftChild.treeFixLeaves(leftList);
        }

        //! and now the right branch...

        if (rCount == 1) {
            //! In this case it's just a simple leaf...

            rightChild = new biDAG();
            rightChild.data = rightList[1];

			/*
			if(Heuristic.DEBUG)
				{
				System.out.println("Setting only data as"+rightList[1]);
				}
			*/

        } else if (rCount == 2) {
            rightChild = new biDAG();

            //! Here it's a cherry...
            biDAG gchild1 = new biDAG();
            biDAG gchild2 = new biDAG();

            rightChild.child1 = gchild1;
            rightChild.child2 = gchild2;

            gchild1.data = rightList[1];
            gchild2.data = rightList[2];

            gchild1.parent = rightChild;
            gchild2.parent = rightChild;

			/*
			if(Heuristic.DEBUG)
				{
				System.out.println("Setting gchild1 as "+rightList[1]);
				System.out.println("Setting gchild2 as "+rightList[2]);
				}
			*/

        } else {

			/*
			if( Heuristic.DEBUG )
				{
				System.out.println("Recursing...");
				}
			*/

            //! and here we recurse...
            //! get all the triplets in the RIGHT partition...
            //! and continue...

            TripletSet rightBranch = new TripletSet();
            rightBranch.setWeightMatrix(rCount);

            Enumeration e = tripVec.elements();
            while (e.hasMoreElements()) {
                int v[] = (int[]) e.nextElement();

                //! We want all 3 of its elements to be in the left
                //! partition...so for each element check whether it
                //! is in the LEFT set...

                if ((partition[v[0]] == Graph.LEFT) || (partition[v[1]] == Graph.LEFT) || (partition[v[2]] == Graph.LEFT))
                    continue;

                //! the backscan variable ensures that the indices are correct...i.e. in
                //! the range [1...rightCount]

                int w = this.getWeight(v[0], v[1], v[2]);

                rightBranch.addTriplet(rightBackMap[v[0]], rightBackMap[v[1]], rightBackMap[v[2]], w);
            }

            rightChild = rightBranch.buildTree();

            if (rightChild == null) return null;

			/*
			if( Heuristic.DEBUG )
				{
				System.out.println("Past recursive step!");
				}
			*/

            //! And now fix the leaf numbering...
            rightChild.treeFixLeaves(rightList);
        }

        papa.child1 = leftChild;
        papa.child2 = rightChild;
        leftChild.parent = papa;
        rightChild.parent = papa;

        papa.tripsWithin = this.getTotalWeight();

        return papa;
    }


    public static biDAG convertSim1(Sim1 bestNet) {
        biDAG root = new biDAG();

        boolean leftPattern[] = bestNet.bestBi.clone();
        boolean rightPattern[] = bestNet.bestNeg.clone();

        boolean done = false;

        biDAG retic = new biDAG();
        biDAG reticLeaf = new biDAG();
        retic.child1 = reticLeaf;
        reticLeaf.data = bestNet.retic;
        reticLeaf.parent = retic;

        biDAG prev = root;

        while (!done) {
            int code = Heuristic.patternToInt(leftPattern);
            int leaf = bestNet.firstleaf[code];
            if (leaf == 0) break;
            Heuristic.report(leaf + " ");
            leftPattern[leaf - 1] = false;

            biDAG b = new biDAG();
            b.parent = prev;

            b.parent.child1 = b;

            biDAG c = new biDAG();
            c.parent = b;
            c.data = leaf;

            b.child2 = c;
            prev = b;
        }

        retic.parent = prev;
        prev.child1 = retic;

        prev = root;

        Heuristic.report("Right side (starting closest to the root):");

        while (!done) {
            int code = Heuristic.patternToInt(rightPattern);
            int leaf = bestNet.firstleaf[code];
            if (leaf == 0) break;
            Heuristic.report(leaf + " ");
            rightPattern[leaf - 1] = false;

            biDAG b = new biDAG();
            b.parent = prev;

            b.parent.child2 = b;

            biDAG c = new biDAG();
            c.parent = b;
            c.data = leaf;

            b.child1 = c;
            prev = b;
        }

        retic.secondParent = prev;
        prev.child2 = retic;
        return root;
    }


    public biDAG buildBlitz(int level, boolean usemaxsn) {
        Heuristic.report("Entering buildBlitz, recursion level " + level);
        Heuristic.report("Constructing the blocks of the partition...");
        if (usemaxsn) Heuristic.report("(we are using maximum SN-sets as blocks)");

        Vector msn = null;

        boolean localPerfection[] = new boolean[1];
        localPerfection[0] = false;

        if (usemaxsn) msn = this.getMaxSNSets();
        else msn = this.getPartition(localPerfection);

        if (localPerfection[0]) {
            System.out.println("// COMMENT: We achieved non-trivial local perfection.");
        }

        Heuristic.report("...done. There are " + msn.size() + " blocks.");
        if (Heuristic.DEBUG) {
            for (int x = 0; x < msn.size(); x++) ((KSet) msn.elementAt(x)).dump();
        }

        biDAG root = null;
        Vector mstar = null;

        boolean success = false;

        int topLevTrips = 0;
        int autoHappyTrips = 0;
        int autoUnhappyTrips = 0;
        int allTopLevTrips = 0;

        //! in general topLevTrips <= allTopLevTrips

        if (msn.size() == 2) {
            Heuristic.report("There were only 2 blocks.");
            root = biDAG.cherry12();
            mstar = msn;

            //! Don't forget to count triplets here...
            topLevTrips = 0;    //! there are no triplets

            //! We only call this for the side effect of computing happy and unhappy triplets!
            TripletSet dummy = this.buildTPrime(mstar);

            autoHappyTrips = dummy.happyTrips;
            autoUnhappyTrips = dummy.unhappyTrips;
            allTopLevTrips = 0;

            dummy = null;

            success = true;
        } else    //! there are at least 3 maximum SN-sets
        {
            for (int loop = 0; loop <= 0; loop++) {
                mstar = msn;

                TripletSet treal = this.buildTPrime(mstar);

                autoHappyTrips = treal.happyTrips;    //! We get these triplets whatever happens...
                autoUnhappyTrips = treal.unhappyTrips;    //! We lose these whatever happens...
                allTopLevTrips = treal.getTotalWeight();

                if (usemaxsn || localPerfection[0]) {
                    Heuristic.report("Let's try and build a simple level-1 structure...");
                    root = treal.buildSimpleLevel1();
                }

                if (root != null) {
                    Heuristic.report("Successfully built simple level-1 network.");
                    //! root.experimentalLeafPrint();
                    //! root.dumpDAG();

                    topLevTrips = treal.getTotalWeight();    //! We got everything!

                    success = true;
                    break;
                } else {

                    Heuristic.report("Trying to build best-possible level-1 network...");

                    //! Insert best-possible-level-1 network algorithm here...
                    //! treal will already contain the weighting information we need...

                    int funnyTrips[][] = treal.getArray();

                    int funnyWeight[] = new int[funnyTrips.length];
                    int totalWeight = 0;

                    for (int x = 0; x < funnyTrips.length; x++) {
                        //! System.out.println("::: "+funnyTrips[x][0]+" "+funnyTrips[x][1]+" "+funnyTrips[x][2] );
                        funnyWeight[x] = treal.getWeight(funnyTrips[x][0], funnyTrips[x][1], funnyTrips[x][2]);
                        if (funnyWeight[x] < 0) {
                            System.out.println("Major error!");
                            System.exit(0);
                        }
                        totalWeight += funnyWeight[x];
                    }

                    Heuristic.report("Triplets have total input weight: " + totalWeight);

                    int query = treal.tellLeaves();
                    Heuristic.report("Partition has " + query + " blocks.");

                    root = new biDAG();
                    int competition = 0;

                    if (query <= Heuristic.SIM_MAX) {
                        Heuristic.report("Using the exact simple level-1 algorithm.");

                        Sim1 bestNet = Heuristic.buildBestSimpleLevel1(funnyTrips, funnyWeight);

                        Heuristic.report("Best simple level-1 network gets a weight of " + bestNet.best);

                        Heuristic.report("Reticulation leaf is " + bestNet.retic);
                        Heuristic.report("Left side (starting closest to the root):");

                        root = convertSim1(bestNet);
                        root.tripsWithin = bestNet.best;

                        competition = bestNet.best;

                    } else {
                        //! Use the greedy heuristic

                        Heuristic.report("Using the greedy simple level-1 algorithm.");

                        root = Heuristic.buildGreedySimpleLevel1(funnyTrips, funnyWeight);
                        competition = root.tripsWithin;
                    }

                    WuAnswer wa = null;

                    Heuristic.report("The simple level-1 network got a weight of " + competition);

                    if (query <= Heuristic.WU_CEILING) {
                        wa = WuWeighted.wu(funnyTrips, funnyWeight);
                        Heuristic.report("Best tree gets a weight of " + wa.tripsGot);

                        if (wa.tripsGot >= competition) {
                            Heuristic.report("Tree is better, I will use that.");
                            root = wa.root;
                            topLevTrips = wa.tripsGot;
                        } else topLevTrips = competition;
                    } else {
                        topLevTrips = competition;
                    }

                    success = true;
                    break;

                }    //! end root != null

            }    //! end zeroth iteration
        }    //! end at least 3 max sn sets

        if (!success) return null;

        int totTrips = topLevTrips + autoHappyTrips;
        //! need to add the results of the subinstances to this...

        Heuristic.report("// Immediately lost " + autoUnhappyTrips + " units of triplet weight due to the partition.");

        //! Now do the funky stuff....
        //! root will contain a network with as many leaves as maximum SN-sets...
        //! i.e. the size of mstar...

        biDAG leafToNode[] = new biDAG[mstar.size() + 1];

        //! This assumes that 'visited' is clean...

        root.getDAGLeafMap(leafToNode);

        //! Now leadToNode[1] points to the biDAG containing element 1, and so on...
        //! ---------------------------------------------------------------------------
        //! Not sure if we need to do this, the only thing that could get hurt is
        //! the printing out of the DAG, but I think it's important to do it anyway...
        //! ---------------------------------------------------------------------------

        root.resetVisited();

        int optelsom = autoHappyTrips + autoUnhappyTrips + allTopLevTrips;

        //! xl -> expand leaf...
        for (int xl = 1; xl <= mstar.size(); xl++) {
            KSet subLeaves = (KSet) mstar.elementAt(xl - 1);
            int numSubLeaves = subLeaves.size();

            int backMap[] = new int[numSubLeaves + 1];

            biDAG subroot = null;

            Heuristic.report("Looking inside block " + xl);

            if (numSubLeaves > 2) {
                Heuristic.report("Ah, this has more than 3 leaves...");
                TripletSet recurse = this.induceTripletSet(subLeaves, backMap);

                optelsom += recurse.getTotalWeight();

                subroot = recurse.buildBlitz(level + 1, usemaxsn);

                totTrips += subroot.tripsWithin;    //! add up the subsolutions

                if (subroot == null) return null;
            } else {
                if (numSubLeaves == 1) {
                    Heuristic.report("Ah, this is a single leaf...");
                    //! Simply fix what's already there.
                    biDAG tweak = leafToNode[xl];
                    int leafNum = subLeaves.getFirstElement();
                    Heuristic.report("Replacing leaf " + tweak.data + " with " + leafNum);
                    tweak.data = leafNum;
                } else

                {
                    //! numSubLeaves == 2

                    Heuristic.report("Ah, this has two leaves.");
                    Heuristic.report("Entering danger zone");

                    biDAG tweak = leafToNode[xl];

                    int pick[] = subLeaves.getFirstTwoElements();

                    biDAG branch = biDAG.cherry12();

                    branch.child1.data = pick[0];
                    branch.child2.data = pick[1];

                    branch.parent = tweak.parent;

                    if (tweak.parent == null) Heuristic.report("Null pointer discovered...");

                    if (tweak.parent.child1 == tweak) tweak.parent.child1 = branch;
                    else if (tweak.parent.child2 == tweak) tweak.parent.child2 = branch;
                    else
                        System.out.println("Mega problem, please report to programmer");

                    Heuristic.report("Leaving danger zone?");

                }
                continue;    //! no need to do the rest...
            }

            //! That subbranch worked, so fix the leaves and then graft back

            Heuristic.report("Still at recursion level " + level);

            subroot.dagFixLeaves(backMap);

            //! Is this necessary??? Might it be dangerous even???
            subroot.resetFixLeaves();

            biDAG knoop = leafToNode[xl];

            Heuristic.report("Problem here? " + knoop);

            subroot.parent = knoop.parent;

            Heuristic.report("Or here?");

            if (knoop.parent.child1 == knoop) knoop.parent.child1 = subroot;
            else if (knoop.parent.child2 == knoop) knoop.parent.child2 = subroot;
            else System.out.println("BL2: That really shouldn't have happened");

            Heuristic.report("Ending iteration.");
        }

        if (optelsom != this.getTotalWeight()) {
            System.out.println("FATAL ERROR at level " + level + ": Somehow after partitioning we lost triplets: " + optelsom + " versus " + this.getTotalWeight());
            System.exit(0);
        }

        root.tripsWithin = totTrips;

        return root;
    }


}


//! ---------------------------------------------------------------------------------------
//! ---------------------------------------------------------------------------------------
//! ---------------------------------------------------------------------------------------
//! ---------------------------------------------------------------------------------------
//! ---------------------------------------------------------------------------------------


public class Heuristic {
    public static final String VERSION = "LEV1ATHAN Version 1.0, 21 september 2009";

    public static final boolean DEBUG = false;

    public static boolean BIGWEIGHTS = false;

    public static int recDepth = 0;

    public static boolean DENSE;
    public static boolean USEMAXSN;
    public static int BLOCKS;
    public static boolean MISSING;
    public static boolean SURPLUS;
    public static int WU_CEILING;
    public static int SIM_MAX;
    public static boolean TEST_TREE;
    public static boolean TEST_EXACT;
    public static boolean TEST_GREEDY;
    public static boolean NOPOSTPROCESS;
    public static boolean ENEWICK;
    public static boolean FORTYEIGHT;
    public static int SDWEIGHT;
    public static boolean NOPERFECTION;
    public static boolean SDRADICAL;
    public static boolean NONETWORK;
    public static boolean CORRUPT;
    public static float CORRUPT_PROB;
    public static boolean SAMPLE;
    public static float SAMPLE_PROB;
    public static boolean DUMPTRIPLETS;
    public static boolean DUMPCONSISTENT;

    public static final boolean DENSE_DEFAULT = false;
    public static final boolean USEMAXSN_DEFAULT = false;
    public static final int BLOCKS_DEFAULT = 20;
    public static final boolean MISSING_DEFAULT = false;
    public static final boolean SURPLUS_DEFAULT = false;
    public static final int WU_CEILING_DEFAULT = 10;
    public static final int SIM_MAX_DEFAULT = 12;
    public static final boolean TEST_TREE_DEFAULT = false;
    public static final boolean TEST_EXACT_DEFAULT = false;
    public static final boolean TEST_GREEDY_DEFAULT = false;
    public static final boolean NOPOSTPROCESS_DEFAULT = false;
    public static final boolean ENEWICK_DEFAULT = false;
    public static final boolean FORTYEIGHT_DEFAULT = false;
    public static final boolean NONETWORK_DEFAULT = false;
    public static final boolean CORRUPT_DEFAULT = false;
    public static final boolean SAMPLE_DEFAULT = false;
    public static final boolean DUMPTRIPLETS_DEFAULT = false;
    public static final boolean DUMPCONSISTENT_DEFAULT = false;

    public static final int SDWEIGHT_DEFAULT = 1;
    public static final int INTERNAL_COEFF_DEFAULT = 4;
    public static final int EXTERNAL_COEFF_DEFAULT = 7;
    public static final int HAPPY_COEFF_DEFAULT = 12;

    public static int INTERNAL_COEFF = INTERNAL_COEFF_DEFAULT;
    public static int EXTERNAL_COEFF = EXTERNAL_COEFF_DEFAULT;
    public static int HAPPY_COEFF = HAPPY_COEFF_DEFAULT;

    public static boolean EXPAND = false;

    //! public static final int DUMMYLEAF = 1000001;

    public static final void report(String text) {
        if (DEBUG) System.out.println(text);
    }

    //! -------------------------------------------------------------------------------------------

    public final static int GREEDY_LEFT = 6;
    public final static int GREEDY_RIGHT = 7;

    //! --------------------------------------------------------------------------------------------

    public static Hashtable nameToNum;
    public static Hashtable numToName;

    public static int seenLeaves = 0;

    public static int getLeafNumber(String leaf) {
        if (nameToNum == null) nameToNum = new Hashtable();

        Integer i = (Integer) nameToNum.get(leaf);

        if (i != null) {
            return i.intValue();
        }

        seenLeaves++;

        i = new Integer(seenLeaves);

        nameToNum.put(leaf, i);

        if (numToName == null) {
            numToName = new Hashtable();
        }

        numToName.put(i, leaf);

        return seenLeaves;
    }


    public static String getLeafName(int num) {
        Integer i = new Integer(num);

        if (numToName == null) {
            numToName = new Hashtable();
        }

        String s = (String) numToName.get(i);

        return s;
    }

    //! -------------------------------------------------------------------------------------------

    public static biDAG buildGreedySimpleLevel1(int trips[][], int weight[]) {
        int highest = 0;

        int bestWeight = -1;

        for (int x = 0; x < trips.length; x++) {
            if (trips[x][0] > highest) highest = trips[x][0];
            if (trips[x][1] > highest) highest = trips[x][1];
            if (trips[x][2] > highest) highest = trips[x][2];
        }

        int n = highest;

        Heuristic.report("Greedy algorithm is building simple level-1 network with " + n + " leaves.");

        //! lazy but should be OK...
        Integer intobj[] = new Integer[n + 1];
        for (int x = 1; x < intobj.length; x++) intobj[x] = new Integer(x);

        biDAG lev1[] = new biDAG[n + 1];    //! store the best networks...

        for (int recomb = 1; recomb <= n; recomb++) {
            //! Ok so we're going to try all possible recomb nodes...

            Heuristic.report("Greedy alg is trying " + recomb + " as the recomb node.");

            Vector left = new Vector();    //! The left side...
            Vector right = new Vector();    //! The right side...

            boolean doneVec[] = new boolean[n + 1];    //! which leaves have already been allocated...

            for (int x = 1; x < doneVec.length; x++) doneVec[x] = (x == recomb);

            int numLeavesAdded = 1;        //! recomb has been added already

            while (numLeavesAdded < n) {
                int bestLeaf = 0;
                int bestScore = 0;
                boolean scoreActive = false;
                int bestPos = 0;
                int bestSide = 0;

                Heuristic.report("New leaf iteration.");

                for (int leaf = 1; leaf < doneVec.length; leaf++) {
                    if (doneVec[leaf]) continue;    //! don't bother considering it...already in the network...

                    Heuristic.report("Considering adding leaf " + leaf);

                    //! So we're going to try adding leaf 'leaf' in all possible places...

                    //! Vector.add shifts elements to the right, last position requires no shifting...

                    for (int pos = 0; pos <= left.size(); pos++) {
                        left.add(pos, intobj[leaf]);

                        //! ------ Compute the score that we get for this... ------------------------------------------
                        //! This code can be heavily speeded up, this is prototyping only!

                        int poslook[] = new int[n + 1];
                        int sidelook[] = new int[n + 1];

                        //! This creates a look-up for side and position in the caterpillar

                        for (int dream = 0; dream < left.size(); dream++) {
                            int val = ((Integer) left.elementAt(dream)).intValue();
                            sidelook[val] = GREEDY_LEFT;
                            poslook[val] = dream;
                        }

                        for (int dream = 0; dream < right.size(); dream++) {
                            int val = ((Integer) right.elementAt(dream)).intValue();
                            sidelook[val] = GREEDY_RIGHT;
                            poslook[val] = dream;
                        }

                        int score = 0;

                        doneVec[leaf] = true;    //! remember to unset this later!

                        tripcheck:
                        for (int spin = 0; spin < trips.length; spin++) {
                            int a = trips[spin][0];
                            int b = trips[spin][1];
                            int c = trips[spin][2];

                            if ((a != leaf) && (b != leaf) && (c != leaf)) continue;

                            if ((!doneVec[a]) || (!doneVec[b]) || (!doneVec[c])) continue;

                            //! So do the usual boring tests for consistency.....

                            int w = weight[spin];

                            if (c == recomb) {
                                if (sidelook[a] != sidelook[b]) score -= w;
                                else score += w;
                                continue tripcheck;
                            }

                            if (a == recomb) {
                                if (sidelook[b] != sidelook[c]) {
                                    score += w;
                                    continue tripcheck;
                                }

                                if (poslook[b] > poslook[c]) score += w;
                                else score -= w;

                                continue tripcheck;
                            }

                            if (b == recomb) {
                                if (sidelook[a] != sidelook[c]) {
                                    score += w;
                                    continue tripcheck;
                                }
                                if (poslook[a] > poslook[c]) score += w;
                                else score -= w;

                                continue tripcheck;
                            }

                            //! So none of the guys are recombination leaves

                            if ((sidelook[a] == sidelook[b]) && (sidelook[c] != sidelook[a])) {
                                score += w;
                                continue tripcheck;
                            }

                            if ((sidelook[a] == sidelook[c]) && (sidelook[b] != sidelook[a])) {
                                score -= w;
                                continue tripcheck;
                            }

                            if ((sidelook[b] == sidelook[c]) && (sidelook[a] != sidelook[b])) {
                                score -= w;
                                continue tripcheck;
                            }

                            if ((poslook[c] < poslook[a]) && (poslook[c] < poslook[b])) score += w;
                            else {
                                score -= w;
                            }

                        }


                        doneVec[leaf] = false;

                        //! ---------------------------------------------------------------------

                        left.remove(pos);

                        if ((!scoreActive) || (scoreActive && (score > bestScore))) {
                            scoreActive = true;
                            bestLeaf = leaf;
                            bestPos = pos;
                            bestScore = score;
                            bestSide = GREEDY_LEFT;
                        }
                    } //! end for pos-left

                    for (int pos = 0; pos <= right.size(); pos++) {
                        right.add(pos, intobj[leaf]);

                        //! ------ Compute the score that we get for this... --------------------
                        //! This code can be heavily speeded up, this is prototyping only!

                        int poslook[] = new int[n + 1];
                        int sidelook[] = new int[n + 1];

                        //! This creates a look-up for side and position in the caterpillar

                        for (int dream = 0; dream < left.size(); dream++) {
                            int val = ((Integer) left.elementAt(dream)).intValue();
                            sidelook[val] = GREEDY_LEFT;
                            poslook[val] = dream;
                        }

                        for (int dream = 0; dream < right.size(); dream++) {
                            int val = ((Integer) right.elementAt(dream)).intValue();
                            sidelook[val] = GREEDY_RIGHT;
                            poslook[val] = dream;
                        }

                        int score = 0;

                        doneVec[leaf] = true;   //! remember to unset this later!

                        pirtcheck:
                        for (int spin = 0; spin < trips.length; spin++) {
                            int a = trips[spin][0];
                            int b = trips[spin][1];
                            int c = trips[spin][2];

                            if ((a != leaf) && (b != leaf) && (c != leaf))
                                continue;    //! only consider triplets containing leaf

                            if ((!doneVec[a]) || (!doneVec[b]) || (!doneVec[c]))
                                continue;    //! only consider fixed leaves

                            int w = weight[spin];

                            if (c == recomb) {
                                if (sidelook[a] != sidelook[b]) score -= w;
                                else score += w;
                                continue pirtcheck;
                            }

                            if (a == recomb) {
                                if (sidelook[b] != sidelook[c]) {
                                    score += w;
                                    continue pirtcheck;
                                }

                                if (poslook[b] > poslook[c]) score += w;
                                else score -= w;

                                continue pirtcheck;
                            }

                            if (b == recomb) {
                                if (sidelook[a] != sidelook[c]) {
                                    score += w;
                                    continue pirtcheck;
                                }
                                if (poslook[a] > poslook[c]) score += w;
                                else score -= w;

                                continue pirtcheck;
                            }

                            //! So none of the guys are recombination leaves

                            if ((sidelook[a] == sidelook[b]) && (sidelook[c] != sidelook[a])) {
                                score += w;
                                continue pirtcheck;
                            }

                            if ((sidelook[a] == sidelook[c]) && (sidelook[b] != sidelook[a])) {
                                score -= w;
                                continue pirtcheck;
                            }

                            if ((sidelook[b] == sidelook[c]) && (sidelook[a] != sidelook[b])) {
                                score -= w;
                                continue pirtcheck;
                            }

                            if ((poslook[c] < poslook[a]) && (poslook[c] < poslook[b])) score += w;
                            else {
                                score -= w;
                            }

                        }

                        doneVec[leaf] = false;

                        //! ---------------------------------------------------------------------

                        right.remove(pos);

                        if ((!scoreActive) || (scoreActive && (score > bestScore))) {
                            scoreActive = true;
                            bestLeaf = leaf;
                            bestPos = pos;
                            bestScore = score;
                            bestSide = GREEDY_RIGHT;
                        }

                    } //! end for pos-right

                } //! end for leaf loop

                //! So we simply add in the best one...

                Vector aanvul = null;
                if (bestSide == GREEDY_LEFT) aanvul = left;
                else if (bestSide == GREEDY_RIGHT) aanvul = right;
                else {
                    System.out.println("Stupid error.");
                    System.exit(0);
                }

                aanvul.add(bestPos, intobj[bestLeaf]);
                doneVec[bestLeaf] = true;

                numLeavesAdded++;
            }    //! end while num added loop

            //! Build the network, compute the weight of triplets it is consistent with and
            //! store this somewhere

            biDAG myguy = glueSimpleLevel1(left, right, recomb);

            dagExplore de = new dagExplore(myguy);

            int myweight = 0;

            for (int scan = 0; scan < trips.length; scan++) {
                if (de.consistent(trips[scan][0], trips[scan][1], trips[scan][2])) {
                    myweight += weight[scan];
                }
            }

            myguy.tripsWithin = myweight;

            lev1[recomb] = myguy;

        } //! end for recomb loop

        //! return the best one!

        int maxVal = -1;
        int maxIndex = -1;

        for (int loop = 1; loop < lev1.length; loop++) {
            if (lev1[loop].tripsWithin > maxVal) {
                maxVal = lev1[loop].tripsWithin;
                maxIndex = loop;
            }
        }

        lev1[maxIndex].resetVisited();

        return lev1[maxIndex];
    }

    //! -------------------------------------------------------------------------------------------------------------------

    //! builds the simple level-1 network with left caterpillar left, ..., right, and recomb recomb!

    private static biDAG glueSimpleLevel1(Vector left, Vector right, int recomb) {
        biDAG root = new biDAG();

        biDAG recombNode = new biDAG();
        biDAG recombLeaf = new biDAG();

        recombLeaf.data = recomb;
        recombLeaf.parent = recombNode;

        recombNode.child1 = recombLeaf;
        recombNode.child2 = null;

        if (left.size() == 0) {
            root.child1 = recombNode;
            recombNode.parent = root;
        } else {
            //! Now add the little bastards in...

            biDAG lastGuy = root;

            for (int loop = 0; loop < left.size(); loop++) {
                biDAG b = new biDAG();

                b.data = ((Integer) left.elementAt(loop)).intValue();

                if (lastGuy == root) {
                    b.parent = root;
                    root.child1 = b;
                    lastGuy = b;
                } else {
                    biDAG jam = new biDAG();

                    jam.parent = lastGuy.parent;

                    if (lastGuy.parent.child1 == lastGuy) lastGuy.parent.child1 = jam;
                    else if (lastGuy.parent.child2 == lastGuy) lastGuy.parent.child2 = jam;
                    else {
                        System.out.println("Urururu!");
                        System.exit(0);
                    }


                    jam.child1 = lastGuy;
                    lastGuy.parent = jam;

                    jam.child2 = b;
                    b.parent = jam;

                    lastGuy = b;
                }
            } //! end loop left


            //! Connect lastGuy to the recomb node...

            biDAG weld = new biDAG();

            weld.child1 = lastGuy;
            weld.parent = lastGuy.parent;

            if (lastGuy.parent.child1 == lastGuy) lastGuy.parent.child1 = weld;
            else if (lastGuy.parent.child2 == lastGuy) lastGuy.parent.child2 = weld;
            else {
                System.out.println("Error with glueing!");
                System.exit(0);
            }

            lastGuy.parent = weld;

            weld.child2 = recombNode;
            recombNode.parent = weld;
        } //! end else left

        if (right.size() == 0) {
            root.child2 = recombNode;
            recombNode.secondParent = root;
        } else {

            //! Now add the little bastards in...

            biDAG lastGuy = root;

            for (int loop = 0; loop < right.size(); loop++) {
                biDAG b = new biDAG();

                b.data = ((Integer) right.elementAt(loop)).intValue();

                if (lastGuy == root) {
                    b.parent = root;
                    root.child2 = b;
                    lastGuy = b;
                } else {
                    biDAG jam = new biDAG();

                    jam.parent = lastGuy.parent;

                    if (lastGuy.parent.child1 == lastGuy) lastGuy.parent.child1 = jam;
                    else if (lastGuy.parent.child2 == lastGuy) lastGuy.parent.child2 = jam;
                    else {
                        System.out.println("Urururu!");
                        System.exit(0);
                    }

                    jam.child1 = lastGuy;
                    lastGuy.parent = jam;

                    jam.child2 = b;
                    b.parent = jam;

                    lastGuy = b;
                }
            } //! end loop left


            //! Connect lastGuy to the recomb node...

            biDAG weld = new biDAG();

            weld.child1 = lastGuy;
            weld.parent = lastGuy.parent;

            if (lastGuy.parent.child1 == lastGuy) lastGuy.parent.child1 = weld;
            else if (lastGuy.parent.child2 == lastGuy) lastGuy.parent.child2 = weld;
            else {
                System.out.println("Error with glueing!");
                System.exit(0);
            }

            lastGuy.parent = weld;

            weld.child2 = recombNode;
            recombNode.secondParent = weld;

        } //! end else right

        return root;
    }


    public static Sim1 buildBestSimpleLevel1(int trips[][], int weight[]) {
		/*
		for( int scan=0; scan<trips.length; scan++)
			{
			System.out.println(">>> "+trips[scan][0]+" "+trips[scan][1]+" "+trips[scan][2]+" weight "+weight[scan]);
			}
		*/

        //! IMPORTANT: Assumes that the interval on the trips is 1...n for some n,
        //! that the interval is closed, and that weight[i] is the
        //! weight of triplet i, a weight of 1 means 'unweighted'

        int highest = 0;

        for (int x = 0; x < trips.length; x++) {
            if (trips[x][0] > highest) highest = trips[x][0];
            if (trips[x][1] > highest) highest = trips[x][1];
            if (trips[x][2] > highest) highest = trips[x][2];
        }

        if (highest == 0) {
            //! System.out.println("Contained no triplets.");

            Sim1 s = new Sim1();

            s.best = 0;

            return s;
        }

        //! System.out.println("Highest leaf was "+highest);

        //! So all leaves are assumed to be in the range 1...highest

        int overallBest = -1;
        int bestLookup[] = null;
        int bestFirstleaf[] = null;
        int bestRetic = 0;

        boolean bestBi[] = new boolean[highest];
        boolean bestNeg[] = new boolean[highest];

        for (int retic = 1; retic <= highest; retic++) {
            //! System.out.println("*** Trying leaf "+retic+" as reticulation.");

            //! We're going to remove leaf 'retic' assuming that it's the
            //! reticulation leaf

            //! So, for every subset of leaves, we build the optimal
            //! caterpillar that has retic as its last leaf

            int roofsubset = (1 << highest);

            //! suppose highest=3, roofsubset = 1<<3 = 8 so we
            //! should scan from 0 to 7

            //! System.out.println("Roofsubset is "+roofsubset);

            int lookup[] = new int[roofsubset];
            int firstleaf[] = new int[roofsubset];

            boolean bipattern[] = new boolean[highest];
            boolean negpattern[] = new boolean[highest];

            for (int x = 0; x < bipattern.length; x++) bipattern[x] = true;

            bipattern[retic - 1] = false;    //! Switch it off!

            buildBestReticCaterpillars(trips, weight, bipattern, lookup, firstleaf, retic);

            //! Ok so now lookup will contain information how to build for each subset the best
            //! caterpillar ending in retic.

            for (int bichoose = 0; bichoose < roofsubset; bichoose++) {
                intToPattern(bichoose, bipattern);

                if (bipattern[retic - 1]) continue;        //! sloppy but should work

                for (int p = 0; p < bipattern.length; p++) negpattern[p] = !bipattern[p];    //! negation

                negpattern[retic - 1] = false;            //! don't want the reticulation vertex either...

                int negchoose = patternToInt(negpattern);    //! probably dumb but oh well

				/*
				System.out.println("LEFT CAT:");

				for(int x=0; x<bipattern.length; x++ )
					{
					if( bipattern[x] ) System.out.print((x+1)+" ");
					}
				System.out.println();

				System.out.println("RIGHT CAT:");

				for(int x=0; x<negpattern.length; x++ )
					{
					if( negpattern[x] ) System.out.print((x+1)+" ");
					}
				System.out.println();
				*/

                int leftIn = lookup[bichoose];
                int rightIn = lookup[negchoose];

                int prov = leftIn + rightIn;

                //! System.out.println("Left cat. got "+leftIn+" and right got "+rightIn+" so a lower bound of "+prov);

                int t[] = new int[3];
                for (int scan = 0; scan < trips.length; scan++) {
                    t[0] = trips[scan][0];
                    t[1] = trips[scan][1];
                    t[2] = trips[scan][2];

                    int w = weight[scan];

                    //! System.out.println("Examining triplet "+t[0]+" "+t[1]+" | "+t[2]);

                    int xcode = t[0] - 1;
                    int ycode = t[1] - 1;
                    int zcode = t[2] - 1;

                    if (bipattern[xcode] && negpattern[ycode]) continue;    //! can't possibly be in
                    if (negpattern[xcode] && bipattern[ycode]) continue;

                    //! System.out.println("Not rejected for immediate inconsistency...");

                    if (bipattern[xcode] && bipattern[ycode] && bipattern[zcode]) continue;    //! already counted
                    if (negpattern[xcode] && negpattern[ycode] && negpattern[zcode]) continue;

                    //! System.out.println("Not rejected for already being included...");

                    if (bipattern[xcode] && bipattern[ycode] && negpattern[zcode]) {
                        //! System.out.println("Reached to other side.");
                        prov += w;
                        continue;

                    }
                    if (negpattern[xcode] && negpattern[ycode] && bipattern[zcode]) {
                        //! System.out.println("Reached to other side.");
                        prov += w;
                        continue;
                    }

                    if (bipattern[xcode] && bipattern[ycode] && (t[2] == retic))            //! wasn't counted within the cat.
                    {
                        //! System.out.println("Tail was reticulation!");
                        prov += w;
                        continue;
                    }

                    if (negpattern[xcode] && negpattern[ycode] && (t[2] == retic)) {
                        //! System.out.println("Tail was reticulation!");
                        prov += w;
                        continue;
                    }

                    if (bipattern[xcode] && (t[1] == retic) && negpattern[zcode]) {
                        //! System.out.println("Reticulation was pivot");
                        prov += w;
                        continue;
                    }

                    if (negpattern[xcode] && (t[1] == retic) && bipattern[zcode]) {
                        //! System.out.println("Reticulation was pivot");
                        prov += w;
                        continue;
                    }

                    if ((t[0] == retic) && bipattern[ycode] && negpattern[zcode]) {
                        //! System.out.println("Reticulation was pivot");
                        prov += w;
                        continue;
                    }

                    if ((t[0] == retic) && negpattern[ycode] && bipattern[zcode]) {
                        //! System.out.println("Reticulation was pivot");
                        prov += w;
                        continue;
                    }

                    //! System.out.println("Triplet not counted.");
                    //! I think that are all possibilities


                } //! end for loop triplets

                if (prov > overallBest) {
                    //! System.out.println("New best: "+prov);
                    overallBest = prov;
                    bestLookup = lookup;
                    bestRetic = retic;
                    bestFirstleaf = firstleaf;

                    for (int s = 0; s < bipattern.length; s++) {
                        bestBi[s] = bipattern[s];
                        bestNeg[s] = negpattern[s];
                    }
                }


            }    //! end for loop subsets


        }    //! end for loop reticulations

        Sim1 s = new Sim1();

        s.best = overallBest;
        s.retic = bestRetic;
        s.lookup = bestLookup;
        s.firstleaf = bestFirstleaf;
        s.bestBi = bestBi;
        s.bestNeg = bestNeg;

        return s;
    }

    //! assumes that bitpattern[retic] = false;

    //! builds the best caterpillar that uses leaves from bitpattern

    public static void buildBestReticCaterpillars(int trips[][], int weight[], boolean bitpattern[], int lookup[], int firstleaf[], int reticLeaf) {
        int leaves = 0;

        int num = patternToInt(bitpattern);

        if (lookup[num] != 0) return;        //! we've been here before...

        if (bitpattern[reticLeaf - 1] != false) {
            System.out.println("SOMETHING WENT WRONG!");
            System.exit(0);
        }

        //! Make a copy of the vector...

        boolean myarray[] = new boolean[bitpattern.length];

        for (int x = 0; x < bitpattern.length; x++) {
            myarray[x] = bitpattern[x];
            if (myarray[x]) {
                leaves++;
            }
        }

        int leavesInOrder[] = new int[leaves];
        int index = 0;

        for (int x = 0; x < myarray.length; x++) {
            if (myarray[x]) {
                leavesInOrder[index] = x;
                index++;
            }
        }

        if (leaves <= 1) {
            //! Base case: combined with retic this is just a cherry, no triplets involved...
            lookup[num] = 0;
            if (leaves == 1) {
                //! System.out.println("A small caterpillar with "+leaves+" leaves.");
                firstleaf[num] = leavesInOrder[0] + 1;
            } else {
                firstleaf[num] = 0;
            }

            return;
        }

        //! Otherwise we will try leaving out every leaf, one at a time

        int bestSoFar = -1;
        int bestLeafSoFar = -1;

        for (int drop = 0; drop < leaves; drop++) {
            int dropLeaf = leavesInOrder[drop] + 1;

            myarray[dropLeaf - 1] = false;

            int subset = patternToInt(myarray);

            buildBestReticCaterpillars(trips, weight, myarray, lookup, firstleaf, reticLeaf);

            int weightsubset = lookup[subset];    //! Weight of all the triplets contained within the subset...

            int t[] = new int[3];

            for (int loop = 0; loop < trips.length; loop++) {
                t[0] = trips[loop][0];
                t[1] = trips[loop][1];
                t[2] = trips[loop][2];

                if ((t[0] != dropLeaf) && (t[1] != dropLeaf) && (t[2] != dropLeaf)) continue;

                //! So the triplet contains the dropped leaf

                if ((t[0] == dropLeaf) || (t[1] == dropLeaf)) continue;    //! can't be consistent, forget it

                //! So t[2] == dropLeaf

                if (myarray[t[0] - 1] || (t[0] == reticLeaf))
                    if (myarray[t[1] - 1] || (t[1] == reticLeaf)) {
                        weightsubset += weight[loop];
                    }

            }

            if (weightsubset > bestSoFar) {
                bestSoFar = weightsubset;
                bestLeafSoFar = dropLeaf;
            }

            //! put the leaf back in, netjes

            myarray[dropLeaf - 1] = true;
        }


		/*
		System.out.println("Built best caterpillar for:");
		for(int x=0; x<bitpattern.length; x++ )
			{
			if( bitpattern[x] ) System.out.print((x+1)+" ");
			else
			System.out.print("  ");
			}
		System.out.println();
		*/

        lookup[num] = bestSoFar;
        firstleaf[num] = bestLeafSoFar;


        //! System.out.println("First leaf should be "+bestLeafSoFar);
        //! System.out.println("This will get "+bestSoFar+" triplets.");

        //! done!
    }


    public static int patternToInt(boolean array[]) {
        int total = 0;
        int base = 1;

        for (int scan = 0; scan < array.length; scan++) {
            if (array[scan]) total = total + base;

            base = base * 2;
        }

        return total;
    }

    public static void intToPattern(int num, boolean array[]) {
        int at = 0;
        int val = num;

        //! clean the array
        for (int c = 0; c < array.length; c++) array[c] = false;

        while (val != 0) {
            if ((val % 2) == 1) array[at] = true;

            at++;
            val = val / 2;
        }

    }

    public static void printHelp() {
        System.out.println();
        System.out.println(Heuristic.VERSION);
        System.out.println("----------------------------------------------");
        System.out.println("Usage: java Heuristic <tripletsfile> [options]");
        System.out.println("----------------------------------------------");
        System.out.println("[options] can be:");
        System.out.println();

        System.out.println("--forcemaxsn : this forces the heuristic to -always- use the maximal SN-sets as the blocks of the partition. Only valid when the input is dense. Default is " + Heuristic.USEMAXSN_DEFAULT + ".");
        // System.out.println();

        System.out.println("--noperfection : switches off the local perfection test. Default is false.");
        // System.out.println();

        System.out.println("--blocks <num>: this forces the heuristic to always partition into (at most) <num> blocks. Default is " + Heuristic.BLOCKS_DEFAULT + ".");
        // System.out.println();

        System.out.println("--missing: this heuristic will display all triplets in the input that are not consistent with the output network. Default is " + Heuristic.MISSING_DEFAULT + ".");
        // System.out.println();

        System.out.println("--surplus: this heuristic will display all triplets in the output network that are not consistent with the input. Default is " + Heuristic.SURPLUS_DEFAULT + ".");
        // System.out.println();

        System.out.println("--sdweight <num>: when computing the symmetric difference, assume that surplus triplets have weight <num> where <num> is a strictly positive integer. Default is " + Heuristic.SDWEIGHT_DEFAULT + ".");
        // System.out.println();

        System.out.println("--sdradical: perform a radical SD-minimizing post-processing. Default is false. Use in conjection with --sdweight. And with caution!");
        // System.out.println();

        System.out.println("--wumax <num>: Wu's algorithm will not be used when there are more than <num> leaves. Default is " + Heuristic.WU_CEILING_DEFAULT + ".");
        // System.out.println();

        System.out.println("--simmax <num>: the exact simple level-1 algorithm will be used for up to and including <num> leaves, the greedy algorithm after that. Default is " + Heuristic.SIM_MAX_DEFAULT + ".");
        // System.out.println();

        System.out.println("--besttree: builds the optimum tree for the input, and quits. Is exponentially slow! Default is " + Heuristic.TEST_TREE_DEFAULT + ".");
        // System.out.println();

        System.out.println("--bestsimple: builds the optimum simple level-1 network for the input, and quits. Is exponentially slow! Default is " + Heuristic.TEST_EXACT_DEFAULT + ".");
        // System.out.println();

        System.out.println("--bestgreedy: builds the greedy simple level-1 network for the input, and quits. Default is " + Heuristic.TEST_GREEDY_DEFAULT + ".");
        // System.out.println();

        System.out.println("--nopostprocess: do not collapse redundant edges. Default is " + Heuristic.NOPOSTPROCESS_DEFAULT);
        //! System.out.println();



		/*
		System.out.println("--coeffs <num1> <num2> <num3>: sets the coefficients for the partitioning strategy. num1 is internal triplets, num2 is external triplets and num3 are definitely satisfied triplets. Defaults are "+INTERNAL_COEFF_DEFAULT+", "+EXTERNAL_COEFF_DEFAULT+", "+HAPPY_COEFF_DEFAULT+" respectively. WARNING changing these values can destablise the program.");
		System.out.println();
		*/

		/*
		System.out.println("--enewick: outputs the resulting network in eNewick format, not in DOT. Default is "+Heuristic.ENEWICK_DEFAULT+".");
		// System.out.println();
		*/

        System.out.println("--nonetwork: do not output the network i.e. only output the statistics of it. Default is " + Heuristic.NONETWORK_DEFAULT + ".");
        //! System.out.println();

        System.out.println("--dumpnettrips: prints every triplet (irrespective of whether it is in input) that is consistent with the final network to the file 'network.trips'. Default is " + Heuristic.DUMPTRIPLETS_DEFAULT + ".");

        System.out.println("--dumpconsistent: outputs to the file 'consistent.trips' the subset of input triplets (including weights) that are consistent with the final network. Default is " + Heuristic.DUMPCONSISTENT_DEFAULT + ".");

        System.out.println("--fortyeight: forces the program to produce (via derandomization) the best caterpillar network; this will be consistent with at least 48% of the input triplets, but will be biologically not so meaningful. Switches all other options off!");
        //! System.out.println();

        System.out.println("--say <text>: 'text', which should be a single word, will be displayed in the information box of the final .dot network produced by the heuristic. There can be multiple --say parameters on the command line.");

        System.out.println("--help: displays this message.");
        System.out.println();
    }


    public static void main(String[] args) {
        Heuristic.DENSE = Heuristic.DENSE_DEFAULT;
        Heuristic.USEMAXSN = Heuristic.USEMAXSN_DEFAULT;
        Heuristic.BLOCKS = Heuristic.BLOCKS_DEFAULT;
        Heuristic.MISSING = Heuristic.MISSING_DEFAULT;
        Heuristic.SURPLUS = Heuristic.SURPLUS_DEFAULT;
        Heuristic.WU_CEILING = Heuristic.WU_CEILING_DEFAULT;
        Heuristic.SIM_MAX = Heuristic.SIM_MAX_DEFAULT;
        Heuristic.TEST_TREE = Heuristic.TEST_TREE_DEFAULT;
        Heuristic.TEST_EXACT = Heuristic.TEST_EXACT_DEFAULT;
        Heuristic.TEST_GREEDY = Heuristic.TEST_GREEDY_DEFAULT;
        Heuristic.ENEWICK = Heuristic.ENEWICK_DEFAULT;
        Heuristic.SDWEIGHT = Heuristic.SDWEIGHT_DEFAULT;
        Heuristic.NOPOSTPROCESS = Heuristic.NOPOSTPROCESS_DEFAULT;
        Heuristic.NONETWORK = Heuristic.NONETWORK_DEFAULT;
        Heuristic.CORRUPT = Heuristic.CORRUPT_DEFAULT;
        Heuristic.SAMPLE = Heuristic.SAMPLE_DEFAULT;
        Heuristic.DUMPTRIPLETS = Heuristic.DUMPTRIPLETS_DEFAULT;
        Heuristic.DUMPCONSISTENT = Heuristic.DUMPCONSISTENT_DEFAULT;

        int weightSpy[] = new int[1000];

        String graphString = "";

        //! Need to read the triplets in from somewhere...
        TripletSet t = new TripletSet();

        TripletSet pure = null;

        if (args.length == 0) {
            Heuristic.printHelp();
            System.out.println("Terminating: No input file specified.");
            System.exit(0);
        } else {
            if (args[0].equals("--help")) {
                Heuristic.printHelp();
                System.exit(0);
            }

            //! ---------- THIS IS A HACK! NEEDS TO BE HERE BECAUSE WE WILL CORRUPT
            //! ---------- THE TRIPLETS AS THEY COME IN...

            corruptscan:
            for (int x = 0; x < args.length; x++) {
                if (args[x].equals("--corrupt")) {
                    Heuristic.CORRUPT = true;
                    Heuristic.CORRUPT_PROB = (float) Float.parseFloat(args[x + 1]);
                    System.out.println("// ADMIN-SETTING: Corrupting triplets with probability " + Heuristic.CORRUPT_PROB);
                    System.out.println("// ADMIN-SETTING: Extra statistics will be included in the CORRUPT field.");
                    pure = new TripletSet();
                    break corruptscan;
                }
            }

            samplescan:
            for (int x = 0; x < args.length; x++) {
                if (args[x].equals("--sample")) {
                    Heuristic.SAMPLE = true;
                    Heuristic.SAMPLE_PROB = (float) Float.parseFloat(args[x + 1]);
                    System.out.println("// ADMIN-SETTING: Sampling triplets with probability " + Heuristic.SAMPLE_PROB);
                    pure = new TripletSet();
                    break samplescan;
                }

            }

            if (Heuristic.SAMPLE && Heuristic.CORRUPT) {
                System.out.println("// ERROR: Can only be in at most one of SAMPLE and CORRUPT mode.");
                System.exit(0);
            }

            if (Heuristic.SAMPLE || Heuristic.CORRUPT) {
                System.out.println("// ADMIN-SETTING: Note that it is ASSUMED that the input triplets are a complete network...");
                System.out.println("// ADMIN-SETTING: See the field that begins STAT: NETWORK-NETWORK SD for network symmetric difference.");
            }

            //! ------------------------------------------------------------

            String fileName = args[0];
            int recCount = 0;

            graphString = graphString + "File: " + fileName;

            Hashtable leafTypes = new Hashtable();

            try {
                System.out.println("// " + Heuristic.VERSION);

                System.out.println("// COMMENT: Pre-processing the input file to count the leaves");

                FileReader fr = new FileReader(fileName);
                BufferedReader br = new BufferedReader(fr);

                String record = new String();
                while ((record = br.readLine()) != null) {
                    if (record.startsWith("//")) continue; //! ignore comments

                    String[] tripData = record.split(" ");

                    if ((tripData.length < 3) || (tripData.length > 4)) {
                        System.out.println("Read line: " + record);
                        System.out.println("Malformed line: each line should consist of three or four arguments separated by spaces, where the");
                        System.out.println("first three are names of leave, and the optional fourth (the weight)");
                        System.out.println("should be greater than or equal to zero.");

                        throw new IOException();
                    }

                    leafTypes.put(tripData[0], tripData[0]);
                    leafTypes.put(tripData[1], tripData[1]);
                    leafTypes.put(tripData[2], tripData[2]);
                }
                System.out.println("// COMMENT: Pre-processing showed that there are " + leafTypes.size() + " leaves in the input.");

                TripletSet.DEFAULT_WEIGHT_MATRIX = leafTypes.size();
            } catch (IOException e) {
                // catch possible io errors from readLine()
                System.out.println("Problem reading file " + fileName);
                System.exit(0);
            }


            //! -------------------------------------------------------------

            try {
                FileReader fr = new FileReader(fileName);
                BufferedReader br = new BufferedReader(fr);

                String record = new String();
                readloop:
                while ((record = br.readLine()) != null) {
                    if (record.startsWith("//")) continue; //! ignore comments

                    recCount++;
                    String[] tripData = record.split(" ");

                    if ((tripData.length < 3) || (tripData.length > 4)) {
                        System.out.println("Read line: " + record);
                        System.out.println("Malformed line: each line should consist of three or four arguments separated by spaces, where the");
                        System.out.println("first three are names of leave, and the optional fourth (the weight)");
                        System.out.println("should be greater than or equal to zero.");

                        throw new IOException();
                    }

                    //! int a = Integer.parseInt(tripData[0]);
                    //! int b = Integer.parseInt(tripData[1]);
                    //! int c = Integer.parseInt(tripData[2]);

                    int a = getLeafNumber(tripData[0]);
                    int b = getLeafNumber(tripData[1]);
                    int c = getLeafNumber(tripData[2]);

                    int w = 1;    //! default weight

                    if (tripData.length == 4) {
                        w = Integer.parseInt(tripData[3]);
                        if (w >= 10000) Heuristic.BIGWEIGHTS = true;
                    }

                    if ((a == b) || (b == c) || (a == c)) {
                        System.out.println("Read line: " + record);
                        System.out.println("Each line should consist of three DISTINCT integers.");
                        throw new IOException();
                    }

                    if ((a <= 0) || (b <= 0) || (c <= 0)) {
                        System.out.println("Read line: " + record);
                        System.out.println("All leaves should be 1 or greater.");
                        throw new IOException();
                    }

                    if (w == 0) {
                        System.out.println("// Ignoring the zero-weight triplet " + a + " " + b + " | " + c);
                        continue;
                    }

                    if (w < 0) {
                        System.out.println("Read line: " + record);
                        System.out.println("Negative weight triplets are not allowed.");
                        throw new IOException();
                    }

                    if (Heuristic.SAMPLE) {
                        if (pure.containsTriplet(a, b, c)) {
                            System.out.println("// Duplicate input triplets in the input (whilst in administrative SAMPLE state) -- triplet " + a + " " + b + " " + c + " -- stopping");
                            System.exit(0);
                        }
                        pure.addTriplet(a, b, c, w);

                        if (Math.random() > Heuristic.SAMPLE_PROB) {
                            continue readloop;
                        }
                    }

                    if (Heuristic.CORRUPT) {
                        if (pure.containsTriplet(a, b, c)) {
                            System.out.println("// Duplicate input triplets in the input (whilst in administrative CORRUPT state) -- triplet " + a + " " + b + " " + c + " -- stopping");
                            System.exit(0);
                        }
                        pure.addTriplet(a, b, c, w);

                        if (Math.random() <= Heuristic.CORRUPT_PROB) {
                            int tempA = a;
                            int tempB = b;
                            int tempC = c;

                            if (Math.random() <= 0.5) {
                                //! b c a
                                a = tempB;
                                b = tempC;
                                c = tempA;
                            } else {
                                //! a c b
                                b = tempC;
                                c = tempB;
                            }
                        }


                    }


                    if (t.containsTriplet(a, b, c)) {
                        //! System.out.println("// Triplet "+a+" "+b+" | "+c+" has already been seen at least once, ignoring this line.");
                        if (Heuristic.CORRUPT == false) {
                            System.out.println("// Terminating because in normal usage duplicate triplets in input not allowed.");
                            System.exit(0);
                        }
                        continue;
                    }

                    weightSpy[a] += w;
                    weightSpy[b] += w;
                    weightSpy[c] += w;

                    t.addTriplet(a, b, c, w);
                }


            } catch (IOException e) {
                // catch possible io errors from readLine()
                System.out.println("Problem reading file " + fileName);
                System.exit(0);
            }
        }

        Heuristic.report("Finished reading input file.");

		/*
		System.out.println("// The leaves found in the input file will have the following internal mapping: ");
		for( int i=1; i <= seenLeaves; i++ )
			{
			System.out.println( "// "+getLeafName(i) + " is mapped to "+i );
			}
		*/

        System.out.println("// SUMMARY: Input had " + seenLeaves + " leaves.");

        //! t.dumpTriplets();

        int errorTrip[] = new int[3];

        if (args.length >= 2) {
            int at = 1;

            while (at < args.length) {
                if (args[at].equals("--forcemaxsn")) {
                    Heuristic.USEMAXSN = true;
                    at++;
                    System.out.println("// USER-SETTING: using max sn-sets as partition blocks.");
                    continue;
                }

                if (args[at].equals("--noperfection")) {
                    Heuristic.NOPERFECTION = true;
                    at++;
                    System.out.println("// USER-SETTING: Will not test for local perfection.");
                    continue;
                }

                if (args[at].equals("--blocks")) {
                    if ((at + 1) >= args.length) {
                        System.out.println("ERROR: Usage --blocks <number of blocks>");
                        System.exit(0);
                    }

                    try {
                        int blox = Integer.parseInt(args[at + 1]);
                        Heuristic.BLOCKS = blox;
                        System.out.println("// USER-SETTING: partitions have at most " + blox + " blocks.");
                    } catch (NumberFormatException e) {
                        System.out.println("ERROR: Usage --blocks <number of blocks>");
                        System.exit(0);
                    }
                    at += 2;
                    continue;
                }

                if (args[at].equals("--missing")) {
                    Heuristic.MISSING = true;
                    at++;
                    System.out.println("// USER-SETTING: Will list triplets that are missing.");
                    continue;
                }

                if (args[at].equals("--surplus")) {
                    Heuristic.SURPLUS = true;
                    at++;
                    System.out.println("// USER-SETTING: Will list surplus triplets.");
                    continue;
                }

                if (args[at].equals("--corrupt")) {
                    at += 2;
                    continue;
                }

                if (args[at].equals("--sample")) {
                    at += 2;
                    continue;
                }

                if (args[at].equals("--say")) {
                    graphString += "\\n" + "Info: " + args[at + 1];
                    System.out.println("// USER-SETTING: Printing the text '" + args[at + 1] + " in the information box of the final network.");
                    at += 2;
                    continue;
                }

                if (args[at].equals("--wumax")) {
                    try {
                        if (at + 1 >= args.length) throw new NumberFormatException();

                        Heuristic.WU_CEILING = Integer.parseInt(args[at + 1]);
                        if (Heuristic.WU_CEILING > 30) {
                            System.out.println("ERROR: This implementation of Wu's tree-building algorithm accepts at most 30 leaves.");
                            System.exit(0);
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("ERROR: please specify a non-negative integer as argument to --wumax.");
                        System.exit(0);
                    }

                    if (Heuristic.WU_CEILING < 0) {
                        System.out.println("ERROR: please specify a non-negative integer as argument to --wumax.");
                        System.exit(0);
                    }

                    at += 2;
                    System.out.println("// USER-SETTING: Will not use Wu's algorithm when there are more than " + WU_CEILING + " leaves.");
                    continue;
                }

                if (args[at].equals("--simmax")) {
                    try {
                        if (at + 1 >= args.length) throw new NumberFormatException();
                        Heuristic.SIM_MAX = Integer.parseInt(args[at + 1]);
                    } catch (NumberFormatException e) {
                        System.out.println("ERROR: please specify a non-negative integer (max. 30) as argument to --simmax.");
                        System.exit(0);
                    }

                    if ((Heuristic.SIM_MAX < 0) || (Heuristic.SIM_MAX > 30)) {
                        System.out.println("ERROR: please specify a non-negative integer (max. 30) as argument to --simmax. I read " + Heuristic.SIM_MAX + ".");
                        System.exit(0);
                    }

                    at += 2;
                    System.out.println("// USER-SETTING: Will use greedy algorithm when there are " + SIM_MAX + " or more leaves, exact algorithm before that.");
                    continue;
                }

                if (args[at].equals("--besttree")) {
                    Heuristic.TEST_TREE = true;
                    at += 1;
                    System.out.println("// USER-SETTING: Will try and compute the optimal tree for the input, nothing more.");
                    continue;
                }

                if (args[at].equals("--bestsimple")) {
                    Heuristic.TEST_EXACT = true;
                    at += 1;
                    System.out.println("// USER-SETTING: Will try and compute the optimal simple level-1 network for the input, nothing more.");
                    continue;
                }

                if (args[at].equals("--bestgreedy")) {
                    Heuristic.TEST_GREEDY = true;
                    at += 1;
                    System.out.println("// USER-SETTING: Will try and compute the greedy simple level-1 network for the input, nothing more.");
                    continue;
                }

                if (args[at].equals("--sdradical")) {
                    Heuristic.SDRADICAL = true;
                    if (Heuristic.NOPOSTPROCESS) {
                        System.out.println("ERROR: It makes no sense to use --sdradical and --nopostprocess together.");
                        System.exit(0);
                    }
                    at += 1;
                    System.out.println("// USER-SETTING: Will try a radical symmetric-difference reducing post-processing.");
                    continue;
                }


                if (args[at].equals("--sdweight")) {
                    try {
                        if (at + 1 >= args.length) throw new NumberFormatException();
                        Heuristic.SDWEIGHT = Integer.parseInt(args[at + 1]);
                        if (SDWEIGHT <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException nfe) {
                        System.out.println("ERROR: --sdweight must be followed by a strictly positive integer. Instead I read " + args[at + 1]);
                        System.exit(0);
                    }

                    System.out.println("// USER-SETTING: Will use " + SDWEIGHT + " as assumed weight of triplets not in the input, when computing symmetric difference.");
                    at += 2;
                    continue;
                }


                if (args[at].equals("--coeffs")) {
                    try {
                        Heuristic.INTERNAL_COEFF = Integer.parseInt(args[at + 1]);
                        Heuristic.EXTERNAL_COEFF = Integer.parseInt(args[at + 2]);
                        Heuristic.HAPPY_COEFF = Integer.parseInt(args[at + 3]);
                    } catch (NumberFormatException e) {
                        System.out.println("ERROR: please specify integers as the coefficients --coeffs.");
                        System.exit(0);
                    }

                    at += 4;
                    System.out.println("// USER-SETTING: Partitioning with coefficients " + INTERNAL_COEFF + ", " + EXTERNAL_COEFF + ", " + HAPPY_COEFF + ".");
                    System.out.println("// WARNING: Altering the partitioning coefficients without care can seriously destablise the program!");
                    continue;
                }

                if (args[at].equals("--nopostprocess")) {
                    Heuristic.NOPOSTPROCESS = true;
                    if (Heuristic.SDRADICAL) {
                        System.out.println("ERROR: It makes no sense to use --sdradical and --nopostprocess together.");
                        System.exit(0);
                    }

                    System.out.println("// USER-SETTING: Will *not* post-process output to collapse edges.");
                    at += 1;
                    continue;
                }

                if (args[at].equals("--fortyeight")) {
                    Heuristic.FORTYEIGHT = true;
                    System.out.println("// USER-SETTING: Will compute the best caterpillar network for this input set, and terminate.");
                    at += 1;
                    continue;
                }

                if (args[at].equals("--dumpnettrips")) {
                    Heuristic.DUMPTRIPLETS = true;
                    System.out.println("// USER-SETTING: Will output every triplet that is consistent with the final network, to the file 'consistent.trips'.");
                    at += 1;
                    continue;
                }

                if (args[at].equals("--dumpconsistent")) {
                    Heuristic.DUMPCONSISTENT = true;
                    System.out.println("// USER-SETTING: Will output all input triplets that are consistent with the final network, to the file 'consistent.trips'.");
                    at += 1;
                    continue;
                }


                if (args[at].equals("--help")) {
                    Heuristic.printHelp();
                    at++;
                    System.exit(0);
                }

				/*
				if( args[at].equals("--enewick") )
					{
					Heuristic.ENEWICK = true;
					at++;
					System.out.println("// USER-SETTING: Will output network in eNewick format.");
					continue;
					}
				*/

                if (args[at].equals("--nonetwork")) {
                    Heuristic.NONETWORK = true;
                    at++;
                    System.out.println("// USER-SETTING: Will not output a network, only the stats of the network.");
                    continue;
                }

                if (args[at].equals("--expand")) {
                    Heuristic.EXPAND = true;
                    at++;
                    System.out.println("// ADMIN-SETTING: Will promote block expansion by saturation.");
                    continue;
                }


                System.out.println("Unrecognised argument '" + args[at] + "', stopping.");
                System.exit(0);
            }

        }

        boolean wasNotDense = false;

        graphString = graphString + "\\n" + "Number of triplets: " + t.countTriplets();

        if (t.isDense(errorTrip) == true) {
            System.out.println("// COMMENT: Triplet set is already dense.");
            Heuristic.DENSE = true;
        } else {
            System.out.println("// COMMENT: Not a dense input set.");

            wasNotDense = true;

            if (Heuristic.USEMAXSN) {
                System.out.println("ERROR: Cannot forcibly partition using maximum SN-sets because input is not dense.");
                System.exit(0);
            }

            System.out.println("// COMMENT: Currently " + t.countTriplets() + " triplets in the input, padding it with 0-weight triplets to make a dense set.");
            t.makeDense();
        }


        Heuristic.report("Attempting to build a good level-1 network.");

        int totWeight = t.getTotalWeight();
        int totTrips = t.countTriplets();

        double n = (totWeight * 1.0);

        System.out.println("// SUMMARY: There are " + totTrips + " triplets in the input.");
        if (wasNotDense) System.out.println("// SUMMARY: (Some of these will be 0-weight triplets that I added).");
        System.out.println("// SUMMARY: Total weight of input triplets is " + totWeight + ".");

        graphString = graphString + "\\n" + "Weight of triplets: " + totWeight;

        if (FORTYEIGHT) {
            double cool = FortyEight.derandomize(t);

            System.out.println();

            System.out.println("Derandomization got " + cool + " units of weight, that's " + (cool / (double) t.getTotalWeight()) + " fraction of total possible");

            System.exit(0);
        }


        biDAG b = null;

        if (TEST_TREE || TEST_EXACT || TEST_GREEDY) {
            int mytrips[][] = t.getArray();
            int myweight[] = new int[mytrips.length];
            for (int x = 0; x < mytrips.length; x++) {
                int w = t.getWeight(mytrips[x][0], mytrips[x][1], mytrips[x][2]);
                if (w < 0) {
                    System.out.println("We fucked up big time.");
                    System.exit(0);
                }
                myweight[x] = w;
            }

            if (TEST_TREE) {
                System.out.println("// COMMENT: Starting Wu's algorithm now.");
                if (t.tellLeaves() > 30) {
                    System.out.println("// ERROR: Wu's algorithm can only accept up to 30 leaves. You tried with " + t.tellLeaves() + ". Terminating.");
                    System.exit(0);
                }

                WuAnswer wa = WuWeighted.wu(mytrips, myweight);
                b = wa.root;
                b.tripsWithin = wa.tripsGot;
            } else if (TEST_GREEDY) {
                System.out.println("// COMMENT: Starting greedy simple level-1 algorithm now.");
                b = buildGreedySimpleLevel1(mytrips, myweight);
                //! tripsWithin is already updated here
            } else if (TEST_EXACT) {
                System.out.println("// COMMENT: Starting exact simple level-1 algorithm now.");
                if (t.tellLeaves() > 30) {
                    System.out.println("// ERROR: Exact simple level-1 algorithm can only accept up to 30 leaves. You tried with " + t.tellLeaves() + ". Terminating.");
                    System.exit(0);
                }

                Sim1 bestNet = buildBestSimpleLevel1(mytrips, myweight);
                b = TripletSet.convertSim1(bestNet);
                b.tripsWithin = bestNet.best;
            }

        } else {
            b = t.buildBlitz(0, Heuristic.USEMAXSN);
        }


        double d = b.tripsWithin * 1.0;

        double percentage = ((int) ((d / n) * 10000)) / 100.0;

        System.out.println("// SUMMARY: (Before post-processing)");

        System.out.println("// SUMMARY: We got " + b.tripsWithin + " units of triplet weight, that's " + percentage + "% of total weight.");

        dagExplore de = new dagExplore(b);

        boolean rememberMissing = Heuristic.MISSING;
        boolean rememberSurplus = Heuristic.SURPLUS;

        Heuristic.MISSING = false;
        Heuristic.SURPLUS = false;

        //! if( Heuristic.MISSING ) System.out.println("// Outputing triplets from the input that were -not- in the output network.");

        int badWeight = t.dumpMissing(de);

        System.out.println("// SUMMARY: In total " + badWeight + " units of triplet weight were -not- consistent with the ouput network.");

        if (b.tripsWithin + badWeight != totWeight) {
            System.out.println("ERROR: Triplets in and out do not sum to input weight!");
            System.exit(0);
        }

        //! if( Heuristic.SURPLUS ) 	System.out.println("// Outputing triplets from the output network that were -not- in the input.");

        int plusWeight = t.dumpSurplus(de);

        System.out.println("// SUMMARY: Output network contained " + plusWeight + " triplets -not- in the original input.");

        if (Heuristic.CORRUPT) {
            t.getUncorrupted(de, pure);
        }

        if (Heuristic.CORRUPT || Heuristic.SAMPLE) {
            int netsd = pure.computeSD(de);

            System.out.println("// STAT: NETWORK-NETWORK SD = " + netsd);
        }

        System.out.println("// SUMMARY: The symmetric difference is thus " + badWeight + " + (" + SDWEIGHT + " * " + plusWeight + ") = " + (badWeight + (SDWEIGHT * plusWeight)));

        int oldsd = (badWeight + (SDWEIGHT * plusWeight));

        int numTrips = b.tripsWithin;

        if (true) {
            //! This is all very controversial

            dagExplore convert = new dagExplore(b);

            genDAG g = convert.buildgenDAG();

            boolean done = false;

            genExplore ge = null;

            if (Heuristic.NOPOSTPROCESS == false)
                contract:while (!done) {
                    ge = new genExplore(g);

                    Vector edges = ge.getEdges();

                    for (int scan = 0; scan < edges.size(); scan++) {
                        //! System.out.println("// COMMENT: Looking at edge "+scan);

                        genDAG e[] = (genDAG[]) edges.elementAt(scan);

                        int tailNum = e[0].nodeNum;
                        int headNum = e[1].nodeNum;

                        genDAG newGuy[] = ge.clonegenDAG();
                        genDAG newRoot = newGuy[0];

                        boolean success = genDAG.collapseEdge(newGuy[tailNum], newGuy[headNum]);

                        if (!success) continue;

                        genExplore geNew = new genExplore(newRoot);

                        int diff[] = t.computeBrokenGained(ge, geNew);

                        //! System.out.println("Contracting that edge means: "+diff[0]+", "+diff[1]);

                        if (diff[0] == 0) {
                            g = newRoot;
                            continue contract;
                        }
                    }
                    done = true;
                }

            g.resetVisited();

            //! -------------------------------------------------------------------------
            //! Here comes the SD minimisation

            if (Heuristic.SDRADICAL == true) {
                boolean anythingContracted = true;

                while (anythingContracted) {

                    System.out.println("// COMMENT: (Re-)entering SD-minimizing phase");

                    boolean deldone = false;

                    delloop:
                    while (deldone == false) {
                        ge = new genExplore(g);

                        int stat[] = new int[seenLeaves + 1];
                        int missing = t.dumpMissing(ge, stat);
                        int surplus = t.dumpSurplus(ge);
                        int currentsd = missing + (Heuristic.SDWEIGHT * surplus);

                        Vector deledges = ge.getEdges();

                        for (int scan = 0; scan < deledges.size(); scan++) {
                            //! So, clone the genDAG
                            genDAG newGuy[] = ge.clonegenDAG();
                            genDAG newRoot = newGuy[0];

                            genExplore after = new genExplore(newRoot);

                            genDAG e[] = (genDAG[]) deledges.elementAt(scan);

                            int tailNum = e[0].nodeNum;
                            int headNum = e[1].nodeNum;

                            //! We are only going to delete arcs that have a recomb as head
                            if (e[1].parents.size() <= 1) continue;

                            after.killEdge(newGuy[tailNum], newGuy[headNum]);

                            genExplore geNew = new genExplore(newRoot);

                            stat = new int[seenLeaves + 1];
                            int newMissing = t.dumpMissing(geNew, stat);
                            int newSurplus = t.dumpSurplus(geNew);
                            int newsd = newMissing + (Heuristic.SDWEIGHT * newSurplus);

                            if (newsd <= currentsd) {
                                System.out.println("// COMMENT: Deleting recombination edge because it lowers SD score");
                                g = newRoot;
                                continue delloop;
                            }
                        }                //! end for

                        //! If we got this far it means we couldn't find anything to delete, so finish
                        deldone = true;
                    }                    //! end while

                    //! So now we're going to contract while that reduces the SD

                    anythingContracted = false;

                    boolean condone = false;

                    conloop:
                    while (condone == false) {
                        ge = new genExplore(g);

                        int stat[] = new int[seenLeaves + 1];
                        int missing = t.dumpMissing(ge, stat);
                        int surplus = t.dumpSurplus(ge);
                        int currentsd = missing + (Heuristic.SDWEIGHT * surplus);

                        Vector deledges = ge.getEdges();

                        for (int scan = 0; scan < deledges.size(); scan++) {
                            //! So, clone the genDAG
                            genDAG newGuy[] = ge.clonegenDAG();
                            genDAG newRoot = newGuy[0];

                            genExplore after = new genExplore(newRoot);

                            genDAG e[] = (genDAG[]) deledges.elementAt(scan);

                            int tailNum = e[0].nodeNum;
                            int headNum = e[1].nodeNum;

                            //! System.out.println("// SD before: "+currentsd);

                            boolean success = genDAG.collapseEdge(newGuy[tailNum], newGuy[headNum]);

                            if (!success) continue;

                            genExplore geNew = new genExplore(newRoot);

                            stat = new int[seenLeaves + 1];
                            int newMissing = t.dumpMissing(geNew, stat);
                            int newSurplus = t.dumpSurplus(geNew);
                            int newsd = newMissing + (Heuristic.SDWEIGHT * newSurplus);

                            //! System.out.println("// SD after: "+newsd);

                            if (newsd <= currentsd) {
                                //! System.out.println("// COMMENT: Contracting edge because it lowers SD score");
                                g = newRoot;
                                anythingContracted = true;
                                continue conloop;
                            }
                        }                               //! end for

                        //! If we got this far it means we couldn't find anything to delete, so finish
                        condone = true;
                    }

                    g.resetVisited();

                } //! end while anythingCountracted

            }                        //! end SDRADICAL

            //! ---------------------------------------------------------------------------------------------
            //! At this point network modification is finished, don't make any more changes!!!!!
            //! ---------------------------------------------------------------------------------------------

            int delConsequence[][] = null;

            ge = new genExplore(g);


            if (Heuristic.NOPOSTPROCESS == false) {
                done = false;

                Vector deledges = ge.getEdges();

                delConsequence = new int[deledges.size()][3];
                for (int x = 0; x < delConsequence.length; x++) {
                    delConsequence[x][0] = -1;
                    delConsequence[x][1] = -1;
                    delConsequence[x][2] = -1;
                }
                int delAt = 0;

                for (int scan = 0; scan < deledges.size(); scan++) {
                    //! So, clone the genDAG
                    genDAG newGuy[] = ge.clonegenDAG();
                    genDAG newRoot = newGuy[0];

                    genExplore after = new genExplore(newRoot);

                    genDAG e[] = (genDAG[]) deledges.elementAt(scan);

                    int tailNum = e[0].nodeNum;
                    int headNum = e[1].nodeNum;

                    after.killEdge(newGuy[tailNum], newGuy[headNum]);

                    int diff[] = t.computeBrokenGained(ge, after);

                    //! System.out.println("// Deleting edge "+tailNum+" -> "+headNum+" means "+diff[0]+" broken triplets and "+diff[1]+" gained triplets.");

                    delConsequence[delAt][0] = tailNum;
                    delConsequence[delAt][1] = headNum;
                    delConsequence[delAt][2] = diff[0];
                    delAt++;
                }
            } //! end NOPOSTPROCESS (part 2)

            //! -------------------------------------------------------------

            g.resetVisited();

            Heuristic.MISSING = rememberMissing;
            Heuristic.SURPLUS = rememberSurplus;

            System.out.println("// SUMMARY: (After post-processing)");

            if (Heuristic.MISSING)
                System.out.println("// SUMMARY: Listing input triplets not present in the final network");

            int gotWeight[] = new int[seenLeaves + 1];

            //! System.out.println("// SUMMARY: Taxon-specific breakdown: ");

            double verPercent[] = new double[seenLeaves + 1];

            int nuBadWeight = t.dumpMissing(ge, gotWeight);


            for (int x = 1; x <= seenLeaves; x++) {
                double tpercent = (gotWeight[x] * 1.0) / (weightSpy[x] * 1.0);
                double pctround = ((int) (tpercent * 10000)) / 100.0;
                verPercent[x] = pctround;
                //! System.out.println("// "+Heuristic.getLeafName(x)+" was involved "+weightSpy[x]+" units of triplet weight, we got "+gotWeight[x]+" units ("+ pctround+"%)");
            }


            double testpercentage = ((int) (((n - nuBadWeight) / n) * 10000)) / 100.0;

            if (Heuristic.NONETWORK == false) {

                graphString = graphString + "\\n" + "Overall %: " + testpercentage + "%";
                g.dumpDAG(verPercent, delConsequence, graphString);

                System.out.print("// SUMMARY: eNewick output: ");

                g.newickDump();
            }

            System.out.println("// SUMMARY: In total " + nuBadWeight + " units of triplet weight were -not- consistent with the ouput network.");

            if (Heuristic.SURPLUS)
                System.out.println("// SUMMARY: Listing triplets in the final network not in the input");

            int nuPlusWeight = t.dumpSurplus(ge);

            System.out.println("// SUMMARY: Output network contained " + nuPlusWeight + " triplets -not- in the original input.");

            if (Heuristic.DUMPTRIPLETS == true) {
                System.out.println("// COMMENT: Dumping the triplets from the final network to the file 'network.trips'");
                ge.dumpEverything();
            }

            if (Heuristic.DUMPCONSISTENT == true) {
                System.out.println("// COMMENT: Dumping all input triplets consistent with the output network, to the file 'consistent.trips'.");
                t.dumpConsistent(ge);
            }

            System.out.println("// SUMMARY: Weight of missing triplets before contraction minus weight of missing triplets afterwards: " + (badWeight - nuBadWeight));

            if ((badWeight - nuBadWeight != 0) && (Heuristic.SDRADICAL == false)) {
                System.out.println("// ERROR! Collapsing an edge led to triplet destruction: that shouldn't have happened!");
                System.exit(0);
            }

            System.out.println("// SUMMARY: Number of surplus triplets before contraction minus number of surplus triplets afterwards: " + (plusWeight - nuPlusWeight));

            int newTriplets = t.computeConsistency(ge);

            if ((n - nuBadWeight) != newTriplets) {
                System.out.println("ERROR: After SD minimisation the triplet weights didn't add up!");
                System.exit(0);
            }

            double newpercentage = ((int) ((newTriplets / n) * 10000)) / 100.0;

            if (newpercentage != testpercentage) {
                System.out.println("// ERROR: End percentages did not mask! Ask the programmer.");
                System.exit(0);
            }

            System.out.println("// CONCLUSION: After post-processing we got " + newTriplets + " units of triplet weight, that's " + newpercentage + "% of total weight (before post-processing this was " + percentage + "%).");
            System.out.println("// CONCLUSION: After post-processing the symmetric difference is thus " + nuBadWeight + " + (" + SDWEIGHT + " * " + nuPlusWeight + ") = " + (nuBadWeight + (SDWEIGHT * nuPlusWeight)) + " (before post-processing this was " + oldsd + ")");
            System.out.println("// STAT: PERCENTAGE = " + newpercentage);
            System.out.println("// STAT: NETWORK-TRIPLET SD = " + (nuBadWeight + (SDWEIGHT * nuPlusWeight)));


        }

    }
}


//! ---------------------------------------------------------------------------------------
//! ---------------------------------------------------------------------------------------
//! ---------------------------------------------------------------------------------------
//! ---------------------------------------------------------------------------------------
//! ---------------------------------------------------------------------------------------

class genDAG {
    public Vector parents;
    public Vector children;

    public int data;
    public int dec[];
    public int auxDat;
    public boolean visited;

    //! -------------------------
    public boolean newickRecVisit;
    public int newickRecNum;
    public boolean newickVisit;
//! -------------------------

    public int nodeNum;

    public genDAG() {
        parents = new Vector();
        children = new Vector();
        newickRecNum = -1;
    }

    public void resetVisited() {
        //! Heuristic.report("In genDAG resetVisited()");

        if (visited == false) return;

        visited = false;

        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            ((genDAG) e.nextElement()).resetVisited();
        }
    }

    public void resetAuxData() {
        if (auxDat == 0) return;

        auxDat = 0;

        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            ((genDAG) e.nextElement()).resetAuxData();
        }
    }

//! This prints the dag (rooted at this node) in the funky .DOT format
//! Still needs to be refined...NOTE THAT THIS HAS SIDE EFFECTS ON AUXDAT
//! AND VISITED SO ONLY CALL IF UNUSED OR "RESET"!!!!

    public void dumpDAG(double vertexPct[], int delConsequence[][], String gString) {
        //! Hmmm, a little bit tricky...

        int num[] = new int[1];

        //! This numbering is arbitrary, just to draw a distinction from leaves;
        num[0] = 1000;

        System.out.println("strict digraph G1 {");

        System.out.println("edge [fontsize=8]");
        //! System.out.println("node [fontsize=8]");
        System.out.println("labelbox [shape=box, width=0.4, label=\"" + gString + "\"];");

        //! First, we'll number ALL the nodes....
        this.numVisit(num, vertexPct, delConsequence);

        //! Ok, so there is a numbering

        this.printOutgoingArcs(delConsequence);
        System.out.println("}");
    }

    private void numVisit(int num[], double vertexPct[], int delConsequence[][]) {
        //! If already been visited, finished
        if (this.auxDat != 0) return;

        this.auxDat = num[0];

        if (this.data != 0) {
            this.auxDat = this.data;
            String pct = (vertexPct == null ? "" : "\\n(" + vertexPct[this.data] + "%)");
            System.out.println(this.auxDat + " [shape=box, width=0.3, label=\"" + Heuristic.getLeafName(this.auxDat) + pct + "\"];");
        } else {
            System.out.println(this.auxDat + " [shape=point];");
        }

        num[0]++;

        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            ((genDAG) e.nextElement()).numVisit(num, vertexPct, delConsequence);
        }
    }

    private void printOutgoingArcs(int delConsequence[][]) {
        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            genDAG c = ((genDAG) e.nextElement());
            System.out.print(this.auxDat + " -> " + c.auxDat);

            if (delConsequence != null) {

                System.out.print(" [label=\"");

                int head = this.nodeNum;
                int tail = c.nodeNum;

                for (int x = 0; x < delConsequence.length; x++) {
                    if ((delConsequence[x][0] == head) && (delConsequence[x][1] == tail)) {
                        int printMe = delConsequence[x][2];

                        if (Heuristic.BIGWEIGHTS) {
                            double conv = (double) printMe;
                            conv = conv / 10000;
                            printMe = (int) conv;
                        }

                        System.out.print(printMe);
                    }
                }

                System.out.println("\"]");
            } //! end if delConsequence
            else System.out.println();

        }

        this.visited = true;

        e = children.elements();
        while (e.hasMoreElements()) {
            genDAG c = ((genDAG) e.nextElement());
            c.printOutgoingArcs(delConsequence);
        }

    }

//! does this work?
//! (does it work when there is a disconnected network?)

    public boolean isLeafNode() {
        if (children.size() == 0) return true;
        else return false;
    }

//! returns false if the operation did not succeed
//! true otherwise
//! note this is an irreversible operation!
//! the node 'head' will cease to exist

//! THIS IS NOT FINISHED YET, WHAT ABOUT GALL EDGES?
//! for now we just won't collapse such edges.

    public static boolean collapseEdge(genDAG tail, genDAG head) {
        if (head.isLeafNode()) return false;

        if (head.parents.size() > 1) return false;

        if (tail.parents.size() > 1) return false;    //! don't collapse exit edges from recomb nodes

        //! System.out.println("// Contracting an edge where tail has "+tail.children.size()+" children and head has "+head.parents.size()+" parents.");

        Enumeration hc = head.children.elements();

        tail.children.remove(head);

        while (hc.hasMoreElements()) {
            genDAG c = (genDAG) hc.nextElement();

            c.parents.remove(head);

            if (!c.parents.contains(tail)) c.parents.add(tail);

            if (!tail.children.contains(c)) tail.children.add(c);
        }
        return true;
    }

//! -------------------------------------------------------------------
//! THIS IS DANGEROUS. No guarantees for what happens after using this

    public static boolean deleteEdge(genDAG tail, genDAG head) {
        tail.children.remove(head);

        head.parents.remove(tail);

        return true;
    }
//! --------------------------------------------------------------------

//! This has been modified from the biDAG version

    public void newickDump() {
        int counter[] = new int[1];
        counter[0] = 1;

        this.numberRecombNodes(counter);

        System.out.println(this.doNewick() + ";");
    }

//! ---------------------------------------------------

    public void numberRecombNodes(int counter[]) {
        if (newickRecVisit == true) return;

        newickRecVisit = true;

        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            genDAG c = ((genDAG) e.nextElement());
            c.numberRecombNodes(counter);
        }

        if (parents.size() > 1) {
            newickRecNum = counter[0];
            counter[0]++;
        }
        return;
    }

//! --------------------------------------------------------
//! This needs to be fixed to make it work for stretched edges

    public String doNewick() {
        this.newickVisit = true;

        //! If it's a leaf...
        if (this.children.size() == 0) {
            return (Heuristic.getLeafName(this.data));
        }

        String childrenStrings[] = new String[children.size()];

        int at = 0;

        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            genDAG c = ((genDAG) e.nextElement());

            if (c.newickVisit == true) {
                childrenStrings[at] = "#H" + c.newickRecNum;
            } else childrenStrings[at] = c.doNewick();
            at++;
        }

        boolean benRecomb = this.parents.size() > 1;

        if ((children.size() > 1) || ((children.size() == 1) && (!benRecomb))) {
            String out = "(";
            for (int x = 0; x < childrenStrings.length; x++) {
                out = out + childrenStrings[x];
                if (x < childrenStrings.length - 1) out = out + ",";
            }
            out = out + ")";
            return (out);
        } else if (benRecomb) {
            //! WHAT IS THIS THEN?
            return ("(" + childrenStrings[0] + ")#H" + newickRecNum);
        } else
            System.out.println("FOUT!");

        return ("BOOM!");

    }


}

//! -----------------------------------------------------------------------------------------------------------
//! -----------------------------------------------------------------------------------------------------------
//! -----------------------------------------------------------------------------------------------------------
//! -----------------------------------------------------------------------------------------------------------
//! -----------------------------------------------------------------------------------------------------------

//! ----------------------
//! The root has no parent;
//! Leaves have no children, but do have data;
//! Nothing more than that...very general...can have cycles...
//! Is considered a leaf if


class biDAG {
    public biDAG parent;
    public biDAG child1, child2;
    public int data;
    public int dec[];
    public biDAG secondParent;
    public int auxDat;
    public boolean visited;
    public boolean simpleVisited;
    public boolean fixVisit;

    public int tripsWithin;

    public int nodeNum;

    public boolean isDummy;

    //! -----------------------
    public boolean newickVisit;

    public int newickRecNum;
    public boolean newickRecVisit;
//! -------------------------


    public biDAG() {
        parent = child1 = child2 = null;
        data = 0;
        dec = null;
        secondParent = null;
        auxDat = 0;
        visited = false;
        simpleVisited = false;    //! used when counting split & recomb vertices...
        tripsWithin = 0;

        isDummy = false;


        //! --------------------
        newickVisit = false;
        newickRecNum = -1;
        newickRecVisit = false;
        //! --------------------

    }

    //! Is only used for diagnostic purposes. Don't use for anything else!
    public void experimentalLeafPrint() {
        if (this.isLeafNode()) {
            System.out.println("leaf: " + this.data);
        } else {
            if (child1 != null) child1.experimentalLeafPrint();
            if (child2 != null) child2.experimentalLeafPrint();
        }
    }


    public void resetFixLeaves() {
        if (fixVisit = false) return;

        fixVisit = false;
        if (child1 != null) child1.resetFixLeaves();
        if (child2 != null) child2.resetFixLeaves();
    }

    public void dagFixLeaves(int map[]) {
        if (fixVisit) return;
        fixVisit = true;

        if (this.isLeafNode()) {
            data = map[data];
            return;
        } else {
            if (child1 != null) child1.dagFixLeaves(map);
            if (child2 != null) child2.dagFixLeaves(map);
        }
    }

    public void treeFixLeaves(int map[]) {
        //! Changes the leaf numberings from l to map[l];
        //! Note that we assume that l>=1;

        if (data != 0) {
            if ((child1 != null) || (child2 != null)) {
                System.out.println("Error 4");
            }
            data = map[data];
            return;
        } else {
            child1.treeFixLeaves(map);
            child2.treeFixLeaves(map);
            return;
        }

    }

    public static biDAG makeLeafNode() {
        biDAG node = new biDAG();
        node.data = 1;
        return node;
    }

    public static biDAG cherry12() {
        //! Returns a cherry, using 3 nodes,
        //! with leaf numbers 1,2.

        biDAG p = new biDAG();
        biDAG c1 = new biDAG();
        biDAG c2 = new biDAG();

        p.child1 = c1;
        p.child2 = c2;

        c1.parent = p;
        c1.data = 1;

        c2.parent = p;
        c2.data = 2;

        return p;
    }

    public void newickDump() {
        int counter[] = new int[1];
        counter[0] = 1;

        numberRecombNodes(counter);

        System.out.println(this.doNewick() + ";");
    }

//! ---------------------------------------------------

    public void numberRecombNodes(int counter[]) {
        if (newickRecVisit == true) return;

        newickRecVisit = true;

        if (child1 != null) {
            child1.numberRecombNodes(counter);
        }

        if (child2 != null) {
            child2.numberRecombNodes(counter);
        }

        if ((parent != null) && (secondParent != null)) {
            newickRecNum = counter[0];
            counter[0]++;
        }
        return;
    }

//! --------------------------------------------------------

    public String doNewick() {
        this.newickVisit = true;

        if (this.isLeafNode()) {
            return (Heuristic.getLeafName(this.data));
        }

        //! Ok, so it's either a leaf node or a recomb node...

        String lstring = null;
        String rstring = null;

        if (child1 != null) {
            if (child1.newickVisit == true) {
                lstring = "#H" + child1.newickRecNum;
            } else
                lstring = child1.doNewick();
        }

        if (child2 != null) {
            if (child2.newickVisit == true) {
                rstring = "#H" + child2.newickRecNum;
            } else
                rstring = child2.doNewick();
        }

        boolean benRecomb = ((parent != null) && (secondParent != null));

        if ((child1 != null) && (child2 != null)) {
            return ("(" + lstring + "," + rstring + ")");
        } else if (benRecomb) {
            return ("(" + lstring + ")#H" + newickRecNum);
        } else
            System.out.println("FOUT!");

        return ("BOOM!");

    }


    public boolean isSplitNode() {
        if (parent == null) return false;    //! don't include the root

        return ((child1 != null) && (child2 != null));
    }

    public boolean isRecombNode() {
        return ((parent != null) && (secondParent != null));
    }

    public boolean isExternalRecomb() {
        if (!isRecombNode()) return false;

        if (child1.isLeafNode()) return true;

        return false;
    }

    public boolean isInternalSplitNode() {
        if (!isSplitNode()) return false;

        if ((child1.isLeafNode()) || (child2.isLeafNode())) return false;

        return true;
    }


    public void dump() {
        if (data != 0) System.out.print(data);
        else {
            if ((child1 == null) || (child2 == null)) {
                System.out.println("Error 5");
                if (child1 == null) System.out.println("child1 is null");
                if (child2 == null) System.out.println("child2 is null");
                System.exit(0);
            }
            System.out.print("[");
            child1.dump();
            System.out.print(",");
            child2.dump();
            System.out.print("]");
        }
    }

//! Subdivides the edge above this, creates a new leaf with element l

    public biDAG insertAbove(int l) {
        biDAG q = new biDAG();
        biDAG c = new biDAG();

        //! This is the 'old' parent...
        biDAG meParent = this.parent;

        c.data = l;
        c.parent = q;

        q.child1 = c;
        q.child2 = this;

        this.parent = q;

        if (meParent != null) {
            q.parent = meParent;

            if (meParent.child1 == this) {
                meParent.child1 = q;
            } else {
                meParent.child2 = q;
            }

        } else {
            q.parent = null;
        }

        //! That should be it......
        return c;
    }


    //! This undoes what we did in the previous function....
    public void removeAbove() {
        biDAG oldParent = this.parent.parent;
        biDAG q = this.parent;

        this.parent = oldParent;

        if (oldParent != null) {
            if (oldParent.child1 == q) {
                oldParent.child1 = this;
            } else
                oldParent.child2 = this;
        } else {
            this.parent = null;
        }

        //! This will probably help garbage collection...
        q.parent = null;
    }


//! We assume that dagMap has indexing space for 1...l where l
//! is the number of leaves. ALSO ASSUMES THAT 'VISITED' is UNUSED/RESET

    public void getDAGLeafMap(biDAG[] dagMap) {
        if (visited == true) return;

        if (data != 0) {
            dagMap[data] = this;
        }
        visited = true;

        if (child1 != null) child1.getDAGLeafMap(dagMap);
        if (child2 != null) child2.getDAGLeafMap(dagMap);
    }

    public void resetVisited() {
        //! Heuristic.report("In resetVisited()");

        if (visited == false) return;

        visited = false;

        if (child1 != null) child1.resetVisited();
        if (child2 != null) child2.resetVisited();
    }

    public void resetAuxData() {
        if (auxDat == 0) return;

        auxDat = 0;

        if (child1 != null) child1.resetAuxData();
        if (child2 != null) child2.resetAuxData();
    }

//! This prints the dag (rooted at this node) in the funky .DOT format
//! Still needs to be refined...NOTE THAT THIS HAS SIDE EFFECTS ON AUXDAT
//! AND VISITED SO ONLY CALL IF UNUSED OR "RESET"!!!!

    public void dumpDAG() {
        //! Hmmm, a little bit tricky...

        int num[] = new int[1];

        //! This numbering is arbitrary, just to draw a distinction from leaves;
        num[0] = 1000;

        System.out.println("strict digraph G {");

        //! First, we'll number ALL the nodes....
        this.numVisit(num);

        //! Ok, so there is a numbering

        this.printOutgoingArcs();
        System.out.println("}");
    }

    private void printOutgoingArcs() {
        if (child1 != null) System.out.println(this.auxDat + " -> " + child1.auxDat);
        if (child2 != null) System.out.println(this.auxDat + " -> " + child2.auxDat);
        this.visited = true;

        if (child1 != null) {
            if (child1.visited == false) child1.printOutgoingArcs();
        }

        if (child2 != null) {
            if (child2.visited == false) child2.printOutgoingArcs();
        }

    }

    private void numVisit(int num[]) {
        //! If already been visited, finished
        if (this.auxDat != 0) return;

        this.auxDat = num[0];

        if (this.data != 0) {
            this.auxDat = this.data;
            System.out.println(this.auxDat + " [shape=box, width=0.3, label=\"" + Heuristic.getLeafName(this.auxDat) + "\"];");
        } else {
            System.out.println(this.auxDat + " [shape=point];");
        }

        num[0]++;

        if (this.child1 != null) child1.numVisit(num);
        if (this.child2 != null) child2.numVisit(num);
    }


//! This is hacked, might not work so well...

    public boolean isLeafNode() {
        if ((this.child1 == null) && (this.child2 == null)) {
            return true;
        }
        return false;
    }

    public boolean isLowLeaf() {
        if (!isLeafNode()) return false;

        biDAG p = this.parent;

        //! If the parent is recomb node, then the leaf is a low leaf...

        if (p.isRecombNode()) return true;

        //! So we're only a low leaf if both children of our parent are leaves...

        if (p.child1 != null) {
            if (p.child1.isLeafNode() == false) return false;
        }

        if (p.child2 != null) {
            if (p.child2.isLeafNode() == false) return false;
        }

        return true;
    }


    public boolean testSplit(Vector left, Vector right) {
        //! Tests whether it has the structure of two caterpillars with a common root
        //! and if so returns the leaf orders for left and right...from which we
        //! can compute stuff easily

        biDAG leftBranch = child1;
        biDAG rightBranch = child2;

        //! First left

        if (leftBranch.isLeafNode()) {
            left.addElement(leftBranch);
        } else {
            //! If it's not a leaf, then it has two children, so
            //! we can test if it's a caterpillar

            boolean check = leftBranch.testCaterpillar(left);

            if (!check) return false;
        }

        //! Now the right...

        if (rightBranch.isLeafNode()) {
            right.addElement(rightBranch);
        } else {
            boolean check = rightBranch.testCaterpillar(right);

            if (!check) return false;
        }

        return true;
    }


//! Tests whether it is a caterpillar and, if so, puts the
//! leaf nodes in order of visiting in Vector order.
//! I'm going to assume that it has at least two
//! leaves....because I don't know what to do with fewer...
//! It might work with fewer leaves, but I can't guarantee it...

    public boolean testCaterpillar(Vector order) {
        boolean finished = false;

        biDAG at = this;

        while (!finished) {
            //! The fact that we assume at most two leaves, and a network structure,
            //! prevents null pointer exceptions here I think

            biDAG lc = at.child1;
            biDAG rc = at.child2;

            boolean lcLeaf = lc.isLeafNode();
            boolean rcLeaf = rc.isLeafNode();

            if ((!lcLeaf) && (!rcLeaf))
                return false;

            if (lcLeaf && rcLeaf) {
                //! Last two elements, order is not important...
                order.addElement(lc);
                order.addElement(rc);
                return true;
            }

            if (lcLeaf) {
                order.addElement(lc);
                at = rc;
                continue;
            }

            if (rcLeaf) {
                order.addElement(rc);
                at = lc;
                continue;
            }
        }

        return false;
    }

//! This assumes that the biDAG is a tree. Funkier things are
//! needed for DAGs...will go WRONG if it is not a tree!
//! is crude and slow, but toch

    public void oneLeafAdjustTree(int correction) {
        if (!isLeafNode()) {
            if (child1 != null) child1.oneLeafAdjustTree(correction);
            if (child2 != null) child2.oneLeafAdjustTree(correction);
        }
        if (data >= correction) data++;
    }

    public static void glueLeafBetween(biDAG leftGraft, biDAG rightGraft, int l) {
        Heuristic.report("In glueleafbetween");

        biDAG lb = new biDAG();
        biDAG rb = new biDAG();

        biDAG lparent = leftGraft.parent;
        biDAG rparent = rightGraft.parent;

        lb.parent = lparent;
        rb.parent = rparent;

        biDAG node = new biDAG();
        node.parent = lb;
        node.secondParent = rb;

        biDAG forgot = new biDAG();
        forgot.data = l;
        forgot.parent = node;
        node.child1 = forgot;

        lb.child1 = node;
        rb.child2 = node;

        lb.child2 = leftGraft;
        rb.child1 = rightGraft;

        leftGraft.parent = lb;
        rightGraft.parent = rb;

        if (lparent.child1 == leftGraft) {
            lparent.child1 = lb;
        } else if (lparent.child2 == leftGraft) {
            lparent.child2 = lb;
        } else System.out.println("ERROR#30: Something went very wrong.");

        if (rparent.child1 == rightGraft) {
            rparent.child1 = rb;
        } else if (rparent.child2 == rightGraft) {
            rparent.child2 = rb;
        } else System.out.println("ERROR#31: Something went very wrong...");

        Heuristic.report("Finishing glue leaf between.");

    }


}

class WuAnswer {
    biDAG root;
    int tripsGot;
}

class WuWeighted {
    public static int lookup[];
    public static int leftp[];
    public static int rightp[];

    public static final int LOWER_AHO = 0;
    public static final int UPPER_AHO = 1000;

    private static final boolean WU_DEBUG = false;

    public static WuAnswer wu(int trips[][], int weight[]) {
        int highest = 0;

        //! Quickly compute the highest leaf...
        for (int x = 0; x < trips.length; x++) {
            for (int y = 0; y < 3; y++) {
                if (trips[x][y] > highest) {
                    highest = trips[x][y];
                }
            }
        }

        if (highest > 30) {
            System.out.println("ERROR: This implementation of Wu's tree-building algorithm will only accept inputs with at most 30 leaves.");
            System.exit(0);
        }

        if (WU_DEBUG) System.out.println("Highest leaf is " + highest);

        int looksize = 1 << (highest + 1);

        //! for example, highest=3, 4 leaves, 4 bits needed, 0...15, roof is 16 = 1 shift 4

        //! System.out.println("Looksize is "+looksize);

        lookup = new int[looksize];
        leftp = new int[looksize];
        rightp = new int[looksize];

        //! Is a lookup for all subsets...

        int answer = internWu(trips, weight, highest, 0);

        if (answer == -1) {
            System.out.println("WEIRD PROBLEM!");

            //! return trips.length;
            System.exit(0);
        }

        boolean start[] = new boolean[highest + 1];
        for (int x = 1; x < start.length; x++) start[x] = true;
        biDAG b = printPartition(start, 0, trips, weight);

        WuAnswer wa = new WuAnswer();
        wa.tripsGot = answer;
        wa.root = b;
        b.tripsWithin = answer;

        return wa;
    }

    //! end wu

    public static biDAG printPartition(boolean bitvec[], int depth, int trips[][], int weight[]) {
        int y = arrayToInt(bitvec);

        int count = 0;
        int firstLeaves[] = new int[3];

        for (int x = 1; x < bitvec.length; x++) {
            if (bitvec[x]) {
                count++;
                if (count < 3) firstLeaves[count] = x;
            }
        }

		/*
		System.out.print("//");
		for(int p=0; p<(depth*3); p++) System.out.print(" ");

		for( int x=0; x<bitvec.length; x++ )
			{
			if(bitvec[x]) System.out.print(x+" ");
			}
		System.out.println();
		*/

        //! So the first at most 2 leaves are in firstLeaves[1] and [2]

        if (count == 0) return null;

        if (count == 1) {
            biDAG r = new biDAG();
            r.data = firstLeaves[1];
            return r;
        }

        if (count == 2) {
            biDAG r = biDAG.cherry12();
            r.child1.data = firstLeaves[1];
            r.child2.data = firstLeaves[2];
            return r;
        }

        int lnum = leftp[y];
        int rnum = rightp[y];

        boolean bv2[] = new boolean[bitvec.length];
        boolean bv3[] = new boolean[bitvec.length];
        intToArray(lnum, bv2);
        intToArray(rnum, bv3);

        biDAG left = null;
        biDAG right = null;

        biDAG root = new biDAG();

        if (lnum > 0) left = printPartition(bv2, depth + 1, trips, weight);
        if (rnum > 0) right = printPartition(bv3, depth + 1, trips, weight);

        if (left != null) {
            root.child1 = left;
            left.parent = root;
        }

        if (right != null) {
            root.child2 = right;
            right.parent = root;
        }

        return root;
    }


    public static int arrayToInt(boolean bitvec[]) {
        int add = 1;
        int total = 0;

        for (int x = 0; x < bitvec.length; x++) {
            if (bitvec[x]) total += add;
            add = add * 2;
        }

        return total;
    }

    private static void intToArray(int num, boolean bitvec[]) {
        int at = 0;
        int val = num;

        //! clean the bitvec
        for (int c = 0; c < bitvec.length; c++) bitvec[c] = false;

        while (val != 0) {
            if ((val % 2) == 1) bitvec[at] = true;

            at++;
            val = val / 2;
        }

    }
    //! Assumes that lookup has already been initialised,
    //! and that highest is the index of the highest leaf

    private static int internWu(int trips[][], int weight[], int highest, int level) {
        if (WU_DEBUG) System.out.println("In internWu, level " + level);
        //! calculate the bitpattern for the subset

        boolean leafSeen[] = new boolean[highest + 1];

        int totWeight = 0;

        for (int x = 0; x < trips.length; x++) {
            leafSeen[trips[x][0]] = true;
            leafSeen[trips[x][1]] = true;
            leafSeen[trips[x][2]] = true;

            totWeight += weight[x];
        }

        int pattern = arrayToInt(leafSeen);

        //! if we've done this subset already, finish
        if (lookup[pattern] != 0) {
            if (WU_DEBUG) System.out.println("Leaving Wu level " + level);
            return lookup[pattern];
        }

		/*
		if( trips.length == 1 )
			{
			lookup[pattern] = weight[0];

			if(WU_DEBUG) System.out.println("Leaving Wu level "+level);
			return weight[0];
			}
		*/

        if (trips.length == 0) {
            if (WU_DEBUG) System.out.println("Leaving Wu level " + level);
            return 0;
        }

        //! Need to do a leaf mapping here to optimise speed

        int totLeaves = 0;
        int backMap[] = new int[highest + 1];
        int forwardMap[] = new int[highest + 1];

        for (int x = 0; x < leafSeen.length; x++) {
            if (leafSeen[x]) {
                if (x == 0) System.out.println("Should never get here.");
                backMap[totLeaves] = x;
                forwardMap[x] = totLeaves;
                totLeaves++;
            }
        }

        if (WU_DEBUG) System.out.println(totLeaves + " leaves involved.");

        //! backMap now points to the indices of the actually existing leaves...starts at backmap[0]

        //! For example, suppose there are 3 leaves in total. that needs 3 bits = 4,2,1 so 8 is the
        //! number we do not do....

        int roof = 1 << totLeaves;

        //! Ok, now we need to try all possible subsets...

        boolean bitvec[] = new boolean[totLeaves];

        boolean bipartition[] = new boolean[highest + 1];
        boolean negpartition[] = new boolean[highest + 1];

        //! This iterates through all subsets...starts at 1
        //! don't need roof-1 either...

        int globalbest = -1;
        boolean bestBi[] = new boolean[highest + 1];
        boolean bestNegBi[] = new boolean[highest + 1];

        boolean ahoGood = false;
        int mycode = 0;

        if ((LOWER_AHO <= totLeaves) && (totLeaves <= UPPER_AHO)) {
            //! Try running the Aho algorithm

            //! System.out.println("Running Aho on "+totLeaves+" leaves.");

            Graph aho = new Graph(totLeaves);

            for (int p = 0; p < trips.length; p++) {
                if (weight[p] == 0) {
                    continue;
                }
                aho.addEdge(forwardMap[trips[p][0]] + 1, forwardMap[trips[p][1]] + 1);
                //! So vertices in the Aho graph range from 0...totLeaves-1;
            }

            boolean status[] = aho.exploreComponent();

            int count = 0;
            for (int scan = 0; scan < status.length; scan++) {
                if (status[scan] == Graph.EXP_VISITED) count++;
            }

            if (count != totLeaves) {
                ahoGood = true;
                //! System.out.println("Aho graph not connected, DOES help here.");
                mycode = arrayToInt(status);
            }

        }

        int begin_scan = 1;
        int end_scan = roof - 1;

        if (ahoGood) {
            begin_scan = mycode;
            end_scan = mycode + 1;
        }

        for (int s = begin_scan; s < end_scan; s++) {
            intToArray(s, bitvec);

            for (int x = 0; x < bipartition.length; x++) {
                bipartition[x] = false;
                negpartition[x] = false;
            }

            if (WU_DEBUG) System.out.println("Trying partition: ");
            if (WU_DEBUG && ahoGood) {
                System.out.println("This is an Aho shortcut");
            }

            for (int x = 0; x < bitvec.length; x++) {
                if (bitvec[x]) {
                    bipartition[backMap[x]] = true;
                    if (WU_DEBUG) System.out.print(backMap[x] + " ");
                }
            }

            if (WU_DEBUG) System.out.println();

            //! make the negative partition...
            for (int x = 0; x < bipartition.length; x++) {
                negpartition[x] = (!bipartition[x] && leafSeen[x]);
                if (WU_DEBUG && negpartition[x] && leafSeen[x]) System.out.print(x + " ");
            }

            if (WU_DEBUG) System.out.println();


            //! so bipartition and negpartition now holds the feitelijke bipartition

            int inLeft = 0;
            int inRight = 0;
            int goodWeight = 0;
            int badWeight = 0;

            int leftWeight = 0;
            int rightWeight = 0;

            int highLeft = 0;
            int highRight = 0;

            for (int x = 0; x < trips.length; x++) {
                if (bipartition[trips[x][0]] && bipartition[trips[x][1]] && bipartition[trips[x][2]]) {
                    inLeft++;

                    leftWeight += weight[x];

                    if (trips[x][0] > highLeft) highLeft = trips[x][0];
                    if (trips[x][1] > highLeft) highLeft = trips[x][1];
                    if (trips[x][2] > highLeft) highLeft = trips[x][2];
                } else if (negpartition[trips[x][0]] && negpartition[trips[x][1]] && negpartition[trips[x][2]]) {
                    inRight++;

                    rightWeight += weight[x];

                    if (trips[x][0] > highRight) highRight = trips[x][0];
                    if (trips[x][1] > highRight) highRight = trips[x][1];
                    if (trips[x][2] > highRight) highRight = trips[x][2];
                } else if (bipartition[trips[x][0]] && bipartition[trips[x][1]] && negpartition[trips[x][2]]) {
                    goodWeight += weight[x];
                } else if (negpartition[trips[x][0]] && negpartition[trips[x][1]] && bipartition[trips[x][2]]) {
                    goodWeight += weight[x];
                } else badWeight += weight[x];
            }

            if ((leftWeight + rightWeight + goodWeight + badWeight) != totWeight) {
                System.out.println("PROBLEM!");
                System.exit(0);
            }

            //! Build the left tree

            int ltrips[][] = new int[inLeft][3];
            int rtrips[][] = new int[inRight][3];

            int lweight[] = new int[ltrips.length];
            int rweight[] = new int[rtrips.length];

            int lat = 0;
            int rat = 0;

            for (int x = 0; x < trips.length; x++) {
                if (bipartition[trips[x][0]] && bipartition[trips[x][1]] && bipartition[trips[x][2]]) {
                    ltrips[lat][0] = trips[x][0];
                    ltrips[lat][1] = trips[x][1];
                    ltrips[lat][2] = trips[x][2];

                    lweight[lat] = weight[x];

                    lat++;
                }
                if (negpartition[trips[x][0]] && negpartition[trips[x][1]] && negpartition[trips[x][2]]) {
                    rtrips[rat][0] = trips[x][0];
                    rtrips[rat][1] = trips[x][1];
                    rtrips[rat][2] = trips[x][2];

                    rweight[rat] = weight[x];

                    rat++;
                }
            }

            //! if( (ltrips.length*0.33) + (rtrips.length*0.33) + goodTrips  > bound ) return -1;

            int lopt = internWu(ltrips, lweight, highLeft, level + 1);

            int ropt = internWu(rtrips, rweight, highRight, level + 1);

            int tot = lopt + ropt + goodWeight;

            if (tot > globalbest) {
                globalbest = tot;
                for (int p = 0; p < bipartition.length; p++) {
                    bestBi[p] = bipartition[p];
                    bestNegBi[p] = negpartition[p];
                }
            }

        } //! the subset iterator

        lookup[pattern] = globalbest;

        //! System.out.println("Registering best for code "+pattern);
        leftp[pattern] = arrayToInt(bestBi);
        rightp[pattern] = arrayToInt(bestNegBi);

        if (WU_DEBUG) System.out.println("Leaving Wu level " + level);

        return globalbest;
    }

    //! end internWu

}

class Graph {
    private int leaves;

    public static final boolean EXP_UNVISITED = false;
    public static final boolean EXP_VISITED = true;

    public static final int NOT_VISITED = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    boolean adj[][];
    int deg[];

    public Graph(int n) {
        leaves = n;
        adj = new boolean[n][n];
        deg = new int[n];
    }

    public boolean containsEdge(int a, int b) {
        return (adj[a - 1][b - 1]);
    }

    public void addEdge(int a, int b) {
        if (adj[a - 1][b - 1] == false) {
            deg[a - 1]++;
            deg[b - 1]++;
        }

        //! These things are always added in unison...
        adj[a - 1][b - 1] = adj[b - 1][a - 1] = true;
    }

    public int degree(int a) {
        return deg[a - 1];
    }

    public boolean[] exploreComponent() {
        boolean state[] = new boolean[leaves];
        expComp(0, state);

        return state;
    }

    private void expComp(int startHere, boolean state[]) {
        if (state[startHere] != EXP_UNVISITED) return;

        state[startHere] = EXP_VISITED;

        for (int x = 0; x < leaves; x++) {
            if (x == startHere) continue;

            //! System.out.println("Trying containsEdge("+startHere+","+x);

            if (adj[startHere][x]) expComp(x, state);
        }

        return;
    }


//! Inspects if the vertices form two disjoint cliques and, zo ja,
//! returns the partition (LEFT/RIGHT) in the return array
//! otherwise returns null
//! The array should be indexed on [1...leaves]

    public int[] getDoubleClique() {
        //! I should first check if the complement graph is bipartite.
        //! If there are more then 2 components, then it won't be
        //! And then I can check if it is a complete bipartite graph.
        //! That is: everyone on LEFT has degree equal to RIGHT, and
        //! viceversa

        int visited[] = new int[leaves + 1];

        visited[0] = -1000;

        //! We'll start with vertex 1
        int start = 1;

        boolean success = visit(1, LEFT, visited);

        if (!success) return null;

        int lClique = 0;
        int rClique = 0;

        for (int scan = 1; scan <= leaves; scan++) {
            if (visited[scan] == NOT_VISITED) return null;
            if (visited[scan] == LEFT) lClique++;
            if (visited[scan] == RIGHT) rClique++;
        }

        //! So if we've got here we know how many guys are in the
        //! left clique, and in he right...

        for (int scan = 1; scan <= leaves; scan++) {
            if (visited[scan] == LEFT) {
                if (degree(scan) != (lClique - 1)) return null;
            } else {
                if (degree(scan) != (rClique - 1)) return null;
            }

        }

        return visited;
    }


    private boolean visit(int vertex, int colourWith, int state[]) {
        if (state[vertex] != NOT_VISITED) {
            if (state[vertex] != colourWith) return false;
            return true;
        }

        state[vertex] = colourWith;

        int childCol;
        if (colourWith == LEFT) childCol = RIGHT;
        else childCol = LEFT;

        //! Now, try all COMPLEMENT children...

        for (int scan = 1; scan <= leaves; scan++) {
            if (vertex == scan) continue;
            if (!containsEdge(vertex, scan)) {
                boolean gelukt = visit(scan, childCol, state);
                if (!gelukt) return false;
            }

        }

        return true;
    }


}


//! ------------------------------------------------------------------------------------

class genExplore {
    //! We need these constants for the dynamic programming...
    public static final int UNKNOWN = 0;
    public static final int YES = 1;
    public static final int NO = -1;

    public int num_leaves;
    public int num_nodes;
    public int num_edges;

    public Vector nodes;
    public Vector edges;
    public Vector leaves;

    public genDAG nodearray[];       //! maps nodeNums -> biDAGs
    public int leafarray[];         //! maps leafNums -> leafNums

    public boolean adjmatrix[][];

    //! for the algorithm of pawel
    public int join[][];
    public int cons[][][];

    public int desc[][];

    public genDAG root;

    public genExplore(genDAG network) {
        this.root = network;

        //! If this is not necessary then it will return
        //! immediately without wasting time.

        root.resetVisited();

        initiateExploration();
    }


//! Using the information contained in the genExplore, this clones the
//! genDAG that it represents.

    public genDAG[] clonegenDAG() {
        genDAG newNodes[] = new genDAG[num_nodes];

        if (root.nodeNum != 0) {
            System.out.println("I expected the root to have number 0...");
            System.exit(0);
        }

        for (int x = 0; x < num_nodes; x++) {
            newNodes[x] = new genDAG();
        }

        for (int x = 0; x < num_nodes; x++) {
            genDAG oldGuy = nodearray[x];
            genDAG newGuy = newNodes[x];

            //! Only copy over necessar stuff...BE CAREFUL HERE

            newGuy.data = oldGuy.data;
            newGuy.nodeNum = oldGuy.nodeNum;

            Enumeration p = oldGuy.parents.elements();

            while (p.hasMoreElements()) {
                genDAG parentNode = (genDAG) p.nextElement();

                newGuy.parents.add(newNodes[parentNode.nodeNum]);
            }

            Enumeration c = oldGuy.children.elements();

            while (c.hasMoreElements()) {
                genDAG childNode = (genDAG) c.nextElement();

                newGuy.children.add(newNodes[childNode.nodeNum]);
            }

        }

        //! This should be the root......
        return newNodes;
    }


//! -------------------------------------------------------------------------
//! ONLY USE THIS IF the matrices (con, desc, join) are still empty, so best
//! to call it immediately after initialization. Assumes that tail and head
//! are edges in this DAG!

    public void killEdge(genDAG tail, genDAG head) {
        genDAG.deleteEdge(tail, head);

        int tailnum = tail.nodeNum;
        int headnum = head.nodeNum;

        adjmatrix[tailnum][headnum] = false;

        //! and, err, that's it...
    }
//! ------------------------------------------------------------------------


    public Vector getEdges() {
        return edges;
    }

    public void initiateExploration() {
        nodes = new Vector();
        edges = new Vector();
        leaves = new Vector();

        genExplore.scan(root, nodes, edges, leaves);

        num_nodes = nodes.size();
        num_edges = edges.size();
        num_leaves = leaves.size();

        //! System.out.println(num_nodes+";"+num_edges+";"+num_leaves);

        nodearray = new genDAG[num_nodes];

        //! leafarray maps leaf numbers to node numbers...
        //! and leaves start at 1, hence the +1

        leafarray = new int[num_leaves + 1];

        //! this is a DIRECTED adjacency matrix...
        adjmatrix = new boolean[num_nodes][num_nodes];

        //! Here we use -1 because we want node numberings to start at 0
        int id = num_nodes - 1;

        for (int x = 0; x < nodes.size(); x++) {
            genDAG g = (genDAG) nodes.elementAt(x);

            //! reverse postorder numbering: topological sort!
            g.nodeNum = id--;

            //! System.out.println("Assigning number "+g.nodeNum+" to genDAG"+g);

            //! nodearray maps nodeNums to biDAGs...
            nodearray[g.nodeNum] = g;
        }

        for (int x = 0; x < edges.size(); x++) {
            genDAG e[] = (genDAG[]) edges.elementAt(x);

            //! System.out.println("dagExplore found edge "+e[0]+" -> "+e[1]);

            int tail = e[0].nodeNum;
            int head = e[1].nodeNum;

            adjmatrix[tail][head] = true;
        }

        for (int x = 0; x < leaves.size(); x++) {
            genDAG g = (genDAG) leaves.elementAt(x);
            leafarray[g.data] = g.nodeNum;
        }

        cons = new int[num_nodes][num_nodes][num_nodes];
        join = new int[num_nodes][num_nodes];
        desc = new int[num_nodes][num_nodes];

        //! if necessary we can add all kinds of other look-up matrices, Vectors and so on...
    }

    public static void scan(genDAG g, Vector nodes, Vector edges, Vector leaves) {
        if (g.visited == true) return;

        //! Simplistic.doublereport("Visiting node "+g);

        //! System.out.println("Scanning the network...");

        g.visited = true;

        Enumeration c = g.children.elements();

        while (c.hasMoreElements()) {
            genDAG myedge[] = new genDAG[2];
            genDAG head = (genDAG) c.nextElement();

            myedge[0] = g;
            myedge[1] = head;

            edges.addElement(myedge);

            genExplore.scan(head, nodes, edges, leaves);
        }

        if (g.isLeafNode()) leaves.addElement(g);

        //! post order...
        nodes.addElement(g);
    }

//! x and y are nodes, not leaves
//! asks: "is x a descendant of y?"
//! again uses memoisation

    public boolean isDescendant(int x, int y) {
        if (x == y) desc[x][y] = YES;

        if (desc[x][y] == YES) return true;
        if (desc[x][y] == NO) return false;

        //! So descendancy relation is not yet known for x, y
        //! Let's compute it and store the answer...

        genDAG X = nodearray[x];

        Enumeration p = X.parents.elements();

        while (p.hasMoreElements()) {
            genDAG above = (genDAG) p.nextElement();
            int xprime = above.nodeNum;

            if (isDescendant(xprime, y)) {
                desc[x][y] = YES;
                return true;
            }
        }

        desc[x][y] = NO;
        return false;
    }

//! join is only internally used, so we assume the ints that you
//! give it refer to nodes, not leaves.

// "where join(x, z) is a predicate stating that N contains t != x and internally vertex-disjoint
// paths t â†’ x and t â†’ z."

    public boolean isJoin(int x, int z) {
        if (x == z) return false;    //! I think that's OK...or not? need to think about that...

        if (join[x][z] == YES) return true;
        if (join[x][z] == NO) return false;

        //! So it is not yet defined. Let's compute it...
        //! this requires some thinking...

        if (isDescendant(x, z)) {
            join[x][z] = YES;
            return true;
        }

        //! So x is not a descendant of z. In this case, the only chance of a join is if there
        //! is an explicit ^ shape with t distinct from both x and z

        genDAG Z = nodearray[z];

        if (Z.parents.size() == 0) {
            //! then z is the root. In this case x is not a descendant of z, and there
            //! can be no 't' higher than z, so the answer is FALSE.

            join[x][z] = NO;
            return false;
        }

        Enumeration p = Z.parents.elements();

        while (p.hasMoreElements()) {
            genDAG parentNode = (genDAG) p.nextElement();

            int zprime = parentNode.nodeNum;

            if (isJoin(x, zprime)) {
                join[x][z] = YES;
                return true;
            }
        }

        join[x][z] = NO;
        return false;
    }

//! this is the internal consistency checker: it
//! assumes that x, y, z refer to nodes not leaves...

    private boolean con(int x, int y, int z) {
        if ((x == y) || (x == z) || (y == z)) {
            cons[x][y][z] = NO;
            return false;
        }

        if (cons[x][y][z] == NO) return false;
        if (cons[x][y][z] == YES) return true;

        //! So cons[x][y][z] is UNKNOWN, we need to compute it!

        //! Remember, the node numberings are also the position
        //! in the topological sort. T'sort begins at 0.

        if ((x < z) && (y < z)) {
            genDAG Z = nodearray[z];

            Enumeration p = Z.parents.elements();

            while (p.hasMoreElements()) {
                genDAG parentNode = (genDAG) p.nextElement();

                int zprime = parentNode.nodeNum;

                if (con(x, y, zprime)) {
                    cons[x][y][z] = YES;
                    return true;
                }
            }

            cons[x][y][z] = NO;
            return false;
        }


        if ((x < y) && (z < y)) {
            if (adjmatrix[x][y] && isJoin(x, z)) {
                cons[x][y][z] = YES;
                return true;
            }

            genDAG Y = nodearray[y];

            Enumeration p = Y.parents.elements();

            while (p.hasMoreElements()) {
                genDAG parentNode = (genDAG) p.nextElement();

                int yprime = parentNode.nodeNum;

                if ((yprime != x) && (yprime != z)) {
                    if (con(x, yprime, z)) {
                        cons[x][y][z] = YES;
                        return true;
                    }

                }

            }

            cons[x][y][z] = NO;
            return false;
        }

        if ((y < x) && (z < x)) {
            if (adjmatrix[y][x] && isJoin(y, z)) {
                cons[x][y][z] = YES;
                return true;
            }

            genDAG X = nodearray[x];

            Enumeration p = X.parents.elements();

            while (p.hasMoreElements()) {
                genDAG parentNode = (genDAG) p.nextElement();

                int xprime = parentNode.nodeNum;

                if ((xprime != z) && (xprime != y)) {
                    if (con(xprime, y, z)) {
                        cons[x][y][z] = YES;
                        return true;
                    }

                }

            }

            cons[x][y][z] = NO;
            return false;
        }

        System.out.println("Shouldn't actually get here...");

        cons[x][y][z] = NO;
        return false;
    }


//! this is the external consistency checker:
//! x, y and z refer to LEAVES !!

    public boolean consistent(int x, int y, int z) {
        if ((x == 0) || (y == 0) || (z == 0)) {
            System.out.println("Leaf with 0 number?");
            System.exit(0);
        }

        if ((x == y) || (x == z) || (z == y)) return false;

        //! System.out.println("Internal: "+leafarray[x] + " " +leafarray[y] + "|" + leafarray[z] );

        return (con(leafarray[x], leafarray[y], leafarray[z]));
    }

//! This is added quite late (17 july 2009) but hopefully it will simply dump every triplet that is
//! in the underlying network.

    public void dumpEverything() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("network.trips"));
            out.write("// File: network.trips\n");
            out.write("// -----------------------------------------\n");
            out.write("// Generated in response to --dumpnettrips setting.\n");
            out.write("// Writing out every triplet that is in the network output by Heuristic.\n");
            out.write("// Please note that a triplet is written even if it is not in the input.\n");
            out.write("// Triplets will always be unweighted.\n");
            out.write("// -----------------------------------------\n");

            for (int a = 1; a <= num_leaves; a++)
                for (int b = (a + 1); b <= num_leaves; b++)
                    for (int c = (b + 1); c <= num_leaves; c++) {
                        if (this.consistent(a, b, c))
                            out.write(Heuristic.getLeafName(a) + " " + Heuristic.getLeafName(b) + " " + Heuristic.getLeafName(c) + "\n");
                        if (this.consistent(a, c, b))
                            out.write(Heuristic.getLeafName(a) + " " + Heuristic.getLeafName(c) + " " + Heuristic.getLeafName(b) + "\n");
                        if (this.consistent(b, c, a))
                            out.write(Heuristic.getLeafName(b) + " " + Heuristic.getLeafName(c) + " " + Heuristic.getLeafName(a) + "\n");
                    }


            out.close();
        } catch (IOException e) {
            System.out.println("//! ERROR: There was a problem writing the triplets to 'triplet.out'");
        }


        //! end dumpEverything
    }

}


//! -------------------------------------------------------------------------------------

class dagExplore {
    //! We need these constants for the dynamic programming...
    public static final int UNKNOWN = 0;
    public static final int YES = 1;
    public static final int NO = -1;

    public int num_mustHits;        //! the number of edge-disjoint edge subsets that
    //! simply have to be subdivided. cherries + dummy leaves
    public int num_leaves;
    public int num_nodes;
    public int num_edges;

    public Vector nodes;
    public Vector edges;
    public Vector leaves;
    public Vector dummy_nodes;

    public biDAG nodearray[];    //! maps nodeNums -> biDAGs
    public int leafarray[];        //! maps leafNums -> leafNums

    public boolean adjmatrix[][];

    //! for the algorithm of pawel
    public int join[][];
    public int cons[][][];

    public int desc[][];

    public biDAG root;

//! ------------ methods --------------

    public dagExplore(biDAG network) {
        this.root = network;

        //! If this is not necessary then it will return
        //! immediately without wasting time.

        root.resetVisited();

        initiateExploration();
    }

//! This is very similar to clonebiDAG();
//! does not support dummys...

    public genDAG buildgenDAG() {
        genDAG newNodes[] = new genDAG[num_nodes];

        if (root.nodeNum != 0) {
            System.out.println("I expected the root to have number 0...");
            System.exit(0);
        }

        for (int x = 0; x < num_nodes; x++) {
            newNodes[x] = new genDAG();
        }

        for (int x = 0; x < num_nodes; x++) {
            biDAG oldGuy = nodearray[x];
            genDAG newGuy = newNodes[x];

            //! Only copy over necessary stuff...BE CAREFUL HERE!!

            newGuy.data = oldGuy.data;
            newGuy.nodeNum = oldGuy.nodeNum;

            if (oldGuy.parent != null) {
                newGuy.parents.add(newNodes[oldGuy.parent.nodeNum]);
            }
            if (oldGuy.child1 != null) {
                newGuy.children.add(newNodes[oldGuy.child1.nodeNum]);
            }
            if (oldGuy.child2 != null) {
                newGuy.children.add(newNodes[oldGuy.child2.nodeNum]);
            }
            if (oldGuy.secondParent != null) {
                newGuy.parents.add(newNodes[oldGuy.secondParent.nodeNum]);
            }

        }

        return newNodes[0];
    }

//! ---------------------------------------------------------------------------

//! Using the information contained in the dagExplore, this clones the
//! biDAG that it represents.
//! changeLabels is a backmap that adjusts the labels (to make room for the ctbr that will be inserted)

    public biDAG[] clonebiDAG(int changeLabels[]) {
        biDAG newNodes[] = new biDAG[num_nodes];

        if (root.nodeNum != 0) {
            System.out.println("I expected the root to have number 0...");
            System.exit(0);
        }

        for (int x = 0; x < num_nodes; x++) {
            newNodes[x] = new biDAG();
        }

        for (int x = 0; x < num_nodes; x++) {
            biDAG oldGuy = nodearray[x];
            biDAG newGuy = newNodes[x];

            //! Only copy over necessar stuff...BE CAREFUL HERE

            newGuy.data = oldGuy.data;
            newGuy.isDummy = oldGuy.isDummy;
            newGuy.nodeNum = oldGuy.nodeNum;

            if (oldGuy.parent != null) {
                newGuy.parent = newNodes[oldGuy.parent.nodeNum];
            }
            if (oldGuy.child1 != null) {
                newGuy.child1 = newNodes[oldGuy.child1.nodeNum];
            }
            if (oldGuy.child2 != null) {
                newGuy.child2 = newNodes[oldGuy.child2.nodeNum];
            }
            if (oldGuy.secondParent != null) {
                newGuy.secondParent = newNodes[oldGuy.secondParent.nodeNum];
            }

            //! This simultaneously relabels the leaves toooo....
            if (oldGuy.data != 0) {
                newGuy.data = changeLabels[oldGuy.data];
                //! System.out.println("Mapped leaf "+oldGuy.data+" to "+newGuy.data);
            }

            //! That should be enough...
        }


        //! This should be the root......
        return newNodes;
    }


    public Vector getEdges() {
        return edges;
    }

    public Vector getDummies() {
        return dummy_nodes;
    }

    private void initiateExploration() {
        nodes = new Vector();
        edges = new Vector();
        leaves = new Vector();
        dummy_nodes = new Vector();

        //! System.out.println("Got here.");

        dagExplore.scan(root, nodes, edges, leaves, dummy_nodes);

        //! System.out.println("Didn't get here.");

        num_nodes = nodes.size();
        num_edges = edges.size();
        num_leaves = leaves.size();

        //! System.out.println(num_nodes+";"+num_edges+";"+num_leaves);

        nodearray = new biDAG[num_nodes];

        //! leafarray maps leaf numbers to node numbers...
        //! and leaves start at 1, hence the +1

        leafarray = new int[num_leaves + 1];

        //! this is a DIRECTED adjacency matrix...
        adjmatrix = new boolean[num_nodes][num_nodes];

        //! Here we use -1 because we want node numberings to start at 0
        int id = num_nodes - 1;

        for (int x = 0; x < nodes.size(); x++) {
            biDAG b = (biDAG) nodes.elementAt(x);

            //! reverse postorder numbering: topological sort!
            b.nodeNum = id--;

            //! System.out.println("Assigning number "+b.nodeNum+" to biDAG"+b);

            if ((b.secondParent == null) && (b.parent != null) && (b.child1 != null) && (b.child2 != null)) {
                biDAG c1 = b.child1;
                biDAG c2 = b.child2;

                //! See if this is the root of a cherry...
                if (c1.isLeafNode() && (!c1.isDummy) && c2.isLeafNode() && (!c2.isDummy)) num_mustHits++;
            } else if (b.isLeafNode() && b.isDummy) {
                biDAG p = b.parent;

                //! This is a dummy leaf that has a recomb node as parent, it has to be subdivided...
                if ((p.parent != null) && (p.secondParent != null)) num_mustHits++;
            }

            //! nodearray maps nodeNums to biDAGs...
            nodearray[b.nodeNum] = b;
        }

        for (int x = 0; x < edges.size(); x++) {
            biDAG e[] = (biDAG[]) edges.elementAt(x);

            //! System.out.println("dagExplore found edge "+e[0]+" -> "+e[1]);

            if ((e[1].parent != e[0]) && (e[1].secondParent != e[0])) {
                System.out.println("Catastrophic error in dagEncode.");
                System.exit(0);
            }

            int tail = e[0].nodeNum;
            int head = e[1].nodeNum;

            adjmatrix[tail][head] = true;
        }

        for (int x = 0; x < leaves.size(); x++) {
            biDAG b = (biDAG) leaves.elementAt(x);
            leafarray[b.data] = b.nodeNum;
        }

        cons = new int[num_nodes][num_nodes][num_nodes];
        join = new int[num_nodes][num_nodes];
        desc = new int[num_nodes][num_nodes];

        //! if necessary we can add all kinds of other look-up matrices, Vectors and so on...
    }

//! x and y are nodes, not leaves
//! asks: "is x a descendant of y?"
//! again uses memoisation

    public boolean isDescendant(int x, int y) {
        if (x == y) desc[x][y] = YES;

        if (desc[x][y] == YES) return true;
        if (desc[x][y] == NO) return false;

        //! So descendancy relation is not yet known for x, y
        //! Let's compute it and store the answer...

        biDAG X = nodearray[x];

        biDAG p1 = X.parent;

        if (p1 == null) {
            //! x is the root, y is not equal to x,
            //! so x cannot be a descendant of y

            desc[x][y] = NO;
            return false;
        }

        int xprime = p1.nodeNum;

        if (isDescendant(xprime, y)) {
            desc[x][y] = YES;
            return true;
        }

        biDAG p2 = X.secondParent;

        if (p2 == null) {
            desc[x][y] = NO;
            return false;
        }

        xprime = p2.nodeNum;

        if (isDescendant(xprime, y)) {
            desc[x][y] = YES;
            return true;
        }

        desc[x][y] = NO;
        return false;
    }


//! join is only internally used, so we assume the ints that you
//! give it refer to nodes, not leaves.

// "where join(x, z) is a predicate stating that N contains t != x and internally vertex-disjoint
// paths t â†’ x and t â†’ z."

    public boolean isJoin(int x, int z) {
        if (x == z) return false;    //! I think that's OK...or not? need to think about that...

        if (join[x][z] == YES) return true;
        if (join[x][z] == NO) return false;

        //! So it is not yet defined. Let's compute it...
        //! this requires some thinking...

        if (isDescendant(x, z)) {
            join[x][z] = YES;
            return true;
        }

        //! So x is not a descendant of z. In this case, the only chance of a join is if there
        //! is an explicit ^ shape with t distinct from both x and z

        biDAG Z = nodearray[z];

        biDAG p1 = Z.parent;

        if (p1 == null) {
            //! then z is the root. In this case x is not a descendant of z, and there
            //! can be no 't' higher than z, so the answer is FALSE.

            join[x][z] = NO;
            return false;
        }

        int zprime = p1.nodeNum;

        if (isJoin(x, zprime)) {
            join[x][z] = YES;
            return true;
        }

        biDAG p2 = Z.secondParent;

        if (p2 == null) {
            join[x][z] = NO;
            return false;
        }

        zprime = p2.nodeNum;

        if (isJoin(x, zprime)) {
            join[x][z] = YES;
            return true;
        }

        join[x][z] = NO;
        return false;
    }

//! this is the internal consistency checker: it
//! assumes that x, y, z refer to nodes not leaves...

    private boolean con(int x, int y, int z) {
        if ((x == y) || (x == z) || (y == z)) {
            cons[x][y][z] = NO;
            return false;
        }

        if (cons[x][y][z] == NO) return false;
        if (cons[x][y][z] == YES) return true;

        //! So cons[x][y][z] is UNKNOWN, we need to compute it!

        //! Remember, the node numberings are also the position
        //! in the topological sort. T'sort begins at 0.

        if ((x < z) && (y < z)) {
            biDAG Z = nodearray[z];

            biDAG p1 = Z.parent;

            if (p1 == null) {
                cons[x][y][z] = NO;
                return false;
            }

            int zprime = p1.nodeNum;

            if (con(x, y, zprime)) {
                cons[x][y][z] = YES;
                return true;
            }

            biDAG p2 = Z.secondParent;

            if (p2 == null) {
                cons[x][y][z] = NO;
                return false;
            }

            zprime = p2.nodeNum;

            if (con(x, y, zprime)) {
                cons[x][y][z] = YES;
                return true;
            }

            cons[x][y][z] = NO;
            return false;
        }


        if ((x < y) && (z < y)) {
            if (adjmatrix[x][y] && isJoin(x, z)) {
                cons[x][y][z] = YES;
                return true;
            }

            biDAG Y = nodearray[y];

            biDAG p1 = Y.parent;

            if (p1 == null) {
                //! Is this correct?

                cons[x][y][z] = NO;
                return false;
            }

            int yprime = p1.nodeNum;

            if ((yprime != x) && (yprime != z)) {
                if (con(x, yprime, z)) {
                    cons[x][y][z] = YES;
                    return true;
                }

            }

            biDAG p2 = Y.secondParent;

            if (p2 == null) {
                cons[x][y][z] = NO;
                return false;
            }

            yprime = p2.nodeNum;

            if ((yprime != x) && (yprime != z)) {
                if (con(x, yprime, z)) {
                    cons[x][y][z] = YES;
                    return true;
                }
            }

            cons[x][y][z] = NO;
            return false;
        }

        if ((y < x) && (z < x)) {
            if (adjmatrix[y][x] && isJoin(y, z)) {
                cons[x][y][z] = YES;
                return true;
            }

            biDAG X = nodearray[x];

            biDAG p1 = X.parent;

            if (p1 == null) {
                //! Is this correct?

                cons[x][y][z] = NO;
                return false;
            }

            int xprime = p1.nodeNum;

            if ((xprime != z) && (xprime != y)) {
                if (con(xprime, y, z)) {
                    cons[x][y][z] = YES;
                    return true;
                }

            }

            biDAG p2 = X.secondParent;

            if (p2 == null) {
                cons[x][y][z] = NO;
                return false;
            }

            xprime = p2.nodeNum;

            if ((xprime != z) && (xprime != y)) {
                if (con(xprime, y, z)) {
                    cons[x][y][z] = YES;
                    return true;
                }
            }

            cons[x][y][z] = NO;
            return false;
        }

        System.out.println("Shouldn't actually get here...");

        cons[x][y][z] = NO;
        return false;
    }

//! this is the external consistency checker:
//! x, y and z refer to LEAVES !!

    public boolean consistent(int x, int y, int z) {
        if ((x == 0) || (y == 0) || (z == 0)) {
            System.out.println("Leaf with 0 number?");
            System.exit(0);
        }

        if ((x == y) || (x == z) || (z == y)) return false;

        //! System.out.println("Internal: "+leafarray[x] + " " +leafarray[y] + "|" + leafarray[z] );

        return (con(leafarray[x], leafarray[y], leafarray[z]));
    }

    public static void scan(biDAG b, Vector nodes, Vector edges, Vector leaves, Vector dummy_nodes) {
        if (b.visited == true) return;

        //! Simplistic.doublereport("Visiting node "+b);

        //! System.out.println("Scanning the network...");

        b.visited = true;

        if (b.child1 != null) {
            biDAG myedge[] = new biDAG[2];
            myedge[0] = b;
            myedge[1] = b.child1;

            if ((myedge[1].parent != myedge[0]) && (myedge[1].secondParent != myedge[0])) {
                System.out.println(myedge[1].parent);
                System.out.println(myedge[1].secondParent);

                System.out.println("I DON'T UNDERSTAND!");
                System.exit(0);
            }


            edges.addElement(myedge);

            dagExplore.scan(b.child1, nodes, edges, leaves, dummy_nodes);
        }

        if (b.child2 != null) {
            biDAG myedge[] = new biDAG[2];
            myedge[0] = b;
            myedge[1] = b.child2;

            if ((myedge[1].parent != myedge[0]) && (myedge[1].secondParent != myedge[0])) {
                System.out.println("I DON'T UNDERSTAND EITHER!");
                System.exit(0);
            }

            edges.addElement(myedge);

            dagExplore.scan(b.child2, nodes, edges, leaves, dummy_nodes);
        }

        //! so it does not get added as a leaf...REMEMBER THIS!!!

        if (b.isLeafNode() && (!b.isDummy)) leaves.addElement(b);

        if (b.isDummy) dummy_nodes.addElement(b);

        //! post order...
        nodes.addElement(b);
    }

}

class FortyEight {
    public static long snarchive[];
    public static int topgall[];

    public static int bestA;

    public static double derandomize(TripletSet t) {
        int n = t.tellLeaves();

        System.out.println("Running conditional expectation derandomization on " + n + " leaves.");

        System.out.println("Step 1 : Calculating ratios and caterpillar network structure for worst case (i.e. the full triplet set)...");

        snarchive = new long[n + 1];
        topgall = new int[n + 1];

        snarchive[0] = 0;
        snarchive[1] = 0;
        snarchive[2] = 0;
        snarchive[3] = 2;

        topgall[2] = 2;
        topgall[1] = 1;

        //! System.out.println("\t\t S(n) \t\t |T_1(n)| \t\t S(n)/|T_1(n)| \t\t TopGall");

        for (long k = 0; k <= n; k++) {
            if (k < 3) {
                //! System.out.println("S("+k+") = 0");
                continue;
            }

            long div = 3 * choose(k, 3);

            long tb = theirBest(k);
            snarchive[(int) k] = tb;
            double ratio = (tb * 1.0) / (div * 1.0);

            //! System.out.println("n="+k+"\t\t"+tb+"\t\t"+div+"\t\t"+((tb*1.0)/(div*1.0))+"\t\t" + topgall[(int)k]);
        }

        int gallMap[] = new int[n + 1];

        getGallNumbers(n, gallMap);

        for (int x = n; x >= 1; x--) {
            int num = gallMap[x];
            //! System.out.println("Gall number of leaf "+x+" is "+num);
        }

        System.out.println("Step 2 : Performing the derandomization NOW...this can take a while...");

        int colourToLeaf[] = new int[n + 1];
        int leafToColour[] = new int[n + 1];

        for (int x = 0; x < colourToLeaf.length; x++) {
            colourToLeaf[x] = 0;
            leafToColour[x] = 0;
        }

        //! 0 means, not done yet...

        //! The leaf and gall index gets higher as you go upwards...

        double finalExpectation = (double) -1.0;

        //! We assign leaf labels from top to bottom
        for (int leaf = n; leaf >= 1; leaf--) {
            //! We can assume that all higher leaves already have a colour

            //! System.out.println("About to search for a colour for leaf "+leaf);

            int bestCol = -10;
            double bestExpectation = (double) 0.0;

            //! Just run through the colours, ignore those already assigned...
            for (int col = 1; col <= n; col++) {
                //! System.out.println("colourToleaf["+col+"] = "+colourToLeaf[col]);

                if (colourToLeaf[col] != 0) continue;

                //! System.out.println("Computing expectation for colour "+col);

                //! Ok, so we're going to assign colour col to leaf leaf :)

                leafToColour[leaf] = col;
                colourToLeaf[col] = leaf;

                Enumeration tripset = t.elements();

                double expectation = (double) 0.0;

                double noneDoneRatio = (double) -1.0;

                triploop:
                while (tripset.hasMoreElements()) {
                    int trip[] = (int[]) tripset.nextElement();

                    int acol = trip[0];
                    int bcol = trip[1];
                    int ccol = trip[2];

                    int w = t.getTripletWeight(acol, bcol, ccol);

                    //! System.out.println("Looking at triplet "+acol+" "+bcol+" "+ccol+" with weight "+w);


                    //! case: all colours allocated -----------------------------------------------------------

                    if ((colourToLeaf[acol] != 0) && (colourToLeaf[bcol] != 0) && (colourToLeaf[ccol] != 0)) {
                        //! All colours of this triplet have already been fully allocated to leaves;

                        if (CNconsistent(colourToLeaf[acol], colourToLeaf[bcol], colourToLeaf[ccol], gallMap))
                            expectation += (double) w;
                        continue triploop;
                    }

                    //! case: no colours allocated ------------------------------------------------------------

                    if ((colourToLeaf[acol] == 0) && (colourToLeaf[bcol] == 0) && (colourToLeaf[ccol] == 0)) {
                        if (noneDoneRatio != -1.0) {
                            expectation += (noneDoneRatio * (double) w);
                            continue triploop;
                        }

                        int denom = 0;
                        int numerator = 0;

                        for (int al = (leaf - 1); al >= 1; al--)
                            for (int bl = (leaf - 1); bl >= 1; bl--)
                                for (int cl = (leaf - 1); cl >= 1; cl--) {
                                    if ((al == bl) || (bl == cl) || (al == cl)) continue;

                                    denom++;

                                    if (CNconsistent(al, bl, cl, gallMap)) numerator++;
                                }

                        noneDoneRatio = ((double) numerator) / ((double) denom);

                        expectation += (noneDoneRatio * (double) w);

                        continue triploop;
                    }

                    //! case one colour allocated... ---------------------------------------------------------

                    boolean oneMissing = false;

                    int index = -1;

                    if ((colourToLeaf[acol] == 0) && (colourToLeaf[bcol] > 0) && (colourToLeaf[ccol] > 0)) {
                        index = acol;
                        oneMissing = true;
                    }

                    if ((colourToLeaf[acol] > 0) && (colourToLeaf[bcol] == 0) && (colourToLeaf[ccol] > 0)) {
                        index = bcol;
                        oneMissing = true;
                    }

                    if ((colourToLeaf[acol] > 0) && (colourToLeaf[bcol] > 0) && (colourToLeaf[ccol] == 0)) {
                        index = ccol;
                        oneMissing = true;
                    }

                    if (oneMissing) {
                        int denom = 0;
                        int numerator = 0;

                        for (int l = (leaf - 1); l >= 1; l--) {
                            denom++;

                            colourToLeaf[index] = l;

                            if (CNconsistent(colourToLeaf[acol], colourToLeaf[bcol], colourToLeaf[ccol], gallMap)) {
                                numerator++;
                            }

                            colourToLeaf[index] = 0;
                        }

                        expectation += w * (((double) numerator) / ((double) denom));

                        continue triploop;
                    }

                    //! remaining case: two colours missing....

                    //! System.out.println("Two colours missing...");

                    int miss1 = 0;
                    int miss2 = 0;

                    if (colourToLeaf[acol] > 0) {
                        miss1 = bcol;
                        miss2 = ccol;
                    } else if (colourToLeaf[bcol] > 0) {
                        miss1 = acol;
                        miss2 = ccol;
                    } else if (colourToLeaf[ccol] > 0) {
                        miss1 = acol;
                        miss2 = bcol;
                    } else {
                        System.out.println("Something went wrong!");
                        System.exit(0);
                    }

                    int numerator = 0;
                    int denom = 0;

                    for (int al = leaf - 1; al >= 1; al--)
                        for (int bl = leaf - 1; bl >= 1; bl--) {
                            if (bl == al) continue;

                            //! System.out.println("Trying position "+al+", "+bl);

                            denom++;

                            colourToLeaf[miss1] = al;
                            colourToLeaf[miss2] = bl;


                            //! System.out.println("Mapping colour "+acol+" to leaf "+colourToLeaf[acol]);
                            //! System.out.println("Mapping colour "+bcol+" to leaf "+colourToLeaf[bcol]);
                            //! System.out.println("Mapping colour "+ccol+" to leaf "+colourToLeaf[ccol]);


                            if (CNconsistent(colourToLeaf[acol], colourToLeaf[bcol], colourToLeaf[ccol], gallMap)) {
                                numerator++;
                                //!System.out.println("YES!");
                            }

                            colourToLeaf[miss1] = 0;
                            colourToLeaf[miss2] = 0;
                        }
                    expectation += w * (((double) numerator) / ((double) denom));
                } // end triplet loop

                colourToLeaf[col] = 0;
                leafToColour[leaf] = 0;

                if (expectation >= bestExpectation) {
                    //! System.out.println("New bestExpectation found for leaf "+leaf+" at colour "+col+" :  "+expectation);
                    bestCol = col;
                    bestExpectation = expectation;
                }
            } // end colour loop

            //! Finally update the ltC and cTL entries with the best one

            colourToLeaf[bestCol] = leaf;
            leafToColour[leaf] = bestCol;

            //! System.out.println("Finishing leaf "+leaf+" it will be mapped to "+bestCol);

            finalExpectation = bestExpectation;    //! This is only needed at the very end
        } // end leaf loop

        //! All done

        System.out.println("Finished derandomization. These are the leaves, from top to bottom:\n");

        for (int x = n; x >= 1; x--) {
            if ((x != n)) {
                if (gallMap[x] != gallMap[x + 1]) System.out.println("----- reticulation -----");
            }

            System.out.println(Heuristic.getLeafName(leafToColour[x]));
        }

        return finalExpectation;    //! will be an int, assuming all the weights are int...
    }


    //! I think this is enough!

    //! a, b, c refer to LEAVES not to COLOURS
    public static boolean CNconsistent(int a, int b, int c, int gm[]) {


        int agall = gm[a];
        int bgall = gm[b];
        int cgall = gm[c];

        //! case: same gall

        //! System.out.println("leaf x ="+a);
        //! System.out.println("leaf y ="+b);
        //! System.out.println("leaf z ="+c);

        //! System.out.println("agall = "+gm[a]);
        //! System.out.println("bgall = "+gm[b]);
        //! System.out.println("cgall = "+gm[c]);

        if ((agall == bgall) && (bgall == cgall)) {
            if ((c > b) && (c > a)) return true;
            return false;
        }

        //! case agall = bgall

        if (agall == bgall) return true;

        //! case bgall = cgall

        if ((bgall == cgall) && (agall > bgall)) return false;

        if ((bgall == cgall) && (b < c)) return true;

        if (bgall == cgall) return false;

        //! case agall = cgall

        if ((agall == cgall) && (bgall > agall)) return false;

        if ((agall == cgall) && (a < c)) return true;

        if ((agall == cgall)) return false;

        //! case all in different galls...

        if ((cgall > agall) && (cgall > bgall)) return true;

        return false;
    }


    public static void getGallNumbers(int n, int array[]) {
        //! first compute how many galls there are

        int left = n;
        int galls = 0;

        while (left > 2) {
            galls++;

            left = left - topgall[left];
        }

        //! Now we're going to fill in all those numbers...

        int at = n;

        while (at > 0) {
            int count = topgall[at];

            for (int af = 1; af <= count; af++) {
                array[at] = galls;
                at--;
            }
            galls--;
        }
    }


    public static long choose(long n, long m) {
        if (m == n) return 1;
        if (m > n) return 0;

        if (m == 2) {
            return (n * (n - 1)) / 2;
        }
        if (m == 3) {
            return (n * (n - 1) * (n - 2)) / 6;
        }

        long answer = fac(n) / (fac(m) * fac(n - m));
        return answer;
    }

    public static long fac(long n) {
        if (n == 0) return 0;
        if (n == 1) return 1;

        return n * fac(n - 1);
    }


    public static long theirBest(long n) {
        //! System.out.prlongln("Entering theirBest("+n+")");
        if (n == 3) {
            bestA = 2;
            topgall[3] = 2;
            return 2;
        }

        if (n < 3) return 0;

        long max = 0;
        int maxA = -1;

        for (int a = 1; a <= n; a++) {
            long sum = choose(a, 3);
            sum += 2 * choose(a, 2) * (n - a);
            sum += a * choose(n - a, 2);

            sum += snarchive[(int) (n - a)];

            if (sum > max) {
                //! System.out.prlongln("In theirBest("+n+"), best a is "+a);
                max = sum;
                maxA = a;
            }
        }

        bestA = maxA;

        topgall[(int) n] = bestA;

        return max;

    }


}


