package ca.nrc.cadc.dlm;

import ca.nrc.cadc.dali.util.ShapeFormat;
import java.util.Comparator;

/**
 * Used to compare DownloadTuples and allow TreeSet to understand what a duplicate is
 */
class DownloadTupleComparator implements Comparator<DownloadTuple> {

    DownloadTupleComparator() {}

    @Override
    public int compare(DownloadTuple lhs, DownloadTuple rhs) {
        // Start with comparing URIs (tupleIDs)
        int ret = lhs.getID().compareTo(rhs.getID());
        if (ret != 0) {
            return ret;
        }

        // URIs are equal, move on to cutout - careful with null
        if (lhs.cutout == null && rhs.cutout == null) {
            ret = 0; // equal, continue to compare
        }
        if (lhs.cutout == null && rhs.cutout != null) {
            return -1; // null before not null
        }
        if (lhs.cutout != null && rhs.cutout == null) {
            return 1;
        }
        if (lhs.cutout != null && rhs.cutout != null) {
            // both cutouts non-null: compare values
            ShapeFormat sf = new ShapeFormat();
            String lhsCutout = sf.format(lhs.cutout);
            String rhsCutout = sf.format(rhs.cutout);

            ret = lhsCutout.compareTo(rhsCutout);
            if (ret != 0) {
                return ret;
            }
        }

        // finally compare labels
        if (lhs.label == null && rhs.label == null) {
            // assert: the rest of the DownloadTuple has been equal
            // up to this point, return 0
            return 0;
        }
        if (lhs.label == null && rhs.label != null) {
            return -1; // null before not null
        }
        if (lhs.label != null && rhs.label == null) {
            return 1;
        }

        // if the code gets this far, everything else is the same and not null
        // so final value is label comparison
        return (lhs.label.compareTo(rhs.label));
    }
}
