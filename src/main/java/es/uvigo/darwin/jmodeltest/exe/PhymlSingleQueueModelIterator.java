package es.uvigo.darwin.jmodeltest.exe;

import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Jose Manuel Santorum, University of A Coruña
 *         jose.santorum@udc.es
 *
 */

public class PhymlSingleQueueModelIterator implements Iterator<PhymlSingleQueueModel> 
{
	private int							cursor = 0;
    private List<PhymlSingleQueueModel>	values;

    public PhymlSingleQueueModelIterator(List<PhymlSingleQueueModel> values)
    {
    	this.values = values;
    }

    @Override
    public boolean hasNext()
    {
    	return cursor < values.size();
    }

    @Override
    public PhymlSingleQueueModel next()
    {
    	if (hasNext()) return values.get(cursor++);
    	else throw new IllegalStateException();
    }

    @Override
    public void remove()
    {
    	values.remove(--cursor);
    }
}
