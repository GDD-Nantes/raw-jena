package fr.gdd.sage.io;

/**
 * Objects being return to the client.
 */
public class RAWOutput {

    Integer nbScans = 0;

    public RAWOutput() {}

    /**
     * Another scan has been performed on a {@link org.apache.jena.dboe.trans.bplustree.BPlusTree}.
     */
    public void addScan() {
        nbScans += 1;
    }

    public Integer getNbScans() {
        return nbScans;
    }

}
