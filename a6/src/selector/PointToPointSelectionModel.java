package selector;

import java.awt.Point;
import java.util.ListIterator;

/**
 * Models a selection tool that connects each added point with a straight line.
 */
public class PointToPointSelectionModel extends SelectionModel {

    public PointToPointSelectionModel(boolean notifyOnEdt) {
        super(notifyOnEdt);
    }

    public PointToPointSelectionModel(SelectionModel copy) {
        super(copy);
    }

    /**
     * Return a straight line segment from our last point to `p`.
     */
    @Override
    public PolyLine liveWire(Point p) {
        return new PolyLine(lastPoint(), p);
    }

    /**
     * Append a straight line segment to the current selection path connecting its end with `p`.
     */
    @Override
    protected void appendToSelection(Point p) {
        selection.add(liveWire(p));
    }

    /**
     * Move the starting point of the segment of our selection with index `index` to `newPos`,
     * connecting to the end of that segment with a straight line and also connecting `newPos` to
     * the start of the previous segment (wrapping around) with a straight line (these straight
     * lines replace both previous segments).  Notify listeners that the "selection" property has
     * changed.
     */
    @Override
    public void movePoint(int index, Point newPos) {
        // Confirm that we have a closed selection and that `index` is valid
        if (state() != SelectionState.SELECTED) {
            throw new IllegalStateException("May not move point in state " + state());
        }
        if (index < 0 || index >= selection.size()) {
            throw new IllegalArgumentException("Invalid segment index " + index);
        }

        ListIterator<PolyLine> iterator = selection.listIterator();

        while (iterator.hasNext()) {
            int thisIndex = iterator.nextIndex();
            PolyLine current = iterator.next();
            if (thisIndex == index-1) { //if index is 0, this condition will never be met
                PolyLine newCurrent = new PolyLine(current.start(), newPos);
                iterator.set(newCurrent);
            }
            if (thisIndex == index) {
                PolyLine newCurrent = new PolyLine(newPos, current.end());
                iterator.set(newCurrent);
                //handle edge case where index is 0 (need to adjust both first segment and last segment)
                if (index == 0) {
                    start = new Point(newPos);
                    while (iterator.hasNext()) {
                        current = iterator.next(); //wrap around to end after updating first segment
                    }
                    PolyLine newLast = new PolyLine(current.start(), new Point(newPos));
                    iterator.set(newLast);
                }
                break;
            }
        }
        propSupport.firePropertyChange("selection", null, selection());
    }
}